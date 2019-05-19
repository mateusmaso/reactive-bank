package money.account;

import java.util.Currency;

public class Account {
  public static String OPERATIONAL_ID = "42bcc9e5-eb15-4c8b-9b43-5d4334c3dcce";

  private final String id;
  private final Currency currency;

  public Account(String id, Currency currency) {
    this.id = id;
    this.currency = currency;
  }

  public String getId() {
    return id;
  }

  public Currency getCurrency() {
    return this.currency;
  }

  public boolean isOperational() {
    return this.id.equals(OPERATIONAL_ID);
  }
  
  public boolean equals(Account other) {
    return this.getId().equals(other.getId());
  }
}