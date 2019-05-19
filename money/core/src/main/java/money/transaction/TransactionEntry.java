package money.transaction;

import java.math.BigDecimal;
import java.util.Optional;

import money.MonetaryAmount;
import money.account.Account;
import money.exceptions.CurrencyNotAllowedException;

public class TransactionEntry {
  private final Account account;
  private final MonetaryAmount amount;
  private final TransactionEntryType type;

  public enum TransactionEntryType {
    DEBIT,
    CREDIT
  }

  private TransactionEntry(Account account, MonetaryAmount amount, TransactionEntryType type) {
    this.account = Optional
      .ofNullable(account)
      .orElseThrow(() -> new RuntimeException("Account must be present"));;

    this.amount = Optional
      .ofNullable(amount)
      .orElseThrow(() -> new RuntimeException("Amount must be present"));

    this.type = Optional
      .ofNullable(type)
      .orElseThrow(() -> new RuntimeException("Type must be present"));

    if (!account.getCurrency().equals(amount.getCurrency())) {
      throw new CurrencyNotAllowedException();
    }
  }

  public static TransactionEntry debit(Account account, MonetaryAmount amount) {
    return new TransactionEntry(account, amount, TransactionEntryType.DEBIT);
  }

  public static TransactionEntry credit(Account account, MonetaryAmount amount) {
    return new TransactionEntry(account, amount, TransactionEntryType.CREDIT);
  }

  public boolean isDebit() {
    return this.type.equals(TransactionEntryType.DEBIT);
  }

  public boolean isCredit() {
    return this.type.equals(TransactionEntryType.CREDIT);
  }

  public Account getAccount() {
    return this.account;
  }

  public MonetaryAmount getAmount() {
    return this.amount;
  }

  public BigDecimal getSignedAmount() {
    if (this.isCredit()) {
      return this.getAmount().getAmount();
    } else {
      return this.getAmount().getAmount().negate();
    }
  }
}