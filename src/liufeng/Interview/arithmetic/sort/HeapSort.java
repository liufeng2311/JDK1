package liufeng.Interview.arithmetic.sort;

import java.util.Arrays;

/**
 * @Author: liufeng
 * @Date: 2020/10/7
 * @desc 堆排序(该排序可以极快的找出排名前N的数据)    堆是对数组的一个抽象
 * @desc 优先级队列就是使用堆排序实现的(PriorityQueue)
 *
 * @TODO 对已有集合构建大顶堆、小顶堆
 * @TODO 向大顶堆、小顶堆中添加元素
 * @TODO 找出一个集合中最大的N个值、最小的N个值
 *
 */
public class HeapSort {

  public static void main(String[] args) {
    long start = System.currentTimeMillis();
    long end = System.currentTimeMillis();
    System.out.println("堆排序time :" + (end - start));
  }

  public static void sort(Integer[] arr, boolean show) {
    Integer[] copy = Arrays.copyOf(arr, arr.length);
    long start = System.currentTimeMillis();
    sort(copy);
    long end = System.currentTimeMillis();
    System.out.println("堆排序time :" + (end - start));
    if(show){
      System.out.println(Arrays.asList(copy));
    }
  }

  public static void sort(Integer[] arr) {
    //1.构建大顶堆(每个数组下只有length/2-1个节点会存在叶子节点)
    for (int i = arr.length / 2 - 1; i >= 0; i--) {
      adjustHeap(arr, i, arr.length);
    }
    //2.调整堆结构+交换堆顶元素与末尾元素
    for (int j = arr.length - 1; j > 0; j--) {
      swap(arr, 0, j);//将堆顶元素与末尾元素进行交换(该步骤是用于排序的)
      adjustHeap(arr, 0, j);//由于之前以构造过大顶堆,所以最大数必然位于1、2、3三个索引中,这里只需要调整索引为0的值的正确位置
    }

  }

  /**
   * 当i不同时,该方法表达的含义不同
   * 构建大顶堆(i = length/2-1)
   * 调整大顶堆(i = 0),每次调整都是从第一个元素开始比较
   *
   */
  public static void adjustHeap(Integer[] arr, int i, int length) {
    int temp = arr[i];
    for (int k = i * 2 + 1; k < length; k = k * 2 + 1) { //调整时需要保证堆特性,必须要保证子节点也要满足特性
      if (k + 1 < length && arr[k] < arr[k + 1]) {
        k++;
      }
      if (arr[k] > temp) { //这里没有直接进行换位,因为换位可能造成子节点不满足特性,通过循环找到最终的位置
        arr[i] = arr[k];
        i = k;   //i表示temp所在的索引
      } else {
        break;
      }
    }
    arr[i] = temp;//将temp值放到最终的位置
  }

  /**
   * 换位
   */
  public static void swap(Integer[] arr, int a, int b) {
    int temp = arr[a];
    arr[a] = arr[b];
    arr[b] = temp;
  }

}
