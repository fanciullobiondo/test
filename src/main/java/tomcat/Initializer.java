package tomcat;

import database.DatabaseManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class Initializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("init");
//        try {
//            System.out.println(((DatabaseManager) sce.getServletContext().getAttribute(TomcatManager.ATTRIBUTE_DATABASE_MANAGER)));
//        } catch (SQLException ex) {
//            Logger.getLogger(Initializer.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("destroy!");
    }
}
