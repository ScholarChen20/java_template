# 演唱会抢票系统 - 第二步完成

## 📦 第二步：实体类和VO/DTO对象创建完成

### 1. 实体类（Entity）- 5个

实体类位于 `com.example.yoyo_data.common.entity` 包：

| 类名 | 文件 | 对应数据表 | 说明 |
|------|------|-----------|------|
| ShowEvent | ShowEvent.java | tb_show_event | 演出活动表 |
| Seat | Seat.java | tb_seat | 座位信息表 |
| TicketOrder | TicketOrder.java | tb_ticket_order | 票务订单表 |
| OrderSeat | OrderSeat.java | tb_order_seat | 订单座位关联表 |
| UserTicketRecord | UserTicketRecord.java | tb_user_ticket_record | 用户购票记录表 |

**特点**：
- 使用 MyBatis-Plus 注解：`@TableName`, `@TableId`, `@TableField`
- 使用 Lombok 注解：`@Data`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`
- 包含乐观锁 `version` 字段
- 实现 `Serializable` 接口

---

### 2. VO 类（View Object）- 3个

视图对象位于 `com.example.yoyo_data.common.vo` 包：

| 类名 | 文件 | 用途 |
|------|------|------|
| ShowEventVO | ShowEventVO.java | 演出活动详情展示 |
| SeatVO | SeatVO.java | 座位信息展示 |
| TicketOrderVO | TicketOrderVO.java | 订单详情展示（包含座位列表） |

**特点**：
- 用于 API 响应数据返回
- 使用 `@JsonFormat` 格式化日期时间字段
- 包含关联数据（如 TicketOrderVO 包含座位列表）
- 隐藏敏感字段（如身份证只保留前6位后4位）

---

### 3. DTO 类（Data Transfer Object）- 3个

数据传输对象位于 `com.example.yoyo_data.common.dto` 包：

| 类名 | 文件 | 用途 |
|------|------|------|
| GrabTicketDTO | GrabTicketDTO.java | 抢票请求参数 |
| PayOrderDTO | PayOrderDTO.java | 支付订单请求参数 |
| QuerySeatDTO | QuerySeatDTO.java | 查询座位请求参数 |

**特点**：
- 用于接收客户端请求参数
- 使用 `javax.validation` 注解进行参数校验
- 支持两种抢票模式：
  - **选座模式**：用户指定具体座位ID列表
  - **快速抢票模式**：系统自动分配指定区域的座位

---

### 4. 常量类（Constant）- 6个

常量定义位于 `com.example.yoyo_data.common.constant` 包：

| 类名 | 文件 | 说明 |
|------|------|------|
| SeatStatus | SeatStatus.java | 座位状态常量（AVAILABLE, LOCKED, SOLD） |
| OrderStatus | OrderStatus.java | 订单状态常量（PENDING, PAID, CANCELLED, TIMEOUT） |
| ShowEventStatus | ShowEventStatus.java | 演出状态常量（PENDING, SELLING, SOLD_OUT, ENDED） |
| ShowType | ShowType.java | 演出类型常量（CONCERT, MOVIE, DRAMA） |
| PayType | PayType.java | 支付方式常量（ALIPAY, WECHAT, CARD） |
| TicketRedisKey | TicketRedisKey.java | Redis缓存Key前缀和过期时间常量 |

**特点**：
- 避免硬编码字符串
- 统一管理业务状态和类型
- 定义 Redis 缓存策略（Key前缀、过期时间）

---

## 🎯 核心业务对象说明

### 抢票请求对象（GrabTicketDTO）

支持两种抢票模式：

#### 模式1：选座模式（精确抢票）
```java
{
  "showEventId": 1,
  "seatIds": [1001, 1002],  // 用户指定具体座位ID
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactIdCard": "110101199001011234"
}
```

#### 模式2：快速抢票模式（系统自动分配）
```java
{
  "showEventId": 1,
  "seatZone": "VIP",        // 指定区域
  "seatCount": 2,           // 需要数量
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactIdCard": "110101199001011234"
}
```

---

## 📊 Redis 缓存策略

在 `TicketRedisKey` 中定义了完整的缓存策略：

| 缓存类型 | Key格式 | 过期时间 | 用途 |
|---------|---------|---------|------|
| 座位库存 | `ticket:seat:stock:{showEventId}` | 30分钟 | 缓存可售座位列表，减少数据库查询 |
| 座位锁定 | `ticket:seat:lock:{seatId}` | 15分钟 | 标记座位被锁定（订单待支付） |
| 用户购票记录 | `ticket:user:record:{userId}:{showEventId}` | - | 用于限购控制 |
| 演出详情 | `ticket:show:detail:{showEventId}` | 1小时 | 缓存演出活动详情 |
| 订单信息 | `ticket:order:{orderId}` | 15分钟 | 临时缓存订单信息 |
| 分布式锁 | `ticket:lock:grab:{userId}:{showEventId}` | 10秒 | 防止用户重复抢票 |

---

## ✅ 第二步完成检查清单

- [x] 创建5个实体类（Entity）
- [x] 创建3个VO类（视图对象）
- [x] 创建3个DTO类（请求对象）
- [x] 创建6个常量类（业务常量）
- [x] 定义Redis缓存策略
- [x] 支持两种抢票模式（选座 + 快速抢票）
- [x] 参数校验注解完整

---

## 🚀 下一步：第三步 - 创建 Mapper 接口

第三步将创建 MyBatis-Plus 的 Mapper 接口，用于数据库操作：

1. **ShowEventMapper** - 演出活动数据访问
   - 查询演出列表
   - 查询演出详情
   - 更新座位数统计

2. **SeatMapper** - 座位数据访问
   - 查询可售座位
   - 锁定座位（CAS更新）
   - 释放座位

3. **TicketOrderMapper** - 订单数据访问
   - 创建订单
   - 查询订单
   - 更新订单状态

4. **OrderSeatMapper** - 订单座位关联
   - 批量插入订单座位关系
   - 查询订单的座位列表

5. **UserTicketRecordMapper** - 用户购票记录
   - 查询用户已购票数
   - 更新购票记录

---

## 📝 注意事项

1. **数据库名称**：所有代码使用 `travel_agent` 数据库
2. **乐观锁**：`ShowEvent` 和 `Seat` 表包含 `version` 字段，用于防止超卖
3. **缓存策略**：座位库存使用30分钟缓存，订单使用15分钟缓存
4. **座位锁定**：座位锁定后15分钟自动释放（订单过期时间）
5. **限购控制**：通过 `UserTicketRecord` 表实现每场演出的限购功能

---

**✨ 第二步完成！已创建17个文件，包含实体类、VO/DTO对象和常量定义。**
