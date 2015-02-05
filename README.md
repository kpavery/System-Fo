# System Fo
An implementation of a parser and type checker for a "lightweight" linear type system that extends System F. The system was introduced in "Lightweight Linear Types in System F◦" by _Karl Mazurak_, _Jianzhou Zhao_, and _Steve Zdancewic_.

**Scala** is used as the implementation language. Parser combinators are used to handle parsing.

### Requirements

The implementation was designed with the following tools, which are necessary to build and execute the type checker.

- **Scala 2.11.5**. Other versions of Scala may or may not work correctly.
- **sbt 0.13.7**
- Java 1.7
- Mac OS X 10.10.2

If you use Homebrew:
```
brew install sbt scala
```

### Build and Run

To build the code:
```
sbt compile
```
To run the code on a test case (`tests/unrestricted.fo` in this example):
```
sbt 'run tests/unrestricted.fo'
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
`tests/badargument.fo` demonstates a type error when a lambda function argument has an incorrect type.  
`tests/call-nonfunction.fo` demonstrates a type error when a non-function is applied to an argument.  
`tests/curry.fo` demonstrates unrestricted function currying.  
`tests/curry-unused.fo` demonstrates a type error when a linear parameter is unused in linear function currying.  
`tests/duplicate.fo` demonstrates a type error when a name is used more than once.  
`tests/linear-mixed.fo` demonstrates a type error when linear and unrestricted types are mixed in a way which violates the typing rules.  
`tests/linear-ordered.fo` demonstrates a type error when an unrestricted variable is used without using all linear variables first.  
`tests/linear-simple.fo` demonstrates a linear (consuming) generic identity function.  
`tests/linear-twice.fo` demonstrates a type error when using a linearly typed variable twice.  
`tests/linear-unused.fo` demonstrates a type error when applying a function without using a linearly typed variable.  
`tests/simple.fo` demonstrates an unrestricted (non-consuming) generic identity function.  
`tests/tapp.fo` demonstrates type application.  
`tests/type.fo` demonstrates more complicated parameter types and type application combined.
`tests/unrestricted.fo` demonstrates the use of unrestricted types for function parameters.  

The `.fo` file extension is used for System Fo source files.

### Syntax

The syntax for System Fo can be found at the top-left of page 3 of the above mentioned paper. The implementation includes annotation showing where each grammar rule is handled.

Two additional rules are added for greatly increased clarity:
- Parenthesized expressions
- Parenthesized types

And one rule is modified:
- Function application has added parentheses around the function. For example: `(λx:⭑.x) 4`

An image showing the syntax rules is below.

<img src="https://raw.githubusercontent.com/kpavery/System-Fo/master/rules/syntax.png" alt="Syntax Rules" height="155" />


### Kinding and Typing Rules

The kinding and typing rules for System Fo can be found at the top of page 4 of the above mentioned paper. The implementation includes annotation showing where each typing or kinding rule is important, excluding the set operation definitions.

An image showing the kinding and typing rules is below.

<img src="https://raw.githubusercontent.com/kpavery/System-Fo/master/rules/typing-kinding.png" alt="Kinding and Typing Rules" height="259" />
