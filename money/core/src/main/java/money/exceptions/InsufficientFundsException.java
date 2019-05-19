package money.exceptions;

import money.MonetaryException;

public final class InsufficientFundsException extends MonetaryException {
  private static final long serialVersionUID = 7272167940711964920L;

  public InsufficientFundsException() {
    super("INSUFFICIENT_FUNDS", "Insufficient funds");
  }
}