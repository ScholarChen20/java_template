# 演唱会抢票系统 - API 接口测试指南

## 📋 接口概览

### 演出活动模块（ShowEventController）

| 接口路径 | 方法 | 说明 | 是否需要认证 |
|---------|------|------|-------------|
| `/api/ticket/shows` | GET | 查询演出活动列表 | ❌ 否 |
| `/api/ticket/shows/{id}` | GET | 查询演出活动详情 | ❌ 否 |
| `/api/ticket/shows/{showEventId}/seats` | GET | 查询座位列表 | ❌ 否 |
| `/api/ticket/shows/{showEventId}/seats/available-count` | GET | 查询可售座位数量 | ❌ 否 |

### 抢票模块（TicketController）

| 接口路径 | 方法 | 说明 | 是否需要认证 |
|---------|------|------|-------------|
| `/api/ticket/grab` | POST | 抢票（核心接口） | ✅ 是 |
| `/api/ticket/pay` | POST | 支付订单 | ✅ 是 |
| `/api/ticket/cancel/{orderId}` | POST | 取消订单 | ✅ 是 |
| `/api/ticket/order/{orderId}` | GET | 查询订单详情 | ✅ 是 |
| `/api/ticket/orders` | GET | 查询我的订单列表 | ✅ 是 |

---

## 🔐 认证说明

需要认证的接口必须在请求头中携带 JWT token：

```
Authorization: Bearer {your_jwt_token}
```

---

## 📡 接口详细说明

### 1. 查询演出活动列表

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

**响应示例**: 同上

---

### 3. 查询座位列表

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

### 5. 抢票（核心接口）⚡

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

### 6. 支付订单

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

### 7. 取消订单

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

### 8. 查询订单详情

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

### 9. 查询我的订单列表

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
| 400 | 座位已被锁定或售出 | 选择其他座位 |
| 400 | 超过限购数量 | 减少购票数量 |
| 400 | 请勿重复提交抢票请求 | 等待10秒后重试 |
| 400 | 演出未开始售票或已结束 | 查看演出售票时间 |
| 400 | 订单已过期 | 重新抢票 |
| 404 | 演出活动不存在 | 检查演出活动ID |
| 404 | 订单不存在 | 检查订单ID |

---

## 📊 性能指标

根据系统设计，以下是预期的性能指标：

| 指标 | 目标值 |
|------|--------|
| 抢票接口 TPS | ≥ 1000 |
| 抢票接口 P99 延迟 | ≤ 500ms |
| 演出详情接口 TPS | ≥ 5000（Redis缓存） |
| 座位查询接口 TPS | ≥ 3000 |
| 订单查询接口 TPS | ≥ 2000 |

---

## 🛠️ Postman 测试集合

建议导入以下 Postman Collection 进行测试：

```json
{
  "info": {
    "name": "演唱会抢票系统API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "演出活动模块",
      "item": [
        {
          "name": "查询演出列表",
          "request": {
            "method": "GET",
            "url": "{{baseUrl}}/api/ticket/shows?page=1&size=10&status=SELLING"
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

**✨ 测试愉快！如有问题，请查看日志或联系开发团队。**
