##Dubbo

###什么是dubbo
    
 * 什么是RPC
        
        RPC(Remote Procedure Call)：指远程方法调用。
        RPC的宗旨是调用远程方法可以像调用本地方法一样。
 
 * 什么是dubbo
 
        Dubbo是Alibaba内部根据RPC开发的一个内部框架,之后开源。
        Dubbo现在将定位从RPC框架向服务框架转变。SpringCloud拥有的功能都会用RPC实现。
        
 * RPC和HTTP
        
        RPC和Http一样,也是一种计算机通信协议,通过自定义数据协议和传输协议进行通讯。RPC的网络传输可以使用http,也可以使用自己实现的dubbo。
        RPC over Http: 基于HTTP实现RPC。
        RPC over TCP: 基于TCP实现RPC。
 
 * Dubbo和SpringCloud的区别
        
        Dubbo的调用时通过服务调用的,侧重的是服务间的调用。只需要接口和接口的实现类,不需要Controller层。
        SpringCloud的调用时通过MVC的请求路径调用。前后端调用和服务端调用都可以。
 
 * 什么是协议
        
        协议指的是通信双方协商好一些列规则,比如http协议商定好的就是request对象
        协议就是双方都同意的约定。
        
              
###dubbo服务提供者
    
 * 启动容器用来接收请求
        
        我们可以启动Tomcat、jetty等容器。以Tomcat为例。
        tomcat是对servlet进行处理的,所以类似于MVC的dispatcherServlet,dubbo也创建一个servlet接收全部的请求来进行统一处理。
        
 * 本地注册
        
        dubbo是对服务的调用,所以提供者需要根据服务名确定唯一的实现类,通过方法名、形参、实参来进行反射调用。
        服务提供者会通过本地注册来保存服务名和具体服务类的对应关系。
        
 * 注册中心注册
        
        将自己暴露出的服务发送至注册中心,格式为: 服务名 --> 服务地址。
        这样消费者就可以获取所有该服务的提供地址,通过负载均衡获取一个实例地址进行服务请求。
        
###dubbo服务消费者
    
 * 消费者
        
        通过注册中心将服务信息都缓存到本地,通过注册中心的监听机制及时跟新最新的数据。
        负载均衡通过消费者实现。
        降级和熔断通过消费者实现。

###为什么zookeeper和redis适合做注册中心
 
 * 分布式
        
        两个服务共用,实现信息共享
        
 * 高可用
        
        集群部署
        
 * 数据存在内存中
        
        数据位于内存中,速度快
        
 * 事件监听机制(重要)
        
        事件监听机制可以可以快速同步注册中心和本地缓存的数据
        
###mock机制
 
 * 什么是mock
        
        当被调用方还没有开发完成时,通过mock返回预期值,这样可以不用发送请求

###工厂模式解决切换问题
 
 * 简单工厂模式
        
        用于使用配置选项来决定选择生成哪个实例。
 
 * 工厂方法模式
        
        通过一个工厂接口来让子类实现具体工厂来生成具体的类。
        
 * 抽象工厂模式
        
        工厂方法模式只能生成一个对象,抽象工厂方法则可以生成多个对象。

###dubbo常用配置(消费端会覆盖提供端的配置)
 
 * check 
        
        Dubbo默认会在启动时检查依赖的服务是否可用,不可用时会抛出异常,阻止Spring初始化完成,以便及早发现问题。默认check="true"。      
        
        关闭某个服务启动时检查
        <dubbo:reference interface="com.foo.BarService" check="false" />
        
        关闭所有服务检查,所有服务的默认值,会被具体的服务设置覆盖
        <dubbo:consumer check="false" />
        
        关闭注册中心检查,订阅失败时报错
        <dubbo:registry check="false" />
        
 * retries
        
        Dubbo服务调用失败时会进行容错(不同的策略重试次数不同),通常会进行重试调用,通过该参数指定重试次数,默认2次
        
        <dubbo:service retries="2" />
        
        <dubbo:reference retries="2" />
 
 * cluster
        
        Dubbo配置集群容错策略
        
        <dubbo:service cluster="failsafe" />
        
 * loadbalance
        
        Dubbo配置负载均衡策略
        
        <dubbo:service interface="..." loadbalance="roundrobin" />
        
 * dispatcher
        
        dubbo派发策略,用于协议配置
        
        <dubbo:protocol name="dubbo" dispatcher="all" threadpool="fixed" threads="100" />
 
 * threads
        
        dubbo线程池策略,用于协议配置
        
        <dubbo:protocol name="dubbo" dispatcher="all" threadpool="fixed" threads="100" />
        
 * url   
        
        dubbo点对点直连,不通过注册中心(一般开发环境使用)
        
 * register 
        
        只订阅,只订阅其他服务,但是不将自己作为服务注册到注册中心
        
        <dubbo:registry address="10.20.153.10:9090" register="false" />
        
 * protocol
        
        dubbo可以配置多个协议,同一个接口默认以多个协议暴露出去,可以通过protocol指定为具体协议
        <dubbo:protocol name="dubbo" port="20880" />
        <dubbo:protocol name="rmi" port="1099" />
        <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" protocol="dubbo" />
 
 * registry
        
        dubbo可以配置多个注册中心,可以指定服务向哪个注册中心注册
        
            <dubbo:registry id="hangzhouRegistry" address="10.20.141.150:9090" />
            <dubbo:registry id="qingdaoRegistry" address="10.20.141.151:9010" default="false" />
            <!-- 向多个注册中心注册 -->
            <dubbo:service interface="com.alibaba.hello.api.HelloService" version="1.0.0" ref="helloService" registry="hangzhouRegistry,qingdaoRegistry" />
            
 * group
        
        dubbo中一个接口可能存在多个实现类,通过group进行区分
        
        <dubbo:service group="feedback" interface="com.xxx.IndexService" />
        <dubbo:service group="member" interface="com.xxx.IndexService" />
        
        <!-- 任意组 -->
        <dubbo:reference id="barService" interface="com.foo.BarService" group="*" />
        
 * version
        
        多一个服务出现两个不兼容的版本,使用版本号区分
        
 * merger
        
        分组聚合,将不同组的数据聚合返回
        
 * validation
        
        开启参数验证
        
 * cache
        
        开启缓存
        
        <dubbo:reference interface="com.foo.BarService" cache="lru" />
        
###dubbo集群容错策略

 * Failover Cluster      
        
        失败自动切换重试,默认为两次
 
 * Failfast Cluster
        
        失败不进行重试,用于非幂等性的写操作
 
 * Failsafe Cluster
        
        失败安全，出现异常时，直接忽略。通常用于写入审计日志等操作。
        
 * Failback Cluster 
        
        失败自动恢复，后台记录失败请求，定时重发。通常用于消息通知操作。
  
 * Forking Cluster      
        
        并行调用多个服务器，只要一个成功即返回。通常用于实时性要求较高的读操作，
        但需要浪费更多服务资源。可通过 forks="2" 来设置最大并行数。
        
 * Broadcast Cluster
        
        广播调用所有提供者，逐个调用，任意一台报错则报错。通常用于通知所有提供者更新缓存或日志等本地资源信息。
        
###dubbo负载均衡策略

 * Random LoadBalance
        
        随机分配
 
 * RoundRobin LoadBalance
        
        轮询
 
 * LeastActive LoadBalance
        
        最少活跃调用数
 
 * ConsistentHash LoadBalance
        
      一致性Hash，相同参数的请求总是发到同一提供者  
###dubbo缓存策略      

 * lru
        
      最少使用
      
 * threadlocal
        
      线程本地缓存
###dubbo线程模型
 
 * 线程模型
        
       如果事件处理的逻辑能迅速完成，并且不会发起新的 IO 请求，比如只是在内存中记个标识，则直接在 IO 线程上处理更快，因为减少了线程池调度。 
       如何逻辑处理较慢或发起新的IO,必须派发到线程池。

###服务降级、本地存根、本地伪装
###dubbo基于xml开发
    
###dubbo基于注解开发

 * 服务提供
        
        @Service：需要在接口的实现类添加该注解即可。该注解是dubbo提供的,而不是spring
        
 * 服务消费者
 
        @Reference: 需要在使用的地方通过该注解引入。
        
        
###dubbo的SPI机制

 * SPI
        
        不同于Spring的SPI, dubbo的SPI机制是按需加载而不是全部加载。
        
 * 自适应SPI
        
        dubbo提供了一种延时加载机制,根据参数的请求动态的扩展。
        @Aadaptive修饰在类上,如果没有配置会自动生成一个。
        该代理对象被用于赋值,只有真实使用时,通过URL告诉代理对象使用哪个。
  
        @Adaptive修饰在方法上,生成代理类会实现被修饰的方法,获取URL  
        
###服务超时(需要自己尝试)

 * 只有一方设置时间
 
 * 都设置时间
 
 
 
 服务端执行时间超过指定时间，只会打印日志