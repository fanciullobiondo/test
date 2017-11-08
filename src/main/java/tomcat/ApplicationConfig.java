/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tomcat;

import java.util.Set;
import javax.ws.rs.core.Application;

/**
 *
 * @author nicolo.boschi
 */
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        resources.add(client.ApiClient.class);
        return resources;
    }
}
