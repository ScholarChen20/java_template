package com.example.yoyo_data.infrastructure.base;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类 - 所有实体的父类
 * 提供通用的ID、创建时间、更新时间字段
 *
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID - 使用自增策略
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间 - 记录数据创建的时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间 - 记录数据最后更新的时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;

    /**
     * 是否删除 - 标记软删除状态 (0: 未删除, 1: 已删除)
     */
    @TableField("is_deleted")
    private Integer isDeleted;

    /**
     * 初始化基础实体 - 创建时自动设置创建时间和更新时间
     * 应在 insert 前调用此方法
     */
    public void initializeBase() {
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
        if (this.isDeleted == null) {
            this.isDeleted = 0;
        }
    }

    /**
     * 更新基础实体 - 更新时自动设置更新时间
     * 应在 update 前调用此方法
     */
    public void updateBase() {
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 软删除 - 标记为已删除
     */
    public void softDelete() {
        this.isDeleted = 1;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * 检查是否已删除
     *
     * @return true: 已删除, false: 未删除
     */
    public boolean isDeleted() {
        return this.isDeleted != null && this.isDeleted == 1;
    }
}
