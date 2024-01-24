package java.util.concurrent;

import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * CountDownLatch是公平锁的一种实现，
 * 获取锁成功的条件是count=0, 释放锁成功的条件也是count=0,
 *
 *
 * 初始化类时,count>=0, 主线程执行时await()时, 当count>0就会阻塞,
 * 每个线程执行完毕后, count--, 当count=0时, 就会唤醒阻塞的主线程
 *
 */
public class CountDownLatch {
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

      /**
       * 使用的是共享锁实现的,返回的值大于等于零,表示获取锁成功
       *
       * 本方法的实现为, 当count=0时,表示获取锁成功, 否则获取锁失败
       * @param acquires
       * @return
       */
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }


      /**
       * 每个线程执行完毕后,count--,当count=0时, 表示释放锁成功
       * 该方法被多个线程调用, 通过CAS实现线程安全
       * @param releases
       * @return
       */
      protected boolean tryReleaseShared(int releases) {
            for (; ; ) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c - 1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }

    private final Sync sync;



    /**
     ***********************************************************************************************
     *                                        构造方法                                              *
     ***********************************************************************************************
     */

    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

  /**
   * 获取锁
   * @throws InterruptedException
   */
  public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }

  /**
   * 获取锁
   * @param timeout
   * @param unit
   * @return
   * @throws InterruptedException
   */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }


  /**
   * 释放锁
   */
  public void countDown() {
        sync.releaseShared(1);
    }

    public long getCount() {
        return sync.getCount();
    }

    public String toString() {
        return super.toString() + "[Count = " + sync.getCount() + "]";
    }
}
