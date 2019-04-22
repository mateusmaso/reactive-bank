package com.mateusmaso.reactive.bank;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.ObjectMapper;
import kong.unirest.Unirest;

public class TransfersApiTest {
  private WebServer webServer;
  private final String BASE_URL = "http://localhost:8080";

  @Before
  public void setUp() {
    this.webServer = new WebServer();
    this.webServer.start(8080);

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

  @Test
	public void itShouldCreateAccount() {
    HttpResponse<JsonNode> response = createAccount("USD");
    assertEquals(response.getStatus(), 201);
  }
  
  private HttpResponse<JsonNode> createAccount(String currency) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("currency", currency);

    return Unirest.post(BASE_URL + "/accounts")
      .header("accept", "application/json")
      .body(payload)
      .asJson();
  }
}
