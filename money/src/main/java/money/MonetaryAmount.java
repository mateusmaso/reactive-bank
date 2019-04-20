package money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

public class MonetaryAmount {
  private final Currency currency;
  private final BigDecimal amount;

  public MonetaryAmount(Currency currency, BigDecimal amount) {
    if (!Optional.ofNullable(currency).isPresent()) {
      throw new RuntimeException("Currency must be present.");
    }

    if (!Optional.ofNullable(amount).isPresent()) {
      throw new RuntimeException("Amount must be present.");
    }

    this.currency = currency;
    this.amount = amount;
  }

  public Currency getCurrency() {
    return this.currency;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }
}