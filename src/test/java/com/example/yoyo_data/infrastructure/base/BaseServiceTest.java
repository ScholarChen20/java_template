package com.example.yoyo_data.infrastructure.base;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * 服务测试基类 - 所有Service单元测试的父类
 * 提供常用的测试工具方法
 *
 * @author Template Framework
 * @version 1.0
 */
@SpringBootTest
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    /**
     * 使用反射注入对象的私有字段（用于mock注入）
     *
     * @param target 目标对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    public void setFieldValue(Object target, String fieldName, Object value) {
        ReflectionTestUtils.setField(target, fieldName, value);
    }

    /**
     * 使用反射获取对象的私有字段值
     *
     * @param target 目标对象
     * @param fieldName 字段名
     * @return 字段值
     */
    public Object getFieldValue(Object target, String fieldName) {
        return ReflectionTestUtils.getField(target, fieldName);
    }

    /**
     * 调用对象的私有方法
     *
     * @param target 目标对象
     * @param methodName 方法名
     * @param paramTypes 参数类型
     * @param args 参数值
     * @return 方法返回值
     */
    public Object invokeMethod(Object target, String methodName, Class<?>[] paramTypes, Object[] args) {
        return ReflectionTestUtils.invokeMethod(target, methodName, args);
    }

    /**
     * 断言两个对象相等
     *
     * @param expected 期望值
     * @param actual 实际值
     * @param message 失败消息
     */
    public void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " - expected: " + expected + ", actual: " + actual);
        }
    }

    /**
     * 断言两个对象不相等
     *
     * @param notExpected 不期望的值
     * @param actual 实际值
     * @param message 失败消息
     */
    public void assertNotEquals(Object notExpected, Object actual, String message) {
        if (notExpected.equals(actual)) {
            throw new AssertionError(message + " - should not be: " + notExpected);
        }
    }

    /**
     * 断言对象不为null
     *
     * @param object 对象
     * @param message 失败消息
     */
    public void assertNotNull(Object object, String message) {
        if (object == null) {
            throw new AssertionError(message + " - object should not be null");
        }
    }

    /**
     * 断言对象为null
     *
     * @param object 对象
     * @param message 失败消息
     */
    public void assertNull(Object object, String message) {
        if (object != null) {
            throw new AssertionError(message + " - object should be null");
        }
    }

    /**
     * 断言布尔值为真
     *
     * @param condition 条件
     * @param message 失败消息
     */
    public void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message + " - condition should be true");
        }
    }

    /**
     * 断言布尔值为假
     *
     * @param condition 条件
     * @param message 失败消息
     */
    public void assertFalse(boolean condition, String message) {
        if (condition) {
            throw new AssertionError(message + " - condition should be false");
        }
    }
}
