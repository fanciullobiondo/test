/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import data.EmbeddedData;
import database.bean.Team;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import static data.EmbeddedData.League.EUROPA;

/**
 *
 * @author nicolo.boschi
 */
public class SeasonCalculator {

    private final Map<Integer, List<Team>> teams;
    private List<Integer> nextMatchHome = new ArrayList<>();
    private List<Integer> nextMatchAway = new ArrayList<>();
    private final List<SingleMatch> matches = new LinkedList<>();
    private int actualLeague;
    private int actualSubleague;
    public static final int NO_REAL_TEAM = -5;

    public SeasonCalculator(Map<Integer, List<Team>> teams) {
        this.teams = teams;
    }

    public Map<Integer, List<SingleMatch>> getResult() {
        return matches.stream().collect(Collectors.groupingBy(SingleMatch::getRound));
    }

    public Map<Integer, List<SingleMatch>> run() {
        int roundCount = 0;
        for (Map.Entry<Integer, List<Team>> entry : teams.entrySet()) {

            actualLeague = entry.getKey();
            List<Team> value = entry.getValue();

            if (actualLeague == EmbeddedData.League.CAMPIONATO) {
                List<Integer> tsids = new ArrayList<>(value.stream().map(Team::getIdteam).collect(Collectors.toList()));
                Collections.shuffle(tsids);
                actualSubleague = EmbeddedData.League.SUB_NONE;
                int rounds = tsids.size() - 1;
                for (int i = 0; i < rounds; i++) {
                    calculateRound(tsids, roundCount, false);
                    roundCount++;

                    int expectedMatchesSize = (i + 1) * (tsids.size() / 2);
                    if (expectedMatchesSize != matches.size()) {
                        throw new IllegalStateException("mi aspettavo al turno " + (i + 1) + " di avere "
                            + expectedMatchesSize + " ma ne ho " + matches.size());
                    }
                }
                nextMatchHome.clear();
                nextMatchAway.clear();
            } else if (actualLeague == EmbeddedData.League.COPPA) {
                List<Integer> tsids = new ArrayList<>(value.stream().map(Team::getIdteam).collect(Collectors.toList()));
                Collections.shuffle(tsids);
                actualSubleague = EmbeddedData.League.SUB_NONE;
                // quarti
                calculateRound(tsids, roundCount, true);
                roundCount++;
                // semi
                for (int j = 0; j < 2; j++) {
                    matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, actualLeague, actualSubleague));
                }
                roundCount++;
                // finale
                matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, actualLeague, actualSubleague));
                roundCount++;
                nextMatchHome.clear();
                nextMatchAway.clear();

            } else {

                List<Integer> el = new ArrayList<>(value.stream()
                    .filter(t -> t.getSubleague() == EmbeddedData.League.SUB_EUROPALEAGUE)
                    .map(Team::getIdteam).collect(Collectors.toList()));

                Collections.shuffle(el);
                List<Integer> cl = new ArrayList<>(value.stream()
                    .filter(t -> t.getSubleague() == EmbeddedData.League.SUB_CHAMPIONSLEAGUE)
                    .map(Team::getIdteam).collect(Collectors.toList()));
                Collections.shuffle(cl);

                // ottavi
                actualSubleague = EmbeddedData.League.SUB_EUROPALEAGUE;
                calculateRound(el, roundCount, true);
                actualSubleague = EmbeddedData.League.SUB_CHAMPIONSLEAGUE;
                calculateRound(cl, roundCount, true);
                roundCount++;
                // mock rounds
                // quarti
                for (int j = 0; j < 4; j++) {
                    matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, EUROPA, EmbeddedData.League.SUB_EUROPALEAGUE));
                }
                for (int j = 0; j < 4; j++) {
                    matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, EUROPA, EmbeddedData.League.SUB_CHAMPIONSLEAGUE));
                }
                roundCount++;
                // semi
                for (int j = 0; j < 2; j++) {
                    matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, EUROPA, EmbeddedData.League.SUB_CHAMPIONSLEAGUE));
                }
                for (int j = 0; j < 2; j++) {
                    matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, EUROPA, EmbeddedData.League.SUB_EUROPALEAGUE));
                }
                roundCount++;
                // finale
                matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, EUROPA, EmbeddedData.League.SUB_CHAMPIONSLEAGUE));
                matches.add(new SingleMatch(NO_REAL_TEAM, NO_REAL_TEAM, roundCount, EUROPA, EmbeddedData.League.SUB_EUROPALEAGUE));
                roundCount++;

                nextMatchHome.clear();
                nextMatchAway.clear();
            }
        }
        return getResult();

    }

    public void calculateRound(List<Integer> tsids, int actualRound, boolean singleRound) {
        if (matches.isEmpty() || singleRound) {
            nextMatchAway = new ArrayList<>(tsids.subList(0, tsids.size() / 2));
            nextMatchHome = new ArrayList<>(tsids.subList(tsids.size() / 2, tsids.size()));
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
        matches.add(new SingleMatch(a, b, actualRound, actualLeague, actualSubleague));
    }

    private boolean alreadyPlayed(Integer a, Integer b) {
        return matches.stream().filter(m -> m.league == actualLeague).anyMatch(m -> new SingleMatch(a, b).equals(m));
    }

    public static class SingleMatch {

        @Override
        public String toString() {
            return "SingleMatch{" + "homeTeam=" + homeTeam + ", awayTeam=" + awayTeam + ", round=" + round + ", league=" + league + ", subleague=" + subleague + '}';
        }

        int homeTeam;
        int awayTeam;
        int round;
        int league;
        int subleague;

        public int getRound() {
            return round;
        }

        public SingleMatch(int homeTeam, int awayTeam) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
        }

        public SingleMatch(int homeTeam, int awayTeam, int round, int league, int subleague) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.round = round;
            this.league = league;
            this.subleague = subleague;
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

        public int getHomeTeam() {
            return homeTeam;
        }

        public int getAwayTeam() {
            return awayTeam;
        }

        public int getLeague() {
            return league;
        }

        public int getSubleague() {
            return subleague;
        }

    }

}
