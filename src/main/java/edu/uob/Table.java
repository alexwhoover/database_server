package edu.uob;

import java.util.ArrayList;
import java.util.HashMap;

public class Table {
    private String name;
    private ArrayList<String> cols;
    private HashMap<Integer, Row> rows;
    private int nextId;

    public Table(String name) {
        this.name = name;
        this.cols = new ArrayList<>();
        this.rows = new HashMap<>();
        this.nextId = 0;
    }

    public void addRow(Row row) {
        int id = row.getId();
        if (rows.containsKey(id)) {
            throw new IllegalArgumentException("Row ID already exists: " + id);
        }

        rows.put(id, row);

        // If the inserted row has an ID larger than the current ID, set the current ID to be one larger
        nextId = Math.max(nextId, id + 1);
    }

    public int getNextId() {
        return nextId;
    }

    public ArrayList<String> getColNames() {
        return cols;
    }
}
