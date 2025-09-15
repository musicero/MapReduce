import java.util.concurrent.atomic.AtomicInteger;

public abstract class Reducer<T> {
  // using AtomicInteger was a suggestion from chatGPT, this worked really well!
  protected final AtomicInteger current = new AtomicInteger(0);
  protected final AtomicInteger count = new AtomicInteger(0);

  protected abstract void reduce(T input);
}
