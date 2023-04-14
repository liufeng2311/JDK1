package liufeng.queue;

import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Spliterator;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Author: liufeng
 * @Date: 2020/11/24
 * @desc
 */
public class DelayQueueDemo {

    public static void main(String[] args) throws InterruptedException {

        ReentrantLock lock = new ReentrantLock();
        lock.lock();
        lock.unlock();
        Condition condition = lock.newCondition();
        condition.await();
        DelayQueue delayQueue = new DelayQueue();
        Iterator iterator = delayQueue.iterator();
        Delayed take = delayQueue.take();

        PriorityQueue priorityQueue = new PriorityQueue();
        Spliterator spliterator = priorityQueue.spliterator();
    }
}
