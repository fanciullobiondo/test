/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author nicolo.boschi
 */
public class BillboardTable extends Table {

    @Override
    public String toString() {
        return "BillboardTable{" + "playedRounds=" + playedRounds + ", totalRounds=" + totalRounds + ", matches=" + matches + '}';
    }


    private int playedRounds; // 3 0 4
    private int totalRounds; // 3 0 4
    private Map<Integer, List<BillBoardMatch>> matches = new HashMap<>();

    public int getPlayedRounds() {
        return playedRounds;
    }

    public int getTotalRounds() {
        return totalRounds;
    }

    public void setTotalRounds(int totalRounds) {
        this.totalRounds = totalRounds;
    }
    

    public Map<Integer, List<BillBoardMatch>> getMatches() {
        return matches;
    }

    public void setPlayedRounds(int playedRounds) {
        this.playedRounds = playedRounds;
    }

    public void setMatches(Map<Integer, List<BillBoardMatch>> matches) {
        this.matches = matches;
    }
    

    public static class BillBoardMatch {

        @Override
        public String toString() {
            return "BillBoardMatch{" + "homeTeam=" + homeTeam + ", awayTeam=" + awayTeam + ", goalHome=" + goalHome + ", goalAway=" + goalAway + '}';
        }


        
        private String homeTeam;
        private String awayTeam;
        private int goalHome;
        private int goalAway;

        public BillBoardMatch(String homeTeam, String awayTeam, int goalHome, int goalAway) {
            this.homeTeam = homeTeam;
            this.awayTeam = awayTeam;
            this.goalHome = goalHome;
            this.goalAway = goalAway;
        }

        public String getHomeTeam() {
            return homeTeam;
        }

        public void setHomeTeam(String homeTeam) {
            this.homeTeam = homeTeam;
        }

        public String getAwayTeam() {
            return awayTeam;
        }

        public void setAwayTeam(String awayTeam) {
            this.awayTeam = awayTeam;
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

    }
}
