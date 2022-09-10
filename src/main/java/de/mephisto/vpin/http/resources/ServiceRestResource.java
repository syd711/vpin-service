package de.mephisto.vpin.http.resources;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.http.GrizzlyHttpServer;
import org.glassfish.grizzly.http.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRestResource {
  private final static Logger LOG = LoggerFactory.getLogger(ServiceRestResource.class);

  private final static String CURL_COMMAND_TABLE_START = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + GrizzlyHttpServer.PORT + "/service/gameLaunch";
  private final static String CURL_COMMAND_TABLE_EXIT = "curl -X POST --data-urlencode \"table=[GAMEFULLNAME]\" http://localhost:" + GrizzlyHttpServer.PORT + "/service/gameExit";

  public static void serve(Request request) {
    String path = request.getRequestURL().toString();
    String table = request.getParameter("table");
    if(path.contains("gameLaunch")) {
      new ServiceRestResource().gameLaunched(table);
    }
    else if(path.contains("gameExit")) {
      new ServiceRestResource().gamedExited(table);
    }
  }

  @GET
  @Path("/list")
  public List<GameInfo> listGames() {
    VPinService service = VPinService.create();
    return service.getGameInfos();
  }

  @POST
  @Path("/gameLaunch")
  public boolean gameLaunched(@FormParam("table") String table) {
    LOG.info("Notified game launch '" + table + "'");
    return true;
  }

  @POST
  @Path("/gameExit")
  public boolean gamedExited(@FormParam("table") String table) {
    LOG.info("Notified game exit '" + table + "'");
    return true;
  }
}
