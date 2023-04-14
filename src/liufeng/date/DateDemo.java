package liufeng.date;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;

/**
 * @author liufeng
 * @date 2021/12/29
 */
public class DateDemo {

    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = now.plusHours(1);
        Duration between = Duration.between(now, end);
        long seconds = between.getSeconds();
        System.out.println(seconds);
    }
}
