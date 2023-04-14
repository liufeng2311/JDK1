package liufeng.aqs;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: liufeng
 * @Date: 2020/11/26
 * @desc  AQS框架
 */
public class AbstractQueuedSynchronizerDemo extends AbstractQueuedSynchronizer {

  public static void main(String[] args) throws InterruptedException {

    LockSupport.park();
    ReentrantLock nonFair = new ReentrantLock();  //默认非公平锁
    nonFair.lock();
    nonFair.tryLock();
    nonFair.tryLock(100L,TimeUnit.SECONDS);
    nonFair.lockInterruptibly();
    nonFair.unlock();



    Condition condition = nonFair.newCondition();
    condition.await();
    condition.signal();
    condition.awaitUninterruptibly();
  }
}
