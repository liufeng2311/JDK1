package java.util.concurrent.locks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import sun.misc.Unsafe;

public abstract class AbstractQueuedSynchronizer extends AbstractOwnableSynchronizer implements java.io.Serializable {

    protected AbstractQueuedSynchronizer() {
    }

    /**
     * AQS队列使用到的节点类型
     */
    static final class Node {
        /**
         * 共享锁模式表示,赋值于nextWaiter属性
         * <br>
         * 在该模式下,获取锁后state的值>0的话,需要判断后继节点是否也为共享锁模式,是的话则唤醒后继节点
         */
        static final Node SHARED = new Node();
        /**
         * 排它锁模式,赋值于nextWaiter属性
         */
        static final Node EXCLUSIVE = null;
        /**
         * 节点由于超时、中断、获取锁失败等异常而变为CANCELLED状态,表示需要从队列中移除
         * <br>
         * 该状态的waitStatus是唯一>0的,后续都通过是否>0判断节点状态是否为CANCELLED状态
         */
        static final int CANCELLED = 1;
        /**
         * 节点的后继节点即将或已经进入阻塞状态,节点释放锁时需要调用unpark()方法确保继节处于非阻塞状态
         */
        static final int SIGNAL = -1;
        /**
         * 该状态的节点只存在于Condition队列上,表示处于等待状态
         * <br>
         * 只有在唤醒时才会转移到同步队列,此时waitStatus会变为0,表示排它锁
         */
        static final int CONDITION = -2;
        /**
         * 共享模式下,该节点会通知等待共享锁的节点,也就是传播,直到遇到一个为排它锁的状节点停止
         */
        static final int PROPAGATE = -3;
        /**
         * 节点状态,作用于同步队列和条件队列
         * 状态值为 1, -1, -2, -3
         */
        volatile int waitStatus;
        /**
         * 前继节点,作用于同步队列
         */
        volatile Node prev;
        /**
         * 后继节点,作用于同步队列
         */
        volatile Node next;
        /**
         * 当前节点代表的线程,作用于同步队列和条件队列
         */
        volatile Thread thread;
        /**
         * 当作用于同步队列上时,表示节点时共享锁,还是排它锁
         * <br>
         * 当作用于条件队列时,表示当前节点的后继节点
         * <p>
         * 该节点没有被volatile修饰,因为在同步队列下,创建后就不会改变,在条件队列下本身就是线程安全的
         */
        Node nextWaiter;

        /**
         * 是否为共享锁模式
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回前继节点,会做非空验证
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null) {
                throw new NullPointerException();
            } else {
                return p;
            }
        }

        /**
         * 构造同步队列head空节点
         */
        Node() {
        }

        /**
         * 构造同步队列节点
         */
        Node(Thread thread, Node mode) {
            this.nextWaiter = mode;
            this.thread = thread;
        }

        /**
         * 构造条件队列节点,条件队列上的均为排它锁节点
         */
        Node(Thread thread, int waitStatus) { // Used by Condition
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }

    /**
     * 同步队列头结点,
     */
    private transient volatile Node head;

    /**
     * 同步队列尾结点
     */
    private transient volatile Node tail;

    /**
     * The synchronization state. 最重要变量,锁状态
     */
    private volatile int state;

    /**
     * 获取当前锁状态,只有获取锁的线程才能调用该方法
     */
    protected final int getState() {
        return state;
    }

    /**
     * 设置当前锁状态,只有获取锁的线程才能调用该方法
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * 多线程竞争获取锁
     */
    protected final boolean compareAndSetState(int expect, int update) {
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }

    /**
     * 进行自旋的默认时间,即该时间内通过自旋获取锁,用于指定之间内获取锁,超过此时间才可以阻塞
     * 默认时间为1000纳秒
     */
    static final long spinForTimeoutThreshold = 1000L;

    /**
     * 1. 采用尾插法添加元素,并返回之前的尾结点
     * <br>
     * 2. 队列的head节点为一个不含线程信息的占位节点
     * <br>
     * 3. 由于双向链表的添加操作是非原子性的(先设置prev再设置next),所以从head节点遍历可能造成元素遍历不到,需要从tail节点向前遍历
     */
    private Node enq(final Node node) {
        for (; ; ) {
            Node t = tail;
            //首元素是一个空节点
            if (t == null) {
                if (compareAndSetHead(new Node())) {
                    tail = head;
                }
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }

    /**
     * 向双向链表新增元素(尾插法),并返回新增的元素
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        Node pred = tail;
        //添加node节点至队列尾部,该方法不一定成功,但是enq(node)一定会成功
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        //节点入队列,该方法可以保证节点可以成功加入队列
        enq(node);
        return node;
    }

    /**
     * 将head指向当前获取锁的线程,head永远是一个空节点
     * 获取锁的线程调用
     */
    private void setHead(Node node) {
        head = node; //指向当前节点
        node.thread = null; //help GC
        node.prev = null;  //help GC
    }

    /**
     * 一个线程释放锁时,需要唤醒后继线程
     * 这里采用的是从tail向前遍历,因为在新增元素时先设置的prev节点,如果从head向后遍历的话,会遍历不到最后增加的元素、
     *
     * @param node 该值只有两种情况,
     */
    private void unparkSuccessor(Node node) {

        int ws = node.waitStatus;

        //头结点为0是使用判断后继是否有节点的,设置成功有节点时会改变,设置失败则已存在节点
        //此处设置为0是为了减少后继节点进入进行休眠的概率, 新节点需要根据head.waitStatus判断是否需要进行阻塞(两次阻塞)
        if (ws < 0) {
            compareAndSetWaitStatus(node, ws, 0);
        }

        //寻找链表中下一个需要获取锁的线程节点,查看next节点是否可用, 不存在则从尾结点开始往前找(从头结点遍历会导致数据遍历不全)
        Node s = node.next;
        if (s == null || s.waitStatus > 0) {
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev) {
                if (t.waitStatus <= 0) {
                    s = t;
                }
            }
        }
        if (s != null) {
            LockSupport.unpark(s.thread);
        }
    }

    /**
     * TODO 需要再次理解
     * 当head的后继节点阻塞时,唤醒它的后继节点
     * <br>
     * 当head的后继节点还没有阻塞,设置当前节点的的状态为PROPAGATE,该状态主要是解决Semaphore下的问题
     */
    private void doReleaseShared() {
        for (; ; ) {
            Node h = head;
            //列表不为空
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    //设置头节点的状态为0,是为了减少后继节点进行阻塞的概率,也可以不设置
                    //此处设置失败,说明后继节点是活跃的
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0)) {
                        continue;
                    }
                    unparkSuccessor(h);  //唤醒后继线程
                } else if (ws == 0 && !compareAndSetWaitStatus(h, 0, Node.PROPAGATE)) { //如果为正在运行状态,设置状态为传播
                    continue;                // loop on failed CAS
                }
            }
            //如果head节点发生变化,继续唤醒新的节点
            if (h == head) {
                break;
            }
        }
    }

    /**
     * propagate > 0表示存在可用的锁,继续唤醒后续共享节点, 该方法表示获取锁成功后执行的
     * h.waitStatus < 0表示的Node.PROPAGATE
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // Record old head for check below
        setHead(node);
        if (propagate > 0 || h == null || h.waitStatus < 0 || (h = head) == null || h.waitStatus < 0) {
            Node s = node.next;
            if (s == null || s.isShared()) {
                doReleaseShared();
            }
        }
    }

    // Utilities for various versions of acquire

    /**
     * 此时的节点已经进入了队列, 获取锁失败需要需求
     * 当获取时失败(获取超时、获取锁超最大值)
     * 取消当前节点,需要考虑三个问题
     * 该节点为tail节点如何处理
     * 该节点为head节点如何处理
     * 该节点为中间节点如何处理
     * <p>
     * 取消的节点为中间节点的话及时不做任何操作也不会影响功能
     */
    private void cancelAcquire(Node node) {
        // 若节点为空,不做处理
        if (node == null) {
            return;
        }
        //清空节点中线程信息、设置节点状态为取消
        node.thread = null;
        node.waitStatus = Node.CANCELLED;

        //指向前继状态不为CANCELLED的节点,由于head节点永远为占位节点,所以node.prev不可能为空
        Node pred = node.prev;
        while (pred.waitStatus > 0) {
            node.prev = pred = pred.prev;
        }
        Node predNext = pred.next;

        /**
         * 清除当前节点,不过不一定成功
         * 因为当前节点已经设置为CANCELLED状态,即使这里清除失败了也会被其他的线程清除掉
         */
        //当前节点为尾结点
        if (node == tail && compareAndSetTail(node, pred)) {
            compareAndSetNext(pred, predNext, null);
        } else {
            //当前节点为中间节点,设置需要唤醒后续节点
            int ws;
            if (pred != head && ((ws = pred.waitStatus) == Node.SIGNAL || (ws <= 0 && compareAndSetWaitStatus(pred, ws, Node.SIGNAL))) && pred.thread != null) {
                Node next = node.next;
                //TODO  这里只保证了前继节点到后继节点的关系, 没有保证后继到前继的关系
                if (next != null && next.waitStatus <= 0) {
                    compareAndSetNext(pred, predNext, next);
                }
            } else {
                //当前节点为头结点
                unparkSuccessor(node);
            }

            node.next = node; // help GC
        }
    }

    /**
     * 节点进入阻塞之前,将前继节点设置为SIGNAL状态,确保可以被唤醒, 具体分为两步：
     * 1. 向前查找第一个状态不为取消的节点, 同时清除期间内找到的状态为取消的节点
     * 2. 设置该节点的状态为SIGNAL
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {

        int ws = pred.waitStatus;
        //如果已设置当前节点的前继节点的状态为SIGNAL, 表示前继节点执行完毕后需要唤醒当前节点, 当前节点返回true表示执行阻塞操作
        if (ws == Node.SIGNAL) {
            return true;
        }
        //如果前继节点的状态为取消,表示节点无效, 向前查找, 直到找到一个不为取消状态的节点,并删除链表中的无效节点
        if (ws > 0) {
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {
            //设置前继节点状态为唤醒, 当前节点准备进行阻塞
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }

    /**
     * 给当前线程一个中断状态
     */
    static void selfInterrupt() {
        Thread.currentThread().interrupt();
    }

    /**
     * 阻塞线程,唤醒时返回中断状态,之所以返回中断状态,是因为不同方法对中断的处理方式不同
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);
        return Thread.interrupted();
    }


    /**
     ***********************************************************************************************
     *                                  首次获取锁失败后,添加至队列中等待                               *
     ***********************************************************************************************
     */


    /**
     * 添加进队列中的线程并没有直接进行阻塞,而是通过前继节点的状态来决定的,分如下情况
     * <br>
     * 情况一:前继节点为头结点的话则不需要阻塞,因为很快就可以获得锁
     * <br>
     * 情况二：前继节点不为头结点时,设置前继节点的waitStatus为SIGNAL(该状态表示需要唤醒后继节点),表示要进行阻塞,再阻塞前再判断前继是否为头结点
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;  //中断状态默认为false,Thread.interrupted()会消除该线程中断状态,使用该变量保存原本的中断状态
            for (; ; ) {
                final Node p = node.predecessor();  //获取该节点的前继节点,相对于head的后继节点来说,head永远是不含线程信息的占位节点
                if (p == head && tryAcquire(arg)) {
                    setHead(node); //head指向当前节点node
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                //前继结点不是head,第一次将前置状态设置为SIGNAL,第二次阻塞自己
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) { //阻塞线程并保存中断状态
                    interrupted = true;  //表示线程中断状态
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    /**
     * 同acquireQueued,线程中断时会抛出InterruptedException异常
     */
    private void doAcquireInterruptibly(int arg) throws InterruptedException {
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return;
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    /**
     * 同acquireQueued,并设置了超时时长
     */
    private boolean doAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
        if (nanosTimeout <= 0L) {
            return false;
        }
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.EXCLUSIVE);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return true;
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L) {
                    return false;
                }
                if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold) {
                    LockSupport.parkNanos(this, nanosTimeout); //阻塞指定时间
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    /**
     * 获取一个共享锁
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        if (interrupted) {
                            selfInterrupt();
                        }
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
                    interrupted = true;
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    /**
     * Acquires in shared interruptible mode.
     *
     * @param arg the acquire argument
     */
    private void doAcquireSharedInterruptibly(int arg) throws InterruptedException {
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return;
                    }
                }
                if (shouldParkAfterFailedAcquire(p, node) && parkAndCheckInterrupt()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    /**
     * Acquires in shared timed mode.
     *
     * @param arg          the acquire argument
     * @param nanosTimeout max wait time
     * @return {@code true} if acquired
     */
    private boolean doAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
        if (nanosTimeout <= 0L) {
            return false;
        }
        final long deadline = System.nanoTime() + nanosTimeout;
        final Node node = addWaiter(Node.SHARED);
        boolean failed = true;
        try {
            for (; ; ) {
                final Node p = node.predecessor();
                if (p == head) {
                    int r = tryAcquireShared(arg);
                    if (r >= 0) {
                        setHeadAndPropagate(node, r);
                        p.next = null; // help GC
                        failed = false;
                        return true;
                    }
                }
                nanosTimeout = deadline - System.nanoTime();
                if (nanosTimeout <= 0L) {
                    return false;
                }
                if (shouldParkAfterFailedAcquire(p, node) && nanosTimeout > spinForTimeoutThreshold) {
                    LockSupport.parkNanos(this, nanosTimeout);
                }
                if (Thread.interrupted()) {
                    throw new InterruptedException();
                }
            }
        } finally {
            if (failed) {
                cancelAcquire(node);
            }
        }
    }

    // Main exported methods

    /**
     * {@link Lock#tryLock()} 等同于该类,获取锁的逻辑需要自己实现
     */
    protected boolean tryAcquire(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean tryRelease(int arg) {
        throw new UnsupportedOperationException();
    }

    protected int tryAcquireShared(int arg) {
        throw new UnsupportedOperationException();
    }

    protected boolean tryReleaseShared(int arg) {
        throw new UnsupportedOperationException();
    }


    protected boolean isHeldExclusively() {
        throw new UnsupportedOperationException();
    }

    /**
     * 1. 调用tryAcquire()获取锁,若成功则返回
     * 2. 获取失败后,将当前线程加入等待队列中
     * 3. 该方法不会对中断做处理
     */
    public final void acquire(int arg) {
        if (!tryAcquire(arg) && acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
            //acquireQueued() 方法会取消线程的中断状态, 通过返回值来表示是否存在中断
            selfInterrupt(); //恢复中断
        }
    }

    /**
     * 获取锁,但是会响应中断
     */
    public final void acquireInterruptibly(int arg) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        if (!tryAcquire(arg)) {
            doAcquireInterruptibly(arg);
        }
    }

    /**
     * 尝试指定时间获取锁
     */
    public final boolean tryAcquireNanos(int arg, long nanosTimeout) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return tryAcquire(arg) || doAcquireNanos(arg, nanosTimeout); //指定时间内尝试获取
    }

    /**
     * 清空锁状态并判断后继是否存在需要唤醒的线程
     */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {
            Node h = head;
            //判断后面有没有需要唤醒的线程,head节点的waitStatus默认值为0,只有当后继存在阻塞节点时才会设置该值为1
            if (h != null && h.waitStatus != 0) {
                unparkSuccessor(h);
            }
            return true;
        }
        return false;
    }


    public final void acquireShared(int arg) {
        if (tryAcquireShared(arg) < 0) {
            doAcquireShared(arg);
        }
    }


    public final void acquireSharedInterruptibly(int arg) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        if (tryAcquireShared(arg) < 0) {
            doAcquireSharedInterruptibly(arg);
        }
    }


    public final boolean tryAcquireSharedNanos(int arg, long nanosTimeout) throws InterruptedException {
        if (Thread.interrupted()) {
            throw new InterruptedException();
        }
        return tryAcquireShared(arg) >= 0 || doAcquireSharedNanos(arg, nanosTimeout);
    }


    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {
            doReleaseShared();
            return true;
        }
        return false;
    }


    /**
     ***********************************************************************************************
     *                                        other                                                *
     ***********************************************************************************************
     */

    public final boolean hasQueuedThreads() {
        return head != tail;
    }

    /**
     * 是否存在过竞争,只有存在竞争时同步队列才会初始化
     */
    public final boolean hasContended() {
        return head != null;
    }

    /**
     * 获取队列中的第一个排队等待的节点
     * head == tail表示没有等待节点
     */
    public final Thread getFirstQueuedThread() {
        // handle only fast path, else relay
        return (head == tail) ? null : fullGetFirstQueuedThread();
    }

    /**
     * 获取队列的第一个元素
     */
    private Thread fullGetFirstQueuedThread() {

        Node h, s;
        Thread st;
        if (((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null) ||
                ((h = head) != null && (s = h.next) != null && s.prev == head && (st = s.thread) != null)) {
            return st;
        }


        //可能头结点没有设置next属性,上述方式没有获取到元素时,从tail遍历
        Node t = tail;
        Thread firstThread = null;
        while (t != null && t != head) {
            Thread tt = t.thread;
            if (tt != null) {
                firstThread = tt;
            }
            t = t.prev;
        }
        return firstThread;
    }

    /**
     * 通过遍历判断当前线程是否存在<p>
     */
    public final boolean isQueued(Thread thread) {
        if (thread == null) {
            throw new NullPointerException();
        }
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread == thread) {
                return true;
            }
        }
        return false;
    }

    /**
     * 第一个等待的节点是否为排他锁
     */
    final boolean apparentlyFirstQueuedIsExclusive() {
        Node h, s;
        return (h = head) != null && (s = h.next) != null && !s.isShared() && s.thread != null;
    }

    /**
     * 判断队列中是否存在等待线程,若存在,判断第一个是否为当前线程
     * tail==head时表示空节点,此时是没有等待线程的
     * tail != head时有两种情况,
     * 情况一: 初始化,节点数为1，h != t && h.next  == null 表示在初始化,此时表示有节点在入队
     * 情况二: 节点数大于1, h != t && s.thread != Thread.currentThread()
     */
    public final boolean hasQueuedPredecessors() {
        Node t = tail;
        Node h = head;
        Node s;
        return h != t && ((s = h.next) == null || s.thread != Thread.currentThread());
    }

    public final int getQueueLength() {
        int n = 0;
        for (Node p = tail; p != null; p = p.prev) {
            if (p.thread != null) {
                ++n;
            }
        }
        return n;
    }

    /**
     * 获取所有等待线程
     */
    public final Collection<Thread> getQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            Thread t = p.thread;
            if (t != null) {
                list.add(t);
            }
        }
        return list;
    }

    /**
     * 获取排他所得所有等待线程
     */
    public final Collection<Thread> getExclusiveQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (!p.isShared()) {
                Thread t = p.thread;
                if (t != null) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    /**
     * 获取共享锁的所有等待线程
     */
    public final Collection<Thread> getSharedQueuedThreads() {
        ArrayList<Thread> list = new ArrayList<Thread>();
        for (Node p = tail; p != null; p = p.prev) {
            if (p.isShared()) {
                Thread t = p.thread;
                if (t != null) {
                    list.add(t);
                }
            }
        }
        return list;
    }

    public String toString() {
        int s = getState();
        String q = hasQueuedThreads() ? "non" : "";
        return super.toString() +
                "[State = " + s + ", " + q + "empty queue]";
    }

    /**
     * 判断该节点是否在sync队列上
     *
     * @param node the node
     * @return true if is reacquiring
     */
    final boolean isOnSyncQueue(Node node) {
        if (node.waitStatus == Node.CONDITION || node.prev == null) {
            return false;
        }
        if (node.next != null) // If has successor, it must be on queue
        {
            return true;
        }
        //从头部遍历可能存在尾节点遍历不到的情况
        return findNodeFromTail(node);
    }

    /**
     * 从尾部遍历某个元素是否存在
     */
    private boolean findNodeFromTail(Node node) {
        Node t = tail;
        for (; ; ) {
            if (t == node) {
                return true;
            }
            if (t == null) {
                return false;
            }
            t = t.prev;
        }
    }

    /**
     * 将condition队列上的该节点转移到sync队列
     */
    final boolean transferForSignal(Node node) {
        if (!compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            return false;
        }

        //状态设置成功,表示应该由本方法唤醒
        Node p = enq(node);
        int ws = p.waitStatus;
        //如同步队列上一个节点为取消或者设置唤醒后继节点失败,直接唤醒当前线程
        if (ws > 0 || !compareAndSetWaitStatus(p, ws, Node.SIGNAL)) {
            LockSupport.unpark(node.thread);
        }
        return true;
    }

    /**
     * 该方法主要保证节点转移到同步队列中
     * 根据等待节点的状态是否为CONDITION判断中断发生在signal()方法前还是方法后
     * 同时需要保证两种中断情况下必须保证节点转移到同步队列
     * 对中断的具体处理也是在同步队列中的节点获取锁之后
     * TODO 其实如果抛出中断异常的节点没必要加入同步队列!!!
     */
    final boolean transferAfterCancelledWait(Node node) {
        //该节点WaitStatus未发生变化,说明不是signal唤醒的,设置节点状态并转移元素
        //可能是中断唤醒,也可能是达到制定等待时长,都必须将节点转移至同步队列
        if (compareAndSetWaitStatus(node, Node.CONDITION, 0)) {
            enq(node);
            return true;
        }

        //该节点WaitStatus发生变化,说明是被signal唤醒的,检查节点是否已转移到sync队列中
        //没有的话让出CPU,直至转移到,因为转移和唤醒由两个线程执行,不确定是否一定转移完成
        while (!isOnSyncQueue(node)) {
            Thread.yield();
        }
        return false;
    }

    /**
     * 释放当前节点全部的锁,并返回state的值
     * <p>
     * 该方法只用于Condition的实现类,可能存在释放锁失败的情况(均和开发者自定义实现有关):
     * 情况一：在没有获取锁的情况下调用,比如自定义tryRelease()方法抛出异常
     * 情况二：state变量的至不等于0
     * <p>
     * 释放锁失败时,需要设置节点为CANCELLED状态
     */
    final int fullyRelease(Node node) {
        boolean failed = true;
        try {
            int savedState = getState();
            if (release(savedState)) {
                failed = false;
                return savedState;
            } else {
                throw new IllegalMonitorStateException();
            }
        } finally {
            if (failed) {
                node.waitStatus = Node.CANCELLED;
            }
        }
    }

    /**
     * 判断当前调用类是否为condition所在的外部类
     */
    public final boolean owns(ConditionObject condition) {
        return condition.isOwnedBy(this);
    }

    public final boolean hasWaiters(ConditionObject condition) {
        if (!owns(condition)) {
            throw new IllegalArgumentException("Not owner");
        }
        return condition.hasWaiters();
    }

    public final int getWaitQueueLength(ConditionObject condition) {
        if (!owns(condition)) {
            throw new IllegalArgumentException("Not owner");
        }
        return condition.getWaitQueueLength();
    }

    public final Collection<Thread> getWaitingThreads(ConditionObject condition) {
        if (!owns(condition)) {
            throw new IllegalArgumentException("Not owner");
        }
        return condition.getWaitingThreads();
    }

    /**
     * 该类中的所有方法是在获取锁之后才会被执行的,所以不存在线程安全问题
     * <p>
     * 该类中的节点的状态只存在三种
     * 第一种：CONDITION:初始节点的状态,表示节点处于等待状态,未被唤醒
     * 第二种：CANCELLED:释放锁失败,节点被取消,参考fullyRelease()方法说明
     * 第三种：0:表示节点被唤醒,已经转移或正在转移至同步队列
     * <p>
     * <p>
     * 节点在等待状态唤醒之后必须进入同步队列再次获取锁
     */
    public class ConditionObject implements Condition, java.io.Serializable {

        private static final long serialVersionUID = 1173984872572414699L;
        /**
         * 等待队列头结点
         */
        private transient Node firstWaiter;
        /**
         * 等待队列尾节点
         */
        private transient Node lastWaiter;

        public ConditionObject() {
        }


        /**
         * 构建当前线程等待节点并添加至链表尾部
         */
        private Node addConditionWaiter() {
            Node t = lastWaiter;
            if (t != null && t.waitStatus != Node.CONDITION) {
                unlinkCancelledWaiters();
                t = lastWaiter;
            }
            Node node = new Node(Thread.currentThread(), Node.CONDITION);
            if (t == null) {
                firstWaiter = node;
            } else {
                t.nextWaiter = node;
            }
            lastWaiter = node;
            return node;
        }

        /**
         * 唤醒队列中的第一个节点
         * 如果唤醒第一个节点失败,则唤醒下一个节点
         */
        private void doSignal(Node first) {
            do {
                if ((firstWaiter = first.nextWaiter) == null) {  //链表元素循遍历至最后一个节点
                    lastWaiter = null;
                }
                first.nextWaiter = null;
            } while (!transferForSignal(first) && (first = firstWaiter) != null);  //如果唤醒失败且下一个元素不为空,继续循环唤醒下一个
        }

        /**
         * 唤醒队列中的所有节点
         */
        private void doSignalAll(Node first) {
            lastWaiter = firstWaiter = null;
            do {
                Node next = first.nextWaiter;
                first.nextWaiter = null;
                transferForSignal(first);
                first = next;
            } while (first != null);
        }

        /**
         * 删除单项链表中状态不为CONDITION的节点
         * <p>
         * 算法：单向链表删除指定条件元素
         */
        private void unlinkCancelledWaiters() {
            Node t = firstWaiter;
            //因为是单向链表删除,需要保留前一个节点的信息
            Node trail = null;
            while (t != null) {
                Node next = t.nextWaiter;
                if (t.waitStatus != Node.CONDITION) {
                    t.nextWaiter = null; //help GC
                    if (trail == null) {
                        firstWaiter = next;
                    } else {
                        trail.nextWaiter = next;
                    }
                    if (next == null) {
                        lastWaiter = trail;
                    }
                } else {
                    trail = t;
                }
                t = next;
            }
        }

        // public methods

        /**
         * 唤醒一个等待线程
         */
        public final void signal() {
            if (!isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            Node first = firstWaiter;
            if (first != null) {
                doSignal(first);
            }
        }

        /**
         * 唤醒全部等待线程
         */
        public final void signalAll() {
            if (!isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            Node first = firstWaiter;
            if (first != null) {
                doSignalAll(first);
            }
        }

        /**
         * 等待直到被signal()方法唤醒(该方法是不响应中断的)
         */
        public final void awaitUninterruptibly() {
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean interrupted = false;
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if (Thread.interrupted()) {
                    interrupted = true;
                }
            }
            //TODO 这里的关系应该是&&而不是||, 后期JDK已修复
            if (acquireQueued(node, savedState) || interrupted) {
                selfInterrupt();
            }
        }

        /**
         * 退出时对中断的处理为抛出异常,中断发生于signal()调用后的处理方式
         */
        private static final int REINTERRUPT = 1;
        /**
         * 退出时对中断的处理为抛出异常,中断发生于signal()调用前的处理方式
         */
        private static final int THROW_IE = -1;

        /**
         * 判断线程是被正常唤醒还是中断唤醒,正常唤醒为0
         */
        private int checkInterruptWhileWaiting(Node node) {
            return Thread.interrupted() ? (transferAfterCancelledWait(node) ? THROW_IE : REINTERRUPT) : 0;
        }

        /**
         * 根据中断模式选择抛出异常还是重新设置中断
         */
        private void reportInterruptAfterWait(int interruptMode) throws InterruptedException {
            if (interruptMode == THROW_IE) {
                throw new InterruptedException();
            } else if (interruptMode == REINTERRUPT) {
                selfInterrupt();
            }
        }

        /**
         * 线程进入等待状态
         */
        public final void await() throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            int interruptMode = 0;
            /**
             * 调用await()方法进行等待的线程可能会立即被signal()方法转移到同步队列,此时是不会调用LockSupport.park(this)进行阻塞的
             *
             * 当通过LockSupport.park(this)阻塞后,有两种方式唤醒,
             * 方式一：通过sginal()方法唤醒,此时节点已经或正在转移至同步队列
             * 方式二: 通过Thread.currentThread().interrupt()唤醒,此时节点未转移到同步队列
             */
            while (!isOnSyncQueue(node)) {
                LockSupport.park(this);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) { //发生中断则退出等待
                    break;
                }
            }
            //TODO 该处对中断的处理不正确,即使未发生中断也会被设置一个中断状态
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
                interruptMode = REINTERRUPT;
            }
            if (node.nextWaiter != null) {
                unlinkCancelledWaiters();
            }
            if (interruptMode != 0) { //如果发生了中断,对中断进行处理
                reportInterruptAfterWait(interruptMode);
            }
        }

        /**
         * 等待指定时长
         *  TODO jdk1.8对异常的处理存在问题
         */
        public final long awaitNanos(long nanosTimeout) throws InterruptedException {
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold) {
                    LockSupport.parkNanos(this, nanosTimeout);
                }
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
                    break;
                }
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
                interruptMode = REINTERRUPT;
            }
            if (node.nextWaiter != null) {
                unlinkCancelledWaiters();
            }
            if (interruptMode != 0) {
                reportInterruptAfterWait(interruptMode);
            }
            return deadline - System.nanoTime();
        }

        public final boolean awaitUntil(Date deadline) throws InterruptedException {
            long abstime = deadline.getTime();
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (System.currentTimeMillis() > abstime) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                LockSupport.parkUntil(this, abstime);
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
                    break;
                }
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
                interruptMode = REINTERRUPT;
            }
            if (node.nextWaiter != null) {
                unlinkCancelledWaiters();
            }
            if (interruptMode != 0) {
                reportInterruptAfterWait(interruptMode);
            }
            return !timedout;
        }

        public final boolean await(long time, TimeUnit unit)
                throws InterruptedException {
            long nanosTimeout = unit.toNanos(time);
            if (Thread.interrupted()) {
                throw new InterruptedException();
            }
            Node node = addConditionWaiter();
            int savedState = fullyRelease(node);
            final long deadline = System.nanoTime() + nanosTimeout;
            boolean timedout = false;
            int interruptMode = 0;
            while (!isOnSyncQueue(node)) {
                if (nanosTimeout <= 0L) {
                    timedout = transferAfterCancelledWait(node);
                    break;
                }
                if (nanosTimeout >= spinForTimeoutThreshold) {
                    LockSupport.parkNanos(this, nanosTimeout);
                }
                if ((interruptMode = checkInterruptWhileWaiting(node)) != 0) {
                    break;
                }
                nanosTimeout = deadline - System.nanoTime();
            }
            if (acquireQueued(node, savedState) && interruptMode != THROW_IE) {
                interruptMode = REINTERRUPT;
            }
            if (node.nextWaiter != null) {
                unlinkCancelledWaiters();
            }
            if (interruptMode != 0) {
                reportInterruptAfterWait(interruptMode);
            }
            return !timedout;
        }

        //  提供了有关监控指标的一些方法

        final boolean isOwnedBy(AbstractQueuedSynchronizer sync) {
            return sync == AbstractQueuedSynchronizer.this;
        }

        /**
         * 是否存在有效等待者
         */
        protected final boolean hasWaiters() {
            if (!isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 获取有效等待者的数量
         */
        protected final int getWaitQueueLength() {
            if (!isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            int n = 0;
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    ++n;
                }
            }
            return n;
        }

        /**
         * 获取有效等待者的线程集合
         */
        protected final Collection<Thread> getWaitingThreads() {
            if (!isHeldExclusively()) {
                throw new IllegalMonitorStateException();
            }
            ArrayList<Thread> list = new ArrayList<Thread>();
            for (Node w = firstWaiter; w != null; w = w.nextWaiter) {
                if (w.waitStatus == Node.CONDITION) {
                    Thread t = w.thread;
                    if (t != null) {
                        list.add(t);
                    }
                }
            }
            return list;
        }
    }

    private static final Unsafe unsafe = Unsafe.getUnsafe();
    private static final long stateOffset;
    private static final long headOffset;
    private static final long tailOffset;
    private static final long waitStatusOffset;
    private static final long nextOffset;

    static {
        try {
            stateOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("state"));
            headOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("head"));
            tailOffset = unsafe.objectFieldOffset(AbstractQueuedSynchronizer.class.getDeclaredField("tail"));
            waitStatusOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("waitStatus"));
            nextOffset = unsafe.objectFieldOffset(Node.class.getDeclaredField("next"));
        } catch (Exception ex) {
            throw new Error(ex);
        }
    }

    private final boolean compareAndSetHead(Node update) {
        return unsafe.compareAndSwapObject(this, headOffset, null, update);
    }

    private final boolean compareAndSetTail(Node expect, Node update) {
        return unsafe.compareAndSwapObject(this, tailOffset, expect, update);
    }

    private static final boolean compareAndSetWaitStatus(Node node, int expect, int update) {
        return unsafe.compareAndSwapInt(node, waitStatusOffset, expect, update);
    }

    private static final boolean compareAndSetNext(Node node, Node expect, Node update) {
        return unsafe.compareAndSwapObject(node, nextOffset, expect, update);
    }
}
