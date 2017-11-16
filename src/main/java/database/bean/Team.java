/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database.bean;

import data.EmbeddedData;
import static database.bean.Match.builder;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 *
 * @author nicolo.boschi
 */
public class Team extends Entity {

    private int idteam;
    private String name;
    private int league;
    private boolean ofuser;
    private int defence;
    private int attack;

    public Team() {
    }

    public static Team of(EmbeddedData.Team t) {
        Team team = new Team();
        team.setLeague(t.getLeague().getId());
        team.setName(t.getName());
        team.setDefence(t.getDefence());
        team.setAttack(t.getAttack());
        return team;
    }

     @Override
    public Team fromResultSet(ResultSet rs) throws SQLException {
        int i = 1;
        this.idteam = rs.getInt(i++);
        this.name = rs.getString(i++);
        this.league = rs.getInt(i++);
        this.ofuser = rs.getInt(i++) != 0;
        this.defence = rs.getInt(i++);
        this.attack = rs.getInt(i++);
        return this;
    }

    public int getIdteam() {
        return idteam;
    }

    public void setIdteam(int idteam) {
        this.idteam = idteam;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLeague() {
        return league;
    }

    public void setLeague(int league) {
        this.league = league;
    }

    public boolean isOfuser() {
        return ofuser;
    }

    public void setOfuser(boolean ofuser) {
        this.ofuser = ofuser;
    }

    public int getDefence() {
        return defence;
    }

    public void setDefence(int defence) {
        this.defence = defence;
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        this.attack = attack;
    }

   

    public static QueryBuilder<Team> builder = new QueryBuilder<Team>() {

        @Override
        public int FILL_STATEMENT(PreparedStatement ps, Team bean) throws SQLException {
            int i = 1;
            ps.setString(i++, bean.name);
            ps.setInt(i++, bean.league);
            ps.setInt(i++, bean.ofuser ? 1 : 0);
            ps.setInt(i++, bean.defence);
            ps.setInt(i++, bean.attack);
            return i;
        }

        @Override
        public String TABLE() {
            return "TEAM";
        }

        @Override
        public String SELECT_HEADER() {
            return "idteam,name,league,ofuser,defence,attack";
        }

        @Override
        public String UPDATE() {
            return "name=?,league=?,ofuser=?,defence=?,attack=?";
        }

        @Override
        public String INSERT_COUNT_ELEMENTS() {
            return "name,league,ofuser,defence,attack";
        }

        @Override
        public String CREATE() {
            return "create table " + TABLE() + "("
                + "idteam integer primary key autoincrement,"
                + "name text not null,"
                + "league int not null,"
                + "ofuser int not null,"
                + "defence int not null,"
                + "attack int not null)";

        }

    };

    @Override
    public QueryBuilder<Team> getBuilder() {
        return builder;
    }

}
