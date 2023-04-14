package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/5
 * @desc 选择排序由于冒泡排序, 直至比较最后才进行元素交换
 * @desc 时间复杂度(比较次数)   O(n^2/2)     排序中第一个元素比较N次,第二个元素比较N-1次,第三个元素比较N-2次,直到最后一个元素比较0次
 * @desc 空间复杂度(额外空间)   O(1)         排序过程中没有创建新的数组对象
 * @desc
 */
public class SelectSort {

  /**
   * 选择排序
   *
   * @param arr
   */
  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    int length = copy.length; //集合中未排序的元素长度
    while (length > 1) {
      int highIndex = 0; //记录最大值所在的索引
      for (int i = 1; i < length; i++) {
        if (copy[i] > copy[highIndex]) {
          highIndex = i;
        }
      }
      //--length有两层含义,一个表示数组最后一个索引,用于交换,而是表示完成一次循环,集合长度减一
      swap(copy, --length, highIndex);  //将最大值所在的索引和未排序的最后一位交换,同时待排序集合长度减一
    }
    long end = System.currentTimeMillis();
    System.out.println("选择排序time :" + (end - start));
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
