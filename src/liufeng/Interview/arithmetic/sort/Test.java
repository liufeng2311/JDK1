package liufeng.Interview.arithmetic.sort;

import com.beiming.collection.sort.util.NumsRandomUtils;

/**
 * @Author: liufeng
 * @Date: 2020/10/8
 * @desc
 */
public class Test {

  public static void main(String[] args) {
    Integer[] arr = NumsRandomUtils.randomDistinct(10000, 20000000);
    BubbleSort.sort(arr, false);
    SelectSort.sort(arr, false);
    InsertSort.sort(arr, false);
    ShellSort.sort(arr, false);
    QuickSort.sort(arr, false);
    MergeSort.sort(arr, false);
    HeapSort.sort(arr, false);
    BitSort.sort(arr, false);  //位图排序数据不可以有重复
    CountSort.sort(arr, false);
    RadixSort.sort(arr, false);
  }
}
