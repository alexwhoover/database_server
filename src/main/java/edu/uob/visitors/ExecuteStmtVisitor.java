package edu.uob.visitors;

import edu.uob.DBServer;
import edu.uob.ds.Row;
import edu.uob.ds.Table;
import edu.uob.nodes.Stmt;
import edu.uob.io.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
        try {
            Writer.writeTable(server.getDatabaseFolder(), stmt.tableName, new Table());
        } catch (IOException e) {
            return "[ERROR] File could not be opened for write for table " + stmt.tableName;
        }
        return "[OK]";
    }

    @Override
    public String visit(Stmt.DropTable stmt) {
        Writer.deleteTable(server.getDatabaseFolder(), stmt.tableName);
        return "[OK]";
    }

    @Override
    public String visit(Stmt.Select stmt) {
        File dbFolder = server.getDatabaseFolder();
        List<String> availTables = Reader.readTableNames(dbFolder);
        if (!availTables.contains(stmt.tableName)) return null;

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
            if (!raw.getColNames().contains(attr)) {
                return "[ERROR] Table '" + stmt.tableName + "' does not contain attribute " + attr;
            }
        }

        Table result = raw.project(stmt.attributeList);

        if (stmt.condition != null) {
            // TODO: Fill this in
        }
        return result.toTabString();
    }

    @Override
    public String visit(Stmt.Alter stmt) {
        return "";
    }
}
