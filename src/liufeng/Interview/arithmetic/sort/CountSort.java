package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/9
 * @desc 计数排序
 * @desc 第一步寻找最小值Max
 * @desc 第二步寻找最大值Mini
 * @desc 第三步创建新数组, 长度为Max-Mini+1
 * <p>
 * select
 */


public class CountSort {

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    long end = System.currentTimeMillis();
    System.out.println("time :" + (end - start));
  }

  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    sort(copy);
    long end = System.currentTimeMillis();
    System.out.println("计数排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(copy));
    }
  }

  //
  public static void sort(Integer[] arr) {
    //当数组长度为0或者1时,不需要进行排序
    if (arr.length < 2) {
      return;
    }

    int[] newArray = newArray(arr);
    int basic = newArray[0];
    newArray[0] = 0;
    for (int i = 0; i < arr.length; i++) {
      int newIndex = arr[i] - basic; //该值在新数组中的索引
      newArray[newIndex] += 1;
    }

    for (int i = 0, j = 0; i < newArray.length; i++) {
      int size = newArray[i];
      while (size > 0) {
        arr[j++] = basic + i;
        size--;
      }
    }

  }

  /**
   * 当数组长度大于1时进行特殊处理 获取数组中最大值和最小值 构建新数组
   *
   * @param arr
   */
  private static int[] newArray(Integer[] arr) {
    int max = arr[0], mini = arr[0];
    for (int i = 1; i < arr.length; i++) {
      if (arr[i] > max) {
        max = arr[i];
      }
      if (arr[i] < mini) {
        mini = arr[i];
      }
    }
    int[] newArray = new int[max - mini + 1];
    newArray[0] = mini;
    return newArray;
  }
}
