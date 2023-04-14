package java.util.stream;

import java.util.Spliterator;
import java.util.function.IntFunction;

/**
 * 定义流的帮助类, 所有的流都会继承该方法
 * @param <P_OUT> 需要遍历的元素
 */
abstract class PipelineHelper<P_OUT> {

    /**
     * 获得流的形状
     * @return
     */
    abstract StreamShape getSourceShape();

    /**
     * 获取流和操作标识
     * @return
     */
    abstract int getStreamAndOpFlags();

    /**
     * 返回此次流元素大小
     * @param spliterator
     * @param <P_IN>
     * @return
     */
    abstract <P_IN> long exactOutputSizeIfKnown(Spliterator<P_IN> spliterator);

    /**
     * 构造流调用链,并进行调用 每个流的实现类都可以发起计算，包括两步：1.构造调用量  2.发起调用
     * @param sink 最后一个消费者
     * @param spliterator
     * @param <P_IN>
     * @param <S>
     * @return
     */
    abstract <P_IN, S extends Sink<P_OUT>> S wrapAndCopyInto(S sink, Spliterator<P_IN> spliterator);

    /**
     * 非短路执行调用链
     * @param wrappedSink
     * @param spliterator
     * @param <P_IN>
     */
    abstract <P_IN> void copyInto(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator);

    /**
     * 短路执行调用链
     * @param wrappedSink
     * @param spliterator
     * @param <P_IN>
     */
    abstract <P_IN> void copyIntoWithCancel(Sink<P_IN> wrappedSink, Spliterator<P_IN> spliterator);

    /**
     * 包装调用链
     * @param sink
     * @param <P_IN>
     * @return
     */
    abstract <P_IN> Sink<P_IN> wrapSink(Sink<P_OUT> sink);

    /**
     * 包装源
     * @param spliterator
     * @param <P_IN>
     * @return
     */
    abstract <P_IN> Spliterator<P_OUT> wrapSpliterator(Spliterator<P_IN> spliterator);

    /**
     * TODO  并发相关,暂不理解
     * @param exactSizeIfKnown
     * @param generator
     * @return
     */
    abstract Node.Builder<P_OUT> makeNodeBuilder(long exactSizeIfKnown, IntFunction<P_OUT[]> generator);

    /**
     * TODO  并发相关,暂不理解
     * @param spliterator
     * @param flatten
     * @param generator
     * @param <P_IN>
     * @return
     */
    abstract <P_IN> Node<P_OUT> evaluate(Spliterator<P_IN> spliterator, boolean flatten, IntFunction<P_OUT[]> generator);
}
