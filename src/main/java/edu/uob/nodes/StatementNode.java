package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

import java.util.List;

// A statement node — top-level SELECT, INSERT, UPDATE, DELETE
public class StatementNode extends ASTNode {
    public enum StatementType {
        USE,
        CREATE_DATABASE,
        CREATE_TABLE,
        DROP_DATABASE,
        DROP_TABLE,
        ALTER_TABLE,
        INSERT,
        SELECT,
        UPDATE,
        DELETE,
        JOIN
    }

    public final StatementType type; // "SELECT", "INSERT", etc.
    public final List<ASTNode> clauses;

    public StatementNode(StatementType type, List<ASTNode> clauses) {
        this.type = type;
        this.clauses = clauses;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitStatement(this);
    }
}
