package edu.uob.nodes;

public class UseNode extends ASTNode {
    private String databaseName;

    public UseNode(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
