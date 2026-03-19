package edu.uob.visitors;

import edu.uob.nodes.Expr;

public interface ExprVisitor<T> {
    T visit(Expr.Binary expr);
    T visit(Expr.Attr expr);
    T visit(Expr.Literal expr);
}
