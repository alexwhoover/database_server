# SQL Database Server

A from-scratch SQL parser and query execution engine written in Java. Raw SQL strings are lexed into tokens, parsed into a typed AST, and then executed against a persistent flat-file store - no external parsing libraries used.

---

## Table of Contents

- [Overview](#overview)
- [Key Technical Features](#key-technical-features)
  - [Lexical Analysis](#lexical-analysis)
  - [Recursive Descent Parser](#recursive-descent-parser)
  - [Custom AST Architecture](#custom-ast-architecture)
  - [Recursive WHERE Expressions](#recursive-where-expressions)
- [Design Patterns](#design-patterns)
  - [Visitor Pattern](#visitor-pattern)
- [Engineering Challenges](#engineering-challenges)
- [Tech Stack](#tech-stack)
- [Installation & Usage](#installation--usage)

---

## Overview

This project implements a subset of SQL from first principles: a hand-written lexer, a recursive descent parser, a typed AST, and a visitor-based execution engine. Queries are served over a TCP socket and run against a tab-separated flat-file store, with support for `SELECT`, `INSERT`, `UPDATE`, `DELETE`, `CREATE`, `DROP`, `ALTER`, and `JOIN`.

The main design goals were correctness and a clean separation between parsing and execution - achieved through a dual-visitor architecture over two independent node hierarchies.

The exact BNF grammar for the subset of SQL this project uses can be found in assets/BNF.txt.

---

## Key Technical Features

### Lexical Analysis

The `Lexer` does a single pass over the raw SQL string and classifies each character sequence into one of 35 `Token.TokenType` variants: keywords (`SELECT`, `WHERE`, `JOIN`, …), operators (`==`, `!=`, `<=`, `>=`), literals (integer, float, string, boolean, `NULL`), and identifiers.

Rather than using regex, the lexer is a hand-coded state machine. Multi-character operators (`<=`, `>=`, `!=`, `==`) are handled by peeking one character ahead. String literals delimited by single quotes are extracted as a contiguous substring, with an explicit error thrown for unterminated strings.

The resulting `List<Token>` is wrapped in a `TokenStream`, which provides cursor-based `peek()`, `consume()`, and `expect()` methods for the parser to use.

```java
// Tokenization entry point in DBServer.handleCommand
TokenStream stream = new TokenStream(new Lexer().tokenize(command));
Stmt stmt = new Parser().parse(stream);
return stmt.accept(new ExecuteStmtVisitor(this));
```

### Recursive Descent Parser

The `Parser` is a top-down recursive descent parser. The public entry point `parse(TokenStream)` dispatches to one of eleven statement-specific sub-parsers based on the first token:

```
USE | CREATE | DROP | ALTER | INSERT | SELECT | UPDATE | DELETE | JOIN
```

Each sub-parser consumes exactly the tokens defined by the grammar for that statement and builds an immutable `Stmt` node. Any token mismatch throws a `ParseException` with a descriptive message.

### Custom AST Architecture

The AST is split across two independent hierarchies, implemented as **static inner classes** on abstract base types.
![AST-class-diagram](https://github.com/user-attachments/assets/4a15b9a5-eff3-4878-a219-3670aa742fa8)
#### `Stmt` - statement nodes

Represents a complete SQL command. All fields are `public final`, making nodes immutable. There are eleven concrete subtypes, one per supported command:

| Node | Key Fields |
|---|---|
| `Stmt.Use` | `dbName` |
| `Stmt.CreateDatabase` / `DropDatabase` | `dbName` |
| `Stmt.CreateTable` / `DropTable` | `tableName`, `attributeList` |
| `Stmt.Alter` | `tableName`, `altType` (`ADD`/`DROP`), `attributeName` |
| `Stmt.Insert` | `tableName`, `List<Expr.Literal> valueList` |
| `Stmt.Select` | `tableName`, `attributeList`, `Expr condition` |
| `Stmt.Update` | `tableName`, `List<NameValuePair>`, `Expr condition` |
| `Stmt.Delete` | `tableName`, `Expr condition` |
| `Stmt.Join` | `table1Name`, `table2Name`, `attr1Name`, `attr2Name` |

#### `Expr` - expression nodes

Represents a predicate or value within a statement (e.g. a `WHERE` clause). Three subtypes form a recursive tree:

- `Expr.Binary` - a binary operation with a left `Expr`, an `Op` (`AND`, `OR`, `EQ`, `NEQ`, `LT`, `GT`, `LTE`, `GTE`, `LIKE`), and a right `Expr`
- `Expr.Attr` - a column name reference
- `Expr.Literal` - a scalar value (string, integer, float, boolean, or `NULL`)

Every node implements `equals` and `hashCode` so nodes can be compared structurally in tests.

### Recursive WHERE Expressions

`WHERE` clauses are parsed by a pair of mutually recursive methods:

- `parseCondition()` - handles `AND`/`OR` chaining and optional parentheses. It calls itself recursively when a `(` is found, then consumes the closing `)`. Logical operators are left-associative and consumed in a `while` loop.
- `parseComparison()` - handles a leaf-level `attr op literal` triple, and similarly delegates recursively if another `(` is found.

This produces a recursive `Expr.Binary` tree that handles arbitrarily nested predicates:

```sql
SELECT * FROM marks WHERE (grade > 50 AND grade < 90) OR pass == TRUE;
```

```
Expr.Binary(OR)
├── Expr.Binary(AND)
│   ├── Expr.Binary(GT, Attr("grade"), Literal("50"))
│   └── Expr.Binary(LT, Attr("grade"), Literal("90"))
└── Expr.Binary(EQ, Attr("pass"), Literal("TRUE"))
```

---

## Design Patterns

### Visitor Pattern

Both node hierarchies (`Stmt`, `Expr`) declare a generic `accept(Visitor<T>)` method. All execution logic lives in visitor implementations, keeping the node classes as plain data structures with no business logic in them.

**`StmtVisitor<T>`** - 11 overloads, one per `Stmt` subtype.

**`ExprVisitor<T>`** - 3 overloads (`Binary`, `Attr`, `Literal`).

Two concrete visitors drive the system:

#### `ExecuteStmtVisitor implements StmtVisitor<String>`

The query execution engine. Each `visit` method contains the logic for its statement type - resolving table and database names case-insensitively against the filesystem, reading and mutating `Table` objects, and delegating condition evaluation to `PredicateExprVisitor`. Returns `[OK]` or `[ERROR] ...` strings to the client.

#### `PredicateExprVisitor implements ExprVisitor<BiPredicate<Integer, Row>>`

Translates an `Expr` tree into a Java `BiPredicate<Integer, Row>`. The visitor recurses through `Expr.Binary` nodes, combining predicates with `&&`/`||` for `AND`/`OR`, and wrapping leaf comparisons in lambdas that do type-aware evaluation (numeric comparison when both operands parse as `double`, lexicographic otherwise). The `id` pseudo-column is handled as a special case.

```java
// In ExecuteStmtVisitor.visit(Stmt.Select):
BiPredicate<Integer, Row> predicate = stmt.condition.accept(new PredicateExprVisitor());
Table filtered = raw.filter(predicate);
```

The upside of this architecture is that adding a new statement type is pretty mechanical: add a `Stmt` subclass, add an overload to `StmtVisitor`, implement it in `ExecuteStmtVisitor`.

---

## Engineering Challenges

### Generic Tree to a Typed Hierarchy

The initial design used a generic `Branch`/`Leaf` tree - a natural first approach that mirrors the recursive structure of a grammar. Every node was the same type, with children stored in untyped lists. This was quick to get working, but it became painful in the execution layer: every handler had to cast, inspect string tags, and manually check child counts, with no compile-time guarantee that a node was well-formed.

The real issue was that `Branch`/`Leaf` conflated two structurally different things: **statements** (which describe a specific command and carry named, typed fields) and **expressions** (which form a recursive predicate tree). A `SELECT` node and a `WHERE` subtree have nothing in common structurally, but both were put into the same representation.

The fix was to split them into two dedicated hierarchies:

- **`Stmt`** captures the shape of each SQL command as its own class with named, `final` fields. The parser constructs `Stmt.Select(tableName, attributeList, condition)` instead of `new Branch("SELECT", children)`. Field access is direct and typed - no casting, no child index arithmetic.

- **`Expr`** captures the recursive nature of predicates. `Expr.Binary` holds two `Expr` children, so nested conditions compose cleanly. `Expr.Attr` and `Expr.Literal` are typed leaves.

The benefit was most visible in the visitor layer. `ExecuteStmtVisitor.visit(Stmt.Select stmt)` gets a fully typed object - `stmt.condition` is either an `Expr` or `null`, not an ambiguous child node. `PredicateExprVisitor` can recurse through `Expr.Binary` with structural guarantees: `AND`/`OR` nodes always have two `Expr` children; comparisons always have an `Attr` on the left and a `Literal` on the right. Invariant violations are caught at parse time rather than execution time.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| Build | Maven (Maven Wrapper included) |
| Testing | JUnit Jupiter 5.8.2 |
| Persistence | Tab-separated flat files (custom `Reader`/`Writer`) |
| Transport | TCP socket server (`ServerSocket` on port 8888) |

---

## Installation & Usage

### Build

```bash
./mvnw clean package
```

### Run the server

```bash
./mvnw exec:java -Dexec.mainClass="edu.uob.DBServer"
```

The server listens on `localhost:8888`. Databases are persisted under the `databases/` directory.

### Run tests

```bash
./mvnw test
```

### Connect with the client

```bash
./mvnw exec:java -Dexec.mainClass="edu.uob.DBClient"
```

### Example session

```sql
CREATE DATABASE university;
USE university;
CREATE TABLE students (name, grade, pass);
INSERT INTO students VALUES ('Alice', 72, TRUE);
INSERT INTO students VALUES ('Bob', 45, FALSE);
SELECT * FROM students WHERE grade > 50;
-- [OK]
-- id    name    grade    pass
-- 1     Alice   72       TRUE
UPDATE students SET grade = 48 WHERE name == 'Bob';
SELECT name, grade FROM students WHERE (grade >= 45 AND grade < 60) OR pass == FALSE;
DROP TABLE students;
DROP DATABASE university;
```
