##JDK
   
###JDK序列化方式(https://blog.csdn.net/hjl21/article/details/86519426)
    
 * JDK中如何实现对象的序列化
    
      需要实例化的类必须实现Serializable接口和Externalizable接口。
      ObjectOutputStream.writeObject(obj)负责把对象序列化为流。
      ObjectInputStream.readObject()负责把流反序列化为对象。
      
      我们可以将流输出到文件中,也可以将流保存在内存中
      文件：FileOutputStream fileOutputStream = new FileOutputStream("D://test.txt");
      内存：ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    
 * Serializable和Externalizable的区别
    
      Serializable默认所有字段都会序列化。
      Externalizable需要指定序列化哪些字段。重写如下两个方法指定
      writeExternal()指定序列化哪些字段。
      readExternal()指定反序列化哪些字段。
    
 * serialVersionUID的作用
    
      serialVersionUID用来确定版本,反序列化时可以通过该字段的信息来验证对象的实体类是否被改变了。
      
 * Serializable如何自定义序列化方式
        
        重写如下两个方法
        
          private void writeObject(ObjectOutputStream stream) throws IOException {
            stream.defaultWriteObject(); //默认序列化非static和transient字段
            stream.writeObject(names); //自定义序列化transient字段
          }
        
          private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
            stream.defaultReadObject(); //默认反序列化非static和transient字段
            name = (String) stream.readObject(); //自定义反序列化transient字段
            names = (String) stream.readObject(); //自定义反序列化static字段
          }
  
 * 代码
        
        SerializableDemo类和SerializableDemo1类 
###事件监听机制
    
    
###JDK动态代理
 
 * 什么是动态代理
        
        动态代理指的是根据一组接口或者一组实现类,在程序运行时动态的添加额外的逻辑。
        动态代理不改变我们原有的接口或者类,只是生成了额外的代码逻辑
        
 * 注解的作用
        
         注解仅仅代表一个标识,没有什么具体的意义,我们可以将注解理解为一个特殊的接口。
         注解主要在生成代理类时起作用,通过提前定义好的逻辑(InvocationHandler)对含有指定注解的代码进行逻辑强化
         
 * 如何实现jdk动态代理
        
        jdk动态代理的本质是生成新的字节码文件用于加强原有的类并重新加载至JVM。
        设计到两个重要的类：
        Proxy：用于生成新的字节码并加载至JVM的
        InvocationHandler：加强的逻辑,代理类方法的调用都会执行到该类中的invoke方法。
        InvocationHandler.invoke()采用反射的方式调用对应的方法。
        
 
 * 切面的实现原理
 
        通过beanFactory的后置处理器处理@Aspect标记的类,生成Advisor(切面)和Aadvice(通知)
        通过bean的后置处理器增强类
        
        JdkDynamicAopProxy类实现了InvocationHandler接口
        
        @Aspect解析
            
            https://blog.csdn.net/supzhili/article/details/98401855
            
        ProxyFactory:代理对象生产工厂,返回值分为如下两种：
            CglibAopProxy:      Cglib
            JdkDynamicAopProxy: jdk
        
        CglibAopProxy和JdkDynamicAopProxy都实现了InvocationHandler接口,
        他们内部创建通过自己的逻辑实现代理类的生成
            
 * 编程式创建切面
 
````java
package com.generate.reset;

import org.aopalliance.aop.Advice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.NameMatchMethodPointcut;

/**
 * @Author: liufeng
 * @Date: 2021/1/27
 * @desc
 */
public class MyAdvisor implements PointcutAdvisor {

  //切点
  @Override
  public Pointcut getPointcut() {
    NameMatchMethodPointcut methodPointcut = new NameMatchMethodPointcut();
    methodPointcut.addMethodName("test");
    return methodPointcut;
  }

  //通知
  @Override
  public Advice getAdvice() {
    MethodBeforeAdvice methodBeforeAdvice = (method, args, target) -> System.out.println("方法执行前");
    return methodBeforeAdvice;
  }

  @Override
  public boolean isPerInstance() {
    return false;
  }
}

class UserService {

  public static void main(String[] args) {
    ProxyFactory factory = new ProxyFactory();
    factory.setTarget(new UserService());
    factory.addAdvisor(new MyAdvisor());
    UserService userService = (UserService) factory.getProxy();
    userService.test();
  }

  public void test() {
    System.out.println("111");
  }
}


````
            
 * 实现AOP
        
        1. BeanFactoryPostProcessor: Bean工厂的后置处理器,作用于beanFactory初始化后和bean实例化前之间
        
            我们可以扫描我们自定义的注解并做相关的处理。
            @Aspect修饰的类就是通过BeanFactory的后置处理器处理切面的
        2. BeanPostProcessor: bean实例化之后放如IOC容器之前执行
            
            我们可以通过定义InvocationHandler生成类的代理类并返回
            Aop就是通过bean的后置处理器将@aspect的逻辑织入的
        3. ImportSelector: 自定义将哪些类交于spring管理,等同于@Import
            
            jar包一般都是通过这种形式指定哪些bean需要被Spring管理的
            
            
 * AOP设计那些设计模式(见设计模式)
    
    1. 工厂模式
    2. 代理模式
    3. 适配器模式
    4. 创建者模式
    5. 策略模式
    6. 模板模式
    7. 责任链模式
    
###ThreadLocal的原理
    
    