import java.util.concurrent.atomic.AtomicInteger;

public abstract class Reducer<T> {
  protected volatile int current = 0;
  protected final AtomicInteger count = new AtomicInteger(0);

  protected abstract void reduce(T input);
}
