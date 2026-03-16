package edu.uob.parse;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;

public class Lexer {
    public enum TokenType {

    }

    public class Token {
        private final TokenType type;
        private final String value;

        public Token(TokenType type, String value) {
            this.type = type;
            this.value = value;
        }

        public TokenType getType() {
            return type;
        }

        public String getValue() {
            return value;
        }
    }

    String command;

    public Lexer(String command) {
        this.command = command;
    }

    public List<Token> tokenize() {
        return null;
    }
}
