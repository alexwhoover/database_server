package edu.uob;

import edu.uob.nodes.Expr;
import edu.uob.nodes.Stmt;
import edu.uob.parse.Lexer;
import edu.uob.parse.Parser;
import edu.uob.parse.TokenStream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ParserTests {
    private Lexer lexer;
    private Parser parser;

    @BeforeEach
    public void setUp() {
        lexer = new Lexer();
        parser = new Parser();
    }

    private Stmt getStmt(String cmd) {
        TokenStream stream = new TokenStream(lexer.tokenize(cmd));
        return parser.parse(stream);
    }

    @Test
    public void testUseCommand() {
        Stmt actual = getStmt("USE myDB;");
        Stmt expected = new Stmt.Use("myDB");
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateDatabaseCommand() {
        Stmt actual = getStmt("CREATE DATABASE mydb;");
        Stmt expected = new Stmt.CreateDatabase("mydb");
        assertEquals(expected, actual);
    }

    @Test
    public void testSelectCommandWithoutWhere() {
        Stmt actual = getStmt("SELECT id, mark FROM students;");

        // Create Expected Output
        Stmt expected = new Stmt.Select(
            "students",
                List.of("id", "mark"),
                null
        );

        assertEquals(actual, expected);
    }

    @Test
    public void testSelectCommandWithWhere() {
        Stmt actual = getStmt("SELECT id, mark FROM students WHERE age >= 25 AND (name == 'Alex');");
        Expr condition = new Expr.Binary(
            new Expr.Binary(
                new Expr.Attr("age"),
                Expr.Binary.Op.GTE,
                new Expr.Literal("25")
            ),
            Expr.Binary.Op.AND,
            new Expr.Binary(
                new Expr.Attr("name"),
                Expr.Binary.Op.EQ,
                new Expr.Literal("Alex")
            )
        );

        // Create Expected Output
        Stmt expected = new Stmt.Select(
                "students",
                List.of("id", "mark"),
                condition
        );

        assertEquals(actual, expected);
    }
}
