package liufeng.Interview.arithmetic;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

/**
 * @Author: liufeng
 * @Date: 2021/1/26
 * @desc
 */
public class SerializableDemo1 implements Externalizable {

  private String name;

  private transient String names;

  private static String namesId = "789";

  public static void main(String[] args) throws IOException, ClassNotFoundException {
    FileOutputStream fileOutputStream = new FileOutputStream("D://test.txt");
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream);
    ObjectOutputStream outputStream1 = new ObjectOutputStream(byteArrayOutputStream);
    SerializableDemo1 demo = new SerializableDemo1();
    demo.name = "123";
    demo.names = "456";
    outputStream.writeObject(demo);
    outputStream1.writeObject(demo);
    System.out.println(byteArrayOutputStream);

    FileInputStream fileInputStream = new FileInputStream("D://test.txt");
    ObjectInputStream inputStream = new ObjectInputStream(fileInputStream);
    SerializableDemo1 demos = (SerializableDemo1) inputStream.readObject();
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

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    out.writeUTF(name);
    out.writeUTF(names);
    out.writeUTF(namesId);

  }

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    this.name = in.readUTF();
    this.names = in.readUTF();
    this.namesId = in.readUTF();
  }
}
