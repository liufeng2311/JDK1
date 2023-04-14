package java.util.concurrent;
import java.util.List;
import java.util.Collection;

public interface ExecutorService extends Executor {

    /**
     * 不再接收新的任务,队列中的任务依旧执行
     */
    void shutdown();

    /**
     * 不再接收新的任务,队列中的任务也不会被执行
     * @return 返回队列中未执行的任务的集合
     */
    List<Runnable> shutdownNow();

    /**
     * 判断当前线程池的状态是否为关闭
     * 该状态下线程不会接收新的任务
     */
    boolean isShutdown();

    /**
     * 线程是否都被回收
     */
    boolean isTerminated();

    /**
     * 指定时间内判断状态是否变为TERMINATED
     */
    boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 有结果集任务
     */
    <T> Future<T> submit(Callable<T> task);

    /**
     * 有结果集任务
     */
    <T> Future<T> submit(Runnable task, T result);

    /**
     * 有结果集任务
     */
    Future<?> submit(Runnable task);

    /**
     * 批量执行,均执行完返回
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException;

    /**
     * 批量执行,均执行完或超时返回
     */
    <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks,long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 批量执行,其中一个执行完返回
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException;

    /**
     * 批量执行,其中一个执行完或超时返回
     */
    <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
