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

    private static void registerTeam(Team t) {
        ALL_TEAMS.put(t.getName(), t);
    }

    static {
        registerTeam(new Team("Juventus", League.campionato(), 4, 6));
        registerTeam(new Team("Inter", League.campionato(), 3, 5));
        registerTeam(new Team("Milan", League.campionato(), 2, 4));
        registerTeam(new Team("Napoli", League.campionato(), 2, 7));
        registerTeam(new Team("Roma", League.campionato(), 4, 4));
        registerTeam(new Team("Lazio", League.campionato(), 3, 6));
        registerTeam(new Team("Fiorentina", League.campionato(), 2, 4));
        registerTeam(new Team("Udinese", League.campionato(), 2, 3));
        registerTeam(new Team("Sampdoria", League.campionato(), 2, 4));
        registerTeam(new Team("Atalanta", League.campionato(), 4, 5));
        registerTeam(new Team("Chievo Verona", League.campionato(), 4, 3));
        registerTeam(new Team("Benevento", League.campionato(), 1, 1));
        registerTeam(new Team("Crotone", League.campionato(), 1, 2));
        registerTeam(new Team("Genoa", League.campionato(), 2, 3));
        registerTeam(new Team("Bologna", League.campionato(), 2, 2));
        registerTeam(new Team("Torino", League.campionato(), 3, 4));
        registerTeam(new Team("SPAL", League.campionato(), 1, 1));
        registerTeam(new Team("Sassuolo", League.campionato(), 2, 2));
        registerTeam(new Team("Hellas Verona", League.campionato(), 1, 1));

        registerTeam(new Team("Real Madrid", League.championsLeague(), 4, 7));
        registerTeam(new Team("Barcelona", League.championsLeague(), 4, 7));
        registerTeam(new Team("Atletico Madrid", League.championsLeague(), 4, 5));
        registerTeam(new Team("Manchester United", League.championsLeague(), 3, 5));
        registerTeam(new Team("Chelsea", League.championsLeague(), 3, 6));
        registerTeam(new Team("Manchester City", League.championsLeague(), 3, 7));
        registerTeam(new Team("PSG", League.championsLeague(), 3, 6));
        registerTeam(new Team("Monaco", League.championsLeague(), 2, 6));
        registerTeam(new Team("Bayern Monaco", League.championsLeague(), 4, 6));
        registerTeam(new Team("Borussia Dortmund", League.championsLeague(), 3, 5));
        registerTeam(new Team("Porto", League.championsLeague(), 3, 4));
        registerTeam(new Team("Benfica", League.championsLeague(), 2, 4));
        registerTeam(new Team("Zenit", League.championsLeague(), 2, 4));
        registerTeam(new Team("Ajax", League.championsLeague(), 2, 3));
        registerTeam(new Team("Lione", League.championsLeague(), 2, 3));
        registerTeam(new Team("Celtic", League.championsLeague(), 1, 3));
        registerTeam(new Team("Olympiakos", League.championsLeague(), 2, 2));
        
        registerTeam(new Team("Liverpool", League.europaLeague(), 3, 5));
        registerTeam(new Team("Tottenham", League.europaLeague(), 3, 6));
        registerTeam(new Team("Arsenal", League.europaLeague(), 3, 5));

        registerTeam(new Team("Villareal", League.europaLeague(), 3, 4));
        registerTeam(new Team("Siviglia", League.europaLeague(), 3, 4));
        registerTeam(new Team("Valencia", League.europaLeague(), 2, 6));

        registerTeam(new Team("Nizza", League.europaLeague(), 2, 4));
        registerTeam(new Team("Lille", League.europaLeague(), 1, 3));

        registerTeam(new Team("Galatasaray", League.europaLeague(), 2, 2));
        registerTeam(new Team("Fenerbahce", League.europaLeague(), 2, 1));

        registerTeam(new Team("Dnipro", League.europaLeague(), 2, 1));
        registerTeam(new Team("Bayer Leverkusen", League.europaLeague(), 3, 3));
        registerTeam(new Team("Schalke 04", League.europaLeague(), 3, 4));
        
        registerTeam(new Team("Wolfsburg", League.europaLeague(), 3, 4));
        registerTeam(new Team("Braga", League.europaLeague(), 2, 2));
        registerTeam(new Team("Dinamo Kiev", League.europaLeague(), 2, 2));
        registerTeam(new Team("AEK Atene", League.europaLeague(), 1, 2));

//        for (int i = 0; i < 18; i++) {
//            registerTeam(new Team("eurl" + i, League.europaLeague(), 2, 4));
//            registerTeam(new Team("ch" + i, League.championsLeague(), 2, 4));
//        }

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
            if (defence < 1 || defence > 7 || attack < 1 || defence > 7) {
                throw new IllegalArgumentException("Squadra " + name + " non valida");
            }
            if (defence + attack > 11) {
                throw new IllegalArgumentException("Squadra " + name + " non valida, troppo alti");
            }
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
        public final static int EUROPA = 3;
        private final int id;
        private final int subid;
        public final static int SUB_NONE = 0;
        public final static int SUB_EUROPALEAGUE = 1;
        public final static int SUB_CHAMPIONSLEAGUE = 2;

        public static String getRelativeNameOfRound(int idleague, int index) {
            switch (idleague) {
                case CAMPIONATO:
                    return "Giornata " + (index + 1);
                case COPPA:
                    switch (index) {
                        case 0:
                            return "Quarti di finale";
                        case 1:
                            return "Semifinale";
                        case 2:
                            return "Finale";
                    }
                    break;
                case EUROPA:
                    switch (index) {
                        case 0:
                            return "Ottavi di finale";
                        case 1:
                            return "Quarti di finale";
                        case 2:
                            return "Semifinale";
                        case 3:
                            return "Finale";
                    }

            }
            return "?";
        }

        public static League getLeague(int id, int subid) {
            if ((id == CAMPIONATO || id == COPPA) && subid != SUB_NONE) {
                throw new IllegalArgumentException("non esiste " + id + " , " + subid);
            }
            if (id == EUROPA && (subid != SUB_CHAMPIONSLEAGUE && subid != SUB_EUROPALEAGUE)) {
                throw new IllegalArgumentException("non esiste " + id + " , " + subid);
            }
            return new League(id, subid);
        }

        public static String getNameById(int id) {
            switch (id) {
                case CAMPIONATO:
                    return "Campionato";
                case COPPA:
                    return "Coppa";
                case EUROPA:
                    return "Turno europeo";
            }
            return "?";
        }

        public int getId() {
            return id;
        }

        public int getSubid() {
            return subid;
        }

        public static League campionato() {
            return new League(CAMPIONATO, 0);
        }

        public static League coppa() {
            return new League(COPPA, 0);
        }

        public static League europaLeague() {
            return new League(EUROPA, SUB_EUROPALEAGUE);
        }

        public static League championsLeague() {
            return new League(EUROPA, SUB_CHAMPIONSLEAGUE);
        }

        private League(int id, int subid) {
            this.id = id;
            this.subid = subid;
        }

    }

}
