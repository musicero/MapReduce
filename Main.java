import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* You are allowed to 1. add modifiers to fields and method signatures of subclasses, and 2. add code at the marked places, including removing the following return */
public class Main {
  public static void main(String[] args) throws InterruptedException {

    LinkedQueue<Integer> inputQueue = new LinkedQueue<>();
    LinkedQueue<Integer> evenQueue = new LinkedQueue<>();
    LinkedQueue<Integer> oddQueue = new LinkedQueue<>();

    HashMap<Boolean, LinkedQueue<Integer>> layer = new HashMap<>();
    layer.put(true, evenQueue);
    layer.put(false, oddQueue);

    int n = 1000;

    ExecutorService inputExc = Executors.newCachedThreadPool();
    ExecutorService distribute = Executors.newCachedThreadPool();
    ExecutorService reduce = Executors.newCachedThreadPool();

    Mapper<Integer, Boolean> mapper1 = new Mapper<Integer, Boolean>(layer) {
      @Override
      void transform(Integer input) {
        boolean isEven = (input % 2 == 0);
        layer.get(isEven).insert(input * input);
        count++;
      }
    };
    Mapper<Integer, Boolean> mapper2 = new Mapper<Integer, Boolean>(layer) {
      @Override
      void transform(Integer input) {
        boolean isEven = (input % 2 == 0);
        layer.get(isEven).insert(input * input);
        count++;
      }
    };

    Reducer<Integer> reducer1 = new Reducer<Integer>() {
      @Override
      protected synchronized void reduce(Integer input) {
        current += input;
        count++;
      }
    };
    Reducer<Integer> reducer2 = new Reducer<Integer>() {

      @Override
      protected synchronized void reduce(Integer input) {
        current += input;
        count++;
      }
    };

    // Start all three phases concurrently
    
    // Fill input queue
    for (int i = 1; i <= n; i++) {
      final int number = i;
      inputExc.submit(() -> inputQueue.insert(number));
    }

    // Distribute work to mappers
    for (int i = 0; i < n; i++) {
      final int taskIndex = i;
      distribute.submit(() -> {
        Integer number = inputQueue.delfront();
        while (number == null) {
          try { Thread.sleep(1); } catch (InterruptedException e) {}
          number = inputQueue.delfront();
        }
        if (taskIndex % 2 == 0) {
          mapper1.transform(number);
        } else {
          mapper2.transform(number);
        }
      });
    }

    // Reduce the results - need to handle both even and odd numbers
    for (int i = 0; i < 500; i++) {
      // Even number processing
      reduce.submit(() -> {
        Integer evenNumber = evenQueue.delfront();
        while (evenNumber == null) {
          try { Thread.sleep(1); } catch (InterruptedException e) {}
          evenNumber = evenQueue.delfront();
        }
        reducer1.reduce(evenNumber);
      });
      
      // Odd number processing  
      reduce.submit(() -> {
        Integer oddNumber = oddQueue.delfront();
        while (oddNumber == null) {
          try { Thread.sleep(1); } catch (InterruptedException e) {}
          oddNumber = oddQueue.delfront();
        }
        reducer2.reduce(oddNumber);
      });
    }

    Thread.sleep(2000);
    System.out.println("Sum even: " + reducer1.current);
    System.out.println("Sum odd: " + reducer2.current);

    int total = 0;
    for (int i = 1; i <= n; i++) {
      total += i * i;
    }
    System.out.println(total - (reducer1.current + reducer2.current));
  }
}
