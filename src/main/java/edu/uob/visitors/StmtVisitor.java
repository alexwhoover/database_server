package edu.uob.visitors;

import edu.uob.nodes.Stmt;

public interface StmtVisitor<T> {
    T visit(Stmt.Use stmt);
}
