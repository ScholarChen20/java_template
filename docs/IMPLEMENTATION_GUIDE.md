# 社交模块后接口完善说明

## 概述

本次完善针对 **4. 社交模块之后的接口**,包括:
- 5. 旅行计划模块 (TravelPlan)
- 6. 对话模块 (Dialog)
- 7. 系统模块 (System)

从模拟数据升级为**真实业务场景**,整合了数据库、Redis缓存和Kafka消息队列,确保数据一致性。

---

## 新增/修改的文件

### 1. 常量定义

#### `KafkaTopic.java`
定义系统中所有Kafka主题名称:
- 旅行计划相关: `TRAVEL_PLAN_CREATED`, `TRAVEL_PLAN_UPDATED`, `TRAVEL_PLAN_DELETED`, `TRAVEL_PLAN_CACHE_SYNC`
- 对话相关: `DIALOG_CREATED`, `MESSAGE_SENT`, `MESSAGE_STATUS_UPDATED`, `DIALOG_ARCHIVED`, `DIALOG_CACHE_SYNC`
- 系统相关: `SYSTEM_HEALTH_CHECK`, `AUDIT_LOG`, `CACHE_INVALIDATION`, `DATA_SYNC`
- 通知相关: `USER_NOTIFICATION`, `SYSTEM_NOTIFICATION`

#### `EventType.java`
定义系统中所有事件类型:
- 旅行计划事件: `TRAVEL_PLAN_CREATE`, `TRAVEL_PLAN_UPDATE`, `TRAVEL_PLAN_DELETE`, `TRAVEL_PLAN_VIEW`
- 对话事件: `DIALOG_CREATE`, `MESSAGE_SEND`, `MESSAGE_RECEIVE`, `MESSAGE_READ`, `DIALOG_ARCHIVE`, `DIALOG_UNARCHIVE`
- 缓存事件: `CACHE_UPDATE`, `CACHE_INVALIDATE`, `CACHE_REFRESH`, `CACHE_CLEAR`
- 系统事件: `SYSTEM_START`, `SYSTEM_SHUTDOWN`, `HEALTH_CHECK`, `AUDIT_LOG`
- 数据同步事件: `DB_TO_CACHE_SYNC`, `CACHE_TO_DB_SYNC`, `DATA_CONSISTENCY_CHECK`

### 2. Service实现完善

#### `TravelPlanServiceImpl.java` (已完善)
**改进点:**
- ✅ 集成Kafka消息生产者
- ✅ 在创建、更新、删除操作时发送Kafka事件
- ✅ 保留原有的Redis缓存逻辑
- ✅ 保留原有的MongoDB存储逻辑

**业务流程:**
```
创建旅行计划
├── 1. 保存到MongoDB
├── 2. 清除列表缓存
└── 3. 发送Kafka事件 (TRAVEL_PLAN_CREATE)
    └── 异步处理: 通知用户、数据统计、推荐攻略
```

#### `DialogServiceImpl.java` (已完善)
**改进点:**
- ✅ 集成Kafka消息生产者
- ✅ 在创建对话、发送消息、更新状态、归档时发送Kafka事件
- ✅ 保留原有的Redis缓存逻辑
- ✅ 保留原有的MongoDB存储逻辑

**业务流程:**
```
发送消息
├── 1. 保存消息到MongoDB
├── 2. 清除对话缓存
└── 3. 发送Kafka事件 (MESSAGE_SEND)
    └── 异步处理: 实时通知接收者、更新未读计数、消息统计
```

#### `SystemServiceImpl.java` (已完善)
**改进点:**
- ✅ 从模拟数据改为真实系统监控
- ✅ 实现真实的健康检查功能
- ✅ 检查MySQL、Redis、MongoDB、Kafka的健康状态
- ✅ 实现真实的系统信息获取(JVM、操作系统、内存等)

**功能:**
- **getSystemInfo()**: 获取系统基础信息、Java环境信息、运行时信息
- **getSystemStatus()**: 获取系统状态、内存使用率、堆内存使用、组件健康检查
- **cleanSystemCache()**: 清理系统缓存(Redis缓存、JVM垃圾回收)
- **restartSystemService()**: 服务重启提示(需管理员权限)

### 3. Kafka消费者

#### `TravelPlanEventConsumer.java` (新增)
监听旅行计划相关事件:
- `handleTravelPlanCreated()`: 处理旅行计划创建事件
- `handleTravelPlanUpdated()`: 处理旅行计划更新事件,同步缓存
- `handleTravelPlanDeleted()`: 处理旅行计划删除事件,清理缓存和相关数据

#### `DialogEventConsumer.java` (新增)
监听对话相关事件:
- `handleDialogCreated()`: 处理对话创建事件,初始化未读计数
- `handleMessageSent()`: 处理消息发送事件,发送实时通知、更新未读计数
- `handleMessageStatusUpdated()`: 处理消息状态更新事件,清除未读计数
- `handleDialogArchived()`: 处理对话归档事件

#### `CacheSyncEventConsumer.java` (新增)
监听缓存同步和数据一致性事件:
- `handleCacheInvalidation()`: 处理缓存失效通知
- `handleDataSync()`: 处理数据同步事件(CREATE/UPDATE/DELETE)
- `handleTravelPlanCacheSync()`: 处理旅行计划缓存同步
- `handleDialogCacheSync()`: 处理对话缓存同步

---

## 架构设计

### 数据流转

```
用户请求
    ↓
Controller
    ↓
Service (业务处理)
    ├─→ MongoDB (持久化存储)
    ├─→ Redis (缓存更新)
    └─→ Kafka (发送事件)
           ↓
    Kafka Consumer (异步处理)
    ├─→ 缓存同步
    ├─→ 实时通知
    ├─→ 数据统计
    └─→ 业务扩展
```

### 缓存一致性保证

#### 1. **Cache Aside模式**
```java
// 读操作
1. 先查Redis缓存
2. 缓存命中 → 返回缓存数据
3. 缓存未命中 → 查询MongoDB → 写入Redis → 返回数据

// 写操作
1. 更新MongoDB数据
2. 删除Redis缓存 (而不是更新缓存)
3. 发送Kafka事件通知其他节点
```

#### 2. **Kafka异步同步**
```java
// Service层
1. 数据库操作完成
2. 清除本地缓存
3. 发送Kafka事件

// Consumer层
1. 接收Kafka事件
2. 清除其他节点的缓存
3. 保证分布式环境下的缓存一致性
```

---

## 使用示例

### 1. 旅行计划模块

#### 创建旅行计划
```bash
POST /api/travel-plans/create
{
    "userId": 1,
    "title": "北京三日游",
    "description": "北京经典三日游行程",
    "destination": "北京市",
    "startDate": "2026-03-01",
    "endDate": "2026-03-03"
}
```

**执行流程:**
1. 数据保存到MongoDB
2. 清除用户的旅行计划列表缓存
3. 发送Kafka事件到 `travel-plan-created` 主题
4. `TravelPlanEventConsumer` 接收事件:
   - 发送通知给用户
   - 更新用户旅行计划统计
   - 推荐相关旅行攻略

#### 获取旅行计划列表
```bash
GET /api/travel-plans/list?userId=1&page=1&size=10
```

**执行流程:**
1. 先查Redis缓存 `travel_plan:list:1:1:10`
2. 缓存命中 → 返回缓存数据
3. 缓存未命中 → 查询MongoDB → 写入Redis(1小时过期) → 返回数据

### 2. 对话模块

#### 创建对话
```bash
POST /api/dialogs
Headers: Authorization: Bearer {token}
{
    "recipient_id": 2,
    "type": "private"
}
```

**执行流程:**
1. 检查是否已存在对话
2. 创建新对话,保存到MongoDB
3. 清除对话列表缓存
4. 发送Kafka事件到 `dialog-created` 主题
5. `DialogEventConsumer` 接收事件:
   - 发送通知给双方用户
   - 初始化未读消息计数

#### 发送消息
```bash
POST /api/dialogs/{dialogId}/messages
Headers: Authorization: Bearer {token}
{
    "content": "你好,最近怎么样?",
    "type": "text"
}
```

**执行流程:**
1. 创建消息对象
2. 添加消息到对话,保存MongoDB
3. 清除对话缓存和列表缓存
4. 发送Kafka事件到 `message-sent` 主题
5. `DialogEventConsumer` 接收事件:
   - 发送实时通知给接收者(WebSocket/推送)
   - 增加接收者的未读消息计数
   - 记录消息统计

### 3. 系统模块

#### 获取系统健康状态
```bash
GET /api/system/health
```

**返回示例:**
```json
{
    "code": 200,
    "data": {
        "status": "healthy",
        "systemLoadAverage": "1.25",
        "availableProcessors": 8,
        "memoryUsage": "45.23%",
        "totalMemory": "2.00 GB",
        "usedMemory": "904.60 MB",
        "freeMemory": "1.12 GB",
        "heapMemoryUsage": "38.50%",
        "heapMemoryUsed": "512.00 MB",
        "heapMemoryMax": "1.33 GB",
        "components": {
            "database": "UP",
            "redis": "UP",
            "mongodb": "UP",
            "kafka": "UP"
        },
        "uptime": "2d 5h 30m 45s",
        "timestamp": 1738867200000
    }
}
```

---

## 配置说明

### Kafka配置
确保 `application.yml` 中配置了Kafka:
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: yoyo-data-group
      auto-offset-reset: earliest
      enable-auto-commit: true
    producer:
      retries: 3
      acks: 1
```

### Redis配置
确保 `application.yml` 中配置了Redis:
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    database: 0
    timeout: 5000
    jedis:
      pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

### MongoDB配置
确保 `application.yml` 中配置了MongoDB:
```yaml
spring:
  data:
    mongodb:
      uri: mongodb://localhost:27017/yoyo_data
```

---

## 监控建议

### 1. Kafka监控
- 监控消费者延迟 (Consumer Lag)
- 监控消息积压情况
- 监控消费者健康状态

### 2. Redis监控
- 监控缓存命中率
- 监控内存使用情况
- 监控Key过期情况

### 3. 业务监控
- 监控旅行计划创建/更新/删除的QPS
- 监控消息发送的QPS和延迟
- 监控系统健康检查结果

---

## 性能优化

### 1. 缓存策略
- **热点数据**: 1小时缓存过期时间
- **列表数据**: 分页缓存,减少缓存内存占用
- **缓存预热**: 可在系统启动时预加载热点数据

### 2. Kafka优化
- **消息批量发送**: 提高吞吐量
- **消费者并行**: 增加消费者数量,提高消费速度
- **异步发送**: 不阻塞主业务流程

### 3. 数据库优化
- **索引优化**: 确保MongoDB查询字段有索引
- **连接池**: 合理配置数据库连接池大小
- **读写分离**: 可考虑MongoDB的副本集读写分离

---

## 扩展性

### 1. 水平扩展
- 多实例部署Service层
- Kafka自动负载均衡消费
- Redis Cluster实现缓存分布式

### 2. 功能扩展
- 添加WebSocket实现实时通知
- 添加消息队列死信处理
- 添加数据同步重试机制
- 添加分布式事务支持

---

## 总结

本次完善实现了以下目标:

✅ **真实业务场景**: 从模拟数据升级为真实的数据库+缓存+消息队列架构
✅ **缓存一致性**: 通过Cache Aside + Kafka实现分布式缓存一致性
✅ **异步处理**: 使用Kafka实现业务解耦和异步处理
✅ **系统监控**: 实现真实的系统健康检查和监控
✅ **可扩展性**: 支持水平扩展和功能扩展

下一步可以:
- 添加分布式事务支持(Seata)
- 添加链路追踪(SkyWalking)
- 添加限流降级(Sentinel)
- 添加监控告警(Prometheus + Grafana)
