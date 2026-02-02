package com.example.yoyo_data.infrastructure.base;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Mapper测试基类 - 所有Mapper/Repository测试的父类
 * 提供数据持久层测试的基础支持
 *
 * @author Template Framework
 * @version 1.0
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public abstract class BaseMapperTest {

    /**
     * 生成测试用的ID
     *
     * @return 测试ID
     */
    public Long generateTestId() {
        return System.currentTimeMillis() % 10000000;
    }

    /**
     * 生成测试用的字符串
     *
     * @param prefix 前缀
     * @return 测试字符串
     */
    public String generateTestString(String prefix) {
        return prefix + "_" + System.currentTimeMillis();
    }

    /**
     * 检查记录是否存在
     *
     * @param id 记录ID
     * @param mapper Mapper实例
     * @return true: 存在, false: 不存在
     */
    public boolean recordExists(Long id, BaseMapper<?> mapper) {
        return mapper.selectById(id) != null;
    }

    /**
     * 清理测试数据（子类可重写）
     */
    public void cleanup() {
        // 子类可重写此方法来清理测试数据
    }

    /**
     * 打印记录信息（用于调试）
     *
     * @param record 记录
     */
    public void printRecord(Object record) {
        System.out.println("Record: " + record);
    }

    /**
     * 断言记录存在
     *
     * @param id 记录ID
     * @param mapper Mapper实例
     * @throws AssertionError 记录不存在时抛出
     */
    public void assertRecordExists(Long id, BaseMapper<?> mapper) {
        if (!recordExists(id, mapper)) {
            throw new AssertionError("Record with id " + id + " does not exist");
        }
    }

    /**
     * 断言记录不存在
     *
     * @param id 记录ID
     * @param mapper Mapper实例
     * @throws AssertionError 记录存在时抛出
     */
    public void assertRecordNotExists(Long id, BaseMapper<?> mapper) {
        if (recordExists(id, mapper)) {
            throw new AssertionError("Record with id " + id + " should not exist");
        }
    }
}
