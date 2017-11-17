/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import client.ApiClient;
import client.BadRequestException;
import data.EmbeddedData;
import data.LeagueTable;
import database.DatabaseManager;
import database.bean.Match;
import database.bean.QueryBuilder.WhereClause;
import database.bean.Round;
import database.bean.Season;
import database.bean.Team;
import engine.SeasonCalculator.SingleMatch;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author nicolo.boschi
 */
public class Engine {

    private final DatabaseManager manager;


    public static final int CAMPIONATO_N_TEAMS = 8;

    public Engine(DatabaseManager manager) throws SQLException {
        this.manager = manager;
        manager.initDatabase();
    }

    public static class MatchResult {

        int goalhome;
        int goalaway;

        @Override
        public String toString() {
            return "MatchResult{" + "goalhome=" + goalhome + ", goalaway=" + goalaway + '}';
        }

    }

    public MatchResult simulateMatch(Team home, Team away) {
        MatchResult r = new MatchResult();

        int goalHome = simulateDice(home.getAttack()) - simulateDice(away.getDefence());
        int goalAway = simulateDice(away.getAttack()) - simulateDice(home.getDefence());;

        r.goalhome = goalHome > 0 ? goalHome : 0;
        r.goalaway = goalAway > 0 ? goalAway : 0;
        return r;

    }

    public int simulateDice(int n) {
        int count = 0;
        for (int i = 0; i < n; i++) {
            if (simulateDice()) {
                count++;
            }
        }
        return count;
    }

    private boolean simulateDice() {
        return ThreadLocalRandom.current().nextInt(0, 3) == 1;
    }

    public void playRound(int idround, List<Match> passed) throws SQLException, BadRequestException {
        if (passed == null || passed.isEmpty()) {
            throw new BadRequestException("no match passed");

        }
        Round theRound = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround).get(0);
        if (theRound == null) {
            throw new BadRequestException("round not exists");
        }
        if (theRound.isPlayed()) {
            throw new BadRequestException("round already played");
        }
        List<Match> allMatchToSimulate = manager.getAllMatchToSimulate(idround);
        for (Match match : allMatchToSimulate) {
            MatchResult rs = simulateMatch(manager.findTeamById(match.getIdTeamHome()), manager.findTeamById(match.getIdTeamAway()));
            match.setGoalHome(rs.goalhome);
            match.setGoalAway(rs.goalaway);
            passed.add(match);
        }

        manager.updateRound(passed, idround);
    }

    public int getActualRound() throws SQLException {
        return manager.getActualRound();
    }

    public List<ApiClient.RoundMatch> getRoundMatches(int idround) throws SQLException {
        List<ApiClient.RoundMatch> finalMatches = new ArrayList<>();
        List<Match> getMatches = manager.queryEntity(Match.class, WhereClause.instance().field("idround", "="), idround);
        for (Match matche : getMatches) {
            ApiClient.RoundMatch roundMatch = new ApiClient.RoundMatch();
            roundMatch.setIdmatch(matche.getIdmatch());
            Team teamHome = manager.queryEntity(Team.class, WhereClause.instance().field("idteam", "="), matche.getIdTeamHome()).get(0);
            roundMatch.setHome(new ApiClient.RoundMatch.TeamResult(teamHome.getIdteam(), teamHome.getName(), matche.getGoalHome(), teamHome.isOfuser()));
            Team teamAway = manager.queryEntity(Team.class, WhereClause.instance().field("idteam", "="), matche.getIdTeamAway()).get(0);
            roundMatch.setAway(new ApiClient.RoundMatch.TeamResult(teamAway.getIdteam(), teamAway.getName(), matche.getGoalAway(), teamAway.isOfuser()));
            roundMatch.setEditable(teamHome.isOfuser() || teamAway.isOfuser());

            finalMatches.add(roundMatch);
        }
        return finalMatches;
    }

    public boolean isRoundPlayed(int idround) throws SQLException {
        Round r = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround).get(0);
        return r.isPlayed();

    }

    public void insertTeams(List<String> choosen) throws SQLException {

        List<String> allteamsChosen = new ArrayList<>(choosen);
        for (String string : choosen) {
            Team t = Team.of(EmbeddedData.ALL_TEAMS.get(string));
            t.setOfuser(true);
            int id = manager.insertEntity(t);
        }

        List<EmbeddedData.Team> allteams = new ArrayList<>(EmbeddedData.ALL_TEAMS.values());
        Collections.shuffle(allteams);
        for (int i = 0; i < (CAMPIONATO_N_TEAMS - choosen.size()); i++) {
            for (EmbeddedData.Team allteam : allteams) {
                if (allteamsChosen.contains(allteam.getName())) {
                    continue;
                }
                Team t = Team.of(EmbeddedData.ALL_TEAMS.get(allteam.getName()));
                t.setOfuser(false);
                manager.insertEntity(t);
                allteamsChosen.add(allteam.getName());
                break;
            }
        }
        System.out.println("alla fine squadre scelte " + allteamsChosen);

    }

    public LeagueTable calculateLeagueTable(int idround, int idleague) throws SQLException {
        Round r = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround).get(0);
        return manager.calculateLeagueTable(r.getIdSeason(), idleague);
    }

    public int nextRound(List<Match> matches) throws SQLException {
        int thisRound = 0;
        for (Match match : matches) {
            if (thisRound == 0) {
                thisRound = match.getIdRound();
            }
            manager.updateEntity(match, WhereClause.instance().field("idmatch", "="), match.getIdmatch());
        }
        if (thisRound == 0) {
            throw new IllegalStateException("non possiamo essere al turno 0");
        }
        Round r = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), thisRound).get(0);
        r.setPlayed(true);
        manager.updateEntity(r, WhereClause.instance().field("idround", "="), r.getIdRound());

        List<Round> remainingRound = manager.queryEntity(Round.class, WhereClause.instance().field("idseason", "=").and().field("played", "="), r.getIdSeason(), 0);
        if (remainingRound.isEmpty()) {
            return -1;
        }
        return ++thisRound;

    }

    public int newSeason() throws SQLException {
        Season season = new Season();
        season.setSequence(1);
        int idSeason = manager.insertEntity(season);

        SeasonCalculator seasonCalculator = new SeasonCalculator(manager.queryEntity(Team.class, null));

        
        Map<Integer, List<SeasonCalculator.SingleMatch>> calculatedRound = seasonCalculator.run();

        int nextRound = 0;
        for (Map.Entry<Integer, List<SingleMatch>> entry : calculatedRound.entrySet()) {
            Round r = new Round();
            r.setIdSeason(idSeason);
            r.setLeague(EmbeddedData.League.CAMPIONATO);
            r.setPlayed(false);
            int idRound = manager.insertEntity(r);
            if (nextRound == 0) {
                nextRound = idRound;
            }
            for (SingleMatch singleMatch : entry.getValue()) {
                Match match = new Match();
                match.setIdRound(idRound);
                match.setIdTeamHome(singleMatch.homeTeam);
                match.setIdTeamAway(singleMatch.awayTeam);
                manager.insertEntity(match);
            }
        }

        return nextRound;
    }

  

}
