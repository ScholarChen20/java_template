package com.example.yoyo_data.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 抢票请求对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrabTicketDTO {

    /**
     * 演出活动ID
     */
    @NotNull(message = "演出活动ID不能为空")
    private Long showEventId;

    /**
     * 座位ID列表（用户选择的座位）
     */
    @NotEmpty(message = "座位列表不能为空")
    @Size(min = 1, max = 4, message = "每次最多选择4个座位")
    private List<Long> seatIds;

    /**
     * 座位区域（可选，用于快速抢票模式）
     */
    private String seatZone;

    /**
     * 座位数量（快速抢票模式：系统自动分配座位）
     */
    private Integer seatCount;

    /**
     * 联系人姓名
     */
    @NotNull(message = "联系人姓名不能为空")
    @Size(min = 2, max = 50, message = "联系人姓名长度为2-50个字符")
    private String contactName;

    /**
     * 联系人手机
     */
    @NotNull(message = "联系人手机不能为空")
    private String contactPhone;

    /**
     * 联系人身份证
     */
    @NotNull(message = "联系人身份证不能为空")
    private String contactIdCard;
}
