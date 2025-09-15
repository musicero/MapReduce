public class LinkedQueue<T> {
  private volatile Node<T> head;
  private volatile Node<T> tail;

  public synchronized int find(T t) {
    Node<T> current = head;
    while (current != null) {
      if (current.content.equals(t)) {
        return System.identityHashCode(current);
      }
      current = current.next;
    }
    return 0;
  }

  public synchronized void insert(T t) {
    Node<T> newNode = new Node<>(t);
    if (tail == null) {
      head = tail = newNode;
    } else {
      tail.next = newNode;
      tail = newNode;
    }
  }

  public synchronized T delfront() {
    if (head == null) {
      return null;
    }
    T content = head.content;
    head = head.next;
    if (head == null) {
      tail = null;
    }
    return content;
  }
}
