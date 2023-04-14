###RocketMQ的知识结构

 * 知识结构
        
        线程模型、淘汰策略、过期机制、持久化方式、数据结构、功能应用、集群部署   
        
 * 参考链接
        
        https://aobing.blog.csdn.net/article/details/103153444 博客

###消息中间件
 
 * 为什么使用消息中间件
        
        消息中间件的使用主要是为了进行异步、削峰、解耦。提高系统的性能。
        
 * 什么消息中间件会有什么问题      
        
        分布式事务问题、重复消息、消息丢失、顺序消费等。
###RocketMQ部署

 * 拉取rocketMQ的镜像
        
        docker pull rocketmqinc/rocketmq
        
 * 启动NameServer
        
        docker run -d -p 9876:9876 --name rmqnamesrv -v /data/rocketmq/namesrv/logs:/root/logs -v /data/rocketmq/namesrv/store:/root/store rocketmqinc/rocketmq sh mqnamesrv
        sh mqnamesrv         通过sh mqnamesrv指定启动时用到的脚本
        -v /data/rocketmq/namesrv/logs:/root/logs -v /data/rocketmq/namesrv/store:/root/store    数据挂载
        
        我们通过 sh mqnamesrv 命令来启动NameServer
        
 * 启动BrokerServer
        
        修改broker.conf的配置文件
        
            brokerClusterName = DefaultCluster   #集群名,统一集群改名字要一样
            brokerName = broker-a                #该台服务的名字,主从服务需要brokerName相同
            namesrvAddr=192.168.64.146:9876      #nameServer地址,多个用分号隔开
            brokerId = 0                         #brokerId=0表示master,其他为slave
            deleteWhen = 04                      #在每天的指定时间删除已经超过保存时长的消息
            fileReservedTime = 48                #持久化消息保存时间，单位小时
            brokerRole = ASYNC_MASTER            #Broker 角色分为 ASYNC_MASTER（异步主机）、SYNC_MASTER（同步主机）以及SLAVE（从机）
            flushDiskType = ASYNC_FLUSH          #刷盘方式，ASYNC_FLUSH 异步刷盘; SYNC_FLUSH 同步刷盘
            brokerIP1 = 192.168.64.157           #监听的网卡地址
            listenPort = 10911                   #监听的的端口
            
        启动brokerServer
            
            docker run -d -p 10911:10911 -p 10909:10909 -v /data/rocketmq/broker/broker.conf:/etc/rocketmq/broker.conf -v /data/rocketmq/broker/logs:/root/logs -v /data/rocketmq/broker/store:/root/store --name rmqbroker1 -e "NAMESRV_ADDR=192.168.64.157:9876" rocketmqinc/rocketmq sh mqbroker -c /etc/rocketmq/broker.conf
            -p 10911:10911 -p 10909:10909    默认NameServer访问10911端口,设置VIP端口时端口-2,即10909,所以两个端口都要开放
            -v /data/rocketmq/broker/logs:/root/logs -v /data/rocketmq/broker/store:/root/store   数据挂载
            -v /data/rocketmq/broker/broker.conf:/etc/rocketmq/broker.conf  挂载第二步创建的配置文件,启动时使用
            -e "NAMESRV_ADDR=192.168.64.146:9876:9876"  设置NameServer地址
            sh mqbroker   启动时执行容器内置脚本
            -c /etc/rocketmq/broker.conf  指定启动时的配置文件,该文件为容器内路径

###RocketMQ的架构

 * NameServer
        
        NameServer是RocketMQ的注册中心,是无状态的节点,集群的话只需要启动多个NameServer集群即可
        Broker在连接时需要将所有的NameServer都拼上才可以
        
 * BrokerServer
        
        BrokerServer是RocketMQ的消息存储地,用来存储和消费我们发送的消息
        修改Broker的配置文件brokerClusterName为相同值表示一个集群(不同会怎样)
        修改Broker的配置文件brokerName为相同值表示主从关系
        
        BrokerServer会和配置文件中namesrvAddr属性指定的每个NameServer保持长连接
        
 * Producer和Consumer
 
        Producer和Consumer都是与NameServer中的一个保持长连接,并且和topic服务的Master和Slave建立长连接

###RocketMQ部署方式

 * 单Master模式
 
 
 * 单Master单Slave模式
        
           主从同步模式不会自动切换,
           主机宕机后从机无法进行写入,提示无topic,但是从机可以提供读操作,消费者可以继续消费
           主机再次启动后无法同步数据 
        
 
 * 多Master模式
        
        一个Master宕机后主题会被分配到其他Broker吗？？？
 
 * 多Master多Slave模式-异步复制
 
 * 多Master多Slave模式-同步双写


###RocketMQ的概念模型

 * 消息生产者(Producer)
        
        用于向Broker Server发送消息
 * 消息消费者(Consumer)
        
        用于消费Broker Server中的消息
 * 消息(Message)
        
        信息的数据结果
 * 消息存储服务器(Broker Server)
        
        消息都会存储在Broker Server上

 * 主题(Topic)
        
        消息发送和消息消费的基本单位。
        一个topic可能储存在多个Broker Server上
        一个topic可能存在多个队列
 * 注册中心(Name Server)
        
        用于记录Topic和Broker Server的对应关系
        
 * 标签(Tag)
        
        Tag是对Topic的细一步划分
        
 * 生产者组
        
        同一类Producer的集合，这类Producer发送同一类消息且发送逻辑一致。如果发送的是事务消息且原始生产者在发送之后崩溃，
        则Broker服务器会联系同一生产者组的其他生产者实例以提交或回溯消费。

 * 消费者组   
        
         同一类Consumer的集合，这类Consumer通常消费同一类消息且消费逻辑一致。
         消费者组使得在消息消费方面，实现负载均衡和容错的目标变得非常容易。
         要注意的是，消费者组的消费者实例必须订阅完全相同的Topic。
 
 * 集群消费和广播消费
        
        集群消费：相同Consumer Group的每个Consumer实例平均分摊消息。
        广播消费：相同Consumer Group的每个Consumer实例都接收全量的消息。
        
 * 顺序消费
        
        普通顺序消费：
            topic对应的每个队列都由一个线程来负责消费,每个队列的消费是有序的。
                        
        严格顺序消费：
            topic对应的所有队列由一个线程来负责消费的
            
 * 业务唯一标识(keys)
        
        存储业务的唯一标识,用于避免重复消费      
        
###RocketMQ的特性
 
 * 发布订阅  
        
        RocketMQ是根据topic进行发布订阅的
 * 消息顺序  
        
        可以控制消息按照FIFO来顺序消费的,分为全局顺序和分区顺序,
        分区顺序指的是各个队列按照FIFO顺序进行消费。
 * 消息过滤 
        
        通过Tag对消息进行消息过滤,消息过滤目前是在Broker端实现的
 * 消息可靠性
        
        消息持久化,分为同步双写和异步双写
 * 至少一次
        
        消费者消费成功后会给Broker Server发送成功信息,确保消息消费成功。
 * 回溯消息
        
        消费成功后,RocketMQ提供了一种机制可以再次消费指定时间内的消息。
        消费成功的消息MQ默认会保存一段时间在删除的
 * 事务消息(暂未理解)
        
        
 * 定时消息
        
        消息先暂时存放在Broker Server中的延迟队列中,而不是直接发送给Topic,
        每一种延迟级别对对一个一个延迟队列,保证了消息的顺序性
        到达指定时间才会真正发送给对应的topic
 * 消息重试
        
        消费失败后的重试机制,
        每个消费者组都有一个重试队列
 * 消息重投
        
        也是消息失败后的处理方式
 * 流量控制
        
        当Broker Server处理能力达到瓶颈时,拒绝send请求,此时消费者不会重试
 * 死信队列
 

###RocketMQ的基本使用