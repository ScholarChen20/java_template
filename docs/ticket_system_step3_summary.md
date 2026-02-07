# 演唱会抢票系统 - 第三步完成

## 📦 第三步：Mapper 接口创建完成

### 1. Mapper 接口列表（5个）

所有 Mapper 接口位于 `com.example.yoyo_data.infrastructure.repository` 包：

| Mapper 接口 | 文件 | 对应实体类 | 核心功能 |
|------------|------|-----------|---------|
| ShowEventMapper | ShowEventMapper.java | ShowEvent | 演出活动数据访问 + 座位数统计更新 |
| SeatMapper | SeatMapper.java | Seat | 座位数据访问 + CAS 锁定/释放座位 |
| TicketOrderMapper | TicketOrderMapper.java | TicketOrder | 订单数据访问 + 状态更新 |
| OrderSeatMapper | OrderSeatMapper.java | OrderSeat | 订单座位关联数据访问 |
| UserTicketRecordMapper | UserTicketRecordMapper.java | UserTicketRecord | 用户购票记录 + 原子性计数 |

---

## 🎯 核心功能详解

### 1. ShowEventMapper（演出活动）

**继承**：`BaseMapper<ShowEvent>`

**自定义方法**：

| 方法名 | 参数 | 返回值 | 说明 |
|-------|------|--------|------|
| lockSeats | showEventId, seatCount, version | int | 使用乐观锁减少可售座位数，增加锁定座位数 |
| confirmSeats | showEventId, seatCount | int | 减少锁定座位数，增加已售座位数（支付成功） |
| releaseSeats | showEventId, seatCount | int | 释放锁定座位，增加可售座位数（订单取消） |

**核心特性**：
- ✅ 使用乐观锁（version字段）防止并发冲突
- ✅ 原子性更新座位数统计
- ✅ 自动更新 updated_at 时间戳

**示例 SQL**：
```sql
-- 锁定座位（乐观锁）
UPDATE tb_show_event SET
  available_seats = available_seats - #{seatCount},
  locked_seats = locked_seats + #{seatCount},
  version = version + 1,
  updated_at = NOW()
WHERE id = #{showEventId}
  AND available_seats >= #{seatCount}
  AND version = #{version}
```

---

### 2. SeatMapper（座位信息）

**继承**：`BaseMapper<Seat>`

**自定义方法**：

| 方法名 | 参数 | 返回值 | 说明 |
|-------|------|--------|------|
| lockSeatWithCAS | seatId, userId, orderId, lockExpireTime, version | int | CAS 锁定单个座位（乐观锁） |
| confirmSeatSold | seatId | int | 确认单个座位已售出 |
| batchConfirmSeatSold | seatIds | int | 批量确认座位已售出 |
| releaseSeat | seatId | int | 释放单个座位 |
| batchReleaseSeat | seatIds | int | 批量释放座位 |
| releaseExpiredSeats | now | int | 释放已过期的锁定座位（定时任务） |

**核心特性**：
- ✅ CAS（Compare-And-Swap）操作防止超卖
- ✅ 乐观锁（version字段）实现无锁并发
- ✅ 座位状态流转：AVAILABLE → LOCKED → SOLD
- ✅ 支持批量操作（使用 MyBatis 动态 SQL）
- ✅ 自动过期释放机制

**状态流转图**：
```
AVAILABLE (可售)
    ↓ lockSeatWithCAS
LOCKED (锁定，15分钟过期)
    ↓ confirmSeatSold (支付成功)
SOLD (已售出)

LOCKED (锁定)
    ↓ releaseSeat (订单取消/超时)
AVAILABLE (可售)
```

**关键 SQL**：
```sql
-- CAS 锁定座位（核心防超卖逻辑）
UPDATE tb_seat SET
  status = 'LOCKED',
  lock_time = NOW(),
  lock_expire_time = #{lockExpireTime},
  order_id = #{orderId},
  user_id = #{userId},
  version = version + 1,
  updated_at = NOW()
WHERE id = #{seatId}
  AND status = 'AVAILABLE'   -- 只能锁定可售状态的座位
  AND version = #{version}   -- 乐观锁版本校验
```

---

### 3. TicketOrderMapper（票务订单）

**继承**：`BaseMapper<TicketOrder>`

**自定义方法**：

| 方法名 | 参数 | 返回值 | 说明 |
|-------|------|--------|------|
| updateOrderToPaid | orderId, payType, payTime | int | 更新订单为已支付状态 |
| updateOrderToCancelled | orderId | int | 更新订单为已取消状态 |
| updateOrderToTimeout | orderId | int | 更新订单为超时取消状态 |
| batchUpdateExpiredOrders | now | int | 批量更新过期订单（定时任务） |

**核心特性**：
- ✅ 订单状态流转：PENDING → PAID / CANCELLED / TIMEOUT
- ✅ 只能从 PENDING 状态转换到其他状态（防止误操作）
- ✅ 支持批量处理过期订单

**订单状态流转图**：
```
PENDING (待支付，15分钟过期)
    ↓ updateOrderToPaid
PAID (已支付，完成)

PENDING (待支付)
    ↓ updateOrderToCancelled (用户主动取消)
CANCELLED (已取消)

PENDING (待支付)
    ↓ updateOrderToTimeout (15分钟未支付)
TIMEOUT (超时取消)
```

---

### 4. OrderSeatMapper（订单座位关联）

**继承**：`BaseMapper<OrderSeat>`

**自定义方法**：无（使用 MyBatis-Plus 默认方法即可）

**常用操作**：
- `saveBatch(List<OrderSeat>)`：批量插入订单座位关联
- `selectList(Wrapper)`：查询订单的座位列表
- 无需物理删除关联记录（保留历史数据）

---

### 5. UserTicketRecordMapper（用户购票记录）

**继承**：`BaseMapper<UserTicketRecord>`

**自定义方法**：

| 方法名 | 参数 | 返回值 | 说明 |
|-------|------|--------|------|
| increaseTicketCount | userId, showEventId, ticketCount | int | 原子性增加用户购票数 |
| decreaseTicketCount | userId, showEventId, ticketCount | int | 原子性减少用户购票数（订单取消） |

**核心特性**：
- ✅ 原子性操作（使用 SQL 的 `ticket_count = ticket_count + #{ticketCount}`）
- ✅ 用于限购控制（每场演出限购2-4张）
- ✅ 唯一约束：(user_id, show_event_id)

**关键 SQL**：
```sql
-- 原子性增加购票数
UPDATE tb_user_ticket_record SET
  ticket_count = ticket_count + #{ticketCount},
  updated_at = NOW()
WHERE user_id = #{userId}
  AND show_event_id = #{showEventId}
```

---

## 🔥 高并发优化设计

### 1. 乐观锁 CAS 操作

所有涉及库存、座位状态的更新都使用乐观锁：

```java
// ShowEventMapper.lockSeats - 使用 version 字段
WHERE id = #{showEventId}
  AND available_seats >= #{seatCount}
  AND version = #{version}  // 乐观锁

// SeatMapper.lockSeatWithCAS - 使用 version 字段 + 状态校验
WHERE id = #{seatId}
  AND status = 'AVAILABLE'  // 状态校验
  AND version = #{version}  // 乐观锁
```

**优势**：
- 无需加悲观锁（SELECT FOR UPDATE）
- 并发性能高
- 失败时返回 0，上层重试即可

---

### 2. 批量操作

使用 MyBatis 动态 SQL 实现批量更新：

```xml
<!-- SeatMapper.batchConfirmSeatSold -->
<script>
UPDATE tb_seat SET
  status = 'SOLD',
  version = version + 1,
  updated_at = NOW()
WHERE id IN
<foreach collection='seatIds' item='seatId' open='(' separator=',' close=')'>
  #{seatId}
</foreach>
AND status = 'LOCKED'
</script>
```

---

### 3. 定时任务支持

提供两个定时任务方法：

| 方法 | 调用频率 | 作用 |
|------|---------|------|
| SeatMapper.releaseExpiredSeats | 每1分钟 | 释放过期锁定的座位 |
| TicketOrderMapper.batchUpdateExpiredOrders | 每1分钟 | 将过期订单设置为超时状态 |

---

## 📊 数据一致性保证

### 事务边界

抢票流程需要在同一事务中执行：

```java
@Transactional(rollbackFor = Exception.class)
public Result grabTicket(GrabTicketDTO dto) {
    // 1. 检查用户限购
    // 2. 锁定座位（SeatMapper.lockSeatWithCAS）
    // 3. 更新演出活动座位数（ShowEventMapper.lockSeats）
    // 4. 创建订单（TicketOrderMapper.insert）
    // 5. 创建订单座位关联（OrderSeatMapper.saveBatch）
    // 6. 更新用户购票记录（UserTicketRecordMapper.increaseTicketCount）
}
```

### 失败回滚

如果任何一步失败，整个事务回滚：
- 座位状态恢复为 AVAILABLE
- 演出活动座位数恢复
- 不创建订单

---

## ✅ 第三步完成检查清单

- [x] 创建 5 个 Mapper 接口
- [x] ShowEventMapper：3个自定义方法（乐观锁更新座位数）
- [x] SeatMapper：6个自定义方法（CAS锁定、批量操作、定时释放）
- [x] TicketOrderMapper：4个自定义方法（状态更新、批量过期处理）
- [x] OrderSeatMapper：基础 CRUD（使用 MyBatis-Plus 默认方法）
- [x] UserTicketRecordMapper：2个自定义方法（原子性计数）
- [x] 所有方法使用 `@Update` 注解（无需 XML 配置）
- [x] 支持批量操作（动态 SQL）
- [x] 支持定时任务（过期数据清理）

---

## 🚀 下一步：第四步 - 实现 Service 层

第四步将实现核心业务逻辑：

### 1. ShowEventService（演出活动服务）
- 查询演出列表（分页、筛选）
- 查询演出详情（缓存优化）
- 查询座位分布图

### 2. TicketService（抢票服务） - **核心**
- **grabTicket**：抢票接口（分布式锁 + 乐观锁 + Redis缓存）
  - 用户限购检查
  - 座位可用性检查（Redis缓存）
  - CAS 锁定座位
  - 创建订单（15分钟过期）
  - 发送 Kafka 消息（异步处理）

- **payOrder**：支付订单
  - 更新订单状态为已支付
  - 确认座位为已售出
  - 更新演出活动座位数
  - 清除 Redis 缓存

- **cancelOrder**：取消订单
  - 释放座位
  - 更新订单状态为已取消
  - 减少用户购票记录

- **queryOrder**：查询订单详情

### 3. OrderTimeoutService（订单超时处理服务）
- **定时任务**：每分钟扫描过期订单
  - 调用 SeatMapper.releaseExpiredSeats
  - 调用 TicketOrderMapper.batchUpdateExpiredOrders
  - 减少用户购票记录

### 4. 核心技术栈
- Redis 分布式锁（防止用户重复抢票）
- Redis 缓存（座位库存、演出详情）
- MySQL 乐观锁（防止超卖）
- Kafka 消息队列（异步通知、削峰）
- Spring @Transactional（事务管理）
- Spring @Scheduled（定时任务）

---

**✨ 第三步完成！已创建 5 个 Mapper 接口，包含 15+ 个自定义数据库操作方法。**
