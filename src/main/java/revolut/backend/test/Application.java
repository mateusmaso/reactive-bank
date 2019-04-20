package revolut.backend.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {
  private static Logger logger = LoggerFactory.getLogger(Application.class);
  private static WebServer webServer = new WebServer();

  public static void main(String[] args) {
    logger.info("Application running");
    webServer.start(8080);
  }
}
