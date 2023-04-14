package liufeng.Interview.arithmetic.tree;

import java.util.LinkedList;
import java.util.List;

/**
 * @Author: liufeng
 * @Date: 2021/2/25
 * @desc 前序遍历、中序遍历、后续遍历、层级遍历
 */
public class TreeTwo {

  //前序遍历
  public void pre(Tree tree) {
    if (tree == null) {
      return;
    }
    System.out.print(tree);
    pre(tree.left);
    pre(tree.right);
  }

  //中序遍历
  public void mid(Tree tree) {
    if (tree == null) {
      return;
    }
    pre(tree.left);
    System.out.print(tree);
    pre(tree.right);
  }

  //后续遍历
  public void after(Tree tree) {
    if (tree == null) {
      return;
    }
    pre(tree.left);
    pre(tree.right);
    System.out.print(tree);

  }

  //层级遍历
  public List cell(Tree tree) {
    LinkedList list = new LinkedList<Tree>();
    list.add(tree); //添加root节点到数组中
    for (int i = 0; i < list.size(); i++) {
      Tree tree1 = (Tree) list.get(i);
      if (tree1.left != null) {
        list.add(tree1.left);
      }
      if (tree1.right != null) {
        list.add(tree1.right);
      }
      System.out.print(tree1 + " ");
    }

    return list;
  }


  //按层打印数据
  public List cell1(Tree tree) {
    LinkedList list = new LinkedList<Tree>();
    list.add(tree); //添加root节点到数组中

    int current = 1; //当前层的元素,根节点为1
    int next = 0;
    for (int i = 0; i < list.size(); i++) {
      Tree tree1 = (Tree) list.get(i);
      if (tree1.left != null) {
        list.add(tree1.left);
        next++;   //记录下层元素个数
      }
      if (tree1.right != null) {
        list.add(tree1.right);
        next++;   //记录下层元素个数
      }
      current--;

      System.out.print(tree1 + " ");

      //当前层遍历结束后,开始下一层
      if (current == 0) {
        current = next;
        next = 0;
        System.out.println();
      }

    }

    return list;
  }
}
