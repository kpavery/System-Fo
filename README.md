# System Fo
An implementation of a parser and type checker for a "lightweight" linear type system that extends System F. The system was introduced in "Lightweight Linear Types in System F◦" by _Karl Mazurak_, _Jianzhou Zhao_, and _Steve Zdancewic_.

**Scala** is used as the implementation language. Parser combinators are used to handle parsing.

### Requirements

The implementation was designed with the following tools, which are necessary to build and execute the type checker.

- **Scala 2.11.5**. Other versions of Scala may or may not work correctly.
- **sbt 0.13.7**
- Java 1.7
- Mac OS X 10.10.2

### Build and Run

To build the code:
```
sbt compile
```
To run the code on a test case (`tests/affine.fo` in this example):
```
sbt 'run tests/affine.fo'
```
The program outputs the AST and either the type of the whole expression, or a message stating that the expression is not well-typed.

To run the program in interactive mode (a "RTcPL"):
```
sbt run
```
The program will wait for input, and then parse and type check each line given on standard input.

### Structure

`ast.scala` contains the definition of the AST nodes for the System Fo syntax.  
`main.scala` contains the main driver.  
`parser.scala` contains the System Fo parser, which utilizes Scala parser combinators.  
`typechecker.scala` contains the System Fo type checker. This is the main interest of the implementation.

`tests/` is a folder containing a few fairly simple test cases to demonstrate the type checker.  
`tests/affine.fo` demonstrates the use of affine types for function parameters.  
`tests/curry.fo` demonstrates affine function currying.  
`tests/linear-mixed.fo` demonstrates a type checking failure when linear and affine types are mixed in a way which violates the typing rules.  
`tests/linear-simple.fo` demonstrates a linear (consuming) generic identity function.  
`tests/linear-twice.fo` demonstrates a type checking failure when using a linearly typed variable twice.  
`tests/simple.fo` demonstrates an affine (non-consuming) generic identity function.  
`tests/tapp.fo` demonstrates type application.  
`tests/type.fo` demonstrates more complicated parameter types and type application combined.

The `.fo` file extension is used for System Fo source files.

### Syntax

The syntax for System Fo can be found at the top-left of page 3 of the above mentioned paper. The implementation includes annotation showing where each grammar rule is handled.

Two additional rules are added for greatly increased clarity:
- Parenthesized expressions
- Parenthesized types

And one rule is modified:
- Function application has added parentheses around the function. For example: `(λx:⭑.x) 4`

An image showing the Typing and kinding rules is below.  
<img src="https://raw.githubusercontent.com/kpavery/System-Fo/master/rules/syntax.png" alt="Syntax Rules" height="180" />


### Typing and Kinding Rules

The typing and kinding rules for System Fo can be found at the top of page 4 of the above mentioned paper. The implementation includes annotation showing where each typing or kinding rule is important, excluding the set operation definitions.

An image showing the Typing and kinding rules is below.  
<img src="https://raw.githubusercontent.com/kpavery/System-Fo/master/rules/typing-kinding.png" alt="Typing and Kinding Rules" height="300" />
