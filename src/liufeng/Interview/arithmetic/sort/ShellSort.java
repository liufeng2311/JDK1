package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/8
 * @desc 希尔排序是插入排序的改良版, 一次希尔排序等于多次插入排序(并不意味着希尔排序总是优于插入排序)(当数据较为有序时, 插入排序更快)
 * @desc 插入排序存在的一个弊端就是比较靠后的元素恰好都是较小的元素, 导致大部分数据数组需要向后移动一位
 * @desc 希尔排序就是为了解决上诉问题的, 通过间隔较大的多次插入排序, 使数组排序逐渐变得较为有序，不会出现最小值在最后一位这种极端情况,使得较少次数即可找到最终位置
 */
public class ShellSort {

  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    int current, gap = copy.length / 2;
    while (gap > 0) {
      for (int i = gap; i < copy.length; i++) {
        current = copy[i];
        int j;
        //当需要排序的数小于已排好序列时,将数据后移以为
        for (j = i - gap; j >= 0 && copy[j] > current; j = j - gap) {
          copy[j + gap] = copy[j];
        }
        //在小于当前值的后一位插入当前值
        copy[j + gap] = current;
      }
      gap = gap / 2;
    }
    long end = System.currentTimeMillis();
    System.out.println("希尔排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(copy));
    }
  }

}
