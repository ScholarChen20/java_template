package com.example.yoyo_data.util;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.write.metadata.style.WriteCellStyle;
import com.alibaba.excel.write.metadata.style.WriteFont;
import com.alibaba.excel.write.style.HorizontalCellStyleStrategy;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: Ish
 * @description
 * @Date: 2024/3/11 10:58
 */
@Slf4j
public class EasyExcelUtil {


    /**
     * 反射生成数据
     *
     * @param dataList
     * @param fields
     * @param <T>
     * @return
     */
    public static <T> List<List<Object>> generateDataList(List<T> dataList, List<String> fields) {
        try {
            List<List<Object>> resultList = new ArrayList<>();
            List<Object> rowList = null;
            for (T t : dataList) {
                rowList = new ArrayList<>();
                for (String fieldName : fields) {
                    try {
                        Field declaredField = t.getClass().getDeclaredField(fieldName);
                        if (declaredField != null) {
                            declaredField.setAccessible(true);
                            String firstLetter = fieldName.substring(0, 1).toUpperCase();
                            String getter = "get" + firstLetter + fieldName.substring(1);
                            Method method = t.getClass().getMethod(getter, new Class[]{});
                            Object fieldValue = method.invoke(t, new Object[]{});
                            rowList.add(fieldValue);
                        }
                    } catch (Exception e) {
                        log.error("[generateDataList] 反射生成表头出现异常，error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
                    }
                }
                resultList.add(rowList);
            }
            return resultList;
        } catch (Exception e) {
            log.error("[generateDataList] 反射生成表头出现异常，error: {} - {}", e.getClass().getSimpleName(), e.getMessage());
        }
        return Lists.newArrayList();

    }

    /**
     * 将实体类的所有字段值转换为列表形式。
     *
     * @param entity 需要转换的实体对象，可以是任意类型T。
     * @return 返回一个包含实体类所有字段值的列表，列表中的元素类型为Object。
     */
    public static <T> List<String> convertFieldsToList(T entity) {
        List<String> fieldValues = new ArrayList<>();

        // 获取实体类的所有字段
        Field[] fields = entity.getClass().getDeclaredFields();

        for (Field field : fields) {
            // 设置字段可访问，即使是私有字段也可以获取到值
            field.setAccessible(true);

            // 将字段的值添加到列表中
            fieldValues.add(field.getName());
        }

        return fieldValues;
    }

    /**
     * 通过属性注解获取表头信息
     *
     * @param clazz
     * @param fields
     * @return
     */
    public static List<List<String>> generateExcelHeadByObj(Class clazz, List<String> fields) {
        List<List<String>> headList = new ArrayList<>();
        List<String> singleHeadList = null;
        for (String fieldName : fields) {
            //获取表头
            singleHeadList = new ArrayList<>();
            Field declaredField = null;
            try {
                declaredField = clazz.getDeclaredField(fieldName);
                declaredField.setAccessible(true);
                ExcelProperty excelProperty = declaredField.getAnnotation(ExcelProperty.class);
                if (excelProperty != null) {
                    String[] value = excelProperty.value();
                    singleHeadList.add(value[0]);
                } else {
                    singleHeadList.add(fieldName);
                }
            } catch (Exception e) {
                if (e instanceof NoSuchFieldException) {
                    singleHeadList.add(fieldName);
                }
            }
            headList.add(singleHeadList);
        }
        return headList;
    }


    /**
     * 设置表头样式
     *
     * @return
     */
    public static HorizontalCellStyleStrategy getHorizontalCellStyleStrategy() {
        // 头的策略
        WriteCellStyle headWriteCellStyle = new WriteCellStyle();
        WriteFont headWriteFont = new WriteFont();
        headWriteFont.setFontHeightInPoints((short) 10);
        headWriteCellStyle.setWriteFont(headWriteFont);
        headWriteCellStyle.setFillPatternType(FillPatternType.NO_FILL);
        headWriteCellStyle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        headWriteCellStyle.setVerticalAlignment(VerticalAlignment.CENTER);

        // 内容的策略
        WriteCellStyle contentWriteCellStyle = new WriteCellStyle();
        // 这个策略是 头是头的样式 内容是内容的样式 其他的策略可以自己实现
        return new HorizontalCellStyleStrategy(headWriteCellStyle, contentWriteCellStyle);
    }
}
