package liufeng.Interview.arithmetic.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: liufeng
 * @Date: 2020/10/9
 * @desc 基数排序
 * @desc 第一步寻找最大值, 计算最大值的位数
 * @desc 从低位开始比较, 直至比较到最高位，
 * @desc 每次比较都需要将原数组中的数组存储到十个list中
 * @desc 每次比较完都需要将是个list的数据按该位的大小排列
 */
public class RadixSort {

  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    sort(copy);
    long end = System.currentTimeMillis();
    System.out.println("基数排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(arr));
    }
  }


  public static void sort(Integer[] arr) {
    int length = maxLength(arr);
    List<ArrayList<Integer>> lists = initTempList();
    for (int i = 1, dev = 1; i <= length; i++, dev = 10 * dev) {
      for (int j = 0; j < arr.length; j++) {
        int arrIndex = (arr[j] / dev) % 10;
        lists.get(arrIndex).add(arr[j]);
      }
      //将临时数组中的元素添加至原数组并清空临时数组
      clearTempList(arr, lists);
    }
  }

  /**
   * 初始化临时集合,0-9对应十个集合
   */
  public static List<ArrayList<Integer>> initTempList() {
    List<ArrayList<Integer>> lists = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      ArrayList<Integer> list = new ArrayList();
      lists.add(list);
    }
    return lists;
  }

  /**
   * 将临时数组中的元素添加至原数组并清空临时数组
   */
  public static void clearTempList(Integer[] arr, List<ArrayList<Integer>> lists) {
    int i = 0;
    for (List<Integer> var : lists) {
      for (Integer temp : var) {
        arr[i++] = temp;
      }
      var.clear();
    }
  }

  /**
   * 求出最大值存在几位
   */
  private static int maxLength(Integer[] arr) {
    int max = arr[0];
    for (int i = 1; i < arr.length; i++) {
      if (arr[i] > max) {
        max = arr[i];
      }
    }
    return String.valueOf(max).length();
  }

}
