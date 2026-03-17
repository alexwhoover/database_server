package edu.uob;

import edu.uob.parse.Lexer;
import edu.uob.parse.Parser;
import org.junit.jupiter.api.BeforeEach;

public class ParserTests {
    private Lexer lexer;
    private Parser parser;

    @BeforeEach
    void setUp() {
        Lexer lexer = new Lexer();
        parser = new Parser();
    }
}
