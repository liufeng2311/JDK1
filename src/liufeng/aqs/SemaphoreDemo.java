package liufeng.aqs;

import java.util.concurrent.Semaphore;

/**
 * @Author: liufeng
 * @Date: 2020/11/27
 * @desc
 */
public class SemaphoreDemo {

  public static void main(String[] args) throws InterruptedException {
    Semaphore semaphore = new Semaphore(3);
    semaphore.acquire(4);
    semaphore.release();
  }
}
