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
        registerTeam(new Team("camp1", League.campionato(), 2, 4));
        registerTeam(new Team("camp2", League.campionato(), 2, 4));
        registerTeam(new Team("camp3", League.campionato(), 2, 4));
        registerTeam(new Team("camp4", League.campionato(), 2, 4));
        registerTeam(new Team("camp5", League.campionato(), 2, 4));
        registerTeam(new Team("camp6", League.campionato(), 2, 4));
        registerTeam(new Team("camp7", League.campionato(), 2, 4));
        registerTeam(new Team("camp8", League.campionato(), 2, 4));
        registerTeam(new Team("camp9", League.campionato(), 2, 4));

        for (int i = 0; i < 18; i++) {
            registerTeam(new Team("eurl" + i, League.europaLeague(), 2, 4));
            registerTeam(new Team("ch" + i, League.championsLeague(), 2, 4));
        }

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
        public final static int EUROPA = 3;
        private final int id;
        private final int subid;
        public final static int SUB_NONE = 0;
        public final static int SUB_EUROPALEAGUE = 1;
        public final static int SUB_CHAMPIONSLEAGUE = 2;

        public static String getRelativeNameOfRound(int idleague, int index) {
            switch (idleague) {
                case CAMPIONATO:
                    return "Giornata " + (index+1);
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
