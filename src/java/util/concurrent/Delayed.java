package java.util.concurrent;

/**
 * @author liufeng15126
 */
public interface Delayed extends Comparable<Delayed> {

    /**
     * 获取延迟时间
     * @param unit
     * @return
     */
    long getDelay(TimeUnit unit);
}
