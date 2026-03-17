package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

import java.util.List;

// A clause node — FROM, WHERE, GROUP BY, VALUES, SET, etc.
public class ClauseNode extends ASTNode {
    public enum ClauseType {
        DATABASE,
        TABLE,
        ATTRIBUTES,
        ALTERATION,
        VALUES,
        SELECT_LIST,
        FROM,
        WHERE,
        SET,
        TABLES,
        ON
    }

    public final ClauseType type; // "FROM", "WHERE", "GROUP_BY", etc.
    public final List<ASTNode> children;

    public ClauseNode(ClauseType type, List<ASTNode> children) {
        this.type = type;
        this.children = children;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitClause(this);
    }
}