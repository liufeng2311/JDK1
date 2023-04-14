package liufeng.list;

import java.util.Vector;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @Author: liufeng
 * @Date: 2020/11/24
 * @desc
 */
public class CopyOnWriteArrayListDemo {

  public static void main(String[] args) {
    CopyOnWriteArrayList demo = new CopyOnWriteArrayList();
    demo.add(11);
    demo.get(1);
  }
}
