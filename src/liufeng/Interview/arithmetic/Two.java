package liufeng.Interview.arithmetic;

/**
 * @Author: liufeng
 * @Date: 2021/1/12
 * @desc 求树的深度
 */
public class Two {

  Two left;

  Two right;


  public static void main(String[] args) {

  }


  //求树的最高深度
  public static int depth(Two two) {
    if (two == null) {
      return 0;
    }

    int left = depth(two.left) + 1;
    int right = depth(two.right) + 1;
    return left > right ? left : right;
  }


  public Two getLeft() {
    return left;
  }

  public void setLeft(Two left) {
    this.left = left;
  }

  public Two getRight() {
    return right;
  }

  public void setRight(Two right) {
    this.right = right;
  }
}

