package de.mephisto.vpin.http;

import de.mephisto.vpin.dof.DOFManager;
import de.mephisto.vpin.popper.PopperManager;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the HTTP server.
 */
public class HttpServer {
  private final static Logger LOG = LoggerFactory.getLogger(HttpServer.class);
  public static final int PORT = 8088;

  private Server server;
  private final PopperManager popperManager;

  public HttpServer(PopperManager popperManager) {
    this.popperManager = popperManager;
    this.start();
  }

  public void start() {
    try {
      server = new Server();
      ServerConnector connector = new ServerConnector(server);
      connector.setPort(8088);
      server.setConnectors(new Connector[]{connector});
      ServletHandler handler = new ServletHandler();
      server.setHandler(handler);
      handler.addServletWithMapping(new ServletHolder(new AsyncServlet(this.popperManager)), "/service/*");
      server.start();
    } catch (Exception e) {
      LOG.error("Failed to start HTTP server: " + e.getMessage(), e);
    }
  }

  public void stop() {
    try {
      server.stop();
      LOG.info("Stopped " + this);
    } catch (Exception e) {
      LOG.error("Failed to stop server: " + e.getMessage(), e);
    }
  }

  @Override
  public String toString() {
    return "HttpServer running on port " + PORT;
  }
}
