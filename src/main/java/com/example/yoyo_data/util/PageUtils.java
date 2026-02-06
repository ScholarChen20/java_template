package com.example.yoyo_data.util;

import com.example.yoyo_data.infrastructure.base.BasePage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * 分页工具类 - 提供分页相关的工具方法
 *
 * @author Template Framework
 * @version 1.0
 */
public class PageUtils {

    /**
     * 默认每页大小
     */
    private static final long DEFAULT_PAGE_SIZE = 10;

    /**
     * 最大每页大小
     */
    private static final long MAX_PAGE_SIZE = 1000;

    /**
     * 创建MyBatis-Plus分页对象
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param <T> 数据类型
     * @return Page对象
     */
    public static <T> Page<T> createPage(long pageNum, long pageSize) {
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
        return new Page<>(pageNum, pageSize);
    }

    /**
     * 创建MyBatis-Plus分页对象（使用默认大小）
     *
     * @param pageNum 页码（从1开始）
     * @param <T> 数据类型
     * @return Page对象
     */
    public static <T> Page<T> createPage(long pageNum) {
        return createPage(pageNum, DEFAULT_PAGE_SIZE);
    }

    /**
     * 创建默认分页对象
     *
     * @param <T> 数据类型
     * @return Page对象（第一页，默认大小）
     */
    public static <T> Page<T> createDefaultPage() {
        return createPage(1, DEFAULT_PAGE_SIZE);
    }

    /**
     * 验证分页参数
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return true: 参数有效, false: 参数无效
     */
    public static boolean isValidPageParam(long pageNum, long pageSize) {
        return pageNum > 0 && pageSize > 0 && pageSize <= MAX_PAGE_SIZE;
    }

    /**
     * 获取有效的分页参数
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @return 调整后的参数数组 [pageNum, pageSize]
     */
    public static long[] getValidPageParam(long pageNum, long pageSize) {
        pageNum = Math.max(1, pageNum);
        pageSize = Math.min(Math.max(1, pageSize), MAX_PAGE_SIZE);
        return new long[]{pageNum, pageSize};
    }

    /**
     * 计算总页数
     *
     * @param total 总记录数
     * @param pageSize 每页大小
     * @return 总页数
     */
    public static long getTotalPages(long total, long pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (total + pageSize - 1) / pageSize;
    }

    /**
     * 计算偏移量（用于limit offset, size）
     *
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 偏移量
     */
    public static long getOffset(long pageNum, long pageSize) {
        return (Math.max(1, pageNum) - 1) * pageSize;
    }

    /**
     * 将IPage转换为BasePage
     *
     * @param ipage MyBatis-Plus的IPage对象
     * @param <T> 数据类型
     * @return BasePage对象
     */
    public static <T> BasePage<T> toBasePage(IPage<T> ipage) {
        return BasePage.of(ipage);
    }

    /**
     * 创建空的分页结果
     *
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param <T> 数据类型
     * @return 空的BasePage对象
     */
    public static <T> BasePage<T> emptyPage(long pageNum, long pageSize) {
        return BasePage.of(pageNum, pageSize, 0, null);
    }
}
