package edu.uob;

import edu.uob.parse.Lexer;
import edu.uob.parse.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LexerTests {
    private Lexer lexer;

    @BeforeEach
    void setUp() {
        lexer = new Lexer();
    }

    private void assertToken(Token token, Token.TokenType expectedType, String expectedValue) {
        assertEquals(expectedType, token.getType());
        assertEquals(expectedValue, token.getValue());
    }

    @Test
    void testSelectCondition() {
        List<Token> tokens = lexer.tokenize("SELECT * FROM marks WHERE name != 'Sion';");
        assertToken(tokens.get(0), Token.TokenType.SELECT, "SELECT");
        assertToken(tokens.get(1), Token.TokenType.WILDCARD, "*");
        assertToken(tokens.get(2), Token.TokenType.FROM, "FROM");
        assertToken(tokens.get(3), Token.TokenType.IDENTIFIER,"marks");
        assertToken(tokens.get(4), Token.TokenType.WHERE, "WHERE");
        assertToken(tokens.get(5), Token.TokenType.IDENTIFIER, "name");
        assertToken(tokens.get(6), Token.TokenType.NEQ, "!=");
        assertToken(tokens.get(7), Token.TokenType.STRING_LITERAL, "Sion");
        assertToken(tokens.get(8), Token.TokenType.SEMICOLON, ";");
    }

    @Test
    void testAddedSpaces() {
        List<Token> tokens = lexer.tokenize("SELECT   (food,   beverages) FROM   items  WHERE   food == 'hot dogs'  OR food ==  'bananas';");
        assertToken(tokens.get(0), Token.TokenType.SELECT, "SELECT");
        assertToken(tokens.get(1), Token.TokenType.LPAREN, "(");
        assertToken(tokens.get(2), Token.TokenType.IDENTIFIER, "food");
        assertToken(tokens.get(3), Token.TokenType.COMMA, ",");
        assertToken(tokens.get(4), Token.TokenType.IDENTIFIER, "beverages");
        assertToken(tokens.get(5), Token.TokenType.RPAREN, ")");
        assertToken(tokens.get(6), Token.TokenType.FROM, "FROM");
        assertToken(tokens.get(7), Token.TokenType.IDENTIFIER,"items");
        assertToken(tokens.get(8), Token.TokenType.WHERE, "WHERE");
        assertToken(tokens.get(9), Token.TokenType.IDENTIFIER, "food");
        assertToken(tokens.get(10), Token.TokenType.EQ, "==");
        assertToken(tokens.get(11), Token.TokenType.STRING_LITERAL, "hot dogs");
        assertToken(tokens.get(12), Token.TokenType.OR, "OR");
        assertToken(tokens.get(13), Token.TokenType.IDENTIFIER, "food");
        assertToken(tokens.get(14), Token.TokenType.EQ, "==");
        assertToken(tokens.get(15), Token.TokenType.STRING_LITERAL, "bananas");
        assertToken(tokens.get(16), Token.TokenType.SEMICOLON, ";");
    }
}
