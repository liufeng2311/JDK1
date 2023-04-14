package liufeng.aqs;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * @Author: liufeng
 * @Date: 2020/11/27
 * @desc
 */
public class CyclicBarrierDemo {

  public static void main(String[] args) throws BrokenBarrierException, InterruptedException {
    CyclicBarrier cyclicBarrier = new CyclicBarrier(10);
    cyclicBarrier.await();
  }
}
