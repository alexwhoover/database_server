package edu.uob.parse;

import edu.uob.exceptions.ParseException;
import edu.uob.nodes.Expr;
import edu.uob.nodes.Stmt;

import java.util.ArrayList;
import java.util.List;

public class Parser {
    private TokenStream stream;
    public Parser() {
        this.stream = null;
    }

    // Public interface
    public Stmt parse(TokenStream stream) {
        this.stream = stream;
        Stmt stmt = parseCommandType();
        stream.expect(Token.TokenType.SEMICOLON);
        return stmt;
    }

    // ---- PRIVATE ----

    // Main delegator
    private Stmt parseCommandType() {
        Token t = stream.peek();
        return switch (t.getType()) {
            case USE      -> parseUse();
            case CREATE   -> parseCreate();
            case DROP     -> parseDrop();
            case ALTER    -> parseAlter();
            case INSERT   -> parseInsert();
            case SELECT   -> parseSelect();
            case UPDATE   -> parseUpdate();
            case DELETE   -> parseDelete();
            default       -> throw new ParseException(
                    "Expected a command keyword, got " + t.toString()
            );
        };
    }

    // Top level statement parsers
    private Stmt parseUse() {
        /*
         * USE:
         * Stmt.Use
         * -> String dbName
         */
        stream.expect(Token.TokenType.USE);
        String dbName = parseIdentifier();
        return new Stmt.Use(dbName);
    }

    private Stmt parseCreate() {
        stream.expect(Token.TokenType.CREATE);
        Token.TokenType nextType = stream.peek().getType();

        if (nextType == Token.TokenType.DATABASE) {
            return parseCreateDatabase();
        }
        else {
            return parseCreateTable();
        }
    }

    private Stmt parseCreateDatabase() {
        /*
         * CREATE DATABASE:
         * Stmt.CreateDatabase
         * -> String dbName
         */
        stream.expect(Token.TokenType.DATABASE);
        String dbName = parseIdentifier();
        return new Stmt.CreateDatabase(dbName);
    }

    private Stmt parseCreateTable() {
        /*
         * CREATE TABLE:
         * Stmt.CreateTable
         * -> String tableName
         * -> List<String> attributeList
         */
        stream.expect(Token.TokenType.TABLE);
        String tableName = parseIdentifier();
        Token.TokenType nextType = stream.peek().getType();
        if (nextType == Token.TokenType.LPAREN) {
            stream.expect(Token.TokenType.LPAREN);
            List<String> attributeList = parseIdentifierList(false);
            stream.expect(Token.TokenType.RPAREN);
            return new Stmt.CreateTable(tableName, attributeList);
        }
        else {
            return new Stmt.CreateTable(tableName, null);
        }
    }

    private Stmt parseDrop() {
        stream.expect(Token.TokenType.DROP);
        Token.TokenType nextType = stream.peek().getType();

        if (nextType == Token.TokenType.DATABASE) {
            return parseDropDatabase();
        }
        else {
            return parseDropTable();
        }
    }

    private Stmt parseDropDatabase() {
        /*
         * DROP DATABASE:
         * Stmt.DropDatabase
         * -> String dbName
         */
        stream.expect(Token.TokenType.DATABASE);
        String dbName = parseIdentifier();
        return new Stmt.DropDatabase(dbName);
    }

    private Stmt parseDropTable() {
        /*
         * DROP TABLE:
         * Stmt.DropTable
         * -> String tableName
         */
        stream.expect(Token.TokenType.TABLE);
        String tableName = parseIdentifier();
        return new Stmt.DropTable(tableName);
    }

    private Stmt parseAlter() {
        /*
         * ALTER TABLE:
         * Stmt.Alter
         * -> String tableName
         * -> String alterationType
         * -> String attributeName
         */
        stream.expect(Token.TokenType.ALTER);
        stream.expect(Token.TokenType.TABLE);
        String tableName = parseIdentifier();
        Stmt.Alter.AltType altType;
        if (stream.peek().getType() == Token.TokenType.ADD) {
            stream.expect(Token.TokenType.ADD);
            altType = Stmt.Alter.AltType.ADD;
        }
        else {
            stream.expect(Token.TokenType.DROP);
            altType = Stmt.Alter.AltType.DROP;
        }
        String attributeName = parseIdentifier();
        return new Stmt.Alter(tableName, altType, attributeName);
    }

    private Stmt parseInsert() {
        /*
         * INSERT INTO:
         * Stmt.Insert
         * -> String tableName
         * -> List<Expr.Literal> valueList
         */
        stream.expect(Token.TokenType.INSERT);
        stream.expect(Token.TokenType.INTO);
        String tableName = parseIdentifier();
        stream.expect(Token.TokenType.VALUES);
        stream.expect(Token.TokenType.LPAREN);
        List<Expr.Literal> valueList = parseValueList();
        stream.expect(Token.TokenType.RPAREN);

        return new Stmt.Insert(tableName, valueList);
    }

    private Stmt parseSelect() {
        /*
         * SELECT:
         * Stmt.Select
         * -> List<String> attributeList
         * -> String tableName
         * -> Expr condition (optional) [Expr.AND || Expr.OR || ...], null if no condition
         */
        stream.expect(Token.TokenType.SELECT);
        List<String> attributeList = parseIdentifierList(true);
        stream.expect(Token.TokenType.FROM);
        String tableName = parseIdentifier();

        if (stream.peek().getType() == Token.TokenType.WHERE) {
            stream.expect(Token.TokenType.WHERE);
            return new Stmt.Select(tableName, attributeList, parseCondition());
        }

        // No condition
        return new Stmt.Select(tableName, attributeList, null);
    }

    private Stmt parseUpdate() {
        /*
         * <Update> ::= "UPDATE " [TableName] " SET " <NameValueList> " WHERE " <Condition>
         * <NameValueList> ::= <NameValuePair> | <NameValuePair> "," <NameValueList>
         * <NameValuePair> ::= [AttributeName] "=" [Value]
         *
         * Stmt.Update
         * -> String tableName
         * -> List<NameValuePair> nameValueList
         * -> Expr condition
         */
        stream.expect(Token.TokenType.UPDATE);
        String tableName = parseIdentifier();
        stream.expect(Token.TokenType.SET);
        List<NameValuePair> nameValueList = parseNameValueList();
        stream.expect(Token.TokenType.WHERE);
        Expr condition = parseCondition();
        return new Stmt.Update(tableName, nameValueList, condition);
    }

    private Stmt parseDelete() {
        /*
         * DELETE FROM <TableName> WHERE <Condition>
         * Stmt.Delete
         * -> String tableName
         * -> Expr condition
         */
        stream.expect(Token.TokenType.DELETE);
        stream.expect(Token.TokenType.FROM);
        String tableName = parseIdentifier();
        stream.expect(Token.TokenType.WHERE);
        Expr condition = parseCondition();
        return new Stmt.Delete(tableName, condition);
    }

    // ---- PRIVATE HELPERS ----
    private String parseIdentifier() {
        Token t = stream.expect(Token.TokenType.IDENTIFIER);
        return t.getValue();
    }

    private List<String> parseIdentifierList(boolean wildcardAllowed) {
        List<String> attributeList = new ArrayList<>();
        if (wildcardAllowed && stream.peek().getType() == Token.TokenType.WILDCARD) {
            stream.expect(Token.TokenType.WILDCARD);
            return attributeList;
        }

        attributeList.add(parseIdentifier());
        while (stream.peek().getType() == Token.TokenType.COMMA) {
            stream.expect(Token.TokenType.COMMA);
            attributeList.add(parseIdentifier());
        }
        return attributeList;
    }

    private Expr parseCondition() {
        // Conditions are left associative in SQL
        // Conditions can optionally be surrounded by ()

        // Conditions can either be <FirstCondition> <BoolOperator> <SecondCondition>
        // or [AttributeName] <Comparator> [Value]
        Expr left;
        if (stream.peek().getType() == Token.TokenType.LPAREN) {
            stream.expect(Token.TokenType.LPAREN);
            left = parseCondition();
            stream.expect(Token.TokenType.RPAREN);
        }
        else {
            left = parseComparison();
        }

        while(stream.peek().getType() == Token.TokenType.AND || stream.peek().getType() == Token.TokenType.OR) {
            Token op = stream.consume();
            Expr right;
            if (stream.peek().getType() == Token.TokenType.LPAREN) {
                stream.expect(Token.TokenType.LPAREN);
                right = parseCondition();
                stream.expect(Token.TokenType.RPAREN);
            } else {
                right = parseComparison();
            }
            if (op.getType() == Token.TokenType.AND) {
                left = new Expr.Binary(left, Expr.Binary.Op.AND, right);
            } else {
                left = new Expr.Binary(left, Expr.Binary.Op.OR, right);
            }
        }

        return left;

    }

    private Expr parseComparison() {
        // [AttributeName] <Comparator> [Value]
        // Conditions can optionally be surrounded by ()
        if (stream.peek().getType() == Token.TokenType.LPAREN) {
            stream.expect(Token.TokenType.LPAREN);
            Expr comparison = parseComparison();
            stream.expect(Token.TokenType.RPAREN);
            return comparison;
        }

        Expr.Attr attrName = new Expr.Attr(parseIdentifier());
        Expr.Binary.Op op = parseComparisonOperator();
        Expr.Literal value = parseLiteral();

        return new Expr.Binary(attrName, op, value);
    }

    private Expr.Binary.Op parseComparisonOperator() {
        Token t = stream.consume();
        return switch (t.getType()) {
            case EQ   -> Expr.Binary.Op.EQ;
            case NEQ  -> Expr.Binary.Op.NEQ;
            case LT   -> Expr.Binary.Op.LT;
            case GT   -> Expr.Binary.Op.GT;
            case LTE  -> Expr.Binary.Op.LTE;
            case GTE  -> Expr.Binary.Op.GTE;
            case LIKE -> Expr.Binary.Op.LIKE;
            default   -> throw new ParseException("Expected a comparison operator, got " + t);
        };
    }

    private Expr.Literal parseLiteral() {
        Token t = stream.consume();
        return new Expr.Literal(t.getValue());
    }

    private List<Expr.Literal> parseValueList() {
        List<Expr.Literal> valueList = new ArrayList<>();
        valueList.add(parseLiteral());
        while (stream.peek().getType() == Token.TokenType.COMMA) {
            stream.expect(Token.TokenType.COMMA);
            valueList.add(parseLiteral());
        }

        return valueList;
    }

    private List<NameValuePair> parseNameValueList() {
        // <NameValueList> ::= <NameValuePair> | <NameValuePair> "," <NameValueList>
        List<NameValuePair> list = new ArrayList<>();
        list.add(parseNameValuePair());
        while (stream.peek().getType() == Token.TokenType.COMMA) {
            stream.expect(Token.TokenType.COMMA);
            list.add(parseNameValuePair());
        }
        return list;
    }

    private NameValuePair parseNameValuePair() {
        // <NameValuePair> ::= [AttributeName] "=" [Value]
        String attributeName = parseIdentifier();
        stream.expect(Token.TokenType.ASSIGN);
        Expr.Literal value = parseLiteral();
        return new NameValuePair(attributeName, value);
    }
}