# 演唱会抢票系统 - 完整设计方案与API接口文档

> 🎯 **设计目标**：参考大麦网功能，打造高性能、高可用、用户体验优秀的抢票平台
>
> 📅 **更新日期**：2026-02-25
>
> 🏗️ **架构特点**：高并发、防超卖、分布式锁、缓存预热、异步处理

---

## 📚 目录

- [系统架构设计](#系统架构设计)
- [功能模块设计](#功能模块设计)
- [接口概览](#接口概览)
- [详细接口文档](#详细接口文档)
- [高并发优化方案](#高并发优化方案)
- [数据库设计](#数据库设计)

---

## 🏛️ 系统架构设计

### 技术架构

```
前端层（Web/App/小程序）
    ↓
API 网关层（Nginx + 限流 + 鉴权）
    ↓
应用服务层（Spring Boot 集群）
    ↓
中间件层（Redis + Kafka + ElasticSearch）
    ↓
数据存储层（MySQL 主从 + Redis 集群）
```

### 核心技术选型

- **框架**：Spring Boot 3.x + MyBatis Plus
- **缓存**：Redis 7.x（座位缓存、热点数据、分布式锁）
- **消息队列**：Kafka（异步订单处理、通知推送）
- **搜索引擎**：ElasticSearch（演出搜索、智能推荐）
- **分布式锁**：Redisson（防止超卖、防重复提交）
- **数据库**：MySQL 8.x（主从复制、读写分离）
- **监控**：Prometheus + Grafana

### 高并发解决方案

1. **接入层**：Nginx 负载均衡 + CDN 加速静态资源
2. **应用层**：服务无状态化 + 水平扩展
3. **缓存层**：多级缓存（本地缓存 + Redis 集群）
4. **数据层**：读写分离 + 分库分表
5. **限流降级**：令牌桶 + 熔断器

---

## 🎨 功能模块设计

### 1. 用户模块（UserController）
- ✅ 用户注册/登录（手机号、邮箱、第三方登录）
- ✅ 实名认证（姓名、身份证、人脸识别）
- ✅ 个人信息管理
- ✅ 收货地址管理
- ✅ 常用观演人管理
- ✅ 会员体系（等级、积分、权益）

### 2. 演出模块（ShowEventController）
- ✅ 演出搜索（ES 全文搜索、智能推荐）
- ✅ 演出分类（演唱会、话剧、电影、体育赛事）
- ✅ 演出详情（基本信息、场次、价格）
- ✅ 城市/场馆筛选
- ✅ 演出日历
- ✅ 热门推荐/即将开售

### 3. 座位模块（SeatController）
- ✅ 交互式座位图
- ✅ 区域座位查询
- ✅ 智能选座（连座推荐）
- ✅ 座位实时状态
- ✅ 价格区间筛选

### 4. 抢票模块（TicketController）
- ✅ 抢票下单（分布式锁防超卖）
- ✅ 购物车（暂存多个订单）
- ✅ 快速抢票（自动分配座位）
- ✅ 排队系统（虚拟队列）

### 5. 订单模块（OrderController）
- ✅ 订单确认
- ✅ 订单支付（支付宝、微信、银行卡）
- ✅ 订单查询（状态、详情）
- ✅ 订单退款/退票
- ✅ 订单超时处理（延迟队列）

### 6. 票务模块（TicketManageController）
- ✅ 电子票查看
- ✅ 电子票二维码
- ✅ 实体票配送
- ✅ 取票/换票
- ✅ 转赠/转让

### 7. 消息通知模块（NotificationController）
- ✅ 开售提醒
- ✅ 抢票通知
- ✅ 订单通知
- ✅ 物流通知
- ✅ 站内信/推送/短信/邮件

### 8. 收藏关注模块（FavoriteController）
- ✅ 演出收藏
- ✅ 艺人关注
- ✅ 场馆关注
- ✅ 我的收藏列表

### 9. 评价模块（ReviewController）
- ✅ 演出评价
- ✅ 评价点赞
- ✅ 热门评价
- ✅ 评价审核

### 10. 风控模块（RiskController）
- ✅ 验证码（滑块、图片）
- ✅ 行为分析（频率限制）
- ✅ 黑名单管理
- ✅ 防刷机制

### 11. 客服模块（CustomerServiceController）
- ✅ 在线客服
- ✅ 工单系统
- ✅ 常见问题
- ✅ 退款申请

---

## 📋 接口概览

### 1️⃣ 用户模块（UserController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/user/register` | POST | 用户注册 | ❌ |
| `/api/user/login` | POST | 用户登录 | ❌ |
| `/api/user/logout` | POST | 用户登出 | ✅ |
| `/api/user/profile` | GET | 获取个人信息 | ✅ |
| `/api/user/profile` | PUT | 更新个人信息 | ✅ |
| `/api/user/verify/real-name` | POST | 实名认证 | ✅ |
| `/api/user/addresses` | GET | 获取收货地址列表 | ✅ |
| `/api/user/addresses` | POST | 添加收货地址 | ✅ |
| `/api/user/addresses/{id}` | PUT | 更新收货地址 | ✅ |
| `/api/user/addresses/{id}` | DELETE | 删除收货地址 | ✅ |
| `/api/user/contacts` | GET | 获取常用观演人 | ✅ |
| `/api/user/contacts` | POST | 添加常用观演人 | ✅ |
| `/api/user/membership` | GET | 获取会员信息 | ✅ |
| `/api/user/points/history` | GET | 积分明细 | ✅ |

### 2️⃣ 演出活动模块（ShowEventController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/ticket/shows` | GET | 查询演出活动列表 | ❌ |
| `/api/ticket/shows/search` | GET | 搜索演出（ES） | ❌ |
| `/api/ticket/shows/hot` | GET | 热门演出推荐 | ❌ |
| `/api/ticket/shows/coming-soon` | GET | 即将开售演出 | ❌ |
| `/api/ticket/shows/categories` | GET | 演出分类列表 | ❌ |
| `/api/ticket/shows/{id}` | GET | 查询演出活动详情 | ❌ |
| `/api/ticket/shows/{id}/sessions` | GET | 查询演出场次 | ❌ |
| `/api/ticket/shows/{showEventId}/seats` | GET | 查询座位列表 | ❌ |
| `/api/ticket/shows/{showEventId}/seats/available-count` | GET | 查询可售座位数量 | ❌ |
| `/api/ticket/shows/{showEventId}/seat-map` | GET | 获取座位图 | ❌ |
| `/api/ticket/cities` | GET | 获取城市列表 | ❌ |
| `/api/ticket/venues` | GET | 获取场馆列表 | ❌ |

### 3️⃣ 抢票模块（TicketController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/ticket/grab` | POST | 抢票（核心接口） | ✅ |
| `/api/ticket/grab/quick` | POST | 快速抢票（自动选座） | ✅ |
| `/api/ticket/cart/add` | POST | 添加到购物车 | ✅ |
| `/api/ticket/cart` | GET | 查看购物车 | ✅ |
| `/api/ticket/cart/{itemId}` | DELETE | 移除购物车项 | ✅ |
| `/api/ticket/queue/join` | POST | 加入排队 | ✅ |
| `/api/ticket/queue/status` | GET | 查询排队状态 | ✅ |

### 4️⃣ 订单模块（OrderController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/ticket/pay` | POST | 支付订单 | ✅ |
| `/api/ticket/pay/status/{orderId}` | GET | 查询支付状态 | ✅ |
| `/api/ticket/cancel/{orderId}` | POST | 取消订单 | ✅ |
| `/api/ticket/refund/{orderId}` | POST | 申请退款 | ✅ |
| `/api/ticket/order/{orderId}` | GET | 查询订单详情 | ✅ |
| `/api/ticket/orders` | GET | 查询我的订单列表 | ✅ |
| `/api/ticket/orders/statistics` | GET | 订单统计 | ✅ |

### 5️⃣ 票务管理模块（TicketManageController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/ticket/my-tickets` | GET | 我的票夹 | ✅ |
| `/api/ticket/my-tickets/{ticketId}` | GET | 票务详情 | ✅ |
| `/api/ticket/my-tickets/{ticketId}/qrcode` | GET | 获取电子票二维码 | ✅ |
| `/api/ticket/my-tickets/{ticketId}/transfer` | POST | 转赠票务 | ✅ |
| `/api/ticket/delivery/{orderId}` | GET | 查询配送信息 | ✅ |

### 6️⃣ 消息通知模块（NotificationController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/notification/messages` | GET | 获取消息列表 | ✅ |
| `/api/notification/messages/{id}` | PUT | 标记已读 | ✅ |
| `/api/notification/messages/read-all` | PUT | 全部已读 | ✅ |
| `/api/notification/subscribe` | POST | 订阅开售提醒 | ✅ |
| `/api/notification/subscriptions` | GET | 我的订阅 | ✅ |
| `/api/notification/unsubscribe/{id}` | DELETE | 取消订阅 | ✅ |

### 7️⃣ 收藏关注模块（FavoriteController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/favorite/shows` | POST | 收藏演出 | ✅ |
| `/api/favorite/shows` | GET | 我的收藏 | ✅ |
| `/api/favorite/shows/{showId}` | DELETE | 取消收藏 | ✅ |
| `/api/favorite/artists` | POST | 关注艺人 | ✅ |
| `/api/favorite/artists` | GET | 关注的艺人 | ✅ |

### 8️⃣ 评价模块（ReviewController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/review/shows/{showId}` | GET | 获取演出评价 | ❌ |
| `/api/review/shows/{showId}` | POST | 发表评价 | ✅ |
| `/api/review/{reviewId}/like` | POST | 点赞评价 | ✅ |
| `/api/review/{reviewId}` | DELETE | 删除评价 | ✅ |

### 9️⃣ 风控模块（RiskController）

| 接口路径 | 方法 | 说明 | 认证 |
|---------|------|------|-----|
| `/api/risk/captcha` | GET | 获取验证码 | ❌ |
| `/api/risk/captcha/verify` | POST | 验证验证码 | ❌ |
| `/api/risk/check` | POST | 风控检查 | ✅ |

---

## 🔐 认证说明

需要认证的接口必须在请求头中携带 JWT token：

```
Authorization: Bearer {your_jwt_token}
```

**获取 Token**：调用登录接口后，在响应中获取 `token` 字段。

---

## 📡 详细接口文档

---

## 一、用户模块

### 1.1 用户注册

**接口**: `POST /api/user/register`

**请求体**:
```json
{
  "phone": "13800138000",
  "password": "password123",
  "verifyCode": "123456",
  "nickname": "张三"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "phone": "13800138000",
    "nickname": "张三"
  }
}
```

### 1.2 用户登录

**接口**: `POST /api/user/login`

**请求体**:
```json
{
  "phone": "13800138000",
  "password": "password123"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "phone": "13800138000",
    "nickname": "张三",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenExpireTime": "2026-02-26 14:15:00",
    "isRealNameVerified": true,
    "memberLevel": "VIP"
  }
}
```

### 1.3 实名认证

**接口**: `POST /api/user/verify/real-name`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "realName": "张三",
  "idCard": "110101199001011234"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "实名认证成功",
  "data": {
    "verified": true,
    "realName": "张三",
    "verifiedAt": "2026-02-25 14:15:00"
  }
}
```

### 1.4 常用观演人管理

**接口**: `GET /api/user/contacts`

**认证**: ✅ 需要 JWT token

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "张三",
      "phone": "13800138000",
      "idCard": "110101199001011234",
      "isDefault": true
    },
    {
      "id": 2,
      "name": "李四",
      "phone": "13900139000",
      "idCard": "110101199101011234",
      "isDefault": false
    }
  ]
}
```

**添加常用观演人**: `POST /api/user/contacts`

**请求体**:
```json
{
  "name": "王五",
  "phone": "13700137000",
  "idCard": "110101199201011234",
  "isDefault": false
}
```

### 1.5 会员信息

**接口**: `GET /api/user/membership`

**认证**: ✅ 需要 JWT token

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1001,
    "memberLevel": "VIP",
    "points": 2580,
    "benefits": [
      "优先购票权",
      "专属客服",
      "积分加倍",
      "免配送费"
    ],
    "nextLevelPoints": 5000,
    "validUntil": "2027-02-25"
  }
}
```

---

## 二、演出活动模块

### 2.1 查询演出活动列表

**接口**: `GET /api/ticket/shows`

**请求参数**:
```
page: 页码（可选，默认1）
size: 每页大小（可选，默认10）
status: 状态筛选（可选，PENDING/SELLING/SOLD_OUT/ENDED）
showType: 演出类型筛选（可选，CONCERT/MOVIE/DRAMA）
```

**示例请求**:
```bash
curl -X GET "http://localhost:8080/api/ticket/shows?page=1&size=10&status=SELLING"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "showName": "周杰伦2026世界巡回演唱会-北京站",
        "showType": "CONCERT",
        "venueName": "国家体育场（鸟巢）",
        "showTime": "2026-05-01 19:30:00",
        "saleStartTime": "2026-03-01 10:00:00",
        "saleEndTime": "2026-04-30 23:59:59",
        "totalSeats": 8000,
        "availableSeats": 7856,
        "soldSeats": 144,
        "maxBuyLimit": 2,
        "posterUrl": "https://example.com/posters/jay_concert.jpg",
        "description": "周杰伦2026世界巡回演唱会北京站，经典歌曲全新演绎...",
        "status": "SELLING",
        "minPrice": 380.00,
        "maxPrice": 1280.00
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 2. 查询演出活动详情

**接口**: `GET /api/ticket/shows/{id}`

**路径参数**:
- `id`: 演出活动ID

**示例请求**:
```bash
curl -X GET "http://localhost:8080/api/ticket/shows/1"
```

### 2.2 演出搜索（ElasticSearch）

**接口**: `GET /api/ticket/shows/search`

**请求参数**:
```
keyword: 搜索关键词（演出名、艺人名、场馆名）
city: 城市
category: 分类（CONCERT/MOVIE/DRAMA/SPORTS）
minPrice: 最低价格
maxPrice: 最高价格
startDate: 开始日期
endDate: 结束日期
page: 页码
size: 每页大小
```

**示例请求**:
```bash
curl -X GET "http://localhost:8080/api/ticket/shows/search?keyword=周杰伦&city=北京&page=1&size=10"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "showName": "周杰伦2026世界巡回演唱会-北京站",
        "artist": "周杰伦",
        "showType": "CONCERT",
        "city": "北京",
        "venueName": "国家体育场（鸟巢）",
        "showTime": "2026-05-01 19:30:00",
        "minPrice": 380.00,
        "maxPrice": 1280.00,
        "posterUrl": "https://example.com/posters/jay_concert.jpg",
        "status": "SELLING",
        "hotScore": 9.8
      }
    ],
    "total": 1,
    "took": 15
  }
}
```

### 2.3 热门演出推荐

**接口**: `GET /api/ticket/shows/hot`

**请求参数**:
```
city: 城市（可选）
category: 分类（可选）
limit: 数量（默认10）
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "showName": "周杰伦2026世界巡回演唱会-北京站",
      "posterUrl": "https://example.com/posters/jay_concert.jpg",
      "minPrice": 380.00,
      "saleStartTime": "2026-03-01 10:00:00",
      "status": "SELLING",
      "viewCount": 125000,
      "favoriteCount": 8900
    }
  ]
}
```

### 2.4 获取座位图

**接口**: `GET /api/ticket/shows/{showEventId}/seat-map`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "showEventId": 1,
    "venueName": "国家体育场（鸟巢）",
    "seatMapUrl": "https://example.com/seatmaps/venue_1.svg",
    "zones": [
      {
        "zone": "VIP",
        "zoneName": "VIP区",
        "price": 1280.00,
        "availableCount": 450,
        "totalCount": 500,
        "color": "#FFD700"
      },
      {
        "zone": "A",
        "zoneName": "A区",
        "price": 880.00,
        "availableCount": 1850,
        "totalCount": 2000,
        "color": "#FF6B6B"
      }
    ],
    "seats": [
      {
        "id": 1,
        "seatCode": "VIP-1-1",
        "zone": "VIP",
        "row": 1,
        "number": 1,
        "x": 100,
        "y": 50,
        "status": "AVAILABLE"
      }
    ]
  }
}
```

---

## 三、抢票模块

**接口**: `GET /api/ticket/shows/{showEventId}/seats`

**路径参数**:
- `showEventId`: 演出活动ID

**请求参数**:
```
page: 页码（可选，默认1）
size: 每页大小（可选，默认20）
seatZone: 座位区域筛选（可选，VIP/A/B/C）
status: 座位状态筛选（可选，AVAILABLE/LOCKED/SOLD）
```

**示例请求**:
```bash
# 查询VIP区的可售座位
curl -X GET "http://localhost:8080/api/ticket/shows/1/seats?seatZone=VIP&status=AVAILABLE&page=1&size=20"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "showEventId": 1,
        "seatZone": "VIP",
        "seatRow": 1,
        "seatNumber": 1,
        "seatCode": "VIP-1-1",
        "price": 1280.00,
        "status": "AVAILABLE"
      },
      {
        "id": 2,
        "showEventId": 1,
        "seatZone": "VIP",
        "seatRow": 1,
        "seatNumber": 2,
        "seatCode": "VIP-1-2",
        "price": 1280.00,
        "status": "AVAILABLE"
      }
    ],
    "total": 500,
    "size": 20,
    "current": 1,
    "pages": 25
  }
}
```

---

### 4. 查询可售座位数量

**接口**: `GET /api/ticket/shows/{showEventId}/seats/available-count`

**路径参数**:
- `showEventId`: 演出活动ID

**请求参数**:
```
seatZone: 座位区域（可选，不传则查询所有区域）
```

**示例请求**:
```bash
# 查询所有区域的可售座位数量
curl -X GET "http://localhost:8080/api/ticket/shows/1/seats/available-count"

# 查询VIP区的可售座位数量
curl -X GET "http://localhost:8080/api/ticket/shows/1/seats/available-count?seatZone=VIP"
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": 7856
}
```

---

### 3.1 抢票（核心接口）⚡

**接口**: `POST /api/ticket/grab`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "showEventId": 1,
  "seatIds": [1, 2],
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactIdCard": "110101199001011234"
}
```

**字段说明**:
- `showEventId`: 演出活动ID（必填）
- `seatIds`: 座位ID列表（必填，1-4个座位）
- `seatZone`: 座位区域（可选，用于快速抢票模式）
- `seatCount`: 座位数量（可选，用于快速抢票模式）
- `contactName`: 联系人姓名（必填，2-50个字符）
- `contactPhone`: 联系人手机（必填）
- `contactIdCard`: 联系人身份证（必填）

**示例请求**:
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

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "orderNo": "TK1709123456789001",
    "showEventId": 1,
    "showName": "周杰伦2026世界巡回演唱会-北京站",
    "venueName": "国家体育场（鸟巢）",
    "showTime": "2026-05-01 19:30:00",
    "seatCount": 2,
    "totalAmount": 2560.00,
    "status": "PENDING",
    "expireTime": "2026-02-07 14:30:00",
    "contactName": "张三",
    "contactPhone": "13800138000",
    "seats": [
      {
        "id": 1,
        "seatZone": "VIP",
        "seatRow": 1,
        "seatNumber": 1,
        "seatCode": "VIP-1-1",
        "price": 1280.00,
        "status": "LOCKED"
      },
      {
        "id": 2,
        "seatZone": "VIP",
        "seatRow": 1,
        "seatNumber": 2,
        "seatCode": "VIP-1-2",
        "price": 1280.00,
        "status": "LOCKED"
      }
    ],
    "createdAt": "2026-02-07 14:15:00"
  }
}
```

**错误示例**:
```json
{
  "code": 400,
  "message": "座位已被锁定或售出: VIP-1-1",
  "data": null
}
```

```json
{
  "code": 400,
  "message": "超过限购数量，每人最多购买2张",
  "data": null
}
```

```json
{
  "code": 400,
  "message": "请勿重复提交抢票请求",
  "data": null
}
```

---

### 3.2 快速抢票（自动选座）

**接口**: `POST /api/ticket/grab/quick`

**认证**: ✅ 需要 JWT token

**说明**: 由系统自动分配指定区域的连座座位，适合不挑座位、追求速度的用户

**请求体**:
```json
{
  "showEventId": 1,
  "seatZone": "VIP",
  "seatCount": 2,
  "contactName": "张三",
  "contactPhone": "13800138000",
  "contactIdCard": "110101199001011234"
}
```

**字段说明**:
- `showEventId`: 演出活动ID（必填）
- `seatZone`: 座位区域（必填，VIP/A/B/C）
- `seatCount`: 座位数量（必填，1-4）
- `preferContinuous`: 是否优先连座（可选，默认true）

**响应示例**: 同普通抢票接口

---

### 3.3 购物车管理

**添加到购物车**: `POST /api/ticket/cart/add`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "showEventId": 1,
  "seatIds": [1, 2]
}
```

**查看购物车**: `GET /api/ticket/cart`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "items": [
      {
        "id": 1,
        "showEventId": 1,
        "showName": "周杰伦2026世界巡回演唱会-北京站",
        "showTime": "2026-05-01 19:30:00",
        "seats": [
          {
            "seatId": 1,
            "seatCode": "VIP-1-1",
            "price": 1280.00
          }
        ],
        "totalAmount": 1280.00,
        "addedAt": "2026-02-25 14:00:00",
        "expireAt": "2026-02-25 14:15:00"
      }
    ],
    "totalItems": 1,
    "totalAmount": 1280.00
  }
}
```

**移除购物车项**: `DELETE /api/ticket/cart/{itemId}`

---

### 3.4 排队系统

**加入排队**: `POST /api/ticket/queue/join`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "showEventId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queueId": "Q202602251415001",
    "position": 158,
    "estimatedWaitTime": 180,
    "status": "WAITING"
  }
}
```

**查询排队状态**: `GET /api/ticket/queue/status?showEventId=1`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "queueId": "Q202602251415001",
    "position": 85,
    "estimatedWaitTime": 90,
    "status": "WAITING",
    "canBuy": false,
    "message": "前面还有84人，预计等待90秒"
  }
}
```

---

## 四、订单模块

### 4.1 支付订单

**接口**: `POST /api/ticket/pay`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "orderId": 123,
  "payType": "ALIPAY"
}
```

**字段说明**:
- `orderId`: 订单ID（必填）
- `payType`: 支付方式（必填，ALIPAY/WECHAT/CARD）

**示例请求**:
```bash
curl -X POST "http://localhost:8080/api/ticket/pay" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..." \
  -H "Content-Type: application/json" \
  -d '{
    "orderId": 123,
    "payType": "ALIPAY"
  }'
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 123,
    "orderNo": "TK1709123456789001",
    "status": "PAID",
    "payType": "ALIPAY",
    "payTime": "2026-02-07 14:20:00",
    "seats": [
      {
        "seatCode": "VIP-1-1",
        "status": "SOLD"
      },
      {
        "seatCode": "VIP-1-2",
        "status": "SOLD"
      }
    ]
  }
}
```

---

### 4.2 查询支付状态

**接口**: `GET /api/ticket/pay/status/{orderId}`

**认证**: ✅ 需要 JWT token

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 123,
    "orderNo": "TK1709123456789001",
    "payStatus": "PAID",
    "payType": "ALIPAY",
    "payTime": "2026-02-25 14:20:00",
    "totalAmount": 2560.00
  }
}
```

---

### 4.3 申请退款

**接口**: `POST /api/ticket/refund/{orderId}`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "reason": "行程变更",
  "description": "临时有事无法参加"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "退款申请已提交",
  "data": {
    "refundId": "RF202602251420001",
    "orderId": 123,
    "refundAmount": 2560.00,
    "refundFee": 128.00,
    "actualRefundAmount": 2432.00,
    "status": "PROCESSING",
    "estimatedTime": "3-5个工作日"
  }
}
```

---

### 4.4 取消订单

**接口**: `POST /api/ticket/cancel/{orderId}`

**认证**: ✅ 需要 JWT token

**路径参数**:
- `orderId`: 订单ID

**示例请求**:
```bash
curl -X POST "http://localhost:8080/api/ticket/cancel/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 4.5 查询订单详情

**接口**: `GET /api/ticket/order/{orderId}`

**认证**: ✅ 需要 JWT token

**路径参数**:
- `orderId`: 订单ID

**示例请求**:
```bash
curl -X GET "http://localhost:8080/api/ticket/order/123" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**响应示例**: 同抢票接口的响应

---

### 4.6 查询我的订单列表

**接口**: `GET /api/ticket/orders`

**认证**: ✅ 需要 JWT token

**请求参数**:
```
page: 页码（可选，默认1）
size: 每页大小（可选，默认10）
status: 订单状态筛选（可选，PENDING/PAID/CANCELLED/TIMEOUT）
```

**示例请求**:
```bash
# 查询所有订单
curl -X GET "http://localhost:8080/api/ticket/orders?page=1&size=10" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# 查询待支付订单
curl -X GET "http://localhost:8080/api/ticket/orders?status=PENDING" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 123,
        "orderNo": "TK1709123456789001",
        "showName": "周杰伦2026世界巡回演唱会-北京站",
        "status": "PAID",
        "totalAmount": 2560.00,
        "seatCount": 2,
        "createdAt": "2026-02-07 14:15:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

---

### 4.7 订单统计

**接口**: `GET /api/ticket/orders/statistics`

**认证**: ✅ 需要 JWT token

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalOrders": 15,
    "pendingOrders": 1,
    "paidOrders": 12,
    "cancelledOrders": 2,
    "totalAmount": 25600.00,
    "recentOrders": [
      {
        "orderId": 123,
        "showName": "周杰伦演唱会",
        "status": "PAID",
        "totalAmount": 2560.00,
        "createdAt": "2026-02-25 14:15:00"
      }
    ]
  }
}
```

---

## 五、票务管理模块

### 5.1 我的票夹

**接口**: `GET /api/ticket/my-tickets`

**认证**: ✅ 需要 JWT token

**请求参数**:
```
status: 票务状态（VALID/USED/EXPIRED/TRANSFERRED）
page: 页码
size: 每页大小
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "ticketId": "T202602251420001",
        "orderId": 123,
        "orderNo": "TK1709123456789001",
        "showName": "周杰伦2026世界巡回演唱会-北京站",
        "showTime": "2026-05-01 19:30:00",
        "venueName": "国家体育场（鸟巢）",
        "seatInfo": "VIP-1-1",
        "price": 1280.00,
        "status": "VALID",
        "ticketType": "ELECTRONIC",
        "qrcodeUrl": "https://example.com/qrcode/T202602251420001",
        "canTransfer": true,
        "canRefund": true
      }
    ],
    "total": 1
  }
}
```

### 5.2 获取电子票二维码

**接口**: `GET /api/ticket/my-tickets/{ticketId}/qrcode`

**认证**: ✅ 需要 JWT token

**响应**: 返回二维码图片（Base64 或 图片流）

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "ticketId": "T202602251420001",
    "qrcodeBase64": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAA...",
    "validUntil": "2026-05-01 23:59:59",
    "checkInStatus": "NOT_CHECKED_IN"
  }
}
```

### 5.3 转赠票务

**接口**: `POST /api/ticket/my-tickets/{ticketId}/transfer`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "receiverPhone": "13900139000",
  "receiverName": "李四",
  "message": "送你一张票，一起去看演唱会吧！"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "转赠成功",
  "data": {
    "transferId": "TR202602251420001",
    "ticketId": "T202602251420001",
    "receiverPhone": "13900139000",
    "status": "TRANSFERRED",
    "transferredAt": "2026-02-25 14:20:00"
  }
}
```

### 5.4 查询配送信息

**接口**: `GET /api/ticket/delivery/{orderId}`

**认证**: ✅ 需要 JWT token

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "orderId": 123,
    "deliveryType": "EXPRESS",
    "expressCompany": "顺丰速运",
    "trackingNumber": "SF1234567890",
    "status": "DELIVERING",
    "address": "北京市朝阳区xxx街道xxx号",
    "receiver": "张三",
    "receiverPhone": "13800138000",
    "estimatedDelivery": "2026-02-27",
    "tracks": [
      {
        "time": "2026-02-25 16:00:00",
        "status": "已发货",
        "location": "北京分拨中心"
      }
    ]
  }
}
```

---

## 六、消息通知模块

### 6.1 获取消息列表

**接口**: `GET /api/notification/messages`

**认证**: ✅ 需要 JWT token

**请求参数**:
```
type: 消息类型（SYSTEM/ORDER/ACTIVITY/PROMOTION）
isRead: 是否已读（true/false）
page: 页码
size: 每页大小
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "type": "ORDER",
        "title": "订单支付成功",
        "content": "您购买的周杰伦演唱会门票已支付成功",
        "isRead": false,
        "createdAt": "2026-02-25 14:20:00",
        "relatedId": 123,
        "relatedType": "ORDER"
      }
    ],
    "total": 5,
    "unreadCount": 3
  }
}
```

### 6.2 订阅开售提醒

**接口**: `POST /api/notification/subscribe`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "showEventId": 1,
  "notifyChannels": ["PUSH", "SMS", "EMAIL"]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "订阅成功",
  "data": {
    "subscriptionId": "SUB202602251420001",
    "showEventId": 1,
    "showName": "周杰伦2026世界巡回演唱会-北京站",
    "saleStartTime": "2026-03-01 10:00:00",
    "notifyChannels": ["PUSH", "SMS", "EMAIL"]
  }
}
```

---

## 七、收藏关注模块

### 7.1 收藏演出

**接口**: `POST /api/favorite/shows`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "showEventId": 1
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "收藏成功",
  "data": {
    "favoriteId": 1,
    "showEventId": 1,
    "favoritedAt": "2026-02-25 14:20:00"
  }
}
```

### 7.2 我的收藏

**接口**: `GET /api/favorite/shows`

**认证**: ✅ 需要 JWT token

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "favoriteId": 1,
      "showEventId": 1,
      "showName": "周杰伦2026世界巡回演唱会-北京站",
      "posterUrl": "https://example.com/posters/jay_concert.jpg",
      "showTime": "2026-05-01 19:30:00",
      "minPrice": 380.00,
      "status": "SELLING",
      "favoritedAt": "2026-02-25 14:20:00"
    }
  ]
}
```

### 7.3 关注艺人

**接口**: `POST /api/favorite/artists`

**请求体**:
```json
{
  "artistId": 1,
  "artistName": "周杰伦"
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "关注成功",
  "data": {
    "artistId": 1,
    "artistName": "周杰伦",
    "followedAt": "2026-02-25 14:20:00",
    "upcomingShowsCount": 3
  }
}
```

---

## 八、评价模块

### 8.1 发表评价

**接口**: `POST /api/review/shows/{showId}`

**认证**: ✅ 需要 JWT token

**请求体**:
```json
{
  "rating": 5,
  "content": "演出非常精彩，现场氛围超棒！",
  "tags": ["精彩", "值得", "音效好"],
  "images": [
    "https://example.com/review/img1.jpg"
  ]
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "评价成功",
  "data": {
    "reviewId": 1,
    "showId": 1,
    "userId": 1001,
    "nickname": "张三",
    "rating": 5,
    "content": "演出非常精彩，现场氛围超棒！",
    "likeCount": 0,
    "createdAt": "2026-02-25 14:20:00",
    "status": "PENDING_REVIEW"
  }
}
```

### 8.2 获取演出评价

**接口**: `GET /api/review/shows/{showId}`

**请求参数**:
```
sort: 排序方式（HOT/LATEST/RATING_HIGH/RATING_LOW）
rating: 评分筛选（1-5）
page: 页码
size: 每页大小
```

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalReviews": 1580,
      "averageRating": 4.8,
      "ratingDistribution": {
        "5": 1200,
        "4": 280,
        "3": 80,
        "2": 15,
        "1": 5
      }
    },
    "records": [
      {
        "reviewId": 1,
        "userId": 1001,
        "nickname": "张三",
        "avatar": "https://example.com/avatar/1001.jpg",
        "rating": 5,
        "content": "演出非常精彩，现场氛围超棒！",
        "likeCount": 128,
        "createdAt": "2026-02-25 14:20:00",
        "isLiked": false
      }
    ]
  }
}
```

---

## 九、风控模块

### 9.1 获取验证码

**接口**: `GET /api/risk/captcha`

**响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "captchaId": "CAPTCHA_202602251420001",
    "captchaType": "SLIDE",
    "backgroundImage": "data:image/png;base64,...",
    "sliderImage": "data:image/png;base64,...",
    "expireTime": 60
  }
}
```

### 9.2 验证验证码

**接口**: `POST /api/risk/captcha/verify`

**请求体**:
```json
{
  "captchaId": "CAPTCHA_202602251420001",
  "xPos": 245,
  "timestamp": 1708847234567
}
```

**响应示例**:
```json
{
  "code": 200,
  "message": "验证成功",
  "data": {
    "verified": true,
    "token": "CAPTCHA_TOKEN_xxx"
  }
}
```

---

## 🔥 高并发优化方案

### 1. 缓存策略

#### 1.1 Redis 多级缓存
```
- L1: 本地缓存（Caffeine）- 热点数据，1分钟过期
- L2: Redis 缓存 - 演出详情、座位信息，10分钟过期
- L3: MySQL 数据库 - 持久化存储
```

#### 1.2 缓存预热
```java
// 演出开售前30分钟，预热座位缓存
@Scheduled(cron = "0 */5 * * * ?")
public void warmupSeatCache() {
    List<ShowEvent> upcomingShows = getUpcomingShows(30);
    for (ShowEvent show : upcomingShows) {
        // 预加载座位信息到 Redis
        loadSeatsToCache(show.getId());
    }
}
```

#### 1.3 热点数据保护
```
- 布隆过滤器：防止缓存穿透
- 互斥锁：防止缓存击穿
- 熔断降级：防止缓存雪崩
```

### 2. 分布式锁方案

#### 2.1 Redisson 分布式锁
```java
// 抢票时对座位加锁
RLock lock = redissonClient.getLock("seat:lock:" + seatId);
try {
    if (lock.tryLock(100, 10, TimeUnit.SECONDS)) {
        // 执行抢票逻辑
        grabTicket(seatId, userId);
    } else {
        throw new BusinessException("座位已被锁定");
    }
} finally {
    if (lock.isHeldByCurrentThread()) {
        lock.unlock();
    }
}
```

#### 2.2 防重复提交
```java
// 用户级别的防重复提交锁
String lockKey = "grab:lock:user:" + userId;
RLock userLock = redissonClient.getLock(lockKey);
if (!userLock.tryLock(0, 10, TimeUnit.SECONDS)) {
    throw new BusinessException("请勿重复提交抢票请求");
}
```

### 3. 异步处理方案

#### 3.1 Kafka 延迟队列
```java
// 订单超时处理
@kafkaListener(topics = "order.timeout")
public void handleOrderTimeout(OrderTimeoutMessage message) {
    Order order = orderService.getById(message.getOrderId());
    if (order.getStatus() == OrderStatus.PENDING) {
        // 取消订单，释放座位
        orderService.cancelOrder(order.getId());
    }
}
```

#### 3.2 异步通知
```java
// 抢票成功后异步发送通知
kafkaTemplate.convertAndSend("notification.exchange",
    "notification.sms",
    new NotificationMessage(userId, "抢票成功", smsContent));
```

### 4. 数据库优化

#### 4.1 索引优化
```sql
-- 座位查询索引
CREATE INDEX idx_seat_show_zone_status
ON t_seat(show_event_id, seat_zone, status);

-- 订单查询索引
CREATE INDEX idx_order_user_status
ON t_order(user_id, status, created_at DESC);
```

### 5. 限流降级

#### 5.1 接口限流
```java
@RateLimiter(value = 100, timeout = 1000)
@GetMapping("/shows")
public Result<PageResult<ShowEvent>> list() {
    // ...
}
```

#### 5.2 用户限流
```java
// 每个用户每秒最多3次抢票请求
RRateLimiter limiter = redissonClient.getRateLimiter("limiter:user:" + userId);
limiter.trySetRate(RateType.OVERALL, 3, 1, RateIntervalUnit.SECONDS);
if (!limiter.tryAcquire()) {
    throw new BusinessException("请求过于频繁，请稍后再试");
}
```

---

## 📊 数据库设计

### 核心表结构

#### 1. 用户表 (t_user)
```sql
CREATE TABLE t_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    phone VARCHAR(11) NOT NULL UNIQUE COMMENT '手机号',
    password VARCHAR(128) NOT NULL COMMENT '密码（加密）',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(255) COMMENT '头像URL',
    real_name VARCHAR(50) COMMENT '真实姓名',
    id_card VARCHAR(18) COMMENT '身份证号',
    is_real_name_verified TINYINT DEFAULT 0 COMMENT '是否实名认证',
    member_level VARCHAR(20) DEFAULT 'NORMAL' COMMENT '会员等级',
    points INT DEFAULT 0 COMMENT '积分',
    status TINYINT DEFAULT 1 COMMENT '状态：1-正常 0-禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone),
    INDEX idx_id_card (id_card)
) COMMENT='用户表';
```

#### 2. 演出活动表 (t_show_event)
```sql
CREATE TABLE t_show_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '演出ID',
    show_name VARCHAR(200) NOT NULL COMMENT '演出名称',
    show_type VARCHAR(20) NOT NULL COMMENT '演出类型',
    artist VARCHAR(100) COMMENT '艺人/演员',
    city VARCHAR(50) COMMENT '城市',
    venue_name VARCHAR(100) COMMENT '场馆名称',
    venue_address VARCHAR(255) COMMENT '场馆地址',
    show_time DATETIME NOT NULL COMMENT '演出时间',
    sale_start_time DATETIME NOT NULL COMMENT '开售时间',
    sale_end_time DATETIME NOT NULL COMMENT '停售时间',
    total_seats INT NOT NULL COMMENT '总座位数',
    available_seats INT NOT NULL COMMENT '可售座位数',
    sold_seats INT DEFAULT 0 COMMENT '已售座位数',
    max_buy_limit INT DEFAULT 4 COMMENT '限购数量',
    poster_url VARCHAR(255) COMMENT '海报URL',
    description TEXT COMMENT '演出描述',
    status VARCHAR(20) NOT NULL COMMENT '状态',
    min_price DECIMAL(10,2) COMMENT '最低价格',
    max_price DECIMAL(10,2) COMMENT '最高价格',
    view_count INT DEFAULT 0 COMMENT '浏览次数',
    favorite_count INT DEFAULT 0 COMMENT '收藏次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_show_time (show_time),
    INDEX idx_sale_start (sale_start_time),
    INDEX idx_status (status),
    INDEX idx_city_type (city, show_type)
) COMMENT='演出活动表';
```

#### 3. 座位表 (t_seat)
```sql
CREATE TABLE t_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
    show_event_id BIGINT NOT NULL COMMENT '演出ID',
    seat_zone VARCHAR(10) NOT NULL COMMENT '座位区域',
    seat_row INT NOT NULL COMMENT '座位排号',
    seat_number INT NOT NULL COMMENT '座位号',
    seat_code VARCHAR(20) NOT NULL COMMENT '座位编码',
    price DECIMAL(10,2) NOT NULL COMMENT '价格',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态',
    locked_at DATETIME COMMENT '锁定时间',
    locked_by BIGINT COMMENT '锁定用户',
    sold_at DATETIME COMMENT '售出时间',
    sold_to BIGINT COMMENT '购买用户',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_seat_code (show_event_id, seat_code),
    INDEX idx_show_zone_status (show_event_id, seat_zone, status),
    INDEX idx_status (status)
) COMMENT='座位表';
```

#### 4. 订单表 (t_order)
```sql
CREATE TABLE t_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(32) NOT NULL UNIQUE COMMENT '订单号',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    show_event_id BIGINT NOT NULL COMMENT '演出ID',
    show_name VARCHAR(200) COMMENT '演出名称',
    venue_name VARCHAR(100) COMMENT '场馆名称',
    show_time DATETIME COMMENT '演出时间',
    seat_count INT NOT NULL COMMENT '座位数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) NOT NULL COMMENT '订单状态',
    pay_type VARCHAR(20) COMMENT '支付方式',
    pay_time DATETIME COMMENT '支付时间',
    expire_time DATETIME COMMENT '过期时间',
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(11) COMMENT '联系人手机',
    contact_id_card VARCHAR(18) COMMENT '联系人身份证',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_status (user_id, status),
    INDEX idx_order_no (order_no),
    INDEX idx_created_at (created_at DESC)
) COMMENT='订单表';
```

#### 5. 订单座位关联表 (t_order_seat)
```sql
CREATE TABLE t_order_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_id BIGINT NOT NULL COMMENT '订单ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    seat_code VARCHAR(20) COMMENT '座位编码',
    price DECIMAL(10,2) COMMENT '座位价格',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_seat_id (seat_id)
) COMMENT='订单座位关联表';
```

---

## 🔥 高并发测试场景

### 场景1：模拟抢票高峰

使用压测工具（如 JMeter, Apache Bench, wrk）模拟高并发抢票：

```bash
# 使用 wrk 进行压测（需要安装 wrk）
wrk -t 10 -c 100 -d 30s -H "Authorization: Bearer YOUR_TOKEN" \
  -s grab_ticket.lua \
  http://localhost:8080/api/ticket/grab
```

**grab_ticket.lua**:
```lua
wrk.method = "POST"
wrk.headers["Content-Type"] = "application/json"
wrk.body = '{"showEventId":1,"seatIds":[' .. math.random(1, 8000) .. '],"contactName":"测试用户","contactPhone":"13800138000","contactIdCard":"110101199001011234"}'
```

### 场景2：验证防超卖机制

1. 准备100个并发请求，同时抢同一个座位
2. 预期结果：只有1个请求成功，其他99个失败（座位已被锁定）

### 场景3：验证限购机制

1. 同一用户连续抢票，超过限购数量
2. 预期结果：第3次抢票失败（假设限购2张）

### 场景4：验证订单超时机制

1. 抢票成功但不支付
2. 等待15分钟后，查询订单状态
3. 预期结果：订单状态变为 TIMEOUT，座位释放

---

## ⚠️ 常见错误码

| 错误码 | 说明 | 解决方案 |
|-------|------|---------|
| 401 | Token无效或已过期 | 重新登录获取新token |
| 403 | 无权访问 | 检查用户权限 |
| 400 | 座位已被锁定或售出 | 选择其他座位 |
| 400 | 超过限购数量 | 减少购票数量 |
| 400 | 请勿重复提交抢票请求 | 等待10秒后重试 |
| 400 | 演出未开始售票或已结束 | 查看演出售票时间 |
| 400 | 订单已过期 | 重新抢票 |
| 400 | 验证码错误 | 重新获取验证码 |
| 400 | 未实名认证 | 先进行实名认证 |
| 404 | 演出活动不存在 | 检查演出活动ID |
| 404 | 订单不存在 | 检查订单ID |
| 429 | 请求过于频繁 | 稍后再试 |
| 500 | 服务器内部错误 | 联系客服 |

---

## 📊 性能指标

根据系统设计，以下是预期的性能指标：

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 抢票接口 TPS | ≥ 2000 | Redis锁 + 缓存优化 |
| 抢票接口 P99 延迟 | ≤ 300ms | 分布式锁性能优化 |
| 演出详情接口 TPS | ≥ 10000 | 多级缓存加速 |
| 座位查询接口 TPS | ≥ 5000 | Redis缓存 + 分页 |
| 订单查询接口 TPS | ≥ 3000 | 索引优化 |
| 搜索接口 TPS | ≥ 5000 | ElasticSearch |
| 系统可用性 | ≥ 99.9% | 集群部署 + 降级保护 |
| 数据一致性 | 100% | 分布式事务保障 |

---

## 🛠️ 开发建议

### 1. 接口调用流程

#### 1.1 用户注册登录流程
```
1. 调用 POST /api/user/register 注册
2. 调用 POST /api/user/login 登录，获取 token
3. 后续请求携带 token 访问需要认证的接口
```

#### 1.2 抢票完整流程
```
1. 浏览演出：GET /api/ticket/shows
2. 查看详情：GET /api/ticket/shows/{id}
3. 查看座位：GET /api/ticket/shows/{showEventId}/seats
4. 实名认证：POST /api/user/verify/real-name（如未认证）
5. 获取验证码：GET /api/risk/captcha
6. 验证验证码：POST /api/risk/captcha/verify
7. 抢票下单：POST /api/ticket/grab
8. 支付订单：POST /api/ticket/pay
9. 查看电子票：GET /api/ticket/my-tickets
```

### 2. 最佳实践

#### 2.1 认证 Token 管理
```javascript
// 前端示例：自动刷新 token
const axios = require('axios');

// 请求拦截器
axios.interceptors.request.use(config => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器
axios.interceptors.response.use(
  response => response,
  error => {
    if (error.response.status === 401) {
      // Token 过期，跳转登录
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);
```

#### 2.2 防重复提交
```javascript
// 前端防抖
let grabbing = false;

async function grabTicket(data) {
  if (grabbing) {
    console.log('请勿重复提交');
    return;
  }

  grabbing = true;
  try {
    const result = await api.post('/api/ticket/grab', data);
    console.log('抢票成功', result);
  } finally {
    setTimeout(() => {
      grabbing = false;
    }, 3000);
  }
}
```

#### 2.3 轮询订单状态
```javascript
// 支付后轮询订单状态
async function pollPaymentStatus(orderId) {
  const maxRetries = 30;
  let retries = 0;

  const poll = setInterval(async () => {
    try {
      const result = await api.get(`/api/ticket/pay/status/${orderId}`);
      if (result.data.payStatus === 'PAID') {
        clearInterval(poll);
        console.log('支付成功');
        // 跳转到订单详情页
      }

      retries++;
      if (retries >= maxRetries) {
        clearInterval(poll);
        console.log('支付超时');
      }
    } catch (error) {
      console.error('查询支付状态失败', error);
    }
  }, 2000);
}
```

### 3. 错误处理

```javascript
// 统一错误处理
try {
  const result = await api.post('/api/ticket/grab', grabData);
  if (result.code === 200) {
    // 成功
    console.log('抢票成功', result.data);
  } else {
    // 业务错误
    console.error('抢票失败', result.message);
    alert(result.message);
  }
} catch (error) {
  // 网络错误或服务器错误
  console.error('请求失败', error);
  alert('网络异常，请稍后重试');
}
```

---

## 🎯 后续优化方向

### 1. 功能扩展
- [ ] AI 智能推荐演出
- [ ] 语音搜索演出
- [ ] 虚拟排队可视化
- [ ] 3D 座位图交互
- [ ] 社交分享功能
- [ ] 拼团购票
- [ ] 盲盒抢票
- [ ] 直播抢票

### 2. 性能优化
- [ ] CDN 加速静态资源
- [ ] WebSocket 实时推送
- [ ] GraphQL API
- [ ] HTTP/3 支持
- [ ] 边缘计算部署
- [ ] 智能 DNS 解析

### 3. 安全增强
- [ ] 设备指纹识别
- [ ] 行为分析防刷
- [ ] 人脸识别验票
- [ ] 区块链电子票
- [ ] 数据加密传输
- [ ] WAF 防护

### 4. 运营功能
- [ ] 优惠券系统
- [ ] 营销活动平台
- [ ] 数据分析报表
- [ ] 用户画像分析
- [ ] A/B 测试平台
- [ ] 智能客服机器人

---

## 🛠️ Postman 测试集合

建议导入以下 Postman Collection 进行测试：

```json
{
  "info": {
    "name": "抢票系统完整API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "用户模块",
      "item": [
        {
          "name": "用户注册",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/user/register",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"phone\": \"13800138000\",\n  \"password\": \"password123\",\n  \"verifyCode\": \"123456\",\n  \"nickname\": \"张三\"\n}"
            }
          }
        },
        {
          "name": "用户登录",
          "request": {
            "method": "POST",
            "url": "{{baseUrl}}/api/user/login",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"phone\": \"13800138000\",\n  \"password\": \"password123\"\n}"
            }
          }
        }
      ]
    },
    {
      "name": "演出模块",
      "item": [
        {
          "name": "查询演出列表",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/ticket/shows?page=1&size=10&status=SELLING"
          }
        },
        {
          "name": "演出搜索",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/ticket/shows/search?keyword=周杰伦&city=北京"
          }
        },
        {
          "name": "查询演出详情",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/ticket/shows/1"
          }
        }
      ]
    },
    {
      "name": "抢票模块",
      "item": [
        {
          "name": "抢票",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": "{{baseUrl}}/api/ticket/grab",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"showEventId\": 1,\n  \"seatIds\": [1, 2],\n  \"contactName\": \"张三\",\n  \"contactPhone\": \"13800138000\",\n  \"contactIdCard\": \"110101199001011234\"\n}"
            }
          }
        },
        {
          "name": "快速抢票",
          "request": {
            "method": "POST",
            "header": [
              {
                "key": "Authorization",
                "value": "Bearer {{token}}"
              }
            ],
            "url": "{{baseUrl}}/api/ticket/grab/quick",
            "body": {
              "mode": "raw",
              "raw": "{\n  \"showEventId\": 1,\n  \"seatZone\": \"VIP\",\n  \"seatCount\": 2,\n  \"contactName\": \"张三\",\n  \"contactPhone\": \"13800138000\",\n  \"contactIdCard\": \"110101199001011234\"\n}"
            }
          }
        }
      ]
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:8080"
    },
    {
      "key": "token",
      "value": "your_jwt_token_here"
    }
  ]
}
```

---

## 📞 技术支持

- 📧 邮箱：support@ticketsystem.com
- 💬 在线文档：https://docs.ticketsystem.com
- 🐛 问题反馈：https://github.com/ticketsystem/issues

---

**✨ 祝开发顺利！如有问题，请查看日志或联系开发团队。**
