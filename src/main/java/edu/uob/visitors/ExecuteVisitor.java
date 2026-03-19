package edu.uob.visitors;

import edu.uob.DBServer;
import edu.uob.ds.Table;
import edu.uob.nodes.Stmt;

public class ExecuteVisitor implements StmtVisitor<Table> {
    private final DBServer server;

    public ExecuteVisitor(DBServer server) {
        this.server = server;
    }

    @Override
    public Table visit(Stmt.Use stmt) {
        server.setDatabaseName(stmt.dbName);
        return null;
    }

}
