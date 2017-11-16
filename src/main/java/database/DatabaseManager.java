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
        String queryHomeGoal = "select (SUM(case when goalhome > goalaway THEN 3 when goalhome=goalaway then 1 else 0 End )) "
            + "as point from " + Match.builder.TABLE() + " where idround in(select idround from round where idseason=? and league=? and played=1) and idteamhome=? ";

        String queryAwayGoal = "select (SUM(case when goalhome > goalaway THEN 0 when goalhome=goalaway then 1 else 3 End )) "
            + "as point from " + Match.builder.TABLE() + " where idround in( select idround from round where idseason=? and league=? and played=1) and idteamaway=?";

        String queryAll = "select idteamhome from match where idround in (select idround from round where idseason=? and league=? and played=1)";
        String queryAllAway = "select idteamaway from match where idround in (select idround from round where idseason=? and league=? and played=1)";

        try (Connection con = openConnection();
            PreparedStatement psHome = con.prepareStatement(queryHomeGoal);
            PreparedStatement psAway = con.prepareStatement(queryAwayGoal);
            PreparedStatement allTeams = con.prepareStatement(queryAll);
            PreparedStatement allTeamsAway = con.prepareStatement(queryAllAway);) {

            Set<Integer> teams = new HashSet<>();
            allTeams.setInt(1, idseason);
            allTeams.setInt(2, idleague);
            allTeamsAway.setInt(1, idseason);
            allTeamsAway.setInt(2, idleague);

            psAway.setInt(1, idseason);
            psAway.setInt(2, idleague);

            psHome.setInt(1, idseason);
            psHome.setInt(2, idleague);

            try (ResultSet rs = allTeams.executeQuery();) {
                while (rs.next()) {
                    teams.add(rs.getInt(1));
                }
            }
            try (ResultSet rs = allTeamsAway.executeQuery();) {
                while (rs.next()) {
                    teams.add(rs.getInt(1));
                }
            }

            for (Integer team : teams) {
                int point = 0;
                psHome.setInt(3, team);
                try (ResultSet rs = psHome.executeQuery();) {
                    while (rs.next()) {

                        point += rs.getInt(1);
                    }
                }
                psAway.setInt(3, team);
                try (ResultSet rs = psAway.executeQuery();) {
                    while (rs.next()) {

                        point += rs.getInt(1);
                    }
                }
                table.getRows().add(new LeagueTable.LeagueTableRow(team, point));
            }

        } catch (Exception e) {
            throw new SQLException(e);
        }
        for (LeagueTable.LeagueTableRow row : table.getRows()) {
            row.setTeamName(this.findTeamById(row.getIdteam()).getName());
        }
        Collections.sort(table.getRows(), (o1, o2) -> {
            if (o1.getPoint() == o2.getPoint()) {
                return 0;
            }
            return o1.getPoint() < o2.getPoint() ? 1 : -1;
        });

        return table;

    }

    public <T extends Entity> List<T> queryEntity(Class<T> clazz, QueryBuilder.WhereClause where, Object... params) throws SQLException {
        try {
            List<T> result = new ArrayList<>();
            System.out.println("clazz" + clazz);
            System.out.println("clazz" + Arrays.toString(clazz.getMethods()));
            String query = QueryBuilder
                .buildSelect((QueryBuilder<T>) clazz.getMethod("getBuilder").invoke(clazz.newInstance()), where);
            System.out.println("query is:" + query);
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
        System.out.println("query is:" + query);
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
