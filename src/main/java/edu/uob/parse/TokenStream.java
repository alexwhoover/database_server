package edu.uob.parse;

import java.util.List;

public class TokenStream {
    private final List<Lexer.Token> tokens;
    private int pos;

    public TokenStream(List<Lexer.Token> tokens) {
        this.tokens = tokens;
        this.pos = 0;
    }

    public Lexer.Token peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return null; // End of stream
    }

    public Lexer.Token next() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return null; // End of stream
    }
}
