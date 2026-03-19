package edu.uob;

import edu.uob.ds.Table;
import edu.uob.nodes.Stmt;
import edu.uob.parse.Lexer;
import edu.uob.parse.Parser;
import edu.uob.parse.Token;
import edu.uob.visitors.ExecuteVisitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CommandTests {

    private DBServer server;
    private Lexer lexer;

    @BeforeEach
    void setUp() {
        server = new DBServer();
        lexer = new Lexer();
    }

    private Table execute(String sql) {
        List<Token> tokens = lexer.tokenize(sql);
        Stmt stmt = new Parser(tokens).parse();
        return stmt.accept(new ExecuteVisitor(server));
    }

    @Test
    void testUseCommand() {
        execute("USE mydb;");
        assertEquals("mydb", server.getDatabaseName());
    }
}