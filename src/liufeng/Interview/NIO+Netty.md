##NIO和Netty

###IO是如何实现的

 * IO的定义
        
        I/O指输入/输出(Input/Output),分为IO设备和IO接口两个部分。一般指的是磁盘IO和网络IO。
       
 * 网络IO
        
        网络IO可以理解为TCP通信,所有的IO操作都是由操作系统实现的,JDK通过JNI调用底层。
        Linux操作系统的实现函数为为select()、poll()、epoll()。这三个函数是一个版本更替的关系。
 
 * 面向流和面向缓冲
        
        BIO是面向流的,一次性读取全部的数据到内存数组中。
        NIO是面向缓存的,缓存是有大小的,每次只能存储的最大值时确定的,数据被覆盖前需要保证被先读取。
        
 * Reactor模型      
        
        单线程(只有一个多路复用器)：
            
            一个线程负责所有的accept()和read()事件的分发。
            
        多线程(存在多个多路复用器)：
             
            多个线程负责所有的accept()和read()事件的分发。
            
        主从:
            
            有的selector负责accept(),有的负责read()。
        
        每个selector都需要对应一个端口。
 
 * Socket和Channel的区别
        
        Socket是BIO中的通道。
        Channel是NIO中的通道。
###Linux内存函数select、poll、epoll
 
 * select
    
      select()函数是早期的版本,通过轮询所有的socket判断是否有事件产生。
      为了限制socket数量过大,设置了最大数量为1024。
      
 * poll
      
      poll()函数时取消了select()函数的数量限制。
      
 * epoll
        
      epoll()不在去遍历所有的socket()事件,而是通过一个集合让socket自己去注册事件,epoll()只对进行集合的遍历。
      
###BIO、NIO、AIO
 * BIO
      
      JDK早期的IO模型。 accept()和read()函数都是阻塞的。
      一个线程在accept()和read()发生阻塞时(阻塞的是当前线程),其他的客户端只能进行等待。
      BIO是通过开辟新的线程来执行每一个socket的。
      
 * NIO
      
      JDK1.4引入的IO模型。accept()和read()函数都是不阻塞的。
      一个线程在执行到accept()和read()时如果没有链接或者数据,线程会直接进行下一步。其他的客户端的信息就能被处理了。
      NIO是将socket放在了一个集合中,当该socket没有链接或者数据时,线程会判断其他socket是否存在连接或者数据。保证了
      当前线程不会阻塞(轮询)。
      
      为了解决所有socket都没有链接和数据时造成线程一直空转的问题,引入了多路复用的概念,通过一个集合存储发生了链接和
      数据的socket,该集合为空就阻塞,存在数据就唤醒。
      
 * AIO
    
      JDK1.5引入的IO模型。是对NIO的进一步优化,采用了异步处理的方式。
      accept()和read()方法不在由主线程处理,交由新的线程去处理。
      Linux内核函数对异步的处理不够成熟、一般不使用。
 
###Netty

 * 什么是Netty
      
      Netty是一个基于NIO实现的网络通信框架,使用它可以快速简单地开发网络应用程序。
      NIO的类库和API使用都比较繁琐,Netty对NIO进行了更好的封装,方便用户开发使用。
      Netty的协议是可配置的。

 * Netty和Tomcat的区别 
      
      Netty和tTomcat都是应用服务器,用来接收处理应用请求的。
      不同的是Tomcat使用的是http协议,Netty可以自定义编码解码协议。  
        
 * Netty的主要组件
        
        Channel: 通道,代表一个连接, 用来传输和处理数据。
        ChancelHandler：具体的处理逻辑。
        ChancelPipeline：责任链,由多个ChancelHandler组成。
        EventLoopGroup: 线程池,具体的处理线程。
        EventLoop：封装了selector,与channel绑定。
        ServerBootstrap: 服务器端启动辅助对象。
        Bootstrap：客户端启动辅助对象。
        ServerBootstrap：服务端启动辅助对象。
        ChannelInitializer：Chancel初始化器, 负责添加ChancelHandler和ChancelPipeline。
        ChannelFuture: IO操作执行结果,netty中的操作是异步的，结果会通过异步回调返回。
        ByteBuf: 字节序列。
      
 * Netty编码和解码 
        
        编码:
            
            将数据转化为Byte[]进行数据传输。
            
        解码：
            将Byte[]转化为数据。
     
 * Netty粘包拆包 
        
        TCP是流协议, 流是没有边间的。为了提高资源的利用率,数据的传输和接收都会存在一个缓冲区。
        只有当缓冲区满了或者超过一定时长才会进行数据的发送和接收,所以才会出现粘包和拆包现象。
        
        粘包：当两条数据的流被当做一条数据的流处理时就属于粘包现象。
        拆包：当一条数据的部分流被当做一条数据的流处理时就属于拆包现象。
        
        对于短连接来说不存在粘包现象。 一条数据传输结束后TCP就关闭了。但是存在拆包(数据超过发送缓冲区大小)。
        对于长连接来说会存在粘包拆包现象。粘包最常见的场景就是聊天时连续发送多条数据。
        
        解决方案:
        
        粘包拆包的根本原因是不能从数据流中判断出一条数据的边界。我们需要让接受端知道数据的边界。
        
        方案一：每次发送数据前先告诉接收端数据的大小,在发送数据。    
        方案二：每次都定长发送数据。
        方案三: 通过特殊符号确定每条消息的边界。
        
        
 * Netty心跳机制  
        
        断网等网络异常出现,异常发生的时候， 
        client与server之间如果没有交互的话,它们是无法发现对方已经掉线的。为了解决这个问题, 我们就需要引入心跳机制。   
        
 * Netty的零拷贝
        
        直接内存就是对外堆存,在JVM之外的内存。
 
 * 链式编程
        
        链式编程是创建者模式的一种实现,每个方法都返回自己本身,每个方法只实现自己的功能。
 
 * 高并发
    
    主从Reactor线程模型,响应式模型(观察者)
    NIO多路复用非阻塞
    无锁串行化设计思想
    支持高性能序列化协议
    零拷贝
    bytebuf内存池设计
    