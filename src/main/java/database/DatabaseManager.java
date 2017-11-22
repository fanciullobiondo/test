/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import data.LeagueTable;
import database.bean.Entity;
import database.bean.GameMetadata;
import database.bean.Match;
import database.bean.QueryBuilder;
import database.bean.Round;
import database.bean.Season;
import database.bean.Team;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author nicolo.boschi
 */
public class DatabaseManager {

    private final String databasePath;
    private final String databaseName;

    private String sanitifyName(String name) {
        return name.replaceAll("[^0-9a-zA-Z]+", "").toLowerCase() + ".db";
    }

    public DatabaseManager(String databasePath, final String databaseName) {
        this.databasePath = databasePath;
        this.databaseName = sanitifyName(databaseName);
    }

    public int getRelativeRoundIndex(int idround) throws SQLException {
        List<Round> queryEntity = queryEntity(Round.class, QueryBuilder.WhereClause.instance().field("idround", "="), idround);
        if (queryEntity.isEmpty()) {
            return -1;
        }
        int idSeason = queryEntity.get(0).getIdSeason();

        try (Connection con = openConnection();) {
            try (PreparedStatement psMatch = con
                .prepareStatement("Select distinct idround from " + Match.builder.TABLE() + " where idround IN "
                    + "(select idround from " + Round.builder.TABLE() + " where idseason = ? ) "
                    + " order by idround")) {
                psMatch.setInt(1, idSeason);
                int count = 1;
                try (ResultSet rs = psMatch.executeQuery();) {
                    while (rs.next()) {
                        if (rs.getInt(1) == idround) {
                            return count;
                        }
                        count++;
                    }
                }

            }
        }
        return -1;

    }

    public Team findTeamById(int id) throws SQLException {
        try (Connection con = openConnection();) {
            try (PreparedStatement psMatch = con
                .prepareStatement("Select  " + Team.builder.SELECT_HEADER() + " from " + Team.builder.TABLE() + " where idteam=?")) {
                psMatch.setInt(1, id);
                try (ResultSet rs = psMatch.executeQuery();) {
                    if (rs.next()) {
                        return new Team().fromResultSet(rs);
                    }
                }

            }
        }
        return null;
    }

    public List<Match> getAllMatchToSimulate(int idround) throws SQLException {
        List<Match> m = new ArrayList<>();
        try (Connection con = openConnection();) {
            try (PreparedStatement psMatch = con.prepareStatement("Select  " + Match.builder.SELECT_HEADER() + " from "
                + Match.builder.TABLE() + " where idround=? and "
                + "idTeamHome not in(select idteam from   " + Team.builder.TABLE() + " where ofuser=1 ) and "
                + "idTeamAway not in(select idteam from   " + Team.builder.TABLE() + " where ofuser=1)")) {
                psMatch.setInt(1, idround);
                try (ResultSet rs = psMatch.executeQuery();) {
                    while (rs.next()) {
                        m.add(new Match().fromResultSet(rs));
                    }
                }

            }
        }
        return m;

    }

    public void updateRound(List<Match> matches, int idround) throws SQLException {
        try (Connection con = openConnection();) {
            con.setAutoCommit(false);
            try (PreparedStatement ps = con.prepareStatement("Update " + Round.builder.TABLE() + " set played=1 where idround=?");
                PreparedStatement psMatch = con.prepareStatement("Update " + Match.builder.TABLE() + " set goalHome=?,goalAway=? "
                    + "where idmatch=?");) {
                try {

                    for (Match matche : matches) {
                        psMatch.setInt(1, matche.getGoalHome());
                        psMatch.setInt(2, matche.getGoalAway());
                        psMatch.setInt(3, matche.getIdmatch());
                        psMatch.executeUpdate();
                    }
                    ps.setInt(1, idround);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    con.rollback();
                    throw e;
                }
            }
            con.commit();
            con.setAutoCommit(true);
        }
    }

    public int getActualRound() throws SQLException {
        String query = "Select idround from " + Round.builder.TABLE() + " where played=0 order by idround asc LIMIT 1";
        try (Connection con = openConnection();
            PreparedStatement allTeams = con.prepareStatement(query);) {

            try (ResultSet rs = allTeams.executeQuery();) {
                while (rs.next()) {
                    return rs.getInt(1);
                }
                return -1;
            }

        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public LeagueTable calculateLeagueTable(int idseason, int idleague) throws SQLException {
        LeagueTable table = new LeagueTable();
        table.setIdseason(idseason);
        String queryHomeGoal = "select "
            + "(SUM(case when goalhome > goalaway THEN 1 ELSE 0 END)) as win, "
            + "(SUM(case when goalhome = goalaway THEN 1 ELSE 0 END)) as draw, "
            + "(SUM(case when goalhome < goalaway THEN 1 ELSE 0 END)) as lose, "
            + "SUM(goalhome) as gf, "
            + "SUM(goalaway) as gs "
            + " from " + Match.builder.TABLE()
            + " where idround in(select idround "
            + "from round "
            + "where idseason=? and league=? and played=1) and idteamhome=? ";

        String queryAwayGoal = "select "
            + "(SUM(case when goalhome < goalaway THEN 1 ELSE 0 END)) as win, "
            + "(SUM(case when goalhome = goalaway THEN 1 ELSE 0 END)) as draw, "
            + "(SUM(case when goalhome > goalaway THEN 1 ELSE 0 END)) as lose, "
            + "SUM(goalhome) as gs, "
            + "SUM(goalaway) as gf "
            + " from " + Match.builder.TABLE()
            + " where idround in(select idround "
            + "from round "
            + "where idseason=? and league=? and played=1) and idteamaway=? ";

        String queryAll = "select idteamhome,idteamaway "
            + "from match "
            + "where idround in (select idround from round where idseason=? and league=? and played=1)";

        try (Connection con = openConnection();
            PreparedStatement psHome = con.prepareStatement(queryHomeGoal);
            PreparedStatement psAway = con.prepareStatement(queryAwayGoal);
            PreparedStatement allTeams = con.prepareStatement(queryAll);) {

            Set<Integer> teams = new HashSet<>();
            allTeams.setInt(1, idseason);
            allTeams.setInt(2, idleague);

            psAway.setInt(1, idseason);
            psAway.setInt(2, idleague);

            psHome.setInt(1, idseason);
            psHome.setInt(2, idleague);

            try (ResultSet rs = allTeams.executeQuery();) {
                while (rs.next()) {
                    teams.add(rs.getInt(1));
                    teams.add(rs.getInt(2));
                }
            }
            for (Integer team : teams) {
                int win = 0;
                int draw = 0;
                int lose = 0;
                int gf = 0;
                int gs = 0;
                psHome.setInt(3, team);
                try (ResultSet rs = psHome.executeQuery();) {
                    while (rs.next()) {
                        win += rs.getInt("win");
                        draw += rs.getInt("draw");
                        lose += rs.getInt("lose");
                        gf += rs.getInt("gf");
                        gs += rs.getInt("gs");
                    }
                }
                psAway.setInt(3, team);
                try (ResultSet rs = psAway.executeQuery();) {
                    while (rs.next()) {
                        win += rs.getInt("win");
                        draw += rs.getInt("draw");
                        lose += rs.getInt("lose");
                        gf += rs.getInt("gf");
                        gs += rs.getInt("gs");
                    }
                }

                table.getRows().add(new LeagueTable.LeagueTableRow(findTeamById(team).getName(), win, draw, lose, gf, gs));
            }

        } catch (Exception e) {
            throw new SQLException(e);
        }
        Collections.sort(table.getRows(), LeagueTable.compare);

        return table;

    }

    public List<Team> getTeamsForLeague(int n, int league) throws SQLException {
        List<Team> result = new ArrayList<>();
        try (Connection con = openConnection();
            PreparedStatement ps = con.prepareStatement("select " + Team.builder.SELECT_HEADER() + " from " + Team.builder.TABLE()
                + " where idleague = ? and ofuser = 0 limit " + n);) {
            ps.setInt(1, league);
            try (ResultSet rs = ps.executeQuery();) {

                while (rs.next()) {
                    result.add(new Team().fromResultSet(rs));
                }
            }
            return result;
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    public <T extends Entity> List<T> queryEntity(Class<T> clazz, QueryBuilder.WhereClause where, Object... params) throws SQLException {
        try {
            List<T> result = new ArrayList<>();
            String query = QueryBuilder.buildSelect((QueryBuilder<T>) clazz.getMethod("getBuilder").invoke(clazz.newInstance()), where);
            try (Connection con = openConnection();
                PreparedStatement ps = con.prepareStatement(query);) {
                int i = 1;
                for (Object param : params) {
                    ps.setObject(i++, param);
                }
                try (ResultSet rs = ps.executeQuery();) {
                    while (rs.next()) {
                        result.add(clazz.newInstance().fromResultSet(rs));
                    }
                }
                return result;
            } catch (Exception e) {
                throw new SQLException(e);
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException | SQLException | InstantiationException e) {
            throw new SQLException(e);
        }
    }

    public int insertEntity(Entity metadata) throws SQLException {
        String query = QueryBuilder.buildInsert(metadata.getBuilder());
        try (Connection con = openConnection();
            PreparedStatement ps = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);) {
            QueryBuilder.fillStatement(ps, metadata.getBuilder(), metadata);
            ps.executeUpdate();
            if (ps.getGeneratedKeys().next()) {
                return ps.getGeneratedKeys().getInt(1);
            }
            throw new SQLException("not inserted");
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }

    private void updateEntity(Connection con, Entity metadata, QueryBuilder.WhereClause where, Object... params) throws SQLException {
        String query = QueryBuilder.buildUpdate(metadata.getBuilder(), where);
        boolean passed = con != null;
        if (con == null) {
            con = openConnection();
        }
        try (PreparedStatement ps = con.prepareStatement(query);) {
            int i = QueryBuilder.fillStatement(ps, metadata.getBuilder(), metadata);
            for (Object param : params) {
                ps.setObject(i++, param);
            }
            ps.executeUpdate();
        } catch (Exception e) {
            throw new SQLException(e);
        } finally {
            if (!passed) {
                con.close();
            }
        }

    }

    public void updateEntity(Entity metadata, QueryBuilder.WhereClause where, Object... params) throws SQLException {
        updateEntity(null, metadata, where, params);
    }

    private Connection openConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC").newInstance();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new SQLException(e);
        }
        return DriverManager.getConnection("jdbc:sqlite:" + databasePath + databaseName);
    }

    public void initDatabase() throws SQLException {
        File file = new File(databasePath + databaseName);

        if (file.exists()) {
            //System.out.print("Database already exists!");
            //return;
            file.delete();
        }

        try (Connection c = openConnection();) {

            if (c.getAutoCommit()) {
                c.setAutoCommit(false);
            }
            try {
                initTables(c);
            } catch (SQLException e) {
                c.rollback();
                throw new SQLException(e);
            }
            c.commit();
            c.setAutoCommit(true);
        }
        System.out.println("OK!");

    }

    private void initTables(Connection c) throws SQLException {
        initTable(GameMetadata.builder.CREATE(), c);
        initTable(Match.builder.CREATE(), c);
        initTable(Round.builder.CREATE(), c);
        initTable(Team.builder.CREATE(), c);
        initTable(Season.builder.CREATE(), c);
    }

    private void initTable(String def, Connection c) throws SQLException {
        try (PreparedStatement preparedStatement = c.prepareStatement(def);) {
            try {
                preparedStatement.execute();
            } catch (SQLException e) {
                throw new SQLException("initTables " + def + " failed exception:" + e);
            }
        }

    }

}
