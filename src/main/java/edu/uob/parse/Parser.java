package edu.uob.parse;

import edu.uob.exceptions.ParseException;
import edu.uob.nodes.Stmt;

import java.util.List;

public class Parser {
    private final TokenStream stream;
    public Parser(TokenStream stream) {
        this.stream = stream;
    }
    public Parser(List<Token> tokens) {
        this.stream = new TokenStream(tokens);
    }

    public Stmt parse() {
        Stmt stmt = parseCommandType();
        stream.expect(Token.TokenType.SEMICOLON);
        return stmt;
    }

    private Stmt parseCommandType() {
        Token t = stream.peek();
        return switch (t.getType()) {
            case USE -> parseUse();
            default -> throw new ParseException(
                    "Expected a command keyword, got " + t.toString()
            );
        };
    }

//    private Stmt parseSelect() {
//        /*
//         * SELECT command should result in following tree structure:
//         * Stmt.Select [SELECT]
//         * -> List<Expr.Attr> [ATTRIBUTE_LIST]
//         * -> String [TABLE_NAME, "students"]
//         * -> (optional) Expr [AND/OR/EQ/...]
//         */
//        stream.expect(Token.TokenType.SELECT);
//        ASTNode attributeList = parseAttributeList();
//        stream.expect(Token.TokenType.FROM);
//        ASTNode tableName = parseIdentifier(LeafNode.LeafType.TABLE_NAME);
//
//        // WHERE clause is optional
//        if (stream.peek().getType() == Token.TokenType.WHERE) {
//            stream.expect(Token.TokenType.WHERE);
//            ASTNode condition = parseCondition();
//            return new BranchNode(
//                    BranchNode.BranchType.SELECT,
//                    List.of(attributeList, tableName, condition)
//            );
//        }
//        return new BranchNode(
//                BranchNode.BranchType.SELECT,
//                List.of(attributeList, tableName)
//        );
//    }
//
//    private ASTNode parseAttributeList() {
//        if (stream.peek().getType() == Token.TokenType.WILDCARD) {
//            Token t = stream.expect(Token.TokenType.WILDCARD);
//            LeafNode wildcard = new LeafNode(LeafNode.LeafType.WILDCARD, t.getValue());
//            return new BranchNode(
//                    BranchNode.BranchType.ATTRIBUTE_LIST, List.of(wildcard)
//            );
//        }
//
//        List<ASTNode> attributes = new ArrayList<>();
//        attributes.add(parseIdentifier(LeafNode.LeafType.ATTRIBUTE_NAME));
//        while (stream.peek().getType() == Token.TokenType.COMMA) {
//            stream.expect(Token.TokenType.COMMA);
//            attributes.add(parseIdentifier(LeafNode.LeafType.ATTRIBUTE_NAME));
//        }
//        return new BranchNode(BranchNode.BranchType.ATTRIBUTE_LIST, attributes);
//    }
//
//    private ASTNode parseCondition() {
//        // Conditions are left associative
//        ASTNode left = parseComparison();
//        while (stream.peek().getType() == Token.TokenType.AND || stream.peek().getType() == Token.TokenType.OR) {
//            Token op = stream.consume();
//            ASTNode right = parseComparison();
//            BranchNode.BranchType type = op.getType() == Token.TokenType.AND
//                    ? BranchNode.BranchType.AND
//                    : BranchNode.BranchType.OR;
//            left = new BranchNode(type, List.of(left, right));
//        }
//        return left;
//    }
//
//    private ASTNode parseComparison() {
//        ASTNode attribute = parseIdentifier(LeafNode.LeafType.ATTRIBUTE_NAME);
//        BranchNode.BranchType op = parseComparisonOperator();
//        ASTNode value = parseValue();
//        return new BranchNode(op, List.of(attribute, value));
//    }
//
//    private BranchNode.BranchType parseComparisonOperator() {
//        Token t = stream.consume();
//        return switch (t.getType()) {
//            case EQ   -> BranchNode.BranchType.EQ;
//            case NEQ  -> BranchNode.BranchType.NEQ;
//            case LT   -> BranchNode.BranchType.LT;
//            case GT   -> BranchNode.BranchType.GT;
//            case LTE  -> BranchNode.BranchType.LTE;
//            case GTE  -> BranchNode.BranchType.GTE;
//            case LIKE -> BranchNode.BranchType.LIKE;
//            default   -> throw new ParseException("Expected a comparison operator, got " + t);
//        };
//    }
//
//    private ASTNode parseValue() {
//        Token t = stream.consume();
//        LeafNode.LeafType type = switch (t.getType()) {
//            case STRING_LITERAL  -> LeafNode.LeafType.STRING_LITERAL;
//            case BOOLEAN_LITERAL -> LeafNode.LeafType.BOOLEAN_LITERAL;
//            case FLOAT_LITERAL   -> LeafNode.LeafType.FLOAT_LITERAL;
//            case INTEGER_LITERAL -> LeafNode.LeafType.INTEGER_LITERAL;
//            case NULL            -> LeafNode.LeafType.NULL;
//            default -> throw new ParseException("Expected a value literal, got " + t);
//        };
//        return new LeafNode(type, t.getValue());
//    }

    private Stmt parseUse() {
        /*
         * USE command should result in following tree structure:
         * Stmt.Use [USE]
         * -> String [DATABASE_NAME, "identifier"]
         */
        stream.expect(Token.TokenType.USE);
        String dbName = stream.expect(Token.TokenType.IDENTIFIER).getValue();
        return new Stmt.Use(dbName);
    }

//    private Expr parseIdentifier(LeafNode.LeafType role) {
//        Token t = stream.expect(Token.TokenType.IDENTIFIER);
//        return new LeafNode(role, t.getValue());
//    }
}
