package liufeng.Interview.arithmetic.sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

/**
 * @Author: liufeng
 * @Date: 2020/10/7
 * @desc 位图排序(用每一个Bit表示一个数, 一个int可以表示32个数)
 * @desc 只适用于不重复的、已知最大值的正整数排序
 * @desc 1MB = 1024KB 1KB = 1024Byte 1Byte = 8Bit
 */
public class BitSort {

  //定义需要排序的数的最大值
  private static int MAX = 100000;

  //定义dest数组类型对应的字节数,2^5表示的是32
  private static int GROUP_LENGTH = 5;

  //计算需要多少个元素可表示数组
  private static int[] dest = new int[(MAX >> GROUP_LENGTH) + 1];

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    long end = System.currentTimeMillis();
    System.out.println("time :" + (end - start));
  }

  /**
   * 位图排序
   *
   * @desc 第一步, 计算该数据在数组中的索引位置  当前数据除以32得到的商(i >> 5)
   * @desc 第二步, 计算该数据在位图中的位置      当前数据除以32的余数(1 << i)
   * @desc 1 << 2 等于 1 << 34  当位移数超过32时会自动取余 1 << (34%32) --->1 << 2
   */
  public static void sort(Integer[] arr) {
    for (int i = 0; i < arr.length; i++) {
      set(arrIndex(i), arr[i]);
    }
    System.out.println(get());
  }

  /**
   * 获取元素所在数组的位置
   */
  private static int arrIndex(int i) {
    return i >> GROUP_LENGTH;
  }

  /**
   * 整数转位图(这里没有规定每个位图从左到右一次增加,指定命中一个位置即可,取值时只需要判断映射位置是否为1即可)
   *
   * @param arrIndex 数组索引
   * @param bitIndex 位图索引
   */
  private static void set(int arrIndex, int bitIndex) {
    bitIndex = 1 << bitIndex; //保证除了指定位数是1以外其与全是0
    dest[arrIndex] |= bitIndex;  //通过或运算将该整数表示的位置至1
  }

  /**
   * 位图转整数
   */
  private static List get() {
    List<Integer> integers = new ArrayList<>();
    for (int i = 0; i < MAX; i++) {
      if ((dest[arrIndex(i)] & (1 << i)) != 0) {
        integers.add(i);
      }
    }
    return integers;
  }

  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    Integer[] java = java(copy);
    long end = System.currentTimeMillis();
    System.out.println("位图排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(java));
    }

  }

  /**
   * 获取最大值
   *
   * @param arr
   * @return
   */
  public static int max(Integer[] arr) {
    int max = arr[0];
    for (int i = 1; i < arr.length; i++) {
      if (arr[i] > max) {
        max = arr[i];
      }
    }
    return max;
  }

  /**
   * jdk自带的位图方法
   *
   * @param arr
   */
  private static Integer[] java(Integer[] arr) {
    Integer[] sort = new Integer[arr.length];
    int max = max(arr);
    BitSet bitSet = new BitSet(max);
    for (int i : arr) {
      bitSet.set(i);
    }
    int sortIndex = 0;
    for (int i = 0; i <= max; i++) {
      if (bitSet.get(i)) {
        sort[sortIndex] = i;
        sortIndex++;
      }
    }
    return sort;
  }

}
