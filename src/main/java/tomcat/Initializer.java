package tomcat;

import database.DatabaseManager;
import engine.Engine;
import java.sql.SQLException;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

@WebListener
public class Initializer implements ServletContextListener {

    public static String ATTRIBUTE_ENGINE = "Engine";
    private final static Logger logger = Logger.getLogger(Initializer.class);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("contextInitialized");
        String databasePath = DatabaseManager.getDefaultDatabasePath();
        logger.info("databasePath = " + databasePath);

        DatabaseManager manager = new DatabaseManager(databasePath);

        try {
            sce.getServletContext().setAttribute(ATTRIBUTE_ENGINE, new Engine(manager));
        } catch (SQLException ex) {
            logger.error("init err" + ex);
        }

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("contextDestroyed");
    }
}
