# 抢票系统接口设计文档 v2.0

> 对标大麦网，基于现有 `Result<T>` 统一响应规范、JWT 认证体系、MyBatis-Plus 分页设计。

---

## 目录

- [统一约定](#统一约定)
- [现有接口优化建议](#现有接口优化建议)
- [模块一：演出发现与搜索](#模块一演出发现与搜索)
- [模块二：观演人管理](#模块二观演人管理)
- [模块三：开票提醒与想看](#模块三开票提醒与想看)
- [模块四：退票与退款](#模块四退票与退款)
- [模块五：场馆信息](#模块五场馆信息)
- [模块六：消息通知中心](#模块六消息通知中心)
- [模块七：演出评价](#模块七演出评价)
- [新增 SQL 表结构](#新增-sql-表结构)

---

## 统一约定

### 响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": { },
  "timestamp": "2026-02-25T10:00:00"
}
```

| code | 含义 |
|------|------|
| 200  | 成功 |
| 400  | 请求参数错误 |
| 401  | 未登录 / Token 失效 |
| 403  | 无权限 |
| 404  | 资源不存在 |
| 500  | 服务端错误 |

### 认证方式

需要登录的接口，请求头携带：
```
Authorization: Bearer {JWT_TOKEN}
```

### 分页响应结构

```json
{
  "code": 200,
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 枚举常量速查

| 类型 | 枚举值 |
|------|--------|
| 演出状态 | `PENDING`(待开票) / `SELLING`(售票中) / `SOLD_OUT`(售罄) / `ENDED`(已结束) |
| 订单状态 | `PENDING`(待支付) / `PAID`(已支付) / `CANCELLED`(已取消) / `TIMEOUT`(超时) / `REFUNDING`(退款中) / `REFUNDED`(已退款) |
| 座位状态 | `AVAILABLE`(可售) / `LOCKED`(锁定中) / `SOLD`(已售) |
| 演出分类 | `CONCERT`(演唱会) / `DRAMA`(话剧) / `MUSICAL`(音乐剧) / `SPORTS`(体育) / `COMEDY`(脱口秀) / `CHILDREN`(亲子) |
| 支付方式 | `ALIPAY`(支付宝) / `WECHAT`(微信) / `CARD`(银行卡) |

---

## 现有接口优化建议

### 1. 演出列表补充城市 / 分类筛选

**现状**：`GET /api/ticket/shows` 缺少城市和分类过滤参数，用户无法按城市找演出。

```
GET /api/ticket/shows
  ?city=上海            ← 新增：城市筛选
  &category=CONCERT     ← 新增：演出类型筛选
  &status=SELLING       ← 新增：状态筛选（默认只查售票中）
  &startDate=2026-03-01 ← 新增：演出日期起始
  &endDate=2026-06-30   ← 新增：演出日期截止
  &sortBy=showTime      ← 新增：排序字段（showTime/saleStartTime/hot）
  &page=1&size=20
```

**响应新增字段**（在 `ShowEventVO` 中追加）：
```json
{
  "id": 1,
  "showName": "周杰伦魔天伦世界巡回演唱会",
  "showType": "CONCERT",
  "city": "上海",
  "venueName": "上海梅赛德斯-奔驰文化中心",
  "showTime": "2026-05-01 19:30:00",
  "saleStartTime": "2026-03-01 10:00:00",
  "status": "SELLING",
  "posterUrl": "https://...",
  "minPrice": 280,
  "maxPrice": 1580,
  "availableSeats": 1200,
  "totalSeats": 18000,
  "isWished": false,
  "isReminded": false,
  "hotScore": 9850
}
```

> `ShowEvent` 实体新增 `city` 字段。`minPrice`/`maxPrice` 从 `tb_seat` 聚合查询。
> `isWished`/`isReminded` 需要登录态，未登录时返回 `false`。

---

### 2. 座位查询补充区域筛选 + 价位区域汇总

**现状**：`GET /api/ticket/shows/{showEventId}/seats` 返回所有座位，百万级演出数据量过大。

```
GET /api/ticket/shows/{showEventId}/seats
  ?zone=A               ← 新增：按区域筛选
  &status=AVAILABLE     ← 新增：按状态筛选
  &page=1&size=100
```

---

### 3. 订单状态枚举扩展

**现状**：`TicketOrder.status` 缺少退款相关状态。

```
现有：PENDING / PAID / CANCELLED / TIMEOUT
扩展：+ REFUNDING（退款申请中）
      + REFUNDED（已退款）
      + USED（已使用/已入场）
```

---

### 4. 快速抢票响应补充区域字段

**现状**：`TicketOrderVO` 没有 `seatZone` 字段，用户不知道买的是哪个区域。

在 `TicketOrderVO` 中新增：
```json
{
  "seatZone": "A",
  "zoneName": "A区（880元档）",
  "seatNote": "具体座位号将在演出前7天通过消息通知告知"
}
```

---

## 模块一：演出发现与搜索

**Base Path**：`/api/ticket/shows`

---

### 1.1 演出关键词搜索

```
GET /api/ticket/shows/search
```

**Query Parameters**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `keyword` | String | 否 | 关键词，匹配演出名/艺人名/场馆名 |
| `city` | String | 否 | 城市，如"上海" |
| `category` | String | 否 | 演出类型枚举 |
| `startDate` | String | 否 | 演出日期起，格式`yyyy-MM-dd` |
| `endDate` | String | 否 | 演出日期止 |
| `priceMin` | Integer | 否 | 最低票价（元） |
| `priceMax` | Integer | 否 | 最高票价（元） |
| `status` | String | 否 | 演出状态，默认 `SELLING` |
| `sortBy` | String | 否 | 排序：`showTime`/`saleStartTime`/`hot`(热度)，默认`hot` |
| `page` | Integer | 否 | 页码，默认 1 |
| `size` | Integer | 否 | 每页大小，默认 20，最大 50 |

**响应示例**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "showName": "周杰伦魔天伦世界巡回演唱会·上海站",
        "showType": "CONCERT",
        "city": "上海",
        "venueName": "上海梅赛德斯-奔驰文化中心",
        "showTime": "2026-05-01 19:30:00",
        "saleStartTime": "2026-03-01 10:00:00",
        "status": "SELLING",
        "posterUrl": "https://cdn.example.com/poster/1.jpg",
        "minPrice": 280,
        "maxPrice": 1580,
        "availableSeats": 1200,
        "isWished": false,
        "isReminded": true,
        "hotScore": 9850
      }
    ],
    "total": 15,
    "current": 1,
    "size": 20
  }
}
```

**业务说明**

- `keyword` 为空时退化为列表筛选
- 搜索结果优先走 Elasticsearch（可选），无 ES 时用 MySQL `LIKE` 兜底
- `hotScore` = 已售票数 × 0.6 + 想看人数 × 0.3 + 浏览量 × 0.1
- 登录态下返回 `isWished`/`isReminded`，未登录均为 `false`

---

### 1.2 获取有演出的城市列表

```
GET /api/ticket/shows/cities
```

**响应示例**

```json
{
  "code": 200,
  "data": [
    { "city": "北京", "showCount": 23, "hotCount": 5 },
    { "city": "上海", "showCount": 31, "hotCount": 8 },
    { "city": "广州", "showCount": 12, "hotCount": 3 }
  ]
}
```

**业务说明**

- 仅返回当前有 `SELLING` 或 `UPCOMING`（未来30天内开票）演出的城市
- 结果缓存 Redis，TTL 1 小时
- `hotCount`：热门演出数量（`hotScore > 5000`）

---

### 1.3 演出分类列表

```
GET /api/ticket/shows/categories
```

**响应示例**

```json
{
  "code": 200,
  "data": [
    { "code": "CONCERT",  "name": "演唱会", "iconUrl": "https://.../concert.png",  "showCount": 45 },
    { "code": "DRAMA",    "name": "话剧",   "iconUrl": "https://.../drama.png",    "showCount": 18 },
    { "code": "MUSICAL",  "name": "音乐剧", "iconUrl": "https://.../musical.png",  "showCount": 22 },
    { "code": "SPORTS",   "name": "体育",   "iconUrl": "https://.../sports.png",   "showCount": 9  },
    { "code": "COMEDY",   "name": "脱口秀", "iconUrl": "https://.../comedy.png",   "showCount": 14 },
    { "code": "CHILDREN", "name": "亲子",   "iconUrl": "https://.../children.png", "showCount": 7  }
  ]
}
```

---

### 1.4 首页聚合数据（热门 / 即将开票 / 猜你喜欢）

```
GET /api/ticket/shows/homepage?city=上海
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "banners": [
      { "id": 1, "title": "周杰伦巡演", "imageUrl": "...", "showEventId": 1 }
    ],
    "hotShows": [ { /* ShowVO */ } ],
    "upcomingShows": [ { /* ShowVO，sale_start_time 在未来48小时内 */ } ],
    "recommendShows": [ { /* 个性化推荐，未登录时返回默认热门 */ } ]
  }
}
```

**业务说明**

- `banners`：运营配置，建议单独做 CMS 管理表
- `hotShows`：按 `hotScore` 降序取前 10，缓存 30 分钟
- `upcomingShows`：`sale_start_time` 在未来 48 小时内，按开票时间升序
- `recommendShows`：登录用户基于历史订单分类推荐，未登录按热度返回

---

### 1.5 价位区域汇总（抢票选区域必须）

```
GET /api/ticket/shows/{showEventId}/zones
```

**响应示例**

```json
{
  "code": 200,
  "data": [
    {
      "zone": "VIP",
      "zoneName": "VIP区",
      "description": "舞台最近区域，视野最佳",
      "price": 1580,
      "totalSeats": 200,
      "availableSeats": 12,
      "soldRate": 94,
      "seatMapImageUrl": "https://cdn.example.com/seatmap/1_VIP.png"
    },
    {
      "zone": "A",
      "zoneName": "A区",
      "description": "黄金视野区",
      "price": 880,
      "totalSeats": 800,
      "availableSeats": 156,
      "soldRate": 80,
      "seatMapImageUrl": "https://cdn.example.com/seatmap/1_A.png"
    },
    {
      "zone": "B",
      "zoneName": "B区",
      "description": "舞台正前方",
      "price": 580,
      "totalSeats": 1200,
      "availableSeats": 430,
      "soldRate": 64,
      "seatMapImageUrl": null
    },
    {
      "zone": "C",
      "zoneName": "C区",
      "description": "性价比之选",
      "price": 280,
      "totalSeats": 2000,
      "availableSeats": 890,
      "soldRate": 55,
      "seatMapImageUrl": null
    }
  ]
}
```

**业务说明**

- `soldRate`：售出率，前端用进度条展示抢购紧迫感
- `availableSeats < 50` 时前端显示"仅剩 XX 张"红色警示
- `seatMapImageUrl`：区域座位平面图，VIP/A 区通常有，B/C 区可选
- 从 `tb_seat` 按 `(show_event_id, seat_zone)` 分组聚合查询，缓存 5 分钟

---

## 模块二：观演人管理

**Base Path**：`/api/user/viewers`
**认证**：所有接口需要 JWT

大麦网"常用观演人"功能核心，用户提前录入身份信息，购票时一键填入，不需要每次手动输入身份证。

---

### 2.1 获取常用观演人列表

```
GET /api/user/viewers
```

**响应示例**

```json
{
  "code": 200,
  "data": [
    {
      "id": 1,
      "name": "张三",
      "idCardMasked": "1101**********1234",
      "phone": "138****8000",
      "isDefault": true,
      "isVerified": true,
      "createdAt": "2026-01-15 10:00:00"
    },
    {
      "id": 2,
      "name": "李四",
      "idCardMasked": "3101**********5678",
      "phone": "139****9001",
      "isDefault": false,
      "isVerified": false,
      "createdAt": "2026-02-01 14:30:00"
    }
  ]
}
```

**业务说明**

- 每个用户最多保存 **10 个**观演人（`tb_viewer` 表加 user_id+count 限制）
- `idCardMasked` / `phone` 脱敏展示，真实数据加密存储（AES-256）
- `isVerified`：是否通过实名核验（对接公安接口）

---

### 2.2 添加观演人

```
POST /api/user/viewers
```

**Request Body**

```json
{
  "name": "王五",
  "idCard": "440101199503151234",
  "phone": "13900139000",
  "isDefault": false
}
```

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| `name` | String | ✅ | 2-50 字符 |
| `idCard` | String | ✅ | 18 位，格式正则校验 |
| `phone` | String | ✅ | 11 位手机号 |
| `isDefault` | Boolean | 否 | 默认 `false` |

**响应示例**

```json
{
  "code": 200,
  "message": "观演人添加成功",
  "data": {
    "id": 3,
    "name": "王五",
    "idCardMasked": "4401**********1234",
    "phone": "139****9000",
    "isDefault": false,
    "isVerified": false
  }
}
```

**业务说明**

- 同一用户不允许添加相同身份证号的观演人（唯一索引 `uk_user_idcard`）
- `isDefault = true` 时将其他观演人的 `isDefault` 置为 `false`
- 身份证号 AES 加密后存储，只在校验时解密

---

### 2.3 更新观演人信息

```
PUT /api/user/viewers/{viewerId}
```

**Request Body**（同添加，所有字段均可选）

```json
{
  "phone": "13700137000",
  "isDefault": true
}
```

**业务说明**

- 只允许修改 `phone` 和 `isDefault`
- `name` 和 `idCard` 涉及实名制，不允许直接修改，需走"删除后重新添加"流程

---

### 2.4 删除观演人

```
DELETE /api/user/viewers/{viewerId}
```

**业务说明**

- 软删除（`is_deleted = 1`）
- 若删除的是默认观演人，自动将最新一条设为默认

---

### 2.5 设置默认观演人

```
PUT /api/user/viewers/{viewerId}/default
```

**响应示例**

```json
{
  "code": 200,
  "message": "已设为默认观演人"
}
```

---

## 模块三：开票提醒与想看

**Base Path**：`/api/ticket/shows`
**认证**：所有接口需要 JWT

---

### 3.1 设置 / 取消开票提醒

```
POST   /api/ticket/shows/{showEventId}/remind   # 设置提醒
DELETE /api/ticket/shows/{showEventId}/remind   # 取消提醒
```

**POST 响应示例**

```json
{
  "code": 200,
  "message": "开票提醒设置成功，开票前1小时将通过消息通知提醒您",
  "data": {
    "showEventId": 1,
    "showName": "周杰伦魔天伦演唱会",
    "saleStartTime": "2026-03-01 10:00:00",
    "remindTime": "2026-03-01 09:00:00",
    "remindCount": 12580
  }
}
```

**业务说明**

- 同一演出同一用户只能设置一次（幂等，重复设置返回成功）
- 提醒节点：**开票前 1 天 / 开票前 1 小时 / 开票时**（三条通知）
- 通过 Kafka `ShowEventReminderEvent` 触发，复用现有 `OrderTimeoutHandler` 延迟队列机制
- `remindCount`：该演出的总提醒人数，用于展示"已有 XX 人设置提醒"激励文案

---

### 3.2 查询提醒状态

```
GET /api/ticket/shows/{showEventId}/remind
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "isReminded": true,
    "remindTime": "2026-03-01 09:00:00",
    "remindCount": 12580
  }
}
```

---

### 3.3 我设置提醒的演出

```
GET /api/ticket/shows/reminded?page=1&size=20
```

**响应**：同演出列表结构，额外字段：

```json
{
  "saleStartTime": "2026-03-01 10:00:00",
  "remindStatus": "PENDING"
}
```

`remindStatus`：`PENDING`(待提醒) / `SENT`(已提醒) / `EXPIRED`(已过期)

---

### 3.4 想看（收藏演出）

```
POST   /api/ticket/shows/{showEventId}/wish   # 添加想看
DELETE /api/ticket/shows/{showEventId}/wish   # 取消想看
```

**POST 响应示例**

```json
{
  "code": 200,
  "message": "已加入想看",
  "data": {
    "showEventId": 1,
    "wishCount": 45820
  }
}
```

**业务说明**

- 幂等操作，重复想看返回成功
- `wishCount` 展示在演出详情页激励购买
- 想看数纳入 `hotScore` 计算权重

---

### 3.5 我的想看列表

```
GET /api/ticket/shows/wish-list
  ?status=ALL    # ALL / SELLING / UPCOMING / ENDED
  &page=1&size=20
```

**响应**：同演出列表结构，追加字段：

```json
{
  "wishTime": "2026-01-20 15:30:00",
  "isReminded": true
}
```

---

## 模块四：退票与退款

**Base Path**：`/api/ticket`
**认证**：所有接口需要 JWT

---

### 4.1 查询退票政策

```
GET /api/ticket/shows/{showEventId}/refund-policy
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "showEventId": 1,
    "showName": "周杰伦魔天伦演唱会",
    "showTime": "2026-05-01 19:30:00",
    "supportRefund": true,
    "rules": [
      {
        "daysBeforeShow": 7,
        "condition": "演出前7天（含）以上",
        "feeRate": 0,
        "feeDesc": "免费退票，全额退款"
      },
      {
        "daysBeforeShow": 3,
        "condition": "演出前3-7天",
        "feeRate": 0.1,
        "feeDesc": "扣除票面价10%手续费"
      },
      {
        "daysBeforeShow": 1,
        "condition": "演出前1-3天",
        "feeRate": 0.3,
        "feeDesc": "扣除票面价30%手续费"
      },
      {
        "daysBeforeShow": 0,
        "condition": "演出当天及演出后",
        "feeRate": 1.0,
        "feeDesc": "不支持退票"
      }
    ],
    "note": "退款将在3-5个工作日内原路退回"
  }
}
```

---

### 4.2 申请退票

```
POST /api/ticket/refund
```

**Request Body**

```json
{
  "orderId": 100023,
  "reason": "PERSONAL_REASON",
  "remark": "临时有急事，无法出席"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `orderId` | Long | ✅ | 订单 ID |
| `reason` | String | ✅ | `PERSONAL_REASON`(个人原因) / `EVENT_CANCELLED`(演出取消) / `FORCE_MAJEURE`(不可抗力) / `OTHER` |
| `remark` | String | 否 | 退票备注，最多 200 字 |

**响应示例**

```json
{
  "code": 200,
  "message": "退票申请提交成功",
  "data": {
    "refundId": "RF20260225001234",
    "orderId": 100023,
    "orderNo": "TK2026022500001",
    "originalAmount": 880.00,
    "handlingFee": 0.00,
    "refundAmount": 880.00,
    "refundStatus": "PENDING",
    "estimatedRefundTime": "3-5个工作日内退回原支付渠道",
    "applyTime": "2026-02-25 14:30:00"
  }
}
```

**业务说明**

- 仅 `PAID` 状态订单可申请退票
- 演出当天 00:00 后不支持退票
- 退款金额 = 票面价 × (1 - 手续费率)
- 调用第三方支付退款接口（复用现有 `paymentService.refund()`）
- 退票成功后订单状态流转：`PAID` → `REFUNDING` → `REFUNDED`
- 座位释放：`SOLD` → `AVAILABLE`（更新 Redis 缓存，发 Kafka 消息）

---

### 4.3 查询退票进度

```
GET /api/ticket/refund/{orderId}
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "refundId": "RF20260225001234",
    "orderId": 100023,
    "orderNo": "TK2026022500001",
    "showName": "周杰伦魔天伦演唱会",
    "originalAmount": 880.00,
    "refundAmount": 880.00,
    "handlingFee": 0.00,
    "refundStatus": "REFUNDED",
    "applyTime": "2026-02-25 14:30:00",
    "refundTime": "2026-02-27 10:15:00",
    "payType": "ALIPAY",
    "transactionId": "2026022522001234560012345678",
    "timeline": [
      { "time": "2026-02-25 14:30:00", "status": "PENDING",   "desc": "退票申请已提交" },
      { "time": "2026-02-25 14:31:00", "status": "APPROVED",  "desc": "审核通过，正在处理退款" },
      { "time": "2026-02-27 10:15:00", "status": "REFUNDED",  "desc": "退款成功，已退至支付宝账户" }
    ]
  }
}
```

---

### 4.4 我的退票记录

```
GET /api/ticket/refunds?page=1&size=20
```

**响应**：同退票进度列表结构（精简版，不含 `timeline`）

---

## 模块五：场馆信息

**Base Path**：`/api/ticket/venues`

---

### 5.1 场馆列表

```
GET /api/ticket/venues?city=上海&page=1&size=20
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1,
        "name": "上海梅赛德斯-奔驰文化中心",
        "city": "上海",
        "address": "上海市浦东新区世博大道1200号",
        "capacity": 18000,
        "logoUrl": "https://cdn.example.com/venue/1.jpg",
        "upcomingShowCount": 3
      }
    ],
    "total": 12,
    "current": 1,
    "size": 20
  }
}
```

---

### 5.2 场馆详情

```
GET /api/ticket/venues/{venueId}
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "上海梅赛德斯-奔驰文化中心",
    "city": "上海",
    "district": "浦东新区",
    "address": "上海市浦东新区世博大道1200号",
    "capacity": 18000,
    "logoUrl": "https://cdn.example.com/venue/1/logo.jpg",
    "panoramaUrl": "https://cdn.example.com/venue/1/panorama.jpg",
    "seatMapUrl": "https://cdn.example.com/venue/1/seatmap.svg",
    "zones": [
      {
        "zone": "VIP",
        "zoneName": "VIP区",
        "description": "舞台最近区域，视野极佳",
        "capacity": 200,
        "seatMapUrl": "https://cdn.example.com/venue/1/zone_vip.png"
      },
      {
        "zone": "A",
        "zoneName": "A区",
        "description": "黄金视野，强烈推荐",
        "capacity": 800,
        "seatMapUrl": "https://cdn.example.com/venue/1/zone_a.png"
      }
    ],
    "transport": {
      "subway": [
        "地铁7号线 耀华路站 4号出口步行约12分钟",
        "地铁8号线 中华艺术宫站 2号出口步行约8分钟"
      ],
      "bus": ["584路", "985路", "龙耀路世博轴站"],
      "parking": [
        { "name": "P1停车场", "address": "世博大道南侧", "capacity": 500 },
        { "name": "P2停车场", "address": "南码头路东侧", "capacity": 300 }
      ],
      "tips": "演出当天建议提前2小时到场，入场高峰期等待时间约30分钟"
    },
    "amenities": ["ATM", "餐饮区", "卫生间", "母婴室", "无障碍通道"],
    "upcomingShows": [
      { "id": 1, "showName": "周杰伦演唱会", "showTime": "2026-05-01 19:30:00", "status": "SELLING" }
    ]
  }
}
```

**业务说明**

- `seatMapUrl` 使用 SVG 格式，支持前端按区域高亮显示
- `upcomingShows`：该场馆未来 90 天内的演出，最多返回 5 条
- 结果缓存 Redis，TTL 24 小时（场馆信息变化频率低）

---

### 5.3 场馆下的演出列表

```
GET /api/ticket/venues/{venueId}/shows?status=SELLING&page=1&size=20
```

**响应**：同演出列表结构

---

## 模块六：消息通知中心

**Base Path**：`/api/notifications`
**认证**：所有接口需要 JWT

复用现有 `Notification` 实体，通过 Kafka 消费各类事件写入通知表。

---

### 6.1 通知列表

```
GET /api/notifications
  ?type=ALL     # ALL / TICKET(抢票) / ORDER(订单) / REFUND(退款) / REMIND(开票提醒) / SYSTEM(系统)
  &read=false   # 未读筛选，不传则全部
  &page=1&size=20
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "records": [
      {
        "id": 1001,
        "type": "REMIND",
        "title": "开票提醒",
        "content": "【周杰伦魔天伦演唱会】将于 10分钟后（10:00）正式开票，请做好抢票准备！",
        "isRead": false,
        "relatedId": 1,
        "relatedType": "SHOW_EVENT",
        "actionUrl": "/shows/1",
        "createdAt": "2026-03-01 09:50:00"
      },
      {
        "id": 1002,
        "type": "ORDER",
        "title": "支付成功",
        "content": "您的订单【TK2026022500001】支付成功，金额 ¥880.00，请保存好您的电子票。",
        "isRead": true,
        "relatedId": 100023,
        "relatedType": "ORDER",
        "actionUrl": "/orders/100023",
        "createdAt": "2026-02-25 14:31:00"
      },
      {
        "id": 1003,
        "type": "REFUND",
        "title": "退款到账",
        "content": "您申请退票的订单【TK2026022400002】退款 ¥580.00 已成功退回您的微信账户。",
        "isRead": false,
        "relatedId": 100024,
        "relatedType": "ORDER",
        "actionUrl": "/orders/100024",
        "createdAt": "2026-02-27 10:15:00"
      }
    ],
    "total": 28,
    "unreadCount": 3,
    "current": 1,
    "size": 20
  }
}
```

**通知类型说明**

| type | 触发场景 | Kafka Topic |
|------|---------|------------|
| `REMIND` | 开票前1天/1小时/开票时 | `show.event.reminder` |
| `ORDER` | 订单创建/支付成功/超时取消 | `order.create` / `order.paid` |
| `REFUND` | 退款申请/退款成功/退款失败 | `order.refund` |
| `TICKET` | 抢票失败/座位分配结果 | `ticket.grab.result` |
| `SYSTEM` | 系统公告/活动通知 | — |

---

### 6.2 未读数量（用于首页小红点）

```
GET /api/notifications/unread-count
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "total": 5,
    "byType": {
      "REMIND": 2,
      "ORDER": 1,
      "REFUND": 1,
      "SYSTEM": 1
    }
  }
}
```

**业务说明**

- 建议客户端每 30 秒轮询一次，或接入 WebSocket 推送
- 结果缓存 Redis（`notification:unread:{userId}`），收到新通知时主动 `INCR`

---

### 6.3 标记单条已读

```
PUT /api/notifications/{notificationId}/read
```

---

### 6.4 全部标记已读

```
PUT /api/notifications/read-all?type=REMIND
```

| 参数 | 说明 |
|------|------|
| `type` | 可选，不传则全部类型已读 |

---

### 6.5 删除通知

```
DELETE /api/notifications/{notificationId}
DELETE /api/notifications/batch?ids=1,2,3
```

---

## 模块七：演出评价

**Base Path**：`/api/ticket/shows/{showEventId}/reviews`

---

### 7.1 演出评价列表

```
GET /api/ticket/shows/{showEventId}/reviews
  ?sort=latest   # latest(最新) / highest(最高分) / lowest(最低分) / helpful(最有用)
  &rating=5      # 筛选指定星级，不传则全部
  &page=1&size=20
```

**响应示例**

```json
{
  "code": 200,
  "data": {
    "summary": {
      "avgRating": 4.7,
      "totalCount": 1258,
      "ratingDistribution": {
        "5": 856,
        "4": 287,
        "3": 89,
        "2": 18,
        "1": 8
      },
      "hotTags": ["音效震撼", "视野棒", "互动性强", "值得回购", "现场氛围好"]
    },
    "records": [
      {
        "id": 5001,
        "userId": 1001,
        "userName": "张**",
        "avatarUrl": "https://cdn.example.com/avatar/1001.jpg",
        "rating": 5,
        "content": "现场效果太震撼了！JJ 的声音一如既往完美，灯光舞台设计非常用心。",
        "tags": ["音效震撼", "视野棒"],
        "images": ["https://cdn.example.com/review/5001_1.jpg"],
        "seatInfo": "A区 12排 28座",
        "isVerifiedPurchase": true,
        "likeCount": 234,
        "isLiked": false,
        "createdAt": "2026-05-02 22:15:00"
      }
    ],
    "total": 1258,
    "current": 1,
    "size": 20
  }
}
```

---

### 7.2 发表演出评价

```
POST /api/ticket/shows/{showEventId}/reviews
```

**认证**：需要 JWT

**Request Body**

```json
{
  "orderId": 100023,
  "rating": 5,
  "content": "现场效果超棒，值得回购！",
  "tags": ["音效震撼", "视野棒", "值得回购"],
  "images": [
    "https://cdn.example.com/uploads/img001.jpg",
    "https://cdn.example.com/uploads/img002.jpg"
  ]
}
```

| 字段 | 类型 | 必填 | 校验规则 |
|------|------|------|---------|
| `orderId` | Long | ✅ | 关联已支付订单，防刷评 |
| `rating` | Integer | ✅ | 1-5 整数 |
| `content` | String | 否 | 10-500 字符 |
| `tags` | List | 否 | 最多 3 个标签，预设标签列表 |
| `images` | List | 否 | 最多 6 张，复用文件上传接口 |

**响应示例**

```json
{
  "code": 200,
  "message": "评价发表成功",
  "data": {
    "id": 5002,
    "rating": 5,
    "content": "现场效果超棒，值得回购！",
    "isVerifiedPurchase": true,
    "createdAt": "2026-05-02 22:30:00"
  }
}
```

**业务说明**

- **购票验证**：`orderId` 必须属于当前用户且状态为 `PAID`，且 `showEventId` 匹配
- **演出后才能评价**：`showTime` 已过才开放评价（演出前不能评）
- **一单一评价**：同一订单只能评价一次（`uk_order_review` 唯一索引）
- `seatInfo`：从 `tb_order_seat` 关联查询，自动填入，不需要用户填写

---

### 7.3 我的评价列表

```
GET /api/ticket/my-reviews?page=1&size=20
```

---

### 7.4 评价点赞

```
POST /api/ticket/reviews/{reviewId}/like
DELETE /api/ticket/reviews/{reviewId}/like
```

**业务说明**

- 复用现有 `Like` 实体，`targetType = REVIEW`
- 每个用户对同一评价只能点赞一次

---

### 7.5 获取评价标签列表

```
GET /api/ticket/shows/{showEventId}/review-tags
```

**响应示例**

```json
{
  "code": 200,
  "data": [
    { "tag": "音效震撼",  "count": 856 },
    { "tag": "视野棒",    "count": 634 },
    { "tag": "互动性强",  "count": 421 },
    { "tag": "值得回购",  "count": 398 },
    { "tag": "现场氛围好","count": 312 },
    { "tag": "等候时间长","count": 89  },
    { "tag": "停车不便",  "count": 54  }
  ]
}
```

---

## 新增 SQL 表结构

```sql
-- ==================== 观演人表 ====================
CREATE TABLE tb_viewer (
    id          BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id     BIGINT      NOT NULL                COMMENT '用户ID',
    name        VARCHAR(50) NOT NULL                COMMENT '真实姓名',
    id_card     VARCHAR(255) NOT NULL               COMMENT '身份证号（AES加密存储）',
    phone       VARCHAR(20)                         COMMENT '手机号',
    is_default  TINYINT(1)  NOT NULL DEFAULT 0      COMMENT '是否默认观演人',
    is_verified TINYINT(1)  NOT NULL DEFAULT 0      COMMENT '是否实名核验通过',
    is_deleted  TINYINT(1)  NOT NULL DEFAULT 0      COMMENT '软删除标志',
    created_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_idcard (user_id, id_card),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='常用观演人表';


-- ==================== 场馆表 ====================
CREATE TABLE tb_venue (
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '场馆ID',
    name              VARCHAR(200) NOT NULL            COMMENT '场馆名称',
    city              VARCHAR(50)  NOT NULL            COMMENT '城市',
    district          VARCHAR(100)                    COMMENT '行政区',
    address           VARCHAR(500) NOT NULL            COMMENT '详细地址',
    capacity          INT          NOT NULL DEFAULT 0  COMMENT '总容量',
    logo_url          VARCHAR(500)                    COMMENT 'Logo图片URL',
    panorama_url      VARCHAR(500)                    COMMENT '全景图URL',
    seat_map_url      VARCHAR(500)                    COMMENT '座位图URL（SVG）',
    transport_info    JSON                            COMMENT '交通信息（subway/bus/parking/tips）',
    amenities         JSON                            COMMENT '设施列表（JSON Array）',
    created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_city (city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场馆信息表';


-- ==================== 开票提醒表 ====================
CREATE TABLE tb_show_remind (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id       BIGINT   NOT NULL COMMENT '用户ID',
    show_event_id BIGINT   NOT NULL COMMENT '演出活动ID',
    remind_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                             COMMENT '提醒状态：PENDING/SENT/EXPIRED',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_show (user_id, show_event_id),
    INDEX idx_show_event_id (show_event_id),
    INDEX idx_remind_status (remind_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开票提醒表';


-- ==================== 想看（收藏）表 ====================
CREATE TABLE tb_show_wish (
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id       BIGINT   NOT NULL COMMENT '用户ID',
    show_event_id BIGINT   NOT NULL COMMENT '演出活动ID',
    created_at    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_show (user_id, show_event_id),
    INDEX idx_show_event_id (show_event_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='演出想看（收藏）表';


-- ==================== 退票申请表 ====================
CREATE TABLE tb_refund_record (
    id             BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '退票ID',
    refund_no      VARCHAR(50)  NOT NULL UNIQUE          COMMENT '退票编号',
    order_id       BIGINT       NOT NULL                 COMMENT '订单ID',
    order_no       VARCHAR(50)  NOT NULL                 COMMENT '订单编号',
    user_id        BIGINT       NOT NULL                 COMMENT '用户ID',
    show_event_id  BIGINT       NOT NULL                 COMMENT '演出活动ID',
    original_amount DECIMAL(10,2) NOT NULL               COMMENT '订单原始金额',
    handling_fee   DECIMAL(10,2) NOT NULL DEFAULT 0      COMMENT '手续费',
    refund_amount  DECIMAL(10,2) NOT NULL                COMMENT '实际退款金额',
    reason         VARCHAR(50)  NOT NULL                 COMMENT '退票原因枚举',
    remark         VARCHAR(200)                          COMMENT '备注',
    refund_status  VARCHAR(20)  NOT NULL DEFAULT 'PENDING'
                                COMMENT '退款状态：PENDING/APPROVED/REFUNDED/REJECTED',
    pay_type       VARCHAR(20)                           COMMENT '原支付方式',
    transaction_id VARCHAR(100)                          COMMENT '原支付流水号（退款用）',
    refund_txn_id  VARCHAR(100)                          COMMENT '退款流水号',
    refund_time    DATETIME                              COMMENT '退款到账时间',
    apply_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id),
    INDEX idx_refund_status (refund_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='退票退款记录表';


-- ==================== 演出评价表 ====================
CREATE TABLE tb_show_review (
    id               BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    show_event_id    BIGINT       NOT NULL                   COMMENT '演出活动ID',
    user_id          BIGINT       NOT NULL                   COMMENT '用户ID',
    order_id         BIGINT       NOT NULL                   COMMENT '关联订单ID（防刷评）',
    rating           TINYINT      NOT NULL                   COMMENT '评分 1-5',
    content          VARCHAR(500)                            COMMENT '评价内容',
    tags             JSON                                    COMMENT '评价标签（JSON Array）',
    images           JSON                                    COMMENT '评价图片URL列表',
    seat_info        VARCHAR(100)                            COMMENT '座位信息（自动填入）',
    like_count       INT          NOT NULL DEFAULT 0         COMMENT '点赞数',
    is_verified_purchase TINYINT(1) NOT NULL DEFAULT 1       COMMENT '是否已核验购票',
    is_deleted       TINYINT(1)   NOT NULL DEFAULT 0         COMMENT '软删除',
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_review (order_id),
    INDEX idx_show_event_id (show_event_id),
    INDEX idx_user_id (user_id),
    INDEX idx_rating (show_event_id, rating),
    INDEX idx_created_at (show_event_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='演出评价表';


-- ==================== tb_show_event 新增字段 ====================
ALTER TABLE tb_show_event
    ADD COLUMN city       VARCHAR(50)  COMMENT '城市'     AFTER venue_name,
    ADD COLUMN category   VARCHAR(50)  COMMENT '演出分类' AFTER show_type,
    ADD COLUMN wish_count INT NOT NULL DEFAULT 0 COMMENT '想看人数（冗余）' AFTER sold_seats,
    ADD COLUMN hot_score  INT NOT NULL DEFAULT 0 COMMENT '热度分'          AFTER wish_count,
    ADD INDEX idx_city_category (city, category),
    ADD INDEX idx_hot_score (hot_score);


-- ==================== tb_ticket_order 新增字段 ====================
ALTER TABLE tb_ticket_order
    ADD COLUMN seat_zone VARCHAR(50) COMMENT '座位区域（快速抢票记录区域）' AFTER seat_count,
    ADD COLUMN refund_id BIGINT      COMMENT '关联退票记录ID'               AFTER qr_code_url;

-- 扩展 status 枚举说明（注释变更，代码中 OrderStatus 类同步新增）:
-- 新增: REFUNDING（退款中）/ REFUNDED（已退款）/ USED（已使用/入场）
```

---

## 接口汇总速查表

| 模块 | HTTP方法 | 路径 | 认证 | 优先级 |
|------|---------|------|------|--------|
| **演出搜索** | GET | `/api/ticket/shows/search` | 否 | P0 |
| | GET | `/api/ticket/shows/cities` | 否 | P0 |
| | GET | `/api/ticket/shows/categories` | 否 | P0 |
| | GET | `/api/ticket/shows/homepage` | 否 | P0 |
| | GET | `/api/ticket/shows/{id}/zones` | 否 | P0 |
| **观演人管理** | GET | `/api/user/viewers` | ✅ | P0 |
| | POST | `/api/user/viewers` | ✅ | P0 |
| | PUT | `/api/user/viewers/{id}` | ✅ | P0 |
| | DELETE | `/api/user/viewers/{id}` | ✅ | P0 |
| | PUT | `/api/user/viewers/{id}/default` | ✅ | P0 |
| **开票提醒** | POST | `/api/ticket/shows/{id}/remind` | ✅ | P1 |
| | DELETE | `/api/ticket/shows/{id}/remind` | ✅ | P1 |
| | GET | `/api/ticket/shows/{id}/remind` | ✅ | P1 |
| | GET | `/api/ticket/shows/reminded` | ✅ | P1 |
| **想看收藏** | POST | `/api/ticket/shows/{id}/wish` | ✅ | P1 |
| | DELETE | `/api/ticket/shows/{id}/wish` | ✅ | P1 |
| | GET | `/api/ticket/shows/wish-list` | ✅ | P1 |
| **退票退款** | GET | `/api/ticket/shows/{id}/refund-policy` | 否 | P0 |
| | POST | `/api/ticket/refund` | ✅ | P0 |
| | GET | `/api/ticket/refund/{orderId}` | ✅ | P0 |
| | GET | `/api/ticket/refunds` | ✅ | P1 |
| **场馆信息** | GET | `/api/ticket/venues` | 否 | P2 |
| | GET | `/api/ticket/venues/{id}` | 否 | P2 |
| | GET | `/api/ticket/venues/{id}/shows` | 否 | P2 |
| **消息通知** | GET | `/api/notifications` | ✅ | P1 |
| | GET | `/api/notifications/unread-count` | ✅ | P1 |
| | PUT | `/api/notifications/{id}/read` | ✅ | P1 |
| | PUT | `/api/notifications/read-all` | ✅ | P1 |
| | DELETE | `/api/notifications/{id}` | ✅ | P1 |
| **演出评价** | GET | `/api/ticket/shows/{id}/reviews` | 否 | P2 |
| | POST | `/api/ticket/shows/{id}/reviews` | ✅ | P2 |
| | GET | `/api/ticket/my-reviews` | ✅ | P2 |
| | POST | `/api/ticket/reviews/{id}/like` | ✅ | P2 |
| | GET | `/api/ticket/shows/{id}/review-tags` | 否 | P2 |
