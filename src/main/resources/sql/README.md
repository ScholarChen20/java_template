# 演唱会抢票系统 - 数据库初始化指南

## 📋 文件说明

本目录包含演唱会抢票系统的数据库初始化脚本：

| 文件名 | 说明 | 执行顺序 |
|--------|------|---------|
| `ticket_system_schema.sql` | 创建5张核心业务表 | ① 第一步执行 |
| `ticket_system_init_data.sql` | 初始化演出活动和座位数据 | ② 第二步执行 |
| `ticket_system_verify.sql` | 验证数据完整性和准确性 | ③ 第三步执行 |

---

## 🗂️ 数据库表结构

### 1. tb_show_event (演出活动表)
- **用途**: 存储演唱会、话剧、电影等演出活动信息
- **关键字段**: 开票时间、总座位数、可售座位数、限购数量、乐观锁版本号
- **索引**: show_time, sale_start_time, status

### 2. tb_seat (座位信息表)
- **用途**: 存储每个演出的所有座位详情
- **关键字段**: 座位编码(唯一)、票价、状态(可售/锁定/已售)、锁定过期时间、乐观锁版本号
- **索引**: show_event_id+status, lock_expire_time, order_id
- **核心**: 通过乐观锁version字段防止超卖

### 3. tb_ticket_order (票务订单表)
- **用途**: 存储用户的抢票订单
- **关键字段**: 订单编号(唯一)、订单状态、过期时间、联系人信息
- **订单状态**: PENDING(待支付) / PAID(已支付) / CANCELLED(已取消) / TIMEOUT(超时取消)
- **索引**: user_id, show_event_id, status, expire_time

### 4. tb_order_seat (订单座位关联表)
- **用途**: 订单与座位的多对多关联关系
- **关键字段**: order_id, seat_id, seat_code, price

### 5. tb_user_ticket_record (用户购票记录表)
- **用途**: 记录用户对每场演出的购票数量，用于限购控制
- **唯一约束**: (user_id, show_event_id)

---

## 🚀 执行步骤

### 方式一：命令行执行（推荐）

```bash
# 1. 登录MySQL
mysql -u root -p

# 2. 选择或创建数据库
CREATE DATABASE IF NOT EXISTS yoyo_data DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE yoyo_data;

# 3. 执行建表脚本
SOURCE D:/JavaPro/yoyo_data/src/main/resources/sql/ticket_system_schema.sql;

# 4. 执行初始化数据脚本
SOURCE D:/JavaPro/yoyo_data/src/main/resources/sql/ticket_system_init_data.sql;

# 5. 执行验证脚本
SOURCE D:/JavaPro/yoyo_data/src/main/resources/sql/ticket_system_verify.sql;
```

### 方式二：使用Navicat/DataGrip等工具

1. 打开工具并连接到MySQL数据库
2. 选择或创建 `yoyo_data` 数据库
3. 依次执行三个SQL文件（按顺序）
4. 查看执行结果

---

## 📊 初始化数据说明

### 演出活动（5场）

| ID | 演出名称 | 类型 | 座位数 | 限购 | 票价范围 | 开票时间 |
|----|---------|------|--------|------|---------|---------|
| 1 | 周杰伦2026世界巡回演唱会-北京站 | 演唱会 | 8000 | 2张 | 380-1280元 | 2026-03-01 10:00 |
| 2 | 五月天"诺亚方舟"演唱会-上海站 | 演唱会 | 6000 | 2张 | 280-1080元 | 2026-02-20 10:00 |
| 3 | 薛之谦"天外来物"演唱会-深圳站 | 演唱会 | 5000 | 2张 | 380-880元 | 2026-03-05 12:00 |
| 4 | 开心麻花《乌龙山伯爵》 | 话剧 | 500 | 4张 | 180-580元 | 2026-02-10 10:00 |
| 5 | 《流浪地球3》全球首映礼 | 电影 | 300 | 2张 | 200-500元 | 2026-02-08 10:00 |

### 座位数据（详细生成）

#### 周杰伦演唱会（8000座）
- **VIP区**: 10排 × 50座 = 500座，1280元/座
- **A区**: 20排 × 60座 = 1200座，880元/座
- **B区**: 30排 × 70座 = 2100座，580元/座
- **C区**: 40排 × 105座 = 4200座，380元/座

#### 五月天演唱会（6000座）
- **VIP区**: 8排 × 40座 = 320座，1080元/座
- **A区**: 20排 × 50座 = 1000座，680元/座
- **B区**: 30排 × 60座 = 1800座，480元/座
- **C区**: 40排 × 72座 = 2880座，280元/座

> **注意**: 其他3场演出的座位数据可按需生成（可以使用相同的SQL模式）

---

## ✅ 验证检查项

执行 `ticket_system_verify.sql` 后，检查以下项目：

1. ✅ **表结构验证**: 5张表都已创建
2. ✅ **演出数据验证**: 5场演出信息完整
3. ✅ **座位数据统计**: 各区域座位数量和价格正确
4. ✅ **座位编码唯一性**: 无重复座位编码
5. ✅ **座位总数验证**: 实际座位数 = 声明的总座位数
6. ✅ **索引完整性**: 所有必要索引都已创建
7. ✅ **空表检查**: 订单相关表为空

---

## 🔍 常见问题

### Q1: 执行初始化数据脚本时间较长？
**A**: 正常现象。周杰伦演唱会8000座 + 五月天演唱会6000座，共需插入14000条座位数据，预计需要5-10秒。

### Q2: 如何清空所有数据重新初始化？
**A**: 依次执行：
```sql
-- 删除所有表（会自动级联删除数据）
DROP TABLE IF EXISTS tb_order_seat;
DROP TABLE IF EXISTS tb_user_ticket_record;
DROP TABLE IF EXISTS tb_ticket_order;
DROP TABLE IF EXISTS tb_seat;
DROP TABLE IF EXISTS tb_show_event;

-- 然后重新执行三个SQL脚本
```

### Q3: 如何生成其他演出的座位数据？
**A**: 参考 `ticket_system_init_data.sql` 中的座位生成SQL，修改以下参数：
- `show_event_id`: 演出活动ID
- `seat_zone`: 座位区域名称
- `price`: 票价
- `LIMIT`: 座位数量

### Q4: 座位编码规则是什么？
**A**: 格式为 `{区域}-{排号}-{座位号}`，例如：
- `VIP-1-5`: VIP区第1排第5号
- `A-10-30`: A区第10排第30号

---

## 📈 性能优化建议

### 索引优化
```sql
-- 如果查询座位时性能不佳，可以添加复合索引
ALTER TABLE tb_seat ADD INDEX idx_show_zone_status (show_event_id, seat_zone, status);

-- 如果按价格区间查询较多
ALTER TABLE tb_seat ADD INDEX idx_show_price (show_event_id, price);
```

### 分区优化（可选，数据量大时）
```sql
-- 按演出活动ID进行分区（适用于座位表数据量超过百万时）
ALTER TABLE tb_seat
PARTITION BY HASH(show_event_id)
PARTITIONS 10;
```

---

## 🎯 下一步

数据库初始化完成后，可以进行：

1. ✅ **第二步**: 创建实体类（Entity）和DTO/VO对象
2. ✅ **第三步**: 实现Mapper接口（MyBatis-Plus）
3. ✅ **第四步**: 实现核心业务Service
4. ✅ **第五步**: 实现Controller接口
5. ✅ **第六步**: 压力测试和优化

---

## 📝 数据字典

详细的数据字典可以通过以下SQL生成：

```sql
SELECT
    TABLE_NAME AS '表名',
    COLUMN_NAME AS '字段名',
    COLUMN_TYPE AS '数据类型',
    IS_NULLABLE AS '是否可空',
    COLUMN_DEFAULT AS '默认值',
    COLUMN_COMMENT AS '注释'
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA = 'yoyo_data'
AND TABLE_NAME LIKE 'tb_%'
ORDER BY TABLE_NAME, ORDINAL_POSITION;
```

---

**✨ 初始化完成后，系统已具备 8000+ 座位数据，可支持高并发抢票测试！**
