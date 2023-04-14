package java.util.concurrent;

/**
 * 有返回值的任务
 * @author liufeng15126
 */
public interface Future<V> {

    /**
     * 为尚未开始的任务设置中断或者取消状态
     * @desc mayInterruptIfRunning的值为true表示设置中断,否则为false
     */
    boolean cancel(boolean mayInterruptIfRunning);

    /**
     * 任务状态是否为中断或者取消
     */
    boolean isCancelled();

    /**
     * 任务是否一开始执行
     */
    boolean isDone();

    /**
     * 获取任务结果
     */
    V get() throws InterruptedException, ExecutionException;

    /**
     * 获取任务结果
     */
    V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;
}
