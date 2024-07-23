![KaTool](http://7n.cdn.wzl.fyi/typora/img/KaTool.png)
&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;&emsp;![SpringBoot-2.7.0](https://img.shields.io/badge/SpringBoot-2.7.0-green)&emsp;&emsp;&emsp;![Hutool-5.8.10](https://img.shields.io/badge/Hutool-5.8.10-green)
# KaTool (卡托)
KaTool - 一款拥有七牛云文件处理、IP工具、IO工具、图形验证码生成、随机验证码生成、Date工具、Base64工具、函数式接口、分布式锁实现的Java开发工具类，依赖于SpringBoot框架<br>
**Tips：该Starter为个人项目使用，Starter制作仅满足于个人目前的开发需求，也只是个人开发，目前还在孵化阶段，工具类和其他同类作品相比不全面望谅解，在图片写入OutputStream时，使用到了Hutool**
> 官方文档：http://katool.cn/ (如果还在备案，请访问http://katool.wzl.fyi/)
## 快速入门
### Maven中央仓库引入
#### pom.xml
在自己的项目中引入依赖
```xml
<!-- https://mvnrepository.com/artifact/cn.katool/KaTool -->
<dependency>
  <groupId>cn.katool</groupId>
  <artifactId>KaTool</artifactId>
  <*version>{{KaTool.version}}</version>
        </dependency>
```
### 本地部署
#### git clone(仅第一次使用)
```shell
git clone https://github.com/Karosown/KaTool.git
```
#### 打开项目
选择Maven install
![image-20231123141406759](http://7n.cdn.wzl.fyi/typora/img/image-20231123141406759.png)
## Application.yml配置说明
使用分布式锁前请开启Redistemplate的事务支持
### V1.9.5 GAMA之前
```yaml
katool:
    qiniu:
        accessKey: 						# 你的七牛云accessKey
        secretKey: 						# 你的七牛云secretKey
        bucket:  						# 空间名称
        zone:  							# 存储区域
        domain:  						# 访问域名
        basedir:  						# 文件存储根目录
    lock:
        internalLockLeaseTime: 30    	# 分布式锁默认租约时间，建议别设太小，不然和没有设置毫无区别
        timeUnit: seconds            	# 租约时间单位
    util:
        redis:
            policy: "caffeine"       	# 选择内存缓存策略，caffeine
            exp-time: {5*60*1000}       # LFU过期时间
            time-unit: milliseconds   	# 过期时间单位
```
### V1.9.5 GAMA及之后
```yaml
katool:
    qiniu:
        accessKey: 						# 你的七牛云accessKey
        secretKey: 						# 你的七牛云secretKey
        bucket:  						# 空间名称
        zone:  							# 存储区域
        domain:  						# 访问域名
        basedir:  						# 文件存储根目录
    auth:
        salt-key: "katooltest"   		# JWT加密盐值，默认值为katool.salt.version::Katool版本号
        exp-time: { 7*24*60*60*1000 }   # JWT过期时间，默认值为7天
        token-header: "Authorization"   # 请求头中存放token的Header，默认值为"Authorization"
    cache:
        policy: "caffeine"      		# 选择内存缓存策略，caffeine
        exp-time: { 5*60*1000 }         # LFU过期时间
        time-unit: milliseconds 		# 过期时间单位
    redis:
        policy: "default"       		# 多级缓存策略模式选定，默认情况下和cache采用同一个策略，我cache使用的是啥，那么redis采用的策略就是啥
        lock:
            internalLockLeaseTime: 30   # 分布式锁默认租约时间
            timeUnit: seconds           # 租约时间单位
```
## Nginx配置
Nginx反向代理后获取真实来源IP
```Nginx.config-server
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header   X-Real-Port      $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
## RedisUtilConfig
如何自定义Redis多层缓存策略
```java
package cn.katool.katooltest.config;
import cn.katool.util.cache.policy.CachePolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
@Configuration
public class RedisUtilConfig {
    @Bean
    @DependsOn({"KaTool-Init"})
    @Primary
    public CachePolicy cachePolicy() {
        return null;
    }
}
```
如果使用默认策略（不采取缓存）直接`return new DefaultCachePolicy();`即可，KaTool自动装配了Caffeine的Cache，所以可以直接对CaffeineUtil进行使用
## 贡献提交规范
先将代码fork到自己的仓库，新建一个分支，代码编写好之后，请自行测试并配上测试截图到根目录下的`test.md`文件
test.md格式：
```markdown
# 新增/修复 贡献名称
# 你的个人简介（如果进入被参考，那么我们会将您计入到官网的贡献者中）
- 头像：
- Name：
- 简介/签名：
- 其他（包括但不限于邮箱/联系方式/博客/github/gitee）：
# 简介：
.....
# 测试结果：
.....
# 使用教程：
.....
```
KaTool-SpringBootTest测试框架github地址：https://github.com/Karosown/KaToolTest.git
## Git/Issues提交规范
### 什么是Git提交规范
Git是目前最流行的分布式版本控制系统，它能够帮助开发者高效管理项目代码。在进行Git操作时，我们需要对代码进行提交，以记录下每一次修改的内容。而Git提交规范则是指在代码提交时，根据一定的格式要求进行提交信息的书写，并在注释中尽可能详细地记录修改的内容，以方便其他人查看。
### Git提交规范的重要性
1. 提高协作效率：当多人协同开发时，不规范的提交信息很容易让别人无法理解代码的变更，从而延误项目进度。
2. 方便代码审查：优秀的提交注释能够帮助代码审核人员快速了解修改的内容，减轻审核负担。
3. 方便代码回退：在需要回退代码到某一个具体版本时，合理规范的Git提交信息能够方便地找到对应的版本，并快速恢复代码。
4. 维护项目历史记录：清晰明了的提交注释可以记录项目开发的历程，方便后期的维护和追溯。
### Git提交规范的要求
Git提交规范通常包括以下信息：
1. 标题（必填）：一句话简述本次提交的内容。
2. 空行：用于分隔标题和正文。
3. 正文（选填）：详细阐述本次提交的内容，可以包括具体修改的文件、代码功能、修复了哪些bug等。
4. 空行：用于分隔正文和注释。
5. 注释（选填）：对本次提交补充说明的信息，可以包括相关链接、参考文献等。
Git提交规范要求的格式通常如下：
```
<type>(<scope>): <subject>
<body>
<footer>
```
其中，表示本次提交的类型，常见的有以下几种：
- feat：新增功能
- fix：修复bug
- docs：修改文档
- style：修改代码风格
- refactor：重构代码
- test：增加或修改测试代码
- chore：修改构建过程或辅助工具
表示本次提交涉及到的模块或功能点。如果本次提交不涉及到具体模块或功能点，可以省略。
表示本次提交的简要说明，一般不超过50个字符。
表示本次提交的详细描述，可以包括多行。表示本次提交的注释，可以包括多行。 ## Git提交规范的代码示例 下面是一个示例代码，演示了如何按照Git提交规范进行代码提交： ```javascript git add . git commit -m "feat(login): 新增用户登录功能 新增了用户登录页面、登录表单提交接口及相关验证逻辑" ``` 在这个示例中，我们按照Git提交规范的格式书写了一条提交信息，其中为feat，表示本次提交新增了功能；为login，表示本次提交涉及到用户登录模块；为“新增用户登录功能”，简要说明了本次提交的内容；为“新增了用户登录页面、登录表单提交接口及相关验证逻辑”，详细描述了本次提交的内容。
## Update
v1.9.6
- ALPHA 2024/ 02 / 01
  - Redis缓存策略新增EhCache
  - 新增ExcelUtils
- BETA 2024/ 02 / 05
  - 修复Redis缓存问题，修改配置类为util
  - RedisUtil支持单个线程内关闭Cache缓存策略
- GAMA 2024/ 04 / 10
  - 修复分布式锁tryLock状态获取问题
  - 解决临时关闭多级缓存失效的问题。 2024.04.12
v1.9.5
- RELEASE 2024 / 01 /14
  - 修复分布式锁问题，进行双检锁优化
- GAMA 2023 / 12 /13
  - 新增阿里云OSS对象存储、腾讯云OSS对象存储issuse（有意向的可以参与开发，实现指定接口即可，参考七牛云开发）
  - 对七牛云文件存储进行包迁移，架构有一点变化，如果介意的不要更新
  - OSS存储配置格式统一化
    ```yaml
    katool:
    	store:
    		qiniu:
    			****
    		aliyun:
    			****
    		tencent:
    		  	****
    ```
  - 新增IDCardValidUtils工具类，支持15位身份证和18位身份证互转以及身份证合法性校验
  - 更新IPUtils，新增IP合法性校验
  - 新增AuthUtil，支持JWT和类格式快速转换
  - 删除MethodIntefaceUtil，因为这个很鸡肋，而且JDK已经有了Function等接口
  - 优化其他工具类架构
- BETA 2023 / 11 / 27
  - 新增`SpringContextUtils`来对SpringBean进行注册、判断、卸载
  - 新增`ClassUtil`来对类进行加载、类初始化，默认采用当前线程的类加载器为父类加载器（）
  - 新增`KaToolClassLoader`,可以自定义父类加载器，用于加载外部class文件（~~为什么这样做，不用UrlLoader，主要是之前项目写一个任务模块，需要从外部导入，但是使用UrlLoader来导入本地class文件没有用，所以我选择使用以字节加载进JVM，再生成对象~~）
- ALPHA 2023 / 10 / 16
  - 将RedisUtil并行获取ZSet数据加入日程
  - 10 / 19 新增LeftPopList和RightPopList并且添加代理，暂未经过严格测试
v1.9.4
- Release 2023 / 09 / 24
  - 添加@ConditionalOnMissingBean注解，用户自定义配置不需要使用@Primary注解
  - 解决内存取得null，不名字Redis的问题
v1.9.3 2023 / 09 /18
- 修改RedisUtil中Zset为ZSet，更符合命名标准
v1.9.2 2023 / 09 / 18
- 解决RedisUtil中，多参数多级分类策略异常问题
v1.9.1 2023 / 09 / 13
- 新增掠夺式、非掠夺式分布式锁（区别在于是否在指定时间之后自觉拿线程锁）
- 关闭分布式锁中的log.info，QPS优化到1000左右（3000并发）
v1.9.0 2023 / 09 /08 - 2023 / 09 / 11
- 新增策略模式，选取内存策略做到内存缓存策略，有效降低缓存穿透、击穿。
- 优化分布式锁，使用消息队列监听+FIFOUNI队列保证分布式锁的公平性与可靠性，看门狗改为使用`ScheduledThreadPoolExecutor`实现，单机模式下（排除网络IO）与synchronized性能差别不大
  20个线程
  ![image-20230910194932722](http://7n.cdn.wzl.fyi/typora/img/image-20230910194932722.png)
  ![image-20230910194945932](http://7n.cdn.wzl.fyi/typora/img/image-20230910194945932.png)
  150个线程
  ![image-20230910195006933](http://7n.cdn.wzl.fyi/typora/img/image-20230910195006933.png)
  ![image-20230910195030253](http://7n.cdn.wzl.fyi/typora/img/image-20230910195030253.png)
v1.8.1 2023 / 08 / 19 / 17：24<br>
优化分布式锁：采用 自旋锁+同步锁，但是并没有解决公平竞争锁的问题，如果要解决可以使用消息队列<br>
v1.8.0  2023 / 08 / 19 / 17：16<br>
修复RedisUtil出现多个LockUtil Bean的问题<br>
v1.8.0  2023 / 08 / 06 / 04：22<br>
修改LockUtil，利用Redistemplate实现可重入分布式锁<br>
RedisUtils新增ZSet和Set类型的支持<br>
v1.7.12 修复时间 2023 / 05 / 29 / 19:02<br>
解决强制配置七牛云服务的问题<br>
v1.7.11 修复时间 2023 / 05 / 29 / 10:39<br>
解决部分类自动装配失败问题<br>
原因：@Scope("Single") -> @Scope("singleton") 可能是最后全局替换的时候替换掉了<br>
v1.7.1  更新日期 2023 / 04 / 15 / 17:37<br>
5-8:对LockConfig进行优化<br>
新增分布式锁看门狗机制，零代码侵入，解决为使用分布式锁而选择Redssion的问题<br>
新增RedisUtils，对RedisTemplate进行封装，并且实现了分布式锁功能<br>
v1.6.1  更新日期 2023 / 02 / 09 / 01:13<br>
将expDateUtil移动只cn.katool.util下<br>
新增expBase64Util，扩展于org.springframework.util.Base64Utils<br>
v1.6.0  更新日期 2023 / 01 / 29 / 02:40<br>
包从com.Karos.KaTool改为cn.katool<br>
新增KaTool异常处理类<br>
基于hutool.DateUtil扩展开发expDateUtil，支持Corn和Date间的互相转换<br>
预计近日将上传Maven中央仓库<br>
v1.5.5<br>
GenerateCode类改名为GenerateCodeUtil<br>
大部分类新增日志记录<br>
v1.5.4<br>
分布式锁优化，新增分布式锁延期<br>
v1.5.3<br>
新增函数式接口，简化lambda开发<br>
v1.5.2<br>
分布式锁优化
v1.5.1<br>
分布式锁单例模式优化
v1.5.0<br>
锁工具类-分布式锁实现，新增启动Banner<br>
**Tips:使用锁工具类必须使用自动装配，我在工具类内部实现了单例模式**<br>
v1.4.8<br>
验证码生成优化<br>
v1.4.7<br>
七牛云文件管理新增通过URL获得源文件名方法<br>
优化Starter架构<br>
临时文件生成<br>
v1.4.6<br>
支持七牛云上传设定子目录<br>
v1.4.5<br>
支持七牛云上传设置上传目录<br>
v1.4.0<br>
新增：七牛云文件存在判断、文件强制上传<br>
增强：文件上传后cdn强制刷新<br>
v1.3.5<br>
Ip工具：避免Nginx反向代理，获得真实IP<br>
Nginx-Config的server中加上<br>
```Nginx.config-server
proxy_set_header   X-Real-IP        $remote_addr;
proxy_set_header   X-Real-Port      $remote_port;
proxy_set_header   X-Forwarded-For  $proxy_add_x_forwarded_for;
```
<br>
v1.3.0<br>
图形验证码生成<br>
V1.1.2<br>
图片与Base64互转<br>
V1.0.0<br>
七牛云<br>
## Todo
各个版本并行开发，如果有功能修复会整合到最新版本中。
1.9.6：
在高并发场景下，解决分布式trylock锁的稳定问题。
（目前有一个问题，因为trylock是不阻塞的拿锁，所以说会频繁请求Redis，但是通过几次对比来看，trylock都有可能在锁被占用的情况下拿到锁，这和Redis的网络多道IO处理和事务同步处理有关，为了缓解这一情况，目前的解决方案是增加一个随机休眠的时间，在trylock中，避免大量请求同时打到Redis)
1.9.7版本预计有以下更新：
- [ ] OSS文件上传多策略
- [ ] 对接市面上常用的文本大模型（GPT3.5/星火/kimi//文心一言等，并尽量使用统一接口进行整合）
- [ ] 对于SPI机制进行一些优化