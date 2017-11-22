/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import static database.bean.Match.builder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nicolo.boschi
 */
public class Season extends Entity{

    private int idSeason;
    private int sequence;

     @Override
    public Season fromResultSet(ResultSet rs) throws SQLException {
        int i = 1;
        this.idSeason = rs.getInt(i++);
        this.sequence = rs.getInt(i++);
        return this;
    }

    public Season() {
    }

    

    public int getIdSeason() {
        return idSeason;
    }

    public int getSequence() {
        return sequence;
    }

    public void setIdSeason(int idSeason) {
        this.idSeason = idSeason;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public static QueryBuilder<Season> builder = new QueryBuilder<Season>() {

        @Override
        public int FILL_STATEMENT(PreparedStatement ps, Season bean) throws SQLException {
            int i = 1;
            ps.setInt(i++, bean.sequence);
            return i;
        }

        @Override
        public String TABLE() {
            return "SEASON";
        }

        @Override
        public String SELECT_HEADER() {
            return "idSeason,sequence";
        }

        @Override
        public String UPDATE() {
            return "sequence=?";
        }

        @Override
        public String INSERT_COUNT_ELEMENTS() {
            return "sequence";
        }

        @Override
        public String CREATE() {
            return "create table " + TABLE() + " ("
                + "idSeason integer primary key autoincrement,"
                + "sequence int not null)";

        }

    };

     @Override
    public QueryBuilder<Season> getBuilder() {
        return builder;
    }

}
