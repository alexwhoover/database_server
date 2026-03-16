package edu.uob.nodes;

import java.util.List;

// A statement node — top-level SELECT, INSERT, UPDATE, DELETE
public class StatementNode extends ASTNode {
    public final String type; // "SELECT", "INSERT", etc.
    public final List<ASTNode> clauses;

    public StatementNode(String type, List<ASTNode> clauses) {
        this.type = type;
        this.clauses = clauses;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitStatement(this);
    }
}
