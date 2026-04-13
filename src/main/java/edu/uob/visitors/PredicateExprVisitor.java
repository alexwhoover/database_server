package edu.uob.visitors;

import edu.uob.ds.Row;
import edu.uob.nodes.Expr;

import java.util.function.BiPredicate;

public class PredicateExprVisitor implements ExprVisitor<BiPredicate<Integer, Row>> {

    /*
     * Builds a RowEvaluator from a leaf node (Expr.Attr or Expr.Literal).
     * Null row values (e.g. columns added after row insertion) are treated as "NULL".
     */
    private RowEvaluator buildEvaluator(Expr expr) {
        if (expr instanceof Expr.Attr attr) {
            if (attr.attrName.equals("id")) {
                return (id, row) -> String.valueOf(id);
            }
            return (id, row) -> {
                String val = row.getValue(attr.attrName);
                return val != null ? val : "NULL";
            };
        } else if (expr instanceof Expr.Literal lit) {
            return (id, row) -> lit.value;
        } else {
            throw new IllegalArgumentException("Expected Attr or Literal in comparison, got: " + expr.getClass());
        }
    }

    @Override
    public BiPredicate<Integer, Row> visit(Expr.Binary expr) {
        if (expr.op == Expr.Binary.Op.AND) {
            BiPredicate<Integer, Row> left = expr.left.accept(this);
            BiPredicate<Integer, Row> right = expr.right.accept(this);
            return (id, row) -> left.test(id, row) && right.test(id, row);
        }
        if (expr.op == Expr.Binary.Op.OR) {
            BiPredicate<Integer, Row> left = expr.left.accept(this);
            BiPredicate<Integer, Row> right = expr.right.accept(this);
            return (id, row) -> left.test(id, row) || right.test(id, row);
        }

        RowEvaluator left = buildEvaluator(expr.left);
        RowEvaluator right = buildEvaluator(expr.right);

        return switch (expr.op) {
            case EQ   -> (id, row) -> compare(left.evaluate(id, row), right.evaluate(id, row)) == 0;
            case NEQ  -> (id, row) -> compare(left.evaluate(id, row), right.evaluate(id, row)) != 0;
            case LT   -> (id, row) -> {
                String l = left.evaluate(id, row);
                String r = right.evaluate(id, row);
                if (areIncompatibleTypes(l, r)) return false;
                return compare(l, r) < 0;
            };
            case GT   -> (id, row) -> {
                String l = left.evaluate(id, row);
                String r = right.evaluate(id, row);
                if (areIncompatibleTypes(l, r)) return false;
                return compare(l, r) > 0;
            };
            case LTE  -> (id, row) -> {
                String l = left.evaluate(id, row);
                String r = right.evaluate(id, row);
                if (areIncompatibleTypes(l, r)) return false;
                return compare(l, r) <= 0;
            };
            case GTE  -> (id, row) -> {
                String l = left.evaluate(id, row);
                String r = right.evaluate(id, row);
                if (areIncompatibleTypes(l, r)) return false;
                return compare(l, r) >= 0;
            };
            case LIKE -> (id, row) -> likeMatch(left.evaluate(id, row), right.evaluate(id, row));
            default   -> throw new IllegalArgumentException("Unhandled op: " + expr.op);
        };
    }

    @Override
    public BiPredicate<Integer, Row> visit(Expr.Attr expr) {
        throw new IllegalStateException("Attr node cannot be evaluated as a standalone predicate");
    }

    @Override
    public BiPredicate<Integer, Row> visit(Expr.Literal expr) {
        throw new IllegalStateException("Literal node cannot be evaluated as a standalone predicate");
    }

    /*
     * Compares two string values. Tries numeric comparison first,
     * falls back to lexicographic.
     */
    private int compare(String a, String b) {
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            return Double.compare(da, db);
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

    /*
     * Returns true if one value is numeric and the other is not.
     * Used to short-circuit ordered comparisons on incompatible types.
     */
    private boolean areIncompatibleTypes(String a, String b) {
        return isNumeric(a) != isNumeric(b);
    }

    private boolean isNumeric(String s) {
        if (s == null) return false;
        try {
            Double.parseDouble(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /*
     * LIKE: checks whether value contains pattern as a substring.
     */
    private boolean likeMatch(String value, String pattern) {
        return value.contains(pattern);
    }
}
