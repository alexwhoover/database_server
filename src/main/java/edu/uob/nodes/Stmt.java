package edu.uob.nodes;

import edu.uob.parse.NameValuePair;
import edu.uob.visitors.StmtVisitor;

import java.util.List;
import java.util.Objects;

public abstract class Stmt {
    public abstract <T> T accept(StmtVisitor<T> v);
    public abstract boolean equals(Object o);

    public static class Select extends Stmt {
        public final String tableName;
        public final List<String> attributeList; // Empty = *
        public final Expr condition;

        public Select(String tableName, List<String> attributeList, Expr condition) {
            this.tableName = tableName;
            this.attributeList = attributeList;
            this.condition = condition;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Select other = (Stmt.Select) o;

            return this.tableName.equals(other.tableName)
                    && this.attributeList.equals(other.attributeList)
                    && Objects.equals(this.condition, other.condition);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName, attributeList, condition); }
    }

    public static class Use extends Stmt {
        public final String dbName;

        public Use(String dbName) {
            this.dbName = dbName;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Use other = (Stmt.Use) o;
            return this.dbName.equals(other.dbName);
        }

        @Override
        public int hashCode() { return Objects.hash(dbName); }
    }

    public static class CreateDatabase extends Stmt {
        public final String dbName;

        public CreateDatabase(String dbName) {this.dbName = dbName; }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.CreateDatabase other = (Stmt.CreateDatabase) o;
            return this.dbName.equals(other.dbName);
        }

        @Override
        public int hashCode() { return Objects.hash(dbName); }
    }

    public static class DropDatabase extends Stmt {
        public final String dbName;

        public DropDatabase(String dbName) {this.dbName = dbName; }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.DropDatabase other = (Stmt.DropDatabase) o;
            return this.dbName.equals(other.dbName);
        }

        @Override
        public int hashCode() { return Objects.hash(dbName); }
    }

    public static class CreateTable extends Stmt {
        public final String tableName;
        public final List<String> attributeList;

        public CreateTable(String tableName, List<String> attributeList) {
            this.tableName = tableName;
            this.attributeList = attributeList;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.CreateTable other = (Stmt.CreateTable) o;
            return this.tableName.equals(other.tableName)
                    && this.attributeList.equals(other.attributeList);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName, attributeList); }
    }

    public static class DropTable extends Stmt {
        public final String tableName;

        public DropTable(String tableName) {this.tableName = tableName; }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.DropTable other = (Stmt.DropTable) o;
            return this.tableName.equals(other.tableName);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName); }
    }

    public static class Alter extends Stmt {
        public enum AltType {
            ADD, DROP
        }

        public final String tableName;
        public final AltType altType; // ADD or DROP
        public final String attributeName;

        public Alter(String tableName, AltType altType, String attributeName) {
            this.tableName = tableName;
            this.altType = altType;
            this.attributeName = attributeName;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Alter other = (Stmt.Alter) o;
            return this.tableName.equals(other.tableName) &&
                    this.altType == other.altType &&
                    this.attributeName.equals(other.attributeName);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName, altType, attributeName); }
    }

    public static class Insert extends Stmt {
        public final String tableName;
        public final List<Expr.Literal> valueList;

        public Insert(String tableName, List<Expr.Literal> valueList) {
            this.tableName = tableName;
            this.valueList = valueList;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Insert other = (Stmt.Insert) o;
            return this.tableName.equals(other.tableName) &&
                    this.valueList.equals(other.valueList);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName, valueList); }
    }

    public static class Update extends Stmt {
        public final String tableName;
        public final List<NameValuePair> nameValueList;
        public final Expr condition;

        public Update(String tableName, List<NameValuePair> nameValueList, Expr condition) {
            this.tableName = tableName;
            this.nameValueList = nameValueList;
            this.condition = condition;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Update other = (Stmt.Update) o;
            return this.tableName.equals(other.tableName)
                    && this.nameValueList.equals(other.nameValueList)
                    && Objects.equals(this.condition, other.condition);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName, nameValueList, condition); }
    }

    public static class Delete extends Stmt {
        public final String tableName;
        public final Expr condition;

        public Delete(String tableName, Expr condition) {
            this.tableName = tableName;
            this.condition = condition;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Delete other = (Stmt.Delete) o;
            return this.tableName.equals(other.tableName)
                    && Objects.equals(this.condition, other.condition);
        }

        @Override
        public int hashCode() { return Objects.hash(tableName, condition); }
    }

    public static class Join extends Stmt {
        public final String table1Name;
        public final String table2Name;
        public final String attr1Name;
        public final String attr2Name;

        public Join(String table1Name, String table2Name, String attr1Name, String attr2Name) {
            this.table1Name = table1Name;
            this.table2Name = table2Name;
            this.attr1Name = attr1Name;
            this.attr2Name = attr2Name;
        }

        @Override
        public <T> T accept(StmtVisitor<T> v) {
            return v.visit(this);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null) return false;
            if (o.getClass() != this.getClass()) return false;
            Stmt.Join other = (Stmt.Join) o;
            return this.table1Name.equals(other.table1Name)
                    && this.table2Name.equals(other.table2Name)
                    && this.attr1Name.equals(other.attr1Name)
                    && this.attr2Name.equals(other.attr2Name);
        }

        @Override
        public int hashCode() { return Objects.hash(table1Name, table2Name, attr1Name, attr2Name); }
    }
}
