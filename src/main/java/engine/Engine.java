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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 *
 * @author nicolo.boschi
 */
public class Engine {

    private final DatabaseManager manager;
    private List<Team> teams;
    private List<Integer> nextMatchHome = new ArrayList<>();
    private List<Integer> nextMatchAway = new ArrayList<>();
    private final List<SingleMatch> matches = new LinkedList<>();

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
        // faccio tutte le competizioni una alla volta
        // calcolo tutti i round in memoria per ogni competizione
        // ho nella table team le squadre
        Season season = new Season();
        season.setSequence(1);
        int idSeason = manager.insertEntity(season);

        this.teams = manager.queryEntity(Team.class, null);
        System.out.println("tms" + this.teams);

        List<Integer> tsids = new ArrayList<>(teams.stream().map(Team::getIdteam).collect(Collectors.toList()));
        Collections.shuffle(tsids);
        int rounds = tsids.size() - 1;
        for (int i = 0; i < rounds; i++) {
            calculateRound(tsids, i == rounds - 1, i);
        }

        System.out.println("match" + matches);

        Map<Integer, List<SingleMatch>> calculatedRound = matches.stream().collect(Collectors.groupingBy(SingleMatch::getRound));

        System.out.println(calculatedRound);
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

    private void calculateRound(List<Integer> tsids, boolean last, int actualRound) {
        if (matches.isEmpty()) {
            nextMatchAway = new ArrayList<>(tsids.subList(0, CAMPIONATO_N_TEAMS / 2));
            nextMatchHome = new ArrayList<>(tsids.subList(CAMPIONATO_N_TEAMS / 2, CAMPIONATO_N_TEAMS));
        }
        List<Integer> _nextHome = new ArrayList<>();
        List<Integer> _nextAway = new ArrayList<>();
        List<Integer> matched = new ArrayList<>();
        List<Integer> all = new ArrayList<>(nextMatchAway);
        if (last) {
            all.addAll(nextMatchHome);
        }
        for (Integer home : last ? all : nextMatchHome) {
            if (matched.contains(home)) {
                continue;
            }
            boolean found = false;

            for (Integer away : last ? all : nextMatchAway) {
                if (matched.contains(away) || away == home) {
                    continue;
                }
                if (!alreadyPlayed(home, away)) {
                    found = true;
                    addMatch(home, away, actualRound);
                    matched.add(home);
                    matched.add(away);
                    _nextAway.add(home);
                    _nextHome.add(away);
                    break;
                }
            }
            if (!found && !last) {
                break;
            }
        }

        nextMatchAway = _nextAway;
        nextMatchHome = _nextHome;

    }

    private void addMatch(Integer a, Integer b, int actualRound) {
        matches.add(new SingleMatch(a, b, actualRound));
    }

    private boolean alreadyPlayed(Integer a, Integer b) {
        return matches.stream().anyMatch(m -> new SingleMatch(a, b, -1).equals(m));
    }

    private class SingleMatch {

        @Override
        public String toString() {
            return "SingleMatch{" + "a=" + homeTeam + ", b=" + awayTeam + '}';
        }

        int homeTeam;
        int awayTeam;
        int round;

        public int getRound() {
            return round;
        }

        public SingleMatch(int a, int b, int round) {
            this.homeTeam = a;
            this.awayTeam = b;
            this.round = round;
        }

        @Override
        public int hashCode() {
            int hash = 5;
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            final SingleMatch other = (SingleMatch) obj;
            return (this.homeTeam == other.homeTeam && this.awayTeam == other.awayTeam) || this.homeTeam == other.awayTeam && this.awayTeam == other.homeTeam;
        }

    }

}
