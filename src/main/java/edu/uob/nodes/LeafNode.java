package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

// A leaf node — identifiers, literals, wildcards
public class LeafNode extends ASTNode {
    public enum LeafType {
        // Names
        DATABASE_NAME,
        TABLE_NAME,
        ATTRIBUTE_NAME,
        // Literals
        STRING,
        BOOLEAN,
        FLOAT,
        INTEGER,
        NULL,
        // Misc
        WILDCARD,
        ALTERATION_TYPE
    }

    public final LeafType type;   // "IDENTIFIER", "STRING", "NUMBER", "WILDCARD"
    public final String value;

    public LeafNode(LeafType type, String value) {
        this.type = type;
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLeaf(this);
    }
}