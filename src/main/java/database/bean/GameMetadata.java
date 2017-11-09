/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nicolo.boschi
 */
public class GameMetadata extends Entity {

    private int human;
    private int total;

    
    public static QueryBuilder<GameMetadata> builder = new QueryBuilder<GameMetadata>() {

        @Override
        public int FILL_STATEMENT(PreparedStatement ps, GameMetadata bean) throws SQLException {
            int i = 1;
            ps.setInt(i++, bean.human);
            ps.setInt(i++, bean.total);
            return i;
        }

        @Override
        public String TABLE() {
            return "GAME_METADATA";
        }

        @Override
        public String SELECT_HEADER() {
            return "human,total";
        }

        @Override
        public String UPDATE() {
            return "human=?,total=?";
        }

        @Override
        public String INSERT_COUNT_ELEMENTS() {
            return SELECT_HEADER();
        }

        @Override
        public String CREATE() {
            return "create table " + TABLE() + "("
                + "human int not null,"
                + "total int not null)";

        }

    };

    public GameMetadata(int human, int total) {
        this.human = human;
        this.total = total;
    }

    @Override
    public GameMetadata fromResultSet(ResultSet rs) throws SQLException {
        int i = 1;
        this.human = rs.getInt(i++);
        this.total = rs.getInt(i++);
        return this;
    }

    public int getHuman() {
        return human;
    }

    public void setHuman(int human) {
        this.human = human;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public void fillInsert(PreparedStatement ps) throws SQLException {
        int i = 1;
        ps.setInt(i++, human);
        ps.setInt(i++, total);
    }

    @Override
    public QueryBuilder<GameMetadata> getBuilder() {
        return builder;
    }

}
