//package com.example.yoyo_data.infrastructure.base;
//
//import com.baomidou.mybatisplus.core.mapper.BaseMapper;
//import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//
///**
// * 基础服务实现类 - 所有服务实现类的父类
// * 提供通用CRUD操作的实现
// *
// * @param <M> Mapper类型
// * @param <T> 实体类型
// * @author Template Framework
// * @version 1.0
// */
//public abstract class BaseServiceImpl<M extends BaseMapper<T>, T extends BaseEntity> extends ServiceImpl<M, T> implements BaseService<T> {
//
//    /**
//     * 获取所有未删除的实体列表
//     * 如果实体不是BaseEntity，则返回所有记录
//     *
//     * @return 实体列表
//     */
//    @Override
//    public List<T> listAll() {
//        return this.list();
//    }
//
//    /**
//     * 保存或更新实体
//     * 保存前调用preSave方法初始化基础字段
//     *
//     * @param entity 实体对象
//     * @return 是否成功
//     */
//    @Override
//    @Transactional
//    public boolean saveOrUpdate(T entity) {
//        this.preUpdate(entity);
//        return super.saveOrUpdate(entity);
//    }
//
//    /**
//     * 批量保存实体
//     * 保存前调用preSave方法初始化基础字段
//     *
//     * @param entityList 实体列表
//     * @return 是否成功
//     */
//    @Override
//    @Transactional
//    public boolean saveBatch(List<T> entityList) {
//        for (T entity : entityList) {
//            this.preSave(entity);
//        }
//        return super.saveBatch(entityList);
//    }
//
//    /**
//     * 根据ID软删除实体
//     * 调用实体的softDelete方法标记为已删除
//     *
//     * @param id 实体ID
//     * @return 是否成功
//     */
//    @Override
//    @Transactional
//    public boolean softDeleteById(Object id) {
//        T entity = this.getById((Comparable<?>) id);
//        if (entity != null) {
//            if (entity instanceof BaseEntity) {
//                ((BaseEntity) entity).softDelete();
//                return this.updateById(entity);
//            }
//        }
//        return false;
//    }
//
//    /**
//     * 根据ID列表软删除实体
//     * 对列表中的每个ID调用softDeleteById方法
//     *
//     * @param idList ID列表
//     * @return 是否成功
//     */
//    @Override
//    @Transactional
//    public boolean softDeleteByIdList(List<?> idList) {
//        boolean success = true;
//        for (Object id : idList) {
//            if (!this.softDeleteById(id)) {
//                success = false;
//            }
//        }
//        return success;
//    }
//}
