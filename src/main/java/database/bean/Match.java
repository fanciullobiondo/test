/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import client.ApiClient;
import data.EmbeddedData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nicolo.boschi
 */
public class Match extends Entity {

    private int idmatch;
    private int idTeamHome;
    private int idTeamAway;
    private int goalHome;
    private int goalAway;
    private int idRound;
    private int subIdLeague;

    @Override
    public String toString() {
        return "Match{" + "idmatch=" + idmatch + ", idTeamHome=" + idTeamHome + ", idTeamAway=" + idTeamAway + ", goalHome=" + goalHome + ", goalAway=" + goalAway + ", idRound=" + idRound + ", subIdLeague=" + subIdLeague + '}';
    }

    public Match() {

    }

    public Match(ApiClient.RoundMatch rm) {
        this.idmatch = rm.getIdmatch();
        this.idTeamHome = rm.getHome().getId();
        this.idTeamAway = rm.getAway().getId();
        this.goalHome = rm.getHome().getGoal();
        this.goalAway = rm.getAway().getGoal();
        this.subIdLeague = rm.isEuropean() ? (rm.isCl() ? EmbeddedData.League.SUB_CHAMPIONSLEAGUE : EmbeddedData.League.SUB_EUROPALEAGUE) : EmbeddedData.League.SUB_NONE;
    }

    @Override
    public Match fromResultSet(ResultSet rs) throws SQLException {
        int i = 1;
        this.idmatch = rs.getInt(i++);
        this.idTeamHome = rs.getInt(i++);
        this.idTeamAway = rs.getInt(i++);
        this.goalHome = rs.getInt(i++);
        this.goalAway = rs.getInt(i++);
        this.idRound = rs.getInt(i++);
        this.subIdLeague = rs.getInt(i++);
        return this;
    }

    public int getSubIdLeague() {
        return subIdLeague;
    }

    public void setSubIdLeague(int subIdLeague) {
        this.subIdLeague = subIdLeague;
    }

    public int getIdmatch() {
        return idmatch;
    }

    public void setIdmatch(int idmatch) {
        this.idmatch = idmatch;
    }

    public int getIdTeamHome() {
        return idTeamHome;
    }

    public void setIdTeamHome(int idTeamHome) {
        this.idTeamHome = idTeamHome;
    }

    public int getIdTeamAway() {
        return idTeamAway;
    }

    public void setIdTeamAway(int idTeamAway) {
        this.idTeamAway = idTeamAway;
    }

    public int getGoalHome() {
        return goalHome;
    }

    public void setGoalHome(int goalHome) {
        this.goalHome = goalHome;
    }

    public int getGoalAway() {
        return goalAway;
    }

    public void setGoalAway(int goalAway) {
        this.goalAway = goalAway;
    }

    public int getIdRound() {
        return idRound;
    }

    public void setIdRound(int idRound) {
        this.idRound = idRound;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Match other = (Match) obj;
        if (this.idmatch != other.idmatch) {
            return false;
        }
        return true;
    }


    public static QueryBuilder<Match> builder = new QueryBuilder<Match>() {

        @Override
        public int FILL_STATEMENT(PreparedStatement ps, Match bean) throws SQLException {
            int i = 1;
            ps.setInt(i++, bean.idTeamHome);
            ps.setInt(i++, bean.idTeamAway);
            ps.setInt(i++, bean.goalHome);
            ps.setInt(i++, bean.goalAway);
            ps.setInt(i++, bean.idRound);
            ps.setInt(i++, bean.subIdLeague);
            return i;
        }

        @Override
        public String TABLE() {
            return "MATCH";
        }

        @Override
        public String SELECT_HEADER() {
            return "idmatch,idteamhome,idteamaway,goalhome,goalaway,idround,subIdLeague";
        }

        @Override
        public String UPDATE() {
            return "idteamhome=?,idteamaway=?,goalhome=?,goalaway=?,idround=?,subIdLeague=?";
        }

        @Override
        public String INSERT_COUNT_ELEMENTS() {
            return "idteamhome,idteamaway,goalhome,goalaway,idround,subIdLeague";
        }

        @Override
        public String CREATE() {
            return "create table " + TABLE() + " ("
                + "idmatch integer primary key autoincrement,"
                + "idteamhome int not null,"
                + "idteamaway int not null,"
                + "goalhome int ,"
                + "goalaway int,"
                + "subIdLeague int not null,"
                + "idround int not null)";

        }

    };

    @Override
    public QueryBuilder<Match> getBuilder() {
        return builder;
    }

}
