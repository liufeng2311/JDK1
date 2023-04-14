package liufeng.Interview.arithmetic.linked;

/**
 * @Author: liufeng
 * @Date: 2021/2/25
 * @desc
 */
public class Node {

  Object obj;

  Node next;

  Node pre;

  public Node(Object obj) {
    this.obj = obj;
  }

  public static Node getLinkedNode() {
    Node node = new Node(1);
    Node node1 = new Node(2);
    Node node2 = new Node(3);
    Node node3 = new Node(4);
    Node node4 = new Node(5);
    Node node5 = new Node(6);
    node.next = node1;
    node1.next = node2;
    node2.next = node3;
    node3.next = node4;
    node4.next = node5;
    return node;
  }
}
