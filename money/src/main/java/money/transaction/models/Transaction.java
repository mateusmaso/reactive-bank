package money.transaction.models;

import java.util.Optional;
import java.util.SortedSet;

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

  public Transaction(String id, TransactionType type, SortedSet<TransactionEntry> doubleEntry) {
    if (!Optional.ofNullable(doubleEntry.first()).isPresent()) {
      throw new RuntimeException("Debit entry must be present.");
    }

    if (!Optional.ofNullable(doubleEntry.last()).isPresent()) {
      throw new RuntimeException("Credit entry must be present.");
    }

    this.id = id;
    this.type = type;
    this.debitEntry = doubleEntry.first();
    this.debitEntry.setTransaction(this);
    this.creditEntry = doubleEntry.last();
    this.creditEntry.setTransaction(this);

    if (!this.debitEntry.getAccount().equals(this.creditEntry.getAccount())) {
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