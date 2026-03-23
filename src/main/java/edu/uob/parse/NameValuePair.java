package edu.uob.parse;

import edu.uob.nodes.Expr;

public class NameValuePair {
    public final String attributeName;
    public final Expr.Literal value;

    public NameValuePair(String attributeName, Expr.Literal value) {
        this.attributeName = attributeName;
        this.value = value;
    }
}