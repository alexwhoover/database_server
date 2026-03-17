package edu.uob.visitors;

import edu.uob.nodes.LeafNode;
import edu.uob.nodes.BranchNode;

public interface ASTVisitor<T> {
    T visitBranch(BranchNode node);
    T visitLeaf(LeafNode node);
}