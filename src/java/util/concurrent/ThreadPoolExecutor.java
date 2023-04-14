package java.util.concurrent;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class ThreadPoolExecutor extends AbstractExecutorService {

    //通过原子类AtomicInteger保存当前线程池状态和线程数(默认线程池状态为运行中、线程数为0)
    private final AtomicInteger ctl = new AtomicInteger(ctlOf(RUNNING, 0));

    //ctl中两个属性的分割线是一个常量值29(低29位表示线程数,高3位表示线程状态)
    private static final int COUNT_BITS = Integer.SIZE - 3;

    //最大线程数,后29位全为1
    private static final int CAPACITY = (1 << COUNT_BITS) - 1;

    //定义线程的五种状态(这里只记录高三位的值,低29位均为零)
    //运行中(111)
    private static final int RUNNING = -1 << COUNT_BITS;
    //关闭(000)(拒绝新任务提交,但会执行队列中的任务)
    private static final int SHUTDOWN = 0 << COUNT_BITS;
    //停止(001)(拒绝新任务提交,并且丢弃队列中的任务)
    private static final int STOP = 1 << COUNT_BITS;
    //整理完毕(010)(线程数为零)
    private static final int TIDYING = 2 << COUNT_BITS;
    //终止(011)(terminated方法执行完成后会设置线程池为该状态)
    private static final int TERMINATED = 3 << COUNT_BITS;


    /**
     * 从原子类ctl中获取运行状态
     */
    private static int runStateOf(int c) {
        return c & ~CAPACITY;
    }

    /**
     * 从原子类ctl中获取线程数
     */
    private static int workerCountOf(int c) {
        return c & CAPACITY;
    }

    /**
     * 根据运行状态和线程数生成ctl
     */
    private static int ctlOf(int rs, int wc) {
        return rs | wc;
    }

    /**
     * 小于指定状态
     */
    private static boolean runStateLessThan(int c, int s) {
        return c < s;
    }

    /**
     * 大于等于指定状态
     */
    private static boolean runStateAtLeast(int c, int s) {
        return c >= s;
    }

    /**
     * 判断状态是否为运行中
     */
    private static boolean isRunning(int c) {
        return c < SHUTDOWN;
    }

    /**
     * 线程数+1(不一定设置成功)
     */
    private boolean compareAndIncrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect + 1);
    }

    /**
     * 线程数-1(不一定设置成功)
     */
    private boolean compareAndDecrementWorkerCount(int expect) {
        return ctl.compareAndSet(expect, expect - 1);
    }

    /**
     * 线程数-1(一定设置成功)
     */
    private void decrementWorkerCount() {
        do {
        } while (!compareAndDecrementWorkerCount(ctl.get()));
    }

    /**
     * 阻塞队列(保存任务)
     */
    private final BlockingQueue<Runnable> workQueue;

    /**
     * 修改公共变量workers、largestPoolSize、completedTaskCount等类属性时需要获取该锁
     *
     * @TODO 待验证
     */
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * 保存线程池中的线程封装类,真正的线程封装在该对象中,只有获取mainLock才可以修改,
     */
    private final HashSet<Worker> workers = new HashSet<Worker>();

    /**
     * mainLock锁的Condition对象
     */
    private final Condition termination = mainLock.newCondition();

    /**
     * 记录线程池中同时存在线程的最大值,只有获取mainLock才可以修改
     */
    private int largestPoolSize;

    /**
     * 记录已完成任务数
     */
    private long completedTaskCount;

    /**
     * 线程工厂
     */
    private volatile ThreadFactory threadFactory;

    /**
     * 拒绝策略
     */
    private volatile RejectedExecutionHandler handler;

    /**
     * 非核心线程数没有任务时的存活时间
     */
    private volatile long keepAliveTime;

    /**
     * 是否允许回收核心线程数,默认false,表示不允许
     */
    private volatile boolean allowCoreThreadTimeOut;

    /**
     * 核心线程数,只有在allowCoreThreadTimeOut=true时才可以被回收
     */
    private volatile int corePoolSize;

    /**
     * 最大线程数
     */
    private volatile int maximumPoolSize;

    /**
     * 默认的拒绝策略采用丢弃
     */
    private static final RejectedExecutionHandler defaultHandler = new AbortPolicy();

    /**
     * 调用shutdown() and shutdownNow()方法需要验证权限
     *
     * @TODO 待验证
     */
    private static final RuntimePermission shutdownPerm = new RuntimePermission("modifyThread");

    /**
     * 继承AQS并实现Runnable,该类内部保存了一个线程池中的线程,用于控制线程 继承AQS是实现锁 实现Runnable是用于生成线程池中的运行线程
     */

    private final class Worker extends AbstractQueuedSynchronizer implements Runnable {

        private static final long serialVersionUID = 6138294804551838833L;

        /**
         * 线程池中真正运行的线程. 只有线程池创建线程才会为Null  Null if factory fails.
         */
        final Thread thread;
        /**
         * 运行第一个任务,可能为Null
         */
        Runnable firstTask;
        /**
         * 当前线程完成的任务数
         */
        volatile long completedTasks;

        /**
         * 构建Worker类,并传入生成线程需要执行的第一个任务,任务可以为空
         *
         * @TODO 为什么设置-1呢(已解决)
         */
        Worker(Runnable firstTask) {
            //主要是防止在线程未调用start方法前就执行中断操作,任务时调用interruptIfStarted()方法
            //实例化后还需要一些其他工作才会启动线程,在未调用start()方法之前对该对象执行中断时无效的
            //通过state来保证启程启动后才能中断,防止出现调用了interruptWorkers(),但是正在启动的线程却接受不到中断指令的信息
            setState(-1);
            this.firstTask = firstTask;
            this.thread = getThreadFactory().newThread(this);  //利用线程池和当前Runnable类开启一个线程,该线程还未启动
        }

        /**
         * 线程池中线程真正执行的逻辑,指向外部类runWorker
         */
        public void run() {
            runWorker(this);
        }

        /**
         * AQS锁方法 state=0表示无锁 state=1表示有锁
         *
         * @TODO 重新理解AQS锁
         */

        /**
         * 返回锁是否被其他线程占用
         *
         * @return
         */
        protected boolean isHeldExclusively() {
            return getState() != 0;
        }

        /**
         * 尝试获取锁
         */
        protected boolean tryAcquire(int unused) {
            if (compareAndSetState(0, 1)) {
                setExclusiveOwnerThread(Thread.currentThread());  //获取成功,设置锁的所有者为当前线程
                return true;
            }
            return false;
        }

        /**
         * 释放锁,设置state=0
         *
         * @param unused
         * @return
         */
        protected boolean tryRelease(int unused) {
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        /**
         * 获取锁(设置state=1)
         */
        public void lock() {
            acquire(1);
        }

        /**
         * 尝试获取锁,设置(state=1)
         */
        public boolean tryLock() {
            return tryAcquire(1);
        }

        /**
         * 释放锁
         *
         * @TODO 查看AQS获取锁原理
         */
        public void unlock() {
            release(1);
        }

        /**
         * 锁是否被占用,等同于isHeldExclusively方法
         */
        public boolean isLocked() {
            return isHeldExclusively();
        }

        /**
         * 给当前线程设置一个中断状态 只有在interruptWorkers()方法中采用调用该方法,给一个中断状态
         * 线程在开始执行每个方法时都会判断中断状态以决定是否进行中断
         */
        void interruptIfStarted() {
            Thread t;
            if (getState() >= 0 && (t = thread) != null && !t.isInterrupted()) {
                try {
                    t.interrupt();
                } catch (SecurityException ignore) {
                }
            }
        }
    }

    /**
     * 设置线程池状态至少为指定状态或指定状态以上
     */
    private void advanceRunState(int targetState) {
        for (; ; ) {
            int c = ctl.get();
            if (runStateAtLeast(c, targetState) ||
                    ctl.compareAndSet(c, ctlOf(targetState, workerCountOf(c)))) {
                break;
            }
        }
    }

    /**
     * 尝试设置线程池状态为Terminate, 在任何需要设置线程池状态为Terminate的地方,如果发现workerCount不为空,都要通过interruptWorkers方法传递一个中断的限号量
     * 当线程池状态为SHUTDOWN且worker和队列都为null,需要尝试 当线程池状态为STOP且列为null,需要尝试
     * <p>
     * 所有线程数减少的地方都需要尝试设置线程状态为Terminate,线程减少可能都是发起了关闭操作
     *
     * @TODO termination.signalAll();   唤醒awaitTermination()方法中等待的线程
     */
    final void tryTerminate() {
        for (; ; ) {
            int c = ctl.get();
            if (isRunning(c) ||         //线程池状态为运行中,不可设置为Terminate
                    runStateAtLeast(c, TIDYING) ||   //线程池状态为TIDYING,Terminate,说明其他线程已开始该操作,此线程不需要执行
                    (runStateOf(c) == SHUTDOWN && !workQueue
                            .isEmpty())) { //线程池状态为SHUTDOWN且队列不为空,不可设置为Terminate
                return;
            }
            if (workerCountOf(c) != 0) { //存在工作线程数,尝试向一个线程发送中断请求
                interruptIdleWorkers(ONLY_ONE);
                return;
            }

            //执行到这里说明没有工作线程数
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                if (ctl.compareAndSet(c, ctlOf(TIDYING, 0))) {  //判断线程池状态是否为TIDYING
                    try {
                        terminated();  //设置线程池状态为TERMINATED前需要做的事,默认是一个空实现
                    } finally {
                        ctl.set(ctlOf(TERMINATED, 0)); //设置线程池状态为TERMINATED
                        termination.signalAll(); //唤醒其它等待锁的线程
                    }
                    return;
                }
            } finally {
                mainLock.unlock();
            }
            // else retry on failed CAS
        }
    }

    /*
     * Methods for controlling interrupts to worker threads.
     */

    /**
     * 如果开启了权限管理器,需要验证权限
     */
    private void checkShutdownAccess() {
        SecurityManager security = System.getSecurityManager();
        if (security != null) {
            security.checkPermission(shutdownPerm);
            final ReentrantLock mainLock = this.mainLock;
            mainLock.lock();
            try {
                for (Worker w : workers) {
                    security.checkAccess(w.thread);
                }
            } finally {
                mainLock.unlock();
            }
        }
    }

    /**
     * 给所有开始执行任务的工作线程一个中断标识
     */
    private void interruptWorkers() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                w.interruptIfStarted();
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断空闲线程,给所有的线程一个中断状态,保证设置的值可以生效,
     * 不会中断新增但未启动的线程
     */
    private void interruptIdleWorkers(boolean onlyOne) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (Worker w : workers) {
                Thread t = w.thread;
                //w.tryLock()成功表示线程处于空闲
                if (!t.isInterrupted() && w.tryLock()) {
                    try {
                        t.interrupt();
                    } catch (SecurityException ignore) {
                    } finally {
                        w.unlock();
                    }
                }
                if (onlyOne) {
                    break;
                }
            }
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 中断所有空闲线程
     */
    private void interruptIdleWorkers() {
        interruptIdleWorkers(false);
    }

    //标志只中断一个空闲线程
    private static final boolean ONLY_ONE = true;


    /**
     * 执行拒绝策略,ScheduledThreadPoolExecutor中可以使用, 因为该方法是受保护的
     */
    final void reject(Runnable command) {
        handler.rejectedExecution(command, this);
    }

    /**
     * 钩子方法 ScheduledThreadPoolExecutor类中实现,用于取消延迟任务
     */
    void onShutdown() {
    }

    /**
     * 钩子方法 ScheduledThreadPoolExecutor类中使用
     */
    final boolean isRunningOrShutdown(boolean shutdownOK) {
        int rs = runStateOf(ctl.get());
        return rs == RUNNING || (rs == SHUTDOWN && shutdownOK);
    }

    /**
     * 移动任务队列至一个list中
     */
    private List<Runnable> drainQueue() {
        BlockingQueue<Runnable> q = workQueue;
        ArrayList<Runnable> taskList = new ArrayList<Runnable>();
        q.drainTo(taskList);
        if (!q.isEmpty()) {
            for (Runnable r : q.toArray(new Runnable[0])) {
                if (q.remove(r)) {
                    taskList.add(r);
                }
            }
        }
        return taskList;
    }


    /**
     * 线程池核心代码 主要是根据任务生成worker
     *
     * @param firstTask 新线程执行的第一个任务
     * @param core      是否创建的是核心线程数
     */
    private boolean addWorker(Runnable firstTask, boolean core) {
        retry:
        for (; ; ) {
            int c = ctl.get();
            int rs = runStateOf(c);

            //不可创建线程的情况
            //线程池状态为STOP、TIDYING、TERMINATED
            //当线程池状态为SHUTDOWN时,不可接受新任务,创建的线程必须是不含初始任务的新线程,用来执行队列中未执行的任务
            if (rs >= SHUTDOWN && !(rs == SHUTDOWN && firstTask == null && !workQueue.isEmpty())) {
                return false;
            }

            for (; ; ) {
                int wc = workerCountOf(c);
                //即使线程池处于运行状态,如下两种情况也不可以创建线程
                //1. 线程数达最大线程数    2.线程数达到了允许创建的最大值
                if (wc >= CAPACITY || wc >= (core ? corePoolSize : maximumPoolSize)) {
                    return false;
                }
                //尝试将ctl+1,成功则退出该层循环(如果创建线程失败时需要-1)
                if (compareAndIncrementWorkerCount(c)) {
                    break retry;  //跳出外层循环
                }
                c = ctl.get();
                //创建失败时需要判断线程池状态是否改变,如果改变跳至外层循环,否则继续执行内部循环
                if (runStateOf(c) != rs) {
                    continue retry;
                }
            }
        }

        boolean workerStarted = false; //工作线程所在的Worker包装类是否启动线层成功
        boolean workerAdded = false; //Worker对象是否添加至workers集合中
        Worker w = null;
        try {
            w = new Worker(firstTask);
            final Thread t = w.thread;  //该线程则为线程池中的工作线程,由于线程工厂可以自定义实现,所以会对线程做验证
            if (t != null) {
                final ReentrantLock mainLock = this.mainLock;
                mainLock.lock();  //这块加锁是为了保证workers线程数和ctl中的线程数保持一致
                try {
                    int rs = runStateOf(ctl.get());
                    //再次判断是否可以创建线程,该结果是ctl+1的结果(1代表当前线层,满足的话就确认添加该工作线程)
                    //情况一：添加该线程依旧处于运行中
                    //情况二：处于SHUTDOWN,但创建的是一个空闲线程用来执行队列里的任务
                    if (rs < SHUTDOWN || (rs == SHUTDOWN && firstTask == null)) {
                        // 线程池可以开发者自己提供,这里判断创建的线程状态,若线程已起动则抛出异常,该线程必须由此框架启动才可以
                        if (t.isAlive()) {
                            throw new IllegalThreadStateException();
                        }
                        workers.add(w);  //添加worker即worker集合,保存所有的工作线程包装类
                        int s = workers.size();
                        if (s > largestPoolSize) { //记录同时工作的最大线程数
                            largestPoolSize = s;
                        }
                        workerAdded = true;
                    }
                } finally {
                    mainLock.unlock();
                }
                if (workerAdded) {   //添加成功后启动线程
                    t.start();
                    workerStarted = true;
                }
            }
        } finally {
            if (!workerStarted) {  //如果启动失败,需要从集合中移除Worker,并且ctl-1
                addWorkerFailed(w);
            }
        }
        return workerStarted;
    }

    /**
     * 添加的Worker启动线程失败,需要从列表移除
     */
    private void addWorkerFailed(Worker w) {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            if (w != null) {
                workers.remove(w); //移除worker
            }
            decrementWorkerCount(); //线程数-1
            tryTerminate(); //所有线程数减少的地方都需要尝试设置线程状态为Terminate,
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 处理worker退出
     * 分为正常退出和异常退出两种情况
     */
    private void processWorkerExit(Worker w, boolean completedAbruptly) {
        //非正常退出的话线程数-1,正常退出时已经-1了,这里不需要-1
        if (completedAbruptly) {
            decrementWorkerCount();
        }

        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            completedTaskCount += w.completedTasks;  //将该线程执行的任务数统计到completedTaskCount中
            workers.remove(w); //移除该线程
        } finally {
            mainLock.unlock();
        }

        tryTerminate(); //尝试Terminate

        int c = ctl.get();
        if (runStateLessThan(c, STOP)) {  //如果现在还不是STOP以上状态
            if (!completedAbruptly) {   //且是正常退出
                int min = allowCoreThreadTimeOut ? 0 : corePoolSize;  //判断线程池中最小线程数
                if (min == 0 && !workQueue.isEmpty()) {  //
                    min = 1;
                }
                //判断当前线层池中的线程是否达到最小值,达到则不用创建新线程,达不到则创建
                if (workerCountOf(c) >= min) {
                    return; // replacement not needed
                }
            }

            //创建一个先的没有任务的等待线程(上述逻辑决定是否需要创建)
            addWorker(null, false);
        }
    }

    /**
     * worker线程获取执行任务
     */
    private Runnable getTask() {
        boolean timedOut = false; //默认该线程不需要回收,只有在通过指定回收时长内没有获取到任务才会设置为true,true表示需要判断是否回收该线程

        for (; ; ) {
            int c = ctl.get();
            int rs = runStateOf(c);

            // 没有任务,线程数-1(这里也就是为什么正常退出的线程在processWorkerExit为什么没有-1的原因)
            if (rs >= SHUTDOWN && (rs >= STOP || workQueue.isEmpty())) {
                decrementWorkerCount();
                return null;
            }

            int wc = workerCountOf(c);

            //是否可以回收空闲线程的前提条件(线程数大于核心线程数或者可回收核心线程数)
            boolean timed = allowCoreThreadTimeOut || wc > corePoolSize;

            //已下条件回收线程
            if ((wc > maximumPoolSize    //大于最大线程数
                    || (timed && timedOut)) && (wc > 1 || workQueue.isEmpty())) { //上轮循环线程通过指定时长未获取到任务且满足回收条件(还存在正在运行的线程,或者只有该线程在运行但已经没有任务了)
                if (compareAndDecrementWorkerCount(c)) {
                    return null;
                }
                continue;  //-1失败,进入下一次循环
            }

            try {
                Runnable r = timed ?  //可回收时通过poll操作,不可回收时通过take操作
                        workQueue.poll(keepAliveTime, TimeUnit.NANOSECONDS) :
                        workQueue.take();
                if (r != null) {  //take方法取到的肯定不为空
                    return r;
                }
                //执行到这里说明通过poll在指定时间内没有取到任务,判断该线程是否需要回收
                timedOut = true;
            } catch (InterruptedException retry) {
                timedOut = false;
            }
        }
    }

    /**
     * 线程池中线程执行的逻辑,由run()方法调用
     *
     * @TODO 为什么每次获取任务都需要获取锁, 释放锁呢(已解决)
     * interruptIdleWorkers()方法终止的是空闲线程,如果有线程正在执行任务是不可以被中断的,必须获取锁才可以中断
     */
    final void runWorker(Worker w) {
        Thread wt = Thread.currentThread();
        Runnable task = w.firstTask; //执行的任务
        w.firstTask = null; //设置任务为空,方便回收
        w.unlock(); //创建线程时设置了state=-1保证不可中断,现在设置为允许中断
        boolean completedAbruptly = true;  //该标识用于表示线程是执行完任务正常结束,还是发生异常而结束的,true表示发生异常而结束
        try {
            while (task != null || (task = getTask()) != null) {  //从队列中循环取任务
                w.lock();  //加锁是为了任务在执行过程中不可以被中断,调用中断任务interruptIdleWorkers()方法必须获取锁才可以设置中断
                //这里必须保证STOP以上(含STOP)必须为中断状态,RUNNABLE状态必须为非中断,interruptIdleWorkers()方法时如果线程恰好没启动,就需要在这里设置中断状态
                if ((runStateAtLeast(ctl.get(), STOP) ||
                        (Thread.interrupted() && runStateAtLeast(ctl.get(), STOP))) && !wt.isInterrupted()) {
                    wt.interrupt();
                }
                try {
                    beforeExecute(wt, task);   //该任务执行前,空实现,可以处理中断
                    Throwable thrown = null;
                    try {
                        task.run();  //执行任务,也就是我们提交的Runnable
                    } catch (RuntimeException x) {
                        thrown = x;
                        throw x;
                    } catch (Error x) {
                        thrown = x;
                        throw x;
                    } catch (Throwable x) {
                        thrown = x;
                        throw new Error(x);
                    } finally {
                        afterExecute(task, thrown); //该任务执行后,空实现,可以处理中断
                    }
                } finally {
                    task = null; //设置任务为空,方便回收
                    w.completedTasks++; //完成的任务数+1
                    w.unlock();
                }
            }
            completedAbruptly = false;  //设置正常结束
        } finally {
            processWorkerExit(w, completedAbruptly);
        }
    }

    /**
     ***********************************************************************************************
     *                                        构造方法                                              *
     ***********************************************************************************************
     */

    /**
     * 使用默认的线程工厂和默认的拒绝策略生成线程池示例
     */
    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), defaultHandler);
    }

    /**
     * 使用默认的拒绝策略生成线程池示例(默认的拒绝策略是抛出异常)
     */
    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, defaultHandler);
    }

    /**
     * 使用默认的线程工厂生成线程池示例
     */
    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, Executors.defaultThreadFactory(), handler);
    }

    /**
     * 生成线程池示例
     *
     * @param corePoolSize    核心线程数(核心线程数空闲时不会被回收,但可以通过参数allowCoreThreadTimeOut设置为可回收)
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   非核心线程数存活时间
     * @param unit            keepAliveTime所设置时间的单位
     * @param workQueue       阻塞队列,存放任务体
     * @param threadFactory   线程工厂,线程池中的线程都是由该工厂创建的
     * @param handler         拒绝策略,当任务无法执行时采用的策略
     */
    public ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit,
                              BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        if (corePoolSize < 0 || maximumPoolSize <= 0 || maximumPoolSize < corePoolSize || keepAliveTime < 0) {
            throw new IllegalArgumentException();
        }
        if (workQueue == null || threadFactory == null || handler == null) {
            throw new NullPointerException();
        }
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.workQueue = workQueue;
        this.keepAliveTime = unit.toNanos(keepAliveTime);
        this.threadFactory = threadFactory;
        this.handler = handler;
    }

    /**
     * 执行没有返回值的任务
     */
    public void execute(Runnable command) {
        if (command == null) {  //任务体不可以为空
            throw new NullPointerException();
        }
        int c = ctl.get();
        //当工作线程小于corePoolSize,尝试启动一个新的工作线程并将当前任务作为该线程的第一个任务
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true)) {
                return;
            }
            c = ctl.get();
        }
        //不能启动核心线程时,将任务添加到阻塞队列中
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (!isRunning(recheck) && remove(command)) {  //添加至队列后发现线程池状态变为非运行状态,移除该任务并执行拒绝策略
                reject(command);
            } else if (workerCountOf(recheck) == 0) {  //线程池状态为运行中,但是没有工作线层时,开启一个无任务的新线程(因为核心线程可能被被完全回收了,在getTask()方法中可能全部回收)
                addWorker(null, false);
            }
            //线程池正处于关闭期间或者阻塞队列也满了,尝试启动一个非核心线程执行该任务
        } else if (!addWorker(command, false)) {
            reject(command);  //再次启动新线程失败,调用拒绝策略
        }
    }

    /**
     * 发起一个shutdown命令
     */
    public void shutdown() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(SHUTDOWN);
            interruptIdleWorkers();
            onShutdown(); // hook for ScheduledThreadPoolExecutor
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
    }

    /**
     * 发起一个stop命令
     */
    public List<Runnable> shutdownNow() {
        List<Runnable> tasks;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            checkShutdownAccess();
            advanceRunState(STOP);
            interruptWorkers();
            tasks = drainQueue();
        } finally {
            mainLock.unlock();
        }
        tryTerminate();
        return tasks;
    }

    public boolean isShutdown() {
        return !isRunning(ctl.get());
    }

    /**
     * 线程池是否正处于Terminate执行的过程中
     */
    public boolean isTerminating() {
        int c = ctl.get();
        return !isRunning(c) && runStateLessThan(c, TERMINATED);
    }

    /**
     * 状态是否为已设置为Terminated
     *
     * @return
     */
    public boolean isTerminated() {
        return runStateAtLeast(ctl.get(), TERMINATED);
    }

    /**
     * 判断状态是否为Termination,并设置等待时间
     */
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (; ; ) {
                if (runStateAtLeast(ctl.get(), TERMINATED)) {
                    return true;
                }
                if (nanos <= 0) {
                    return false;
                }
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
        }
    }

    protected void finalize() {
        shutdown();
    }

    /**
     * 设置线程池
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        if (threadFactory == null) {
            throw new NullPointerException();
        }
        this.threadFactory = threadFactory;
    }

    /**
     * 获取线程池
     */
    public ThreadFactory getThreadFactory() {
        return threadFactory;
    }

    /**
     * 设置拒绝策略
     */
    public void setRejectedExecutionHandler(RejectedExecutionHandler handler) {
        if (handler == null) {
            throw new NullPointerException();
        }
        this.handler = handler;
    }

    /**
     * 获取拒绝策略
     */
    public RejectedExecutionHandler getRejectedExecutionHandler() {
        return handler;
    }

    /**
     * 设置核心线程数
     */
    public void setCorePoolSize(int corePoolSize) {
        if (corePoolSize < 0) {
            throw new IllegalArgumentException();
        }
        int delta = corePoolSize - this.corePoolSize;
        this.corePoolSize = corePoolSize;
        if (workerCountOf(ctl.get()) > corePoolSize) {
            interruptIdleWorkers();
        } else if (delta > 0) {
            int k = Math.min(delta, workQueue.size());
            while (k-- > 0 && addWorker(null, true)) {
                if (workQueue.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
     * 获取核心线程数
     */
    public int getCorePoolSize() {
        return corePoolSize;
    }

    /**
     * 尝试创建一个核心线程数,该线程是一个空闲线程等待新任务
     */
    public boolean prestartCoreThread() {
        return workerCountOf(ctl.get()) < corePoolSize &&
                addWorker(null, true);
    }

    /**
     * 创建一个线程数
     */
    void ensurePrestart() {
        int wc = workerCountOf(ctl.get());
        if (wc < corePoolSize) {
            addWorker(null, true);
        } else if (wc == 0) {
            addWorker(null, false);
        }
    }

    /**
     * 创建全部的没有任务的核心线程数
     */
    public int prestartAllCoreThreads() {
        int n = 0;
        while (addWorker(null, true)) {
            ++n;
        }
        return n;
    }

    /**
     * 是否允许回收核心线程数
     */
    public boolean allowsCoreThreadTimeOut() {
        return allowCoreThreadTimeOut;
    }

    /**
     * 修改allowCoreThreadTimeOut的值
     */
    public void allowCoreThreadTimeOut(boolean value) {
        if (value && keepAliveTime <= 0) {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        }
        if (value != allowCoreThreadTimeOut) {
            allowCoreThreadTimeOut = value;
            if (value) {
                interruptIdleWorkers();
            }
        }
    }

    /**
     * 修改最大线程数
     *
     * @param maximumPoolSize
     */
    public void setMaximumPoolSize(int maximumPoolSize) {
        if (maximumPoolSize <= 0 || maximumPoolSize < corePoolSize) {
            throw new IllegalArgumentException();
        }
        this.maximumPoolSize = maximumPoolSize;
        if (workerCountOf(ctl.get()) > maximumPoolSize) {
            interruptIdleWorkers();
        }
    }

    public int getMaximumPoolSize() {
        return maximumPoolSize;
    }


    public void setKeepAliveTime(long time, TimeUnit unit) {
        if (time < 0) {
            throw new IllegalArgumentException();
        }
        if (time == 0 && allowsCoreThreadTimeOut()) {
            throw new IllegalArgumentException("Core threads must have nonzero keep alive times");
        }
        long keepAliveTime = unit.toNanos(time);
        long delta = keepAliveTime - this.keepAliveTime;
        this.keepAliveTime = keepAliveTime;
        if (delta < 0) {
            interruptIdleWorkers();
        }
    }


    public long getKeepAliveTime(TimeUnit unit) {
        return unit.convert(keepAliveTime, TimeUnit.NANOSECONDS);
    }

    public BlockingQueue<Runnable> getQueue() {
        return workQueue;
    }

    public boolean remove(Runnable task) {
        boolean removed = workQueue.remove(task);
        tryTerminate(); // In case SHUTDOWN and now empty
        return removed;
    }

    /**
     * 移除队列中状态为取消的任务
     */
    public void purge() {
        final BlockingQueue<Runnable> q = workQueue;
        try {
            Iterator<Runnable> it = q.iterator();
            while (it.hasNext()) {
                Runnable r = it.next();
                if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) {
                    it.remove();
                }
            }
        } catch (ConcurrentModificationException fallThrough) {
            // Take slow path if we encounter interference during traversal.
            // Make copy for traversal and call remove for cancelled entries.
            // The slow path is more likely to be O(N*N).
            for (Object r : q.toArray()) {
                if (r instanceof Future<?> && ((Future<?>) r).isCancelled()) {
                    q.remove(r);
                }
            }
        }

        tryTerminate(); // In case SHUTDOWN and now empty
    }


    /**
     * 获取当前线程池线程数
     */
    public int getPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return runStateAtLeast(ctl.get(), TIDYING) ? 0
                    : workers.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 获取正在执行任务的线程数
     */
    public int getActiveCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            int n = 0;
            for (Worker w : workers) {
                if (w.isLocked()) {
                    ++n;
                }
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 获取最大线程数
     *
     * @return
     */
    public int getLargestPoolSize() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            return largestPoolSize;
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 获取所有任务数(包括已执行和未执行的)
     *
     * @return
     */
    public long getTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
                if (w.isLocked()) {
                    ++n;
                }
            }
            return n + workQueue.size();
        } finally {
            mainLock.unlock();
        }
    }

    /**
     * 获取完成任务数
     *
     * @return
     */
    public long getCompletedTaskCount() {
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            long n = completedTaskCount;
            for (Worker w : workers) {
                n += w.completedTasks;
            }
            return n;
        } finally {
            mainLock.unlock();
        }
    }


    public String toString() {
        long ncompleted;
        int nworkers, nactive;
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            ncompleted = completedTaskCount;
            nactive = 0;
            nworkers = workers.size();
            for (Worker w : workers) {
                ncompleted += w.completedTasks;
                if (w.isLocked()) {
                    ++nactive;
                }
            }
        } finally {
            mainLock.unlock();
        }
        int c = ctl.get();
        String rs = (runStateLessThan(c, SHUTDOWN) ? "Running" :
                (runStateAtLeast(c, TERMINATED) ? "Terminated" :
                        "Shutting down"));
        return super.toString() +
                "[" + rs +
                ", pool size = " + nworkers +
                ", active threads = " + nactive +
                ", queued tasks = " + workQueue.size() +
                ", completed tasks = " + ncompleted +
                "]";
    }


    protected void beforeExecute(Thread t, Runnable r) {

    }

    protected void afterExecute(Runnable r, Throwable t) {
    }

    protected void terminated() {
    }


    //当前线程调用
    public static class CallerRunsPolicy implements RejectedExecutionHandler {
        public CallerRunsPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                r.run();
            }
        }
    }

    //抛异常
    public static class AbortPolicy implements RejectedExecutionHandler {

        public AbortPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            throw new RejectedExecutionException("Task " + r.toString() +
                    " rejected from " +
                    e.toString());
        }
    }

    //不做任何处理
    public static class DiscardPolicy implements RejectedExecutionHandler {

        public DiscardPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
        }
    }

    //丢弃最旧的任务
    public static class DiscardOldestPolicy implements RejectedExecutionHandler {

        public DiscardOldestPolicy() {
        }

        public void rejectedExecution(Runnable r, ThreadPoolExecutor e) {
            if (!e.isShutdown()) {
                e.getQueue().poll();
                e.execute(r);
            }
        }
    }
}
