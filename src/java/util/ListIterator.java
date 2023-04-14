package java.util;

/**
 * 定义了双向遍历
 */
public interface ListIterator<E> extends Iterator<E> {

  boolean hasNext();

  E next();

  boolean hasPrevious();

  E previous();

  int nextIndex();

  int previousIndex();

  void remove();

  void set(E e);

  void add(E e);
}
