package de.mephisto.vpin.http;

import com.google.common.base.Joiner;
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import de.mephisto.vpin.VPinService;
import de.mephisto.vpin.http.resources.ServiceRestResource;
import de.mephisto.vpin.util.SystemInfo;
import org.glassfish.grizzly.http.server.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.UriBuilder;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;

/**
 * Manages the HTTP server.
 */
public class GrizzlyHttpServer {
  private final static Logger LOG = LoggerFactory.getLogger(GrizzlyHttpServer.class);
  public static final String RESOURCES = "/resources"; //don't forget the leading slash
  public static final int PORT = 8088;

  private File resourceDirectory;

  private org.glassfish.grizzly.http.server.HttpServer httpServer;

  public GrizzlyHttpServer() {
    this.resourceDirectory = new File(SystemInfo.RESOURCES);
    this.start();
  }

  /**
   * Starts the http server for the configured port.
   */
  public HttpServer start() {
    try {
      URI url = UriBuilder.fromUri("http://" + "localhost" + "/").port(PORT).build();
      LOG.info("Starting server on: " + url.toString() + ", using resource directory " + resourceDirectory.getAbsolutePath());

      //TODO maybe someday you and I can play together
      httpServer = GrizzlyServerFactory.createHttpServer(url, new HttpHandler() {
        @Override
        public void service(Request request, Response response) throws Exception {
          ServiceRestResource.serve(request);
        }
      });
      httpServer.getServerConfiguration().addHttpHandler(new StaticHttpHandler(resourceDirectory.getAbsolutePath()), RESOURCES);
      LOG.info("Http server resources available under " + url.toString() + "resources");

      for (NetworkListener l : httpServer.getListeners()) {
        l.getFileCache().setEnabled(false);
      }
      httpServer.start();
      LOG.info("Started " + this.toString());
      return httpServer;
    } catch (IOException e) {
      LOG.error("Failed to start HTTP server: " + e.getMessage(), e);
    }
    return null;
  }

  public void stop() {
    httpServer.stop();
    LOG.info("Stopped " + this);
  }

  @Override
  public String toString() {
    return "HttpServer running on port " + PORT;
  }
}
