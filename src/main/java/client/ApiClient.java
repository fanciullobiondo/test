package client;

import data.BillboardTable;
import data.EmbeddedData;
import data.EmbeddedData.League;
import static data.EmbeddedData.League.CAMPIONATO;
import data.LeagueTable;
import database.DatabaseManager;
import database.bean.Match;
import engine.Engine;
import java.io.IOException;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
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
import org.apache.log4j.Logger;
import static tomcat.Initializer.ATTRIBUTE_ENGINE;
import utils.FileUtils;

@Path("api")
@Produces(MediaType.APPLICATION_JSON)
public class ApiClient {

    @Context
    private UriInfo context;

    @Context
    private HttpServletRequest servletRequest;

    private Map<Integer, LeagueTable> leagueCache = new HashMap<>();

    private Map<Integer, BillboardTable> coppaBillBoardCache = new HashMap<>();
    private Map<Integer, BillboardTable> euroBillBoardCache = new HashMap<>();
    private Map<Integer, BillboardTable> championsBillBoardCache = new HashMap<>();

    private final static Logger logger = Logger.getLogger(ApiClient.class);

    private LeagueTable getLeagueTable(int idround) throws SQLException {
        LeagueTable get = leagueCache.get(idround);
        if (get == null) {
            get = getManager().calculateLeagueTable(idround);
            leagueCache.put(idround, get);
        }
        return get;

    }

    private BillboardTable getBillboardTable(int idround, int idleague, int subleague) throws SQLException {
        Map<Integer, BillboardTable> cache = coppaBillBoardCache;
        if (idleague == League.EUROPA) {
            if (subleague == League.SUB_EUROPALEAGUE) {
                cache = euroBillBoardCache;
            } else if (subleague == League.SUB_CHAMPIONSLEAGUE) {
                cache = championsBillBoardCache;
            }
        }
        BillboardTable get = cache.get(idround);
        if (get == null) {
            get = getManager().calculateBillboard(idround, idleague, subleague);
            cache.put(idround, get);
        }
        return get;

    }

    private Engine getManager() {
        return (Engine) servletRequest.getServletContext().getAttribute(ATTRIBUTE_ENGINE);
    }

    @GET
    @Path("/test")
    public Map<String, Object> test() throws SQLException, NamingException {
        Map<String, Object> a = new HashMap<>();
        a.put("ciao", "fu");
        return a;
    }

    @GET
    @Path("/requestreset")
    public Map<String, Object> requestreset() throws SQLException, NamingException {
        try {
            servletRequest.getServletContext().setAttribute(ATTRIBUTE_ENGINE, new Engine(new DatabaseManager(DatabaseManager.getDefaultDatabasePath())));
            return simpleResult();
        } catch (Exception e) {
            return internalError(e);
        }

    }

    @GET
    @Path("/actual")
    public Map<String, Object> actual() {
        try {
            try {
                int idround = getManager().getActualRound();
                if (idround <= 0) {
                    return simpleResult(FileUtils.readExistingDatabases(getManager().getDatabasesPath()));
                }
                return simpleResult(getManager().getRoundInfo(idround));
            } catch (SQLException e) {
                return simpleResult(FileUtils.readExistingDatabases(getManager().getDatabasesPath()));
            }
        } catch (IOException e) {
            return internalError(e);
        }
    }

    @GET
    @Path("/round")
    public Map<String, Object> getRoundInfo(@QueryParam("idround") int idround) {
        try {

            List<RoundMatch> roundMatches = getManager().getRoundMatches(idround);
            if (roundMatches == null) {
                // round non esiste
                return error("round non esiste");
            }
            Collections.sort(roundMatches, (o1, o2) -> {
                return o1.isCl() ? 1 : -1;
            });

            Map<String, Object> simpleResult = simpleResult(roundMatches);
            simpleResult.put("played", getManager().isRoundPlayed(idround));
            simpleResult.put("table", getLeagueTable(idround));
            simpleResult.put("billboardCh", getBillboardTable(idround, EmbeddedData.League.EUROPA, EmbeddedData.League.SUB_CHAMPIONSLEAGUE));
            simpleResult.put("billboardEl", getBillboardTable(idround, EmbeddedData.League.EUROPA, EmbeddedData.League.SUB_EUROPALEAGUE));
            simpleResult.put("billboardCo", getBillboardTable(idround, EmbeddedData.League.COPPA, EmbeddedData.League.SUB_NONE));
            simpleResult.put("round", getManager().getRoundInfo(idround));
            return simpleResult;
        } catch (SQLException e) {
            return internalError(e);
        }
    }

    @POST
    @Path("/postdbchooser")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Map<String, Object> postdbchooser(Map<String, Object> content) throws SQLException, NamingException {
        try {

            String selected = (String) content.get("selected");
            if (selected.equals("0")) {
                SimpleDateFormat smf = new SimpleDateFormat("dd-MMMM__HH-mm-ss", Locale.ITALY);
                selected = "Gioco__" + smf.format(new Timestamp(System.currentTimeMillis())) + ".db";
            }
            return simpleResult(getManager().startDatabase(selected));
        } catch (SQLException e) {
            return internalError(e);
        }
    }

    @POST
    @Path("/postteamchooser")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Map<String, Object> postteamchooser(Map<String, Object> content) throws SQLException, NamingException {
        try {

            List<String> selected = (List<String>) content.get("selected");

            if (selected.size() > 8 || selected.size() < 1) {
                return error("i giocatori devono essere da 1 a 8");
            }
            Set<String> set = new HashSet<>(selected);
            if (selected.size() != set.size()) {
                return error("Una squadra è stata scelta piu volte");
            }

            getManager().insertTeams(selected);
            int nextRound = getManager().getActualRound();
            return simpleResult(nextRound);
        } catch (Exception e) {
            return internalError(e);
        }
    }

    @POST
    @Path("/postround")
    @Consumes(MediaType.APPLICATION_JSON)
    @SuppressWarnings("unchecked")
    public Map<String, Object> postround(Map<String, Object> content) throws SQLException, NamingException {
        try {

            List<?> matches = (List<?>) content.get("matches");
            Integer idround = (Integer) content.get("idround");

            try {
                getManager().playRound(idround, matches.stream().map(rm -> new Match(RoundMatch.wrap((Map) rm))).collect(Collectors.toList()));
            } catch (BadRequestException e) {
                return error(e.getMessage());
            }

            return simpleResult();
        } catch (Exception e) {
            return internalError(e);
        }
    }

    @GET
    @Path("/teamchooser")
    public Map<String, Object> teamchooser() throws SQLException, NamingException {
        try {
            return simpleResult(EmbeddedData.ALL_TEAMS.values()
                .stream()
                .filter(t -> t.getLeague().getId() == CAMPIONATO)
                .map(t -> t.getName())
                .collect(Collectors.toList()));
        } catch (Exception e) {
            return internalError(e);
        }
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

    private static Map<String, Object> internalError(Object e) {
        logger.info("ERROR:" + e.toString());
        logger.info(e);
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("error", "Internal error" + e.toString());
        return res;
    }

    private static Map<String, Object> error(String obj) {
        Map<String, Object> res = new HashMap<>();
        res.put("ok", false);
        res.put("error", obj);
        return res;
    }

    public static class RoundMatch {

        public static RoundMatch wrap(Map<?, ?> map) {
            RoundMatch rm = new RoundMatch();
            rm.setIdmatch((Integer) map.get("idmatch"));
            rm.setHome(TeamResult.wrap((Map) map.get("home")));
            rm.setAway(TeamResult.wrap((Map) map.get("away")));
            rm.setEuropean((Boolean) map.get("european"));
            rm.setCl((Boolean) map.get("cl"));
            return rm;
        }

        private int idmatch;
        private TeamResult home;
        private TeamResult away;
        private boolean editable;
        private boolean campionato;
        private boolean european;
        private boolean cl;

        public boolean isCampionato() {
            return campionato;
        }

        public void setCampionato(boolean campionato) {
            this.campionato = campionato;
        }

        public boolean isEuropean() {
            return european;
        }

        public void setEuropean(boolean european) {
            this.european = european;
        }

        public boolean isCl() {
            return cl;
        }

        public void setCl(boolean cl) {
            this.cl = cl;
        }

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

        public void setMoneyHome(int m) {
            if (home != null) {
                home.setMoney(m);
            }
        }

        public void setMoneyAway(int m) {
            if (away != null) {
                away.setMoney(m);
            }

        }

        public static class TeamResult {

            public static TeamResult wrap(Map<?, ?> map) {
                return new TeamResult((Integer) map.get("id"), (Integer) map.get("goal"));
            }

            public TeamResult(int id, int goal) {
                this.id = id;
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
