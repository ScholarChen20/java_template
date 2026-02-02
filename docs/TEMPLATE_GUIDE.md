# 企业级系统模板框架完整指南

## 目录
1. [快速开始](#快速开始)
2. [框架架构](#框架架构)
3. [核心组件](#核心组件)
4. [使用示例](#使用示例)
5. [扩展开发](#扩展开发)
6. [最佳实践](#最佳实践)
7. [常见问题](#常见问题)

## 快速开始

### 前置条件
- Java 8+
- Maven 3.6+
- MySQL 5.7+
- Redis 5.0+
- Spring Boot 2.3.12+

### 1分钟快速创建新模块

#### 步骤1：使用BaseEntity创建实体类

```java
@Data
@Builder
@TableName("products")
public class Product extends BaseEntity {

    @TableField("name")
    private String name;

    @TableField("description")
    private String description;

    @TableField("price")
    private BigDecimal price;
}
```

#### 步骤2：创建Mapper接口

```java
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
```

#### 步骤3：创建Service接口和实现

```java
public interface ProductService extends BaseService<Product> {
    List<Product> findByName(String name);
}

@Service
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product>
    implements ProductService {

    @Override
    public List<Product> findByName(String name) {
        return lambdaQuery()
            .like(Product::getName, name)
            .list();
    }
}
```

#### 步骤4：创建Controller

```java
@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController extends BaseController<Product, ProductService> {

    @GetMapping("/search")
    public Result<List<Product>> search(@RequestParam String name) {
        try {
            List<Product> products = baseService.findByName(name);
            return Result.success(products);
        } catch (Exception e) {
            log.error("搜索产品失败", e);
            return Result.error("搜索产品失败");
        }
    }
}
```

完成！你现在有一个完整的RESTful API服务。

## 框架架构

### 分层架构

```
┌─────────────────────────────────────────────┐
│         API层 (api/controller)               │
│  ├─ HTTP请求处理                            │
│  ├─ 异常处理 (@ControllerAdvice)            │
│  └─ 拦截器 (Interceptor)                    │
├─────────────────────────────────────────────┤
│     业务层 (service)                         │
│  ├─ 业务逻辑实现                            │
│  ├─ 事务管理 (@Transactional)                │
│  └─ 限流保护 (@RateLimit)                    │
├─────────────────────────────────────────────┤
│    数据层 (data / mapper)                    │
│  ├─ 数据查询                                │
│  ├─ 缓存管理                                │
│  └─ 消息队列                                │
├─────────────────────────────────────────────┤
│   基础设施层 (infrastructure)                │
│  ├─ 配置管理                                │
│  ├─ AOP切面                                 │
│  ├─ 工具函数                                │
│  └─ 异常处理                                │
├─────────────────────────────────────────────┤
│    支撑层 (support)                          │
│  ├─ 异常定义                                │
│  ├─ 常量定义                                │
│  └─ 响应格式                                │
└─────────────────────────────────────────────┘
```

## 核心组件

### 1. 基类 (infrastructure/base)

#### BaseEntity
所有实体的父类，提供通用字段：
- `id`: 主键
- `createdAt`: 创建时间
- `updatedAt`: 更新时间
- `isDeleted`: 软删除标记

```java
@Data
@TableName("users")
public class User extends BaseEntity {
    private String username;
    private String email;
}
```

#### BaseService
所有Service的接口，定义通用操作：
- CRUD操作
- 软删除
- 批量操作

#### BaseServiceImpl
BaseService的实现，继承MyBatis-Plus的ServiceImpl

#### BaseController
所有Controller的父类，提供通用API：
- GET /:id - 获取单条记录
- GET / - 获取所有记录
- POST / - 创建记录
- PUT /:id - 更新记录
- DELETE /:id - 删除记录
- POST /batch-delete - 批量删除
- DELETE /soft/:id - 软删除

### 2. 异常处理 (support/exception & api/handler)

#### 异常类型

| 异常类 | HTTP状态码 | 使用场景 |
|-------|---------|--------|
| BusinessException | 400 | 业务逻辑错误 |
| ValidationException | 400 | 参数验证失败 |
| SystemException | 500 | 系统内部错误 |

#### GlobalExceptionHandler
统一处理所有异常，返回统一格式的错误响应

```java
{
    "code": 400,
    "message": "参数验证失败",
    "data": null,
    "timestamp": "2026-02-02T10:00:00"
}
```

### 3. 限流 (infrastructure/limiter)

使用`@RateLimit`注解实现声明式限流

```java
@GetMapping("/{id}")
@RateLimit(
    key = "user:{userId}",
    rate = 10,
    interval = 60,
    message = "请求过于频繁"
)
public Result<User> getUser(@PathVariable Long id) {
    return Result.success(userService.getById(id));
}
```

支持的键变量：
- `{userId}`: 用户ID
- `{username}`: 用户名
- `{ipAddress}`: IP地址
- `{requestId}`: 请求ID
- `{method}`: 方法名
- `{arg0}`, `{arg1}`: 方法参数

### 4. 缓存 (data/cache)

使用`CacheKeyManager`统一管理缓存键

```java
@Service
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User> {

    @Autowired
    private RedisService redisService;

    @Override
    public User getById(Object id) {
        String cacheKey = CacheKeyManager.getUserKey((Long) id);

        // 尝试从缓存获取
        String cached = redisService.stringGetString(cacheKey);
        if (cached != null) {
            return JSON.parseObject(cached, User.class);
        }

        // 从数据库获取
        User user = super.getById(id);

        // 存入缓存
        if (user != null) {
            redisService.stringSetString(
                cacheKey,
                JSON.toJSONString(user),
                CacheKeyManager.CacheTTL.ONE_HOUR
            );
        }

        return user;
    }
}
```

### 5. 消息队列 (data/message)

#### 发送消息

```java
@Component
public class OrderService {

    @Autowired
    private KafkaProducerTemplate kafkaProducer;

    public void createOrder(Order order) {
        // 创建订单
        save(order);

        // 发送订单创建事件
        MessageEvent event = MessageEvent.builder()
            .eventType("ORDER_CREATED")
            .source("OrderService")
            .data(JSON.toJSONString(order))
            .userId(order.getUserId())
            .timestamp(LocalDateTime.now())
            .build();

        kafkaProducer.sendEvent("order-events", event);
    }
}
```

#### 消费消息

```java
@Component
public class OrderEventConsumer extends KafkaConsumerTemplate {

    @KafkaListener(topics = "order-events", groupId = "order-service")
    public void consumeOrderEvent(String message) {
        if (!validateMessage(message)) {
            return;
        }

        MessageEvent event = parseEvent(message);
        if (!validateEvent(event)) {
            return;
        }

        try {
            if ("ORDER_CREATED".equals(event.getEventType())) {
                handleOrderCreated(event);
            }
            event.markAsProcessed("success");
        } catch (Exception e) {
            if (event.shouldRetry()) {
                event.incrementRetryCount();
                // 重新发送到队列
            } else {
                event.markAsFailed(e.getMessage());
                // 记录失败日志
            }
        }
    }

    private void handleOrderCreated(MessageEvent event) {
        Order order = JSON.parseObject(event.getData(), Order.class);
        // 处理订单创建逻辑
    }
}
```

### 6. 异步任务 (infrastructure/config/AsyncTaskConfig)

```java
@Component
public class EmailService {

    @Async("asyncExecutor")
    public void sendEmailAsync(String to, String subject, String content) {
        // 异步发送邮件，不阻塞主线程
        // ...
    }
}

// 使用
@Service
public class UserServiceImpl {

    @Autowired
    private EmailService emailService;

    public void registerUser(User user) {
        save(user);
        // 异步发送注册确认邮件
        emailService.sendEmailAsync(user.getEmail(), "欢迎注册", "...");
    }
}
```

### 7. 日志和性能监控

自动通过AOP记录所有Controller/Service/Mapper的调用：

```
[Controller] [RequestID: abc123] UsersController.getUser() 开始执行, 参数: [1]
[Service] [RequestID: abc123] UserService.getById() 执行完成, 耗时: 45ms, 返回值: User(...)
[Controller] [RequestID: abc123] UsersController.getUser() 执行完成, 耗时: 50ms, 返回值: Result(...)
```

## 使用示例

### 创建完整的订单管理模块

#### 1. 创建实体 (Order.java)

```java
@Data
@Builder
@TableName("orders")
public class Order extends BaseEntity {

    @TableField("user_id")
    private Long userId;

    @TableField("product_id")
    private Long productId;

    @TableField("quantity")
    private Integer quantity;

    @TableField("total_amount")
    private BigDecimal totalAmount;

    @TableField("status")
    private String status; // PENDING, PAID, SHIPPED, COMPLETED

    @TableField("remark")
    private String remark;
}
```

#### 2. 创建Mapper

```java
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    List<Order> selectByUserId(Long userId);
}
```

#### 3. 创建Service

```java
public interface OrderService extends BaseService<Order> {
    List<Order> getUserOrders(Long userId);
    void createOrder(Order order, Long userId);
    void updateOrderStatus(Long orderId, String status);
}

@Service
@Slf4j
public class OrderServiceImpl extends BaseServiceImpl<OrderMapper, Order>
    implements OrderService {

    @Autowired
    private KafkaProducerTemplate kafkaProducer;

    @Autowired
    private RedisService redisService;

    @Override
    @Transactional
    public void createOrder(Order order, Long userId) {
        try {
            order.setUserId(userId);
            order.setStatus("PENDING");

            // 保存订单
            this.save(order);

            // 发送事件
            MessageEvent event = MessageEvent.builder()
                .eventType("ORDER_CREATED")
                .source("OrderService")
                .data(JSON.toJSONString(order))
                .userId(userId)
                .timestamp(LocalDateTime.now())
                .build();
            kafkaProducer.sendEvent("order-events", event);

            log.info("订单创建成功: orderId={}, userId={}", order.getId(), userId);
        } catch (Exception e) {
            log.error("订单创建失败", e);
            throw new BusinessException("创建订单失败");
        }
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return lambdaQuery()
            .eq(Order::getUserId, userId)
            .orderByDesc(Order::getCreatedAt)
            .list();
    }
}
```

#### 4. 创建Controller

```java
@RestController
@RequestMapping("/api/orders")
@Slf4j
public class OrderController extends BaseController<Order, OrderService> {

    @PostMapping
    @RateLimit(key = "user:{userId}", rate = 5, interval = 60)
    public Result<Order> createOrder(@RequestBody Order order) {
        try {
            Long userId = ThreadLocalUtils.getUserId();
            if (userId == null) {
                return Result.unauthorized("用户未认证");
            }

            baseService.createOrder(order, userId);
            return Result.success(order);
        } catch (BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("创建订单失败", e);
            return Result.error("创建订单失败");
        }
    }

    @GetMapping("/user")
    @RateLimit(key = "user:{userId}", rate = 20, interval = 60)
    public Result<List<Order>> getUserOrders() {
        try {
            Long userId = ThreadLocalUtils.getUserId();
            if (userId == null) {
                return Result.unauthorized("用户未认证");
            }

            List<Order> orders = baseService.getUserOrders(userId);
            return Result.success(orders);
        } catch (Exception e) {
            log.error("获取订单列表失败", e);
            return Result.error("获取订单列表失败");
        }
    }
}
```

## 扩展开发

### 添加自定义注解

```java
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomAnnotation {
    String value();
}

@Aspect
@Component
public class CustomAspect {

    @Around("@annotation(com.example.yoyo_data.infrastructure.aspect.CustomAnnotation)")
    public Object handle(ProceedingJoinPoint pjp) throws Throwable {
        // 切面逻辑
        return pjp.proceed();
    }
}
```

### 集成第三方服务

```java
@Configuration
public class ThirdPartyConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean
    public SomeThirdPartyService thirdPartyService() {
        return new SomeThirdPartyService();
    }
}
```

## 最佳实践

### 1. 异常处理
```java
// ❌ 不好的做法
public void process() {
    try {
        // ...
    } catch (Exception e) {
        e.printStackTrace();
    }
}

// ✅ 好的做法
public void process() {
    try {
        // ...
    } catch (BusinessException e) {
        throw e;
    } catch (Exception e) {
        log.error("处理失败", e);
        throw new SystemException("系统处理失败", e);
    }
}
```

### 2. 缓存使用
```java
// ❌ 不好的做法
String key = "user:" + userId; // 硬编码缓存键

// ✅ 好的做法
String key = CacheKeyManager.getUserKey(userId); // 使用统一管理
```

### 3. 日志记录
```java
// ❌ 不好的做法
System.out.println("用户ID: " + userId);

// ✅ 好的做法
log.info("处理用户请求: userId={}", userId);
```

### 4. 参数验证
```java
// ❌ 不好的做法
public void updateUser(User user) {
    if (user == null || user.getId() == null) {
        // ...
    }
}

// ✅ 好的做法
public void updateUser(@Valid User user) {
    // Spring自动验证，BaseController已处理
}
```

## 常见问题

### Q: 如何自定义返回格式？
A: 修改 `Result` 类或在 `GlobalExceptionHandler` 中自定义

### Q: 如何添加请求认证？
A: 在 `RequestContextInterceptor` 中处理，使用 `ThreadLocalUtils` 存储用户信息

### Q: 如何处理跨域请求？
A: 创建 `WebConfig` 并配置 `CorsRegistry`

### Q: 如何优化数据库查询性能？
A: 利用 `CacheKeyManager` 和 `RedisService` 实现缓存策略

### Q: 如何测试异步任务？
A: 使用 `@Test` 和 `@AsyncTest` 注解，参考 `BaseServiceTest`

---

**更新时间**: 2026-02-02
**版本**: 1.0.0
**作者**: Template Framework Team
