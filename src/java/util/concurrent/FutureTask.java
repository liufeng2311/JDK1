package java.util.concurrent;
import java.util.concurrent.locks.LockSupport;


public class FutureTask<V> implements RunnableFuture<V> {
    /**
     * 任务状态只能有以下几种
     *
     * NEW -> COMPLETING -> NORMAL            初始化 -> 设置结果中 -> 正常运行结果
     * NEW -> COMPLETING -> EXCEPTIONAL       初始化 -> 设置结果中 -> 发生异常
     * NEW -> CANCELLED                       初始化 -> 取消
     * NEW -> INTERRUPTING -> INTERRUPTED     初始化 -> 开始中断 -> 中断结束
     */
    private volatile int state;
    private static final int NEW          = 0;
    //表示设置值中,值可能是正常结果,也可能是异常
    private static final int COMPLETING   = 1;
    private static final int NORMAL       = 2;
    private static final int EXCEPTIONAL  = 3;
    private static final int CANCELLED    = 4;
    private static final int INTERRUPTING = 5;
    private static final int INTERRUPTED  = 6;

    //需要执行的任务
    private Callable<V> callable;

    //任务结果
    private Object outcome;

    //运行该任务的线程
    private volatile Thread runner;


    //等待该任务结果的线程队列(单向列表)
    private volatile WaitNode waiters;

    /**
     * 设置返回值,该值可能是正常输出结果,也可能是抛出异常
     */
    private V report(int s) throws ExecutionException {
        Object x = outcome;
        if (s == NORMAL)  //正常返回
        {
            return (V)x;
        }
        if (s >= CANCELLED)  //cancel()方法取消
        {
            throw new CancellationException();
        }
        throw new ExecutionException((Throwable)x);  //任务异常
    }


    /**
     * 初始化任务体,任务的状态为NEW,表示初始化
     */
    public FutureTask(Callable<V> callable) {
        if (callable == null) throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result);
        this.state = NEW;
    }

    /**
     * 是否取消
     */
    public boolean isCancelled() {
        return state >= CANCELLED;
    }

    /**
     * 是否开始执行
     */
    public boolean isDone() {
        return state != NEW;
    }

    /**
     * 参数为true表示给一个中断状态, false表示给一个取消状态
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!(state == NEW && UNSAFE.compareAndSwapInt(this, stateOffset, NEW, mayInterruptIfRunning ? INTERRUPTING : CANCELLED))) {
            return false;
        }
        try {
            if (mayInterruptIfRunning) {  //给运行线程设置一个中断
                try {
                    Thread t = runner;
                    if (t != null) {
                        t.interrupt();
                    }
                } finally {
                    UNSAFE.putOrderedInt(this, stateOffset, INTERRUPTED);
                }
            }
        } finally {
            finishCompletion();  //任务取消,唤醒线程
        }
        return true;
    }

    /**
     * 获取执行结果
     */
    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)   //任务未完成
            s = awaitDone(false, 0L);
        return report(s);
    }

    /**
     *  获取执行结果
     */
    public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (unit == null)
            throw new NullPointerException();
        int s = state;
        if (s <= COMPLETING &&
            (s = awaitDone(true, unit.toNanos(timeout))) <= COMPLETING)
            throw new TimeoutException();
        return report(s);
    }

    /**
     * 保留方法,子类实现
     */
    protected void done() { }

    /**
     * 正常完成赋值
     */
    protected void set(V v) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {  //设值中
            outcome = v;
            UNSAFE.putOrderedInt(this, stateOffset, NORMAL); // 设值完成
            finishCompletion(); //任务结束,唤醒等待线程
        }
    }

    /**
     * 异常完成赋值
     */
    protected void setException(Throwable t) {
        if (UNSAFE.compareAndSwapInt(this, stateOffset, NEW, COMPLETING)) {  //设值中
            outcome = t;
            UNSAFE.putOrderedInt(this, stateOffset, EXCEPTIONAL); // 设值完成
            finishCompletion(); //任务结束,唤醒等待线程
        }
    }

    /**
     * 线程池执行的run方法就是该方法
     */
    public void run() {
        //任务状态异常或设置执行线程异常时直接返回
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {   //新建状态才可以执行
                V result;
                boolean ran;
                try {
                    result = c.call();
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            runner = null;
            int s = state;
            if (s >= INTERRUPTING)  //如果状态为中断,需要确保中断设置成功
                handlePossibleCancellationInterrupt(s);
        }
    }

    /**
     *  任务执行成功后且不改变任务状态
     *
     */
    protected boolean runAndReset() {
        if (state != NEW || !UNSAFE.compareAndSwapObject(this, runnerOffset, null, Thread.currentThread()))
            return false;
        boolean ran = false;
        int s = state;
        try {
            Callable<V> c = callable;
            if (c != null && s == NEW) {
                try {
                    c.call(); // don't set result
                    ran = true;
                } catch (Throwable ex) {
                    setException(ex);
                }
            }
        } finally {
            runner = null;
            s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
        return ran && s == NEW;
    }

    /**
     * 确保中断操作完成
     * cancel()方法设置中断时一个过程,CPU切换时可能方法还未执行完,这里需要等待cancel()方法执行完
     */
    private void handlePossibleCancellationInterrupt(int s) {
        if (s == INTERRUPTING)
            while (state == INTERRUPTING)
                Thread.yield();
    }

    /**
     * 等待结果节点
     */
    static final class WaitNode {
        volatile Thread thread;
        volatile WaitNode next;
        WaitNode() { thread = Thread.currentThread(); }
    }

    //唤醒所有等待结果的线程
    private void finishCompletion() {
        for (WaitNode q; (q = waiters) != null;) {
            if (UNSAFE.compareAndSwapObject(this, waitersOffset, q, null)) {  //help GC
                for (;;) {  //唤醒所有等待结果的线程
                    Thread t = q.thread;
                    if (t != null) {
                        q.thread = null;
                        LockSupport.unpark(t);
                    }
                    WaitNode next = q.next;
                    if (next == null)
                        break;
                    q.next = null; // unlink to help gc
                    q = next;
                }
                break;
            }
        }

        done(); //空实现  ExecutorCompletionService中有实现

        callable = null;
    }

    /**
     * Awaits completion or aborts on interrupt or timeout.
     *
     * 等待任务完成或者被中断终止或者超时
     */
    private int awaitDone(boolean timed, long nanos) throws InterruptedException {
        final long deadline = timed ? System.nanoTime() + nanos : 0L; //计算等待时间,0表示无限期等待
        WaitNode q = null;
        boolean queued = false;
        for (;;) {
            if (Thread.interrupted()) {   //线程状态为中断,从等待队列中移除并抛出中断异常
                removeWaiter(q);
                throw new InterruptedException();
            }

            int s = state;
            if (s > COMPLETING) { //任务已完成,返回结果
                if (q != null)
                    q.thread = null;
                return s;
            }
            else if (s == COMPLETING) // 正在设置结果,让出CPU使用权
                Thread.yield();
            else if (q == null)  //任务还未结束,准备将当前线程存储至等待队列
                q = new WaitNode();
            else if (!queued) //加入等待队列,头插法
                queued = UNSAFE.compareAndSwapObject(this, waitersOffset, q.next = waiters, q);
            else if (timed) {
                nanos = deadline - System.nanoTime();
                if (nanos <= 0L) {
                    removeWaiter(q);  //获取结果任务超时,返回任务现在的状态
                    return state;
                }
                LockSupport.parkNanos(this, nanos); //阻塞当前线程指定时间
            }
            else
                LockSupport.park(this);  //阻塞当前线程
        }
    }

    /**
     * 移除某一节点
     * 该方法和参数node没有任何关系,移除等待链表中所有取消的线程
     */
    private void removeWaiter(WaitNode node) {
        if (node != null) {
            node.thread = null;
            retry:
            for (;;) {          // restart on removeWaiter race
                for (WaitNode pred = null, q = waiters, s; q != null; q = s) {
                    s = q.next;
                    if (q.thread != null)  //当前节点存在线程信息,pred = q   pred永远指向的是遍历到的节点,可以理解为前继节点
                        pred = q;
                    else if (pred != null) {  //当前节点为空,前继节点指向后继节点
                        pred.next = s;
                        if (pred.thread == null) // 前继节点获取到任务了,开始新的遍历
                            continue retry;
                    }
                    else if (!UNSAFE.compareAndSwapObject(this, waitersOffset, q, s)) //走到这一步说明s前的节点都失效了,waiters = s
                        continue retry;
                }
                break;
            }
        }
    }

    // Unsafe mechanics
    private static final sun.misc.Unsafe UNSAFE;
    private static final long stateOffset;
    private static final long runnerOffset;
    private static final long waitersOffset;
    static {
        try {
            UNSAFE = sun.misc.Unsafe.getUnsafe();
            Class<?> k = FutureTask.class;
            stateOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("state"));
            runnerOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("runner"));
            waitersOffset = UNSAFE.objectFieldOffset
                (k.getDeclaredField("waiters"));
        } catch (Exception e) {
            throw new Error(e);
        }
    }

}
