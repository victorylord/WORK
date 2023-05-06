/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.utils;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RedisUtil {
    private static final String prefix;

    static {
        prefix = "GAME_";
    }

    // 从Spring容器中获取RedisTemplate对象
    private static RedisTemplate<String, String> template = (RedisTemplate<String, String>) SpringContextHolder.getBean(
            "redisTemplate");

    /**
     * 向Redis缓存中设置数据
     * 保存值的格式为json字符串
     *
     * @param key
     * @param value
     * @param validTime
     * @param <T>
     * @return
     */
    public static <T> boolean set(String key, T value, Long validTime) {
        validTime = validTime == null ? -1L : validTime;
        key = prefix + key;
        String str = "";
        // 将对象转换为json字符串
        if (value instanceof String) {
            str = (String) value;
        } else {
            str = JSON.toJSONString(value);
        }

        return setRedis(key, str, validTime);
    }

    /**
     * 向Redis中设置值
     *
     * @param key
     * @param value
     * @param validTime
     * @return
     */
    private static boolean setRedis(String key, String value, long validTime) {
        try {
            template.opsForValue().set(key, value, validTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis set error:", e);
            return false;
        }
        return true;
    }

    /**
     * 递增
     *
     * @param key 键
     * @return
     */
    public static long incr(String key) {
        try {
            return template.opsForValue().increment(key);
        } catch (Exception e) {
            log.error("redis set error:", e);
        }
        return -1l;
    }

    /**
     * 从Redis中获取值，并将其转换为对应的类型
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T get(String key, Class<T> clazz) {
        key = prefix + key;
        return JSONObject.parseObject(getFromRedis(key), clazz);
    }

    /**
     * 从Redis中获取值，并将其转换为对应的类型，该类型带有泛型参数
     *
     * @param key
     * @param type
     * @return
     */
    public static <T> T get(String key, Type type) {
        key = prefix + key;
        return JSONObject.parseObject(getFromRedis(key), type);
    }

    /**
     * 从Redis中获取值，并将其转换为对应类型的集合
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getList(String key, Class<T> clazz) {
        key = prefix + key;
        return JSONObject.parseArray(getFromRedis(key), clazz);
    }

    /**
     * 从Redis中获取值
     *
     * @param key
     * @return
     */
    public static String get(String key) {
        key = prefix + key;
        return getFromRedis(key);
    }

    /**
     * 从Redis中获取值
     *
     * @param key
     * @return
     */
    private static String getFromRedis(String key) {
        try {
            String obj = template.opsForValue().get(key);
            return obj;
        } catch (Exception e) {
            log.error("redis get error:", e);
            return null;
        }
    }

    /**
     * 根据key删除Redis中指定的值
     *
     * @param key
     * @return
     */
    public static boolean del(String key) {
        boolean flag = true;
        try {
            key = prefix + key;
            log.info("清除缓存KEY：" + key);
            template.delete(key);
        } catch (Exception e) {
            log.error("缓存清理异常");
            flag = false;
        }
        return flag;
    }

    /**
     * 向Redis中设置值，值为Json字符串格式，使用hash方式进行存储
     *
     * @param key
     * @param field
     * @param value
     * @param validTime
     * @param <T>
     */
    public static <T> void setHash(String key, String field, T value, Long validTime) {
        validTime = validTime == null ? -1L : validTime;
        key = prefix + key;
        String str = "";
        if (value instanceof String) {
            str = (String) value;
        } else {
            str = JSON.toJSONString(value);
        }
        setHashRedis(key, field, str, validTime);
    }

    /**
     * 不设置过期时间的散列表
     *
     * @return
     * @date 2022/8/30 14:14
     * @author siqiangguo
     */
    public static <T> void setHash(String key, String field, T value) {
        key = prefix + key;
        String str = "";
        if (value instanceof String) {
            str = (String) value;
        } else {
            str = JSON.toJSONString(value);
        }
        try {
            template.opsForHash().put(key, field, str);
        } catch (Exception e) {
            log.error("redis setHashRedis error:", e);
        }
    }

    /**
     * 不设置过期时间的散列表
     *
     * @return
     * @date 2022/8/30 14:14
     * @author siqiangguo
     */
    public static void setHashAll(String key, Map<String, String> map) {
        key = prefix + key;
        if (CollectionUtils.isEmpty(map)) {
            return;
        }
        try {
            template.opsForHash().putAll(key, map);
        } catch (Exception e) {
            log.error("redis setHashAll error:", e);
        }
    }

    /**
     * 向redis中设置值，使用hash格式进行存储
     *
     * @param key
     * @param field
     * @param value
     * @param validTime
     * @return
     */
    public static boolean setHashRedis(String key, String field, String value, long validTime) {
        try {
            template.opsForHash().put(key, field, value);
            template.expire(key, validTime, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("redis setHashRedis error:", e);
            return false;
        }
        return true;
    }

    /**
     * 删除 hash 表中的多个 field
     *
     * @date 2022/8/26 11:03
     * @author siqiangguo
     */
    public static boolean deleteHashFields(String key, String... fields) {
        try {
            template.opsForHash().delete(prefix + key, fields);
        } catch (Exception e) {
            log.error("redis delete Hash error:", e);
            return false;
        }
        return true;
    }

    /**
     * 从redis中获取某个hash值中某个属性key所对应的值
     *
     * @param key
     * @param field
     * @return
     */
    public static String getHash(String key, String field) {

        return getHashFromRedis(prefix + key, field);
    }

    /**
     * 从redis中获取某个hash值中某个属性key所对应的指定数据类型的值
     *
     * @param key
     * @param field
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> List<T> getHash(String key, String field, Class<T> clazz) {
        key = prefix + key;
        List<T> ts = JSONObject.parseArray(getHashFromRedis(key, field), clazz);
        return ts;
    }

    /**
     * 从redis中获取某个hash值中某个属性key所对应的值
     *
     * @param key
     * @param field
     * @return
     */
    private static String getHashFromRedis(String key, String field) {
        try {
            String obj = (String) template.opsForHash().get(key, field);
            return obj;
        } catch (Exception e) {
            log.error("redis getHash error:", e);
            return null;
        }
    }

    /**
     * 从redis中获取某个hash值中某个属性key所对应的指定数据类型的值,
     * 该数据类型带有泛型
     *
     * @param key
     * @param field
     * @param type
     * @return
     */
    public static Object getHash(String key, String field, Type type) {
        key = prefix + key;
        return JSONObject.parseObject(getHashFromRedis(key, field), type);
    }

    /**
     * 删除某个key所对应的值
     *
     * @param key
     */
    public static void delete(String key) {
        key = prefix + key;
        try {
            template.opsForHash().getOperations().delete(key);
        } catch (Exception e) {
            log.error("缓存删除异常 key:" + key, e);
        }
    }

}


