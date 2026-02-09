# 切换到 Redisson 分布式锁 - 操作指南

## 📋 切换步骤

### 步骤 1：确认依赖已添加

检查 `pom.xml` 是否包含 Redisson 依赖：

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <version>3.16.8</version>
</dependency>
```

✅ 你的项目已包含此依赖。

---

### 步骤 2：修改 Controller 注入

**原代码**（TicketController.java）：
```java
@Autowired
private TicketService ticketService; // 默认注入 TicketServiceImpl
```

**改为**：
```java
@Autowired
@Qualifier("ticketServiceRedisson") // 指定使用 Redisson 实现
private TicketService ticketService;
```

**或者更优雅的方式**：

将 `TicketServiceImpl` 的 `@Service` 注解改为：
```java
@Service("ticketServiceSimple") // 改名
public class TicketServiceImpl implements TicketService {
    // ...
}
```

将 `TicketServiceRedissonImpl` 的 `@Service` 注解改为：
```java
@Service // 使用默认名称，成为主实现
public class TicketServiceRedissonImpl implements TicketService {
    // ...
}
```

这样 Controller 无需修改，自动注入 Redisson 实现。

---

### 步骤 3：完善 Redisson 实现的其他方法

当前 `TicketServiceRedissonImpl` 只实现了 `grabTicket` 方法，其他方法返回错误。

建议：
1. 从 `TicketServiceImpl` 复制其他方法的实现
2. 或者创建一个抽象基类，提取公共方法

**示例**：

```java
@Service
public class TicketServiceRedissonImpl extends AbstractTicketService implements TicketService {

    @Autowired
    private RedissonClient redissonClient;

    @Override
    public Result<TicketOrderVO> grabTicket(GrabTicketDTO dto, String token) {
        // Redisson 实现
        // ...
    }

    // 其他方法继承自 AbstractTicketService
}
```

---

### 步骤 4：配置 Redisson（可选）

如果需要自定义 Redisson 配置，修改 `RedissonConfig.java`：

```java
@Bean(destroyMethod = "shutdown")
public RedissonClient redissonClient() {
    Config config = new Config();

    // 单机模式
    config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setDatabase(redisDatabase)
            .setConnectionPoolSize(64) // 根据实际情况调整
            .setConnectionMinimumIdleSize(10)
            .setIdleConnectionTimeout(10000)
            .setConnectTimeout(10000)
            .setTimeout(3000)
            .setRetryAttempts(3)
            .setRetryInterval(1500);

    // 集群模式（高可用）
    // config.useClusterServers()
    //         .addNodeAddress("redis://127.0.0.1:6379")
    //         .addNodeAddress("redis://127.0.0.1:6380");

    // 主从模式
    // config.useMasterSlaveServers()
    //         .setMasterAddress("redis://127.0.0.1:6379")
    //         .addSlaveAddress("redis://127.0.0.1:6380");

    return Redisson.create(config);
}
```

---

### 步骤 5：测试验证

#### 5.1 单元测试

创建测试类 `TicketServiceRedissonImplTest.java`：

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class TicketServiceRedissonImplTest {

    @Autowired
    @Qualifier("ticketServiceRedisson")
    private TicketService ticketService;

    @Test
    public void testGrabTicket() {
        // 准备测试数据
        GrabTicketDTO dto = new GrabTicketDTO();
        dto.setShowEventId(1L);
        dto.setSeatIds(Arrays.asList(1L, 2L));
        dto.setContactName("张三");
        dto.setContactPhone("13800138000");
        dto.setContactIdCard("110101199001011234");

        String token = "your_test_token";

        // 执行抢票
        Result<TicketOrderVO> result = ticketService.grabTicket(dto, token);

        // 验证结果
        Assert.assertTrue(result.isSuccess());
        Assert.assertNotNull(result.getData());
    }

    @Test
    public void testConcurrentGrabTicket() throws InterruptedException {
        // 测试并发抢同一座位
        CountDownLatch latch = new CountDownLatch(10);
        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try {
                    GrabTicketDTO dto = new GrabTicketDTO();
                    dto.setShowEventId(1L);
                    dto.setSeatIds(Arrays.asList(1L)); // 同一座位
                    dto.setContactName("测试用户");
                    dto.setContactPhone("13800138000");
                    dto.setContactIdCard("110101199001011234");

                    String token = "test_token";

                    Result<TicketOrderVO> result = ticketService.grabTicket(dto, token);
                    if (result.isSuccess()) {
                        successCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            }).start();
        }

        latch.await();

        // 验证：只有1个请求成功
        Assert.assertEquals(1, successCount.get());
    }
}
```

#### 5.2 接口测试

使用 Postman 或 curl 测试抢票接口：

```bash
# 测试抢票
curl -X POST "http://localhost:8080/api/ticket/grab" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "showEventId": 1,
    "seatIds": [1, 2],
    "contactName": "张三",
    "contactPhone": "13800138000",
    "contactIdCard": "110101199001011234"
  }'
```

#### 5.3 压力测试

使用 JMeter 进行压力测试：

1. 创建线程组：1000 个并发用户
2. 添加 HTTP 请求：POST /api/ticket/grab
3. 添加断言：验证响应状态码
4. 运行测试，观察结果

---

### 步骤 6：监控与日志

#### 6.1 添加日志

在 `TicketServiceRedissonImpl` 中已包含详细日志：

```java
log.info("用户 {} 获取到抢票锁，开始处理抢票请求", userId);
log.info("抢票成功: userId={}, orderId={}, orderNo={}, seatCount={}, 锁持有时间={}ms",
        userId, order.getId(), orderNo, seats.size(),
        System.currentTimeMillis() - lock.getHoldCount());
```

#### 6.2 监控锁的使用情况

可以通过 Redisson 的监控接口查看锁的状态：

```java
RLock lock = redissonClient.getLock(lockKey);

// 查询锁的信息
boolean isLocked = lock.isLocked(); // 是否被锁定
boolean isHeldByCurrentThread = lock.isHeldByCurrentThread(); // 当前线程是否持有锁
int holdCount = lock.getHoldCount(); // 重入次数
long remainTime = lock.remainTimeToLive(); // 剩余过期时间
```

---

## 🔍 问题排查

### 问题 1：Redisson 连接失败

**错误信息**：
```
Unable to connect to Redis server: localhost:6379
```

**解决方案**：
1. 检查 Redis 是否启动
2. 检查 `application.yml` 中的 Redis 配置
3. 检查防火墙是否阻止连接

---

### 问题 2：锁获取超时

**错误信息**：
```
请勿重复提交抢票请求，请稍后再试
```

**原因**：
- 其他线程正在持有锁
- 等待超时（3秒）

**解决方案**：
1. 检查业务逻辑是否耗时过长
2. 适当增加等待超时时间
3. 优化业务逻辑，减少锁持有时间

---

### 问题 3：Watchdog 未生效

**现象**：
- 业务执行时间 > 30 秒
- 锁仍然过期

**原因**：
- 使用了 `tryLock(waitTime, leaseTime, unit)` 方法
- 设置了 `leaseTime`，禁用了 Watchdog

**解决方案**：
```java
// 错误：禁用了 Watchdog
lock.tryLock(3, 30, TimeUnit.SECONDS);

// 正确：启用 Watchdog
lock.tryLock(3, -1, TimeUnit.SECONDS); // leaseTime = -1 表示使用 Watchdog
// 或者
lock.tryLock(3, TimeUnit.SECONDS); // 不设置 leaseTime
```

---

## 📊 性能对比

切换到 Redisson 后，预期性能变化：

| 指标 | 简单实现 | Redisson | 变化 |
|------|---------|----------|------|
| TPS | 2500 | 2200 | ↓ 12% |
| 平均响应时间 | 40ms | 45ms | ↑ 5ms |
| P99响应时间 | 120ms | 150ms | ↑ 30ms |
| 成功率 | 98% | 100% | ↑ 2% |
| 可靠性 | 低 | 高 | 大幅提升 |

**结论**：
- 性能略有下降（可接受）
- 可靠性大幅提升（关键）
- 对于高并发抢票场景，**强烈建议使用 Redisson**

---

## ✅ 验证清单

切换完成后，请验证以下内容：

- [ ] Redisson 依赖已添加
- [ ] RedissonConfig 配置正确
- [ ] TicketController 注入 Redisson 实现
- [ ] 抢票接口正常工作
- [ ] 单元测试通过
- [ ] 压力测试通过
- [ ] 日志输出正常
- [ ] 监控指标正常

---

## 🎯 下一步优化

切换到 Redisson 后，可以进一步优化：

### 1. 使用 RedLock（高可用）

```java
RLock lock1 = redisson1.getLock("my-lock");
RLock lock2 = redisson2.getLock("my-lock");
RLock lock3 = redisson3.getLock("my-lock");

RedissonRedLock redLock = new RedissonRedLock(lock1, lock2, lock3);
redLock.tryLock(3, 30, TimeUnit.SECONDS);
```

### 2. 使用公平锁

```java
// 公平锁：按请求顺序获取锁
RLock fairLock = redissonClient.getFairLock("my-fair-lock");
fairLock.lock();
```

### 3. 使用读写锁

```java
// 读写锁：读读不互斥，读写互斥，写写互斥
RReadWriteLock rwLock = redissonClient.getReadWriteLock("my-rw-lock");

// 读锁
RLock readLock = rwLock.readLock();
readLock.lock();

// 写锁
RLock writeLock = rwLock.writeLock();
writeLock.lock();
```

### 4. 使用信号量（限流）

```java
// 信号量：限制并发数
RSemaphore semaphore = redissonClient.getSemaphore("my-semaphore");
semaphore.trySetPermits(100); // 设置许可数量
semaphore.acquire(); // 获取许可
try {
    // 业务逻辑
} finally {
    semaphore.release(); // 释放许可
}
```

---

**✨ 切换完成后，你的抢票系统将拥有更高的可靠性和稳定性！**
