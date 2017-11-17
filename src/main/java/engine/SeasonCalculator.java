/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import database.bean.Team;
import static engine.Engine.CAMPIONATO_N_TEAMS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author nicolo.boschi
 */
public class SeasonCalculator {

    private final List<Team> teams;
    private List<Integer> nextMatchHome = new ArrayList<>();
    private List<Integer> nextMatchAway = new ArrayList<>();
    private final List<SingleMatch> matches = new LinkedList<>();

    public SeasonCalculator(List<Team> teams) {
        System.out.println("passed" + teams.size());
        this.teams = teams;
    }

    public Map<Integer, List<SingleMatch>> run() {
        List<Integer> tsids = new ArrayList<>(teams.stream().map(Team::getIdteam).collect(Collectors.toList()));
        Collections.shuffle(tsids);
        int rounds = tsids.size() - 1;
        for (int i = 0; i < rounds; i++) {
            calculateRound(tsids, i);

            int expectedMatchesSize = (i + 1) * (CAMPIONATO_N_TEAMS / 2);
            if (expectedMatchesSize != matches.size()) {
                throw new IllegalStateException("mi aspettavo al turno " + (i + 1) + " di avere "
                    + expectedMatchesSize + " ma ne ho " + matches.size());
            }
        }
        return matches.stream().collect(Collectors.groupingBy(SingleMatch::getRound));
    }

    private void calculateRound(List<Integer> tsids, int actualRound) {
        if (matches.isEmpty()) {
            nextMatchAway = new ArrayList<>(tsids.subList(0, CAMPIONATO_N_TEAMS / 2));
            nextMatchHome = new ArrayList<>(tsids.subList(CAMPIONATO_N_TEAMS / 2, CAMPIONATO_N_TEAMS));
        }
        List<Integer> _nextHome = new ArrayList<>();
        List<Integer> _nextAway = new ArrayList<>();
        List<Integer> matched = new ArrayList<>();
        List<Integer> all = new ArrayList<>(nextMatchHome);
        all.addAll(nextMatchAway);
        for (Integer team : all) {
            if (matched.contains(team)) {
                continue;
            }
            boolean found = findMatch(team, nextMatchAway, actualRound, matched, _nextHome, _nextAway);
            if (!found) {
                findMatch(team, all, actualRound, matched, _nextHome, _nextAway);
            }
        }
        nextMatchAway = _nextAway;
        nextMatchHome = _nextHome;

    }

    private boolean findMatch(int who, List<Integer> where, int actualRound, List<Integer> matched, List<Integer> _nextHome, List<Integer> _nextAway) {
        for (Integer away : where) {
            if (matched.contains(away) || away == who) {
                continue;
            }
            if (!alreadyPlayed(who, away)) {
                addMatch(who, away, actualRound);
                matched.add(who);
                matched.add(away);
                _nextAway.add(who);
                _nextHome.add(away);
                return true;
            }
        }
        return false;
    }

    private void addMatch(Integer a, Integer b, int actualRound) {
        matches.add(new SingleMatch(a, b, actualRound));
    }

    private boolean alreadyPlayed(Integer a, Integer b) {
        return matches.stream().anyMatch(m -> new SingleMatch(a, b, -1).equals(m));
    }

    public static class SingleMatch {

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
