package launcher;

import database.DatabaseManager;
import java.sql.SQLException;
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
        String databasePath = System.getenv("HOME") + "/";
        if (args.length >= 1) {
            port = Integer.parseInt(args[0]);
        }
        if (args.length >= 2) {
            databasePath = args[1];
        }

        TomcatManager tomcat = new TomcatManager();
        try {
            DatabaseManager.initDatabase(databasePath);
            tomcat.start(port);
        } catch (SQLException | ServletException | LifecycleException e) {
            System.err.println("Error on startup");
            System.err.println(e);
            tomcat.shutdown();
        }
    }

}
