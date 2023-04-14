##JVM相关面试题

###JVM内存模型
    
###类加载机制

###双亲委派机制

###JMM内存模型

###volatile关键字底层实现

###OOM如何排查

 * OOM分类
    
        JVM设置的内存过小(内存溢出)
        应用的内存无法释放(内存泄漏)

 * OOM常见情况
        
        堆溢出：内存泄漏或堆大小太小造成
        方法区溢出：Class文件太多或者动态生成的Class太多    
        栈溢出：死循环或者深度递归造成   

###JVM常用参数

 * 如何设置新上线的java服务的内存大小
        
        JVM大小: Full GC后老年代大小的三到四倍
        方法区大小：PermSize和MaxPermSize为Full GC后老年代的1.2到1.5倍
        年轻代：Full GC后老年代的1到1.5倍
        老年代：Full GC后老年代的2到3倍
        
        堆大小：堆大小 = 年轻代 + 年老代(xmx = xmn + 老年代)
        
  
 * 设置JVM最大内存和最小内存(通常我们将JVM最大内存和最小内存设置为相同的值防止内存波动)
        
        -Xmx512M       设置JVM最大内存
        -Xms512M       设置JVM最小内存
        -Xmn           年轻代大小
        -Xss           每个线程最大大小
        

 * 错误诊断
        
        -verbose:class  输出jvm载入类的相关信息，当jvm报告说找不到类或者类冲突时可此进行诊断。
        -verbose:gc     输出每次GC的相关情况。
        -verbose:jni    输出native方法调用的相关情况，一般用于诊断jni调用错误信息。                

    
 * 常用设置
        
        -XX:+PrintGCDetails           +表示启动   -表示禁用
        -XX:PretenureSizeThreshold    设置超过该值大小的直接进入老年代
        -XX:NewRatio=4                设置年轻代和老年代的比例
        -XX:SurvivorRatio=4           设置年轻代中Eden区与Survivor区的大小比值。设置为4，则两个Survivor区与一个Eden区的比值为2:4，一个Survivor区占整个年轻代的1/6
        
        
        -XX:+UseParallelGC -XX:ParallelGCThreads=20    年轻代使用ParallelGC,并行数为20
        -XX:+UseParallelOldGC          配置老年代GC      
        -XX:MaxGCPauseMillis=100       设置每次年轻代垃圾回收的最长时间
        -XX:+UseAdaptiveSizePolicy     设置此选项后，并行收集器会自动选择年轻代区大小和相应的Survivor区比例
        -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./oom.hprof
        
        -XX:MaxMetaspaceSize=512m      配置元空间大小
        
 * OOM排查
        
        dmesg |grep -E ‘kill|oom|out of memory’  对Heap size和垃圾回收状况的监控。
        jmap -histo:live pid    查看存活的大对象
        jmap -dump:format=b,file=文件名 [pid] 
        