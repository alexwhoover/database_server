package edu.uob.parse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lexer {
    private static final Map<String, Token.TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("USE",      Token.TokenType.USE);
        KEYWORDS.put("CREATE",   Token.TokenType.CREATE);
        KEYWORDS.put("DATABASE", Token.TokenType.DATABASE);
        KEYWORDS.put("TABLE",    Token.TokenType.TABLE);
        KEYWORDS.put("DROP",     Token.TokenType.DROP);
        KEYWORDS.put("ALTER",    Token.TokenType.ALTER);
        KEYWORDS.put("INSERT",   Token.TokenType.INSERT);
        KEYWORDS.put("INTO",     Token.TokenType.INTO);
        KEYWORDS.put("VALUES",   Token.TokenType.VALUES);
        KEYWORDS.put("SELECT",   Token.TokenType.SELECT);
        KEYWORDS.put("FROM",     Token.TokenType.FROM);
        KEYWORDS.put("WHERE",    Token.TokenType.WHERE);
        KEYWORDS.put("UPDATE",   Token.TokenType.UPDATE);
        KEYWORDS.put("SET",      Token.TokenType.SET);
        KEYWORDS.put("DELETE",   Token.TokenType.DELETE);
        KEYWORDS.put("JOIN",     Token.TokenType.JOIN);
        KEYWORDS.put("AND",      Token.TokenType.AND);
        KEYWORDS.put("OR",       Token.TokenType.OR);
        KEYWORDS.put("ON",       Token.TokenType.ON);
        KEYWORDS.put("ADD",      Token.TokenType.ADD);
        KEYWORDS.put("LIKE",     Token.TokenType.LIKE);
        KEYWORDS.put("TRUE",     Token.TokenType.TRUE);
        KEYWORDS.put("FALSE",    Token.TokenType.FALSE);
        KEYWORDS.put("NULL",     Token.TokenType.NULL);
    }

    private String input;
    private int pos;
    private final List<Token> tokens = new ArrayList<>();

    public TokenStream tokenize(String input) {
        this.input = input;
        this.pos = 0;
        tokens.clear();

        while (pos < input.length()) {
            skipWhitespace();
            if (pos >= input.length()) break;

            char c = input.charAt(pos);

            // Single Character Tokens
            if (c == '(') {
                tokens.add(new Token(Token.TokenType.LPAREN, "("));
                pos++;
            }
            else if (c == ')') {
                tokens.add(new Token(Token.TokenType.RPAREN, ")"));
                pos++;
            }
            else if (c == ',') {
                tokens.add(new Token(Token.TokenType.COMMA, ","));
                pos++;
            }
            else if (c == ';') {
                tokens.add(new Token(Token.TokenType.SEMICOLON, ";"));
                pos++;
            }
            else if (c == '*') {
                tokens.add(new Token(Token.TokenType.WILDCARD, "*"));
                pos++;
            }

            // Comparators
            else if (c == '=') {
                // Check for '==' or '='
                readEquals();
            }
            else if (c == '<') {
                // Check for '<=' or '<'
                readLessThan();
            }
            else if (c == '>') {
                // Check for '<=' or '<'
                readGreaterThan();
            }
            else if (c == '!') {
                // Check for '!='
                readNotEquals();
            }

            // Literals
            else if (c == '\'') {
                // If encountering a ', the enclosed string must be a String Literal
                // [Value] ::= "'" [StringLiteral] "'"
                readStringLiteral();
            }
            else if (c == '-' || c == '+') {
                // If encountering a - or +, the following must be an Integer Literal or Float Literal
                readSignedNumber(); // For both integers and floats
            }
            else if (Character.isDigit(c)) {
                // If encountering a digit, the following must be an Integer Literal or Float Literal
                readNumber(); // For both integers and floats
            }
            else if (Character.isLetter(c)) {
                // If encountering a letter, the following must be a keyword or an identifier
                readWord(); // For both boolean literals and keywords
            }
            else {
                throw new IllegalStateException("Unexpected character: " + c);
            }

        }

        return new TokenStream(tokens);
    }

    private void skipWhitespace() {
        while (pos < input.length() && Character.isWhitespace(input.charAt(pos))) {
            pos++;
        }
    }

    private void readWord() {
        int start = pos;
        while (pos < input.length() && Character.isLetterOrDigit(input.charAt(pos))) {
            pos++;
        }
        String word = input.substring(start, pos);
        Token.TokenType type = KEYWORDS.getOrDefault(word.toUpperCase(), Token.TokenType.IDENTIFIER);
        tokens.add(new Token(type, word));
    }

    private void readSignedNumber() {
        int start = pos;
        pos++; // Consume + or -
        if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
            throw new IllegalArgumentException(
                    "Expected digit after sign character at position " + pos
            );
        }
        readNumber(start);
    }

    private void readNumber() {
        // Helper to avoid duplication between readSignedNumber and readNumber
        readNumber(pos);
    }

    private void readNumber(int start) {
        consumeDigits();
        if (pos < input.length() && input.charAt(pos) == '.') {
            // Float Literal
            pos++; // Consume .
            if (pos >= input.length() || !Character.isDigit(input.charAt(pos))) {
                throw new IllegalArgumentException(
                        "Expected digit after decimal point at position " + pos
                );
            }
            consumeDigits();
            tokens.add(new Token(Token.TokenType.FLOAT_LITERAL, input.substring(start, pos)));
        }
        else {
            // Integer Literal
            tokens.add(new Token(Token.TokenType.INTEGER_LITERAL, input.substring(start, pos)));
        }
    }

    private void consumeDigits() {
        // Helper for readNumber
        while (pos < input.length() && Character.isDigit(input.charAt(pos))) {
            pos++;
        }
    }

    private void readStringLiteral() {
        pos++;
        StringBuilder sb = new StringBuilder();

        while (pos < input.length() && input.charAt(pos) != '\'') {
            sb.append(input.charAt(pos));
            pos++;
        }
        if (pos >= input.length()) {
            throw new IllegalArgumentException("Unterminated string literal at position " + pos);
        }
        pos++; // Consume closing single quote
        tokens.add(new Token(Token.TokenType.STRING_LITERAL, sb.toString()));
    }

    private void readNotEquals() {
        // Check for '!=' or '!'
        pos++;
        if (pos < input.length() && input.charAt(pos) == '=') {
            tokens.add(new Token(Token.TokenType.NEQ, "!="));
            pos++;
        }
        else {
            throw new IllegalArgumentException(
                    "Expected '=' after '!' at position " + pos
            );
        }
    }

    private void readLessThan() {
        // Check for '<=' or '<'
        pos++;
        if (pos < input.length() && input.charAt(pos) == '=') {
            tokens.add(new Token(Token.TokenType.LTE, "<="));
            pos++;
        }
        else {
            tokens.add(new Token(Token.TokenType.LT, "<"));
        }
    }

    private void readGreaterThan() {
        // Check for '>=' or '>'
        pos++;
        if (pos < input.length() && input.charAt(pos) == '=') {
            tokens.add(new Token(Token.TokenType.GTE, ">="));
            pos++;
        }
        else {
            tokens.add(new Token(Token.TokenType.GT, ">"));
        }
    }

    private void readEquals() {
        // Check for '==' or '='
        pos++;
        if (pos < input.length() && input.charAt(pos) == '=') {
            tokens.add(new Token(Token.TokenType.EQ, "=="));
            pos++;
        }
        else {
            tokens.add(new Token(Token.TokenType.ASSIGN, "="));
        }
    }
}
