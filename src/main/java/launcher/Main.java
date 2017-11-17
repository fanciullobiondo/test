package launcher;

import data.EmbeddedData;
import database.DatabaseManager;
import database.bean.Match;
import database.bean.QueryBuilder.WhereClause;
import database.bean.Team;
import engine.Engine;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
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
//        engine.insertTeams(EmbeddedData.ALL_TEAMS.values().stream().limit(2).map(t -> t.getName()).collect(Collectors.toList()));
            
//        engine.newSeason();

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
