package java.lang;

/**
 * InheritableThreadLocal实现了父子线程之间的数据传递功能,
 * Thread中存在两个Map用来存储数据：inheritableThreadLocals和threadLocals
 * 创建子线程时, 只有inheritableThreadLocals中的值会被传递
 * @param <T>
 */
public class InheritableThreadLocal<T> extends ThreadLocal<T> {

  /**
   * 指定复制父线程属性时的转换规则 ,默认不做转换
   * @param parentValue
   * @return
   */
  protected T childValue(T parentValue) {
    return parentValue;
  }

  /**
   * 获取线程
   */
  ThreadLocalMap getMap(Thread t) {
    return t.inheritableThreadLocals;
  }

  /**
   * 创建Map
   */
  void createMap(Thread t, T firstValue) {
    t.inheritableThreadLocals = new ThreadLocalMap(this, firstValue);
  }
}
