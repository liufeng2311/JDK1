package liufeng.map.safe;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: liufeng
 * @Date: 2020/11/24
 * @desc ConcurrentHashMap源码解读
 */
public class ConcurrentHashMapDemo {

  public static void main(String[] args) {

    ConcurrentHashMap map = new ConcurrentHashMap();
    map.put("hello", "concurrentHashMap");
    map.get("hello");
    map.remove("");
    map.forEach((o, o2) -> {

    });
  }
}
