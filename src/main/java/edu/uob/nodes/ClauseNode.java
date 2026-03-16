package edu.uob.nodes;

import java.util.List;

// A clause node — FROM, WHERE, GROUP BY, VALUES, SET, etc.
public class ClauseNode extends ASTNode {
    public final String type; // "FROM", "WHERE", "GROUP_BY", etc.
    public final List<ASTNode> children;

    public ClauseNode(String type, List<ASTNode> children) {
        this.type = type;
        this.children = children;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitClause(this);
    }
}