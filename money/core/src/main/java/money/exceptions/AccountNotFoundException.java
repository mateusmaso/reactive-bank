package money.exceptions;

import money.MonetaryException;

public final class AccountNotFoundException extends MonetaryException {
  private static final long serialVersionUID = 2130166836791836428L;

  public AccountNotFoundException() {
    super("ACCOUNT_NOT_FOUND", "Account not found");
  }
}