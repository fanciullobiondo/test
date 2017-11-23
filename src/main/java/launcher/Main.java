package launcher;

import database.DatabaseManager;
import engine.Engine;
import java.io.IOException;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
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
        if (port < 8000 || port > 9000) {
            throw new IllegalArgumentException("port " + port + " non va bene");
        }
        kill(port);

        TomcatManager tomcat = new TomcatManager();

        // nuova partita
        // creo db col nome dato o lo apro
        DatabaseManager manager = new DatabaseManager(databasePath, "esempio 1 oggi");


        engine.Engine engine = new Engine(manager);
//        engine.insertTeams(EmbeddedData.ALL_TEAMS.values()
//            .stream()
//            .filter(m -> m.getLeague().getId() == EmbeddedData.League.CAMPIONATO)
//            .limit(2)
//            .map(t -> t.getName())
//            .collect(Collectors.toList()));
////
//        int newSeason = engine.newSeason();
//        System.out.println("news" + newSeason);
//        System.out.println(manager.getRelativeRoundIndex(newSeason));
//        System.out.println(manager.getRelativeRoundIndex(newSeason+1));




        try {
            tomcat.start(port, engine);
        } catch (ServletException | LifecycleException e) {
            System.err.println("Error on startup tomcat");
            System.err.println(e);
            tomcat.shutdown();
        }
    }

    private static void kill(int port) throws IOException, InterruptedException {
        Runtime.getRuntime().exec("fuser -k " + port + "/tcp").waitFor();

    }
}
