import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Mapper<T, R> {
  protected final Map<R, LinkedQueue<T>> layer;
  protected final AtomicInteger count = new AtomicInteger(0);

  protected Mapper(Map<R, LinkedQueue<T>> layer) {
    this.layer = layer;
  }

  abstract void transform(T input);
}
