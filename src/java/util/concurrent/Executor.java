package java.util.concurrent;

/**
 * @author liufeng15126
 */
public interface Executor {

    /**
     * 定义了唯一的方法,执行任务
     * @param command
     */
    void execute(Runnable command);
}
