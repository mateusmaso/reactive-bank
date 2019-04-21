package money.exceptions;

import money.MonetaryException;

public final class InvalidTransactionException extends MonetaryException {
  private static final long serialVersionUID = 3460980750930011833L;

  public InvalidTransactionException() {
    super("INVALID_TRANSACTION", "Invalid transaction");
  }
}