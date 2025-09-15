import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.Condition;

public class LinkedQueue<T> {
  private Node<T> head;
  private Node<T> tail;
  private final ReentrantLock lock = new ReentrantLock();
  private final Condition notEmpty = lock.newCondition();
  private volatile boolean producerDone = false;

  public Node<T> find(T t) {
    lock.lock();
    try {
      Node<T> current = head;
      while (current != null) {
        if (current.content.equals(t)) {
          return current;
        }
        current = current.next;
      }
      return null;
    } finally {
      lock.unlock();
    }
  }

  public void insert(T t) {
    lock.lock();
    try {
      Node<T> newNode = new Node<>(t);
      if (tail == null) { // if empty
        head = tail = newNode;
      } else {
        tail.next = newNode;
        tail = newNode;
      }
      notEmpty.signalAll(); // wake consumers
    } finally {
      lock.unlock();
    }
  }

  public T delfront() throws InterruptedException {
    lock.lock();
    try {
      while (head == null && !producerDone) {
        notEmpty.await();
      }

      if (head == null) {
        return null;
      }

      T content = head.content;
      head = head.next;
      if (head == null) {
        tail = null;
      }
      return content;
    } finally {
      lock.unlock();
    }
  }

  public void signalProducerDone() {
    lock.lock();
    try {
      producerDone = true;
      notEmpty.signalAll(); // wake consumers
    } finally {
      lock.unlock();
    }
  }
}
