package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

import java.util.List;
import java.util.Objects;

// A statement node — top-level SELECT, INSERT, UPDATE, DELETE
public class BranchNode implements ASTNode {
    public enum BranchType {
        // Statements
        USE,
        CREATE_DATABASE,
        CREATE_TABLE,
        DROP_DATABASE,
        DROP_TABLE,
        ALTER,
        INSERT,
        SELECT,
        UPDATE,
        DELETE,
        JOIN,

        // Lists
        ATTRIBUTE_LIST,

        // Expressions
        AND,
        OR,
        EQ,
        NEQ,
        LT,
        GT,
        LTE,
        GTE,
        LIKE,
        ASSIGN
    }

    public final BranchType type;
    public final List<ASTNode> children;

    public BranchNode(BranchType type, List<ASTNode> children) {
        this.type = type;
        this.children = children;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitBranch(this);
    }

    @Override
    public boolean equals(Object o) {
        // 1. Check if they are the exact same instance in memory
        if (this == o) return true;

        // 2. Check for null and ensure exact class match
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Cast to BranchNode
        BranchNode that = (BranchNode) o;

        // 4. Compare type and recursively compare children
        // (List.equals handles the deep equality check of elements)
        return this.type == that.type &&
                Objects.equals(this.children, that.children);
    }
}
