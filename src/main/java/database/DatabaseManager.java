/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package database;

import data.BillboardTable;
import data.EmbeddedData;
import data.LeagueTable;
import database.bean.Entity;
import database.bean.GameMetadata;
import database.bean.Match;
import database.bean.QueryBuilder;
import database.bean.Round;
import database.bean.Season;
import database.bean.Team;
import engine.SeasonCalculator;
import engine.SeasonCalculator.SingleMatch;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
    private String databaseName;

    public DatabaseManager(String databasePath) {
        this.databasePath = databasePath;
    }

    public String getDatabasePath() {
        return databasePath;
    }

    public int getRelativeRoundIndex(int idround) throws SQLException {
        List<Round> queryEntity = queryEntity(Round.class, QueryBuilder.WhereClause.instance().field("idround", "="), idround);
        if (queryEntity.isEmpty()) {
            return -1;
        }
        int idSeason = queryEntity.get(0).getIdSeason();
        int league = queryEntity.get(0).getLeague();

        try (Connection con = openConnection();) {
            try (PreparedStatement psMatch = con
                .prepareStatement("Select count(*) from " + Round.builder.TABLE() + " where idseason = ?"
                    + " and idround < ? and league = ?")) {
                psMatch.setInt(1, idSeason);
                psMatch.setInt(2, idround);
                psMatch.setInt(3, league);
                try (ResultSet rs = psMatch.executeQuery();) {
                    return rs.getInt(1);
                }

            }
        }
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

    public void adjustRoundMatches(List<SeasonCalculator.SingleMatch> matches, int idround, int subleague) throws SQLException {
        try (Connection con = openConnection();) {
            try (PreparedStatement psMatch
                = con.prepareStatement("Update " + Match.builder.TABLE() + " set idteamhome=?,idteamaway=? "
                    + " where idmatch=?");
                PreparedStatement psToRemove
                = con.prepareStatement("Select idmatch from " + Match.builder.TABLE() + " "
                    + " where idteamhome=? and idround=? and subIdLeague=?");) {

                psToRemove.setInt(1, SeasonCalculator.NO_REAL_TEAM);
                psToRemove.setInt(2, idround);
                psToRemove.setInt(3, subleague);
                List<Integer> allMatches = new ArrayList<>();
                try (ResultSet rs = psToRemove.executeQuery();) {
                    while (rs.next()) {
                        allMatches.add(rs.getInt(1));
                    }
                }
                if (allMatches.size() < matches.size()) {
                    throw new IllegalStateException("Ci sono " + allMatches.size() + " posti ma ne vuoi mettere " + matches.size());
                }

                int c = 0;
                for (SingleMatch matche : matches) {
                    psMatch.setInt(1, matche.getHomeTeam());
                    psMatch.setInt(2, matche.getAwayTeam());
                    psMatch.setInt(3, allMatches.get(c));
                    psMatch.executeUpdate();
                    c++;
                }
            }
        }
    }

    public int getNextRound(int idround) throws SQLException {
        String query = "Select idround from " + Round.builder.TABLE() + " "
            + "where played=0 and idround <> ? and league = (Select league "
            + "from " + Round.builder.TABLE() + " where idround=?) order by idround asc LIMIT 1";
        try (Connection con = openConnection();
            PreparedStatement allTeams = con.prepareStatement(query);) {
            allTeams.setInt(1, idround);
            allTeams.setInt(2, idround);
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

    public BillboardTable calculateBillboard(int idseason, int idleague, int subleague) throws SQLException {
        if (idleague == EmbeddedData.League.CAMPIONATO) {
            throw new IllegalArgumentException();
        }
        BillboardTable table = new BillboardTable();
//
        if (idleague == EmbeddedData.League.COPPA) {
            table.setTotalRounds(3);
        } else {
            table.setTotalRounds(4);
        }
        List<Match> matches = new ArrayList<>();
        List<Round> rounds = new ArrayList<>();

        try (Connection con = openConnection();
            PreparedStatement allMatch = con.prepareStatement("Select " + Match.builder.SELECT_HEADER()
                + " from " + Match.builder.TABLE() + " where idround = ? and subIdLeague=? ");
            PreparedStatement allRound = con.prepareStatement("Select " + Round.builder.SELECT_HEADER()
                + " from " + Round.builder.TABLE() + " where  idseason=? and league=? and played=1 "
                + "");) {

            allRound.setInt(1, idseason);
            allRound.setInt(2, idleague);
            try (ResultSet rs = allRound.executeQuery();) {
                while (rs.next()) {
                    rounds.add(new Round().fromResultSet(rs));
                }
            }
            table.setPlayedRounds(rounds.size());
            if (rounds.isEmpty()) {
                try (PreparedStatement firstRound = con.prepareStatement("Select " + Round.builder.SELECT_HEADER()
                    + " from " + Round.builder.TABLE() + " where  idseason=? and league=? and played=0 order by idround LIMIT 1  "
                    + "");) {
                    firstRound.setInt(1, idseason);
                    firstRound.setInt(2, idleague);
                    try (ResultSet rs = firstRound.executeQuery();) {
                        while (rs.next()) {
                            rounds.add(new Round().fromResultSet(rs));
                            break;
                        }
                    }
                }
            }

            for (Round r : rounds) {
                allMatch.setInt(1, r.getIdRound());
                allMatch.setInt(2, subleague);

                try (ResultSet rs = allMatch.executeQuery();) {
                    while (rs.next()) {
                        matches.add(new Match().fromResultSet(rs));
                    }
                }
            }

        } catch (Exception e) {
            throw new SQLException(e);
        }

        for (Round r : rounds) {
            List<BillboardTable.BillBoardMatch> billMatches = new ArrayList<>();
            for (Match matche : matches) {
                if (matche.getIdRound() == r.getIdRound()) {
                    billMatches.add(new BillboardTable.BillBoardMatch(findTeamById(matche.getIdTeamHome()).getName(),
                        findTeamById(matche.getIdTeamAway()).getName(), matche.getGoalHome(), matche.getGoalAway()));
                }
            }
            table.getMatches().put(getRelativeRoundIndex(r.getIdRound()), billMatches);

        }
        return table;

    }

    public LeagueTable calculateLeagueTable(int idseason, int idleague) throws SQLException {
        if (idleague != EmbeddedData.League.CAMPIONATO) {
            throw new IllegalArgumentException();
        }
        LeagueTable table = new LeagueTable();
        table.setIdseason(idseason);
        table.setLeague(idleague);
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

        String queryAll = "select idteam "
            + "from team "
            + "where league=?";

        try (Connection con = openConnection();
            PreparedStatement psHome = con.prepareStatement(queryHomeGoal);
            PreparedStatement psAway = con.prepareStatement(queryAwayGoal);
            PreparedStatement allTeams = con.prepareStatement(queryAll);) {

            Set<Integer> teams = new HashSet<>();
            allTeams.setInt(1, idleague);

            psAway.setInt(1, idseason);
            psAway.setInt(2, idleague);

            psHome.setInt(1, idseason);
            psHome.setInt(2, idleague);

            try (ResultSet rs = allTeams.executeQuery();) {
                while (rs.next()) {
                    teams.add(rs.getInt(1));
                }
            }

            boolean isFirstRound = true;
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
                if (isFirstRound && (win > 0 || draw > 0 || lose > 0)) {
                    isFirstRound = false;
                }
                table.getRows().add(new LeagueTable.LeagueTableRow(findTeamById(team).getName(), team, win, draw, lose, gf, gs));
            }

        } catch (Exception e) {
            throw new SQLException(e);
        }
        Collections.sort(table.getRows(), LeagueTable.compare);

        return table;

    }

    public List<Team> getTeamsForLeague(int n, int league, int subleague) throws SQLException {
        List<Team> result = new ArrayList<>();
        try (Connection con = openConnection();
            PreparedStatement ps = con.prepareStatement("select " + Team.builder.SELECT_HEADER() + " from " + Team.builder.TABLE()
                + " where league = ? and subleague = ? and ofuser = 0 limit " + n);) {
            ps.setInt(1, league);
            ps.setInt(2, subleague);
            try (ResultSet rs = ps.executeQuery();) {
                while (rs.next()) {
                    result.add(new Team().fromResultSet(rs));
                }
            }

            if (n != result.size()) {
                throw new IllegalStateException("ne volevo " + n + " me ne darebbe " + result.size());
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

    public boolean initDatabase(String dbname) throws SQLException {
        databaseName = dbname;
        File file = new File(databasePath + databaseName);
        System.out.println("initDatabase at" + file.getAbsolutePath());

        if (file.exists()) {
            System.out.print("Database already exists!");
            return true;
//            file.delete();
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
        return false;

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
