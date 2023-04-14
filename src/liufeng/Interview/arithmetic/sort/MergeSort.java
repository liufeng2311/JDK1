package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/5
 * @desc 归并排序(空间换时间)
 */
public class MergeSort {

  public static void sort(Integer[] arr, boolean show) {
    long start = System.currentTimeMillis();
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    sort(arr, copy, 0, arr.length - 1); //第一个参数为src
    long end = System.currentTimeMillis();
    System.out.println("归并排序time :" + (end - start));
    if (show) {
      System.out.println(Arrays.asList(arr));
    }
  }


  private static void sort(Integer[] src, Integer[] temp, int left, int right) {
    //当最后分组为一个时,直接返回该组
    if (left == right) {
      return;
    }
    //当最后分组为二个时,判断是否需要换位
    if (right - left == 1) {
      swap(src, left, right);
      return;
    }

    int i = left, j = right;
    int mid = (i + j) >>> 1;
    sort(temp, src, left, mid); //对临时数组进行排序
    sort(temp, src, mid + 1, right); //对临时数组进行排序

    //将临时数组中的元素合并至数组中
    for (int k = i, p = left, q = mid + 1; k <= right; k++) {
      if (q > right || p <= mid && temp[p] < temp[q]) {
        src[k] = temp[p++];
      } else {
        src[k] = temp[q++];
      }
    }

  }

  private static void swap(Integer[] x, int a, int b) {
    //不需要判断的直接返回
    if (x[a] < x[b]) {
      return;
    }
    Integer t = x[a];
    x[a] = x[b];
    x[b] = t;
  }

}
