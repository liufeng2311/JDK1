package java.util.stream;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.IntConsumer;
import java.util.function.Predicate;

/**
 * 定义流的操作,
 * @param <T>  需要遍历的元素
 * @param <S>  返回一个新的流对象
 */
public interface BaseStream<T, S extends BaseStream<T, S>> extends AutoCloseable {

    /**
     * 旧版遍历
     * @return
     */
    Iterator<T> iterator();

    /**
     * 支持并发遍历
     * @return
     */
    Spliterator<T> spliterator();

    /**
     * 是否为并发流
     * @return
     */
    boolean isParallel();

    /**
     * 返回一个串行流
     * @return
     */
    S sequential();

    /**
     * 返回一个并行流
     * @return
     */
    S parallel();

    /**
     * 返回一个无序流
     * @return
     */
    S unordered();

    /**
     * 返回一个带有关闭方法的流, close时会执行, 也就是指定一个关闭时要执行的方法
     * @param closeHandler
     * @return
     */
    S onClose(Runnable closeHandler);

    @Override
    void close();
}
