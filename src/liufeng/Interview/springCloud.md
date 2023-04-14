##SpringCloud面试题

###SpringCloud中用到了哪些组件

 * 常用五大组件
        
        Eureka: 服务发现组件,分为客户端和服务端
        Ribbon: 负载均衡组件,通过相同的应用名实现负载均衡
        Zuul: 服务网关,负责接口的监控和转发
        Hystrix: 断路器,负责服务的容灾,包括降级、段融
        Config: 分布式配置中心
        
###SpringCloud集群

 * eureka server 如何集群
        
        在配置文件中指定其他eureka server的地址即可
        eureka.client.service-url.defaultZone=address1,address2
        
 * eureka client 如何集群
 
        标识服务中的具体节点,不可重复 eureka.instatce.instance-id=uuid
        标识服务名,可重复   spring.application.name=eureka-cluster

###Eureka属于CA还是CP

 * CAP理论
        
        C: 一致性
        A: 可用性
        P: 分区容错
        
        一致性和可用性是冲突的,所以只能存在一个,分为CP和AP
 * Eureka
    
        Eureka属于AP,在Eureka集群中,各个节点是平等的
        客户端只向集群中的一个节点发送注册请求,该节点向集群中的其他节点发相同的注册请求
        当客户端发现该节点宕机时,会向其他节点发送注册请求,同一时间各个节点的信息可能存在差异,但最终会是一样的
 * Zookeeper
        
        zookeeper属于CP,在zookeeper及群众,各个节点存在主从关系
        在主节点宕机时需要进行选举,此时不对外提供服务
 

###Eureka实现服务治理的原理

 * Eureka  Server
 
     设计思路:
     内部存在一个MVC层来处理网络请求(不是SpringMVC),使用的是jersey,是通过过滤器来实现的。
     
     
     
 * 开启注册中心
        
        @EnableEurekaServer,主要是将注册EurekaServerMarkerConfiguration到Spring
        其中只是实例化了Marker对象,作为一个开关。作为eureka自动注册类的一个开关
         
        通过SPI加载eureka自动配置类EurekaServerAutoConfiguration
         
 * 服务注册
        
        客户端通过配置的注册中心地址向其中的一个注册中心发送自己的注册信息(服务名、地址等信息)
        
 * 服务集群
        
        该注册中心负责把同样的信息发送给集群中的其他注册中心,通过isReplication判断该请求来自注册中心还是客户端
        
 * 服务发现
        
        客户端从注册中心拉取所有服务的信息到本地,定时从注册中心拉取最新信息
        
 * 服务下架
        
        通过定时器去判断服务是否正常,如果超过指定时间服务没有响应,则将服务下架。
        
 * 心跳链接
        
        客户端会定时和注册中心进行心跳测试,以便及时将出现异常的服务下架。
        
 * 自我保护机制
        
        开启自我保护机制时，注册中心会保存实例数量,当一定时间(15分钟)超过一定百分比(85%)的服务下架,会先按照阈值的最大值就剔除微服务，
        然后检查是否是注册中心自己出现问题,若注册中心没问题,再继续其他剔除过期的服务。
        
###Ribbon原理

 * 远程服务调用
        
        使用服务名代替IP+Port
        通过Ribbon对RestTemplate进行封装,使其可以通过服务名调用
        @LoadBalanced修饰RestTemplate实例即可
        
 * Ribbon原理
        
        Ribbon其实是RestTemplate的拦截器,通过该逻辑实现了对服务名称到IP地址的转化,同时实现了负载均衡。
        不同的微服务使用的是不同的负载均衡器,每个微服务名称都对应一个spring容器,实现了环境隔离。负载均衡器位于容器中   
        负载均衡器第一步是从本地缓存中读取服务数据(30s同步一次,10s判断一次缓存的微服务是否宕机), 本地服务信息定时去EurekaServer去同步数据。
        根据负载均衡原则获取唯一的实例(该实例必须是可用的)

###Feign原理
 * 什么是feign
        
        接口式方法调用,是通过动态代理实现的
        BeanFactory注入Spring
        支持重试
###Hystrix原理
 
 * 原理
        
        切面
 
 * 降级
        
        调用远程失败,这行本地的降级方法
 * 熔断
        
        一定时间内全部失败的话,则认为被调用方宕机了,后续请求不在请求远程,而是直接返回降级信息。
        
 * 