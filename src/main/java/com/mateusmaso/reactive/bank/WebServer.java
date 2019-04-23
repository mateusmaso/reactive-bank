package com.mateusmaso.reactive.bank;

import money.MonetaryAmount;
import money.MonetaryException;
import money.account.Account;
import money.account.AccountService;
import money.exceptions.AccountNotFoundException;
import money.exceptions.CurrencyNotAllowedException;
import money.exceptions.InsufficientBalanceException;
import money.exceptions.InvalidTransactionException;
import money.inmemory.*;
import money.transaction.Transaction;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;

public class WebServer {
  private AccountService accountService;
  private Logger logger;
  private Javalin app;
  private boolean started;

  public WebServer() {
    InMemoryStore store = new InMemoryStore();
    InMemoryAccountOperation accountOperation = new InMemoryAccountOperation();
    InMemoryAccountRepository accountRepository = new InMemoryAccountRepository(store);
    InMemoryTransactionRepository transactionRepository = new InMemoryTransactionRepository(store);

    this.accountService = new AccountService(accountOperation, accountRepository, transactionRepository);
    this.logger = LoggerFactory.getLogger(WebServer.class);
    this.app = Javalin.create();
    this.started = false;
  }

  public void start(Integer port) {    
    if (!this.started) {
      logger.info("Web server started on port " + port);
      this.app.start(port);
      this.started = true;
      handleTransfers();
      handleAccounts();
      handleLoad();
      handleUnload();
      handleExceptions();
    }
  }

  private static String dataToJson(Object data) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      StringWriter sw = new StringWriter();
      mapper.writeValue(sw, data);
      return sw.toString();
    } catch (IOException e){
      throw new RuntimeException("Something went wrong while serializing JSON");
    }
  }

  private static Map<String, Object> jsonToData(String json) {         
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
    } catch (IOException e) {
      throw new RuntimeException("Something went wrong while deserializing JSON");
    }
  }

  private void handleTransfers() {
    this.app.post("/transfers", ctx -> {
      Map<String, Object> payload = jsonToData(ctx.body());
      this.logger.info(dataToJson(payload));

      String fromAccountId = (String) payload.get("fromAccountId");
      String toAccountId = (String) payload.get("toAccountId");
      MonetaryAmount amount = new MonetaryAmount(
        Currency.getInstance((String) payload.get("currency")),
        parseAmount(payload.get("amount"))
      );

      ctx.status(201);
      ctx.contentType("application/json");
      ctx.result(
        accountService
          .transfer(fromAccountId, toAccountId, amount)
          .thenCombine(accountService.getBalance(fromAccountId), (transfer, balance) -> {
            return dataToJson(buildResponseMap(transfer, balance));
          })
      );
    });
  }

  private void handleAccounts() {
    this.app.post("/accounts", ctx -> {
      Map<String, Object> payload = jsonToData(ctx.body());
      this.logger.info(dataToJson(payload));

      String currency = (String) payload.get("currency");

      ctx.status(201);
      ctx.contentType("application/json");
      ctx.result(
        accountService
          .createAccount(Currency.getInstance(currency))
          .thenApply((account) -> dataToJson(buildResponseMap(account)))
      );
    });

    this.app.get("/accounts/:id/balance", ctx -> {
      String accountId = ctx.pathParam("id");

      ctx.status(200);
      ctx.contentType("application/json");
      ctx.result(
        accountService
          .getBalance(accountId)
          .thenApply((balance) -> dataToJson(balance))
      );
    });
  }

  private void handleLoad() {
    this.app.post("/load", ctx -> {
      Map<String, Object> payload = jsonToData(ctx.body());
      this.logger.info(dataToJson(payload));

      String accountId = (String) payload.get("accountId");
      MonetaryAmount amount = new MonetaryAmount(
        Currency.getInstance((String) payload.get("currency")),
        parseAmount(payload.get("amount"))
      );

      ctx.status(201);
      ctx.contentType("application/json");
      ctx.result(
        accountService
          .load(accountId, amount)
          .thenCombine(accountService.getBalance(accountId), (load, balance) -> {
            return dataToJson(buildResponseMap(load, balance));
          })
      );
    });
  }

  private void handleUnload() {
    this.app.post("/unload", ctx -> {
      Map<String, Object> payload = jsonToData(ctx.body());
      this.logger.info(dataToJson(payload));

      String accountId = (String) payload.get("accountId");
      MonetaryAmount amount = new MonetaryAmount(
        Currency.getInstance((String) payload.get("currency")),
        parseAmount(payload.get("amount"))
      );

      ctx.status(201);
      ctx.contentType("application/json");
      ctx.result(
        accountService
          .unload(accountId, amount)
          .thenCombine(accountService.getBalance(accountId), (unload, balance) -> {
            return dataToJson(buildResponseMap(unload, balance));
          })
      );
    });
  }

  private void handleExceptions() {
    this.app.exception(CompletionException.class, (exception, ctx) -> {
      CompletionException completionException = (CompletionException) exception;
      
      boolean isMonetaryException = Arrays.asList(
        AccountNotFoundException.class,
        CurrencyNotAllowedException.class,
        InsufficientBalanceException.class,
        InvalidTransactionException.class
      ).contains(completionException.getCause().getClass());

      if (isMonetaryException) {
        MonetaryException monetaryException = (MonetaryException) completionException.getCause();
        ctx.status(422);
        ctx.contentType("application/json");
        ctx.result(dataToJson(buildResponseMap(monetaryException)));  
      }
    });
  }

  private BigDecimal parseAmount(Object amount) {
    if (amount instanceof Integer) {
      return new BigDecimal((Integer) amount);
    } else if (amount instanceof Double) {
      return BigDecimal.valueOf((Double) amount);
    } else if (amount instanceof Float) {
      return BigDecimal.valueOf((Float) amount);
    } else {
      return null;
    }
  }

  private HashMap<String, Object> buildResponseMap(MonetaryException exception){
    HashMap<String, Object> result = new HashMap<>();
    result.put("code", exception.getErrorCode());
    result.put("message", exception.getMessage());
    return result;
  }

  private HashMap<String, Object> buildResponseMap(Transaction transaction, MonetaryAmount balance){
    HashMap<String, Object> result = new HashMap<>();
    result.put("transactionId", transaction.getId());
    result.put("balance", balance);
    return result;
  }

  private HashMap<String, Object> buildResponseMap(Account account){
    HashMap<String, Object> result = new HashMap<>();
    result.put("id", account.getId());
    return result;
  }
}
