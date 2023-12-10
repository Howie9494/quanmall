# 趣安商城

#### 介绍
趣安商城

#### 软件架构
基于微服务架构的优惠券商城系统

#### 技术总览

1. 项目环境：linux（CentOS7）、Docker（linux）、MySQL（Docker）、Redis（Docker）、数据检索：Elasticsearch（Docker）<br/>
Maven、Git、Node、服务器Tomcat、负载均衡和反向代理服务器Nginx。

2. 技术栈：SSM、SpringBoot、Mybatis-plus、RabbitMQ、Seata分布式事务<br/>
SpringCloud（网关：Gateway、注册中心和配置中心：SpringCloudAlibaba Nacos、服务熔断降级限流：SpringCloudAlibaba Sentinel、
远程调用：OpenFeign、负载均衡：Ribbon、链路追踪：Sleuth（界面：Zipkin））。

3. 高可用集群

#### 安装教程
`git clone ${https}`

#### 版本控制
jdk 1.8<br/>
SpringBoot 2.3.2.RELEASE<br/>
SpringCloud Hoxton.SR9<br/>
SpringCloudAlibaba 2.2.6.RELEASE(Sentinel 1.8.1\Nacos 1.4.2)<br/>
MybatisPlus 3.2.0<br/>
Lombok 1.18.8<br/>
Httpcore 4.4.12<br/>
Commons-lang 2.6<br/>
MySQL 8.0.17<br/>
Servlet 2.5<br/>
Elasticsearch-Rest-Client 7.4.2<br/>

#### 分布式原则
分布式系统的CAP原则（CAP定理）：
1. 一致性（Consistency):<br/>
在分布式系统中的所有数据备份，在同一时刻是否是同样的值。（等同于所有节点访问同一份最新的数据副本）
2. 可用性（Availability）：<br/>
在集群中一部分节点故障后，集群整体是否还能影响客户端的读写请求。（对数据更新具备高可用性）
3. 分区容错性（Partition tolerance）：<br/>
大多数分布式系统都分布在多个子网络。每个子网络就叫做一个区（partition）。分区容错的意思是，区间通信可能失败.
比如，一台服务器放在中国，另一台服务器放在美国，这就是两个区，他们之间可能无法通信。

CAP原则指的是，这三个要素最多只能同时实现两点，不可能三者兼顾。

在分布式系统中网络之间的传输错误无法避免，所以我们必须要满足分区容错性，这也就意味着我们需要在一致性和可用性时间进行选择。

我们可以在实现CP原则的情况下，通过一些算法实现一致性的需求；如raft算法（领导选举和日志复制，动画效果：中文版http://www.kailing.pub/raft/index.html、原版http://thesecretlivesofdata.com/raft/）<br/>

Raft算法:日志复制
1. 每个节点有三种状态，分别为随从、候选者、领导者。
2. 在一段时间后没有收到领导者的消息，其中一个节点就会变为候选者；加下来给其他节点发起一个投票，如果收到大多数节点的投票就会成为领导者。
3. 所有的修改都只能发给领导者。每个改变的命令都以日志的方式存储（未提交，不会改变数据），然后会复制这个日志发送给随从节点；当大多数随从节点都是收到并写入日志后，
领导者就会把修改提交（改变数据），然后通知随从节点提交数据。
4. 日志复制会在领导者的下一个心跳时发出，领导完成提交后会在下个心跳让随从节点完成提交。

Raft算法:领导选举：
1. 有两个超时时间来控制选举过程：<br/>
一个是选举超时:是随从想要变为候选者的间隔时间（150ms-300ms，节点的自旋时间），每个节点的选举超时时间是随机的。<br/>
另一个是心跳超时：在心跳时间结束后领导者会发送心跳消息给随从，当随从收到消息后会再次重置选举超时时间。如果没有在选举超时时间内收到心跳消息，则会变为候选者发起新的一轮投票。
2. 成为候选者后会发起一轮投票，初始的投票数为1（即自己投票），如果随从节点没有投票则会给该候选节点投票，如果已经给其他候选节点投过票则无法再次投票；
如果投票成功后会重置选取超时时间并开始自旋。
3. 该心跳会一直维持到有一个随从节点停止接受心跳，并成为候选者（即领导节点宕机）。
4. 投票分离：两个候选者都发起投票，票数统计都一样；会重置选举之间开启新一轮的自旋，直到领导者产生。

Raft可以在面临网络分区时保持一致性。在出现分区后，不同分区会产生不同的领导节点（也就是不能接受领导节点心跳的随从节点，重新进行领导节点的选择，从而不同分区不同领导者）
1. 如果给其中一个领导节点发送修改请求，如果没有超过一半的节点完成日志的复制，则领导节点和该分区的随从节点都之后将修改放在日志中不提交。
2. 如果大部分节点完成日志的复制，则会照常进行修改的提交。
3. 在回复网络通讯的时候，会以更高轮选举（意味着随从的节点更多，更容易进行修改的提交，也就意味着其中的数据为最新数据）的领导者为新的领导者。
4. 在原本未更新的分区中，所有节点的日志信息中未提交的信息全部回滚。在匹配新领导的日志并提交。

对于多数大型互联网应用的场景，主机众多部署分散，而且现在的集群规模越来越大。所以节点故障、网络故障是常态，而且要保证服务的高可用性，
即保证AP，舍弃C。
<br/>
<br/>
<br/>
BASE理论

是对CAP理论的延伸，思想是即使无法做到强一致性（CAP的一致性就是强一致性），但可以采用适当的采取弱一致性，即最终一致性。
1. 基本可用（Basically Available）：基本可用是指分布式系统在出现故障的时候，允许损失部分可用性（如响应时间、功能上的可用性），
但并不意味着系统不可用。<br/>
响应时间上的损失：正常情况下搜索引擎需要在0.5秒之内返回给用户响应的查询结果，但由于出现故障（比如系统部分机房发生断电或断网），查询结果的响应时间增加到1~2秒。<br/>
功能上的损失：购物网站在购物高峰（如双十一）时，为了保护系统的稳定性，部分消费者可能会被引导到一个降级页面。
2. 软状态（Soft State）：软状态是指允许系统存在中间状态，而该中间状态不会影响系统整体可用性。分布式存储中一般一份数据会有多个副本，
允许不同副本同步的延时就是软状态的体现。mysql replication的异步复制也是一种体现。
3. 最终一致性（Eventual Consistency）：最终一致性是指系统中的所有数据副本经过一定时间后，最终能够达到一致的状态。
弱一致性和强一致性相反，最终一致性是弱一致性的一种特殊情况。

强一致性：要求更新过的数据能被后续的访问都能看到。<br/>
弱一致性：能容忍后续部分或全部访问不到。<br/>
最终一致性：经过一段时间后要求能访问到更新后的数据。<br/>


#### 技术选择

1. 服务器环境：本系统选用linux系统作为服务器环境，主要原因是市场上互联网公司的服务器都是linux系统（linux更加高效安全，有一种说法：linux系统之所以比windows更安全更适合做服务器是因为它是开源的，大家发现linux的漏洞后会马上公开并进行维护；而windows系统一直掌握在微软手中，哪怕有黑客可以看到其底层的代码，也不能告诉微软公司进行维护），linux系统更加稳定，哪怕长时间的运行也不会像windows一样死机重启，而这恰恰就是作为服务器最需要的。<br/>
CentOS系统是中国程序员广泛使用的，推荐7以上的版本。(7以下的版本已经停更，尤其是yum源已经没有了，哪怕使用阿里镜像也会有各种问题)

2. 服务网关：zuul是基于servlet之上的一个阻塞式处理模型而且已经停更，zuul2由于各种问题无法及时推出。Spring自家推出Gateway取而代之，Gateway是基于Reactor完全异步非阻塞的框架，且与自家框架有更好的适配。

3. 注册中心：当下SpringCloud技术中心主要有eureka、zookeeper、consul和nacos。其中eureka使用AP原则，但需要自己配置启动类且已经停更。zookeeper和consul使用CP原则，但是nacos无论在功能还是使用方面都要优于前者，且nacos可以在CP和AP之间进行切换。<br/>
(CAP原则：C:Consistency（强一致性）、A:Availability（可用性）、P:Partition tolerance（分区容错性）)

4. 配置中心：原本SpringCloud需要通过服务配置config进行配置，通过服务总线bus解决更新问题。而nacos可以完美解决这写问题，且无需配置，在界面即可进行添加配置。何乐不为？

5. 服务熔断降级限流：原本Hystrix已经停更，但其设计思路仍广为借鉴。Sentinel有优秀的图形界面且降级限流等操作在界面即可完成而无需另外配置。另外它的熔断机制更多更全面，还有热点key限流特别棒。

6. 远程调用：原本Feign已经停更，OpenFeign成为更好的选择。想比于RestTemplate，OpenFeign更符合我们的设计习惯（通过service进行远程调用）。

7. 负载均衡：选用Ribbon

8. 分布式事务：在分布式架构下，分库分表成为必然。那么分布式事务则是我们必须要解决的问题，可以通过redis完成分布式事务，但是seata成为更好的解决方案。

9. 缓存：在Mybatis的设计中有一级缓存和二级缓存。但是Mybatis的缓存仅仅是通过一个Map进行实现，在很多时候不能满足我们的需求，所以需要Redis。

10. Nginx：反向代理 负载均衡 动静分离

11. RabbitMQ：流量削峰 应用解耦 异步处理

12. Docker：应用隔离避免相互影响

13. 高可用集群：通过集群可以避免雪崩


#### 配置细节

1、 通过镜像安装虚拟机并修改配置文件把ip地址设置为静态的<br/>

`vim /etc/sysconfig/network-script/ifcfg-eth33`<br/>
ps：配置文件名字可能不同，但都以ifcfg-th开头
```$xslt
ONBOOT=yes
BOOTPROTO=static//静态的
IPADDR=指定ip
GATEWAY=网关
DNS1=和网关保持一致即可
```

2、 数据库：quanmall_admin（管理系统 /renren-fast/db/mysql.sql)、quanmall_oms(订单数据库 /sql/gulimall_oms.sql)、quanmall_pms（产品数据库 /sql/gulimall_pms.sql)、quanmall_sms(优惠数据库 /sql/gulimall_sms.sql)、quanmall_ums（会员数据库 /sql/gulimall_ums.sql)、quanmall_wms（库存数据库 /sql/gulimall_wms.sql);<br/>

3、 后台管理系统启动：<br/>

后端：修改renren-fast/src/main/resources/application-dev.yml的url、username和password（mysql）<br/>
前端：下载node.js，可以在cmd窗口用`node -v`和`npm -v`测试是否安装成功。使用vscode打开renren-fast-vue，运行`npm install`，待完成后运行`npm run dev`即可启动后台管理系统。<br/>
ps：前端启动时，后端也需启动<br/>

4、 整合Mybatis-plus<br/>

    i. 导入依赖
    ```xml
       <dependency>
            <groupId>com.baomidou</groupId>
            <artifactId>mybatis-plus-boot-starter</artifactId>
            <version>3.2.0</version>
        </dependency>
    ```<br/>
   ii. 配置<br/>
        1）配置数据源；<br/>
            导入数据驱动（由于每个微服务都需要数据库驱动，所以放在common里可以减少代码冗余）<br/>
            在application.yml中配置数据源相关信息<br/>
        2）配置Mybatis-plus；<br/>
            使用@MapperScan<br/>
            告诉Mybatis-plus，sql映射文件位置<br/>

5、Nacos注册中心<br/>

解压/dev_tools/nacos-server-1.4.2.zip（Windows），需要运行/bin/startup.com（因为1.4.2默认是集群启动，开始为了测试方便就不设置集群，且下载在Windows上）<br/>
单机启动nacos需要使用命令`startup.cmd -m standalone`<br/>
引入依赖：
```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
        </dependency>
```
修改配置：在微服务的yml中配置nacos.discovery.server-addr和application.name(注册到注册中心必须要有)<br/>
添加注解：在主启动类上添加@EnableDiscoveryClient<br/>

6、OpenFeign远程调用<br/>

引入依赖：
```xml
        <dependency>
             <groupId>org.springframework.cloud</groupId>
             <artifactId>spring-cloud-starter-openfeign</artifactId>
        </dependency>
```
编写接口（添加注解@FeignClient("服务名")）并在主启动类上添加注解@EnableFeignClients(basePackages = "接口所在的包路径")<br/>

7、Nacos配置中心<br/>

引入依赖：
```xml
        <dependency>
             <groupId>com.alibaba.cloud</groupId>
             <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
        </dependency>
```
创建一个bootstrap.properties：<br/>
```properties
    spring.application.name=quanmall-coupon
    spring.cloud.nacos.config.server-addr=127.0.0.1:8848
```
给配置中心中添加DataId（quanmall-coupon.properties）。默认规则:应用名.properties。<br/>
动态获取配置：在对应的Controller上添加注解@RefreshScope，@Value用于获取配置文件中的值。（优先使用配置中心的配置）<br/>

8、通过java8的Stream API对数据库的category表进行循环遍历并以树状的结构展现。<br/>

9、在renren-fast-vue获取category信息需要修改api接口请求地址，但由于需要访问的服务不同需要在其中进行切换。所以改由网关控制,但也会因同源策略导致跳转被拒绝。
在跨域的流程中，浏览器会提前发送一个OPTIONS预检请求，只有在预检请求被允许后才会发送真正的请求。
我们可以使用nginx部署为同一个域，也可以配置允许跨域（由于大量请求都需要跨域，所以可以在网关中配置filter跨域）。

10、实现页面对category的添加和删除（逻辑删除）操作<br/>

逻辑删除：<br/>
1)配置全局的逻辑删除规则<br/>
2)配置逻辑删除的组件Bean（3.1.1之后可省略）<br/>
3)给实体类字段加上逻辑删除注解@TableLogic<br/>

11、品牌管理页面的增删改查结构通过逆向工程完成，其中文件上传功能由于是分布式架构需要服务器专门负责文件的存储工作（在本项目中我们采用阿里云的oss通过服务端签名后直传完成）<br/>
oss对象存储流程：<br/>

1）引入oss-starter<br/>
2）配置key，endpoint相关信息<br/>
3）使用OSSClient进行相关操作<br/>
并对数据在前端、后端（JSR303）都进行校验<br/>

JSR303：<br/>

1）给Bean添加校验注解(javax.validation.constraints)并定义自己的message提示<br/>
2）在controller中添加@Valid开启校验<br/>
3）给校验的bean后添加一个BindingResult就可以获取到校验的结果<br/>
4）分组校验@Validated指定校验分组,而没有指定分组的校验不生效（多场景复杂校验）<br/>
5）自定义校验（编写并关联自定义的校验注解和校验器）<br/>

统一的异常处理：<br/>

1）编写异常处理类，使用@ControllerAdvice。<br/>
2）使用@ExceptionHandler标注方法可以处理的异常。<br/>

12、创建枚举类定义错误状态码规范

13、编写获取分类属性分组接口`Get:/product/attrgroup/list/{catelogId}(配置模糊查询、根据catelogId查询三级目录地址等功能)<br/>

14、完善之前完成的模块（通过配置类导入分页插件、修改品牌的模糊查询功能、修改获取品牌关联分类的接口）<br/>

15、由于电商项目的数据库设计要尽可能避免表之间的关联，所以没有设计外键，而是通过属性冗余的方式。这也就意味着在修改一个表时也需要更新
其他相关表的冗余信息(在更新级联属性的时候需要使用事务：在主启动类上标注@EnableTransactionManagement，在对应的方法上标注@Transactional)。<br/>

16、根据前端对应的接口完善所需相关接口功能

17、在docker容器中下载ElasticSearch和Kibana。通过http协议通过9200端口完成SpringBoot对Elasticsearch-Rest-Client整合，
通过quanmall-search微服务配置进行全文检索的使用。<br/>

1）导入依赖
```xml
<dependency>
            <groupId>org.elasticsearch.client</groupId>
            <artifactId>elasticsearch-rest-high-level-client</artifactId>
            <version>7.4.2</version>
        </dependency>
```
2）编写配置，给容器中注入一个RestHighLevelClient
```
    @Bean
    public RestHighLevelClient esRestClient(){
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.94.80",9200,"http")
                )
        );
        return client;
    }
```
3）参照官方API操作即可。<br/>

18、商城页面采用thymeleaf模板引擎，通过nginx进行动静分离的方式。<br/>

19、通过nginx反向代理首页。<br/>

20、使用jmeter压测对系统进行jvm优化。（开启缓存、增加索引、关闭日志、动静分离）<br/>

1)可以根据jmeter测试结果得知MySQL的优化问题、模板渲染速度问题和静态资源问题对吞吐量有一定影响。<br/>
通过开启缓存、给数据库的三级分类查询字段增加索引、关闭日志对吞吐量有一定的提升。<br/>
2)可以根据jvisualvm的监测和jvm设计结构得知YGC和FULLGC对响应时间有一定影响，其中FULLGC时间长影响大。<br/>
从Visual GC可视化中可以看出有大量的GC，主要原因是Eden区和Old区分配内存空间较小。可以修改VM options为`-Xmx1024m -Xms1024m -Xmn512m`
在一定程度上可以减少GC次数。<br/>
3)修改业务逻辑，将云本三级分类所需的多次查询修改为：一次查询将结果放在list中，后续的查询带条件查询仅需从list中进行筛选查询即可。<br/>

21、引入Redis缓存，将即时性、数据一致性要求不高的数据和访问量大且更新频率不高的数据放入缓存。<br/>

1)本地缓存模式在分布式下在不同主机下的集群服务每台主机都需要查询并自行缓存，且如果需要修改缓存内容会导致数据一致性问题。<br/>
2)缓存中间件：打破缓存容量限制，使用简单、维护容易，也可以做到高可用高性能。<br/>

22、为了避免出现缓存使用问题采取如下方案：<br/>

1)空结果缓存，解决缓存穿透<br/>
2)设置过期时间（加随机值），解决缓存雪崩<br/>
3)加锁，解决缓存击穿<br/>

23、整合redisson作为分布式锁等功能的框架<br/>

lock()阻塞式等待，默认加的锁都是30s<br/>
1)锁的自动续期，如果业务超长，运行期间自动给锁续上新的30s。不用担心业务时间长，锁自动过期被删掉。<br/>
2)加锁的业务只要运行完成，就不会给当前锁续期，即使不手动解锁，锁默认在30s以后自动删除。<br/>
如果传递了锁的超时时间，就发送给redis执行脚本，进行占锁，默认超时就是制定的超时时间。<br/>
如果未指定锁的超时时间，就是用30*1000【LockWatchdogTimeout看门狗的默认时间】；
只要占锁成功，就会启动一个定时任务【重新给锁设置过期时间，新的过期时间就是看门狗的默认时间】
续期时间为internalLockLeaseTime(看门狗的默认时间)/3 <br/>
最佳实战：指定过期时间。<br/><br/>
读写锁（ReadWriteLock）：写锁是排它锁（互斥锁、独享锁），读锁是共享锁。<br/>
读+读=相当于无锁，并发读，只会在redis中记录所有当前的读锁；所以都会同时加锁成功。<br/>
写+读=等待写锁释放<br/>
写+写=阻塞方式<br/>
读+写=有读锁，写锁需要等待。<br/><br/>
闭锁（CountDownLatch）<br/>
信号量（Semaphore）也可以用作分布式限流<br/>

24、缓存数据一致性<br/>

双写模式:脏数据问题、缓存不一致<br/>
失效模式:缓存不一致<br/>
可以通过增加过期时间解决大部分业务对于缓存要求，也可以在加上读写锁保证并发读写。<br/>
总结：数据实时性、一致性高的就去数据库查，要求不高的读多写少的场景放在缓存中，害怕出现脏数据就用读写锁。<br/>

最好解决方案：Canal<br/>
本系统的一致性解决方案：<br/>

1)缓存所有数据设置过期时间，数据过期下一次查询出发主动更新。<br/>
2)读写数据时加上分布式的读写锁，对经常读性能几乎没有影响。<br/>

25、整合SpringCache简化缓存开发<br/>

1)引入依赖
```xml
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
```
2)写配置`spring.cache.type=redis`<br/>

3)使用缓存：<br/>

@Cacheable:触发将数据保存到缓存的操作(代表当前方法的结果需要缓存，如果缓存中有则方法不调用；如果没有，则会调用方法并最后将方法的结果放入缓存。)<br/>
@CacheEvict:触发将数据从缓存删除的操作(失效模式)<br/>
@CachePut:不影响方法执行更新操作(双写模式)<br/>
@Caching:组合以上多个操作<br/>
@CacheConfig:在类级别共享缓存的相同配置<br/>
(1)开启缓存功能@EnableCaching<br/>
(2)只需要使用注解就能完成缓存操作<br/>

默认行为：<br/>

1)如果缓存中有，方法不调用。<br/>
2)key默认自动生成，缓存的名字`::SimpleKey{}(自主生成的key值)`<br/>
3)缓存的value值，默认使用jdk序列化机制，将序列化后的数据存到redis<br/>
4)默认ttl时间-1

自定义：<br/>

1)指定生成缓存使用的key:key属性指定，接受一个SpEL表达式<br/>
2)指定缓存数据的存活时间:在配置文件中设置`spring.cache.redis.time-to-live`的值<br/>
3)将数据保存为json格式<br/>

不足：<br/>

读模式：<br/>
1)缓存穿透：查询一个null数据。解决：缓存空数据；cache-null-values=true可以解决。<br/>
2)缓存击穿：大量并发进来同时查询一个正好过期的数据。解决：加锁；默认无加锁，需要设置sync=true（只有@Cacheable有）使其调用synchronized方法可以解决。<br/>
3)缓存雪崩：大量的key同时过期。解决：加随机时间；加上过期时间time-to-live可以解决。<br/>

总结：常规数据（都多写少，即时性、一致性要求不高的数据）完全可以使用spring-cache；写模式（只要缓存
的数据有过期时间就足够了）。特殊数据，特殊设计。<br/>

26、异步<br/>

1 )继承Thread【不能的得到返回值、不能控制资源】
```java
Thread01 thread = new Thread01();
thread.start();
```
2 )实现Runable【不能的得到返回值、不能控制资源】
```java
Runable01 runable = new Runable01();
new Thread(runable).start();
```
3 )实现Callable接口+FutureTask（可以拿到返回结果，可以处理异常）【不能控制资源】
```java
FutureTask<Integer> futureTask = new FutureTask<>(new Callable01());
new Thread(futureTask).start();
Integer integer = futureTask.get();
```
4 )线程池【可以控制资源、性能稳定】

七大参数：

1 )corePoolSize:核心线程数【一直存在，除非（allowCoreThreadTimeOut）】；线程池，创建好后就准备就绪的线程数量，
就等待来接受异步任务去执行。<br/>
2 )maximumPoolSize:最大线程数量，控制资源。<br/>
3 )keepAliveTime:存活时间。如果当前的线程数量大于core数量，只要线程空闲时间大于keepAliveTime，就
释放空闲的线程（maximumPoolSize-corePoolSize）。<br/>
4 )unit:时间单位<br/>
5 )BlockingQueue<Runnable>:阻塞队列。如果任务有很多，就会将目前多的任务放在队列中。只要有线程空闲，就会
去队列里面取出新的任务继续执行。<br/>
6 )threadFactory:线程的创建工厂。<br/>
7 )RejectedExecutionHandler:如果队列满了，按照指定的拒绝策略拒绝执行任务。

工作顺序(new ThreadPoolExecutor())：

1 )线程池创建，准备好了core数量的核心线程，准备接受任务。<br/>
2 )core满了，就将再进来的任务放入阻塞队列中，空闲的core就会自己去阻塞队列获取任务执行。<br/>
3 )阻塞队列满了，就直接开新线程执行，最大只能开到max指定的数量。<br/>
4 )max满了就用RejectedExecutionHandler拒绝任务。<br/>
5 )max都执行完成，有很多空闲。在指定的时间keepAliveTime后，释放（max-core）这些线程<br/>
new LinkedBlockingQueue<>():默认是Integer的最大值。

Executors创建线程池：

Executors.newCachedThreadPool() 【core是0，所有的都可以回收】<br/>
Executors.newFixedThreadPool() 【固定大小，core=max，都不可以回收】<br/>
Executors.newScheduledThreadPool() 【定时任务的线程池】<br/>
Executors.newSingleThreadExecutor() 【单线程的线程池，后台从队列里获取任务逐个执行】

27、CompletableFuture异步编排（jdk1.8以后）

1 )创建异步对象（4个静态方法）<br/>
```java
//没有返回值，异步
public static CompletableFuture<Void> runAsync(Runnable runnable);
public static CompletableFuture<Void> runAsync(Runnable runnable,Executor executor);

//有返回值，异步
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier);
public static <U> CompletableFuture<U> supplyAsync(Supplier<U> supplier,Executor executor);
``` 

2 )计算完成时回调方法
```java
//非异步，参数：result(返回值)、Throwable(异常值)
public CompletableFuture<T> whenComplete(BiConsumer<? super T,? super TThrowable> action);

//异步，参数：result(返回值)、Throwable(异常值)
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super TThrowable> action);
public CompletableFuture<T> whenCompleteAsync(BiConsumer<? super T,? super TThrowable> action,Executor executor);

//非异步，参数：Throwable(异常值)
public CompletableFuture<T> exceptionally(Function<Throwable,? extends T> fn);
```

3 )handle方法（和complete一样，可对结果做最后的处理：可处理异常、可改变返回值。）
```java
public <U> CompletionStage<U> handle(BiFunction<? super T,Throwable,? extends U> fn);
public <U> CompletionStage<U> handleAsync(BiFunction<? super T,Throwable,? extends U> fn);
public <U> CompletionStage<U> handleAsync(BiFunction<? super T,Throwable,? extends U> fn,Executor executor);
```

4 )线程串行化方法
```java
//获取上一个任务返回的结果，并返回当前任务的返回值。
public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn);
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn);
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn,Executor executor);

//消费处理结果。接收任务的处理结果，并消费处理，无返回结果。
public CompletionStage<Void> thenAccept(Consumer<? super T> action);
public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);

//只要上面的任务执行完成，就执行thenRun。
public CompletionStage<Void> thenRun(Runnable action);
public CompletionStage<Void> thenRunAsync(Runnable action);
public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);
```

5 )两任务组合 - 都要完成
```java
//组合两个future，获取两个future的返回结果，并返回当前任务的返回值。
public <U,V> CompletableFuture<V> thenCombine(
    CompletionStage<? extends U> other,
    BiFunction<? super T,? super U,? extends V> fn
);
public <U,V> CompletableFuture<V> thenCombineAsync(
    CompletionStage<? extends U> other,
    BiFunction<? super T,? super U,? extends V> fn
);
public <U,V> CompletableFuture<V> thenCombineAsync(
    CompletionStage<? extends U> other,
    BiFunction<? super T,? super U,? extends V> fn,
    Executor executor
);

//组合两个future，获取两个future任务的返回结果，然后处理任务没有返回值。
public <U> CompletableFuture<Void> thenAcceptBoth(
    CompletionStage<? extends U> other,
    BiFunction<? super T,? super U> fn
);
public <U> CompletableFuture<Void> thenAcceptBothAsync(
    CompletionStage<? extends U> other,
    BiFunction<? super T,? super U> fn
);
public <U> CompletableFuture<Void> thenAcceptBothAsync(
    CompletionStage<? extends U> other,
    BiFunction<? super T,? super U> fn,
    Executor executor
);

//组合两个future，不需要获取future的结果，只需要两个future处理完成后处理该任务。
public CompletableFuture<Void> runAfterBoth(
    CompletionStage<?> other,
    Runnable action
);
public CompletableFuture<Void> runAfterBothAsync(
    CompletionStage<?> other,
    Runnable action
);
public CompletableFuture<Void> runAfterBothAsync(
    CompletionStage<?> other,
    Runnable action,
    Executor executor
);
```

6 )两任务组合 - 一个完成
```java
//两个任务有一个执行完成，获取它的返回值，处理任务并有新的返回值。
public <U> CompletableFuture<U> applyToEither(
    CompletionStage<? extends T> other,
    Function<? super T, U> fn
);
public <U> CompletableFuture<U> applyToEitherAsync(
    CompletionStage<? extends T> other,
    Function<? super T, U> fn
);
public <U> CompletableFuture<U> applyToEitherAsync(
    CompletionStage<? extends T> other,
    Function<? super T, U> fn,
    Executor executor
);

//两个任务有一个执行完成，获取它的返回值，处理任务，没有新的返回值。
public CompletableFuture<Void> acceptEither(
    CompletionStage<? extends T> other,
    Consumer<? super T> action
);
public CompletableFuture<Void> acceptEitherAsync(
    CompletionStage<? extends T> other,
    Consumer<? super T> action
);
public CompletableFuture<Void> acceptEitherAsync(
    CompletionStage<? extends T> other,
    Consumer<? super T> action,
    Executor executor
);

//两个任务有一个执行完成，不需要获取future的结果，处理任务，也没有返回值。
public CompletableFuture<Void> runAfterEither(
    CompletionStage<?> other,
    Runnable action
);
public CompletableFuture<Void> runAfterEitherAsync(
    CompletionStage<?> other,
    Runnable action
);
public CompletableFuture<Void> runAfterEitherAsync(
    CompletionStage<?> other,
    Runnable action,
    Executor executor
);
```

7 )多任务组合
```java
//等待所有任务完成
public static CompletableFuture<Void> allOf(CompletableFuture<?>... cfs);

//只要有一个任务完成
public static CompletableFuture<Void> anyOf(CompletableFuture<?>... cfs);
```

28、认证服务

1)短信验证码使用阿里云的短信接口实现（将随机生成的验证码存入缓存，1分钟内不得重复发送，验证码有效时间为5分钟。使用令牌机制，验证码验证成功后立即删除。）

2)密码不能使用明文，使用不可逆的加密MD5&MD5盐值加密

Message Digest algorithm 5，信息摘要算法<br/>
1. 压缩性：任意长度的数据，算出的MD5值长度固定<br/>
2. 容易计算：从原数据计算出MD5值很容易<br/>
3. 抗修改性：对原数据进行任何改动，所得到的MD5值都有很大区别<br/>
4. 强抗碰撞性：想找到两个不同的数据，使他们具有相同的MD5值是非常困难的<br/>
5. 不可逆性<br/>

但是单纯的MD5很容易被破解。由于其抗修改性，通过彩虹表可以进行暴力破解。所以可以使用盐值加密，而盐值加密需要在数据库中有字段来存储盐值，
我们可以选择spring的BCryptPasswordEncoder。

3)通过OAuth2完成社交登录，使用用户授权得到的Code换取Access Token（只能使用一次），在使用Access Token访问开放接口获取信息。

4)分布式系统下session共享问题：

1. session不同步<br/>
2. session不能共享<br/>

解决方案：

1. session复制，Tomcat原生支持只需修改配置文件即可，但会占用高大量网络带宽、降低服务器群的业务处理能力。
2. 客户端存储，可以节约服务端资源，但由于不安全且cookie长度有限制，无法使用。
3. hash一致性，只需要修改nginx配置，将相同的ip负载均衡到同一台服务器，虽然服务器重启会导致部分session丢失、水平扩展后悔有部分用户路由不到正确的session，但由于
session本身有有效期，所以问题不是很大。
4. 统一存储，没有安全问题且不会丢失，但增加了一次网络调用且需要修改大量代码，但可以通过SpringSession完美解决。

需要指定作用域来解决子域session共享问题。

单点登录

登录逻辑通过单点登录服务器实现，在完成登录后将token保存在登录服务器的cookie中（使得在浏览器不关闭的情况下可以免登陆），再跳转回相应服务器。

29、购物车功能实现

购物车分为在线购物车和离线购物车，在登录后会将离线购物车中的商品添加进对应用户的购物车，并清空离线购物车。
且购物车中的信息需要持久化保存，所以需要使用持久化策略。由于购物车是高并发的读写操作，这对于关系型数据库有极大的负担，
故需以非关系型数据库为主，其中具有高并发读写优势的redis成为最好的选择。当然仅仅通过缓存来解决购物车是完全不够的，为了将
购物车中的信息持久化保存，还需要开启redis持久化。

在redis的数据结构设计时，需要在存储大量的商品信息，如skuId、商品的名称、数量、价格等。所以我们需要选择list、hash这样的数据类型，
由于购物车中的数据有大量的修改需求，而list查找修改的过程复杂，为了提高效率hash成为更好的选择。

离线购物车功能实现：<br/>
浏览器有一个cookie用于标识用户身份，一个月后过期；如果第一次使用购物车功能，会创建一个临时的用户身份，在浏览器保存后每次访问都会带上这个cookie。<br/>
已登录：有session就使用session<br/>
未登录：没有session使用cookie中的user-key<br/>
第一次：没有cookie就创建一个cookie<br/>
为了快速得到用户信息可以使用ThreadLocal（Map<Thread,Object>）在同一个线程共享数据。<br/>

30、使用RabbitMQ

1)引入amqp（JMS是java api，AMQP是标准）场景；RabbitAutoConfiguration就会自动生效。<br/>
2)给容器中自动配置了RabbitTemplate、AmqpAdmin、CachingConnectionFactory、RabbitMessagingTemplate。
所有的属性都是在@ConfigurationProperties(prefix="spring.rabbitmq")进行绑定。<br/>
3)@EnableRabbit开启相关功能<br/>
4)监听消息@RabbitListener:类+方法(可以标在类上，用于监听哪些队列即可)/@RabbitHandler:方法（重载区分不同的消息）（必须开启@EnableRabbit且Queue必须存在）<br/>
返回值类型为org.springframework.amqp.core.Message,方法参数（用于将返回值自动注入）可以写以下类型：<br/>
1. Message message:原生消息详细信息（头+体）
2. T<发送的消息类型>:spring会自动转换为发送时对应类型的实体类
3. Channel channel:当前传输数据的通道

为了保证消息不丢失，可靠抵达，可以使用事务消息但会使性能大幅下降，所以使用确认机制。
1. confirmCallback（Publisher）:Broker收到消息后就会调用该回调方法，表示消息以成功传递到服务器。
2. returnCallback（Publisher）:消息到达Broker后，消息到到Exchange然后投递给Queue队列，如果没有抵达队列则会调用该回调方法。
3. ack（Consumer）:Consumer正确接收到消息就会调用该回调方法并删除队列中的消息，如果没有接收到则会进行重新投递等操作。

定制RabbitTemplate

confirmCallback

1. 开启发送端确认spring.rabbitmq.publisher-confirm-type=correlated（none不开启）/spring.rabbitmq.publisher-confirms=true(已经deprecated)
2. 配置中setConfirmCallback设置确认回调（@PostConstruct  //对象创建完成后，执行这个方法）

returnCallback

1. 开启发送端消息是否抵达队列的确认spring.rabbitmq.publisher-returns=true
2. 设置只要抵达队列，以异步发送回调returnConfirm（最好开启）spring.rabbitmq.template.mandatory=true
3. 配置中setReturnCallback设置回调。

ack（保证每个消息都被正确消费，才可以让broker删除这个消息）

1. 默认是自动确认的，只要消息收到客户端会自动确认，服务端就会移除这个消息。（自动回复消息容易丢失）
2. 将确认模式设置为手动spring.rabbitmq.listener.simple.acknowledge-mode=manual。
3. 通过channel.basicAck手动确认消息，channel.basicNack和channel.basicReject拒绝接收信息（设置requeue决定是否重新入队）。

31、支付接口幂等性

防止用户多次点击按钮、游湖页回退再次提交、微服务互相调用、feign触发重试机制等问题

解决方案：

1. token机制（也可以是验证码，获取和删除令牌要保证原子性）：在服务器存储令牌，在用户发送请求时带上令牌，在收到请求后会将客户端携带的令牌与服务器中的令牌对比。
如果结果一直则删除服务器中存储的令牌并执行接下来的业务，在后续请求中如果仍未原来的令牌，则无法进行后续业务的执行。
2. 锁机制（乐观锁、悲观锁、分布式锁）
3. 唯一约束
4. 防重表
5. 全局请求唯一id

32、分布式事务

仅在方法上标注@Transactional使用本地事务，尽管可以通过抛出异常的方式解决分布式事务需求，但仍存在问题：
1. 远程服务假失败：远程服务其实成功了，但是由于网络故障等没有返回；导致订单回滚，但库存扣减。
2. 远程服务执行完成，下面的方法出现问题：导致已执行的远程请求不能回滚。

本地事务只能控制自己的回滚，控制不了其他的服务回滚。

分布式事务的方案（刚性事务：遵循ACID原则，强一致性；柔性事务：遵循BASE理论，最终一致性）：

1. 2PC模式（XA协议）：协议比较简单，且商业数据库大多实现XA协议，使用成本低。但是性能不理想，不适用于高并发场景。
2. 柔性事务-TCC事务补偿型方案：自定义prepare、commit、rollback逻辑，即支持把自定义的分支事务纳入到全局事务的管理中。
3. 柔性事务-最大努力通知方案：不保证数据一定能通知成功，但会提供可查询操作接口进行校对。（允许大并发）
4. 柔性事务-可靠消息+最终一致性方案（异步确保型）：（允许大并发）

SEATA（默认AT，基于2PC的一种演变）：

1. TC-事务协调者：维护全局和分支事务的状态，驱动全局事提交或回滚。
2. TM-事务管理器：定义全局事务的范围：开始全局事务、提交、或回滚全局事务。
3. RM-资源管理器：管理分支事务处理的资源，与TC交谈以注册分支事务和报告分支事务的状态，并启动分支事务提交或回滚。

1）给每个微服务数据库添加undo_log表
```sql
CREATE TABLE `undo_log` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
  `branch_id` BIGINT(20) NOT NULL,
  `xid` VARCHAR(100) NOT NULL,
  `context` VARCHAR(128) NOT NULL,
  `rollback_info` LONGBLOB NOT NULL,
  `log_status` INT(11) NOT NULL,
  `log_created` DATETIME NOT NULL,
  `log_modified` DATETIME NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ux_undo_log` (`xid`,`branch_id`)
) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8;
```
2）安装事务协调器，seata-server：https://github.com/seata/seata/releases

3）引入依赖
```xml
        <dependency>
            <groupId>com.alibaba.cloud</groupId>
            <artifactId>spring-cloud-starter-alibaba-seata</artifactId>
        </dependency>
```
4）启动seata-server
1. registry.conf:注册中心配置，修改为nacos并设置nacos地址
2. 使用seata-server.bat启动seata服务器

5）在对应的RM上标注@GlobalTransactional（同时该方法也要开启事务@Transactional）

6）所有需要用到分布式事务的微服务使用seata都需要DataProxy代理自己的数据源。
```java
@Configuration
public class MySeataConfig {

    @Autowired
    DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource(DataSourceProperties dataSourceProperties){
        HikariDataSource dataSource = dataSourceProperties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
        if (StringUtils.hasText(dataSourceProperties.getName())){
            dataSource.setPoolName(dataSourceProperties.getName());
        }
        return new DataSourceProxy(dataSource);
    }

}
```
7）每个微服务都必须导入registry.conf和file.conf,并修改file：service.vgroup_mapping.${spring.application.name}-seata-service-group，作为服务名注册到seata-server上。<br/>
也可以通过在配置文件中配置spring.cloud.alibaba.seata.tx-service-group修改配置，但必须和file.conf中的配置保持一致。

8）给分布式事务的入口标注@GlobalTransactional，每一个远程调用的小事务标注@Transactional即可。

在高并发的场景下，如支付模块需要不能通过AT模式实现，为了保证高并发应该使用柔性事务-可靠消息+最终一致性方案（异步确保型）

为了使seata分布式事务可以满足高并发的需求，可以通过定时任务来解决。如设置30分钟定时任务，在到达时间后检查订单是否完成，如果未完成则控制事务回滚。<br/>
但是定时任务消耗内存，增加数据库的压力且存在较大的时间误差。可以通过RabbitMQ的延时队列（即消息TTL和死信Exchange结合）来解决。由于RabbitMQ的惰性检查机制，推荐在队列上设置TTL。

通过RabbitMQ延时队列实现柔性事务：

1. 实现库存延时队列：<br/>
1)在订单调用远程方法（@Transactional）锁定库存时，如果有库存则锁定库存并发送消息到TTL的队列/如果在锁定库存时出现异常则回滚事务。<br/>
2)当TTL消息（60min）过期成为死信后，会转发到死信交换机（这里死信交换机和发送消息时的交换机为同一个topic），并根据路由key到死信队列。<br/>
3)监听器StockReleaseListener会监听死信队列中的消息，接受到消息后会调用unlockStock(StockLockedTo to)，在该方法中会判断是否锁定库存，
如果已经锁定库存会查询订单状态，如果查询不到订单或状态为已取消则会调用unlockStock(skuId,wareId,num,taskDetailId)对数据库进行解锁库存操作。<br/>
4)如果出现异常则会拒绝接收消息并重新放回队列/如果成功完成解锁操作则会接收消息。
2.实现订单延时队列：<br/>
1)在发送提交订单请求时（@Transactional）会创建订单，如果完成创建订单则会发送消息到TTL的队列/如果创建订单失败则会回滚事务。<br/>
2)当TTL消息过期（30min）成为死信后，会转发到死信交换机（这里死信交换机和发送消息时的交换机为同一个topic），并根据路由key到死信队列。<br/>
3)监听器OrderCloseListener会监听死信队列中的消息，接收到消息后会调用closeOrder(OrderEntity)关闭订单并发送消息到库存的死信队列。<br/>
4)如果出现异常则会拒绝接收消息并重新放回队列/如果成功完成解锁操作则会接收消息。
3.为了避免网络延迟，实现取消订单消息：<br/>
1)监听器StockReleaseListener会监听死信队列中的消息，如果接收到消息的类型为OrderTo则会调用unlockStock(orderTo)进行库存解锁。<br/>
2)如果出现异常则会拒绝接收消息并重新放回队列/如果成功完成解锁操作则会接收消息。

为了保证可靠消息：
1. 消息发送出去但由于网络问题没有抵达服务器：可以对每个消息做好日志记录，定期扫描将失败的消息重新发送。
2. 消息抵达Broker但尚未持久化就宕机：使用生产者的确认机制（ConfirmCallback说明服务器收到，ReturnCallback说明报错需要修改日志中的消息状态）。
3. 消费者收到消息没有处理就宕机：开启手动确认机制。
4. 消息重复：将业务设置成幂等性的。
5. 消息积压：增加消费者或先将消息取出记录在数据库离线处理。


33、秒杀功能模块

使用定时任务（cron表达式），将秒杀商品提前存储到redis中上架。<br/>
异步运行spring的定时任务(spring中只允许6位组成，不允许第7位年。周一-周日：1-7；默认是阻塞的)

定时任务：
1. @EnableScheduling（类）开启定时任务
2. @Schedule（方法，cron=""）开启一个定时任务
3. 自动配置类TaskSchedulingAutoConfiguration

异步任务
1. @EnableAsync（类）开启异步任务功能
2. @Async（方法）标注在需要开启异步执行的方法上
3. 自动配置类TaskExecutionAutoConfiguration属性绑定在TaskExecutionProperties

通过redisson信号量实现库存扣减工作和限流以满足高并发需求。

通过幂等性避免重复上架。

#### debug
1、 父Pom报错<br/>

在Maven中，< dependencyManagement >标签提供了一种统一管理依赖版本号的方式。通常用在项目的最顶层的父POM中。<br/>
原因：dependencyManagement中只是声明依赖，而不实现引入。所以在声明前，应该确保对应版本的依赖已经下载到了本地仓库。<br/>

2、 微服务Run Dashboard未开启<br/>
.idea > workspace.xml 中添加如下配置，重启idea
```$xml
<component name="RunDashboard">
    <option name="configurationTypes">
      <set>
        <option value="SpringBootApplicationConfigurationType" />
      </set>
    </option>
    <option name="ruleStates">
      <list>
        <RuleState>
          <option name="name" value="ConfigurationTypeDashboardGroupingRule" />
        </RuleState>
        <RuleState>
          <option name="name" value="StatusDashboardGroupingRule" />
        </RuleState>
      </list>
    </option>
  </component>
```


3、 nacos集群启动失败（Linux）

(1)需要自行下载1.8以上的jdk，最好不要用自带的openjdk。（需要重新配置javahome）<br/>
`vim /etc/profile`<br/>
`source /etc/profile`<br/>
(2)数据库为未配置<br/>
linux中nacos默认集群启动，如果单机启动需要使用命令<br/>
`./start.sh -m standalone`<br/>
集群启动时不能使用内置的数据库，必须配置数据库（mysql）<br/>
ps：可以在logs/start.out查看启动信息，如果有报错会在/bin下生成错误的日志文件<br/>
可以使用`ps -ef | grep nacos | grep -v grep | wc -l`查看启动数量<br/>

4、linux防火墙相关命令

永久开启端口`firewall-cmd --zone=public --add-port=80/tcp --permanent`<br/>
重启防火墙`firewall-cmd --reload`<br/>
查看防火墙开启端口`firewall-cmd --list-port`<br/>
关闭防火墙（仅一次）`systemctl stop firewalld`<br/>
ps:CentOS7之后为systemctl，CentOS7之前为service<br/>

5、cmd可以正常运行npm，但在vscode上报错<br/>
powershell-Kill Terminal（可以尝试管理员运行）<br/>

6、nacos1.4.2在Windows系统中无法添加命名空间<br/>

使用linux系统的nacos（在windows系统中nacos有很多功能无法使用和bug）<br/>

7、中文乱码问题<br/>

前端->后端乱码：meta标签中设置charset="UTF-8"<br/>
后端->数据库乱码：url后添加?useUnicode=true&characterEncoding=utf-8<br/>

8、SpringBoot2.3.0之后@Valid失效<br/>

从 springboot-2.3开始，校验包被独立成了一个 starter组件，所以需要引入。可以选择降版本或引入依赖 spring-boot-starter-validation；
```xml
    <dependency>
         <groupId>org.springframework.boot</groupId>
         <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
```

9、前端获取品牌信息时报缺少pubsub.js的警告导致无法获取品牌。<br/>

1）执行`npm install --save pubsub-js`或`cnpm install --save pubsub-js`命令通过npm下载pubsub。<br/>
2）在src下的main.js中引用：
```javascript
import PubSub from 'pubsub-js'
Vue.prototype.PubSub = PubSub
```
3）引入pubsub-js后仍出现报错：this.PubSub.publish is not a function<br/>
将this.去掉即可<br/>

10、mybatis在标注了@TableId的属性默认是主键自增，部分表中没有自增需要输入。这导致在mapper映射文件得到的sql语句中没有id主键的插入属性，
可以修改原本的注释为@TableId(type = IdType.INPUT)<br/>

11、在引入elasticsearch-rest-high-level-client时由于SpringBoot的依赖中设置了elasticsearch.version,
导致elasticsearch-rest-high-level-client依赖的版本不一致。且可能由于项目结构原因，在父pom和子pom中的版本覆盖都
没有办法将版本修改。只能通过exclusion排除版本异常的依赖，在另行引入合适版本的依赖包。<br/>

12、通过nginx反向代理给网关，网关通过Host断言进行请求转发出现问题。<br/>
原因：nginx代理给网关的时候，会丢失请求的host信息。<br/>
解决方案：需要在nginx的server块中的location块修改配置：`proxy_set_header Host $host;`<br/>

13、产生堆外内存溢出 OutOfDirectMemoryError<br/>

springboot2.0以后默认使用lettuce作为操作redis的客户端。使用netty进行网络通信。
lettuce的bug导致netty堆外内存溢出。 netty如果没有指定堆外内存，默认使用-Xmx。
可以通过-Dio.netty.maxDirectMemory进行设置。<br/>
解决方案：不能使用-Dio.netty.maxDirectMemory只去调大内存，只是延缓。<br/>
1、升级lettuce（底层使用netty，吞吐量大）<br/>
2、切换使用jedis（很久没更新）<br/>
lettuce和jedis都是操作redis的底层客户端，由spring再次封装成RedisTemplate。<br/>

14、ElasticSearch的range检索部分范围无法查询到结果

范围：90-10000<br/>
可能原因：mapping为keywords<br/>
待解决

15、修改session的作用域时报错：Invalid cookie domain

需要将作用域修改到父域，前面不需要.

16、Feign远程调用丢失请求头的问题

在远程调用的时候会新创建一个请求，所以在这个请求中没有任何请求头，所以在远程调用的时候就会导致请求头丢失的问题。
由于没有请求头，购物车服务就会认为没有登录。<br/>
解决方案：在执行远程调用的时候会经过很多请求拦截器，如果有会逐个调用请求拦截器的apply()来丰富请求模板。
我们可以增加Feign远程调用的请求拦截器。通过获取当前页面的请求头并将其同步到新的请求中。

17、Feign异步请求丢失上下文的问题

在之前是通过ThreadLocal存储登录信息和对应的请求头信息，在开启异步后，使用不同的线程执行任务没法共享数据。<br/>
解决方案:在开启异步之前获取到RequestContextHolder中的请求参数并在每一个异步线程中将请求参数添加进去即可。

18、Error resolving template [currentUserCartItems]模板解析异常

原因：由于是远程调用的方法，为了方便获取在Controller的方法中直接返回一个集合对象，但是由于有Thymleaf模板引擎，
且直接将对象返回无法解析，所以必须在对应方法上添加@ResponseBody。

19、endpoint format should like ip:port引入分布式事务seata报错

原因:
1. 版本不匹配。由于SpringCloudAlibaba版本为2.2.6所以引入的seata-starter也是2.2.6，依赖的是seata-all版本为1.3.0，所以我们需要的seata服务器也要1.3.0。
2. vgroupMapping.fsp_tx_group后面的值需要与registry.conf中cluster的值相匹配
3. service的vgroupMapping在1.0.0后改为驼峰命名法，如果没有修改会报错
4. service的vgroupMapping后的组名信息应该为${服务名}-seata-service-group，在1.0.0后的版本按照官网说明的-fescar-service-group会异常，原因还不知道。
5. seata1.0.0后需要自己添加service
6. 连接db会出现异常，为seata failed load driverclass in either of hikariconfig class loader...

官方解释：

file.conf 的 service.vgroup_mapping 配置必须和spring.application.name一致<br/>
在 org.springframework.cloud:spring-cloud-starter-alibaba-seata 的org.springframework.cloud.alibaba.seata.GlobalTransactionAutoConfiguration 类中，默认会使用 ${spring.application.name}-fescar-service-group作为服务名注册到 Seata Server上，如果和file.conf 中的配置不一致，会提示 no available server to connect错误
<br/>也可以通过配置 spring.cloud.alibaba.seata.tx-service-group修改后缀，但是必须和file.conf中的配置保持一致

待解决!!!

20、no suitable HttpMessageConverter found for response type and content type

原因：由于远程调用order服务，没有携带登录信息被interceptor拦截。<br/>
解决方案：在interceptor中排除对该远程调用的拦截

#### 编写习惯

1、在编写Mybatis的mapper映射文件时，最好写上ResultMap标签。把实体类上的属性和数据库中的属性一一对应（哪怕名字一样或按规范命名）<br/>

2、在Mybatis中dao用@Mapper注解标注而不要使用@Repository。当方法的参数有多个时要用@Param注解标注给每个参数起名字（省的在获取参数时麻烦）。<br/>

3、Restful风格的请求经常需要给@RequestMapping添加method属性，方便起见，我们可以用@GetMapping\@PutMapping\@PostMapping\@DeleteMapping代替。<br/>

4、在使用SRS303规范校验时@valid和@Validate都是用于激活校验，@Validate开启分组校验（需要在属性最后中添加{class（空接口）}属性）。且在生产环境中实体类上的
校验注解最好戴上message属性用于添加校验异常信息，是否使用分组校验视情况而定。<br/>

5、在Controller的参数中：<br/>

1）使用@RequestBody必须是post请求，springmvc会自动将请求体的数据（json），转换为对应的对象。<br/>
2）使用@PathVariable，用于获取请求路径中的参数。在Restful风格中{参数名}，注解的属性要与请求路径中的属性。<br/>
3）使用@RequestParam，用于接受的参数是来自HTTP请求体或请求url中的参数。也可以用于接受POST、DELETE等其他请求，
处理 Content-Type 为 application/x-www-form-urlencoded 编码的内容，不推荐使用@RequestParam接收application/json，这时候就需要使用到@RequestBody。<br/>

6、实体类中的属性会有为了实现某种业务而添加的属性，但不存在用于数据库中。可以使用@TableField(exist = false)标注。
但这样不规范，包括JsonInclude、校验等注解都不应该直接标注在PO上，需要对Object进行划分：<br/>

PO（持久对象，对应数据库中的记录）<br/>
DO（领域对象，抽象出来的业务实体）<br/>
TO（数据传输对象，微服务之间传输和发送的对象）<br/>
DTO（数据传输对象，和TO类似）<br/>
VO（值对象，视图对象，数据库中没有的信息,用于接受页面传递来的数据，封装对象。除此之外将业务处理完成的对象，封装成页面要用的数据）<br/>
BO（业务对象，主要作用是把业务逻辑封装为一个对象，这个对象可以包括一个或多个其他的对象）<br/>
POJO（简单无规则的java对象）<br/>
DAO（数据访问对象）<br/>
当需要将VO等赋值给PO时逐个使用set方法代码量大且麻烦，可以使用BeanUtils.copyProperties完成需求。<br/>

7、封装实体类中，部分属性会有空值。在生成json字符串时会对业务产生影响，可以标注@JsonInclude(JsonInclude.Include.NON_EMPTY)<br/>

8、由于dao使用的是@Mapper注解，而@Mapper注解并不是spring的原生注解，所以使用@Autowired时候会有报错，但不影响使用。
如果不希望报错，可以使用jdk原生的注解@Resource。<br/>

9、后端业务在前后端分离的模式下，都是返回json字符串。一般规范都是定义一个实体类，定义成功或失败方法（静态），并将业务数据put进data中。
所以controller的方法上都需要标注@ResponseBody注解，为了方便起见，可以用@RestContoller注解标注在Controller类上来替换。<br/>

10、大数据量的访问不建议多表连接查询，不建议设置外键。<br/>

11、Controller：处理请求，接受和校验数据。<br/>
Service接受Controller传来的数据，进行业务处理。<br/>
Controller接受Service处理完的数据，封装页面制定的vo。<br/>
当调用其他业务逻辑直接注入Service，虽然可以直接注入Dao，但是Service可以有更丰富的业务逻辑。<br/>

12、宁可方法名很长也要见名知意。<br/>

13、和数据库相关的注解(@MapperScan、@EnableTransactionManagement)最好标注在mybatis相关的配置类上。<br/>

14、在使用SpringCache缓存时，每个需要缓存的数据都要指定要放到哪个名字的缓存。【缓存的分区（推荐按照业务类型分）】<br/>

15、在Mybatis的mapping映射文件中，只要有嵌套属性就要封装自定义结果集。

16、判断String是否为空最好用StringUtils.isEmpty，判断数组集合是否为空要除了要判断!=null之外还需要判断length/size!=0<br/>

17、在一个服务中有多个远程调用的需求就使用多线程，最好是异步编排（可以解决复杂的逻辑需求）。

18、在将数据存入redis中时，需要对数据进行序列化（默认是jdk的序列化），为了方便使用应该使用`JSON.parseObject`和`JSON.toJSONString`
来使用json存储。且json可以在任何情况下使用，而不局限于java。

19、SpringMvc重定向携带数据用RedirectAttributes(addFlashAttribute是将数据放在session中可以在页面取出，但只能取一次；
addAttribute是将数据拼接在路径上),快速获取cookie的值可以用@CookieValue

20、远程调用最好用PostMapping和RequestBody


#### 参与贡献

本项目参考：尚硅谷-谷粒商城、人人开源



