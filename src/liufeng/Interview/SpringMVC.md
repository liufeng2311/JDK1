##MVC面试题

###什么是SpringMVC

    WEB框架的工作逻辑可以分为三个步骤: 请求接受 + 逻辑处理 + 结果返回
    SpringMVC主要负责WEB框架中的逻辑处理, WEB容器负责请求的接收和结果的返回
    SpringMVC和WEB容器遵守共同的Servlet规范, 各司其职, 共同完成WEB框架的功能
    
    MVC的全名是Model View Controller, 表示一种设计规范, 具体含义如下:
    M表示Model, 表示逻辑数据
    V表示View, 表示用户交互视图
    C表示Controller, 表示请求处理逻辑
    请求通过逻辑处理完后得到需要的数据,并将数据显示在用户页面上
###SpringMVC执行流程

    1.WEB容器接收到请求后调用DispatherServlet实现类的service方法
    2.通过HandleMapping组件找到对应的处理器, 封装所有的拦截器返回一个执行器链
    3.通过HandlerAdapter确定该处理器的类型并执行处理器,返回ModelAndView
    4.通过视图解析器解析器找到需要的视图进行相关处理
    5.通过model渲染视图并返回给前端
    
    微服务采用的前后端分离返回的都是json数据,视图解析这块已经过时了
###转发和重定向(技术已过时)
    
    转发是在WEB容器中进行的,根据转发的请求重新进行一次servlet的选择,与前端无关
    重定向是返回前端,由前端再次发送请求的
    

###请求路径中的/*和/的区别
    
    本质上是一样的,同时存在/*优先级更高
    tomcat的配置文件web.xml中存在两个servet,一个拦截的是jsp文件,另一个拦截的是/(转向404页面)
    我们的mvc项目中配置的是/*,优先级时最高的

###拦截器和过滤器的区别

    两者的作用是一样的,只是触发执行的时机不同
    过滤器是提供给WEB容器,在进入Servlet容器前就会触发,不限制只进入DispatcherServlet,可以拦截所有的servlet(包括页面请求)场景：统一编码设置、请求记录等
    拦截器是提供给MVC的,在进入DispatcherServlet后触发,场景：统一编码设置、请求记录等
    
###父子容器
    
    父容器是在WEB容器的的监听器中创建的,解析的是spring.xml,生成spring容器的(配置解析Controller以外的bean)
    子容器是给Servlet用到的,解析spring-servlet.xml(配置解析Controller)
    子容器可以访问父容器的bean,但是父容器不能访问子容器的bean,跨容器会造成AOP失效
    两个容器扫描相同的路径都会存有一份自己的bean实例,造成空间浪费,配置两份配置文是由于之前WEB框架除了
    SpringMVC还有其他的框架,两个容器方便框架切换,现在SpringMVC成为了主流,配置一份就好了
    