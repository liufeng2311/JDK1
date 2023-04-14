package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/8
 * @desc 插入排序
 * @desc 时间复杂度(比较次数)   最差O(n^2/2)  最优O(n)     排序中第一个元素比较N次,第二个元素比较N-1次,第三个元素比较N-2次,直到最后一个元素比较0次
 * @desc 空间复杂度(额外空间)   O(1)                       排序过程中没有创建新的数组对象
 * @desc 优点 插入排序总是在往有序的集合中插入元素,所以当前元素只需要找到适合的位置,而不必要和有序集合中的所有元素进行比较,所以优于选择排序
 */
public class InsertSort {

  /**
   * 插入排序
   *
   * @param arr
   */
  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    int length = copy.length, currentIndex = 1;  //从索引为1的位置开始执行插入排序
    while (currentIndex < length) {
      int current = copy[currentIndex], i;
      for (i = currentIndex - 1; i >= 0 && current < copy[i]; i--) {
        copy[i + 1] = copy[i];
      }
      copy[i + 1] = current;
      currentIndex++;
    }
    long end = System.currentTimeMillis();
    System.out.println("插入排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(copy));
    }
  }

}
