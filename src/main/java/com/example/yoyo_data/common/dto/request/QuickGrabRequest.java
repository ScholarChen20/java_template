package com.example.yoyo_data.common.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.*;
import java.util.List;

/**
 * 快速抢票请求DTO
 *
 * 适用场景：大型演唱会/演出开票瞬间的高并发抢票。
 * 用户只需选择价位区域（如 880元=A区），系统自动分配座位，
 * 演出前一周才会显示具体座位号（对应大麦网快速购票模式）。
 *
 * 区域与票价对应示例：
 *   VIP区 → ¥1580
 *   A 区  → ¥880
 *   B 区  → ¥580
 *   C 区  → ¥280
 */
@Data
@ApiModel(description = "快速抢票请求（系统自动分配座位）")
public class QuickGrabRequest {

    @ApiModelProperty(value = "演出活动ID", required = true, example = "1")
    @NotNull(message = "演出活动ID不能为空")
    private Long showEventId;

    @ApiModelProperty(value = "座位区域（价位档位），如 VIP/A/B/C，对应不同票价",
            required = true, example = "A")
    @NotBlank(message = "座位区域不能为空")
    private String seatZone;

    @ApiModelProperty(value = "购票数量，必须与观影人列表数量一致",
            required = true, example = "2")
    @NotNull(message = "购票数量不能为空")
    @Min(value = 1, message = "购票数量最少为1张")
    @Max(value = 4, message = "购票数量最多为4张（单次限购）")
    private Integer seatCount;

    @ApiModelProperty(value = "是否优先分配连座（默认true，适合朋友/家庭同行）",
            example = "true")
    private Boolean preferContinuous = true;

    @ApiModelProperty(value = "观影人列表，数量必须与 seatCount 一致，每人需提供真实姓名和身份证",
            required = true)
    @NotEmpty(message = "观影人列表不能为空")
    @Size(min = 1, max = 4, message = "观影人数量为1-4人")
    @Valid
    private List<TicketUser> ticketUsers;

    /**
     * 观影人信息（每张票对应一个观影人，实名制入场）
     */
    @Data
    @ApiModel(description = "观影人信息")
    public static class TicketUser {

        @ApiModelProperty(value = "观影人真实姓名", required = true, example = "张三")
        @NotBlank(message = "观影人姓名不能为空")
        @Size(min = 2, max = 50, message = "姓名长度为2-50个字符")
        private String contactName;

        @ApiModelProperty(value = "观影人手机号", required = true, example = "13800138000")
        @NotBlank(message = "观影人手机不能为空")
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String contactPhone;

        @ApiModelProperty(value = "观影人身份证号（实名制）", required = true, example = "110101199001011234")
        @NotBlank(message = "观影人身份证不能为空")
        @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$",
                message = "身份证号格式不正确")
        private String contactIdCard;
    }
}
