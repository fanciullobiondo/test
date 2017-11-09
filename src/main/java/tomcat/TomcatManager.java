/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tomcat;

import database.DatabaseManager;
import java.io.File;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

/**
 *
 * @author nicolo.boschi
 */
public class TomcatManager {

    private Tomcat instance;

    public static String ATTRIBUTE_DATABASE_MANAGER = "DatabaseManager";

    public void start(final int port, final DatabaseManager databaseManager) throws ServletException, LifecycleException, MalformedURLException {
        String webappDirLocation = "src/main/webapp/";
        instance = new Tomcat();

        instance.setPort(port);

        StandardContext ctx = (StandardContext) instance.addWebapp("/embedded", new File(webappDirLocation).getAbsolutePath());
        Tomcat.addServlet(ctx, "jersey-container-servlet", new ServletContainer(new ResourceConfig(new ApplicationConfig().getClasses())));
        ctx.addServletMapping("/rest/*", "jersey-container-servlet");
        ctx.addApplicationListener(Initializer.class.getName());
        ServletContext servletContext = ctx.getServletContext();
        servletContext.setAttribute(ATTRIBUTE_DATABASE_MANAGER, databaseManager);
        

        instance.start();
        instance.getServer().await();
        instance.getService();

        Logger.getLogger("TomcatManager").log(Level.INFO, "tomcat started at http://localhost:{0}/embedded", port);
    }

    public void shutdown() throws LifecycleException {
        if (instance != null) {
            instance.stop();
            instance.destroy();
        }
    }

}
