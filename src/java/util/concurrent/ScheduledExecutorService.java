package java.util.concurrent;

public interface ScheduledExecutorService extends ExecutorService {

    ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit);


    <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit);

    /**
     * 以开始时间为准
     */
    ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit);

    /**
     * 已结束时间为准
     */
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit);

}
