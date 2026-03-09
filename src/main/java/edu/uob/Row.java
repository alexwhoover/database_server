package edu.uob;

import java.util.HashMap;

public class  Row {
    private final int id;
    // Maps column name to value
    private HashMap<String, String> values;

    public Row(int id) {
        this.id = id;
        this.values = new HashMap<>();
    }

    public Row(int id, HashMap<String, String> values) {
        this.id = id;
        this.values = new HashMap<>(values);
    }

    public int getId() { return id; }

    public void setValue(String colName, String value) {
        values.put(colName, value);
    }

    public String getValue(String colName) {
        return values.get(colName);
    }
}