package liufeng.aqs;

import java.util.concurrent.CountDownLatch;

/**
 * @Author: liufeng
 * @Date: 2020/11/27
 * @desc
 */
public class CountDownLatchDemo {

  public static void main(String[] args) throws InterruptedException {
    CountDownLatch count = new CountDownLatch(10);  //初始化CountDownLatch对象,并设置锁的总数
    count.countDown();  //释放锁
    count.await(); //发现锁不为0,加入队列等待
  }
}
