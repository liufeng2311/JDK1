package liufeng.executor;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * @Author: liufeng
 * @Date: 2021/6/1
 * @desc
 */
public class Test{

  public static void main(String[] args) throws ExecutionException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(1);
    CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
      try {
        TimeUnit.SECONDS.sleep(2);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      return 12;
    }, executor).thenApply(x -> {
      System.out.println("####");
      return 2 * x;
    });
    System.out.println("====");
    Integer integer = future.get();
    System.out.println("====");
    System.out.println(integer);
  }
}
