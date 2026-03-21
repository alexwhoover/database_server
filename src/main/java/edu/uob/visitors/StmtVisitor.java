package edu.uob.visitors;

import edu.uob.nodes.Stmt;

public interface StmtVisitor<T> {
    T visit(Stmt.Use stmt);
    T visit(Stmt.CreateDatabase stmt);
    T visit(Stmt.DropDatabase stmt);
    T visit(Stmt.CreateTable stmt);
    T visit(Stmt.DropTable stmt);
    T visit(Stmt.Select stmt);
    T visit(Stmt.Alter stmt);
//    T visit(Stmt.Insert stmt);
//    T visit(Stmt.Update stmt);
//    T visit(Stmt.Delete stmt);
}
