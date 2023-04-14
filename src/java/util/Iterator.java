package java.util;

import java.util.function.Consumer;

public interface Iterator<E> {

    /**
     * 是否有下一个
     */
    boolean hasNext();

    /**
     * 下一个
     */
    E next();

    /**
     * 移除当前元素
     */
    default void remove() {
        throw new UnsupportedOperationException("remove");
    }

    /**
     * 消费每一个元素
     * @param action
     */
    default void forEachRemaining(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        while (hasNext())
            action.accept(next());
    }
}
