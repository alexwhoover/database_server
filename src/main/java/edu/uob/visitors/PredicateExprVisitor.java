package edu.uob.visitors;

import edu.uob.ds.Row;
import edu.uob.nodes.Expr;

import java.util.function.BiPredicate;

public class PredicateExprVisitor implements ExprVisitor<BiPredicate<Integer, Row>> {

    /*
     * Builds a RowEvaluator from a leaf node (Expr.Attr or Expr.Literal).
     * This is called by Binary when it needs to compare two sides.
     */
    private RowEvaluator buildEvaluator(Expr expr) {
        if (expr instanceof Expr.Attr attr) {
            if (attr.attrName.equals("id")) {
                return (id, row) -> String.valueOf(id); // I know this is ugly, I'm going from String -> Integer -> String -> Double, new casting record!!
            }
            return (id, row) -> row.getValue(attr.attrName);
        } else if (expr instanceof Expr.Literal lit) {
            return (id, row) -> lit.value; // Assumes no null values
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
            case LT   -> (id, row) -> compare(left.evaluate(id, row), right.evaluate(id, row)) < 0;
            case GT   -> (id, row) -> compare(left.evaluate(id, row), right.evaluate(id, row)) > 0;
            case LTE  -> (id, row) -> compare(left.evaluate(id, row), right.evaluate(id, row)) <= 0;
            case GTE  -> (id, row) -> compare(left.evaluate(id, row), right.evaluate(id, row)) >= 0;
            case LIKE -> (id, row) -> likeMatch(left.evaluate(id, row), right.evaluate(id, row));
            default   -> throw new IllegalArgumentException("Unhandled op: " + expr.op);
        };
    }

    /*
     * Attr and Literal are not valid as standalone predicates.
     * They should only appear as children of a Binary comparison node.
     */
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
        // Dangerous casting, beware!
        try {
            double da = Double.parseDouble(a);
            double db = Double.parseDouble(b);
            return Double.compare(da, db);
        } catch (NumberFormatException e) {
            return a.compareTo(b);
        }
    }

    /*
     * SQL LIKE: supports % (any sequence) and _ (any single char).
     * Converts the pattern to a regex.
     */
    private boolean likeMatch(String value, String pattern) {
        String regex = "^" + pattern
                .replace("\\", "\\\\")
                .replace(".", "\\.")
                .replace("%", ".*")
                .replace("_", ".")
                + "$";
        return value.matches(regex);
    }
}