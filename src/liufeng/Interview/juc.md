##JUC包

###JUC

 * 什么是JUC
        
       JUC是jdk提供的并发工具包,帮助我们更方便的写出线程安全的程序。
       
 * JUC包主要包括哪些
       
       atomic包,主要提供了原子操作功能,解决了类似i++不能保证原子性的问题
       
       locks包,主要定义了Lock和AQS,比如我们常用的ReentrantLock和ReentrantReadWriteLock
       以及1.8后对读写锁的优化StampedLock
       
       线程池框架：
       
       安全集合框架：
       
       AQS实现的其他类
       
###原子类

 * 常用原子类
        
        AtomicInteger :原子操作数值
        AtomicIntegerArray :原子操作数组
        AtomicIntegerFieldUpdater :原子操作对象属性
        LongAdder：高并发计数
###锁

 * 常用锁
        
        ReentrantLock  : 独占锁，类似synchronized
        ReentrantReadWriteLock ：读写锁
        StampedLock: 1.8对读写锁优化的产物
 
###线程池
 
 * 线程池框架
        
       提供了定时任务、有返回值任务、无返回值任务 
 
 * 线程池
    
        不描述
        

###常用集合类
   
  * list
        
        CopyOnWriteArrayList :底层通过复制新数组实现线程安全
  * map
        
        ConcurrentHashMap、ConcurrentSkipListMap
  * queue
        ArrayBlockingQueue、LinkedBlockingQueue、LinkedBlockingDeque、ConcurrentLinkedQueue、ConcurrentLinkedDeque
  * set

###工具类
    
 * CountDownLatch
        
       主线程等待其他线程都执行结束后,主线程才可以结束
 
 * CyclicBarrier
        
      当线程数数量达到指定的数量后,所有的线程再一起执行,此时CyclicBarrier会被重置,可以再次被使用,类似与栅栏
 
 * Semaphore
      
      维护同时有多少个线程可以同时访问