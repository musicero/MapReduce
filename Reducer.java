public abstract class Reducer<T> {
  protected volatile int current = 0;
  protected volatile int count = 0;

  protected abstract void reduce(T input);
}
