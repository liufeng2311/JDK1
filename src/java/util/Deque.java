package java.util;

/**
 * 该类是对队列的补充, 补充了首元素和尾元素的相关操作
 * @param <E>
 */
public interface Deque<E> extends Queue<E> {
    /**
     * 添加首元素
     * @param e
     */
    void addFirst(E e);

    /**
     * 添加尾元素
     */
    void addLast(E e);

    /**
     * 添加尾元素
     */
    boolean offerFirst(E e);

    /**
     * 添加尾元素
     */
    boolean offerLast(E e);

    /**
     * 移除首元素
     */
    E removeFirst();

    /**
     * 移除尾元素
     */
    E removeLast();

    /**
     * 获取首元素
     */
    E pollFirst();

    /**
     * 获取尾元素
     */
    E pollLast();

    /**
     * 查看首元素
     */
    E getFirst();

    /**
     * 查看尾元素
     */
    E getLast();

    /**
     * 查看首元素
     */
    E peekFirst();

    /**
     * 查看尾元素
     */
    E peekLast();

    /**
     * 删除第一个匹配的元素
     */
    boolean removeFirstOccurrence(Object o);

    /**
     * 删除最后一个匹配的元素
     */
    boolean removeLastOccurrence(Object o);

    boolean add(E e);

    boolean offer(E e);

    E remove();

    E poll();

    E element();

    E peek();

    void push(E e);

    E pop();


    boolean remove(Object o);


    boolean contains(Object o);

    public int size();

    /**
     * 正序遍历
     * @return
     */
    Iterator<E> iterator();

    /**
     * 倒序遍历
     * @return
     */
    Iterator<E> descendingIterator();

}
