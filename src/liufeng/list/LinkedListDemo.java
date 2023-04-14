package liufeng.list;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: liufeng
 * @Date: 2021/2/14
 * @desc
 */
public class LinkedListDemo {

  public static void main(String[] args) {
    LinkedList linkedList = new LinkedList();
    linkedList.add(123);

    AtomicInteger integer = new AtomicInteger();
    integer.lazySet(12);

    Iterator iterator = linkedList.iterator();
  }
}
