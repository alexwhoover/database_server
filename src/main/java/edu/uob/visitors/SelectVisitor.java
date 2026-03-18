package edu.uob.visitors;

import edu.uob.Table;
import edu.uob.nodes.BranchNode;
import edu.uob.nodes.LeafNode;

public class SelectVisitor implements ASTVisitor<Table> {
    @Override
    public Table visitBranch(BranchNode node) {
        return null;
    }

    @Override
    public Table visitLeaf(LeafNode node) {
        return null;
    }
}
