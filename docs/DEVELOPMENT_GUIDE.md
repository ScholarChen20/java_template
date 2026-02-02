# 开发规范指南

## 目录
1. [编码规范](#编码规范)
2. [项目结构](#项目结构)
3. [依赖管理](#依赖管理)
4. [Git工作流](#git工作流)
5. [代码审查](#代码审查)
6. [性能优化](#性能优化)

## 编码规范

### 1.1 Java编码规范

#### 类和接口

```java
// ✅ 好的做法
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("users")
public class User extends BaseEntity {
    // 字段
    private String username;
    // 方法
}

// ❌ 不好的做法
public class User {
    public String username;
    public String getUsername() { }
    public void setUsername(String username) { }
}
```

#### 字段声明

```java
// ✅ 好的做法
@Data  // Lombok自动生成getter/setter/equals/hashCode/toString
public class User extends BaseEntity {
    @TableField("username")
    private String username;

    @TableField("email")
    private String email;
}

// ❌ 不好的做法
public class User {
    private String username;
    private String email;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    // ... 大量重复的getter/setter代码
}
```

#### 方法编写

```java
// ✅ 好的做法
@Override
public User getById(Object id) {
    if (id == null) {
        return null;
    }

    String cacheKey = CacheKeyManager.getUserKey((Long) id);
    String cached = redisService.stringGetString(cacheKey);

    if (cached != null) {
        return JSON.parseObject(cached, User.class);
    }

    User user = super.getById(id);
    if (user != null) {
        redisService.stringSetString(
            cacheKey,
            JSON.toJSONString(user),
            3600000
        );
    }

    return user;
}

// ❌ 不好的做法
public User getById(Long id) {
    if (id != null && id > 0) {
        User u = userMapper.selectById(id);
        if (u != null) {
            redisService.set("u_" + id, u);
            return u;
        }
    }
    return null;
}
```

### 1.2 异常处理

```java
// ✅ 好的做法
try {
    user = userService.getById(id);
} catch (BusinessException e) {
    log.warn("业务异常: {}", e.getMessage());
    throw e;
} catch (Exception e) {
    log.error("系统异常", e);
    throw new SystemException("获取用户失败", e);
}

// ❌ 不好的做法
try {
    user = userService.getById(id);
} catch (Exception e) {
    e.printStackTrace();
    return null;
}
```

### 1.3 集合操作

```java
// ✅ 好的做法
List<User> users = userService.list();
if (users != null && !users.isEmpty()) {
    users.forEach(user -> log.info("User: {}", user.getId()));
}

// ❌ 不好的做法
List<User> users = userService.list();
if (users.size() > 0) {
    for (int i = 0; i < users.size(); i++) {
        User user = users.get(i);
    }
}
```

### 1.4 字符串操作

```java
// ✅ 好的做法
String message = String.format("用户%s登录成功", username);
log.info("User login: username={}, timestamp={}", username, LocalDateTime.now());

// ❌ 不好的做法
String message = "用户" + username + "登录成功";
System.out.println("User login: " + username + " " + new Date());
```

### 1.5 null检查

```java
// ✅ 好的做法
if (user != null && user.getId() != null) {
    // 处理用户
}

// 使用Objects工具类
Objects.requireNonNull(user, "用户不能为空");

// 使用Optional
Optional.ofNullable(user)
    .ifPresent(u -> processUser(u));

// ❌ 不好的做法
if (user != null) {
    if (user.getId() != null) {
        // 深层嵌套
    }
}
```

## 项目结构

### 2.1 标准目录结构

```
yoyo_data/
├── src/
│   ├── main/
│   │   ├── java/com/example/yoyo_data/
│   │   │   ├── api/                          # API层
│   │   │   │   ├── controller/               # 控制器
│   │   │   │   ├── handler/                  # 异常处理
│   │   │   │   └── interceptor/              # 拦截器
│   │   │   ├── business/                     # 业务层
│   │   │   │   ├── service/                  # 服务接口
│   │   │   │   └── service/impl/             # 服务实现
│   │   │   ├── data/                         # 数据层
│   │   │   │   ├── mapper/                   # 数据映射
│   │   │   │   ├── cache/                    # 缓存
│   │   │   │   └── message/                  # 消息队列
│   │   │   ├── domain/                       # 领域模型
│   │   │   │   ├── entity/                   # 实体类
│   │   │   │   ├── dto/                      # 数据传输对象
│   │   │   │   └── vo/                       # 视图对象
│   │   │   ├── infrastructure/               # 基础设施
│   │   │   │   ├── base/                     # 基类
│   │   │   │   ├── config/                   # 配置
│   │   │   │   ├── aspect/                   # 切面
│   │   │   │   ├── utils/                    # 工具类
│   │   │   │   └── limiter/                  # 限流
│   │   │   └── support/                      # 支撑层
│   │   │       ├── exception/                # 异常
│   │   │       ├── constant/                 # 常量
│   │   │       └── response/                 # 响应
│   │   └── resources/
│   │       ├── application.yml               # 主配置
│   │       ├── application-dev.yml           # 开发配置
│   │       ├── application-test.yml          # 测试配置
│   │       ├── application-prod.yml          # 生产配置
│   │       └── mapper/                       # MyBatis XML文件
│   └── test/
│       └── java/com/example/yoyo_data/
│           └── infrastructure/base/          # 测试基类
├── docs/                                     # 文档
├── scripts/                                  # 脚本
├── pom.xml                                   # Maven配置
└── README.md                                 # 项目说明
```

### 2.2 单个模块的标准结构

```
user-module/
├── UserEntity.java              # 实体
├── UserDTO.java                 # 数据传输对象
├── UserVO.java                  # 视图对象
├── UserMapper.java              # 数据映射
├── UserService.java             # 服务接口
├── UserServiceImpl.java          # 服务实现
├── UserController.java          # 控制器
└── UserControllerTest.java      # 测试
```

## 依赖管理

### 3.1 Maven依赖原则

```xml
<!-- ✅ 好的做法：明确指定版本，避免冲突 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <version>2.3.12.RELEASE</version>
</dependency>

<!-- ❌ 不好的做法：不指定版本 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```

### 3.2 依赖范围

| 范围 | 用途 |
|-----|------|
| compile | 默认，编译和运行时都需要 |
| provided | 编译和测试时需要，运行时由容器提供 |
| runtime | 编译时不需要，运行时需要 |
| test | 仅在测试时需要 |
| system | 依赖于本地系统 |

```xml
<!-- 测试依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## Git工作流

### 4.1 分支命名规范

```
main              # 主分支（生产）
├── develop       # 开发分支
│   ├── feature/user-auth          # 功能分支
│   ├── bugfix/login-issue         # 缺陷修复
│   ├── refactor/redis-cache       # 重构
│   └── hotfix/security-patch      # 紧急修复
```

### 4.2 提交信息规范

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### 类型 (type)

| 类型 | 说明 |
|-----|------|
| feat | 新功能 |
| fix | 缺陷修复 |
| refactor | 代码重构 |
| style | 代码格式调整 |
| test | 测试代码 |
| docs | 文档更新 |
| chore | 构建、依赖等变更 |

#### 示例

```
feat(user): 添加用户注册功能

- 创建用户注册API
- 实现邮箱验证
- 集成Redis缓存验证码

Closes #123
```

### 4.3 合并请求规范

```
Title: [FEATURE] 实现订单管理模块

Description:
- 实现订单创建、查询、更新、删除API
- 集成Kafka事件发送
- 添加单元测试（覆盖率>80%）

Checklist:
- [x] 代码通过本地测试
- [x] 遵循编码规范
- [x] 已更新文档
- [x] 已添加/更新单元测试
```

## 代码审查

### 5.1 审查重点

- [ ] 代码是否遵循规范
- [ ] 是否有性能问题
- [ ] 是否有安全漏洞
- [ ] 是否有异常处理
- [ ] 是否有日志记录
- [ ] 是否有单元测试
- [ ] 是否有文档更新

### 5.2 常见问题

```java
// ❌ 问题1：缺少异常处理
public List<User> getUsers() {
    return userService.list();
}

// ✅ 改进
public Result<List<User>> getUsers() {
    try {
        return Result.success(userService.list());
    } catch (Exception e) {
        log.error("获取用户列表失败", e);
        return Result.error("获取用户列表失败");
    }
}

// ❌ 问题2：缺少日志
public void updateUser(User user) {
    userService.updateById(user);
}

// ✅ 改进
public void updateUser(User user) {
    log.info("更新用户: userId={}", user.getId());
    userService.updateById(user);
    log.info("用户更新成功: userId={}", user.getId());
}

// ❌ 问题3：缺少验证
public Result<User> getById(Long id) {
    return Result.success(userService.getById(id));
}

// ✅ 改进
public Result<User> getById(Long id) {
    if (id == null || id <= 0) {
        return Result.badRequest("ID不能为空或小于等于0");
    }
    User user = userService.getById(id);
    if (user == null) {
        return Result.notFound("用户不存在");
    }
    return Result.success(user);
}
```

## 性能优化

### 6.1 数据库优化

```java
// ❌ 不好的做法：N+1查询
List<Order> orders = orderService.list();
for (Order order : orders) {
    User user = userService.getById(order.getUserId());
    // 处理...
}

// ✅ 好的做法：使用join一次查询
List<OrderWithUser> result = orderService.listWithUser();
```

### 6.2 缓存优化

```java
// ✅ 好的做法：合理使用缓存
@Override
public User getById(Object id) {
    String cacheKey = CacheKeyManager.getUserKey((Long) id);

    // 先从缓存获取
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
```

### 6.3 API性能优化

```java
// ✅ 好的做法：分页查询
@GetMapping
public Result<BasePage<User>> list(
    @RequestParam(defaultValue = "1") int pageNum,
    @RequestParam(defaultValue = "10") int pageSize
) {
    Page<User> page = PageUtils.createPage(pageNum, pageSize);
    IPage<User> result = baseService.page(page);
    return Result.success(PageUtils.toBasePage(result));
}

// ❌ 不好的做法：一次查询所有数据
@GetMapping
public Result<List<User>> list() {
    return Result.success(baseService.list());
}
```

---

**版本**: 1.0.0
**最后更新**: 2026-02-02
