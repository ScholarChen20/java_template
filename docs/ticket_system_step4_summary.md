# 演唱会抢票系统 - 第四步完成

## 📦 第四步：Service 层创建完成

### 1. Service 接口和实现类列表（3个服务）

所有 Service 位于 `com.example.yoyo_data.service` 和 `com.example.yoyo_data.service.impl` 包：

| Service | 实现类 | 核心功能 | 方法数 |
|---------|-------|---------|--------|
| ShowEventService | ShowEventServiceImpl | 演出活动查询服务 | 4个 |
| TicketService | TicketServiceImpl | 抢票核心业务逻辑 | 5个 |
| OrderTimeoutService | OrderTimeoutServiceImpl | 订单超时处理（定时任务） | 2个 |

---

## 🎯 核心功能详解

### 1. ShowEventService（演出活动服务）

**功能**：提供演出活动和座位信息的查询服务

| 方法名 | 参数 | 返回值 | 说明 |
|-------|------|--------|------|
| getShowEventList | page, size, status, showType | Page<ShowEventVO> | 分页查询演出活动列表（支持状态和类型筛选） |
| getShowEventDetail | showEventId | ShowEventVO | 查询演出活动详情（Redis缓存，1小时过期） |
| getSeatList | QuerySeatDTO | Page<SeatVO> | 分页查询座位列表（支持区域和状态筛选） |
| getAvailableSeatCount | showEventId, seatZone | Integer | 查询指定区域的可售座位数量 |

**核心特性**：
- ✅ 演出详情使用 **Redis 缓存**（1小时过期）
- ✅ 自动计算最低票价和最高票价
- ✅ 支持多维度筛选（状态、类型、区域）
- ✅ 按开票时间倒序排列

---

### 2. TicketService（抢票服务） - **核心**

**功能**：实现高并发抢票的核心业务逻辑

#### 2.1 grabTicket（抢票接口） - **最核心**

**完整业务流程**：

```java
@Transactional(rollbackFor = Exception.class)
public Result<TicketOrderVO> grabTicket(GrabTicketDTO dto, String token) {
    // 1. 验证 token，获取用户ID
    // 2. 验证演出活动（存在性、状态、售票时间）
    // 3. 获取 Redis 分布式锁（防止用户重复抢票）
    //    lockKey = "ticket:lock:grab:{userId}:{showEventId}"
    //    过期时间：10秒

    try {
        // 4. 检查用户限购（查询 UserTicketRecord）
        // 5. 验证座位可用性（循环检查每个座位）
        // 6. 创建订单（15分钟过期时间）
        // 7. CAS 锁定座位（乐观锁，逐个锁定）
        //    WHERE id = ? AND status = 'AVAILABLE' AND version = ?
        // 8. 更新演出活动座位数（乐观锁）
        //    WHERE id = ? AND available_seats >= ? AND version = ?
        // 9. 创建订单座位关联（批量插入）
        // 10. 更新用户购票记录（原子性增加）
        // 11. 清除演出详情缓存
        // 12. 发送 Kafka 消息（ORDER_CREATE）
        // 13. 返回订单信息
    } finally {
        // 14. 释放分布式锁
    }
}
```

**防超卖机制**：
- **Redis 分布式锁**：防止同一用户重复提交（10秒锁）
- **座位乐观锁**：`WHERE status = 'AVAILABLE' AND version = ?`
- **演出乐观锁**：`WHERE available_seats >= ? AND version = ?`
- **事务回滚**：任何步骤失败，整个事务回滚

**性能优化**：
- 先检查限购，避免无效的座位锁定
- 先锁定座位，再更新演出座位数（减少锁冲突）
- 异步发送 Kafka 消息（不阻塞主流程）

---

#### 2.2 payOrder（支付订单）

**业务流程**：

```java
@Transactional(rollbackFor = Exception.class)
public Result<TicketOrderVO> payOrder(PayOrderDTO dto, String token) {
    // 1. 验证 token 和订单所有权
    // 2. 验证订单状态（必须是 PENDING）
    // 3. 验证订单是否过期
    // 4. 查询订单座位
    // 5. 更新订单状态为已支付（PAID）
    //    WHERE id = ? AND status = 'PENDING'
    // 6. 批量确认座位为已售出（LOCKED → SOLD）
    //    WHERE id IN (...) AND status = 'LOCKED'
    // 7. 更新演出活动座位数（locked → sold）
    // 8. 清除演出详情缓存
    // 9. 发送 Kafka 消息（ORDER_PAID）
    // 10. 返回订单信息
}
```

**座位状态流转**：
```
LOCKED (锁定) → SOLD (已售出)
```

---

#### 2.3 cancelOrder（取消订单）

**业务流程**：

```java
@Transactional(rollbackFor = Exception.class)
public Result<Void> cancelOrder(Long orderId, String token) {
    // 1. 验证 token 和订单所有权
    // 2. 验证订单状态（只能取消 PENDING 状态的订单）
    // 3. 查询订单座位
    // 4. 批量释放座位（LOCKED → AVAILABLE）
    //    WHERE id IN (...) AND status = 'LOCKED'
    // 5. 更新演出活动座位数（locked → available）
    // 6. 更新订单状态为已取消（CANCELLED）
    // 7. 减少用户购票记录（原子性减少）
    // 8. 清除演出详情缓存
    // 9. 发送 Kafka 消息（ORDER_CANCEL）
}
```

**座位状态流转**：
```
LOCKED (锁定) → AVAILABLE (可售)
```

---

#### 2.4 queryOrder（查询订单详情）

- 验证 token 和订单所有权
- 查询订单、演出、座位信息
- 组装返回 TicketOrderVO

---

#### 2.5 queryMyOrders（查询我的订单列表）

- 验证 token
- 分页查询用户的所有订单
- 支持订单状态筛选
- 按创建时间倒序排列

---

### 3. OrderTimeoutService（订单超时处理服务）

**功能**：定时任务自动处理过期订单和座位

#### 3.1 handleExpiredOrders（处理过期订单）

**定时任务配置**：
```java
@Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
```

**业务流程**：

```java
@Transactional(rollbackFor = Exception.class)
public int handleExpiredOrders() {
    // 1. 查询所有已过期的待支付订单
    //    WHERE status = 'PENDING' AND expire_time < NOW()

    // 2. 逐个处理过期订单：
    //    2.1 查询订单座位
    //    2.2 批量释放座位（LOCKED → AVAILABLE）
    //    2.3 更新演出活动座位数（locked → available）
    //    2.4 更新订单状态为超时（TIMEOUT）
    //    2.5 减少用户购票记录
    //    2.6 发送 Kafka 消息（ORDER_TIMEOUT）

    // 3. 返回处理的订单数量
}
```

**容错机制**：
- 单个订单处理失败不影响其他订单
- 每个订单独立的 try-catch
- 详细的日志记录

---

#### 3.2 handleExpiredSeats（处理过期锁定座位）

**定时任务配置**：
```java
@Scheduled(cron = "0 * * * * ?") // 每分钟执行一次
```

**业务流程**：

```java
public int handleExpiredSeats() {
    // 1. 批量释放所有过期的锁定座位
    //    UPDATE tb_seat SET status = 'AVAILABLE', ...
    //    WHERE status = 'LOCKED' AND lock_expire_time < NOW()

    // 2. 返回释放的座位数量
}
```

**作用**：
- 作为兜底策略
- 防止订单表更新失败导致座位永久锁定
- 与 handleExpiredOrders 互为补充

---

## 🔥 高并发设计亮点

### 1. 三重防超卖机制

| 层级 | 机制 | 实现方式 | 作用范围 |
|------|------|---------|---------|
| 第一层 | Redis 分布式锁 | `setIfAbsent(lockKey, "1", 10秒)` | 防止同一用户重复抢票 |
| 第二层 | 座位乐观锁 | `WHERE status = 'AVAILABLE' AND version = ?` | 防止同一座位被多人抢到 |
| 第三层 | 演出乐观锁 | `WHERE available_seats >= ? AND version = ?` | 防止座位数超卖 |

---

### 2. 缓存策略

| 缓存类型 | Key格式 | 过期时间 | 更新时机 |
|---------|---------|---------|---------|
| 演出详情 | `ticket:show:detail:{showEventId}` | 1小时 | 抢票、支付、取消后清除 |
| 分布式锁 | `ticket:lock:grab:{userId}:{showEventId}` | 10秒 | 抢票流程结束后释放 |

---

### 3. 异步处理

**Kafka 事件**：

| 事件类型 | Topic | 优先级 | 触发时机 |
|---------|-------|--------|---------|
| ORDER_CREATE | order-create | 8 | 抢票成功 |
| ORDER_PAID | order-paid | 9（最高） | 支付成功 |
| ORDER_CANCEL | order-cancel | 7 | 取消订单 |
| ORDER_TIMEOUT | order-timeout | 7 | 订单超时 |

**用途**：
- 发送用户通知（短信、邮件）
- 数据统计分析
- 日志审计
- 削峰填谷

---

### 4. 事务管理

所有写操作使用 `@Transactional(rollbackFor = Exception.class)`：
- 抢票流程：14个步骤，任何一步失败整体回滚
- 支付流程：10个步骤，原子性提交
- 取消流程：9个步骤，确保数据一致性

---

### 5. 定时任务

| 任务 | 执行频率 | 作用 |
|------|---------|------|
| handleExpiredOrders | 每分钟 | 处理过期订单，释放座位 |
| handleExpiredSeats | 每分钟 | 兜底策略，释放过期锁定座位 |

**优势**：
- 自动化处理，无需人工干预
- 保证座位资源及时释放
- 提高系统可用性

---

## 📊 数据流转图

### 抢票流程

```
用户请求
    ↓
验证 token + 演出状态
    ↓
获取 Redis 分布式锁
    ↓
检查用户限购
    ↓
验证座位可用性
    ↓
创建订单（15分钟过期）
    ↓
CAS 锁定座位（乐观锁）
    ↓
更新演出座位数（乐观锁）
    ↓
创建订单座位关联
    ↓
更新用户购票记录
    ↓
清除 Redis 缓存
    ↓
发送 Kafka 消息
    ↓
释放分布式锁
    ↓
返回订单信息
```

### 订单状态流转

```
PENDING (待支付，15分钟)
    ↓ payOrder
PAID (已支付)

PENDING (待支付)
    ↓ cancelOrder
CANCELLED (已取消)

PENDING (待支付)
    ↓ handleExpiredOrders（定时任务）
TIMEOUT (超时取消)
```

### 座位状态流转

```
AVAILABLE (可售)
    ↓ lockSeatWithCAS
LOCKED (锁定，15分钟)
    ↓ confirmSeatSold / releaseSeat
SOLD (已售出) / AVAILABLE (可售)
```

---

## ✅ 第四步完成检查清单

- [x] 创建 ShowEventService 接口和实现类（4个方法）
- [x] 创建 TicketService 接口和实现类（5个方法）
- [x] 创建 OrderTimeoutService 接口和实现类（2个定时任务）
- [x] 实现 grabTicket 核心抢票逻辑（14个步骤）
- [x] 实现 payOrder 支付逻辑（10个步骤）
- [x] 实现 cancelOrder 取消逻辑（9个步骤）
- [x] 实现 queryOrder 和 queryMyOrders 查询逻辑
- [x] 实现定时任务：处理过期订单
- [x] 实现定时任务：处理过期座位（兜底策略）
- [x] 添加 Redis 分布式锁
- [x] 添加乐观锁防超卖
- [x] 添加事务管理
- [x] 添加 Kafka 异步消息
- [x] 添加 Redis 缓存策略
- [x] 添加完整的日志记录
- [x] 添加完整的异常处理
- [x] 添加 Kafka Topic 常量（ORDER_CREATE, ORDER_PAID, ORDER_CANCEL, ORDER_TIMEOUT）

---

## 🚀 下一步：第五步 - 实现 Controller 层

第五步将创建 HTTP 接口：

### 1. ShowEventController（演出活动控制器）
- `GET /api/ticket/shows` - 查询演出列表
- `GET /api/ticket/shows/{id}` - 查询演出详情
- `GET /api/ticket/seats` - 查询座位列表
- `GET /api/ticket/seats/count` - 查询可售座位数量

### 2. TicketController（抢票控制器）
- `POST /api/ticket/grab` - 抢票（核心接口）
- `POST /api/ticket/pay` - 支付订单
- `POST /api/ticket/cancel/{orderId}` - 取消订单
- `GET /api/ticket/order/{orderId}` - 查询订单详情
- `GET /api/ticket/orders` - 查询我的订单列表

### 3. 核心技术栈
- **限流**：使用 `@RateLimit` 注解（抢票接口：100 req/s）
- **JWT 认证**：从 Header 获取 token
- **参数校验**：使用 `@Valid` 注解
- **统一响应**：使用 `Result` 封装
- **异常处理**：全局异常处理器
- **API 文档**：Swagger 注解

---

**✨ 第四步完成！已创建 3 个 Service（11个方法），实现了完整的高并发抢票业务逻辑。**
