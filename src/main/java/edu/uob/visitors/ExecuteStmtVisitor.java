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
import java.util.function.Predicate;

public class ExecuteStmtVisitor implements StmtVisitor<String> {
    private final DBServer server;

    public ExecuteStmtVisitor(DBServer server) {
        this.server = server;
    }

    @Override
    public String visit(Stmt.Use stmt) {
        server.setDatabaseName(stmt.dbName);
        return "[OK]";
    }

    @Override
    public String visit(Stmt.CreateDatabase stmt) {
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
        Writer.deleteTable(dbFolder, stmt.tableName);
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
        List<String> availTables = Reader.readTableNames(dbFolder);
        if (!availTables.contains(stmt.tableName)) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table raw;
        try {
            raw = Reader.readTable(dbFolder, stmt.tableName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (raw == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        // Check that attribute names exist in table
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
        List<String> availTables = Reader.readTableNames(dbFolder);
        if (!availTables.contains(stmt.tableName)) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, stmt.tableName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        if (stmt.altType == Stmt.Alter.AltType.ADD) {
            if (table.getColNames().contains(stmt.attributeName)) {
                return "[ERROR] Column '" + stmt.attributeName + "' already exists in table '" + stmt.tableName + "'.";
            }
            table.addCol(stmt.attributeName);
        } else {
            if (!table.getColNames().contains(stmt.attributeName)) {
                return "[ERROR] Column '" + stmt.attributeName + "' does not exist in table '" + stmt.tableName + "'.";
            }
            table.dropCol(stmt.attributeName);
        }

        try {
            Writer.writeTable(dbFolder, stmt.tableName, table);
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
        List<String> availTables = Reader.readTableNames(dbFolder);
        if (!availTables.contains(stmt.tableName)) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, stmt.tableName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        // Check value count matches column count
        List<String> cols = table.getColNames();
        if (stmt.valueList.size() != cols.size()) {
            return "[ERROR] Expected " + cols.size() + " values, got " + stmt.valueList.size();
        }

        // Build and add the row
        Row row = new Row();
        for (int i = 0; i < cols.size(); i++) {
            Object value = stmt.valueList.get(i).value;
            row.setValue(cols.get(i), value == null ? "NULL" : value.toString());
        }

        table.addRow(table.getNextId(), row);

        try {
            Writer.writeTable(dbFolder, stmt.tableName, table);
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
        List<String> availTables = Reader.readTableNames(dbFolder);
        if (!availTables.contains(stmt.tableName)) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, stmt.tableName);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        if (table == null) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        // Validate all pairs before applying any changes
        for (NameValuePair pair : stmt.nameValueList) {
            if (pair.attributeName.equals("id")) {
                return "[ERROR] Cannot update 'id' column.";
            }
            if (!table.getColNames().contains(pair.attributeName)) {
                return "[ERROR] Column '" + pair.attributeName + "' does not exist in table '" + stmt.tableName + "'.";
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
                    row.setValue(pair.attributeName, newValue);
                }
            }
        }

        try {
            Writer.writeTable(dbFolder, stmt.tableName, table);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        return "[OK]";
    }

    @Override
    public String visit(Stmt.Delete stmt) {
        File dbFolder;
        try {
            dbFolder = server.getDatabaseFolder();
        } catch (IllegalStateException e) {
            return "[ERROR] " + e.getMessage();
        }
        List<String> availTables = Reader.readTableNames(dbFolder);
        if (!availTables.contains(stmt.tableName)) {
            return "[ERROR] Table '" + stmt.tableName + "' does not exist.";
        }

        Table table;
        try {
            table = Reader.readTable(dbFolder, stmt.tableName);
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

        // Collect ids to delete first to avoid modifying the map while iterating
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
            Writer.writeTable(dbFolder, stmt.tableName, table);
        } catch (IOException e) {
            return "[ERROR] " + e.getMessage();
        }

        return "[OK]";
    }
}
