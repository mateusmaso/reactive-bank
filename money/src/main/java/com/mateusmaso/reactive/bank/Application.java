package com.mateusmaso.reactive.bank;

import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;

public class Application {
  private final static PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  private static Logger log = LoggerFactory.getLogger(Application.class);
  private static QueuedThreadPool queuedThreadPool = new QueuedThreadPool(200, 8, 60_000);
  private static StatisticsHandler statisticsHandler = new StatisticsHandler();
  private static WebServer webServer = new WebServer(queuedThreadPool, statisticsHandler, registry);

  public static void main(String[] args) {
    log.info("Application running");

    Metrics.globalRegistry.add(registry);

    webServer.start(8080);
  }
}
