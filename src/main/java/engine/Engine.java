/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package engine;

import client.ApiClient;
import client.BadRequestException;
import data.BillboardTable;
import data.EmbeddedData;
import static data.EmbeddedData.League.CAMPIONATO;
import static data.EmbeddedData.League.COPPA;
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
import static data.EmbeddedData.League.EUROPA;

/**
 *
 * @author nicolo.boschi
 */
public class Engine {

    private DatabaseManager manager;

    public static final int CAMPIONATO_N_TEAMS = 8;
    public static final int EUROPA_LEAGUE_N_TEAMS = 16;
    public static final int EUROPA_LEAGUE_CPU_TEAMS = 12;
    public static final int EUROPA_CHAMPIONS_N_TEAMS = 16;
    public static final int EUROPA_CHAMPIONS_CPU_TEAMS = 13;
    public static final int ALL_N_TEAMS = 40;
    public static final int ALL_CPU_TEAMS = 33;

    public static final LinkedList<Integer> MATCHES_PATTERN = new LinkedList<>(Arrays.asList(CAMPIONATO,
        EUROPA,
        CAMPIONATO,
        COPPA,
        CAMPIONATO,
        EUROPA,
        CAMPIONATO,
        COPPA,
        CAMPIONATO,
        EUROPA,
        CAMPIONATO,
        COPPA,
        CAMPIONATO,
        EUROPA
    ));

    public Engine() {
    }


    public String getDatabasesPath() {
        return manager.getDatabasePath();
    }
    public Engine(DatabaseManager manager) throws SQLException {
        this.manager = manager;
    }

    public boolean startDatabase(String name) throws SQLException {
        return manager.initDatabase(name);
    }

    public static class MatchResult {

        int goalhome;
        int goalaway;

        @Override
        public String toString() {
            return "MatchResult{" + "goalhome=" + goalhome + ", goalaway=" + goalaway + '}';
        }

    }

    public MatchResult simulateMatch(Team home, Team away, boolean canDraw) {
        MatchResult r = new MatchResult();
        while (r.goalhome == r.goalaway) {
            int goalHome = simulateDice(home.getAttack()) - simulateDice(away.getDefence());
            int goalAway = simulateDice(away.getAttack()) - simulateDice(home.getDefence());
            r.goalhome = goalHome > 0 ? goalHome : 0;
            r.goalaway = goalAway > 0 ? goalAway : 0;
            if (canDraw) {
                break;
            }
        }

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
        if (theRound.getLeague() != CAMPIONATO) {
            System.out.println("passed= " + passed);
            System.out.println("all" + allMatchToSimulate) ;
            boolean anyMatch = passed.stream().anyMatch(m -> m.getGoalAway() == m.getGoalHome() && !allMatchToSimulate.contains(m));
            if (anyMatch) {
                throw new BadRequestException("Non puoi inserire pareggi.");
            }
        }


        

        for (Match simulated : allMatchToSimulate) {
            MatchResult rs = simulateMatch(manager.findTeamById(simulated.getIdTeamHome()),
                manager.findTeamById(simulated.getIdTeamAway()), theRound.getLeague() == CAMPIONATO);
            for (Match real : passed) {
                if (real.getIdmatch() == simulated.getIdmatch()) {
                    real.setGoalHome(rs.goalhome);
                    real.setGoalAway(rs.goalaway);
                    real.setSubIdLeague(simulated.getSubIdLeague());
                    break;
                }
            }
        }

        manager.updateRound(passed, idround);

        if (theRound.getLeague() != CAMPIONATO) {
            int next = manager.getNextRound(idround);
            if (next != -1) {
                if (theRound.getLeague() == EUROPA) {
                    adjust(extractWinners(passed.stream()
                        .filter(t -> t.getSubIdLeague() == EmbeddedData.League.SUB_EUROPALEAGUE)
                        .collect(Collectors.toList())), next, EmbeddedData.League.SUB_EUROPALEAGUE);
                    adjust(extractWinners(passed.stream()
                        .filter(t -> t.getSubIdLeague() == EmbeddedData.League.SUB_CHAMPIONSLEAGUE)
                        .collect(Collectors.toList())), next, EmbeddedData.League.SUB_CHAMPIONSLEAGUE);
                } else {
                    adjust(extractWinners(passed), next, EmbeddedData.League.SUB_NONE);
                }

            }
        }

    }

    private static List<Integer> extractWinners(List<Match> m) {
        return m.stream()
            .map((t) -> {
                int h = t.getGoalHome();
                int a = t.getGoalAway();
                if (h > a) {
                    return t.getIdTeamHome();
                } else {
                    // TO DO GESTIRE PAREGGIO
                    return t.getIdTeamAway();
                }
            }).collect(Collectors.toList());
    }

    private void adjust(List<Integer> ids, int idround, int subleague) throws SQLException {
        SeasonCalculator seasonCalculator = new SeasonCalculator(null);
        seasonCalculator.calculateRound(ids, 0, true);
        Map<Integer, List<SingleMatch>> res = seasonCalculator.getResult();
        manager.adjustRoundMatches(res.get(0), idround, subleague);
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
        Round rou = getRounds.get(0);
        LeagueTable league = null;
        if (rou.getLeague() == CAMPIONATO) {
            league = calculateLeagueTable(idround - 1);
        }
        
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
            } else {
                roundMatch.setMoneyHome(40);
                roundMatch.setMoneyAway(40);
            }
            switch (getRounds.get(0).getLeague()) {
                case CAMPIONATO:
                    roundMatch.setEuropean(false);
                    roundMatch.setCampionato(true);
                    break;
                case COPPA:
                    roundMatch.setCampionato(false);
                    roundMatch.setEuropean(false);
                    break;
                case EUROPA:
                    roundMatch.setEuropean(true);
                    roundMatch.setCl(matche.getSubIdLeague() == EmbeddedData.League.SUB_CHAMPIONSLEAGUE);
                    break;
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
                    .filter(s -> s.getLeague().getId() == EUROPA && s.getLeague().getSubid() == EmbeddedData.League.SUB_EUROPALEAGUE)
                    .collect(Collectors.toList()));
            Collections.shuffle(allteams);
            reallyInsertCPUTeams(allteams, EUROPA_LEAGUE_N_TEAMS, choosen);
        }
        {
            List<EmbeddedData.Team> allteams = new ArrayList<>(
                EmbeddedData.ALL_TEAMS.values()
                    .stream()
                    .filter(s -> s.getLeague().getId() == EUROPA && s.getLeague().getSubid() == EmbeddedData.League.SUB_CHAMPIONSLEAGUE)
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
        res.put("relativeround", EmbeddedData.League.getRelativeNameOfRound(rounds.get(0).getLeague(), manager.getRelativeRoundIndex(idround)));
        res.put("idround", idround);
        return res;
    }

    public LeagueTable calculateLeagueTable(int idround) throws SQLException {
        List<Round> round = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround);
        if (round == null || round.isEmpty()) {
            return null;
        }
        return manager.calculateLeagueTable(round.get(0).getIdSeason(), CAMPIONATO);
    }

    public BillboardTable calculateBillboard(int idround, int idleague, int subleague) throws SQLException {
        List<Round> round = manager.queryEntity(Round.class, WhereClause.instance().field("idround", "="), idround);
        if (round == null || round.isEmpty()) {
            return null;
        }
        return manager.calculateBillboard(round.get(0).getIdSeason(), idleague, subleague);
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
            List<Team> eulTeams = manager.queryEntity(Team.class,
                WhereClause.instance()
                    .field("league", "="), EUROPA);

            allTeamsByLeague.put(EmbeddedData.League.EUROPA, eulTeams);
        } else {
            List<Team> cpuTeams = manager.getTeamsForLeague(EUROPA_LEAGUE_CPU_TEAMS, EUROPA, EmbeddedData.League.SUB_EUROPALEAGUE);
            List<Team> eut = getQualifiedForLeague(idSeason - 1, EmbeddedData.League.SUB_EUROPALEAGUE);
            for (Team team : eut) {
                team.setLeague(EUROPA);
                team.setSubleague(EmbeddedData.League.SUB_EUROPALEAGUE);
            }
            cpuTeams.addAll(eut);

            cpuTeams.addAll(manager.getTeamsForLeague(EUROPA_CHAMPIONS_CPU_TEAMS, EUROPA, EmbeddedData.League.SUB_CHAMPIONSLEAGUE));
            List<Team> t = getQualifiedForLeague(idSeason - 1, EmbeddedData.League.SUB_CHAMPIONSLEAGUE);
            for (Team team : t) {
                team.setLeague(EUROPA);
                team.setSubleague(EmbeddedData.League.SUB_CHAMPIONSLEAGUE);
            }
            cpuTeams.addAll(t);
            allTeamsByLeague.put(EUROPA, cpuTeams);

        }

        SeasonCalculator seasonCalculator = new SeasonCalculator(allTeamsByLeague);

        Map<Integer, List<SeasonCalculator.SingleMatch>> calculatedRound = seasonCalculator.run();

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


        Map<Integer, List<SeasonCalculator.SingleMatch>> sortedRound = new LinkedHashMap<>();
        for (Integer idleague : MATCHES_PATTERN) {
            if (leagueRounds.get(idleague).isEmpty()) {
                throw new IllegalStateException("impposbile");
            }
            Integer idRound = leagueRounds.get(idleague).remove(0);
            sortedRound.put(idRound, calculatedRound.get(idRound));
        }

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
                match.setSubIdLeague(singleMatch.subleague);
                manager.insertEntity(match);
            }
        }

        return nextRound;
    }

    public int getPositionForTeams(int idteam, int idseason) throws SQLException {
        return getPositionsOfSeason(idseason, true)
            .get(manager
                .findTeamById(idteam));
    }

    // squdra, pos
    private Map<Team, Integer> getPositionsOfSeason(int idseason, boolean reverse) throws SQLException {
        Map<Team, Integer> positions = new HashMap<>();
        LeagueTable table = manager.calculateLeagueTable(idseason, CAMPIONATO);
        Collections.sort(table.getRows(), LeagueTable.compare);
        if (reverse) {
            Collections.reverse(table.getRows());
        }
        for (int i = 0; i < table.getRows().size(); i++) {
            positions.put(manager.findTeamById(table.getRows().get(i).getTeamId()), i);
        }
        return positions;
    }

    private static Team getTeamAtPosition(Map<Team, Integer> l, int i) {
        for (Map.Entry<Team, Integer> entry : l.entrySet()) {
            if (entry.getValue() == i) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException();

    }

    private List<Team> getQualifiedForLeague(int idseason, int league) throws SQLException {
        List<Team> res = new ArrayList<>();
        Map<Team, Integer> ps = getPositionsOfSeason(idseason, false);
        if (league == EmbeddedData.League.SUB_CHAMPIONSLEAGUE) {
            res.add(getTeamAtPosition(ps, 0));
            res.add(getTeamAtPosition(ps, 1));
            res.add(getTeamAtPosition(ps, 2));
        } else {
            res.add(getTeamAtPosition(ps, 3));
            res.add(getTeamAtPosition(ps, 4));
            res.add(getTeamAtPosition(ps, 5));
            res.add(getTeamAtPosition(ps, 6));
        }
        return res;
    }

    public int calculateMoneyForTeamLeagueTable(int idteam, LeagueTable table) throws SQLException {

        if (table.getLeague() == CAMPIONATO) {
            return getPositionForTeams(idteam, table.getIdseason()) * 5;
        }
        return -8;

    }
}
