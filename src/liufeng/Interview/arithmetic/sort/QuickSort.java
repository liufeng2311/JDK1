package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/5
 * @desc 快速排序
 * @desc 缺点:内存不足会溢出
 */
public class QuickSort {


  public static void main(String[] args) {
    Integer[] arr = new Integer[]{2, 1, 3, 1, 42, 2, 5, 12, 43, 64, 121, 997};
    sort(arr, true);
  }

  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    sort(copy, 0, arr.length - 1); //第一个参数为src
    long end = System.currentTimeMillis();
    System.out.println("快速排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(copy));
    }
  }

  /**
   * 快速排序算法(右侧的哨兵先动)
   *
   * @param arr   需要排序的数组
   * @param left  数组的开始索引
   * @param right 数组的结束索引
   */
  private static void sort(Integer[] arr, int left, int right) {
    //如果左侧索引大于等于右侧索引,表示已经遍历完毕返回即可
    if (left >= right) {
      return;
    }
    //设置数组第一个为基准数
    int base = arr[left];
    int i = left, j = right;
    while (i < j) {
      while (arr[j] >= base && j > i) {
        j--;
      }

      while (arr[i] <= base && j > i) {
        i++;
      }
      if (i < j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
      }
    }

    arr[left] = arr[i];
    arr[i] = base;

    sort(arr, left, i - 1);
    sort(arr, i + 1, right);
  }
}
