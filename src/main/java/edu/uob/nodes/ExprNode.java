package edu.uob.nodes;

import java.util.List;

// An expression node — handles operators, function calls, predicates
public class ExprNode extends ASTNode {
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
