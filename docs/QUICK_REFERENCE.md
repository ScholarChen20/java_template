# 快速参考指南

## 快速查找

### 基类位置
- **BaseEntity** → `infrastructure/base/BaseEntity.java`
- **BaseService** → `infrastructure/base/BaseService.java`
- **BaseServiceImpl** → `infrastructure/base/BaseServiceImpl.java`
- **BaseController** → `infrastructure/base/BaseController.java`
- **BasePage** → `infrastructure/base/BasePage.java`

### 异常类位置
- **BusinessException** → `support/exception/BusinessException.java`
- **ValidationException** → `support/exception/ValidationException.java`
- **SystemException** → `support/exception/SystemException.java`
- **GlobalExceptionHandler** → `api/handler/GlobalExceptionHandler.java`

### 工具类位置
- **ThreadLocalUtils** → `infrastructure/utils/ThreadLocalUtils.java`
- **PageUtils** → `infrastructure/utils/PageUtils.java`
- **DateTimeUtils** → `infrastructure/utils/DateTimeUtils.java`
- **ResultBuilder** → `infrastructure/utils/ResultBuilder.java`
- **CacheKeyManager** → `data/cache/CacheKeyManager.java`

### 配置类位置
- **JacksonConfig** → `infrastructure/config/JacksonConfig.java`
- **MybatisPlusConfig** → `infrastructure/config/MybatisPlusConfig.java`
- **AsyncTaskConfig** → `infrastructure/config/AsyncTaskConfig.java`

### 限流相关
- **@RateLimit注解** → `infrastructure/limiter/RateLimit.java`
- **RateLimitAspect** → `infrastructure/limiter/RateLimitAspect.java`

### 消息队列
- **MessageEvent** → `data/message/MessageEvent.java`
- **KafkaProducerTemplate** → `data/message/KafkaProducerTemplate.java`
- **KafkaConsumerTemplate** → `data/message/KafkaConsumerTemplate.java`

---

## 常用代码片段

### 1. 创建新的Service和实现

```java
// 1. 创建Service接口
public interface ProductService extends BaseService<Product> {
    List<Product> findByCategory(String category);
}

// 2. 创建Service实现
@Service
@Slf4j
public class ProductServiceImpl extends BaseServiceImpl<ProductMapper, Product>
    implements ProductService {

    @Override
    public List<Product> findByCategory(String category) {
        return lambdaQuery()
            .eq(Product::getCategory, category)
            .list();
    }
}
```

### 2. 创建新的Controller

```java
@Slf4j
@RestController
@RequestMapping("/api/products")
public class ProductController extends BaseController<Product, ProductService> {

    @GetMapping("/category/{category}")
    public Result<List<Product>> getByCategory(@PathVariable String category) {
        try {
            List<Product> products = baseService.findByCategory(category);
            return Result.success(products);
        } catch (Exception e) {
            log.error("按分类查询产品失败: category={}", category, e);
            return Result.error("按分类查询失败");
        }
    }
}
```

### 3. 使用限流保护

```java
@PostMapping
@RateLimit(key = "user:{userId}", rate = 5, interval = 60)
public Result<Order> createOrder(@RequestBody Order order) {
    // ...
}
```

### 4. 缓存使用示例

```java
@Override
public Product getById(Object id) {
    String cacheKey = CacheKeyManager.getProductKey((Long) id);

    // 尝试从缓存获取
    String cached = redisService.stringGetString(cacheKey);
    if (cached != null) {
        return JSON.parseObject(cached, Product.class);
    }

    // 从数据库获取
    Product product = super.getById(id);

    // 存入缓存
    if (product != null) {
        redisService.stringSetString(
            cacheKey,
            JSON.toJSONString(product),
            CacheKeyManager.CacheTTL.ONE_HOUR
        );
    }

    return product;
}
```

### 5. 发送消息事件

```java
@Autowired
private KafkaProducerTemplate kafkaProducer;

public void createProduct(Product product) {
    save(product);

    MessageEvent event = MessageEvent.builder()
        .eventType("PRODUCT_CREATED")
        .source("ProductService")
        .data(JSON.toJSONString(product))
        .timestamp(LocalDateTime.now())
        .build();

    kafkaProducer.sendEvent("product-events", event);
}
```

### 6. 消费消息事件

```java
@Component
@Slf4j
public class ProductEventConsumer extends KafkaConsumerTemplate {

    @KafkaListener(topics = "product-events", groupId = "product-service")
    public void consume(String message) {
        MessageEvent event = parseEvent(message);
        if (event != null && "PRODUCT_CREATED".equals(event.getEventType())) {
            handleProductCreated(event);
        }
    }

    private void handleProductCreated(MessageEvent event) {
        Product product = JSON.parseObject(event.getData(), Product.class);
        log.info("产品创建事件处理: productId={}", product.getId());
    }
}
```

### 7. 异步任务处理

```java
@Service
public class EmailService {

    @Async("asyncExecutor")
    public void sendEmailAsync(String to, String subject, String content) {
        // 异步发送邮件
        log.info("异步发送邮件: to={}, subject={}", to, subject);
        // ...
    }
}
```

### 8. 分页查询

```java
@GetMapping
public Result<BasePage<Product>> list(
    @RequestParam(defaultValue = "1") int pageNum,
    @RequestParam(defaultValue = "10") int pageSize
) {
    Page<Product> page = PageUtils.createPage(pageNum, pageSize);
    IPage<Product> result = baseService.page(page);
    return Result.success(PageUtils.toBasePage(result));
}
```

### 9. ThreadLocal上下文使用

```java
// 在拦截器中设置用户信息
ThreadLocalUtils.setUserId(user.getId());
ThreadLocalUtils.setUsername(user.getUsername());
ThreadLocalUtils.setIpAddress(ipAddress);
ThreadLocalUtils.setRequestId(requestId);

// 在Service中获取用户信息
Long userId = ThreadLocalUtils.getUserId();
String username = ThreadLocalUtils.getUsername();

// 请求处理完成后清理
ThreadLocalUtils.clear();
```

### 10. 日期时间操作

```java
// 获取当前日期时间
LocalDateTime now = DateTimeUtils.getCurrentDateTime();

// 格式化
String formatted = DateTimeUtils.formatDateTime(now);

// 解析
LocalDateTime parsed = DateTimeUtils.parseDateTime("2026-02-02 10:00:00");

// 计算
long daysBetween = DateTimeUtils.daysBetween(startDate, endDate);

// 检查
boolean isToday = DateTimeUtils.isToday(date);
```

---

## 常见错误和解决方案

### 错误1：BaseController泛型异常

```java
// ❌ 错误
public class UserController extends BaseController<User, UserService> {
    // 没有注入baseService
}

// ✅ 正确
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User, UserService> {
    @Autowired
    private UserService baseService;  // 必须注入
}
```

### 错误2：缓存键命名不统一

```java
// ❌ 错误
String key = "user:" + id;
String key2 = "user_" + id;

// ✅ 正确
String key = CacheKeyManager.getUserKey(id);
```

### 错误3：异常处理不当

```java
// ❌ 错误
try {
    service.doSomething();
} catch (Exception e) {
    e.printStackTrace();
}

// ✅ 正确
try {
    service.doSomething();
} catch (BusinessException e) {
    throw e;
} catch (Exception e) {
    log.error("处理失败", e);
    throw new SystemException("处理失败", e);
}
```

### 错误4：限流键指定错误

```java
// ❌ 错误：不支持的变量
@RateLimit(key = "user:{username}")  // ThreadLocal中没有username

// ✅ 正确
@RateLimit(key = "user:{userId}")  // ThreadLocal中有userId
```

---

## 配置快速参考

### 开发环境启动

```bash
# 使用Maven运行
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# 或打包后运行
mvn clean package
java -jar target/yoyo_data.jar --spring.profiles.active=dev
```

### 生产环境启动

```bash
# 使用环境变量
export DB_URL=jdbc:mysql://prod-db:3306/yoyo_data
export DB_USERNAME=root
export DB_PASSWORD=password123
export JWT_SECRET=my-secret-key

java -jar app.jar --spring.profiles.active=prod
```

### Docker启动

```bash
docker run \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e DB_URL=jdbc:mysql://mysql:3306/yoyo_data \
  -e DB_USERNAME=root \
  -e DB_PASSWORD=password123 \
  -e JWT_SECRET=my-secret-key \
  -p 8080:8080 \
  yoyo_data:latest
```

---

## 性能相关配置建议

| 场景 | 建议 |
|------|------|
| 高并发查询 | 增加Redis连接池，使用CacheKeyManager缓存 |
| 高并发写入 | 增加数据库连接池，使用@Async异步处理 |
| 实时性要求高 | 使用Kafka消息队列+消费者处理 |
| 慢查询多 | 添加数据库索引，优化查询条件 |
| 内存占用高 | 检查缓存策略，设置合理TTL |

---

## 测试快速参考

### Controller测试

```java
public class UserControllerTest extends BaseControllerTest {

    @Test
    public void testGetUser() throws Exception {
        mockMvc.perform(
            get("/api/users/1")
                .accept(MediaType.APPLICATION_JSON)
        )
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value(200));
    }
}
```

### Service测试

```java
public class UserServiceTest extends BaseServiceTest {

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setUsername("test");

        boolean success = userService.save(user);
        assertTrue(success, "用户创建失败");
    }
}
```

---

## 文档导航

| 需求 | 参考文档 |
|------|---------|
| 快速开始 | TEMPLATE_GUIDE.md → 快速开始 |
| API设计 | API_GUIDE.md → 接口规范 |
| 编码规范 | DEVELOPMENT_GUIDE.md → 编码规范 |
| 配置管理 | CONFIGURATION_GUIDE.md |
| 常见问题 | TEMPLATE_GUIDE.md → 常见问题 |

---

**最后更新**: 2026-02-02
**版本**: 1.0.0
