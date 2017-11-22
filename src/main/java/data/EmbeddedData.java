/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package data;

import engine.Engine;
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
        
        ALL_TEAMS.put("eu1", new Team("eu1", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu2", new Team("eu2", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu3", new Team("eu3", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu4", new Team("eu4", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu5", new Team("eu5", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu6", new Team("eu6", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu7", new Team("eu7", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu8", new Team("eu8", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu9", new Team("eu9", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu10", new Team("eu10", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu11", new Team("eu11", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu12", new Team("eu12", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu13", new Team("eu13", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu14", new Team("eu14", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu15", new Team("eu15", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu16", new Team("eu16", League.europaLeague(), 2, 4));
        ALL_TEAMS.put("eu17", new Team("eu17", League.europaLeague(), 2, 4));
        
        ALL_TEAMS.put("ch1", new Team("ch1", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch2", new Team("ch2", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch3", new Team("ch3", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch4", new Team("ch4", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch5", new Team("ch5", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch6", new Team("ch6", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch7", new Team("ch7", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch8", new Team("ch8", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch9", new Team("ch9", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch10", new Team("ch10", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch11", new Team("ch11", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch12", new Team("ch12", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch13", new Team("ch13", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch14", new Team("ch14", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch15", new Team("ch15", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch16", new Team("ch16", League.championsLeague(), 2, 4));
        ALL_TEAMS.put("ch17", new Team("ch17", League.championsLeague(), 2, 4));

        if (ALL_TEAMS.size() < Engine.ALL_N_TEAMS) {
            throw new IllegalArgumentException("le squadre devono essere almeno 34");
        }
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

        public final static int CAMPIONATO = 1;
        public final static int COPPA = 2;
        public final static int CHAMPIONS_LEAGUE = 3;
        public final static int EUROPA_LEAGUE = 4;
        private final String name;
        private final int id;
        public static String getNameById(int id) {
            switch (id ) {
                case CAMPIONATO:
                    return "campionato";
                case COPPA:
                    return "Coppa";
                case CHAMPIONS_LEAGUE:
                    return "Champions League";
                case EUROPA_LEAGUE:
                    return "Europa League";
            }
            return "?";
        }

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
