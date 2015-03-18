// CS290C: Implementing Linear Types in System Fo
//
// Keith Avery, Winter 2015
//
// Type Checker
//

package systemfo

// Class that represents a typing context. The context is separated into an
// unrestricted typing context (gamma), a linear typing context (delta),
// and a kinding context (kinds).
case class Context(gamma:Map[String, Type], delta:Map[String, Type], kinds:Map[Type, Kind]) {
	// Function to get the kind of a type. Begins by trying to lookup the kind directly, and then
	// proceeds to try and use the TVAR, ALL, and ARR kinding rules. Returns an optional kind.
	def getKind(t:Type):Option[Kind] = {
		kinds.get(t) match {
			// Rule: K-TVAR
			case n@Some(k) => n

			case None      => {
				t match {
					// Rule: K-ALL
					case ForAll(a, k, tprime) => {
						val newcontext = Context(gamma, delta, kinds + (NamedType(a) -> k))
						newcontext.getKind(tprime)
					}

					// Rule: K-ARR
					case KindArrow(t1, t2, k) => {
						if (getKind(t1) != None && getKind(t2) != None) {
							Some(k)
						} else {
							None
						}
					}
					case _ => None
				}
			}
		}
	}
}

// The type checker. Uses typing and kinding rules on page 4 of Mazurak, et al. 2010.
object TypeChecker {
	// Entry point function to check a given AST node. It creates an empty context and calls the
	// real check function. Returns an optional type, which comes from the call.
	def check(node:Node):Either[Type, Error] = {
		val initial = Context(Map[String, Type](), Map[String, Type](), Map[Type, Kind]())
		check(node, initial)._1
	}

	// Function to perform the {a |-> t}t' substitution. Returns a new type with occurrences of a
	// in t' replaced by t.
	def replace(tprime:Type, a:String, t:Type) : Type = {
		tprime match {
			// Base case where we are substituting on a single named type
			case n@NamedType(x) => {
				if (x == a) {
					t
				} else {
					n
				}
			}

			// For function and forall types, recursively substitute
			case KindArrow(t1, t2, k) => KindArrow(replace(t1, a, t), replace(t2, a, t), k)
			case ForAll(x, k, t1) => ForAll(x, k, replace(t1, a, t))
		}
	}

	// Function to check a given AST node with a given typing context. The typing context consists of
	// an unrestricted context, a linear context, and a kinding context. Returns an optional type and
	// a new context.
	def check(node:Node, context:Context) : (Either[Type, Error], Context) = {
		node match {
			// Types already have a type
			case t:Type => (Left(t), context)

			// Rule: T-TLAM
			case TypeLambda(a, k, v) => {
				val at = NamedType(a)
				// Condition: a not in Gamma
				context.getKind(at) match {
					case None => {
						// Condition: Gamma, a:k; Delta derives v:t
						val newcontext = Context(context.gamma, context.delta, context.kinds + (at -> k))
						val (t, returnedcontext) = check(v, newcontext)
						t match {
							case Left(t) => {
								// Restore original kinds from original context 
								val finalcontext = Context(returnedcontext.gamma, returnedcontext.delta, context.kinds)
								(Left(ForAll(a, k, t)), finalcontext)
							}
							case n@Right(e) => (n, context)
						}
					}
					case Some(k) => (Right(TypeError("Type " + at.pretty + " has no kind")), context)
				}
			}

			// Rule: T-LAM
			case n@ValueLambda(k, x, t1, e) => {
				// Condition: (Delta = . ) or (k = linear)
				if (context.delta.isEmpty || k == Linear()) {
					// Condition: [Gamma; Delta], x:t_1 produces Gamma';Delta'
					context.getKind(t1) match {
						case Some(kt1) => {
							// Apply B-LIN or B-UN to produce new context
							// Condition: x not in Gamma, Delta
							context.delta.get(x) match {
								case None => {
									context.gamma.get(x) match {
										case None => {
											val newcontext = kt1 match {
												// Rule: B-LIN
												case Linear() => {
													Context(context.gamma, context.delta + (x -> t1), context.kinds)
												}
												// Rule B-UN
												case Unrestricted() => {
													Context(context.gamma + (x -> t1), context.delta, context.kinds)
												}
											}
											// Condition: Gamma';Delta' derives e:t_2
											val (t2, returnedcontext) = check(e, newcontext)

											// Apply B-LIN or B-UN to restore original context, but by subtracting added term.
											// This allows us to preserve the removal of linear terms removed *while* checking e.
											val finalcontext = kt1 match {
												// Rule: B-LIN
												case Linear() => {
													returnedcontext.delta.get(x) match {
														case None => Context(context.gamma, returnedcontext.delta - x, context.kinds)
														case Some(_) => return (Right(TypeError("Unused linear term " + x)), context)
													}
												}
												// Rule: B-UN
												case Unrestricted() => {
													Context(context.gamma - x, returnedcontext.delta, context.kinds)
												}
											}
											// Condition Continued: Gamma';Delta' derives e:t_2
											t2 match {
												case Left(t2)   => (Left(KindArrow(t1, t2, k)), finalcontext)
												case n@Right(e) => (n, finalcontext)
											}
										}
										case Some(_) => (Right(TypeError("Term " + x + " already exists in unrestricted context")), context)
									}
								}
								case Some(_) => (Right(TypeError("Term " + x + " already exists in linear context")), context)
							}

						}
						case None => (Right(TypeError("Type " + t1.pretty + " has no kind")), context)
					}
				} else {
					(Right(TypeError("Lambda has parameter of unrestricted type when linear context is non-empty")), context)
				}
			}

			// Rule: T-APP
			case Application(e1, e2) => {
				// Condition: Gamma;Delta_1 derives e_1:(t_1 ->k t_2)
				val (ft, functioncontext) = check(e1, context)
				// Condition: Gamma;Delta_2 derives e_2:t_1
				// Make sure to use new context, since Delta_1 and Delta_2 are a split of Delta.
				val (pt, argumentcontext) = check(e2, functioncontext)
				if (!argumentcontext.delta.isEmpty) {
					(Right(TypeError("Unused linear terms in T-APP")), context)
				} else {
					ft match {
						// Condition Continued: Gamma;Delta_1 derives e_1:(t_1 ->k t_2)
						case Left(KindArrow(t1, t2, k)) => {
							pt match {
								// Condition Continued: Gamma;Delta_2 derives e_2:t_1
								case Left(pt) => {
									if (t1 == pt) {
										(Left(t2), argumentcontext)
									} else {
										(Right(TypeError("Argument type " + pt.pretty + " does not match parameter type " + t1.pretty)), context)
									}
								}
								case n@Right(e) => (n, context)
							}
						}
						case n@Left(t)  => (Right(TypeError("First expression in T-APP does not have function type")), context)
						case n@Right(e) => (n, context)
					}
				}
			}

			// Rule: T-TAPP
			case TypeApplication(e, t) => {
				// Condition: Gamma derives t:k
				context.getKind(t) match {
					case Some(k1) => {
						// Condition: Gamma; Delta derives e:ForAll a:k . t'
						val (et, returnedcontext) = check(e, context)
						et match {
							case Left(ForAll(a, k2, tprime)) => {
								// Verify that both kappas are the same
								if (k1 == k2) {
									(Left(replace(tprime, a, t)), returnedcontext)
								} else {
									(Right(TypeError("Agument kind " + k1.pretty + " does not match forall bound kind " + k2.pretty)), context)
								}
							}
							case n@Left(t)  => (Right(TypeError("First expression in T-TAPP does not have forall type")), context)
							case n@Right(e) => (n, context)
						}
					}
					case None => (Right(TypeError("Type " + t.pretty + " has no kind")), context)
				}
			}

			case Variable(x) => {
				// Determine whether the variable is unrestricted
				context.gamma.get(x) match {
					// Rule: T-UVAR
					case Some(t) => {
						if (context.delta.isEmpty) {
							(Left(t), context)
						} else {
							(Right(TypeError("Unrestricted variable used with non-empty linear context")), context)
						}
					}
					// If the variable is not unrestricted, determine whether it is linear 
					case None => { 
						context.delta.get(x) match {
							// Rule: T-LVAR
							case Some(t) => {
								val newcontext = Context(context.gamma, context.delta - x, context.kinds)
								return (Left(t), newcontext)
							}
							// If the variable is neither unrestricted nor linear, it has no type
							case None => (Right(TypeError("Term " + x + " not found in either linear or unrestricted contexts")), context)
						}
					}
				}
			}

			// Other AST nodes have no type rules and therefore have no type
			case _ => (Right(TypeError("AST node has no typing rule")), context)
		}
	}
}
