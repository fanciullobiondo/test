/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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

    public Engine(DatabaseManager manager) {
        this.manager = manager;
    }

    public LeagueTable calculateLeagueTable(int idseason, int idleague) throws SQLException {
        return manager.calculateLeagueTable(idseason, idleague);
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

        List<Integer> tsids = new ArrayList<>(Arrays.asList(1, 5, 10, 15));
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
            nextMatchAway = new ArrayList<>(Arrays.asList(tsids.get(2), tsids.get(3)));
            nextMatchHome = new ArrayList<>(Arrays.asList(tsids.get(0), tsids.get(1)));
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
                return;
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
