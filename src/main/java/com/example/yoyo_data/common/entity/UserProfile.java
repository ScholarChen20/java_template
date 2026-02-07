package com.example.yoyo_data.common.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户档案表实体类
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)  // 忽略父类属性
@TableName(value = "user_profiles", autoResultMap = true)
public class UserProfile implements Serializable {
    /**
     * 用户档案ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     *  用户ID
     */
    @TableField("user_id")
    private Long userId;
    /**
     *  姓名
     */
    @TableField("full_name")
    private String fullName;
    /**
     * 性别
     */
    @TableField("gender")
    private String gender;
    /**
     *  出生日期
     */
    @TableField("birth_date")
    private LocalDate birthDate;
    /**
     *  居住地
     */
    @TableField("location")
    private String location;
    /**
     * 旅游偏好
     */
    @TableField(value = "travel_preferences", typeHandler = JacksonTypeHandler.class)
    private List<String> travelPreferences;
    /**
     * 访问过的城市
     */
    @TableField(value = "visited_cities", typeHandler = JacksonTypeHandler.class)
    private List<String> visitedCities;
    /**
     * 旅游统计
     */
    @TableField(value = "travel_stats", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> travelStats;
    /**
     * 创建时间
     */
    @TableField("created_at")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    /**
     * 更新时间
     */
    @TableField("updated_at")
    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}
