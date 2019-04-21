package revolut.backend.test;

import money.MonetaryAmount;
import money.MonetaryException;
import money.account.models.Account;
import money.account.repositories.AccountRepository;
import money.account.services.AccountService;
import money.account.services.AccountServiceImpl;
import money.exceptions.AccountNotFoundException;
import money.exceptions.CurrencyNotAllowedException;
import money.exceptions.InsufficientBalanceException;
import money.exceptions.InvalidTransactionException;
import money.transaction.models.Transaction;
import money.transaction.repositories.TransactionRepository;
import money.inmemory.*;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.javalin.Javalin;

public class WebServer {
  private AccountService accountService;
  private Logger logger;
  private Javalin app;

  public WebServer() {
    AccountStore accountStore = new AccountStore();
    InMemoryAccountRepository accountRepository = new InMemoryAccountRepository(accountStore);
    TransactionEntriesByAccountStore transactionEntriesByAccountStore = new TransactionEntriesByAccountStore();
    InMemoryTransactionRepository transactionRepository = new InMemoryTransactionRepository(transactionEntriesByAccountStore);

    this.accountService = new AccountServiceImpl(accountRepository, transactionRepository);
    this.logger = LoggerFactory.getLogger(WebServer.class);
    this.app = Javalin.create();
  }

  public void start(Integer port) {
    logger.info("Web server started on port " + port);

    this.app.start(port);
    this.app.post("/transfers", ctx -> {
      Map<String, Object> payload = jsonToData(ctx.body());
      this.logger.info(dataToJson(payload));

      String fromAccountId = (String) payload.get("fromAccountId");
      String toAccountId = (String) payload.get("toAccountId");
      MonetaryAmount amount = new MonetaryAmount(
        Currency.getInstance((String) payload.get("currency")),
        BigDecimal.valueOf((Float) payload.get("amount"))
      );

      ctx.status(201);
      ctx.contentType("application/json");
      ctx.result(
        accountService
          .transfer(fromAccountId, toAccountId, amount)
          .thenApply((transfer) -> dataToJson(transfer))
      );
    });

    Arrays.asList(
      AccountNotFoundException.class,
      CurrencyNotAllowedException.class,
      InsufficientBalanceException.class,
      InvalidTransactionException.class
    ).stream().forEach((exceptionClass) -> {
      this.app.exception(exceptionClass, (exception, ctx) -> {
        MonetaryException monetaryException = (MonetaryException) exception;

        ctx.status(422);
        ctx.contentType("application/json");
        ctx.result(dataToJson(buildError(monetaryException.getErrorCode(), monetaryException.getMessage())));
      });
    });
  }

  public static Map<String, String> buildError(String code, String message) {
    Map<String, String> errorMap = new HashMap<>();
    return errorMap;
  }

  public static String dataToJson(Object data) {
    try {
      ObjectMapper mapper = new ObjectMapper();
      mapper.enable(SerializationFeature.INDENT_OUTPUT);
      StringWriter sw = new StringWriter();
      mapper.writeValue(sw, data);
      return sw.toString();
    } catch (IOException e){
      throw new RuntimeException("Something went wrong while serializing JSON");
    }
  }

  public static Map<String, Object> jsonToData(String json) {         
    try {
      ObjectMapper mapper = new ObjectMapper();
      return mapper.readValue(json, new TypeReference<Map<String, Object>>(){});
    } catch (IOException e) {
      throw new RuntimeException("Something went wrong while deserializing JSON");
    }
  }
}
