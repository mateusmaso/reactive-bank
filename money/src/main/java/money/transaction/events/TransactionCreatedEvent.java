package money.transaction.events;

import money.MonetaryEvent;
import money.transaction.models.Transaction;

public class TransactionCreatedEvent extends MonetaryEvent {
  private final Transaction transaction;

  public TransactionCreatedEvent(Transaction transaction) {
    this.transaction = transaction;
  }

  public Transaction getTransaction() {
    return this.transaction;
  }
}