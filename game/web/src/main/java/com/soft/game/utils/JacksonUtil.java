/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * @version V1.0
 * @description: jackson 工具类
 * @author: xuexiaojin
 * @create: 2023/1/4 9:36 下午
 **/
@Slf4j
public class JacksonUtil {

    private static ObjectMapper objectMapper = null;
    public static final String DYNC_FILTER = "DYNC_FILTER";

    static {
        if (objectMapper == null) {
            objectMapper = getInstants();
        }
    }

    @JsonFilter(DYNC_FILTER)
    interface DynamicFilter {

    }


    public static ObjectMapper getInstants() {
        ObjectMapper mapper = new ObjectMapper();
        // 非规范化输出，原始输出
        mapper.enable(MapperFeature.USE_STD_BEAN_NAMING);
        // 空对象不抛异常
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        // 反序列化时，未知属性不抛异常
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        // 反序列化时，空字符串对应的实例属性为null
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        mapper.setDateFormat(new SimpleDateFormat(DateUtils.DEFAULT_PATTERN));

        // 屏蔽get方法的序列化
        mapper.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        // 任何属性均可见
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

        // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); 忽略为null的字段
        return mapper;
    }

    public static void filter(Class<?> clazz, ObjectMapper mapper, String... filter) {
        if (clazz == null) {
            return;
        }
        if (filter != null && filter.length > 0) {
            mapper.setFilterProvider(new SimpleFilterProvider().addFilter(DYNC_FILTER,
                    SimpleBeanPropertyFilter.serializeAllExcept(filter)));
            mapper.addMixIn(clazz, DynamicFilter.class);
        }
    }


    public static String toJson(Object obj)  {
        if (obj == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("jackson toJson error", e);
        }
        return null;
    }

    public static String toJsonWithDateFormat(Object obj, String defaultPattern) {
        if (obj == null || StringUtils.isBlank(defaultPattern)) {
            return null;
        }
        try {
            // 获取一个新对象objectMapper，防止自定义配置参数对全局的影响
            ObjectMapper mapper = getInstants();
            SimpleDateFormat dateFormat = new SimpleDateFormat(defaultPattern);
            mapper.setDateFormat(dateFormat);
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("jackson toJson error", e);
        }
        return null;
    }

    public static String toJsonExcept(Object obj, String... filedName)  {
        if (obj == null) {
            return null;
        }
        try {
            ObjectMapper mapper = objectMapper;
            if (filedName != null) {
                // 获取一个新对象objectMapper，防止自定义配置参数对全局的影响
                mapper = getInstants();
                filter(obj.getClass(), mapper, filedName);
            }
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            log.error("jackson toJson error", e);
        }
        return null;
    }


    public static <T> T fromJson(String json, TypeReference<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        T t = null;
        try {
            t = objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("jackson fromJson error", e);
        }
        return t;
    }

    public static <T> T fromJson(String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        T t = null;
        try {
            t = objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("jackson fromJson error", e);
        }
        return t;
    }

    public static <T> List<T> parseArray(String json, Class<T> type) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        List<T> t = null;
        try {
            t = objectMapper.readValue(json, objectMapper.getTypeFactory().constructParametricType(List.class, type));
        } catch (JsonProcessingException e) {
            log.error("jackson parseArray error", e);
        }
        return t;
    }

    public static JsonNode parseJsonNode(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        JsonNode t = null;
        try {
            t = objectMapper.readTree(json);
        } catch (JsonProcessingException e) {
            log.error("jackson parseJsonNode error", e);
        }
        return t;
    }

    public static Map<String, Object> parseJsonNodeMap(String json) {
        if (StringUtils.isBlank(json)) {
            return null;
        }
        Map<String, Object> t = null;
        try {
            JsonNode jsonNode = parseJsonNode(json);
            if (jsonNode != null) {
                t = objectMapper.convertValue(jsonNode, new TypeReference<Map<String, Object>>() {
                });
            }

        } catch (IllegalArgumentException e) {
            log.error("jackson parseJsonNodeMap error", e);
        }
        return t;
    }


}

