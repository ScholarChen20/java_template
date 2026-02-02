package com.example.yoyo_data.infrastructure.base;

import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * 基础服务接口 - 所有服务接口的父接口
 * 提供通用的CRUD操作定义
 *
 * @param <T> 实体类型
 * @author Template Framework
 * @version 1.0
 */
public interface BaseService<T> extends IService<T> {

    /**
     * 保存实体前初始化基础字段
     *
     * @param entity 实体对象
     */
    default void preSave(T entity) {
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).initializeBase();
        }
    }

    /**
     * 更新实体前更新基础字段
     *
     * @param entity 实体对象
     */
    default void preUpdate(T entity) {
        if (entity instanceof BaseEntity) {
            ((BaseEntity) entity).updateBase();
        }
    }

    /**
     * 根据ID获取实体（排除已删除记录）
     *
     * @param id 实体ID
     * @return 实体对象
     */
    T getById(Object id);

    /**
     * 获取所有未删除的实体列表
     *
     * @return 实体列表
     */
    List<T> listAll();

    /**
     * 保存或更新实体
     *
     * @param entity 实体对象
     * @return 是否成功
     */
    boolean saveOrUpdate(T entity);

    /**
     * 批量保存实体
     *
     * @param entityList 实体列表
     * @return 是否成功
     */
    boolean saveBatch(List<T> entityList);

    /**
     * 根据ID删除实体（硬删除）
     *
     * @param id 实体ID
     * @return 是否成功
     */
    boolean removeById(Object id);

    /**
     * 根据ID软删除实体
     *
     * @param id 实体ID
     * @return 是否成功
     */
    boolean softDeleteById(Object id);

    /**
     * 根据ID列表软删除实体
     *
     * @param idList ID列表
     * @return 是否成功
     */
    boolean softDeleteByIdList(List<?> idList);
}
