package edu.uob.visitors;

import edu.uob.ds.Row;
import edu.uob.nodes.Expr;

import java.util.function.Predicate;

public class EvaluateExprVisitor implements ExprVisitor<Object> {
    @Override
    public Object visit(Expr.Binary expr) {
        /*
         * Expr left;
         * Expr.Binary.Op op;
         * Expr right;
         */
        // TODO: Fill this in
        return null;
    }

    @Override
    public Object visit(Expr.Attr expr) {
        return expr.attrName;
    }

    @Override
    public Object visit(Expr.Literal expr) {
        return expr.value;
    }
}
