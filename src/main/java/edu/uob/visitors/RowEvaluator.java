package edu.uob.visitors;

import edu.uob.ds.Row;

@FunctionalInterface
public interface RowEvaluator {
    /*
     * Represents an expression that can be evaluated against a row to produce a String value.
     * Used during WHERE clause evaluation to resolve the two sides of a comparison operator
     * before comparing them.
     */
    String evaluate(int id, Row row);
}
