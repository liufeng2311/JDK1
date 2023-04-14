##SpringBoot

### SpringBoot启动原理
 
 * 启动原理
 
 * 自动适配原理
        
        
### SpringBoot文件加载顺序

 * 文件加载顺序(定义在ConfigFileApplicationListener文件中)
        
        String DEFAULT_SEARCH_LOCATIONS = "classpath:/,classpath:/config/,file:./,file:./config/*/,file:./config/";
        
        文件的优先级由低到高,不同属性互补,相同属性高优先级进行替换。