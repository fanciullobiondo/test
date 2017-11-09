/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.Arrays;
import java.util.List;

/**
 *
 * @author nicolo.boschi
 */
public class EmbeddedData {

    public static List<Team> ALL_TEAMS = Arrays.asList(
        new Team("a", League.campionato()));

    public static class Team {

        private final String name;
        private final League league;

        public Team(String name, League league) {
            this.name = name;
            this.league = league;
        }

        public String getName() {
            return name;
        }

        public League getLeague() {
            return league;
        }

    }

    public static class League {

        public static int CAMPIONATO = 1;
        public static int COPPA = 2;
        public static int CHAMPIONS_LEAGUE = 3;
        public static int EUROPA_LEAGUE = 4;
        private final String name;
        private final int id;

        public String getName() {
            return name;
        }

        public int getId() {
            return id;
        }

        public static League campionato() {
            return new League("campionato", CAMPIONATO);
        }

        public static League coppa() {
            return new League("coppa", COPPA);
        }

        public static League europaLeague() {
            return new League("europaLeague", EUROPA_LEAGUE);
        }

        public static League championsLeague() {
            return new League("championsLeague", CHAMPIONS_LEAGUE);
        }

        private League(String name, int id) {
            this.name = name;
            this.id = id;
        }

    }

}
