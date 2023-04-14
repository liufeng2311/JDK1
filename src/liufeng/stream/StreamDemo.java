package liufeng.stream;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stream 的常用操作
 * @author liufeng
 * @date 2021/12/13
 */
public class StreamDemo {

    static List<String> list = new ArrayList<>();

    public static void main(String[] args) {
        Spliterator<String> spliterator = list.spliterator();


        //一、
        List<Integer> collect1 = list.stream().map(s -> s.length()).collect(Collectors.toList());
        //二、ForEach相关操作
        list.stream().map(s -> s.length()).forEach(x -> System.out.println(x));
        //三、Find相关操作
        Optional<Integer> any = list.stream().map(s -> s.length()).findAny();
        //四、Match相关操作
        boolean b = list.stream().map(s -> s.length()).anyMatch(Objects::nonNull);

        list.stream().collect(Collectors.groupingBy(String::length));
    }

}
