package java.util.concurrent;

import java.util.Collection;
import java.util.Queue;

public interface BlockingQueue<E> extends Queue<E> {

    boolean add(E e);

    boolean offer(E e);

    boolean offer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    void put(E e) throws InterruptedException;


    E take() throws InterruptedException;

    E poll(long timeout, TimeUnit unit) throws InterruptedException;

    /**
     * 剩余空间
     */
    int remainingCapacity();

    boolean remove(Object o);

    boolean contains(Object o);

    /**
     * 复制元素去一个新的集合中
     */
    int drainTo(Collection<? super E> c);

    /**
     * 复制元素去一个新的集合中,并制定数量
     */
    int drainTo(Collection<? super E> c, int maxElements);
}
