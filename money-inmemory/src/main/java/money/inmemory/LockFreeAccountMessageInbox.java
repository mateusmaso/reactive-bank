package money.inmemory;

import java.util.concurrent.ConcurrentLinkedQueue;

import money.account.AccountMessageInbox;
import money.account.models.Account;
import money.MonetaryMessage;

public class LockFreeAccountMessageInbox implements AccountMessageInbox {
  private final ConcurrentLinkedQueue<MonetaryMessage> queue;

  public LockFreeAccountMessageInbox() {
    this.queue = new ConcurrentLinkedQueue<MonetaryMessage>();
  }

  @Override
  public void send(Account account, MonetaryMessage message) {
    this.queue.add(message);
  }

  @Override
  public MonetaryMessage read() {
    return null;
  }
}