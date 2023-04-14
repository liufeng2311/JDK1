package liufeng.Interview.arithmetic.other;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @Author: liufeng
 * @Date: 2021/3/11
 * @desc 扑克牌算法
 */
public class PlayCard implements Cloneable {

  public PlayCard(String number1, String flower1) {
    this.number1 = number1;
    this.flower1 = flower1;
  }

  String number1;
  String flower1;
  final static List<PlayCard> playCardList = new ArrayList<>();

  static String[] number = new String[]{"A", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J",
      "Q", "K"};
  static String[] flower = new String[]{"黑", "红", "梅", "方"};

  //初始化54张扑克牌元素

  static {
    for (int i = 0; i < number.length; i++) {
      for (int j = 0; j < flower.length; j++) {
        playCardList.add(new PlayCard(number[i], flower[j]));
      }
    }

    PlayCard playCard = new PlayCard("大王", "小王");
    PlayCard playCard1 = new PlayCard("大王", "小王");
    playCardList.add(playCard);
    playCardList.add(playCard1);
  }


  public static List<PlayCard> getPlayCardList() {
    return playCardList;
  }


  public static void main(String[] args) {
    List<PlayCard> playCardList = PlayCard.getPlayCardList();
    Collections.shuffle(playCardList);
  }
}
