package liufeng.map;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author: liufeng
 * @Date: 2020/11/24
 * @desc  HashMap
 */
public class HashMapDemo {

  public static void main(String[] args) {
    HashMap map = new HashMap<>();
    ConcurrentHashMap maps = new ConcurrentHashMap();
    map.put("1","2");
    maps.put("1","2");
    map.get("1");
  }
}
