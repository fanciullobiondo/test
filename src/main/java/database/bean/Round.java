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
public class Round extends Entity {

    private int idRound;
    private int sequence;
    private int league;
    private int idSeason;
    private boolean played;

     @Override
    public Round fromResultSet(ResultSet rs) throws SQLException {
        int i = 1;
        this.idRound = rs.getInt(i++);
        this.sequence = rs.getInt(i++);
        this.league = rs.getInt(i++);
        this.idSeason = rs.getInt(i++);
        this.played = rs.getInt(i++) != 0;
        return this;
    }

    public int getIdRound() {
        return idRound;
    }

    public void setIdRound(int idRound) {
        this.idRound = idRound;
    }

    public int getSequence() {
        return sequence;
    }

    public void setSequence(int sequence) {
        this.sequence = sequence;
    }

    public int getLeague() {
        return league;
    }

    public void setLeague(int league) {
        this.league = league;
    }

    public int getIdSeason() {
        return idSeason;
    }

    public void setIdSeason(int idSeason) {
        this.idSeason = idSeason;
    }

    public boolean isPlayed() {
        return played;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }


    public static QueryBuilder<Round> builder = new QueryBuilder<Round>() {

        @Override
        public int FILL_STATEMENT(PreparedStatement ps, Round bean) throws SQLException {
            int i = 1;
            ps.setInt(i++, bean.sequence);
            ps.setInt(i++, bean.league);
            ps.setInt(i++, bean.idSeason);
            ps.setInt(i++, bean.played ? 1 : 0);
            return i;

        }

        @Override
        public String TABLE() {
            return "ROUND";
        }

        @Override
        public String SELECT_HEADER() {
            return "idround,sequence,league,idseason,played";
        }

        @Override
        public String UPDATE() {
            return "sequence=?,league=?,idseason=?,played=?";
        }

        @Override
        public String INSERT_COUNT_ELEMENTS() {
            return "sequence,league,idseason,played";
        }

        @Override
        public String CREATE() {
            return "create table " + TABLE() + " ("
                + "idround integer primary key autoincrement,"
                + "sequence int not null,"
                + "league int not null,"
                + "idseason int not null,"
                + "played int not null"
                + ")";

        }

    };

    @Override
    public QueryBuilder<Round> getBuilder() {
        return builder;
    }
}
