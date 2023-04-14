package liufeng.Interview.arithmetic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * @Author: liufeng
 * @Date: 2021/1/26
 * @desc
 */
public class SerializableDemo implements Serializable {

  private String name;

  private transient String names;

  private static String namesId = "789";

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    FileOutputStream fileOutputStream = new FileOutputStream("D://test.txt");
    ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
    SerializableDemo demo = new SerializableDemo();
    demo.name = "123";
    demo.names = "456";
    outputStream.writeObject(demo);

    FileInputStream fileInputStream = new FileInputStream("D://test.txt");
    ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
    SerializableDemo demos = (SerializableDemo) inputStream.readObject();
    System.out.println(demos);
  }

  @Override
  public String toString() {
    return "SerializableDemo{" +
        "name='" + name + '\'' +
        ", names='" + names + '\'' +
        ", namesId='" + namesId + '\'' +
        '}';
  }

  private void writeObject(ObjectOutputStream stream) throws IOException {
    stream.defaultWriteObject(); //默认序列化非static和transient字段
    stream.writeObject(names); //自定义序列化transient字段
  }

  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    stream.defaultReadObject(); //默认反序列化非static和transient字段
    name = (String) stream.readObject(); //自定义反序列化transient字段
    names = (String) stream.readObject(); //自定义反序列化static字段
  }
}
