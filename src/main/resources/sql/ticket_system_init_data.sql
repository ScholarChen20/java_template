-- ========================================
-- 演唱会抢票系统 - 初始化数据
-- Author: YoYo Data Team
-- Date: 2026-02-07
-- ========================================

-- ========================================
-- 1. 插入演出活动数据
-- ========================================
USE travel_agent;
INSERT INTO tb_show_event
(id, show_name, show_type, venue_id, venue_name, show_time, sale_start_time, sale_end_time,
 total_seats, available_seats, locked_seats, sold_seats, max_buy_limit, poster_url, description, status)
VALUES
-- 周杰伦演唱会（热门，8000座）
(1, '周杰伦2026世界巡回演唱会-北京站', 'CONCERT', 1, '国家体育场（鸟巢）',
 '2026-05-01 19:30:00', '2026-03-01 10:00:00', '2026-04-30 23:59:59',
 8000, 8000, 0, 0, 2,
 'https://example.com/posters/jay_concert.jpg',
 '周杰伦2026世界巡回演唱会北京站，经典歌曲全新演绎，一场不容错过的音乐盛宴！',
 'PENDING'),

-- 五月天演唱会（热门，6000座）
(2, '五月天"诺亚方舟"演唱会-上海站', 'CONCERT', 2, '梅赛德斯-奔驰文化中心',
 '2026-04-15 19:00:00', '2026-02-20 10:00:00', '2026-04-14 23:59:59',
 6000, 6000, 0, 0, 2,
 'https://example.com/posters/mayday_concert.jpg',
 '五月天"诺亚方舟"世界巡回演唱会，陪你一起追逐梦想！',
 'PENDING'),

-- 薛之谦演唱会（中热度，5000座）
(3, '薛之谦"天外来物"巡回演唱会-深圳站', 'CONCERT', 3, '深圳湾体育中心',
 '2026-04-20 19:30:00', '2026-03-05 12:00:00', '2026-04-19 23:59:59',
 5000, 5000, 0, 0, 2,
 'https://example.com/posters/joker_concert.jpg',
 '薛之谦"天外来物"演唱会，用音乐讲述不一样的故事。',
 'PENDING'),

-- 话剧（小型，500座）
(4, '开心麻花爆笑舞台剧《乌龙山伯爵》', 'DRAMA', 4, '保利剧院',
 '2026-03-15 19:30:00', '2026-02-10 10:00:00', '2026-03-14 23:59:59',
 500, 500, 0, 0, 4,
 'https://example.com/posters/drama.jpg',
 '开心麻花经典爆笑舞台剧，笑到停不下来！',
 'PENDING'),

-- 电影首映礼（小型，300座）
(5, '《流浪地球3》全球首映礼', 'MOVIE', 5, 'CGV影城-朝阳大悦城店',
 '2026-02-15 19:00:00', '2026-02-08 10:00:00', '2026-02-14 23:59:59',
 300, 300, 0, 0, 2,
 'https://example.com/posters/movie_premiere.jpg',
 '《流浪地球3》全球首映礼，导演郭帆携主创团队亲临现场！',
 'SELLING');

-- ========================================
-- 2. 生成座位数据（周杰伦演唱会 - 8000座）
-- ========================================

-- VIP区：10排，每排50座，票价1280元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    1 AS show_event_id,
    'VIP' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('VIP-', row_num, '-', seat_num) AS seat_code,
    1280.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 50 + 1 AS row_num,
        (@row_num) MOD 50 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3
    LIMIT 500
) AS seats;

-- A区：20排，每排60座，票价880元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    1 AS show_event_id,
    'A' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('A-', row_num, '-', seat_num) AS seat_code,
    880.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 60 + 1 AS row_num,
        (@row_num) MOD 60 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) AS t4
    LIMIT 1200
) AS seats;

-- B区：30排，每排70座，票价580元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    1 AS show_event_id,
    'B' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('B-', row_num, '-', seat_num) AS seat_code,
    580.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 70 + 1 AS row_num,
        (@row_num) MOD 70 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) AS t4
    LIMIT 2100
) AS seats;

-- C区：40排，每排105座，票价380元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    1 AS show_event_id,
    'C' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('C-', row_num, '-', seat_num) AS seat_code,
    380.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 105 + 1 AS row_num,
        (@row_num) MOD 105 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t4,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) AS t5
    LIMIT 4200
) AS seats;

-- ========================================
-- 3. 生成座位数据（五月天演唱会 - 6000座）
-- ========================================

-- VIP区：8排，每排40座，票价1080元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    2 AS show_event_id,
    'VIP' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('VIP-', row_num, '-', seat_num) AS seat_code,
    1080.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 40 + 1 AS row_num,
        (@row_num) MOD 40 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) AS t3
    LIMIT 320
) AS seats;

-- A区：20排，每排50座，票价680元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    2 AS show_event_id,
    'A' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('A-', row_num, '-', seat_num) AS seat_code,
    680.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 50 + 1 AS row_num,
        (@row_num) MOD 50 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2) AS t4
    LIMIT 1000
) AS seats;

-- B区：30排，每角60座，票价480元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    2 AS show_event_id,
    'B' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('B-', row_num, '-', seat_num) AS seat_code,
    480.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 60 + 1 AS row_num,
        (@row_num) MOD 60 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8) AS t4
    LIMIT 1800
) AS seats;

-- C区：40排，每排72座，票价280元
INSERT INTO tb_seat (show_event_id, seat_zone, seat_row, seat_number, seat_code, price, status)
SELECT
    2 AS show_event_id,
    'C' AS seat_zone,
    row_num AS seat_row,
    seat_num AS seat_number,
    CONCAT('C-', row_num, '-', seat_num) AS seat_code,
    280.00 AS price,
    'AVAILABLE' AS status
FROM (
    SELECT
        (@row_num := @row_num + 1) DIV 72 + 1 AS row_num,
        (@row_num) MOD 72 + 1 AS seat_num
    FROM
        (SELECT @row_num := -1) AS init,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t1,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t2,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t3,
        (SELECT 0 UNION ALL SELECT 1 UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4 UNION ALL SELECT 5 UNION ALL SELECT 6 UNION ALL SELECT 7 UNION ALL SELECT 8 UNION ALL SELECT 9) AS t4
    LIMIT 2880
) AS seats;

-- ========================================
-- 数据初始化完成
-- ========================================

-- 验证数据
SELECT '✅ 演出活动数据初始化完成！' AS message;
SELECT CONCAT('周杰伦演唱会座位数: ', COUNT(*)) AS seat_count FROM tb_seat WHERE show_event_id = 1;
SELECT CONCAT('五月天演唱会座位数: ', COUNT(*)) AS seat_count FROM tb_seat WHERE show_event_id = 2;
SELECT '提示：其他演出的座位数据可按需生成' AS notice;
