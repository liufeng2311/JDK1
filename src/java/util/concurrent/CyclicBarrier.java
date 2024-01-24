package java.util.concurrent;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CyclicBarrier {

  /**
   *
   */
  private static class Generation {
    //当前循环是否终止标识, 正常情况下由最后一个线程修改, 标识所有线程已经完成本次循环, 准备好进入下一循环
    boolean broken = false;
  }

  /**
   * 线程间通讯的方式,
   */
  private final ReentrantLock lock = new ReentrantLock();

  private final Condition trip = lock.newCondition();

  /**
   * 总参与者
   */
  private final int parties;

  /**
   * 栅栏破裂
   */
  private final Runnable barrierCommand;

  /**
   * 表示一个批次
   */
  private Generation generation = new Generation();

  /**
   * 此次参与者
   */
  private int count;

  /**
   * 开启下一个迭代
   */
  private void nextGeneration() {
    trip.signalAll();
    count = parties;
    generation = new Generation();
  }

  /**
   * 终止此次迭代
   */
  private void breakBarrier() {
    generation.broken = true;
    count = parties;
    trip.signalAll();
  }

  private int dowait(boolean timed, long nanos) throws InterruptedException, BrokenBarrierException, TimeoutException {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      final Generation g = generation;

      if (g.broken)
        throw new BrokenBarrierException();

      if (Thread.interrupted()) {
        breakBarrier();
        throw new InterruptedException();
      }

      int index = --count;
      if (index == 0) {  // tripped
        boolean ranAction = false;
        try {
          final Runnable command = barrierCommand;
          if (command != null)
            command.run();
          ranAction = true;
          nextGeneration();
          return 0;
        } finally {
          if (!ranAction)
            breakBarrier();
        }
      }

      // loop until tripped, broken, interrupted, or timed out
      for (; ; ) {
        try {
          if (!timed)
            trip.await();
          else if (nanos > 0L)
            nanos = trip.awaitNanos(nanos);
        } catch (InterruptedException ie) {
          if (g == generation && !g.broken) {
            breakBarrier();
            throw ie;
          } else {
            // We're about to finish waiting even if we had not
            // been interrupted, so this interrupt is deemed to
            // "belong" to subsequent execution.
            Thread.currentThread().interrupt();
          }
        }

        if (g.broken)
          throw new BrokenBarrierException();

        if (g != generation)
          return index;

        if (timed && nanos <= 0L) {
          breakBarrier();
          throw new TimeoutException();
        }
      }
    } finally {
      lock.unlock();
    }
  }

  /**
   * **********************************************************************************************
   * 构造方法                                              *
   * **********************************************************************************************
   */

  public CyclicBarrier(int parties, Runnable barrierAction) {
    if (parties <= 0) throw new IllegalArgumentException();
    this.parties = parties;
    this.count = parties;
    this.barrierCommand = barrierAction;
  }

  public CyclicBarrier(int parties) {
    this(parties, null);
  }


  public int getParties() {
    return parties;
  }

  public int await() throws InterruptedException, BrokenBarrierException {
    try {
      return dowait(false, 0L);
    } catch (TimeoutException toe) {
      throw new Error(toe); // cannot happen
    }
  }


  public int await(long timeout, TimeUnit unit) throws InterruptedException, BrokenBarrierException, TimeoutException {
    return dowait(true, unit.toNanos(timeout));
  }

  public boolean isBroken() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return generation.broken;
    } finally {
      lock.unlock();
    }
  }


  public void reset() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      breakBarrier();   // break the current generation
      nextGeneration(); // start a new generation
    } finally {
      lock.unlock();
    }
  }

  /**
   *
   * @return
   */
  public int getNumberWaiting() {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
      return parties - count;
    } finally {
      lock.unlock();
    }
  }
}
