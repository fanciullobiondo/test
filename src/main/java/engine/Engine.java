/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import client.ApiClient;
import client.BadRequestException;
import data.EmbeddedData;
import static data.EmbeddedData.League.CAMPIONATO;
import static data.EmbeddedData.League.CHAMPIONS_LEAGUE;
import static data.EmbeddedData.League.COPPA;
import static data.EmbeddedData.League.EUROPA_LEAGUE;
import data.LeagueTable;
import database.DatabaseManager;
import database.bean.Match;
import database.bean.QueryBuilder.WhereClause;
import database.bean.Round;
import database.bean.Season;
import database.bean.Team;
import engine.SeasonCalculator.SingleMatch;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

    public static final int CAMPIONATO_N_TEAMS = 8;
    public static final int EUROPA_LEAGUE_N_TEAMS = 16;
    public static final int EUROPA_LEAGUE_CPU_TEAMS = 12;
    public static final int EUROPA_CHAMPIONS_N_TEAMS = 16;
    public static final int EUROPA_CHAMPIONS_CPU_TEAMS = 13;
    public static final int ALL_N_TEAMS = 40;
    public static final int ALL_CPU_TEAMS = 33;

    public static final LinkedList<Integer> MATCHES_PATTERN = new LinkedList<>(Arrays.asList(
        CAMPIONATO,
        EUROPA_LEAGUE,
        CAMPIONATO,
        COPPA,
        CAMPIONATO,
        EUROPA_LEAGUE,
        CAMPIONATO,
        COPPA,
        CAMPIONATO,
        EUROPA_LEAGUE,
        CAMPIONATO,
        COPPA,
        CAMPIONATO,
        EUROPA_LEAGUE
    ));

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
        int actualRound = manager.getActualRound();
        if (actualRound == -1 && !manager.queryEntity(Team.class, null).isEmpty()) {
            return newSeason();
        }
        return actualRound;
    }

    public List<ApiClient.RoundMatch> getRoundMatches(int idround) throws SQLException {
        List<ApiClient.RoundMatch> finalMatches = new ArrayList<>();
        List<Round> getRounds = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround);
        if (getRounds.isEmpty()) {
            return null;
        }
        List<Match> getMatches = manager.queryEntity(Match.class, WhereClause.instance().field("idround", "="), idround);
        LeagueTable league = calculateLeagueTable(idround - 1, getRounds.get(0).getLeague());

        for (Match matche : getMatches) {
            ApiClient.RoundMatch roundMatch = new ApiClient.RoundMatch();
            roundMatch.setIdmatch(matche.getIdmatch());
            Team teamHome = manager.queryEntity(Team.class, WhereClause.instance().field("idteam", "="), matche.getIdTeamHome()).get(0);
            roundMatch.setHome(new ApiClient.RoundMatch.TeamResult(teamHome.getIdteam(),
                teamHome.getName(), matche.getGoalHome(), teamHome.isOfuser()));
            Team teamAway = manager.queryEntity(Team.class, WhereClause.instance().field("idteam", "="), matche.getIdTeamAway()).get(0);
            roundMatch.setAway(new ApiClient.RoundMatch.TeamResult(teamAway.getIdteam(),
                teamAway.getName(), matche.getGoalAway(), teamAway.isOfuser()));
            roundMatch.setEditable(teamHome.isOfuser() || teamAway.isOfuser());
            if (league != null) {
                roundMatch.setMoneyHome(calculateMoneyForTeamLeagueTable(teamAway.getIdteam(), league));
                roundMatch.setMoneyAway(calculateMoneyForTeamLeagueTable(teamHome.getIdteam(), league));
            }

            finalMatches.add(roundMatch);
        }
        return finalMatches;
    }

    public boolean isRoundPlayed(int idround) throws SQLException {
        List<Round> rs = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround);
        if (rs.isEmpty()) {
            return false;
        }
        return rs.get(0).isPlayed();

    }

    public void insertTeams(List<String> choosen) throws SQLException {

        for (String string : choosen) {
            if (EmbeddedData.ALL_TEAMS.get(string).getLeague().getId() != CAMPIONATO) {
                throw new IllegalArgumentException("S possno sceglere solo squadre di campionato");
            }
            Team t = Team.of(EmbeddedData.ALL_TEAMS.get(string));
            t.setOfuser(true);
            int id = manager.insertEntity(t);
        }

        int campionatoCPUTeam = CAMPIONATO_N_TEAMS - choosen.size();

        {
            List<EmbeddedData.Team> allteams = new ArrayList<>(
                EmbeddedData.ALL_TEAMS.values()
                    .stream()
                    .filter(s -> s.getLeague().getId() == CAMPIONATO)
                    .collect(Collectors.toList()));
            Collections.shuffle(allteams);
            reallyInsertCPUTeams(allteams, campionatoCPUTeam, choosen);
        }
        {
            List<EmbeddedData.Team> allteams = new ArrayList<>(
                EmbeddedData.ALL_TEAMS.values()
                    .stream()
                    .filter(s -> s.getLeague().getId() == EUROPA_LEAGUE)
                    .collect(Collectors.toList()));
            Collections.shuffle(allteams);
            reallyInsertCPUTeams(allteams, EUROPA_LEAGUE_N_TEAMS, choosen);
        }
        {
            List<EmbeddedData.Team> allteams = new ArrayList<>(
                EmbeddedData.ALL_TEAMS.values()
                    .stream()
                    .filter(s -> s.getLeague().getId() == CHAMPIONS_LEAGUE)
                    .collect(Collectors.toList()));
            Collections.shuffle(allteams);
            reallyInsertCPUTeams(allteams, EUROPA_CHAMPIONS_N_TEAMS, choosen);
        }

        if (choosen.size() != ALL_N_TEAMS) {
            throw new IllegalStateException("le squadre inserite sono " + choosen.size() + " ma devono essere " + ALL_N_TEAMS);
        }
        System.out.println("alla fine squadre scelte " + choosen);

    }

    private void reallyInsertCPUTeams(List<EmbeddedData.Team> teams, int size, List<String> choosen) throws SQLException {

        for (int i = 0; i < size; i++) {
            boolean inserted = false;
            for (EmbeddedData.Team allteam : teams) {
                if (choosen.contains(allteam.getName())) {
                    continue;
                }
                Team t = Team.of(EmbeddedData.ALL_TEAMS.get(allteam.getName()));
                t.setOfuser(false);
                manager.insertEntity(t);
                choosen.add(allteam.getName());
                inserted = true;
                break;
            }
            if (!inserted) {
                throw new IllegalStateException("non sono riuscito a inserire una qsuadra su " + size);
            }
        }
    }

    public Map<String, Object> getRoundInfo(int idround) throws SQLException {

        List<Round> rounds = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround);
        if (rounds.isEmpty()) {
            return null;
        }

        Map<String, Object> res = new HashMap<>();
        res.put("league", EmbeddedData.League.getNameById(rounds.get(0).getLeague()));
        res.put("season", rounds.get(0).getIdSeason());
        res.put("relativeround", manager.getRelativeRoundIndex(idround));
        res.put("idround", idround);
        return res;
    }

    public LeagueTable calculateLeagueTable(int idround, int idleague) throws SQLException {
        List<Round> round = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround);
        if (round == null || round.isEmpty()) {
            return null;
        }
        return manager.calculateLeagueTable(round.get(0).getIdSeason(), idleague);
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
        List<Team> teams = manager.queryEntity(Team.class, null);
        if (teams.size() != ALL_N_TEAMS) {
            throw new IllegalStateException("Non posso iniziare una stagione con " + teams.size() + " squadre");
        }
        Season season = new Season();
        boolean first = manager.queryEntity(Season.class, null).isEmpty();

        int idSeason = manager.insertEntity(season);

        Map<Integer, List<Team>> allTeamsByLeague = new HashMap<>();

        List<Team> campionatoTeams = manager.queryEntity(Team.class, WhereClause.instance().field("league", "="), CAMPIONATO);
        allTeamsByLeague.put(EmbeddedData.League.CAMPIONATO, campionatoTeams);
        allTeamsByLeague.put(EmbeddedData.League.COPPA, campionatoTeams);

        if (first) {

            List<Team> eulTeams = manager.queryEntity(Team.class, WhereClause.instance().field("league", "="), EUROPA_LEAGUE);
            allTeamsByLeague.put(EmbeddedData.League.EUROPA_LEAGUE, eulTeams);

            List<Team> chTeams = manager.queryEntity(Team.class, WhereClause.instance().field("league", "="), CHAMPIONS_LEAGUE);
            allTeamsByLeague.put(EmbeddedData.League.CHAMPIONS_LEAGUE, chTeams);
        } else {
            {
                List<Team> cpuTeams = manager.getTeamsForLeague(EUROPA_LEAGUE_CPU_TEAMS, EUROPA_LEAGUE);
                cpuTeams.addAll(getQualifiedForLeague(idSeason - 1, EUROPA_LEAGUE));
                allTeamsByLeague.put(EmbeddedData.League.EUROPA_LEAGUE, cpuTeams);
            }
            {
                List<Team> cpuTeams = manager.getTeamsForLeague(EUROPA_CHAMPIONS_CPU_TEAMS, CHAMPIONS_LEAGUE);
                cpuTeams.addAll(getQualifiedForLeague(idSeason - 1, CHAMPIONS_LEAGUE));
                allTeamsByLeague.put(EmbeddedData.League.CHAMPIONS_LEAGUE, cpuTeams);
            }

        }

        System.out.println("allT" + allTeamsByLeague);

        SeasonCalculator seasonCalculator = new SeasonCalculator(allTeamsByLeague);

        Map<Integer, List<SeasonCalculator.SingleMatch>> calculatedRound = seasonCalculator.run();
        for (Map.Entry<Integer, List<SingleMatch>> entry : calculatedRound.entrySet()) {
            Integer key = entry.getKey();
            List<SingleMatch> value = entry.getValue();
            System.out.println("k:" + key + " " + value.get(0).league + "  " + value.size());
        }

        Map<Integer, List<Integer>> leagueRounds = new HashMap<>();
        for (Map.Entry<Integer, List<SingleMatch>> entry : calculatedRound.entrySet()) {
            Integer key = entry.getKey();
            List<SingleMatch> value = entry.getValue();
            int league = value.get(0).league;
            if (leagueRounds.get(league) == null) {
                leagueRounds.put(league, new LinkedList<>());
            }
            leagueRounds.get(league).add(key);
        }

        System.out.println(leagueRounds);

        Map<Integer, List<SeasonCalculator.SingleMatch>> sortedRound = new LinkedHashMap<>();
        for (Integer idleague : MATCHES_PATTERN) {
            System.out.println(idleague);
            if (leagueRounds.get(idleague).isEmpty()) {
                throw new IllegalStateException("impposbile");
            }
            Integer idRound = leagueRounds.get(idleague).remove(0);
            sortedRound.put(idRound, calculatedRound.get(idRound));
        }
//        for (Map.Entry<Integer, List<SingleMatch>> entry : sortedRound.entrySet()) {
//            Integer key = entry.getKey();
//            List<SingleMatch> value = entry.getValue();
//            System.out.println("league:" + key + "" + value);
//        }

        int nextRound = 0;
        for (Map.Entry<Integer, List<SingleMatch>> entry : sortedRound.entrySet()) {
            Round r = new Round();
            r.setIdSeason(idSeason);
            r.setLeague(entry.getValue().get(0).league);
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

    public int getPositionForTeams(int idteam, int idseason) throws SQLException {
        String name = manager.findTeamById(idteam).getName();
        return getPositionsOfSeason(idseason, true).get(name);
    }

    public Map<String, Integer> getPositionsOfSeason(int idseason, boolean reverse) throws SQLException {
        Map<String, Integer> positions = new HashMap<>();
        LeagueTable table = manager.calculateLeagueTable(idseason, CAMPIONATO);
        Collections.sort(table.getRows(), LeagueTable.compare);
        if (reverse) {
            Collections.reverse(table.getRows());
        }
        for (int i = 0; i < table.getRows().size(); i++) {
            positions.put(table.getRows().get(i).getTeamName(), i);
        }
        return positions;
    }

    public List<Team> getQualifiedForLeague(int idseason, int league) throws SQLException {
        List<Team> res = new ArrayList<>();
        Map<String, Integer> ps = getPositionsOfSeason(idseason, false);
        if (league == CHAMPIONS_LEAGUE) {
            res.add(manager.findTeamById(ps.get(0)));
            res.add(manager.findTeamById(ps.get(1)));
            res.add(manager.findTeamById(ps.get(2)));
        } else {
            res.add(manager.findTeamById(ps.get(3)));
            res.add(manager.findTeamById(ps.get(4)));
            res.add(manager.findTeamById(ps.get(5)));
            res.add(manager.findTeamById(ps.get(6)));
        }
        return res;
    }

    public int calculateMoneyForTeamLeagueTable(int idteam, LeagueTable table) throws SQLException {
        return getPositionForTeams(idteam, table.getIdseason()) * 5;
    }
}
