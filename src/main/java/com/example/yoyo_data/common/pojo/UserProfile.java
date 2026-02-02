package com.example.yoyo_data.common.pojo;

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
@EqualsAndHashCode(callSuper = false)
@TableName(value = "user_profiles", autoResultMap = true)
public class UserProfile implements Serializable {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("user_id")
    private Long userId;

    @TableField("full_name")
    private String fullName;

    @TableField("gender")
    private String gender;

    @TableField("birth_date")
    private LocalDate birthDate;

    @TableField("location")
    private String location;

    @TableField(value = "travel_preferences", typeHandler = JacksonTypeHandler.class)
    private List<String> travelPreferences;

    @TableField(value = "visited_cities", typeHandler = JacksonTypeHandler.class)
    private List<String> visitedCities;

    @TableField(value = "travel_stats", typeHandler = JacksonTypeHandler.class)
    private Map<String, Object> travelStats;

    @TableField("created_at")
    private LocalDateTime createdAt;

    @TableField("updated_at")
    private LocalDateTime updatedAt;
}
