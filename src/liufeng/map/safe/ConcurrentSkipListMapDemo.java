package liufeng.map.safe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * @Author: liufeng
 * @Date: 2020/11/24
 * @desc
 */
public class ConcurrentSkipListMapDemo {

  public static void main(String[] args) {
    ConcurrentSkipListMap map = new ConcurrentSkipListMap();
    ConcurrentHashMap concurrentHashMap = new ConcurrentHashMap();
  }
}
