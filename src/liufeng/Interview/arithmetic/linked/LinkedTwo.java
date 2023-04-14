package liufeng.Interview.arithmetic.linked;

/**
 * @Author: liufeng
 * @Date: 2021/2/25
 * @desc 单向链表N分之一节点问题
 */
public class LinkedTwo {

  public static void main(String[] args) {
    Node node = Node.getLinkedNode();
    StringBuilder builder = new StringBuilder();
    builder.reverse();
  }

  /**
   * 反转链表
   */
  public static Node reverse(Node node) {

    Node head = null;

    while (node != null) {
      Node next = node.next;
      if (head == null) {
        head = node;
      } else {
        node.next = head;
        head =node;
      }
      node = next;
    }
    return node;
  }


}
