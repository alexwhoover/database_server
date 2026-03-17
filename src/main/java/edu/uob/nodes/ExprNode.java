package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

import java.util.List;

// An expression node — handles operators, function calls, predicates
public class ExprNode extends ASTNode {
    public enum ExprType {
        // Boolean operators
        AND,
        OR,
        // Comparators
        EQ,
        NEQ,
        LT,
        GT,
        LTE,
        GTE,
        LIKE,
        // Assignment (SET clause name=value pairs)
        ASSIGN
    }
    
    public final String type;   // "EQ", "AND", "CALL", "ALIAS", etc.
    public final List<ASTNode> children;

    public ExprNode(String type, List<ASTNode> children) {
        this.type = type;
        this.children = children;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitExpr(this);
    }
}
