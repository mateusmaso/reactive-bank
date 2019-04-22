package money.inmemory;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

public class AppendOnlyWaitFreeList<E> {
  private ConcurrentLinkedQueue<E> entries;

  public AppendOnlyWaitFreeList() {
    this.entries = new ConcurrentLinkedQueue<E>();
  }

  public Stream<E> getAll() {
    return entries.stream();
  }

  public E append(E entry) {
    entries.add(entry);
    return entry;
  }
}