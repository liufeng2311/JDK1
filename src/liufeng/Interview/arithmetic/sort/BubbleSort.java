package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/5
 * @desc 冒泡排序
 * @desc 时间复杂度(比较次数)   O(n^2/2)     排序中第一个元素比较N次,第二个元素比较N-1次,第三个元素比较N-2次,直到最后一个元素比较0次
 * @desc 空间复杂度(额外空间)   O(1)         排序过程中没有创建新的数组对象
 * @desc 缺点：每次排序都可能进行位置交换
 */
public class BubbleSort {

  /**
   * 冒泡排序
   *
   * @param arr
   */
  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    int length = copy.length; //集合中未排序的元素长度
    while (length > 1) {
      for (int i = 1; i < length; i++) {
        if (copy[i - 1] > copy[i]) {
          swap(copy, i - 1, i);
        }
      }
      length--; //完成对一个元素的排序,未排序集合数减一
    }
    long end = System.currentTimeMillis();
    System.out.println("冒泡排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(copy));
    }
  }

  /**
   * 数组元素交换
   */
  private static void swap(Integer[] arr, int i, int j) {
    Integer x = arr[i];
    arr[i] = arr[j];
    arr[j] = x;
  }

}
