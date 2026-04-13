package edu.uob.nodes;

import edu.uob.visitors.ExprVisitor;
import java.util.Objects;

public abstract class Expr {
    public abstract <T> T accept(ExprVisitor<T> v);
    public abstract boolean equals(Object o);

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

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Expr.Binary other = (Expr.Binary) o;

            return this.left.equals(other.left)
                    && this.op == other.op
                    && this.right.equals(other.right);
        }

        @Override
        public int hashCode() { return Objects.hash(left, op, right); }
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

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Expr.Attr other = (Expr.Attr) o;

            return this.attrName.equals(other.attrName);
        }

        @Override
        public int hashCode() { return Objects.hash(attrName); }
    }

    public static class Literal extends Expr {
        public final String value;

        public Literal(String value) {
            this.value = value;
        }

        @Override
        public <T> T accept(ExprVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Expr.Literal other = (Expr.Literal) o;

            return this.value.equals(other.value);
        }

        @Override
        public int hashCode() { return Objects.hash(value); }
    }
}
