package java.lang.reflect;

public interface InvocationHandler {

  /**
   * @param proxy  代理实例
   * @param method 方法名
   * @param args   参数
   */
  public Object invoke(Object proxy, Method method, Object[] args)
      throws Throwable;
}
