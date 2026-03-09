package edu.uob.nodes;

public class CreateDatabaseNode extends CreateNode {
    private String databaseName;

    public CreateDatabaseNode(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getDatabaseName() {
        return databaseName;
    }
}
