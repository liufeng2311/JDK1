package liufeng.Interview.arithmetic;

import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @Author: liufeng
 * @Date: 2021/1/26
 * @desc 实现一个定时线程池
 */
public class DelayThreadPool {


  public static void main(String[] args) throws InterruptedException {

    //延迟队列中的元素需要实现Delayed接口
    DelayQueue queue = new DelayQueue<DelayedDemo>();

    DelayedDemo demo = new DelayedDemo(() -> System.out.println("3"), 3);
    DelayedDemo demo2 = new DelayedDemo(() -> System.out.println("5"), 5);
    DelayedDemo demo3 = new DelayedDemo(() -> System.out.println("7"), 7);
    add(demo, queue);
    add(demo2, queue);
    add(demo3, queue);

  }


  static void add(DelayedDemo demo, DelayQueue queue) {
    queue.add(demo);
    new Thread(() -> {
      while (true) {
        DelayedDemo take = null;
        try {
          take = (DelayedDemo) queue.take();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        take.getObj().run();
        queue.add(new DelayedDemo(take.getObj(), take.getTimePer()));
      }
    }).start();
  }
}


class DelayedDemo implements Delayed {

  private Runnable obj;

  //过期时间
  private long expireTime;

  //时间间隔
  private long timePer;

  public DelayedDemo(Runnable obj, long expireTime) {
    this.obj = obj;
    this.timePer = expireTime;
    this.expireTime =
        TimeUnit.NANOSECONDS.convert(expireTime, TimeUnit.SECONDS) + System.nanoTime();
  }

  @Override
  public long getDelay(TimeUnit unit) {
    return unit.convert(this.expireTime - System.nanoTime(), TimeUnit.NANOSECONDS);
  }

  @Override
  public int compareTo(Delayed o) {
    DelayedDemo demo = (DelayedDemo) o;
    return demo.expireTime - this.expireTime >= 0 ? -1 : 1;
  }

  public Runnable getObj() {
    return obj;
  }

  public long getExpireTime() {
    return expireTime;
  }

  public long getTimePer() {
    return timePer;
  }
}