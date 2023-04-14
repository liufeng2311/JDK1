package liufeng.executor;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Author: liufeng
 * @Date: 2020/11/25
 * @desc 线程池
 */
public class ThreadPoolExecutorDemo{


  public static void main(String[] args) throws ExecutionException, InterruptedException {

    ExecutorService executorService = Executors.newSingleThreadExecutor();
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    ExecutorService executorService1 = Executors.newFixedThreadPool(2);
    ExecutorService executorService2 = Executors.newCachedThreadPool();
    LinkedBlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(100);
    //构造线程池
    ThreadPoolExecutor pool = new ThreadPoolExecutor(10, 20, 60L, TimeUnit.SECONDS, queue);
    //执行没有返回值的任务体
    pool.execute(() -> {
      System.out.println("执行没有返回值的任务");
    });
    pool.shutdown();

    //执行有返回值的任务体
    Future submit = pool.submit(() -> 11);
    submit.cancel(true);
    submit.get();

    ForkJoinPool forkJoinPool = new ForkJoinPool();
    forkJoinPool.execute(() -> {
      System.out.println("ForkJoinPool");
    });

    scheduledExecutorService.execute(() -> {
      System.out.println("设置延迟队列");
    });
    Executors.newSingleThreadExecutor();
  }
}
