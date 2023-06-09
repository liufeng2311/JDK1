##TCP/IP模型

###OSI模型

 * ISO和OSI的区别
        
        OSI(Open System InterConnect): 开放式系统互联,是网络通信方面所定义的开放系统互连模型。
        ISO(International Organization for Standardization)：国际标准化组织,定义了各个行业的标准。
        
        OSI是ISO指定的网络通信行业的标准。
 
 * 什么是OSI模型(https://blog.csdn.net/dingqinghui/article/details/106499736)
        
        OSI标准定义了网络互联的七层模型,自下而上分别为：
        物理层：
                
                为数据端设备提供原始比特流的传输通路,比特流的传播是不可靠的。
                比如网线、中继器
        数据链路层: 
        
                在通信的实体间建立数据链路连接,接收网络层的ip数据包。
                在物理层比特流在介质上传输是不可靠的，数据链路层在物理层的基础上提供差错检测，差错控制，流量控制。为网络层提供一个可靠传输
                数据链路层信道通信方式：点对点通信  广播通信。
                比如网卡、网桥
        网络层:
                
                为数据在节点之间传输创建逻辑链路,并分组转发数据,对子网间的数据包进行路由选择
                比如路由器、多层交换机、防火墙、IP
        传输层:
                
                提供应用进程间的逻辑处理,建立连接、处理数据包错误
                比如TCP、UDP、Socket
        会话层:
                
                建立端连接并提供访问验证和会话管理(Session)
                比如服务器验证用户登录、断点续传
        表示层:
                
                提供数据格式转换服务,加密、解密、编码、压缩
                比如URL加密、口令加密
        应用层:
                
                访问网络服务的接口,为操作系统或网络应用提供访问网络服务的接口
                比如Telnet、HTTP、FTP、DNS
        
 * 什么是TCP/IP模型
        
        TCP/IP是对ISO模型的优化,也是现在公认的模型,划分为四个层次
        
        主机到网络层、网络互联层、传输层、应用层

 * 什么是TCP/IP协议
        
        TCP/IP不是指只有TCP和IP两种协议,而是一个协议簇,是一种泛称。
         
 * 数据传输模式
        
        数据的传输是从应用层开始的,从上往下传输(一层一层封装)
        数据的接收是从主机到网络层开始的,从下往上传输(一层一层解析)
        
 * 什么是socket
        
        socket是在应用层和传输层之间的一个抽象层，它把TCP/IP层复杂的操作抽象为几个简单的接口供应用层调用已实现进程在网络中通信

###IP地址与MAC地址        
        
 * 区别  
        
      IP是逻辑地址，用于路由寻址。
      MAC是硬件地址（每台主机或路由唯一）用于数据链路层帧传递的地址      
###TCP
 
 * 什么是TCP
        
        TCP属于传输层,是一种可靠的, 面向连接的字节流服务。建立两个主机间的联系。
        在此连接上,被编号的数据段按序收发。同时,要求对每个数据段进行确认,保证了可靠性。
        如果在指定的时间内没有收到目标主机对所发数据段的确认,源主机将再次发送该数据段。　　
        
 * 什么是TCP报文(不包含IP,IP在网络层封装)
        
        TCP是对数据的封装,并且含有源端口信息和目标端口信息,报文的性质、顺序等,具体如下：
        
        源端口号：    发送者端口信息
        目标端口号：  接收者端口信息
        顺序号：      用来标识从TCP源端向TCP目标端发送的数据字节流，它表示在这个报文段中的第一个数据字节。
        确认号：      只有ACK标志为1时,该字段才生效,它包含目标端所期望收到源端的下一个数据字节。
        报文状态字段:  ACK(确认序号有效)、SYN(发起一个连接)、FIN(释放一个连接)

 * 什么是UDP报文(不包含IP,IP在网络层封装)   
        
        UDP是一种不可靠的、无连接的数据报服务。源主机在传送数据前不需要和目标主机建立连接。
        数据被冠以源、目标端口号等UDP报头字段后直接发往目的主机。这时，每个数据段的可靠性依靠上层协议来保证。在传送数据较少、较小的情况下，UDP比TCP更加高效。　　
             
 * 什么是套接字
        
        在每个TCP、UDP数据段中都包含源端口和目标端口字段。有时,我们把一个IP地址和一个端口号合称为一个套接字（Socket）,
        而一个套接字对（Socket pair）可以唯一地确定互连网络中每个TCP连接的双方（客户IP地址、客户端口号、服务器IP地址、服务器端口号）。
        
###TCP连接建立与释放
        
 * 连接建立(三次握手)(三次TCP报文交换)
        
        三次握手的目标是使数据段的发送和接收同步。同时也向其他主机表明其一次可接收的数据量(窗口大小),并建立逻辑连接。
        
        1.源主机发送一个同步标志位(SYN)置1的TCP数据段。此段中同时标明初始序号(ISN)。ISN是一个随时间变化的随机值。　　
        2.目标主机发回确认数据段,此段中的同步标志位(SYN)同样被置1，且确认标志位（ACK）也置1,
          同时在确认序号字段表明目标主机期待收到源主机下一个数据段的序号(即表明前一个数据段已收到并且没有错误)。此外，此段中还包含目标主机的段初始序号。
        3.源主机再回送一个数据段，同样带有递增的发送序号和确认序号。　
        
        通俗理解：第一次和第二次可以让客户端知道可以连接到服务端,第二次和第三次可以让服务端知道可以连接到客户端
        场景类似：  你能听到我说话吗，我能，你能听到我说话吗，我也能
        
        计算机网络解释(两次握手的问题)：
        由于TCP模式下，客户端没有收到确认请求会认为失败而进行重新发送,由于网络原因可能导致发送第二次，
        发送第二次时会清除第一次的信息，如果是因为网络波动第一次的到达了服务器,又需要重新发给客户端，这个链接其实
        是无效的，通过三次握手，服务器可以得知当前以建立连接，直接废弃掉即可。
        
 * 连接释放(四次挥手)
    
        1. 客户端发送FIN给服务器
        2. 服务器接收到回复ACK, 表示接收到
        3. 服务器处理完后发送ACK给客户端
        4. 客户端回复收到, 两端等待片刻后关闭
        
        

###滑动窗口
 * 滑动窗口的作用
        
        在未收到确认时发送端可以连续发送多个数据包，但是如果发送端发送包数量不加以控制，可能会导致接收端缓冲区溢出。
        滑动窗口可以有效解决此问题，窗口用于控制发送端发送速率。
        
        当滑动窗口里被占用满且都未得到回复时是不会发送新的包到服务器的。

###http流程
 * http执行过程
        
        1. 通过DNS服务器将域名解析为IP
        2. 通过IP地址和Port端口与目标服务器建立Socket连接
        3. 组装http的请求参数(请求头和参数),将数据转化为二进制发送给服务器
        4. 服务器处理二进制找到对应的处理器,将数据转化为二进制将数据返回给客户端
        
        
###https流程

 * http执行过程
        
        相比于http的明文传输, https对传输的内容进行了加密,通过对称加密和非对称加密实现了加密传输
        SSL其实是表现成的一种实现
 
 * 如何实现https
        
        消息的传输是通过对称加密实现的,但是对称加密的秘钥第一次传递时采用的是明文,依旧不安全,所以
        对称秘钥的传输是通过非对称加密实现的,服务器通过将对对称加密的公钥传给客户端,客户端用该公钥加密
        非对称加密的秘钥,传给服务器用私钥解密,但是中间攻击者可能拦截第一次的公钥而提供假的公钥,所以
        认证机构用来管理所有的受信任的网站,生成证书,服务器将自己的证书发送给客户端
        所有的网站会负责维持所有受信任的证书,客户端收到服务器的证书会在浏览器的证书管理中查证该证书的合法性
        如何证书的信息和请求的服务器的信息一致则表示安全。从证书中取出服务器对称加密的秘钥进行传输。
 
 * ^运算(对称加密的实现)
        
        A^B=C   A^C=B   B^C=A
        
        
###一个TCP连接可以发送多少个HTTP请求？

 * HTTP/1.0
        
        HTTP/1.0之前默认是一个TCP请求只处理一个http连接,可以通过Connection: keep-alive打开长连接
 
 * HTTP/1.1 
        
        HTTP/1.1之后默认打开长连接。
 
 * TCP长连接     
        
        TCP长连接可以避免每次请求都建立新的TCP连接。
        各个请求是不可以并发的，必须顺序访问。
        每个浏览器TCP的连接数是有上限的,一般为六个。