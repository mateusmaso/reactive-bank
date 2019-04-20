package money;

import java.util.concurrent.ConcurrentLinkedQueue;

// 
public class MonetaryMessageInbox {
  // Lock-free push queue
  private final ConcurrentLinkedQueue<MonetaryMessage> queue;

  public MonetaryMessageInbox() {
    this.queue = new ConcurrentLinkedQueue<MonetaryMessage>();
  }

  public void add(MonetaryMessage message) {
    this.queue.add(message);
  }
}