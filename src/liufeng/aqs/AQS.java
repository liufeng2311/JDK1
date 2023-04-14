package liufeng.aqs;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class AQS {


    Map map = new ConcurrentSkipListMap();
    ReentrantLock lock = new ReentrantLock();
    CountDownLatch downLatch = new CountDownLatch(10);
    ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
    Semaphore semaphore = new Semaphore(12);
    CyclicBarrier barrier = new CyclicBarrier(12);

    public  void  test() throws InterruptedException, BrokenBarrierException {
        lock.lock();
        lock.unlock();
        lock.tryLock();
        lock.tryLock(5, TimeUnit.SECONDS);
        lock.lockInterruptibly();
        lock.isLocked();
        downLatch.await();
        downLatch.countDown();
        ReentrantReadWriteLock.ReadLock readLock = reentrantReadWriteLock.readLock();
        ReentrantReadWriteLock.WriteLock writeLock = reentrantReadWriteLock.writeLock();
        writeLock.lock();
        readLock.lock();
        barrier.await();
    }
}
