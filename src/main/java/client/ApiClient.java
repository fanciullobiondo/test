package client;



import database.DatabaseManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import javax.naming.NamingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class ApiClient {

    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest servletRequest;

    @GET
    @Path("/test")
    public Map<String,Object> test() throws SQLException, NamingException {
        Map<String, Object> a = new HashMap<>();
        a.put("ciao", "fu");
        return a;
    }
    
    @POST
    @Path("/insert")
    public void insert(@FormParam("name") String name) throws SQLException, NamingException {
        System.out.println("passed" + name);
    }
    @GET
    @Path("/get")
    public Map<String, Object> get() throws SQLException, NamingException {
        Map<String, Object> a = new HashMap<>();
        return a;
    }

}
