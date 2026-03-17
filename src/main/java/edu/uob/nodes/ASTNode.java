package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

public abstract class ASTNode {
    public abstract <T> T accept(ASTVisitor<T> visitor);
}
