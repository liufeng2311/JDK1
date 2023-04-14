##线程

###线程间的通讯
 
 * 方式一: wait()、notify()、notifyAll()
        
        三个方法都是Object类中的非静态方法, 只能通过实例对象来调用。
        必须获取实例的锁synchronized时才可以调用这三个方法(类实例对象不能调用这三个方法)。
        notify()唤醒是随机的唤醒一个线程,只能唤醒指定的线程。
        作用在锁上面而不是线程上面。
        阻塞和唤醒必须顺序执行, 如果先唤醒再阻塞的话会造成阻塞。
        
 * 方式二：park()和unpark(Thread thread)
        
        两个方法是LockSupport类中的静态方法,通过JNI实现。
        作用在线程上而不是锁上面。阻塞和唤醒的是指定的线程而非随机线程
        JUC包下的阻塞机制都是通过LockSupport实现的。
        阻塞和唤醒不必按照顺序执行, 即使先唤醒再阻塞的话会不会造成阻塞。
 
 * 方式三：volatile
        
        通过volatile关键字的可见性实现通讯
        
 * LockSupport实现的常用工具类
        
        CountDownLatch、Semaphore、CyclicBarrier、Condition
        
 * LockSupport实现原理
        
        通过一个变量来进行控制线程的阻塞与唤醒。
        线程阻塞需要消耗一个凭证,如果存在凭证则消耗凭证并退出,否则进行等待。
        线程唤醒则是添加一个凭证,但凭证最多只有一个。
        
 * wait()、notify()和park()、unpark的隔离性
        
        两种机制才用的是不同的机制,相互不影响
 
 * wait()、sleep()、park()对中断的处理
        
        wait()、sleep()会抛出异常
        park()不抛出异常而是直接退出阻塞
        