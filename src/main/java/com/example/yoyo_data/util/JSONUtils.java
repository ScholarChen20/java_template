package com.example.yoyo_data.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.example.yoyo_data.common.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;


/**
 * jackson工具类
 */
@Slf4j
public class JSONUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        // 解决不支持 Java 8，LocalDateTime的问题
        mapper.registerModule(new JavaTimeModule());
    }

    @Nullable
    public static <E> String toString(E entity) {
        if (entity == null) {
            return null;
        }

        if (entity instanceof String) {
            return (String)entity;
        }

        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        try {
            return mapper.writeValueAsString(entity);
        } catch (JsonProcessingException e) {
            throw new SystemException("数据序列化/反序列化失败", e);
        }
    }

    @Nullable
    public static <T> T deserialize(String jsonStr, Class<T> type) {
        if (jsonStr == null) {
            return null;
        }

        if (type.equals(String.class)) {
            return (T)readStr(jsonStr);
        }


        try {
            return mapper.readValue(jsonStr, type);
        } catch (IOException e) {
            throw new SystemException("数据序列化/反序列化失败", e);
        }
    }

    @Nullable
    public static <T> T convertValue(Object fromValue, Type toType) {
        if (fromValue == null) {
            return null;
        }

        try {
            JavaType javaType = TypeFactory.defaultInstance().constructType(toType);
            return mapper.convertValue(fromValue, javaType);
        } catch (Exception e) {
            throw new SystemException("数据序列化/反序列化失败", e);
        }
    }

    public static <T> T deserialize(String jsonStr, TypeReference<T> typeReference) {
        if (jsonStr == null) {
            return null;
        }

        try {
            return mapper.readValue(jsonStr, typeReference);
        } catch (IOException e) {
            throw new SystemException("数据序列化/反序列化失败", e);
        }
    }

    private static String readStr(String jsonStr) {
        if (jsonStr == null) {
            return null;
        }

        if (jsonStr.startsWith("\"") && jsonStr.endsWith("\"")) {
            return jsonStr.substring(1, jsonStr.length() - 1);
        }

        return jsonStr;
    }

    @Nullable
    public static <T> List<T> deserializeList(String jsonStr, Class<T> type) {
        if (jsonStr == null) {
            return null;
        }

        try {
            JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, type);
            return mapper.readValue(jsonStr, javaType);
        } catch (IOException e) {
            throw new SystemException("数据序列化/反序列化失败", e);
        }
    }
}
