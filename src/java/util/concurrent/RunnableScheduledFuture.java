package java.util.concurrent;

/**
 * 组合接口,同时提供周期性方法
 * @author liufeng15126
 */
public interface RunnableScheduledFuture<V> extends RunnableFuture<V>, ScheduledFuture<V> {

    /**
     * R判断是否为周期性的
     * @return 是否为周期性实现
     */
    boolean isPeriodic();
}
