package java.util;

import java.util.function.UnaryOperator;

public interface List<E> extends Collection<E> {

    int size();

    boolean isEmpty();

    boolean contains(Object o);

    Iterator<E> iterator();

    Object[] toArray();

    /**
     * 集合转指定类型数组
     */
    <T> T[] toArray(T[] a);

    boolean add(E e);

    /**
     * 移除元素,存在多个只移除第一个
     */
    boolean remove(Object o);

    boolean containsAll(Collection<?> c);

    boolean addAll(Collection<? extends E> c);

    boolean addAll(int index, Collection<? extends E> c);

    boolean removeAll(Collection<?> c);

    /**
     * 保留两个集合的交集元素
     */
    boolean retainAll(Collection<?> c);

    /**
     * 对结合中的元素进行操作并只保留操作后的结果
     */
    default void replaceAll(UnaryOperator<E> operator) {
        Objects.requireNonNull(operator);
        final ListIterator<E> li = this.listIterator();
        while (li.hasNext()) {
            li.set(operator.apply(li.next()));
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    default void sort(Comparator<? super E> c) {
        Object[] a = this.toArray();
        Arrays.sort(a, (Comparator) c);
        ListIterator<E> i = this.listIterator();
        for (Object e : a) {
            i.next();
            i.set((E) e);
        }
    }

    void clear();

    boolean equals(Object o);

    int hashCode();

    E get(int index);

    E set(int index, E element);

    void add(int index, E element);

    E remove(int index);

    int indexOf(Object o);

    int lastIndexOf(Object o);

    ListIterator<E> listIterator();

    ListIterator<E> listIterator(int index);

    List<E> subList(int fromIndex, int toIndex);

    @Override
    default Spliterator<E> spliterator() {
        return Spliterators.spliterator(this, Spliterator.ORDERED);
    }
}
