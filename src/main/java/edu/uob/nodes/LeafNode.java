package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

import java.util.Objects;

// A leaf node — identifiers, literals, wildcards
public class LeafNode implements ASTNode {
    public enum LeafType {
        // Names
        DATABASE_NAME,
        TABLE_NAME,
        ATTRIBUTE_NAME,
        // Literals
        STRING_LITERAL,
        BOOLEAN_LITERAL,
        FLOAT_LITERAL,
        INTEGER_LITERAL,
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

    @Override
    public boolean equals(Object o) {
        // 1. Check if they are the exact same instance in memory
        if (this == o) return true;

        // 2. Check for null and ensure exact class match
        if (o == null || getClass() != o.getClass()) return false;

        // 3. Cast to LeafNode
        LeafNode leafNode = (LeafNode) o;

        // 4. Compare type and value
        // Objects.equals is safe to use even if 'value' is null
        return this.type == leafNode.type &&
                Objects.equals(this.value, leafNode.value);
    }
}