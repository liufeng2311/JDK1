package java.util.stream;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;

public interface Collector<T, A, R> {
    /**
     * 返回存储结果的容器
     */
    Supplier<A> supplier();

    /**
     * 同意线程叠加到容器的方法
     */
    BiConsumer<A, T> accumulator();

    /**
     * 不同线程合并的方法
     */
    BinaryOperator<A> combiner();

    /**
     * 累加器A到最终结果类型R的转化
     */
    Function<A, R> finisher();

    /**
     * 返回收集器的特征
     */
    Set<Characteristics> characteristics();

    public static <T, R> Collector<T, R, R> of(Supplier<R> supplier, BiConsumer<R, T> accumulator, BinaryOperator<R> combiner, Characteristics... characteristics) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs = (characteristics.length == 0)
                ? Collectors.CH_ID
                : Collections.unmodifiableSet(EnumSet.of(Collector.Characteristics.IDENTITY_FINISH,
                characteristics));
        return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, cs);
    }


    public static <T, A, R> Collector<T, A, R> of(Supplier<A> supplier, BiConsumer<A, T> accumulator, BinaryOperator<A> combiner, Function<A, R> finisher, Characteristics... characteristics) {
        Objects.requireNonNull(supplier);
        Objects.requireNonNull(accumulator);
        Objects.requireNonNull(combiner);
        Objects.requireNonNull(finisher);
        Objects.requireNonNull(characteristics);
        Set<Characteristics> cs = Collectors.CH_NOID;
        if (characteristics.length > 0) {
            cs = EnumSet.noneOf(Characteristics.class);
            Collections.addAll(cs, characteristics);
            cs = Collections.unmodifiableSet(cs);
        }
        return new Collectors.CollectorImpl<>(supplier, accumulator, combiner, finisher, cs);
    }

    enum Characteristics {
        /**
         * 并发执行的
         */
        CONCURRENT,

        /**
         * 不用按照集合元素的顺序来保留结果的顺序
         */
        UNORDERED,

        /**
         * 表示结果为等值函数,可直接进行强制转化
         */
        IDENTITY_FINISH
    }
}
