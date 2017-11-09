/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 *
 * @author nicolo.boschi
 */
public interface QueryBuilder<T extends Entity> {

    public int FILL_STATEMENT(PreparedStatement ps, T bean) throws SQLException;

    public String TABLE();

    public String SELECT_HEADER();

    public String UPDATE();

    public String INSERT_COUNT_ELEMENTS();

    public String CREATE();

    public static <T extends Entity> int fillStatement(PreparedStatement ps, QueryBuilder<T> build, T t) throws SQLException {
        return build.FILL_STATEMENT(ps, t);
    }

    public static <T extends Entity> String buildSelect(QueryBuilder<T> build, WhereClause where) {
        StringBuilder b = new StringBuilder();
        b.append("SELECT ");
        b.append(build.SELECT_HEADER());
        b.append(" FROM ");
        b.append(build.TABLE());
        b.append(" ");
        if (where != null) {
            b.append(where.get());
        }
        return b.toString();
    }

    public static <T extends Entity> String buildInsert(QueryBuilder<T> build) {
        StringBuilder b = new StringBuilder();
        b.append("INSERT INTO ");
        b.append(build.TABLE());
        b.append(" (");
        b.append(build.INSERT_COUNT_ELEMENTS());
        b.append(") VALUES (");
        b.append(Arrays
            .asList(build
                .INSERT_COUNT_ELEMENTS()
                .split("\\s*,\\s*"))
            .stream()
            .map(f -> "?")
            .collect(Collectors.joining(",")));

        b.append(")");
        return b.toString();
    }

    public static <T extends Entity> String buildUpdate(QueryBuilder<T> build, WhereClause where) {
        StringBuilder b = new StringBuilder();
        b.append("UPDATE ");
        b.append(build.TABLE());
        b.append(" SET ");
        b.append(build.UPDATE());
        if (where != null) {
            b.append(where.get());
        }
        return b.toString();
    }

    public static <T extends Entity> String buildTableDefinition(QueryBuilder<T> build) {
        return build.CREATE();
    }

    public class WhereClause {

        @Override
        public String toString() {
            return get();
        }

        public static WhereClause instance() {
            return new WhereClause();
        }

        StringBuilder text = new StringBuilder(" WHERE ");

        public WhereClause setText(String text) {
            this.text = new StringBuilder(text);
            return this;
        }

        public String get() {
            return text.toString();
        }

        public WhereClause field(String fieldName, String op) {
            text.append(" ");
            text.append(fieldName);
            text.append(op);
            text.append("? ");
            return this;
        }

        public WhereClause and() {
            text.append(" AND ");
            return this;
        }

        public WhereClause or() {
            text.append(" OR ");
            return this;
        }
    }

}
