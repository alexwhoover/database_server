package edu.uob.visitors;

public interface ASTVisitor<T> {
    T visitStatement(StatementNode node);
    T visitClause(ClauseNode node);
    T visitExpr(ExprNode node);
    T visitLeaf(LeafNode node);
}