package tomcat;

import database.DatabaseManager;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import javax.ws.rs.core.Context;
import org.apache.catalina.core.StandardContext;

@WebListener
public class Initializer implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("init");
        
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("destroy!");
    }
}
