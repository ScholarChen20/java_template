//package com.example.yoyo_data.infrastructure.base;
//
//import com.example.yoyo_data.common.Result;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
///**
// * 基础控制器类 - 所有控制器的父类
// * 提供通用的CRUD接口模板
// *
// * @param <T> 实体类型
// * @param <S> 服务类型
// * @author Template Framework
// * @version 1.0
// */
//@Slf4j
//public abstract class BaseController<T extends BaseEntity, S extends BaseService<T>> {
//
//    @Autowired
//    protected S baseService;
//
//    /**
//     * 获取所有记录
//     *
//     * @return 记录列表
//     */
//    @GetMapping
//    public Result<List<T>> listAll() {
//        try {
//            List<T> list = baseService.listAll();
//            return Result.success(list);
//        } catch (Exception e) {
//            log.error("获取记录列表失败", e);
//            return Result.error("获取记录列表失败");
//        }
//    }
//
//    /**
//     * 根据ID获取单个记录
//     *
//     * @param id 记录ID
//     * @return 记录详情
//     */
//    @GetMapping("/{id}")
//    public Result<T> getById(@PathVariable Long id) {
//        try {
//            if (id == null || id <= 0) {
//                return Result.badRequest("ID不能为空或小于等于0");
//            }
//            T entity = baseService.getById(id);
//            if (entity == null) {
//                return Result.notFound("记录不存在");
//            }
//            return Result.success(entity);
//        } catch (Exception e) {
//            log.error("获取记录失败, ID: {}", id, e);
//            return Result.error("获取记录失败");
//        }
//    }
//
//    /**
//     * 创建新记录
//     *
//     * @param entity 实体对象
//     * @return 创建结果
//     */
//    @PostMapping
//    public Result<T> create(@RequestBody T entity) {
//        try {
//            if (entity == null) {
//                return Result.badRequest("请求体不能为空");
//            }
//            baseService.preSave(entity);
//            boolean success = baseService.save(entity);
//            if (success) {
//                return Result.success(entity);
//            } else {
//                return Result.error("创建记录失败");
//            }
//        } catch (Exception e) {
//            log.error("创建记录失败", e);
//            return Result.error("创建记录失败");
//        }
//    }
//
//    /**
//     * 更新记录
//     *
//     * @param id 记录ID
//     * @param entity 实体对象
//     * @return 更新结果
//     */
//    @PutMapping("/{id}")
//    public Result<T> update(@PathVariable Long id, @RequestBody T entity) {
//        try {
//            if (id == null || id <= 0) {
//                return Result.badRequest("ID不能为空或小于等于0");
//            }
//            if (entity == null) {
//                return Result.badRequest("请求体不能为空");
//            }
//            T existing = baseService.getById(id);
//            if (existing == null) {
//                return Result.notFound("记录不存在");
//            }
//            entity.setId(id);
//            baseService.preUpdate(entity);
//            boolean success = baseService.updateById(entity);
//            if (success) {
//                return Result.success(entity);
//            } else {
//                return Result.error("更新记录失败");
//            }
//        } catch (Exception e) {
//            log.error("更新记录失败, ID: {}", id, e);
//            return Result.error("更新记录失败");
//        }
//    }
//
//    /**
//     * 删除记录（硬删除）
//     *
//     * @param id 记录ID
//     * @return 删除结果
//     */
//    @DeleteMapping("/{id}")
//    public Result<Void> delete(@PathVariable Long id) {
//        try {
//            if (id == null || id <= 0) {
//                return Result.badRequest("ID不能为空或小于等于0");
//            }
//            T existing = baseService.getById(id);
//            if (existing == null) {
//                return Result.notFound("记录不存在");
//            }
//            boolean success = baseService.removeById(id);
//            if (success) {
//                return Result.success();
//            } else {
//                return Result.error("删除记录失败");
//            }
//        } catch (Exception e) {
//            log.error("删除记录失败, ID: {}", id, e);
//            return Result.error("删除记录失败");
//        }
//    }
//
//    /**
//     * 软删除记录（标记为已删除）
//     *
//     * @param id 记录ID
//     * @return 删除结果
//     */
//    @DeleteMapping("/soft/{id}")
//    public Result<Void> softDelete(@PathVariable Long id) {
//        try {
//            if (id == null || id <= 0) {
//                return Result.badRequest("ID不能为空或小于等于0");
//            }
//            T existing = baseService.getById(id);
//            if (existing == null) {
//                return Result.notFound("记录不存在");
//            }
//            boolean success = baseService.softDeleteById(id);
//            if (success) {
//                return Result.success();
//            } else {
//                return Result.error("删除记录失败");
//            }
//        } catch (Exception e) {
//            log.error("删除记录失败, ID: {}", id, e);
//            return Result.error("删除记录失败");
//        }
//    }
//
//    /**
//     * 批量删除记录（硬删除）
//     *
//     * @param ids ID列表
//     * @return 删除结果
//     */
//    @PostMapping("/batch-delete")
//    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
//        try {
//            if (ids == null || ids.isEmpty()) {
//                return Result.badRequest("ID列表不能为空");
//            }
//            boolean success = baseService.removeByIds(ids);
//            if (success) {
//                return Result.success();
//            } else {
//                return Result.error("批量删除失败");
//            }
//        } catch (Exception e) {
//            log.error("批量删除失败", e);
//            return Result.error("批量删除失败");
//        }
//    }
//
//    /**
//     * 批量软删除记录
//     *
//     * @param ids ID列表
//     * @return 删除结果
//     */
//    @PostMapping("/batch-soft-delete")
//    public Result<Void> batchSoftDelete(@RequestBody List<Long> ids) {
//        try {
//            if (ids == null || ids.isEmpty()) {
//                return Result.badRequest("ID列表不能为空");
//            }
//            boolean success = baseService.softDeleteByIdList(ids);
//            if (success) {
//                return Result.success();
//            } else {
//                return Result.error("批量删除失败");
//            }
//        } catch (Exception e) {
//            log.error("批量删除失败", e);
//            return Result.error("批量删除失败");
//        }
//    }
//}
