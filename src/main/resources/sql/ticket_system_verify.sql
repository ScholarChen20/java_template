-- ========================================
-- 演唱会抢票系统 - 数据验证SQL
-- Author: YoYo Data Team
-- Date: 2026-02-07
-- ========================================

-- 1. 验证表结构
SELECT '========== 表结构验证 ==========' AS step;
SELECT TABLE_NAME, TABLE_COMMENT, TABLE_ROWS
FROM information_schema.TABLES
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE 'tb_%'
ORDER BY TABLE_NAME;

-- 2. 验证演出活动数据
SELECT '========== 演出活动数据验证 ==========' AS step;
SELECT
    id,
    show_name,
    show_type,
    venue_name,
    DATE_FORMAT(show_time, '%Y-%m-%d %H:%i') AS show_time,
    DATE_FORMAT(sale_start_time, '%Y-%m-%d %H:%i') AS sale_start_time,
    total_seats,
    available_seats,
    max_buy_limit,
    status
FROM tb_show_event
ORDER BY id;

-- 3. 验证座位数据（按演出和区域统计）
SELECT '========== 座位数据统计 ==========' AS step;
SELECT
    se.show_name,
    s.seat_zone,
    COUNT(*) AS seat_count,
    MIN(s.price) AS min_price,
    MAX(s.price) AS max_price,
    s.status
FROM tb_seat s
JOIN tb_show_event se ON s.show_event_id = se.id
GROUP BY se.show_name, s.seat_zone, s.status
ORDER BY se.id, s.seat_zone;

-- 4. 验证座位编码唯一性
SELECT '========== 座位编码唯一性验证 ==========' AS step;
SELECT
    show_event_id,
    seat_code,
    COUNT(*) AS duplicate_count
FROM tb_seat
GROUP BY show_event_id, seat_code
HAVING COUNT(*) > 1;
-- 如果返回空结果，说明没有重复

-- 5. 验证座位总数
SELECT '========== 座位总数验证 ==========' AS step;
SELECT
    se.id,
    se.show_name,
    se.total_seats AS declared_total,
    COUNT(s.id) AS actual_total,
    IF(se.total_seats = COUNT(s.id), '✅ 一致', '❌ 不一致') AS status
FROM tb_show_event se
LEFT JOIN tb_seat s ON se.id = s.show_event_id
GROUP BY se.id, se.show_name, se.total_seats;

-- 6. 查看座位样例数据（周杰伦演唱会，每个区域前5个座位）
SELECT '========== 座位样例数据（周杰伦演唱会） ==========' AS step;
(SELECT seat_zone, seat_code, price, status FROM tb_seat WHERE show_event_id = 1 AND seat_zone = 'VIP' LIMIT 5)
UNION ALL
(SELECT seat_zone, seat_code, price, status FROM tb_seat WHERE show_event_id = 1 AND seat_zone = 'A' LIMIT 5)
UNION ALL
(SELECT seat_zone, seat_code, price, status FROM tb_seat WHERE show_event_id = 1 AND seat_zone = 'B' LIMIT 5)
UNION ALL
(SELECT seat_zone, seat_code, price, status FROM tb_seat WHERE show_event_id = 1 AND seat_zone = 'C' LIMIT 5);

-- 7. 检查索引
SELECT '========== 索引检查 ==========' AS step;
SELECT
    TABLE_NAME,
    INDEX_NAME,
    COLUMN_NAME,
    SEQ_IN_INDEX,
    NON_UNIQUE
FROM information_schema.STATISTICS
WHERE TABLE_SCHEMA = DATABASE()
AND TABLE_NAME LIKE 'tb_%'
ORDER BY TABLE_NAME, INDEX_NAME, SEQ_IN_INDEX;

-- 8. 检查空表
SELECT '========== 空表检查 ==========' AS step;
SELECT '订单表应为空（初始化时）' AS notice;
SELECT COUNT(*) AS order_count FROM tb_ticket_order;
SELECT COUNT(*) AS order_seat_count FROM tb_order_seat;
SELECT COUNT(*) AS user_record_count FROM tb_user_ticket_record;

SELECT '✅ 数据验证完成！' AS final_message;
