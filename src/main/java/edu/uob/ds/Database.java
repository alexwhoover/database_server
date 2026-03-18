package edu.uob.ds;

import java.util.HashMap;

public class Database {
    private String name;
    private HashMap<String, Table> tables;

    public Database(String name) {
        this.name = name;
        this.tables = new HashMap<>();
    }

    public void addTable(String tableName, Table table) {
        tables.put(tableName, table);
    }

    public Table getTable(String tableName) {
        return tables.get(tableName);
    }

    public boolean dropTable(String tableName) {
        // Remove a table from database. Returns true if successful.
        if (tables.containsKey(tableName)) {
            tables.remove(tableName);
            return true;
        }
        return false;
    }

    public String getName() {
        return name;
    }
}
