package liufeng.Interview.arithmetic.linked;

/**
 * @Author: liufeng
 * @Date: 2021/2/25
 * @desc 判断链表是否有环(双指针法)
 */
public class LinkedOne {

  public static void main(String[] args) {
    Node node = Node.getLinkedNode();
    System.out.println("isLoop(node) = " + isLoop(node));
  }

  public static boolean isLoop(Node node) {
    boolean result = false;

    if (node == null) {
      return false;
    }

    Node fast = node;
    Node fastNext;
    Node slow = node;

    while ((fastNext = fast.next) != null) {
      fast = fastNext.next;
      slow = slow.next;

      if (fast == null) {
        break;
      }
      if (fast == slow) {
        result = true;
      }
    }
    return result;
  }
}
