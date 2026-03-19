package edu.uob.nodes;

import edu.uob.visitors.StmtVisitor;

import java.util.List;

public abstract class Stmt {
    public abstract <T> T accept(StmtVisitor<T> v);

//    public static class Select extends Stmt {
//        /*
//         * SELECT command should result in following tree structure:
//         * Stmt.Select
//         * -> List<String> attributeList
//         * -> String tableName
//         * -> (optional) [Expr.AND || Expr.OR || ...]
//         */
//        public final String tableName;
//        public final List<String> attributeList; // Empty = *
//        public final Expr condition;
//
//        public Select(String tableName, List<String> attributeList, Expr condition) {
//            this.tableName = tableName;
//            this.attributeList = attributeList;
//            this.condition = condition;
//        }
//
//        @Override
//        public <T> T accept(StmtVisitor<T> v) {
//            return v.visit(this);
//        }
//    }

    public static class Use extends Stmt {
        /*
         * USE command should result in following tree structure:
         * Stmt.Use
         * -> String dbName
         */
        public final String dbName;

        public Use(String dbName) {
            this.dbName = dbName;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }
    }
}
