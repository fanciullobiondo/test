/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nicolo.boschi
 */
public class EmbeddedData {

    public final static Map<String, Team> ALL_TEAMS = new HashMap<>();

    static {
        ALL_TEAMS.put("Primo", new Team("Primo", League.campionato(), 2, 4));
        ALL_TEAMS.put("Secondo", new Team("Secondo", League.campionato(), 2, 4));
        ALL_TEAMS.put("Terzo", new Team("Terzo", League.campionato(), 2, 4));
        ALL_TEAMS.put("qu", new Team("qu", League.campionato(), 2, 4));
        ALL_TEAMS.put("ci", new Team("ci", League.campionato(), 2, 4));
        ALL_TEAMS.put("sei", new Team("sei", League.campionato(), 2, 4));
        ALL_TEAMS.put("sett", new Team("sett", League.campionato(), 2, 4));
        ALL_TEAMS.put("ott", new Team("ott", League.campionato(), 2, 4));
        ALL_TEAMS.put("DICIIO", new Team("ott", League.campionato(), 2, 4));
    }

    public static class Team {

        private final String name;
        private final League league;
        private final int defence;
        private final int attack;

        public Team(String name, League league, int defence, int attack) {
            this.name = name;
            this.league = league;
            this.defence = defence;
            this.attack = attack;
        }

        public int getDefence() {
            return defence;
        }

        public int getAttack() {
            return attack;
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
