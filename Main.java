import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/* You are allowed to 1. add modifiers to fields and method signatures of subclasses, and 2. add code at the marked places, including removing the following return */
public class Main {
  public static void main(String[] args) throws InterruptedException {

    // this one is where we put all the numbers
    LinkedQueue<Integer> inputQueue = new LinkedQueue<>();

    // we split them between those two
    LinkedQueue<Integer> evenQueue = new LinkedQueue<>();
    LinkedQueue<Integer> oddQueue = new LinkedQueue<>();

    //
    HashMap<Boolean, LinkedQueue<Integer>> layer = new HashMap<>();
    layer.put(true, evenQueue);
    layer.put(false, oddQueue);

    int n = 1000;

    // first, we make all the thread pools
    ExecutorService inputPool = Executors.newCachedThreadPool();
    ExecutorService distributerPool = Executors.newCachedThreadPool();
    ExecutorService reducerPool = Executors.newCachedThreadPool();

    // These has to be atomic because they will be decremtented concurrently
    AtomicInteger inputTasksRemaining = new AtomicInteger(n);
    AtomicInteger mappingTasksRemaining = new AtomicInteger(n);

    Mapper<Integer, Boolean> mapper1 = new Mapper<Integer, Boolean>(layer) {
      @Override
      void transform(Integer input) {
        boolean isEven = (input % 2 == 0);
        layer.get(isEven).insert(input * input);
        count.incrementAndGet();
      }
    };
    Mapper<Integer, Boolean> mapper2 = new Mapper<Integer, Boolean>(layer) {
      @Override
      void transform(Integer input) {
        boolean isEven = (input % 2 == 0);
        layer.get(isEven).insert(input * input);
        count.incrementAndGet();
      }
    };

    Reducer<Integer> reducer1 = new Reducer<Integer>() {
      @Override
      protected synchronized void reduce(Integer input) {
        current += input;
        count.incrementAndGet();
      }
    };
    Reducer<Integer> reducer2 = new Reducer<Integer>() {

      @Override
      protected synchronized void reduce(Integer input) {
        current += input;
        count.incrementAndGet();
      }
    };

    // Start all three phases concurrently

    // Fill input queue
    for (int i = 1; i <= n; i++) {
      final int number = i;
      inputPool.submit(() -> {
        try {
          inputQueue.insert(number);
        } finally {
          if (inputTasksRemaining.decrementAndGet() == 0) {
            inputQueue.signalProducerDone();
          }
        }
      });
    }

    // Distribute work to mappers
    for (int i = 0; i < n; i++) {
      final int taskIndex = i;
      distributerPool.submit(() -> {
        try {
          Integer number = inputQueue.delfront();
          if (number != null) {
            // alternate
            if (taskIndex % 2 == 0) {
              mapper1.transform(number);
            } else {
              mapper2.transform(number);
            }
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        } finally {
          if (mappingTasksRemaining.decrementAndGet() == 0) {
            // All mapping tasks are submitted to the pool.
            evenQueue.signalProducerDone();
            oddQueue.signalProducerDone();
          }
        }
      });
    }

    int evenNumbers = n / 2;
    int oddNumbers = n - n / 2;

    // Reduce the results - even numbers
    for (int i = 0; i < evenNumbers; i++) {
      reducerPool.submit(() -> {
        try {
          Integer evenNumber = evenQueue.delfront();
          if (evenNumber != null) {
            reducer1.reduce(evenNumber);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    // reducing the odd numbers
    for (int i = 0; i < oddNumbers; i++) {
      reducerPool.submit(() -> {
        try {
          Integer oddNumber = oddQueue.delfront();
          if (oddNumber != null) {
            reducer2.reduce(oddNumber);
          }
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
        }
      });
    }

    // stop threads
    inputPool.shutdown();
    distributerPool.shutdown();
    reducerPool.shutdown();

    // wait for each thread, it should never even get close to this...
    long maxTerminationTime = 100;
    inputPool.awaitTermination(maxTerminationTime, TimeUnit.SECONDS);
    distributerPool.awaitTermination(maxTerminationTime, TimeUnit.SECONDS);
    reducerPool.awaitTermination(maxTerminationTime, TimeUnit.SECONDS);

    System.out.println("Sum even: " + reducer1.current);
    System.out.println("Sum odd: " + reducer2.current);

    int total = 0;
    for (int i = 1; i <= n; i++) {
      total += i * i;
    }
    System.out.println(total - (reducer1.current + reducer2.current));
  }
}
