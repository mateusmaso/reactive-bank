package com.mateusmaso.reactive.bank;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.prometheus.client.CollectorRegistry;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;
import money.MonetaryAmount;
import money.MonetaryException;
import money.exceptions.AccountNotFoundException;
import money.exceptions.InsufficientFundsException;

public class TransfersApiTest {
  private WebServer webServer;
  private Integer port;
  private String apiBaseUrl;
  private PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
  private QueuedThreadPool queuedThreadPool = new QueuedThreadPool(200, 8, 60_000);
  private StatisticsHandler statisticsHandler = new StatisticsHandler();

  @Before
  public void setUp() throws IOException {
    this.port = this.findRandomOpenPort();
    this.apiBaseUrl = "http://localhost:" + this.port;
    this.webServer = new WebServer(queuedThreadPool, statisticsHandler, registry);
    this.webServer.start(this.port);
    configUnirest();  
  }

  @After
  public void tearDown() {
    CollectorRegistry.defaultRegistry.clear();
    this.webServer.stop();
  }

  @Test
  public void itShouldCreateAccount() {
    HttpResponse<JsonNode> response = requestCreateAccount("USD");
    assertEquals(201, response.getStatus());
    assertNotNull(response.getBody().getObject().get("id"));
  }

  @Test
  public void itShouldGetBalance() {
    HttpResponse<JsonNode> createAccountResponse = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse.getStatus());
    String accountId = createAccountResponse.getBody().getObject().getString("id");

    HttpResponse<JsonNode> getBalanceResponse = requestGetBalance(accountId);
    assertEquals(200, getBalanceResponse.getStatus());
    JSONObject balance = getBalanceResponse.getBody().getObject();
    assertEquals(BigDecimal.ZERO.setScale(2), balance.getBigDecimal("amount").setScale(2));
  }

  @Test
  public void itShouldNotGetBalanceIfAccountIsNotCreated() {
    HttpResponse<JsonNode> getBalanceResponse = requestGetBalance("undefined");
    assertEquals(422, getBalanceResponse.getStatus());
    assertResponseError(getBalanceResponse, new AccountNotFoundException());
  }

  @Test
  public void itShouldLoadAccount() {
    HttpResponse<JsonNode> createAccountResponse = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse.getStatus());
    String accountId = createAccountResponse.getBody().getObject().getString("id");

    HttpResponse<JsonNode> loadResponse = requestLoad(accountId, MonetaryAmount.usd(BigDecimal.TEN));
    assertEquals(201, loadResponse.getStatus());
    JSONObject balance = loadResponse.getBody().getObject().getJSONObject("balance");
    assertEquals(BigDecimal.TEN.setScale(2), balance.getBigDecimal("amount").setScale(2));
  }

  @Test
  public void itShouldUnloadAccount() {
    HttpResponse<JsonNode> createAccountResponse = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse.getStatus());
    String accountId = createAccountResponse.getBody().getObject().getString("id");

    HttpResponse<JsonNode> loadResponse = requestLoad(accountId, MonetaryAmount.usd(BigDecimal.TEN));
    assertEquals(201, loadResponse.getStatus());

    HttpResponse<JsonNode> unloadResponse = requestUnload(accountId, MonetaryAmount.usd(BigDecimal.TEN));
    assertEquals(201, unloadResponse.getStatus());
    JSONObject balance = unloadResponse.getBody().getObject().getJSONObject("balance");
    assertEquals(BigDecimal.ZERO.setScale(2), balance.getBigDecimal("amount").setScale(2));
  }

  @Test
  public void itShouldTransferBetweenAccounts() {
    HttpResponse<JsonNode> createAccountResponse1 = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse1.getStatus());
    String fromAccountId = createAccountResponse1.getBody().getObject().getString("id");

    HttpResponse<JsonNode> createAccountResponse2 = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse2.getStatus());
    String toAccountId = createAccountResponse2.getBody().getObject().getString("id");

    HttpResponse<JsonNode> loadResponse = requestLoad(fromAccountId, MonetaryAmount.usd(BigDecimal.TEN));
    assertEquals(201, loadResponse.getStatus());
    JSONObject balance = loadResponse.getBody().getObject().getJSONObject("balance");
    assertEquals(BigDecimal.TEN.setScale(2), balance.getBigDecimal("amount").setScale(2));

    HttpResponse<JsonNode> transferResponse = requestTransfer(fromAccountId, toAccountId, MonetaryAmount.usd(BigDecimal.TEN));
    assertEquals(201, transferResponse.getStatus());

    HttpResponse<JsonNode> getBalanceResponse1 = requestGetBalance(fromAccountId);
    assertEquals(200, getBalanceResponse1.getStatus());
    JSONObject balance1 = getBalanceResponse1.getBody().getObject();
    assertEquals(BigDecimal.ZERO.setScale(2), balance1.getBigDecimal("amount").setScale(2));

    HttpResponse<JsonNode> getBalanceResponse2 = requestGetBalance(toAccountId);
    assertEquals(200, getBalanceResponse2.getStatus());
    JSONObject balance2 = getBalanceResponse2.getBody().getObject();
    assertEquals(BigDecimal.TEN.setScale(2), balance2.getBigDecimal("amount").setScale(2));
  }

  @Test
  public void itShouldNotTransferBetweenAccountsIfHasInsufficientFunds() {
    HttpResponse<JsonNode> createAccountResponse1 = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse1.getStatus());
    String fromAccountId = createAccountResponse1.getBody().getObject().getString("id");

    HttpResponse<JsonNode> createAccountResponse2 = requestCreateAccount("USD");
    assertEquals(201, createAccountResponse2.getStatus());
    String toAccountId = createAccountResponse2.getBody().getObject().getString("id");

    HttpResponse<JsonNode> loadResponse = requestLoad(fromAccountId, MonetaryAmount.usd(BigDecimal.TEN));
    assertEquals(201, loadResponse.getStatus());
    JSONObject balance = loadResponse.getBody().getObject().getJSONObject("balance");
    assertEquals(BigDecimal.TEN.setScale(2), balance.getBigDecimal("amount").setScale(2));

    HttpResponse<JsonNode> transferResponse = requestTransfer(fromAccountId, toAccountId, MonetaryAmount.usd(BigDecimal.TEN.pow(2)));
    assertResponseError(transferResponse, new InsufficientFundsException());

    HttpResponse<JsonNode> getBalanceResponse1 = requestGetBalance(fromAccountId);
    assertEquals(200, getBalanceResponse1.getStatus());
    JSONObject balance1 = getBalanceResponse1.getBody().getObject();
    assertEquals(BigDecimal.TEN.setScale(2), balance1.getBigDecimal("amount").setScale(2));

    HttpResponse<JsonNode> getBalanceResponse2 = requestGetBalance(toAccountId);
    assertEquals(200, getBalanceResponse2.getStatus());
    JSONObject balance2 = getBalanceResponse2.getBody().getObject();
    assertEquals(BigDecimal.ZERO.setScale(2), balance2.getBigDecimal("amount").setScale(2));
  }
  
  private HttpResponse<JsonNode> requestCreateAccount(String currency) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("currency", currency);

    return Unirest.post(this.apiBaseUrl + "/accounts")
      .header("accept", "application/json")
      .body(payload)
      .asJson();
  }

  private HttpResponse<JsonNode> requestGetBalance(String accountId) {
    return Unirest.get(this.apiBaseUrl + "/accounts/" + accountId + "/balance")
      .header("accept", "application/json")
      .asJson();
  }

  private HttpResponse<JsonNode> requestLoad(String accountId, MonetaryAmount amount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("accountId", accountId);
    payload.put("amount", amount.getAmount());
    payload.put("currency", amount.getCurrency());

    return Unirest.post(this.apiBaseUrl + "/load")
      .header("accept", "application/json")
      .body(payload)
      .asJson();
  }

  private HttpResponse<JsonNode> requestUnload(String accountId, MonetaryAmount amount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("accountId", accountId);
    payload.put("amount", amount.getAmount());
    payload.put("currency", amount.getCurrency());

    return Unirest.post(this.apiBaseUrl + "/unload")
      .header("accept", "application/json")
      .body(payload)
      .asJson();
  }

  private HttpResponse<JsonNode> requestTransfer(String fromAccountId, String toAccountId, MonetaryAmount amount) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("fromAccountId", fromAccountId);
    payload.put("toAccountId", toAccountId);
    payload.put("amount", amount.getAmount());
    payload.put("currency", amount.getCurrency());

    return Unirest.post(this.apiBaseUrl + "/transfers")
      .header("accept", "application/json")
      .body(payload)
      .asJson();
  }

  private void assertResponseError(HttpResponse<JsonNode> response, MonetaryException exception) {
    JSONObject error = response.getBody().getObject();
    assertEquals(exception.getErrorCode(), error.getString("code"));
  }

  private void configUnirest() {
    Unirest.config().setObjectMapper(new ObjectMapper() {
      private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper = 
        new com.fasterxml.jackson.databind.ObjectMapper();
  
      public <T> T readValue(String value, Class<T> valueType) {
        try {
          return jacksonObjectMapper.readValue(value, valueType);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
  
      public String writeValue(Object value) {
        try {
          return jacksonObjectMapper.writeValueAsString(value);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private Integer findRandomOpenPort() throws IOException {
    try (
      ServerSocket socket = new ServerSocket(0);
    ) {
      return socket.getLocalPort();
    }
  }
}
