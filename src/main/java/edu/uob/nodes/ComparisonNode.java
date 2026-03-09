package edu.uob.nodes;

public class ComparisonNode extends ConditionNode {
    private String attributeName;
    private ComparatorType comparator;
    private Object value;

    public ComparisonNode(String attributeName, ComparatorType comparator, Object value) {
        this.attributeName = attributeName;
        this.comparator = comparator;
        this.value = value;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public ComparatorType getComparator() {
        return comparator;
    }

    public Object getValue() {
        return value;
    }
}
