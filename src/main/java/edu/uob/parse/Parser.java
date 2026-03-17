package edu.uob.parse;

import edu.uob.exceptions.ParseException;
import edu.uob.nodes.ClauseNode;
import edu.uob.nodes.LeafNode;
import edu.uob.nodes.StatementNode;

import java.util.List;

public class Parser {
    private final TokenStream stream;
    public Parser(TokenStream stream) {
        this.stream = stream;
    }

    public StatementNode parse() {
        StatementNode stmt = parseCommandType();
        stream.expect(Token.TokenType.SEMICOLON);
        return stmt;
    }

    private StatementNode parseCommandType() {
        Token t = stream.peek();
        return switch (t.getType()) {
            case USE -> parseUse();
            case CREATE -> parseCreate();
            case DROP -> parseDrop();
            case ALTER -> parseAlter();
            case INSERT -> parseInsert();
            case SELECT -> parseSelect();
            case UPDATE -> parseUpdate();
            case DELETE -> parseDelete();
            case JOIN -> parseJoin();
            default -> throw new ParseException(
                    "Expected a command keyword, got " + t.toString()
            );
        };
    }

    private StatementNode parseJoin() {
        return null;
    }

    private StatementNode parseDelete() {
        return null;
    }

    private StatementNode parseUpdate() {
        return null;
    }

    private StatementNode parseSelect() {
        return null;
    }

    private StatementNode parseInsert() {
        return null;
    }

    private StatementNode parseAlter() {
        return null;
    }

    private StatementNode parseDrop() {
        return null;
    }

    private StatementNode parseCreate() {
        return null;
    }

    private StatementNode parseUse() {
        stream.expect(Token.TokenType.USE);
        LeafNode dbName = parseIdentifier(LeafNode.LeafType.DATABASE_NAME);
        return new StatementNode(StatementNode.StatementType.USE,
                List.of(new ClauseNode(ClauseNode.ClauseType.DATABASE, List.of(dbName))));
    }

    private LeafNode parseIdentifier(LeafNode.LeafType role) {
        Token t = stream.expect(Token.TokenType.IDENTIFIER);
        return new LeafNode(role, t.getValue());
    }
}
