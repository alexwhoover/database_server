package edu.uob.ds;

import java.util.*;
import java.util.function.BiPredicate;

public class Table {
    private ArrayList<String> cols;
    private HashMap<Integer, Row> rows;
    private int nextId;

    public Table() {
        this.cols = new ArrayList<>();
        this.rows = new HashMap<>();
        this.nextId = 1;
    }

    public void addCol(String colName) {
        if (cols.contains(colName)) {
            throw new IllegalArgumentException("Column already exists: " + colName);
        }
        cols.add(colName);
    }

    public void dropCol(String attributeName) {
        if (!cols.contains(attributeName)) {
            throw new IllegalArgumentException("Column does not exist: " + attributeName);
        }
        cols.remove(attributeName);
        for (Row row : rows.values()) {
            row.removeValue(attributeName);
        }
    }

    public void addRow(int id, Row row) {
        rows.put(id, row);
        nextId = Math.max(nextId, id + 1);
    }

    public void removeRow(Integer id) {
        rows.remove(id);
    }

    public Row getRow(int id) {
        return rows.get(id);
    }

    public int getNextId() { return nextId; }

    public Set<Integer> getRowIds() { return rows.keySet(); }

    public Collection<Row> getRowValues() {
        return rows.values();
    }

    public List<String> getColNames() {
        return cols;
    }

    public Table filter(BiPredicate<Integer, Row> predicate) {
        /*
         * Returns a new table containing only the rows that satisfy the given predicate.
         * The original table is not modified. Original row IDs are preserved in the result.
         * All columns from the original table are carried over.
         *
         * Example usage:
         *   Table adults = students.filter(row -> Integer.parseInt(row.getValue("age")) >= 18);
         */
        Table result = new Table();

        for (String col : cols) {
            result.addCol(col);
        }

        for (Map.Entry<Integer, Row> entry : rows.entrySet()) {
            int id = entry.getKey();
            Row row = entry.getValue();

            if (predicate.test(id, row)) {
                result.rows.put(id, new Row(row.getValues()));
                result.nextId = Math.max(result.nextId, id + 1);
            }
        }

        return result;
    }

    public Table project(List<String> selectedCols) {
        /*
         * Returns a new table containing only the specified columns, in the order given.
         * The original table is not modified. All rows are carried over, projected down
         * to only the selected columns. Original row IDs are preserved.
         *
         * Example usage:
         *   Table nameAndAge = students.project(List.of("name", "age"));
         */
        for (String col : selectedCols) {
            if (!col.equals("id") && !cols.contains(col)) {
                throw new IllegalArgumentException("Column not found: " + col);
            }
        }

        Table result = new Table();
        for (String col : selectedCols) {
            if (!col.equals("id")) {
                result.addCol(col);
            }
        }

        for (Map.Entry<Integer, Row> entry : rows.entrySet()) {
            int id = entry.getKey();
            Row row = entry.getValue();

            Row projected = new Row();
            for (String col : selectedCols) {
                if (!col.equals("id")) {
                    projected.setValue(col, row.getValue(col));
                }
            }

            result.rows.put(id, projected);
            result.nextId = Math.max(result.nextId, id + 1);
        }

        return result;
    }

    public String toTabString() {
        StringBuilder sb = new StringBuilder();

        // Header row
        sb.append("id");
        for (String col : cols) {
            sb.append("\t").append(col);
        }
        sb.append("\n");

        // Data rows
        for (Map.Entry<Integer, Row> entry : rows.entrySet()) {
            int id = entry.getKey();
            sb.append(id);
            for (String col : cols) {
                sb.append("\t").append(entry.getValue().getValue(col));
            }
            sb.append("\n");
        }

        return sb.toString();
    }
}
