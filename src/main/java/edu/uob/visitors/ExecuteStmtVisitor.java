package edu.uob.visitors;

import edu.uob.DBServer;
import edu.uob.ds.Row;
import edu.uob.ds.Table;
import edu.uob.nodes.Stmt;
import edu.uob.io.*;
import edu.uob.parse.NameValuePair;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class ExecuteStmtVisitor implements StmtVisitor<String> {
    private final DBServer server;

    public ExecuteStmtVisitor(DBServer server) {
        this.server = server;
    }

    // Finds the actual stored table name by case-insensitive match, or null if not found.
    private String resolveTableName(File dbFolder, String tableName) {
        for (String t : Reader.readTableNames(dbFolder)) {
            if (t.equalsIgnoreCase(tableName)) {
                return t;
            }
        }
        return null;
    }

    // Finds the actual stored database name by case-insensitive match, or null if not found.
    private String resolveDatabase(String dbName) {
        for (String db : Reader.readDatabaseNames(server.getStorageFolder())) {
            if (db.equalsIgnoreCase(dbName)) {
                return db;
            }
        }
        return null;
    }

    @Override
    public String visit(Stmt.Use stmt) {
        String resolved = resolveDatabase(stmt.dbName);
        if (resolved == null) {
            return "[ERROR] Database '" + stmt.dbName + "' does not exist.";
        }
        server.setDatabaseName(resolved);
        return "[OK]";
    }

    @Override
    public String visit(Stmt.CreateDatabase stmt) {
        if (resolveDatabase(stmt.dbName) != null) {
            return "[ERROR] Database '" + stmt.dbName + "' already exists.";
        }
        Writer.createDatabase(server.getStorageFolder(), stmt.dbName);
        return "[OK]";
    }

    @Override
    public String visit(Stmt.DropDatabase stmt) {
        Writer.deleteDatabase(server.getStorageFolder(), stmt.dbName);
        return "[OK]";
    }

    @Override
    public String visit(Stmt.CreateTable stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (stmt.attributeList != null) {
            for (String attr : stmt.attributeList) {
                if (attr.equalsIgnoreCase("id")) {
                    return "[ERROR] 'id' cannot be used as a column name.";
                }
            }
        }

        if (resolveTableName(dbFolder, stmt.tableName) != null) {
            return "[ERROR] Table '" + stmt.tableName + "' already exists.";
        }

        Table table = new Table();
        if (stmt.attributeList != null) {
            for (String attr : stmt.attributeList) {
                table.addCol(attr);
            }
        }

        try {
            Writer.writeTable(dbFolder, stmt.tableName, table);
        } catch (IOException e) {
            return "[ERROR] File could not be opened for write for table " + stmt.tableName;
        }
        return "[OK]";
    }

    @Override
    public String visit(Stmt.DropTable stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        String resolved = resolveTableName(dbFolder, stmt.tableName);
        if (resolved == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }
        Writer.deleteTable(dbFolder, resolved);
        return "[OK]";
    }

    @Override
    public String visit(Stmt.Select stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        String resolvedName = resolveTableName(dbFolder, stmt.tableName);
        if (resolvedName == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table raw;
        try {
            raw = Reader.readTable(dbFolder, resolvedName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (raw == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        for (String attr : stmt.attributeList) {
            if (!attr.equals("id") && !raw.getColNames().contains(attr)) {
                return "[ERROR] Table '" + stmt.tableName + "' does not contain attribute " + attr;
            }
        }

        Table filtered = raw;

        if (stmt.condition != null) {
            try {
                BiPredicate<Integer, Row> predicate = stmt.condition.accept(new PredicateExprVisitor());
                filtered = raw.filter(predicate);
            } catch (Exception e) {
                return "[ERROR] Invalid WHERE condition: " + e.getMessage();
            }
        }

        Table result = stmt.attributeList.isEmpty()
                ? filtered
                : filtered.project(stmt.attributeList);

        return "[OK]\n" + result.toTabString();
    }

    @Override
    public String visit(Stmt.Alter stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        String resolvedName = resolveTableName(dbFolder, stmt.tableName);
        if (resolvedName == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, resolvedName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        if (stmt.altType == Stmt.Alter.AltType.ADD) {
            if (stmt.attributeName.equalsIgnoreCase("id")) {
                return "[ERROR] 'id' cannot be used as a column name.";
            }
            if (table.getColNames().contains(stmt.attributeName)) {
                return "[ERROR] Column '" + stmt.attributeName + "' already exists in table '" + resolvedName + "'.";
            }
            table.addCol(stmt.attributeName);
        } else {
            if (!table.getColNames().contains(stmt.attributeName)) {
                return "[ERROR] Column '" + stmt.attributeName + "' does not exist in table '" + resolvedName + "'.";
            }
            table.dropCol(stmt.attributeName);
        }

        try {
            Writer.writeTable(dbFolder, resolvedName, table);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        return "[OK]";
    }

    @Override
    public String visit(Stmt.Insert stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        String resolvedName = resolveTableName(dbFolder, stmt.tableName);
        if (resolvedName == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, resolvedName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        List<String> cols = table.getColNames();
        if (stmt.valueList.size() != cols.size()) {
            return "[ERROR] Expected " + cols.size() + " values, got " + stmt.valueList.size();
        }

        Row row = new Row();
        for (int i = 0; i < cols.size(); i++) {
            String value = stmt.valueList.get(i).value;
            row.setValue(cols.get(i), value != null ? value : "NULL");
        }

        table.addRow(table.getNextId(), row);

        try {
            Writer.writeTable(dbFolder, resolvedName, table);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        return "[OK]";
    }

    @Override
    public String visit(Stmt.Update stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        String resolvedName = resolveTableName(dbFolder, stmt.tableName);
        if (resolvedName == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, resolvedName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        for (NameValuePair pair : stmt.nameValueList) {
            if (pair.attributeName.equals("id")) {
                return "[ERROR] Cannot update 'id' column.";
            }
            if (!table.getColNames().contains(pair.attributeName)) {
                return "[ERROR] Column '" + pair.attributeName + "' does not exist in table '" + resolvedName + "'.";
            }
        }

        BiPredicate<Integer, Row> predicate;
        try {
            predicate = stmt.condition.accept(new PredicateExprVisitor());
        } catch (Exception e) {
            return "[ERROR] Invalid WHERE condition: " + e.getMessage();
        }

        for (Integer id : table.getRowIds()) {
            Row row = table.getRow(id);
            if (predicate.test(id, row)) {
                for (NameValuePair pair : stmt.nameValueList) {
                    String newValue = pair.value.value;
                    row.setValue(pair.attributeName, newValue != null ? newValue : "NULL");
                }
            }
        }

        try {
            Writer.writeTable(dbFolder, resolvedName, table);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        return "[OK]";
    }

    @Override
    public String visit(Stmt.Join stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }

        String resolved1 = resolveTableName(dbFolder, stmt.table1Name);
        if (resolved1 == null) {
            return "[ERROR] Table '" + stmt.table1Name + "' does not exist.";
        }
        String resolved2 = resolveTableName(dbFolder, stmt.table2Name);
        if (resolved2 == null) {
            return "[ERROR] Table '" + stmt.table2Name + "' does not exist.";
        }

        Table table1, table2;
        try {
            table1 = Reader.readTable(dbFolder, resolved1);
            table2 = Reader.readTable(dbFolder, resolved2);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table1 == null || table2 == null) {
            return "[ERROR] One or more tables could not be read.";
        }

        boolean attr1IsId = stmt.attr1Name.equalsIgnoreCase("id");
        boolean attr2IsId = stmt.attr2Name.equalsIgnoreCase("id");

        if (!attr1IsId && !table1.getColNames().contains(stmt.attr1Name)) {
            return "[ERROR] Table '" + resolved1 + "' does not contain attribute '" + stmt.attr1Name + "'.";
        }
        if (!attr2IsId && !table2.getColNames().contains(stmt.attr2Name)) {
            return "[ERROR] Table '" + resolved2 + "' does not contain attribute '" + stmt.attr2Name + "'.";
        }

        // Build result column list: resolved1.col for each col in table1, then resolved2.col for each col in table2.
        // The id columns from each table are excluded (they are not in getColNames()).
        Table result = new Table();
        for (String col : table1.getColNames()) {
            result.addCol(resolved1 + "." + col);
        }
        for (String col : table2.getColNames()) {
            result.addCol(resolved2 + "." + col);
        }

        int newId = 1;
        for (Integer id1 : table1.getRowIds()) {
            Row row1 = table1.getRow(id1);
            String val1 = attr1IsId ? String.valueOf(id1) : row1.getValue(stmt.attr1Name);

            for (Integer id2 : table2.getRowIds()) {
                Row row2 = table2.getRow(id2);
                String val2 = attr2IsId ? String.valueOf(id2) : row2.getValue(stmt.attr2Name);

                if (valuesMatch(val1, val2)) {
                    Row joined = new Row();
                    for (String col : table1.getColNames()) {
                        joined.setValue(resolved1 + "." + col, row1.getValue(col));
                    }
                    for (String col : table2.getColNames()) {
                        joined.setValue(resolved2 + "." + col, row2.getValue(col));
                    }
                    result.addRow(newId++, joined);
                }
            }
        }

        return "[OK]\n" + result.toTabString();
    }

    // Compares two join-key values: numeric equality if both parse as numbers, else string equality.
    private boolean valuesMatch(String a, String b) {
        if (a == null || b == null) return false;
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            return Double.compare(da, db) == 0;
        } catch (NumberFormatException e) {
            return a.equals(b);
        }
    }

    @Override
    public String visit(Stmt.Delete stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        String resolvedName = resolveTableName(dbFolder, stmt.tableName);
        if (resolvedName == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, resolvedName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        BiPredicate<Integer, Row> predicate;
        try {
            predicate = stmt.condition.accept(new PredicateExprVisitor());
        } catch (Exception e) {
            return "[ERROR] Invalid WHERE condition: " + e.getMessage();
        }

        List<Integer> toDelete = new ArrayList<>();
        for (Integer id : table.getRowIds()) {
            if (predicate.test(id, table.getRow(id))) {
                toDelete.add(id);
            }
        }

        for (Integer id : toDelete) {
            table.removeRow(id);
        }

        try {
            Writer.writeTable(dbFolder, resolvedName, table);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        return "[OK]";
    }
}
