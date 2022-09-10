package de.mephisto.vpin.http.resources;

import de.mephisto.vpin.GameInfo;
import de.mephisto.vpin.VPinService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/service")
@Produces(MediaType.APPLICATION_JSON)
public class ServiceRestResource {
  private final static Logger LOG = LoggerFactory.getLogger(ServiceRestResource.class);

  @GET
  @Path("/list")
  public List<GameInfo> listGames() {
    VPinService service = VPinService.create();
    return service.getGameInfos();
  }
}
