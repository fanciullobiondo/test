/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tomcat;

import engine.Engine;
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
    private static final Logger LOG = Logger.getLogger(TomcatManager.class.getName());


    public static String ATTRIBUTE_ENGINE = "Engine";

    public void start(final int port, final Engine engine) throws ServletException, LifecycleException, MalformedURLException {
        String webappDirLocation = "src/main/webapp/";
        instance = new Tomcat();

        instance.setPort(port);
        LOG.log(Level.INFO, "Tomcat started at \n\nhttp://localhost:{0}/embedded\n", String.valueOf(port));
        

        StandardContext ctx = (StandardContext) instance.addWebapp("/embedded", new File(webappDirLocation).getAbsolutePath());
        Tomcat.addServlet(ctx, "jersey-container-servlet", new ServletContainer(new ResourceConfig(new ApplicationConfig().getClasses())));
        ctx.addServletMapping("/rest/*", "jersey-container-servlet");
        ctx.addApplicationListener(Initializer.class.getName());
        ServletContext servletContext = ctx.getServletContext();
        servletContext.setAttribute(ATTRIBUTE_ENGINE, engine);
        

        instance.start();
        instance.getServer().await();
        instance.getService();
        

    }

    public void shutdown() throws LifecycleException {
        if (instance != null) {
            instance.stop();
            instance.destroy();
        }
    }

}
