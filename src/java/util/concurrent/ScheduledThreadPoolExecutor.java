package java.util.concurrent;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import java.util.AbstractQueue;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author liufeng15126
 */
public class ScheduledThreadPoolExecutor extends ThreadPoolExecutor implements ScheduledExecutorService {

  /**
   *  当线程池的状态是SHUTDOWN时,是否继续执行周期性任务
   */
  private volatile boolean continueExistingPeriodicTasksAfterShutdown;

  /**
   *  当线程池的状态是SHUTDOWN时,是否继续执行延迟任务
   */
  private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

  /**
   * 是否移除取消状态的任务
   */
  private volatile boolean removeOnCancel = false;

  /**
   * 会为每一个定时任务生成一个不重复的序列号,用于排队比较
   */
  private static final AtomicLong sequencer = new AtomicLong();

  /**
   * 当前纳秒
   */
  final long now() {
    return System.nanoTime();
  }


  private class ScheduledFutureTask<V> extends FutureTask<V> implements RunnableScheduledFuture<V> {

    /**
     * 定时任务序列号
     */
    private final long sequenceNumber;

    /**
     * 定义多久后开始执行第一次任务
     */
    private long time;

    /**
     * 周期性任务时间间隔
     */
    private final long period;

    /**
     * 保留自身信息,用于设置下次执行任务
     */
    RunnableScheduledFuture<V> outerTask = this;

    /**
     * 该任务在队列中的索引值
     */
    int heapIndex;

    ScheduledFutureTask(Runnable r, V result, long ns) {
      super(r, result);
      this.time = ns;
      this.period = 0;
      this.sequenceNumber = sequencer.getAndIncrement();
    }

    ScheduledFutureTask(Runnable r, V result, long ns, long period) {
      super(r, result);
      this.time = ns;
      this.period = period;
      this.sequenceNumber = sequencer.getAndIncrement();
    }

    ScheduledFutureTask(Callable<V> callable, long ns) {
      super(callable);
      this.time = ns;
      this.period = 0;
      this.sequenceNumber = sequencer.getAndIncrement();
    }

    /**
     * 具体执行的时间
     */
    public long getDelay(TimeUnit unit) {
      return unit.convert(time - now(), NANOSECONDS);
    }

    public int compareTo(Delayed other) {
        if (other == this)
        {
            return 0;
        }
      if (other instanceof ScheduledFutureTask) {
        ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
        long diff = time - x.time;
          if (diff < 0) {
              return -1;
          } else if (diff > 0) {
              return 1;
          } else if (sequenceNumber < x.sequenceNumber) {
              return -1;
          } else {
              return 1;
          }
      }
      long diff = getDelay(NANOSECONDS) - other.getDelay(NANOSECONDS);
      return (diff < 0) ? -1 : (diff > 0) ? 1 : 0;
    }

    /**
     * 任务是否为循环执行
     */
    public boolean isPeriodic() {
      return period != 0;
    }

    /**
     * 下次执行的时间,   分为方法开始前计时和方法开始后计时
     */
    private void setNextRunTime() {
      long p = period;
        if (p > 0) {
            time += p;
        } else {
            time = triggerTime(-p);  //当p小于零时,-p为整数,当前时间+周期
        }
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
      boolean cancelled = super.cancel(mayInterruptIfRunning);
        if (cancelled && removeOnCancel && heapIndex >= 0) {
            remove(this);
        }
      return cancelled;
    }

    /**
     * 线程执行逻辑,分为周期性任务和非周期性任务
     */
    public void run() {
      boolean periodic = isPeriodic();  //判断是否为周期性调用
        if (!canRunInCurrentRunState(periodic)) {
            cancel(false);
        } else if (!periodic) {
            ScheduledFutureTask.super.run();
        } else if (ScheduledFutureTask.super.runAndReset()) {
            setNextRunTime();
            reExecutePeriodic(outerTask);
        }
    }
  }

  /**
   * 在shutdown状态下是否可以执行 延迟任务不会执行,循环任务会
   */
  boolean canRunInCurrentRunState(boolean periodic) {
    return isRunningOrShutdown(periodic ? continueExistingPeriodicTasksAfterShutdown : executeExistingDelayedTasksAfterShutdown);
  }

  /**
   * 延迟任务执行逻辑
   */
  private void delayedExecute(RunnableScheduledFuture<?> task) {
      if (isShutdown()) {
          reject(task);
      } else {
          super.getQueue().add(task);
          if (isShutdown() && !canRunInCurrentRunState(task.isPeriodic()) && remove(task)) {
              task.cancel(false);
          } else {
              ensurePrestart();
          }
      }
  }

  /**
   * 当前周期性任务执行完成后,添加下一次任务
   */
  void reExecutePeriodic(RunnableScheduledFuture<?> task) {
    if (canRunInCurrentRunState(true)) {
      super.getQueue().add(task);
        if (!canRunInCurrentRunState(true) && remove(task)) {
            task.cancel(false);
        } else {
            ensurePrestart();
        }
    }
  }

  /**
   * 关闭时判断延迟任务和定时任务是否还需要执行,不需要取消相关任务
   */
  void onShutdown() {
    BlockingQueue<Runnable> q = super.getQueue();
    boolean keepDelayed =
        getExecuteExistingDelayedTasksAfterShutdownPolicy();
    boolean keepPeriodic =
        getContinueExistingPeriodicTasksAfterShutdownPolicy();
    if (!keepDelayed && !keepPeriodic) {
        for (Object e : q.toArray()) {
            if (e instanceof RunnableScheduledFuture<?>) {
                ((RunnableScheduledFuture<?>) e).cancel(false);
            }
        }
      q.clear();
    } else {
      // Traverse snapshot to avoid iterator exceptions
      for (Object e : q.toArray()) {
        if (e instanceof RunnableScheduledFuture) {
          RunnableScheduledFuture<?> t =
              (RunnableScheduledFuture<?>) e;
          if ((t.isPeriodic() ? !keepPeriodic : !keepDelayed) ||
              t.isCancelled()) { // also remove if already cancelled
              if (q.remove(t)) {
                  t.cancel(false);
              }
          }
        }
      }
    }
    tryTerminate();
  }

  protected <V> RunnableScheduledFuture<V> decorateTask(Runnable runnable, RunnableScheduledFuture<V> task) {
    return task;
  }

  protected <V> RunnableScheduledFuture<V> decorateTask(Callable<V> callable, RunnableScheduledFuture<V> task) {
    return task;
  }

  /**
   * 定时任务的最大线程数为Integer.MAX_VALUE,回收时间均为0
   * @param corePoolSize
   */
  public ScheduledThreadPoolExecutor(int corePoolSize) {
    super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue());
  }

  public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
    super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory);
  }

  public ScheduledThreadPoolExecutor(int corePoolSize, RejectedExecutionHandler handler) {
    super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), handler);
  }

  public ScheduledThreadPoolExecutor(int corePoolSize, ThreadFactory threadFactory, RejectedExecutionHandler handler) {
    super(corePoolSize, Integer.MAX_VALUE, 0, NANOSECONDS, new DelayedWorkQueue(), threadFactory, handler);
  }

  /**
   * 返回触发的延迟时间,纳秒
   */
  private long triggerTime(long delay, TimeUnit unit) {
    return triggerTime(unit.toNanos((delay < 0) ? 0 : delay));
  }

  /**
   * 触发任务的时间戳,理解为当前时间+延迟时间即可
   */
  long triggerTime(long delay) {
    return now() + ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
  }


  /**
   * 主要防止compare方法中的time比较溢出,
   * 保证两个时间差在Long.MAX_VALUE内
   * 如果头节点-Long.MAX_VALUE/2, 当前节点大于Long.MAX_VALUE/2,compare方法中因为溢出Long.MAX_VALUE而变为负数，导致出错
   * @param delay
   * @return
   */
  private long overflowFree(long delay) {
    Delayed head = (Delayed) super.getQueue().peek();
    if (head != null) {
      long headDelay = head.getDelay(NANOSECONDS);
        if (headDelay < 0 && (delay - headDelay < 0)) {
            delay = Long.MAX_VALUE + headDelay;
        }
    }
    return delay;
  }

  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
      if (command == null || unit == null) {
          throw new NullPointerException();
      }
    RunnableScheduledFuture<?> t = decorateTask(command, new ScheduledFutureTask<Void>(command, null, triggerTime(delay, unit)));
    delayedExecute(t);
    return t;
  }

  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
      if (callable == null || unit == null) {
          throw new NullPointerException();
      }
    RunnableScheduledFuture<V> t = decorateTask(callable, new ScheduledFutureTask<V>(callable, triggerTime(delay, unit)));
    delayedExecute(t);
    return t;
  }

  public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
    ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit), unit.toNanos(period));
    RunnableScheduledFuture<Void> t = decorateTask(command, sft);
    sft.outerTask = t;
    delayedExecute(t);
    return t;
  }

  public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
    ScheduledFutureTask<Void> sft = new ScheduledFutureTask<Void>(command, null, triggerTime(initialDelay, unit), unit.toNanos(-delay));
    RunnableScheduledFuture<Void> t = decorateTask(command, sft);
    sft.outerTask = t;
    delayedExecute(t);
    return t;
  }

  public void execute(Runnable command) {
    schedule(command, 0, NANOSECONDS);  //无延迟时间,表示立即执行
  }

  public Future<?> submit(Runnable task) {
    return schedule(task, 0, NANOSECONDS);
  }

  public <T> Future<T> submit(Runnable task, T result) {
    return schedule(Executors.callable(task, result), 0, NANOSECONDS);
  }

  public <T> Future<T> submit(Callable<T> task) {
    return schedule(task, 0, NANOSECONDS);
  }

  public void setContinueExistingPeriodicTasksAfterShutdownPolicy(boolean value) {
    continueExistingPeriodicTasksAfterShutdown = value;
      if (!value && isShutdown()) {
          onShutdown();
      }
  }

  public boolean getContinueExistingPeriodicTasksAfterShutdownPolicy() {
    return continueExistingPeriodicTasksAfterShutdown;
  }

  public void setExecuteExistingDelayedTasksAfterShutdownPolicy(boolean value) {
    executeExistingDelayedTasksAfterShutdown = value;
      if (!value && isShutdown()) {
          onShutdown();
      }
  }

  public boolean getExecuteExistingDelayedTasksAfterShutdownPolicy() {
    return executeExistingDelayedTasksAfterShutdown;
  }

  public void setRemoveOnCancelPolicy(boolean value) {
    removeOnCancel = value;
  }

  public boolean getRemoveOnCancelPolicy() {
    return removeOnCancel;
  }

  public void shutdown() {
    super.shutdown();
  }

  public List<Runnable> shutdownNow() {
    return super.shutdownNow();
  }

  public BlockingQueue<Runnable> getQueue() {
    return super.getQueue();
  }

  /**
   * 延迟队列
   */
  static class DelayedWorkQueue extends AbstractQueue<Runnable> implements BlockingQueue<Runnable> {

    private static final int INITIAL_CAPACITY = 16;
    private RunnableScheduledFuture<?>[] queue = new RunnableScheduledFuture<?>[INITIAL_CAPACITY];
    private final ReentrantLock lock = new ReentrantLock();
    private int size = 0;

    /**
     * 等待队列头部任务的线程,线程池中的线程
     * 等进行等待时设置为自己,当添加元素事如果队列头发生改变,设置为空
     * 获取元素的线程通过阻塞前后判断该值是否变化可以判断出队列头是否发生改变
     */
    private Thread leader = null;

    private final Condition available = lock.newCondition();

    /**
     * Sets f's heapIndex if it is a ScheduledFutureTask.
     */
    private void setIndex(RunnableScheduledFuture<?> f, int idx) {
        if (f instanceof ScheduledFutureTask) {
            ((ScheduledFutureTask) f).heapIndex = idx;
        }
    }

    /**
     * 向上遍历,寻找合适的位置
     */
    private void siftUp(int k, RunnableScheduledFuture<?> key) {
      while (k > 0) {
        int parent = (k - 1) >>> 1;
        RunnableScheduledFuture<?> e = queue[parent];
          if (key.compareTo(e) >= 0) {
              break;
          }
        queue[k] = e;
        setIndex(e, k);
        k = parent;
      }
      queue[k] = key;
      setIndex(key, k);
    }

    /**
     * 向下遍历,寻找合适的位置
     */
    private void siftDown(int k, RunnableScheduledFuture<?> key) {
      int half = size >>> 1;
      while (k < half) {
        int child = (k << 1) + 1;
        RunnableScheduledFuture<?> c = queue[child];
        int right = child + 1;
          if (right < size && c.compareTo(queue[right]) > 0) {
              c = queue[child = right];
          }
          if (key.compareTo(c) <= 0) {
              break;
          }
        queue[k] = c;
        setIndex(c, k);
        k = child;
      }
      queue[k] = key;
      setIndex(key, k);
    }

    /**
     * 扩容
     */
    private void grow() {
      int oldCapacity = queue.length;
      int newCapacity = oldCapacity + (oldCapacity >> 1); // grow 50%
        if (newCapacity < 0) // overflow
        {
            newCapacity = Integer.MAX_VALUE;
        }
      queue = Arrays.copyOf(queue, newCapacity);
    }

    /**
     * 查找对象索引值
     * TODO 任务非两种情况应该是为了兼容
     */
    private int indexOf(Object x) {
      if (x != null) {
        if (x instanceof ScheduledFutureTask) {
          int i = ((ScheduledFutureTask) x).heapIndex;
            if (i >= 0 && i < size && queue[i] == x) {
                return i;
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (x.equals(queue[i])) {
                    return i;
                }
            }
        }
      }
      return -1;
    }

    public boolean contains(Object x) {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        return indexOf(x) != -1;
      } finally {
        lock.unlock();
      }
    }

    public boolean remove(Object x) {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        int i = indexOf(x);
          if (i < 0) {
              return false;
          }

        setIndex(queue[i], -1);
        int s = --size;
        RunnableScheduledFuture<?> replacement = queue[s];
        queue[s] = null;
        if (s != i) {
          siftDown(i, replacement);
            if (queue[i] == replacement) {
                siftUp(i, replacement);
            }
        }
        return true;
      } finally {
        lock.unlock();
      }
    }

    public int size() {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        return size;
      } finally {
        lock.unlock();
      }
    }

    public boolean isEmpty() {
      return size() == 0;
    }

    public int remainingCapacity() {
      return Integer.MAX_VALUE;
    }

    public RunnableScheduledFuture<?> peek() {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        return queue[0];
      } finally {
        lock.unlock();
      }
    }

    public boolean offer(Runnable x) {
        if (x == null) {
            throw new NullPointerException();
        }
      RunnableScheduledFuture<?> e = (RunnableScheduledFuture<?>) x;
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        int i = size;
          if (i >= queue.length) {
              grow();
          }
        size = i + 1;   //size永远保存的是真实元素个数+1
        if (i == 0) {
          queue[0] = e;
          setIndex(e, 0);
        } else {
          siftUp(i, e);
        }
        //说明头结点发生了变化,此时通知所有的等待线程
        if (queue[0] == e) {
          leader = null;  //表示头结点没有被线程等待
          available.signal(); //唤醒所有的等待线程,使得头结点被某一个线程监控
        }
      } finally {
        lock.unlock();
      }
      return true;
    }

    //统一调用的是offer()方法
    public void put(Runnable e) {
      offer(e);
    }

    public boolean add(Runnable e) {
      return offer(e);
    }

    public boolean offer(Runnable e, long timeout, TimeUnit unit) {
      return offer(e);
    }

    /**
     * 返回第一个元素后重新构造大顶堆
     */
    private RunnableScheduledFuture<?> finishPoll(RunnableScheduledFuture<?> f) {
      int s = --size;
      RunnableScheduledFuture<?> x = queue[s];
      queue[s] = null;
        if (s != 0) {
            siftDown(0, x);  //把最后一个元素放到table[0]出重新构建
        }
      setIndex(f, -1);
      return f;
    }

    public RunnableScheduledFuture<?> poll() {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        RunnableScheduledFuture<?> first = queue[0];
          if (first == null || first.getDelay(NANOSECONDS) > 0) {
              return null;
          } else {
              return finishPoll(first);
          }
      } finally {
        lock.unlock();
      }
    }

    public RunnableScheduledFuture<?> take() throws InterruptedException {
      final ReentrantLock lock = this.lock;
      lock.lockInterruptibly();
      try {
        for (; ; ) {
          RunnableScheduledFuture<?> first = queue[0];
            if (first == null) {
                available.await(); //
            } else {
                long delay = first.getDelay(NANOSECONDS);
                if (delay <= 0) {
                    return finishPoll(first);
                }
                first = null;
                if (leader != null) {  //表明已经存在线程等待第一个节点的任务了,后续的线程直接进行等待即可
                    available.await();
                } else {
                    Thread thisThread = Thread.currentThread();
                    leader = thisThread;
                    try {
                        available.awaitNanos(delay);
                    } finally {
                        if (leader == thisThread) {  //该条件成立表示头结点的元素未改变,该线程等待了指定时间醒来,如果不成立表示有新的节点成为了头结点
                            leader = null;
                        }
                    }
                }
            }
        }
      } finally {
          if (leader == null && queue[0] != null) {
              available.signal();
          }
        lock.unlock();
      }
    }

    public RunnableScheduledFuture<?> poll(long timeout, TimeUnit unit) throws InterruptedException {
        //等待时间
      long nanos = unit.toNanos(timeout);
      final ReentrantLock lock = this.lock;
      lock.lockInterruptibly();
      try {
        for (; ; ) {
          RunnableScheduledFuture<?> first = queue[0];
          if (first == null) {
              if (nanos <= 0) {
                  return null;
              } else {
                  nanos = available.awaitNanos(nanos);  //返回的是剩余的等待时间
              }
          } else {
            long delay = first.getDelay(NANOSECONDS);  //获取第一个元素的延迟时间
              if (delay <= 0) {
                  return finishPoll(first);
              }
              if (nanos <= 0) {
                  return null;
              }
              first = null; //将first设置为null，当线程等待时，不持有first的引用
              if (nanos < delay || leader != null) {
                  nanos = available.awaitNanos(nanos);
              } else {
                  Thread thisThread = Thread.currentThread();
                  leader = thisThread;
                  try {
                      long timeLeft = available.awaitNanos(delay);
                      nanos -= delay - timeLeft;
                  } finally {
                      if (leader == thisThread) {
                          leader = null;
                      }
                  }
              }
          }
        }
      } finally {
          if (leader == null && queue[0] != null) {
              available.signal();
          }
        lock.unlock();
      }
    }

    public void clear() {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        for (int i = 0; i < size; i++) {
          RunnableScheduledFuture<?> t = queue[i];
          if (t != null) {
            queue[i] = null;
            setIndex(t, -1);   //为什么要设置为-1
          }
        }
        size = 0;
      } finally {
        lock.unlock();
      }
    }

    /**
     *判断数组中的第一个元素的执行时间是否已经到达
     */
    private RunnableScheduledFuture<?> peekExpired() {
      RunnableScheduledFuture<?> first = queue[0];
      return (first == null || first.getDelay(NANOSECONDS) > 0) ?
          null : first;
    }


    //============================================实现了Queue接口中的定义=============================


    public int drainTo(Collection<? super Runnable> c) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        RunnableScheduledFuture<?> first;
        int n = 0;
        while ((first = peekExpired()) != null) {
          c.add(first);   // In this order, in case add() throws.
          finishPoll(first);
          ++n;
        }
        return n;
      } finally {
        lock.unlock();
      }
    }

    public int drainTo(Collection<? super Runnable> c, int maxElements) {
        if (c == null) {
            throw new NullPointerException();
        }
        if (c == this) {
            throw new IllegalArgumentException();
        }
        if (maxElements <= 0) {
            return 0;
        }
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        RunnableScheduledFuture<?> first;
        int n = 0;
        while (n < maxElements && (first = peekExpired()) != null) {
          c.add(first);   // In this order, in case add() throws.
          finishPoll(first);
          ++n;
        }
        return n;
      } finally {
        lock.unlock();
      }
    }

    public Object[] toArray() {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
        return Arrays.copyOf(queue, size, Object[].class);
      } finally {
        lock.unlock();
      }
    }

    public <T> T[] toArray(T[] a) {
      final ReentrantLock lock = this.lock;
      lock.lock();
      try {
          if (a.length < size) {
              return (T[]) Arrays.copyOf(queue, size, a.getClass());
          }
        System.arraycopy(queue, 0, a, 0, size);
          if (a.length > size) {
              a[size] = null;
          }
        return a;
      } finally {
        lock.unlock();
      }
    }

    public Iterator<Runnable> iterator() {
      return new Itr(Arrays.copyOf(queue, size));
    }

    private class Itr implements Iterator<Runnable> {

      final RunnableScheduledFuture<?>[] array;
      int cursor = 0;     // index of next element to return
      int lastRet = -1;   // index of last element, or -1 if no such

      Itr(RunnableScheduledFuture<?>[] array) {
        this.array = array;
      }

      public boolean hasNext() {
        return cursor < array.length;
      }

      public Runnable next() {
          if (cursor >= array.length) {
              throw new NoSuchElementException();
          }
        lastRet = cursor;
        return array[cursor++];
      }

      public void remove() {
        if (lastRet < 0) {
          throw new IllegalStateException();
        }
        DelayedWorkQueue.this.remove(array[lastRet]);
        lastRet = -1;
      }
    }
  }
}
