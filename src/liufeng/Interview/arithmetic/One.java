package liufeng.Interview.arithmetic;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: liufeng
 * @Date: 2021/1/12
 * @desc 给定一个数组,求某个位置上的元素满足左边的全小于它, 右边的全大于它
 */
public class One {


  public static void main(String[] args) {
    AtomicBoolean bl = new AtomicBoolean();
    bl.set(false);
    bl.getAndSet(true);
  }

}
