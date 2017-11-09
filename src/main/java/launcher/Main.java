package launcher;

import data.EmbeddedData;
import database.DatabaseManager;
import database.bean.Match;
import database.bean.QueryBuilder.WhereClause;
import database.bean.Team;
import engine.Engine;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import tomcat.TomcatManager;

/**
 *
 * @author nicolo.boschi
 */
public class Main {

    public static void main(String[] args) throws Exception {
        int port = 8097;
//        String databasePath = System.getenv("HOME") + "/";
        String databasePath = "";
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            databasePath = args[1];
        }

        TomcatManager tomcat = new TomcatManager();

        // nuova partita
        // creo db col nome dato o lo apro
        DatabaseManager manager = new DatabaseManager(databasePath, "esempio 1 oggi");
        manager.initDatabase();
        // scegliere squadre, mostro tutte quelle in embeddedData
        //
        for (int i = 0; i < 1; i++) {
            Team t = Team.of(EmbeddedData.ALL_TEAMS.get(0));
            t.setOfuser(true);
            int id = manager.insertEntity(t);
        }

        engine.Engine engine = new Engine(manager);
        int c = 1;
        simulateSeasons(manager, engine, c);
        System.out.println("primo anno" +  engine.calculateLeagueTable(1, 1));
//        try {
//            tomcat.start(port, manager);
//        } catch (ServletException | LifecycleException e) {
//            System.err.println("Error on startup tomcat");
//            System.err.println(e);
//            tomcat.shutdown();
//        }
    }

    private static boolean simulateSeasons(DatabaseManager manager, engine.Engine engine, int ns) throws SQLException {
        int round = engine.newSeason();
        // calcolo
        {
            List<Match> matches = manager.queryEntity(Match.class, WhereClause.instance().field("idround", "="), round);
            List<Match> results = new ArrayList<>();
            for (Match matche : matches) {
                matche.setGoalHome(1);
                matche.setGoalAway(3);
                results.add(matche);
            }
            round = engine.nextRound(results);
        }

        {
            List<Match> matches = manager.queryEntity(Match.class, WhereClause.instance().field("idround", "="), round);
            List<Match> results = new ArrayList<>();
            for (Match matche : matches) {
                matche.setGoalHome(2);
                matche.setGoalAway(2);
                results.add(matche);
            }
            round = engine.nextRound(results);
        }
        {
            List<Match> matches = manager.queryEntity(Match.class, WhereClause.instance().field("idround", "="), round);
            List<Match> results = new ArrayList<>();
            for (Match matche : matches) {
                matche.setGoalHome(2);
                matche.setGoalAway(2);
                matche.setGoalAway(2);
                results.add(matche);
            }
            round = engine.nextRound(results);
        }

        if (round == -1 && ns < 4) {
            return simulateSeasons(manager, engine, ++ns);
        }
        return true;

    }

}
