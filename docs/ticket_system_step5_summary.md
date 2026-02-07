# 演唱会抢票系统 - 第五步完成

## 📦 第五步：Controller 层创建完成

### 1. Controller 列表（2个控制器）

所有 Controller 位于 `com.example.yoyo_data.controller` 包：

| Controller | 文件 | 接口数量 | 说明 |
|-----------|------|---------|------|
| ShowEventController | ShowEventController.java | 4个 | 演出活动查询接口（公开） |
| TicketController | TicketController.java | 5个 | 抢票核心接口（需要认证） |

---

## 🎯 接口清单

### 1. ShowEventController（演出活动控制器）

**基础路径**: `/api/ticket/shows`

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|------|
| `/api/ticket/shows` | GET | 查询演出活动列表（分页、筛选） | ❌ 否 |
| `/api/ticket/shows/{id}` | GET | 查询演出活动详情（Redis缓存） | ❌ 否 |
| `/api/ticket/shows/{showEventId}/seats` | GET | 查询座位列表（分页、筛选） | ❌ 否 |
| `/api/ticket/shows/{showEventId}/seats/available-count` | GET | 查询可售座位数量 | ❌ 否 |

**特性**：
- ✅ 所有接口都是公开的，无需认证
- ✅ 演出详情接口使用 Redis 缓存（1小时）
- ✅ 支持分页查询
- ✅ 支持多维度筛选（状态、类型、区域）
- ✅ Swagger 文档注解完整

---

### 2. TicketController（抢票控制器）

**基础路径**: `/api/ticket`

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|------|
| `/api/ticket/grab` | POST | **抢票（核心接口）** | ✅ 是 |
| `/api/ticket/pay` | POST | 支付订单 | ✅ 是 |
| `/api/ticket/cancel/{orderId}` | POST | 取消订单 | ✅ 是 |
| `/api/ticket/order/{orderId}` | GET | 查询订单详情 | ✅ 是 |
| `/api/ticket/orders` | GET | 查询我的订单列表 | ✅ 是 |

**特性**：
- ✅ 所有接口都需要 JWT 认证
- ✅ 使用 `@Valid` 注解进行参数校验
- ✅ 抢票接口建议添加限流（100 req/s）
- ✅ 统一使用 `Result` 封装响应
- ✅ Swagger 文档注解完整

---

## 🔐 认证机制

### JWT Token 传递

所有需要认证的接口必须在请求头中携带 JWT token：

```http
Authorization: Bearer {your_jwt_token}
```

### Token 提取逻辑

Controller 中统一使用 `extractToken` 方法提取 token：

```java
private String extractToken(HttpServletRequest request) {
    String token = request.getHeader("Authorization");
    if (token != null && token.startsWith("Bearer ")) {
        token = token.substring(7); // 去掉 "Bearer " 前缀
    }
    return token;
}
```

### Token 验证

Token 的验证在 Service 层进行：

```java
if (token == null || !jwtUtils.validateToken(token)) {
    return Result.unauthorized("Token无效或已过期");
}
Long userId = jwtUtils.getUserIdFromToken(token);
```

---

## 📝 参数校验

### 使用 @Valid 注解

Controller 使用 `@Valid` 注解自动校验请求参数：

```java
@PostMapping("/grab")
public Result<TicketOrderVO> grabTicket(
    @Valid @RequestBody GrabTicketDTO dto, // @Valid 自动校验
    HttpServletRequest request
) {
    // ...
}
```

### DTO 中的校验注解

在 DTO 类中使用 `javax.validation` 注解：

```java
public class GrabTicketDTO {
    @NotNull(message = "演出活动ID不能为空")
    private Long showEventId;

    @NotEmpty(message = "座位列表不能为空")
    @Size(min = 1, max = 4, message = "每次最多选择4个座位")
    private List<Long> seatIds;

    @NotNull(message = "联系人姓名不能为空")
    @Size(min = 2, max = 50, message = "联系人姓名长度为2-50个字符")
    private String contactName;

    // ...
}
```

### 校验失败响应

参数校验失败时，Spring 会自动返回 400 错误：

```json
{
  "code": 400,
  "message": "座位列表不能为空",
  "data": null
}
```

---

## 📊 接口详细说明

### 1. 抢票接口（核心）⚡

**接口**: `POST /api/ticket/grab`

**功能**：
- 验证用户身份（JWT token）
- 检查演出状态和售票时间
- 检查用户限购
- CAS 锁定座位（乐观锁）
- 创建订单（15分钟过期）
- 返回订单信息

**请求示例**：

```bash
curl -X POST "http://localhost:8080/api/ticket/grab" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "showEventId": 1,
    "seatIds": [1, 2],
    "contactName": "张三",
    "contactPhone": "13800138000",
    "contactIdCard": "110101199001011234"
  }'
```

**成功响应**（200）：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "orderNo": "TK1709123456789001",
    "showName": "周杰伦2026世界巡回演唱会-北京站",
    "status": "PENDING",
    "expireTime": "2026-02-07 14:30:00",
    "totalAmount": 2560.00,
    "seatCount": 2,
    "seats": [
      {
        "seatCode": "VIP-1-1",
        "price": 1280.00,
        "status": "LOCKED"
      }
    ]
  }
}
```

**失败响应**（400）：

```json
{
  "code": 400,
  "message": "座位已被锁定或售出: VIP-1-1",
  "data": null
}
```

**限流说明**：

抢票接口需要添加限流保护，建议配置：
- **限流策略**：100 req/s（每秒100个请求）
- **实现方式**：Guava RateLimiter 或 Redis + Lua 脚本
- **超限响应**：429 Too Many Requests

```java
// 伪代码示例（需要在拦截器中实现）
@PostMapping("/grab")
// @RateLimit(qps = 100, message = "请求过于频繁，请稍后再试")
public Result<TicketOrderVO> grabTicket(...) {
    // ...
}
```

---

### 2. 支付订单接口

**接口**: `POST /api/ticket/pay`

**功能**：
- 验证订单所有权
- 验证订单状态（必须是 PENDING）
- 更新订单状态为 PAID
- 确认座位为已售出（LOCKED → SOLD）
- 返回订单信息

**请求示例**：

```bash
curl -X POST "http://localhost:8080/api/ticket/pay" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 123,
    "payType": "ALIPAY"
  }'
```

---

### 3. 取消订单接口

**接口**: `POST /api/ticket/cancel/{orderId}`

**功能**：
- 验证订单所有权
- 验证订单状态（只能取消 PENDING 状态）
- 释放座位（LOCKED → AVAILABLE）
- 更新订单状态为 CANCELLED
- 减少用户购票记录

**请求示例**：

```bash
curl -X POST "http://localhost:8080/api/ticket/cancel/123" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 4. 查询订单详情接口

**接口**: `GET /api/ticket/order/{orderId}`

**功能**：
- 验证订单所有权
- 查询订单详细信息
- 包含座位列表

**请求示例**：

```bash
curl -X GET "http://localhost:8080/api/ticket/order/123" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

### 5. 查询我的订单列表接口

**接口**: `GET /api/ticket/orders`

**功能**：
- 分页查询当前用户的所有订单
- 支持订单状态筛选
- 按创建时间倒序排列

**请求示例**：

```bash
# 查询所有订单
curl -X GET "http://localhost:8080/api/ticket/orders?page=1&size=10" \
  -H "Authorization: Bearer YOUR_TOKEN"

# 查询待支付订单
curl -X GET "http://localhost:8080/api/ticket/orders?status=PENDING" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

---

## 📖 Swagger 文档

### 访问地址

启动项目后，访问 Swagger UI：

```
http://localhost:8080/swagger-ui.html
```

### Swagger 注解说明

#### @Api 注解（类级别）

```java
@Api(tags = "抢票模块", description = "抢票、支付订单、取消订单、查询订单等操作")
public class TicketController {
    // ...
}
```

#### @ApiOperation 注解（方法级别）

```java
@ApiOperation(value = "抢票", notes = "核心抢票接口，支持选座模式和快速抢票模式。需要JWT认证。建议添加限流保护（100 req/s）")
public Result<TicketOrderVO> grabTicket(...) {
    // ...
}
```

#### @ApiParam 注解（参数级别）

```java
@ApiParam(value = "页码", defaultValue = "1")
@RequestParam(value = "page", defaultValue = "1") Integer page
```

---

## 🔥 性能优化建议

### 1. 接口限流

**抢票接口**必须添加限流，防止恶意刷票：

```java
// 方式1：使用 Guava RateLimiter（单机限流）
private RateLimiter rateLimiter = RateLimiter.create(100.0); // 100 qps

@PostMapping("/grab")
public Result<TicketOrderVO> grabTicket(...) {
    if (!rateLimiter.tryAcquire()) {
        return Result.error("请求过于频繁，请稍后再试");
    }
    // ...
}

// 方式2：使用 Redis + Lua 脚本（分布式限流）
// 详见限流工具类实现
```

### 2. 接口缓存

演出详情接口已在 Service 层使用 Redis 缓存：
- **缓存Key**: `ticket:show:detail:{showEventId}`
- **过期时间**: 1小时
- **更新策略**: 抢票、支付、取消后清除缓存

### 3. 响应压缩

在 `application.yml` 中启用 Gzip 压缩：

```yaml
server:
  compression:
    enabled: true
    min-response-size: 1024
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
```

### 4. 异步处理

Kafka 消息发送已使用异步处理：
- 抢票成功后异步发送 Kafka 消息
- 不阻塞主流程
- 提高响应速度

---

## 🛡️ 安全防护

### 1. SQL 注入防护

使用 MyBatis-Plus 的参数化查询，自动防止 SQL 注入：

```java
// 安全：使用参数化查询
queryWrapper.eq(ShowEvent::getId, id);

// 不安全：字符串拼接（MyBatis-Plus 不会出现这种情况）
// "SELECT * FROM tb_show_event WHERE id = " + id
```

### 2. XSS 防护

在返回 HTML 内容时，自动转义特殊字符：

```java
// Spring Boot 默认启用 XSS 防护
// 可以在配置类中自定义 XSS 过滤器
```

### 3. CSRF 防护

对于修改数据的接口（POST、PUT、DELETE），启用 CSRF 防护：

```yaml
# application.yml
spring:
  security:
    csrf:
      enabled: true
```

### 4. 限流防护

抢票接口必须添加限流，防止恶意攻击：
- 单IP限流：每秒最多10次
- 全局限流：每秒最多1000次

---

## ✅ 第五步完成检查清单

- [x] 创建 ShowEventController（4个接口）
- [x] 创建 TicketController（5个接口）
- [x] 添加 JWT 认证机制
- [x] 添加参数校验（@Valid）
- [x] 添加 Swagger 文档注解
- [x] 统一使用 Result 封装响应
- [x] 添加限流说明（抢票接口）
- [x] 创建 API 测试指南文档
- [x] 创建 Postman 测试集合示例

---

## 🚀 下一步：第六步 - 系统测试与优化

第六步将进行系统测试和性能优化：

### 1. 单元测试
- Service 层单元测试（Mockito）
- Controller 层单元测试（MockMvc）
- Mapper 层单元测试

### 2. 集成测试
- 完整业务流程测试
- 事务回滚测试
- 异常场景测试

### 3. 性能测试
- 抢票接口压力测试（JMeter / wrk）
- 并发场景测试（1000+ 并发）
- 防超卖验证测试
- 限购验证测试

### 4. 优化建议
- 数据库索引优化
- SQL 查询优化
- Redis 缓存优化
- JVM 参数调优
- 连接池配置优化

### 5. 监控告警
- 添加 Prometheus + Grafana 监控
- 添加接口性能指标
- 添加订单超时告警
- 添加座位库存告警

---

## 📄 相关文档

- **API 测试指南**: `docs/ticket_system_api_guide.md`
- **第一步总结**: `docs/ticket_system_step1_summary.md` - 数据库设计
- **第二步总结**: `docs/ticket_system_step2_summary.md` - 实体类和 VO/DTO
- **第三步总结**: `docs/ticket_system_step3_summary.md` - Mapper 接口
- **第四步总结**: `docs/ticket_system_step4_summary.md` - Service 业务逻辑
- **第五步总结**: `docs/ticket_system_step5_summary.md`（本文档）- Controller 接口

---

**✨ 第五步完成！已创建 2 个 Controller（9个接口），提供完整的 RESTful API。**

**系统已具备完整的高并发抢票能力，包括：**
- ✅ 演出活动查询
- ✅ 座位查询
- ✅ 高并发抢票（三重防超卖机制）
- ✅ 订单支付
- ✅ 订单取消
- ✅ 订单查询
- ✅ 自动超时处理（定时任务）

**可以开始进行接口测试和性能压测！**
