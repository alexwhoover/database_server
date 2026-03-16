package edu.uob.parse;

import java.util.ArrayList;
import java.util.List;

public class TokenStream {
    private final List<Token> tokens;
    private int pos;

    public TokenStream(List<Token> tokens) {
        this.tokens = new ArrayList<>(tokens);
        this.pos = 0;
    }

    public Token peek() {
        if (pos < tokens.size()) {
            return tokens.get(pos);
        }
        return null; // End of stream
    }

    public Token consume() {
        if (pos < tokens.size()) {
            return tokens.get(pos++);
        }
        return null; // End of stream
    }

    public Token expect(Token.TokenType expected) throws IllegalStateException {
        /*
         * Consumes the next token and checks that it matches the expected type. If it does, returns the token.
         */
        Token token = consume();
        if (token == null) {
            throw new IllegalStateException("Expected token " + expected + " but reached end of stream");
        }
        if (token.getType()!= expected) {
            throw new IllegalStateException("Expected token " + expected + " but got " + token.getType());
        }
        return token;
    }

    public boolean hasNext() {
        return pos < tokens.size();
    }
}
