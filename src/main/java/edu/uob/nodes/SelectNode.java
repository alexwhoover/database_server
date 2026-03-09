package edu.uob.nodes;

import java.util.List;

// <Select> ::= "SELECT " <WildAttribList> " FROM " [TableName]
//            | "SELECT " <WildAttribList> " FROM " [TableName] " WHERE " <Condition>
public class SelectNode extends ASTNode {
    private String tableName;            // [TableName]
    private List<String> attributeNames; // null if *
    private boolean selectAll;           // true if *
    private ConditionNode conditionNode; // null if not used

    public SelectNode(String tableName, List<String> attributeNames) {
        this.tableName = tableName;
        this.attributeNames = attributeNames;
        this.selectAll = false;
    }

    public SelectNode(String tableName) {
        this.tableName = tableName;
        this.attributeNames = null;
        this.selectAll = true;
    }

    public void setCondition(ConditionNode conditionNode) {
        this.conditionNode = conditionNode;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }

    public boolean isSelectAll() {
        return selectAll;
    }

    public ConditionNode getConditionNode() {
        return conditionNode;
    }
}
