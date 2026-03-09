package edu.uob.nodes;

import java.util.List;

public class CreateTableNode {
    private String tableName;
    private List<String> attributeNames; // Optional, null if not used

    public CreateTableNode(String tableName, List<String> attributeNames) {
        this.tableName = tableName;
        this.attributeNames = attributeNames;
    }

    public CreateTableNode(String tableName) {
        this.tableName = tableName;
        this.attributeNames = null;
    }

    public String getTableName() {
        return tableName;
    }

    public List<String> getAttributeNames() {
        return attributeNames;
    }
}
