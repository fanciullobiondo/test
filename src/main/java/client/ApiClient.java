package client;

import data.EmbeddedData;
import data.EmbeddedData.League;
import data.LeagueTable;
import database.bean.Match;
import engine.Engine;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.naming.NamingException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import tomcat.TomcatManager;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class ApiClient {

    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest servletRequest;

    private Map<Integer, LeagueTable> leagueCache  = new HashMap<>();

    private LeagueTable getLeagueTable(int idround) throws SQLException {
        LeagueTable get = leagueCache.get(idround);
        if (get == null) {
            get = getManager().calculateLeagueTable(idround, League.CAMPIONATO);
            leagueCache.put(idround, get);
        }
        return get;
        
    }

    private Engine getManager() {
        return (Engine) servletRequest.getServletContext().getAttribute(TomcatManager.ATTRIBUTE_ENGINE);
    }

    @GET
    @Path("/test")
    public Map<String, Object> test() throws SQLException, NamingException {
        Map<String, Object> a = new HashMap<>();
        a.put("ciao", "fu");
        return a;
    }

    @GET
    @Path("/actual")
    public Map<String, Object> actual() throws SQLException, NamingException {
        return simpleResult(getManager().getActualRound());
    }

    @GET
    @Path("/round")
    public Map<String, Object> getRoundInfo(@QueryParam("idround") int idround) throws SQLException, NamingException {
        List<RoundMatch> roundMatches = getManager().getRoundMatches(idround);
        Map<String, Object> simpleResult = simpleResult(roundMatches);
        simpleResult.put("played", getManager().isRoundPlayed(idround));
        simpleResult.put("table", getLeagueTable(idround));
        return simpleResult;
    }

    @POST
    @Path("/postteamchooser")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Map<String, Object> postteamchooser(Map<String, Object> content) throws SQLException, NamingException {
        System.out.println("passed" + content);

        List<String> selected = (List<String>) content.get("selected");
        System.out.println(content.getClass());
        System.out.println(selected);
        System.out.println(content.get("selected").getClass());

        if (selected.size() > 8 || selected.size() < 1) {
            return error("i giocatori devono essere da 1 a 8");
        }
        Set<String> set = new HashSet<>(selected);
        if (selected.size() != set.size()) {
            return error("Una squadra è stata scelta piu volte");
        }

        getManager().insertTeams(selected);
        int nextRound = getManager().newSeason();
        return simpleResult(nextRound);
    }

    @POST
    @Path("/postround")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Map<String, Object> postround(Map<String, Object> content) throws SQLException, NamingException {
        System.out.println("passed postround " + content);

        List<?> matches = (List<?>) content.get("matches");
        Integer idround = (Integer) content.get("idround");

        try {
            getManager().playRound(idround, matches.stream().map(rm -> new Match(RoundMatch.wrap((Map)rm))).collect(Collectors.toList()));
        } catch (BadRequestException e) {
            return error(e.getMessage());
        }

        return simpleResult();
    }

    @GET
    @Path("/teamchooser")
    public Map<String, Object> teamchooser() throws SQLException, NamingException {
        return simpleResult(EmbeddedData.ALL_TEAMS.keySet());
    }

    @POST
    @Path("/newgame")
    public Map<String, Object> newgame() throws SQLException, NamingException {
        Map<String, Object> a = new HashMap<>();
        return a;
    }

    private static Map<String, Object> simpleResult(Object obj) {
        Map<String, Object> res = new HashMap<>();
        res.put("simple", obj);
        res.put("ok", true);
        return res;
    }

    private static Map<String, Object> simpleResult() {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", true);
        return res;
    }

    private static Map<String, Object> error(String obj) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("error", obj);
        return res;
    }

    public static class RoundMatch {

        public static RoundMatch wrap(Map<?,?> map) {
            RoundMatch rm = new RoundMatch();
            rm.setIdmatch((Integer)map.get("idmatch"));
            rm.setHome(TeamResult.wrap((Map)map.get("home")));
            rm.setAway(TeamResult.wrap((Map)map.get("away")));
            return rm;
        }

        private int idmatch;
        private TeamResult home;
        private TeamResult away;
        private boolean editable;

        public boolean isEditable() {
            return editable;
        }

        public void setEditable(boolean editable) {
            this.editable = editable;
        }

        public void setIdmatch(int idmatch) {
            this.idmatch = idmatch;
        }

        public int getIdmatch() {
            return idmatch;
        }

        public TeamResult getHome() {
            return home;
        }

        public void setHome(TeamResult home) {
            this.home = home;
        }

        public TeamResult getAway() {
            return away;
        }

        public void setAway(TeamResult away) {
            this.away = away;
        }

        public static class TeamResult {
            public static TeamResult wrap(Map<?,?> map) {
                return new TeamResult((Integer)map.get("goal"));
            }

            public TeamResult(int goal) {
                this.goal = goal;
            }

            
            public TeamResult(int id, String name, int goal, boolean ofUser) {
                this.id = id;
                this.name = name;
                this.goal = goal;
                this.ofUser = ofUser;
            }

            private int id;
            private String name;
            private int goal;
            private boolean ofUser;
            private int money;

            public int getMoney() {
                return money;
            }

            public void setMoney(int money) {
                this.money = money;
            }

            
            public boolean isOfUser() {
                return ofUser;
            }

            public void setOfUser(boolean ofUser) {
                this.ofUser = ofUser;
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public int getGoal() {
                return goal;
            }

            public void setGoal(int goal) {
                this.goal = goal;
            }

        }

    }

}
