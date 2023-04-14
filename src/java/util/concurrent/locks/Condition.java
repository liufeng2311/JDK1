package java.util.concurrent.locks;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public interface Condition {

    /**
     * 当前线程等待,直到被唤醒或响应中断
     */
    void await() throws InterruptedException;

    /**
     * 当前线程等待,直到被唤醒,忽略阻塞
     */
    void awaitUninterruptibly();

    /**
     * 当前线程等待,直到被唤醒或响应中断或超时,等待时间为纳秒
     */
    long awaitNanos(long nanosTimeout) throws InterruptedException;

    /**
     * 当前线程等待,直到被唤醒或响应中断或超时
     */
    boolean await(long time, TimeUnit unit) throws InterruptedException;

    /**
     * 当前线程等待,直到被唤醒或响应中断或超时
     */
    boolean awaitUntil(Date deadline) throws InterruptedException;

    /**
     * 唤醒一个等待线程
     */
    void signal();

    /**
     * 唤醒所有等待线程
     */
    void signalAll();
}
