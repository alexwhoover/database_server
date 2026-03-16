package edu.uob.nodes;

// A leaf node — identifiers, literals, wildcards
public class LeafNode extends ASTNode {
    public final String type;   // "IDENTIFIER", "STRING", "NUMBER", "WILDCARD"
    public final String value;

    public LeafNode(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public <T> T accept(ASTVisitor<T> visitor) {
        return visitor.visitLeaf(this);
    }
}