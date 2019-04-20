package revolut.backend.test;

import money.MonetaryAmount;
import money.account.models.Account;
import money.account.repositories.AccountRepository;
import money.account.repositories.AccountRepositoryImpl;
import money.account.services.AccountService;
import money.account.services.AccountServiceImpl;
import money.exceptions.AccountNotFoundException;
import money.exceptions.CurrencyNotAllowedException;
import money.exceptions.InsufficientBalanceException;
import money.exceptions.InvalidTransactionException;
import money.store.AccountStore;
import money.store.TransactionEntriesByAccountStore;
import money.transaction.models.Transaction;
import money.transaction.repositories.TransactionRepository;
import money.transaction.repositories.TransactionRepositoryImpl;

import static spark.Spark.*;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebServer {
  private static AccountStore accountStore = new AccountStore();
  private static AccountRepository accountRepository = new AccountRepositoryImpl(accountStore);
  private static TransactionEntriesByAccountStore transactionEntriesByAccountStore = new TransactionEntriesByAccountStore();
  private static TransactionRepository transactionRepository = new TransactionRepositoryImpl(transactionEntriesByAccountStore);
  private static AccountService accountService = new AccountServiceImpl(accountRepository, transactionRepository);
  private static Logger logger = LoggerFactory.getLogger(WebServer.class);

	public void start(Integer port) {
    logger.info("Web server started on port " + port);

    port(port);
    post("transfers", (request, response) -> {
      Map<String, Object> payload = jsonToData(request.body());
      logger.info(dataToJson(payload));

      // validate amount (bigdecimal 2)
      // accountService.transfer(String fromAccountId, String toAccountId, MonetaryAmount amount);

      response.status(201);
      response.type("application/json");
      return dataToJson(new HashMap<>());
    });

    exception(AccountNotFoundException.class, (exception, request, response) -> {
      response.status(422);
      response.type("application/json");
      response.body(dataToJson(buildError("ACCOUNT_NOT_FOUND", exception.getMessage())));
    });

    exception(CurrencyNotAllowedException.class, (exception, request, response) -> {
      response.status(422);
      response.type("application/json");
      response.body(dataToJson(buildError("CURRENCY_NOT_ALLOWED", exception.getMessage())));
    });

    exception(InsufficientBalanceException.class, (exception, request, response) -> {
      response.status(422);
      response.type("application/json");
      response.body(dataToJson(buildError("INSUFFICIENT_BALANCE", exception.getMessage())));
    });

    exception(InvalidTransactionException.class, (exception, request, response) -> {
      response.status(422);
      response.type("application/json");
      response.body(dataToJson(buildError("TRANSFER_DENIED", exception.getMessage())));
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
