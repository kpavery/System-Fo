// CS290C: Implementing Linear Types in System Fo
//
// Keith Avery, Winter 2015
//
// Parser
//

package systemfo

import scala.util.parsing.combinator.PackratParsers
import scala.util.parsing.combinator.syntactical.StandardTokenParsers
import scala.util.parsing.combinator.lexical.StdLexical

// Scala StdLexical treats lambda as a letter, which causes λx to be treated as one identifier.
// Override and create a new Lexical which handles lambda (both upper and lower cases) correctly.
class LambdaLexical extends StdLexical {
	override def letter = elem("letter", c => c.isLetter && c != 'λ' && c != 'Λ')
}

// The parser. Uses syntax rules on page 3 of Mazurak, et al. 2010.
object Parser extends StandardTokenParsers with PackratParsers {
	type P[T] = PackratParser[T]
	// Use lambda lexer - see above
	override val lexical = new LambdaLexical
	lexical.delimiters += ("λ", "Λ", "→", "∀", ":", ".", "(", ")", "[", "]", "*", "⭑", "○")

	def parse(program:String) = {
		val lexer = new lexical.Scanner(program)
		val result = phrase(expression)(lexer)

		result match {
			case Success(ast, _) => Some(ast)
			case NoSuccess(message, next) => {
				// Print full error message with position
				println("Parse error: " + message)
				println("At line " + next.pos.line + ", column " + next.pos.column)
				println(next.pos.longString)
				None
			}
		}
	}

	// Rule e -> x | lambda^k x:t . e | e e | Lambda a:k . v | e [t] | (e)
	// Parenthesized expressions are added for convenience
	// Recursively use value rule to avoid duplication
	lazy val expression: P[Expression] = variable | value | application | typeapplication | parenthesized
	// Rule v -> lambda^k x:t . e | Lambda a:k . v
	lazy val value: P[Value]           = lambda | upperlambda

	// Rule e -> x
	lazy val variable          = ident ^^ { case x => Variable(x) }
	// Rule e -> e e
	lazy val application       = "(" ~ expression ~ ")" ~ expression ^^ { case _ ~ first ~ _ ~ second => Application(first, second) }
	// Rule e -> e [t]
	lazy val typeapplication   = expression ~ "[" ~ typerule ~ "]" ^^ { case e ~ _ ~ t ~ _ => TypeApplication(e, t) }
	// New Rule e -> (e)
	lazy val parenthesized     = "(" ~ expression ~ ")" ^^ { case _ ~ e ~ _ => e }

	// Rule e/v -> lambda^k x:t . e
	lazy val lambda            = "λ" ~ kind ~ ident ~ ":" ~ typerule ~ "." ~ expression ^^ { case _ ~ k ~ x ~ _ ~ t ~ _ ~ e => ValueLambda(k, x, t, e) }
	// Rule e/v -> Lambda a:k . v
	lazy val upperlambda       = "Λ" ~ ident ~ ":" ~ kind ~ "." ~ value ^^ { case _ ~ a ~ _ ~ k ~ _ ~ v => TypeLambda(a, k, v) }

	// Rule k -> ⭑ | ○
	lazy val kind              = "*" ^^^ Unrestricted() | "⭑" ^^^ Unrestricted() | "○" ^^^ Linear()

	// Rule t -> a | t ->k t | forall a:k . t | (t)
	// Parenthesized types are added for convenience
	lazy val typerule: P[Type] = ( ("(" ~ typerule ~ ")" ^^ { case _ ~ t ~ _ => t })
	                             | (typerule ~ "→" ~ kind ~ typerule ^^ { case t1 ~ _ ~ k ~ t2 => KindArrow(t1, t2, k) })
	                             | ("∀" ~ ident ~ ":" ~ kind ~ "." ~ typerule ^^ { case _ ~ a ~ _ ~ k ~ _ ~ t => ForAll(a, k, t) })
	                             | (ident ^^ { case a => NamedType(a) })
	                             )
}
