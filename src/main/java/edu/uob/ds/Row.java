package edu.uob.ds;

import java.util.HashMap;

public class  Row {
    // Maps column name to value
    private HashMap<String, String> values;

    public Row() {
        this.values = new HashMap<>();
    }

    public Row(HashMap<String, String> values) {
        this.values = new HashMap<>(values);
    }

    public void setValue(String colName, String value) {
        values.put(colName, value);
    }

    public String getValue(String colName) {
        return values.get(colName);
    }

    public void removeValue(String colName) {
        values.remove(colName);
    }

    public HashMap<String, String> getValues() {
        return new HashMap<>(values);
    }
}