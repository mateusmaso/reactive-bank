package money.exceptions;

import money.MonetaryException;

public final class CurrencyNotAllowedException extends MonetaryException {
  private static final long serialVersionUID = 3415794103767293458L;

  public CurrencyNotAllowedException() {
    super("CURRENCY_NOT_ALLOWED", "Currency not allowed");
  }
}