/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import client.ApiClient;
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


    public Match() {
        
    }
    public Match(ApiClient.RoundMatch rm) {
        this.idmatch = rm.getIdmatch();
        this.goalHome = rm.getHome().getGoal();
        this.goalAway = rm.getAway().getGoal();
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
        return this;
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

    public static QueryBuilder<Match> builder = new QueryBuilder<Match>() {

        @Override
        public int FILL_STATEMENT(PreparedStatement ps, Match bean) throws SQLException {
            int i = 1;
            ps.setInt(i++, bean.idTeamHome);
            ps.setInt(i++, bean.idTeamAway);
            ps.setInt(i++, bean.goalHome);
            ps.setInt(i++, bean.goalAway);
            ps.setInt(i++, bean.idRound);
            return i;
        }

        @Override
        public String TABLE() {
            return "MATCH";
        }

        @Override
        public String SELECT_HEADER() {
            return "idmatch,idteamhome,idteamaway,goalhome,goalaway,idround";
        }

        @Override
        public String UPDATE() {
            return "idteamhome=?,idteamaway=?,goalhome=?,goalaway=?,idround=?";
        }

        @Override
        public String INSERT_COUNT_ELEMENTS() {
            return "idteamhome,idteamaway,goalhome,goalaway,idround";
        }

        @Override
        public String CREATE() {
            return "create table " + TABLE() + " ("
                + "idmatch integer primary key autoincrement,"
                + "idteamhome int not null,"
                + "idteamaway int not null,"
                + "goalhome int ,"
                + "goalaway int,"
                + "idround int not null)";

        }

    };

    @Override
    public QueryBuilder<Match> getBuilder() {
        return builder;
    }

}
