package com.example.yoyo_data.infrastructure.base;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 分页对象 - 用于分页查询的响应包装
 * 包含分页信息和数据列表
 *
 * @param <T> 数据类型
 * @author Template Framework
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BasePage<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码（从1开始）
     */
    private long current;

    /**
     * 每页大小
     */
    private long size;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private long pages;

    /**
     * 数据列表
     */
    private List<T> records;

    /**
     * 是否有上一页
     */
    private boolean hasPrevious;

    /**
     * 是否有下一页
     */
    private boolean hasNext;

    /**
     * 从MyBatis-Plus的IPage对象转换
     *
     * @param page MyBatis-Plus分页对象
     * @param <T> 数据类型
     * @return BasePage对象
     */
    public static <T> BasePage<T> of(IPage<T> page) {
        BasePage<T> basePage = new BasePage<>();
        basePage.setCurrent(page.getCurrent());
        basePage.setSize(page.getSize());
        basePage.setTotal(page.getTotal());
        basePage.setPages(page.getPages());
        basePage.setRecords(page.getRecords());
        basePage.setHasPrevious(page.getCurrent() > 1);
        basePage.setHasNext(page.getCurrent() < page.getPages());
        return basePage;
    }

    /**
     * 手动构建分页对象
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param total 总记录数
     * @param records 数据列表
     * @param <T> 数据类型
     * @return BasePage对象
     */
    public static <T> BasePage<T> of(long current, long size, long total, List<T> records) {
        BasePage<T> basePage = new BasePage<>();
        basePage.setCurrent(current);
        basePage.setSize(size);
        basePage.setTotal(total);
        basePage.setRecords(records != null ? records : new ArrayList<>());

        // 计算总页数
        long pages = (total + size - 1) / size;
        basePage.setPages(pages);
        basePage.setHasPrevious(current > 1);
        basePage.setHasNext(current < pages);
        return basePage;
    }

    /**
     * 检查是否有数据
     *
     * @return true: 有数据, false: 无数据
     */
    public boolean isEmpty() {
        return records == null || records.isEmpty();
    }

    /**
     * 获取数据列表大小
     *
     * @return 列表大小
     */
    public int getRecordCount() {
        return records != null ? records.size() : 0;
    }
}
