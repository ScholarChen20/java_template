package com.example.yoyo_data.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 座位信息表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@TableName(value = "tb_seat")
public class Seat implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 座位ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 演出活动ID
     */
    @TableField("show_event_id")
    private Long showEventId;

    /**
     * 座位区域：VIP, A, B, C
     */
    @TableField("seat_zone")
    private String seatZone;

    /**
     * 排号
     */
    @TableField("seat_row")
    private Integer seatRow;

    /**
     * 座位号
     */
    @TableField("seat_number")
    private Integer seatNumber;

    /**
     * 座位编码：如VIP-1-5
     */
    @TableField("seat_code")
    private String seatCode;

    /**
     * 票价
     */
    @TableField("price")
    private BigDecimal price;

    /**
     * 状态：AVAILABLE-可售, LOCKED-已锁定, SOLD-已售出
     */
    @TableField("status")
    private String status;

    /**
     * 锁定时间
     */
    @TableField("lock_time")
    private LocalDateTime lockTime;

    /**
     * 锁定过期时间
     */
    @TableField("lock_expire_time")
    private LocalDateTime lockExpireTime;

    /**
     * 订单ID
     */
    @TableField("order_id")
    private Long orderId;

    /**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 乐观锁版本号
     */
    @TableField("version")
    private Integer version;

    /**
     * 创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
