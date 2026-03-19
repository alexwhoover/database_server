package edu.uob.nodes;

import edu.uob.visitors.ExprVisitor;

public abstract class Expr {
    public abstract <T> T accept(ExprVisitor<T> v);

    public static class Binary extends Expr {
        public enum Op {
            AND, OR, EQ, NEQ, LT, GT, LTE, GTE, LIKE,
        }

        public final Expr left;
        public final Op op;
        public final Expr right;

        public Binary(Expr left, Op op, Expr right) {
            this.left = left;
            this.op = op;
            this.right = right;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visit(this);
        }
    }

    public static class Attr extends Expr {
        public final String attrName;

        public Attr(String attrName) {
            this.attrName = attrName;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visit(this);
        }
    }

    public static class Literal extends Expr {
        //
        // Will need to update parser to use ex. Integer.parseInt
        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visit(this);
        }
    }
}
