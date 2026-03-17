package edu.uob.nodes;

import edu.uob.visitors.ASTVisitor;

public interface ASTNode {
    <T> T accept(ASTVisitor<T> visitor);
}
