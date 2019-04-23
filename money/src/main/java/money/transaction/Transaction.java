package money.transaction;

import java.util.Optional;

import money.exceptions.InvalidTransactionException;

public class Transaction {
  private final String id;
  private final TransactionType type;
  private final TransactionEntry debitEntry;
  private final TransactionEntry creditEntry;

  public enum TransactionType {
    LOAD,
    UNLOAD,
    TRANSFER
  }

  public Transaction(String id, TransactionType type, TransactionEntry debitEntry, TransactionEntry creditEntry) {
    this.id = id;
    this.type = type;
    this.debitEntry = Optional
      .ofNullable(debitEntry)
      .orElseThrow(() -> new RuntimeException("Debit entry must be present"));
    
    this.creditEntry = Optional
      .ofNullable(creditEntry)
      .orElseThrow(() -> new RuntimeException("Credit entry must be present"));

    if (this.debitEntry.getAccount().equals(this.creditEntry.getAccount())) {
      throw new InvalidTransactionException();
    }
  }

  public String getId() {
    return this.id;
  }

  public boolean isTransfer() {
    return this.type.equals(TransactionType.TRANSFER);
  }

  public TransactionEntry getDebitEntry() {
    return this.debitEntry;
  }

  public TransactionEntry getCreditEntry() {
    return this.creditEntry;
  }
}