package liufeng.Interview.arithmetic.tree;

/**
 * @Author: liufeng
 * @Date: 2021/2/25
 * @desc 树节点数量、树高度、
 */
public class TreeThree {

  //树节点的数量
  public int num(Tree tree) {
    if (tree == null) {
      return 0;
    }

    return 1 + num(tree.left) + num(tree.right);
  }


  //树节点的深度
  public int depth(Tree tree) {
    if (tree == null) {
      return 0;
    }
    return 1 + Math.max(depth(tree.left), depth(tree.right));
  }

}
