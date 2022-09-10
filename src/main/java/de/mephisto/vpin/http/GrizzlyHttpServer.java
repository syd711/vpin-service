package de.mephisto.vpin.http;

import com.google.common.base.Joiner;
import com.sun.jersey.api.container.grizzly2.GrizzlyWebContainerFactory;
import de.mephisto.vpin.util.SystemInfo;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.NetworkListener;
import org.glassfish.grizzly.http.server.StaticHttpHandler;
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

  private int port;
  private String host;
  private File resourceDirectory;

  private org.glassfish.grizzly.http.server.HttpServer httpServer;

  public GrizzlyHttpServer(String host, int port) {
    this.host = host;
    this.port = port;
    this.resourceDirectory = new File(SystemInfo.RESOURCES);
  }

  /**
   * Starts the http server for the configured port.
   */
  public HttpServer start() {
    try {
      final Map<String, String> initParams = new HashMap<>();

      List<String> packageNames = new ArrayList<>(Arrays.asList("de.mephisto.vpin.http.resources"));
      packageNames.add("com.sun.jersey");
      initParams.put("com.sun.jersey.config.property.packages", Joiner.on(",").join(packageNames));
      initParams.put("com.sun.jersey.api.json.POJOMappingFeature", "true");

      URI url = UriBuilder.fromUri("http://" + host + "/").port(port).build();
      LOG.info("Starting server on: " + url.toString() + ", using resource directory " + resourceDirectory.getAbsolutePath());

      httpServer = GrizzlyWebContainerFactory.create(url, initParams);
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
    return "HttpServer running on port " + port;
  }
}
