// CS290C: Implementing Linear Types in System Fo
//
// Keith Avery, Winter 2015
//
// AST
//

package systemfo

// Define classes for AST nodes. Allow kinds and types to have pretty printing capability
sealed abstract class Node

sealed abstract class Kind extends Node { def pretty = toString }
sealed abstract class Type extends Node { def pretty = toString }
sealed abstract class Expression extends Node
sealed abstract class Value extends Expression

// Define unrestricted and linear kinds, with pretty printing
case class Unrestricted() extends Kind {
	override def pretty = { "⭑" }
}
case class Linear() extends Kind {
	override def pretty = { "○" }
}

// Defined named, function, and forall types, with pretty printing
case class NamedType(a:String) extends Type {
	override def pretty = { a }
}
case class KindArrow(t1:Type, t2:Type, k:Kind) extends Type {
	override def pretty = { t1.pretty + " →" + k.pretty + " " + t2.pretty }
}
case class ForAll(a:String, k:Kind, t:Type) extends Type {
	override def pretty = { "∀" + a + ":" + k.pretty + ". " + t.pretty }
}

// Define expressions, not including both types of lambda expressions
case class Variable(x:String) extends Expression
case class Application(e1:Expression, e2:Expression) extends Expression
case class TypeApplication(e:Expression, t:Type) extends Expression

// Define values, which are the two lambda expressions
case class ValueLambda(k:Kind, x:String, t:Type, e:Expression) extends Value
case class TypeLambda(a:String, k:Kind, v:Value) extends Value
