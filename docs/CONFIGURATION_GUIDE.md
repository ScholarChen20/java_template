# 配置管理指南

## 目录
1. [配置文件](#配置文件)
2. [环境配置](#环境配置)
3. [配置属性](#配置属性)
4. [敏感信息管理](#敏感信息管理)
5. [配置验证](#配置验证)

## 配置文件

### 1.1 配置文件层次结构

```
application.yml                    # 主配置文件（通用配置）
├── application-dev.yml            # 开发环境配置（覆盖主配置）
├── application-test.yml           # 测试环境配置
├── application-prod.yml           # 生产环境配置
└── application-custom.yml         # 业务自定义配置
```

### 1.2 激活配置文件

```yaml
# application.yml
spring:
  profiles:
    active: dev  # 默认激活dev配置
```

#### 命令行激活

```bash
# 打包时指定环境
mvn clean package -DskipTests

# 启动时指定环境
java -jar app.jar --spring.profiles.active=prod
```

## 环境配置

### 2.1 开发环境 (dev)

```yaml
# application-dev.yml
spring:
  profiles: dev
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/yoyo_data_dev
    username: root
    password: 123456
  redis:
    host: localhost
    port: 6379
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update  # 开发环境自动更新表结构

mybatis-plus:
  configuration:
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl  # 输出SQL语句

logging:
  level:
    root: INFO
    com.example.yoyo_data: DEBUG  # 调试级别日志
```

### 2.2 测试环境 (test)

```yaml
# application-test.yml
spring:
  profiles: test
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://test-db:3306/yoyo_data_test
    username: test_user
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # 测试环境不修改表结构

logging:
  level:
    root: WARN
    com.example.yoyo_data: INFO
```

### 2.3 生产环境 (prod)

```yaml
# application-prod.yml
spring:
  profiles: prod
  datasource:
    url: ${DB_URL}  # 使用环境变量
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
    hikari:
      maximum-pool-size: 30  # 增加连接池大小
      minimum-idle: 10
  jpa:
    hibernate:
      ddl-auto: validate  # 生产环境不修改表结构

logging:
  level:
    root: WARN
    com.example.yoyo_data: INFO  # 生产环境只记录INFO及以上级别
```

## 配置属性

### 3.1 配置属性类

配置属性类使用 `@ConfigurationProperties` 注解绑定配置文件：

```java
@Data
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {
    private String driverClassName;
    private String url;
    private String username;
    private String password;
}
```

### 3.2 Redis配置

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 123456
    timeout: 5000
    database: 0
    lettuce:
      pool:
        min-idle: 8          # 最小空闲连接
        max-idle: 500        # 最大空闲连接
        max-active: 2000     # 最大活跃连接
        max-wait: 10000      # 最大等待时间(ms)
```

### 3.3 MySQL配置

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/yoyo_data
          ?useUnicode=true
          &characterEncoding=UTF-8
          &serverTimezone=Asia/Shanghai
          &useSSL=false
    username: root
    password: 123456
    hikari:
      maximum-pool-size: 20      # 最大连接数
      minimum-idle: 5            # 最小空闲连接
      connection-timeout: 30000   # 连接超时时间
      idle-timeout: 600000        # 空闲超时时间
      max-lifetime: 1800000       # 最大生存时间
```

### 3.4 Kafka配置

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    producer:
      acks: all              # 所有副本都收到消息才认为发送成功
      retries: 3             # 重试次数
      batch-size: 16384      # 批处理大小
      linger-ms: 10          # 等待时间
      compression-type: snappy  # 压缩算法
    consumer:
      group-id: yoyo_data_group
      enable-auto-commit: true   # 自动提交偏移量
      auto-commit-interval-ms: 1000
      max-poll-records: 500      # 一次拉取的最大记录数
```

### 3.5 JWT配置

```yaml
jwt:
  secret: ${JWT_SECRET}           # 密钥
  expiration: 7200000             # 过期时间(ms)
  token-type: Bearer              # 令牌类型
  header-name: Authorization      # 请求头名称
```

### 3.6 MinIO配置

```yaml
minio:
  endpoint: http://localhost:9000
  accessKey: minioadmin
  secretKey: minioadmin
  bucketName: yoyo-data
  secure: false                   # 是否使用HTTPS
  connect-timeout: 10000
  write-timeout: 10000
  read-timeout: 10000
```

## 敏感信息管理

### 4.1 环境变量设置

使用环境变量存储敏感信息，配置文件中引用：

```yaml
# application.yml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/yoyo_data}
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:123456}

jwt:
  secret: ${JWT_SECRET:default-secret-key}
```

### 4.2 Docker环境设置

```dockerfile
FROM openjdk:8-jdk-alpine

# 设置环境变量
ENV DB_URL=jdbc:mysql://mysql:3306/yoyo_data
ENV DB_USERNAME=root
ENV DB_PASSWORD=${MYSQL_PASSWORD}
ENV JWT_SECRET=${JWT_SECRET}

COPY app.jar /app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 4.3 .env文件（开发环境）

创建 `.env` 文件在项目根目录（**不要提交到Git**）：

```
DB_URL=jdbc:mysql://localhost:3306/yoyo_data
DB_USERNAME=root
DB_PASSWORD=123456
JWT_SECRET=my-secret-key
REDIS_PASSWORD=123456
```

`.gitignore` 中添加：

```
.env
*.properties
application-local.yml
```

## 配置验证

### 5.1 配置绑定验证

```java
@Data
@Validated
@Component
@ConfigurationProperties(prefix = "spring.datasource")
public class DataSourceProperties {

    @NotBlank(message = "数据库URL不能为空")
    private String url;

    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @Positive(message = "连接超时时间必须为正数")
    private long connectionTimeout = 30000;
}
```

### 5.2 启动时配置验证

```java
@Configuration
public class ConfigValidationConfig {

    @Bean
    public ConfigValidator configValidator() {
        return new ConfigValidator();
    }
}

@Component
@Slf4j
public class ConfigValidator implements ApplicationRunner {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Autowired
    private RedisProperties redisProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        // 验证数据库配置
        if (!isValidUrl(dataSourceProperties.getUrl())) {
            throw new IllegalStateException("无效的数据库URL");
        }

        // 验证Redis配置
        if (redisProperties.getHost() == null || redisProperties.getHost().isEmpty()) {
            throw new IllegalStateException("Redis地址未配置");
        }

        log.info("配置验证成功");
    }

    private boolean isValidUrl(String url) {
        return url != null && !url.isEmpty() && url.startsWith("jdbc:");
    }
}
```

## 性能相关配置

### 6.1 数据库连接池配置

```yaml
spring:
  datasource:
    hikari:
      # 核心配置
      maximum-pool-size: 20      # 最多20个连接
      minimum-idle: 5            # 最少保持5个空闲连接

      # 超时配置
      connection-timeout: 30000   # 获取连接最多等待30秒
      idle-timeout: 600000        # 10分钟无使用自动关闭
      max-lifetime: 1800000       # 30分钟后连接自动关闭

      # 验证配置
      connection-test-query: SELECT 1
      leak-detection-threshold: 60000  # 60秒未归还连接告警
```

### 6.2 缓存配置

```yaml
spring:
  redis:
    lettuce:
      pool:
        # 根据CPU核心数和并发量调整
        min-idle: ${REDIS_MIN_IDLE:8}
        max-idle: ${REDIS_MAX_IDLE:500}
        max-active: ${REDIS_MAX_ACTIVE:2000}
        max-wait: 10000
        time-between-eviction-runs-millis: 30000
```

### 6.3 消息队列配置

```yaml
spring:
  kafka:
    producer:
      # 批处理提高吞吐量
      batch-size: 32768      # 32KB
      linger-ms: 100         # 等待100ms或达到批大小

      # 压缩降低网络传输
      compression-type: snappy

    consumer:
      # 单次拉取数量
      max-poll-records: 500

      # 处理超时
      session-timeout-ms: 10000
      heartbeat-interval-ms: 3000
```

## 配置热更新（高级）

### 7.1 使用Nacos（推荐）

```xml
<dependency>
    <groupId>com.alibaba.cloud</groupId>
    <artifactId>spring-cloud-starter-alibaba-nacos-config</artifactId>
</dependency>
```

```yaml
# bootstrap.yml
spring:
  cloud:
    nacos:
      config:
        server-addr: localhost:8848
        namespace: production
        group: yoyo_data
        file-extension: yml
```

### 7.2 使用Apollo

```xml
<dependency>
    <groupId>com.ctrip.framework.apollo</groupId>
    <artifactId>apollo-client</artifactId>
</dependency>
```

```properties
# application.properties
apollo.bootstrap.enabled=true
apollo.app.id=yoyo_data
apollo.bootstrap.namespaces=application
```

---

**版本**: 1.0.0
**最后更新**: 2026-02-02
