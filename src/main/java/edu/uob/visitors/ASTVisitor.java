package edu.uob.visitors;

import edu.uob.nodes.ClauseNode;
import edu.uob.nodes.ExprNode;
import edu.uob.nodes.LeafNode;
import edu.uob.nodes.StatementNode;

public interface ASTVisitor<T> {
    T visitStatement(StatementNode node);
    T visitClause(ClauseNode node);
    T visitExpr(ExprNode node);
    T visitLeaf(LeafNode node);
}