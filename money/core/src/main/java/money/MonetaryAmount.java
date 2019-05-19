package money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Currency;
import java.util.Optional;

public class MonetaryAmount {
  private final Currency currency;
  private final BigDecimal amount;

  public MonetaryAmount(Currency currency, BigDecimal amount) {
    this.currency = Optional
      .ofNullable(currency)
      .orElseThrow(() -> new RuntimeException("Currency must be present"));
    
    this.amount = Optional
      .ofNullable(amount)
      .map((amountUnscaled) -> amountUnscaled.setScale(2, RoundingMode.HALF_DOWN))
      .orElseThrow(() -> new RuntimeException("Amount must be present"));
  }

  public static MonetaryAmount usd(BigDecimal amount) {
    return new MonetaryAmount(Currency.getInstance("USD"), amount);
  }

  public Currency getCurrency() {
    return this.currency;
  }

  public BigDecimal getAmount() {
    return this.amount;
  }
}