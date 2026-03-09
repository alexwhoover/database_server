package edu.uob.nodes;

public class BooleanConditionNode extends ConditionNode {
    private BoolOperator operator;
    private ConditionNode left;
    private ConditionNode right;

    public BooleanConditionNode(BoolOperator operator, ConditionNode left, ConditionNode right) {
        this.operator = operator;
        this.left = left;
        this.right = right;
    }

    public BoolOperator getOperator() {
        return operator;
    }

    public ConditionNode getLeft() {
        return left;
    }

    public ConditionNode getRight() {
        return right;
    }
}
