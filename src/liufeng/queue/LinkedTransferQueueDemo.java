package liufeng.queue;

import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.SynchronousQueue;

/**
 * @Author: liufeng
 * @Date: 2020/11/24
 * @desc
 */
public class LinkedTransferQueueDemo {

  public static void main(String[] args) throws InterruptedException {
    LinkedTransferQueue demo = new LinkedTransferQueue();
    SynchronousQueue demo1 = new SynchronousQueue();
//    demo1.put("1213");
//    demo1.put("11213");
//    demo1.put("123");
    demo.transfer("123");
    demo.transfer("123");
//    demo.add("123");
//    demo.offer("123");
//    demo.put("123");
//    demo.peek();
//    demo.poll();
  }
}
