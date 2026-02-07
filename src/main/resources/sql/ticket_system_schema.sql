-- ========================================
-- 演唱会抢票系统 - 数据库表结构
-- Author: YoYo Data Team
-- Date: 2026-02-07
-- ========================================

-- 1. 演出活动表
DROP TABLE IF EXISTS tb_show_event;
CREATE TABLE tb_show_event (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '活动ID',
    show_name VARCHAR(200) NOT NULL COMMENT '演出名称',
    show_type VARCHAR(50) NOT NULL COMMENT '演出类型：CONCERT-演唱会, MOVIE-电影, DRAMA-话剧',
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    venue_name VARCHAR(200) NOT NULL COMMENT '场馆名称',
    show_time DATETIME NOT NULL COMMENT '演出时间',
    sale_start_time DATETIME NOT NULL COMMENT '开票时间',
    sale_end_time DATETIME NOT NULL COMMENT '结束售票时间',
    total_seats INT NOT NULL DEFAULT 0 COMMENT '总座位数',
    available_seats INT NOT NULL DEFAULT 0 COMMENT '可售座位数',
    locked_seats INT NOT NULL DEFAULT 0 COMMENT '锁定座位数',
    sold_seats INT NOT NULL DEFAULT 0 COMMENT '已售座位数',
    max_buy_limit INT NOT NULL DEFAULT 2 COMMENT '每人限购数量',
    poster_url VARCHAR(500) COMMENT '海报图片URL',
    description TEXT COMMENT '演出介绍',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待开票, SELLING-售票中, SOLD_OUT-售罄, ENDED-已结束',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_show_time (show_time),
    INDEX idx_sale_start_time (sale_start_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='演出活动表';

-- 2. 座位信息表
DROP TABLE IF EXISTS tb_seat;
CREATE TABLE tb_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '座位ID',
    show_event_id BIGINT NOT NULL COMMENT '演出活动ID',
    seat_zone VARCHAR(50) NOT NULL COMMENT '座位区域：VIP, A, B, C',
    seat_row INT NOT NULL COMMENT '排号',
    seat_number INT NOT NULL COMMENT '座位号',
    seat_code VARCHAR(50) NOT NULL COMMENT '座位编码：如VIP-1-5',
    price DECIMAL(10,2) NOT NULL COMMENT '票价',
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE' COMMENT '状态：AVAILABLE-可售, LOCKED-已锁定, SOLD-已售出',
    lock_time DATETIME COMMENT '锁定时间',
    lock_expire_time DATETIME COMMENT '锁定过期时间',
    order_id BIGINT COMMENT '订单ID',
    user_id BIGINT COMMENT '用户ID',
    version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_seat (show_event_id, seat_code),
    INDEX idx_show_status (show_event_id, status),
    INDEX idx_lock_expire (lock_expire_time),
    INDEX idx_order_id (order_id),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='座位信息表';

-- 3. 票务订单表
DROP TABLE IF EXISTS tb_ticket_order;
CREATE TABLE tb_ticket_order (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    show_event_id BIGINT NOT NULL COMMENT '演出活动ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    seat_count INT NOT NULL COMMENT '购买座位数量',
    total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待支付, PAID-已支付, CANCELLED-已取消, TIMEOUT-超时取消',
    pay_type VARCHAR(20) COMMENT '支付方式：ALIPAY, WECHAT, CARD',
    pay_time DATETIME COMMENT '支付时间',
    expire_time DATETIME NOT NULL COMMENT '订单过期时间',
    contact_name VARCHAR(100) NOT NULL COMMENT '联系人姓名',
    contact_phone VARCHAR(20) NOT NULL COMMENT '联系人手机',
    contact_id_card VARCHAR(20) NOT NULL COMMENT '联系人身份证',
    remark TEXT COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_show_event_id (show_event_id),
    INDEX idx_status (status),
    INDEX idx_expire_time (expire_time),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='票务订单表';

-- 4. 订单座位关联表
DROP TABLE IF EXISTS tb_order_seat;
CREATE TABLE tb_order_seat (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    order_id BIGINT NOT NULL COMMENT '订单ID',
    seat_id BIGINT NOT NULL COMMENT '座位ID',
    show_event_id BIGINT NOT NULL COMMENT '演出活动ID',
    seat_code VARCHAR(50) NOT NULL COMMENT '座位编码',
    price DECIMAL(10,2) NOT NULL COMMENT '座位价格',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_order_id (order_id),
    INDEX idx_seat_id (seat_id),
    INDEX idx_show_event_id (show_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单座位关联表';

-- 5. 用户购票记录表
DROP TABLE IF EXISTS tb_user_ticket_record;
CREATE TABLE tb_user_ticket_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    show_event_id BIGINT NOT NULL COMMENT '演出活动ID',
    ticket_count INT NOT NULL DEFAULT 0 COMMENT '已购票数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_user_show (user_id, show_event_id),
    INDEX idx_show_event_id (show_event_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户购票记录表';

-- 表创建完成
SELECT '✅ 数据库表创建完成！' AS message;
