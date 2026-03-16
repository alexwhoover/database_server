package edu.uob.parse;

public class Token {
    public enum TokenType {
        // Single Character Tokens
        LPAREN, RPAREN, COMMA, SEMICOLON, WILDCARD,

        // Comparators
        EQ, NEQ, LTE, GTE, LT, GT, ASSIGN,

        // Literals
        INTEGER_LITERAL, FLOAT_LITERAL, BOOLEAN_LITERAL, STRING_LITERAL,

        // Keywords
        USE, CREATE, DATABASE, TABLE, DROP, ALTER, INSERT, INTO, VALUES,
        SELECT, FROM, WHERE, UPDATE, SET, DELETE, JOIN, AND, OR, ON, ADD,
        TRUE, FALSE, NULL, LIKE,

        // Identifier (name of a table, database, or attribute)
        IDENTIFIER
    }

    private final TokenType type;
    private final String value;

    public Token(TokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    public TokenType getType() { return type; }
    public String getValue() { return value; }

    @Override
    public String toString() {
        return "Token(" + type + ", " + value + ")";
    }
}
