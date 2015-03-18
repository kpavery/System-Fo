// CS290C: Implementing Linear Types in System Fo
//
// Keith Avery, Winter 2015
//
// Errors: General, Parse, and Type Check
//

package systemfo

sealed abstract class Error(message:String) {
	override def toString(): String = { "Error: " + message + "."}
}
case class ParseError(message:String) extends Error(message) {
	override def toString(): String = { "Parse Error:\n" + message}
}
case class TypeError(message:String) extends Error(message) {
	override def toString(): String = { "Type Error: " + message + "."}
}
