# API 开发规范指南

## 概述

本指南定义了yoyo_data项目中API开发的规范和标准，确保所有API的一致性、可维护性和可扩展性。

## 1. 命名规范

### 1.1 包名规范

遵循反向域名命名规则，结合项目层级结构：

```
com.example.yoyo_data.
├── api.controller          - API控制器
├── api.handler             - 异常处理器
├── api.interceptor         - 拦截器
├── business.service        - 业务服务
├── business.service.impl   - 服务实现
├── data.mapper             - 数据映射层
├── data.cache              - 缓存层
├── data.message            - 消息队列
├── domain.entity           - 实体类
├── domain.dto              - 数据传输对象
├── domain.vo               - 视图对象
├── infrastructure.base     - 基类
├── infrastructure.config   - 配置类
├── infrastructure.aspect   - 切面
├── infrastructure.utils    - 工具类
├── support.exception       - 异常定义
└── support.constant        - 常量定义
```

### 1.2 类名规范

| 类型 | 后缀 | 示例 |
|------|------|------|
| 实体类 | Entity | UserEntity, ProductEntity |
| 数据传输对象 | DTO | UserDTO, CreateUserRequest |
| 视图对象 | VO | UserVO, UserResponse |
| 控制器 | Controller | UserController |
| 服务接口 | Service | UserService |
| 服务实现 | ServiceImpl | UserServiceImpl |
| 数据映射 | Mapper | UserMapper |
| 工具类 | Utils | StringUtils, DateUtils |
| 配置类 | Config | RedisConfig |
| 异常类 | Exception | BusinessException |
| 切面类 | Aspect | LoggingAspect |

### 1.3 方法名规范

遵循驼峰式命名，名词在前，动词在后：

```java
// ✅ 好的做法
getUserById(Long id)
getUsersByStatus(String status)
createUser(UserDTO dto)
updateUserInfo(Long id, UserDTO dto)
deleteUserById(Long id)
queryUsersByCondition(UserQuery query)

// ❌ 不好的做法
getUser()           // 太通用
get_user_by_id()    // 下划线命名
getUserByIdAndStatus() // 参数应该单独传递
```

### 1.4 常量命名规范

```java
// ✅ 好的做法
public static final String DEFAULT_TIMEZONE = "UTC+8";
public static final int MAX_RETRY_COUNT = 3;
public static final long CACHE_TIMEOUT_MS = 3600000;

// ❌ 不好的做法
public static final String defaultTimezone = "UTC+8";
public static final String MAX_RETRY = "3";
```

## 2. 接口规范

### 2.1 RESTful API设计

遵循REST原则，使用标准HTTP方法：

| HTTP方法 | 操作 | 路径示例 |
|---------|------|---------|
| GET | 读取 | `/api/users/{id}` |
| POST | 创建 | `/api/users` |
| PUT | 全量更新 | `/api/users/{id}` |
| PATCH | 部分更新 | `/api/users/{id}` |
| DELETE | 删除 | `/api/users/{id}` |

### 2.2 URL路径设计

```java
// ✅ 好的做法
GET    /api/users              // 列表
POST   /api/users              // 创建
GET    /api/users/{id}         // 详情
PUT    /api/users/{id}         // 更新
DELETE /api/users/{id}         // 删除
GET    /api/users/search       // 搜索

// ❌ 不好的做法
GET    /api/user/list
POST   /api/user/add
GET    /api/user/get
PUT    /api/user/update
DELETE /api/user/delete
GET    /api/user/findByName
```

### 2.3 请求参数规范

```java
// 路径参数 - 获取单个资源
@GetMapping("/{id}")
public Result<User> getById(@PathVariable Long id) { }

// 查询参数 - 列表、搜索、过滤
@GetMapping
public Result<List<User>> list(
    @RequestParam(defaultValue = "1") int pageNum,
    @RequestParam(defaultValue = "10") int pageSize,
    @RequestParam(required = false) String status
) { }

// 请求体 - 创建、更新
@PostMapping
public Result<User> create(@RequestBody @Valid UserDTO dto) { }

@PutMapping("/{id}")
public Result<User> update(
    @PathVariable Long id,
    @RequestBody @Valid UserUpdateDTO dto
) { }
```

### 2.4 响应格式规范

所有API都必须返回统一的Result格式：

```json
{
    "code": 200,
    "message": "success",
    "data": {
        "id": 1,
        "name": "John Doe",
        "email": "john@example.com"
    },
    "timestamp": "2026-02-02T10:30:00"
}
```

#### 常见状态码

| 状态码 | 含义 | 场景 |
|--------|------|------|
| 200 | 成功 | 正常响应 |
| 400 | 请求错误 | 参数验证失败 |
| 401 | 未认证 | 需要登录 |
| 403 | 禁止访问 | 权限不足 |
| 404 | 资源不存在 | 记录未找到 |
| 429 | 限流 | 请求过于频繁 |
| 500 | 服务器错误 | 系统异常 |

## 3. 控制器规范

### 3.1 基本结构

```java
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController extends BaseController<User, UserService> {

    // 继承BaseController提供的基础CRUD操作
    // 只需添加业务相关的特殊接口

    /**
     * 搜索用户
     *
     * @param keyword 搜索关键词
     * @return 用户列表
     */
    @GetMapping("/search")
    @RateLimit(key = "user:{userId}", rate = 10, interval = 60)
    public Result<List<User>> search(@RequestParam String keyword) {
        try {
            List<User> users = baseService.searchByKeyword(keyword);
            return Result.success(users);
        } catch (BusinessException e) {
            return Result.badRequest(e.getMessage());
        } catch (Exception e) {
            log.error("搜索用户失败: keyword={}", keyword, e);
            return Result.error("搜索用户失败");
        }
    }

    /**
     * 获取用户统计信息
     *
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public Result<UserStatistics> getStatistics() {
        try {
            UserStatistics stats = baseService.getStatistics();
            return Result.success(stats);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return Result.error("获取统计信息失败");
        }
    }
}
```

### 3.2 异常处理

```java
// ✅ 好的做法：统一使用全局异常处理器
@GetMapping("/{id}")
public Result<User> getById(@PathVariable Long id) {
    User user = baseService.getById(id);
    if (user == null) {
        throw new BusinessException("用户不存在");
    }
    return Result.success(user);
}

// ❌ 不好的做法：在控制器中处理异常
@GetMapping("/{id}")
public Result<User> getById(@PathVariable Long id) {
    try {
        User user = baseService.getById(id);
        if (user == null) {
            return Result.notFound("用户不存在");
        }
        return Result.success(user);
    } catch (Exception e) {
        return Result.error(e.getMessage());
    }
}
```

## 4. 服务规范

### 4.1 业务逻辑组织

```java
@Service
@Slf4j
public class UserServiceImpl extends BaseServiceImpl<UserMapper, User>
    implements UserService {

    @Autowired
    private RedisService redisService;

    @Autowired
    private KafkaProducerTemplate kafkaProducer;

    /**
     * 创建用户并发送欢迎邮件
     *
     * @param user 用户对象
     * @return 创建的用户
     */
    @Override
    @Transactional
    public User createUser(User user) {
        try {
            // 业务验证
            validateUserData(user);

            // 保存到数据库
            this.save(user);

            // 发送事件
            publishUserCreatedEvent(user);

            log.info("用户创建成功: userId={}", user.getId());
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("创建用户失败", e);
            throw new SystemException("用户创建失败", e);
        }
    }

    /**
     * 验证用户数据
     */
    private void validateUserData(User user) {
        if (user == null || user.getUsername() == null) {
            throw new ValidationException("username", "用户名不能为空");
        }
        if (user.getEmail() == null) {
            throw new ValidationException("email", "邮箱不能为空");
        }
    }

    /**
     * 发布用户创建事件
     */
    private void publishUserCreatedEvent(User user) {
        MessageEvent event = MessageEvent.builder()
            .eventType("USER_CREATED")
            .source("UserService")
            .data(JSON.toJSONString(user))
            .timestamp(LocalDateTime.now())
            .build();
        kafkaProducer.sendEvent("user-events", event);
    }
}
```

### 4.2 事务管理

```java
// ✅ 好的做法：正确使用事务
@Service
public class UserServiceImpl {

    @Override
    @Transactional
    public void createUserWithProfile(User user, UserProfile profile) {
        // 两个操作在同一个事务中
        userMapper.insert(user);
        profileMapper.insert(profile);
        // 如果任何一个失败，都会回滚
    }

    // 不需要事务的方法
    public User getById(Long id) {
        return userMapper.selectById(id);
    }
}
```

## 5. 数据库规范

### 5.1 表设计

```sql
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(64) NOT NULL UNIQUE,
    email VARCHAR(256) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(32) DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_deleted TINYINT DEFAULT 0,
    INDEX idx_username (username),
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5.2 查询优化

```java
// ❌ 不好的做法：N+1查询
List<User> users = userService.list(); // 1次查询
for (User user : users) {
    List<Order> orders = orderService.listByUserId(user.getId()); // N次查询
}

// ✅ 好的做法：一次查询获取所有数据
List<UserWithOrders> result = userService.listWithOrders();
```

## 6. 日志规范

### 6.1 日志级别

| 级别 | 用途 |
|-----|------|
| DEBUG | 开发调试信息 |
| INFO | 重要业务事件 |
| WARN | 可能存在的问题 |
| ERROR | 错误事件 |

### 6.2 日志记录

```java
// ✅ 好的做法
log.info("用户登录成功: username={}, ipAddress={}", username, ipAddress);
log.warn("验证失败，尝试次数过多: username={}, attemptCount={}", username, attemptCount);
log.error("数据库连接失败: error={}", exception.getMessage(), exception);

// ❌ 不好的做法
log.info("用户: " + user + "登录");
log.error("error");
System.out.println("debug");
```

## 7. 版本控制规范

### 7.1 API版本

使用URL版本控制：

```java
@RequestMapping("/api/v1/users")
public class UserControllerV1 { }

@RequestMapping("/api/v2/users")
public class UserControllerV2 { }
```

## 8. 安全规范

### 8.1 输入验证

```java
// 使用Spring Validation进行参数验证
@Data
public class CreateUserRequest {
    @NotBlank(message = "用户名不能为空")
    @Length(min = 3, max = 32, message = "用户名长度在3-32之间")
    private String username;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @NotBlank(message = "密码不能为空")
    @Length(min = 6, max = 32, message = "密码长度在6-32之间")
    private String password;
}
```

### 8.2 敏感信息保护

```java
// ❌ 不好的做法：记录敏感信息
log.info("用户密码: {}", user.getPassword());

// ✅ 好的做法：不记录或脱敏
log.info("用户认证成功: userId={}", user.getId());
```

## 9. 文档规范

### 9.1 Javadoc注释

```java
/**
 * 创建用户
 *
 * @param user 用户信息
 * @return 创建后的用户对象
 * @throws BusinessException 用户名已存在时抛出
 * @since 1.0.0
 */
@PostMapping
public Result<User> createUser(@RequestBody User user) { }
```

### 9.2 方法注释

```java
@Service
public class UserService {

    /**
     * 根据用户ID获取用户信息
     *
     * 该方法首先尝试从缓存获取用户信息，
     * 如果缓存未命中，则从数据库查询并缓存结果。
     *
     * @param userId 用户ID
     * @return 用户信息，如果不存在返回null
     */
    public User getById(Long userId) {
        // ...
    }
}
```

---

**版本**: 1.0.0
**最后更新**: 2026-02-02
**作者**: Framework Team
