// CS290C: Implementing Linear Types in System Fo
//
// Keith Avery, Winter 2015
//
// Driver
//

import scala.io.Source

import java.io.FileNotFoundException
import java.io.IOException

import systemfo._

object SystemFO {
	// Entry point
	def main(args:Array[String]) {
		if (args.length > 0) {
			// Usage case: scala SystemFO source.fo
			try {
				// Parse and type check entire file
				val source = Source.fromFile(args(0)).mkString
				check(source) match {
					case Some(t) => println("Program has type: " + t.pretty)
					case None    => println("Program is not well-typed.")
				}
			} catch {
				case e : FileNotFoundException => println("File not found.")
				case e : IOException           => println("File open failed.")
				case e : Exception             => println("Error: " + e)
			}
		} else {
			// Usage case: scala SystemFO
			// Read each line from the standard input and parse and type check it
			// Similar to a REPL, except RTcPL
			for (line <- Source.stdin.getLines) {
				check(line) match {
					case Some(t) => println("Line has type: " + t.pretty)
					case None    => println("Line is not well-typed.")
				}
			}
		}
	}

	// Check function takes in source code as string and returns an optional type.
	// Uses the TypeChecker object to type check the program and the Parser object
	// to parse the program.
	def check(source:String) : Option[Type] = {
		// Verify the parse first
		val ast = Parser.parse(source)
		ast match {
			case Some(ast) => {
				// Print the AST and type check
				println("AST: " + ast)
				println("")
				val programtype = TypeChecker.check(ast)
				programtype match {
					case n@Some(t) => n
					case None      => None
				}
			}

			case None      => {
				println("Parsing Failed.")
				None
			}
		}
	}
}
