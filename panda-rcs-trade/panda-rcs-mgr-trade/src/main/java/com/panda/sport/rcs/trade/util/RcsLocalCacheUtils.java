package com.panda.sport.rcs.trade.util;

import cn.hutool.cache.impl.TimedCache;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 本地缓存工具类
 *
 * @Author: Magic
 * @Description:
 * @Date: create in 2022/09/25 15:45
 */
@Slf4j
public class RcsLocalCacheUtils {


    /**
     * 本地缓存工具类 默认5分钟
     */
    public static TimedCache<String, Object> timedCache = new TimedCache<>(5 * 60 * 1000, new ConcurrentHashMap<>());


    public static <T> T getValue(String key, Function<String,T> getValue) {
        return getValue(key, getValue, null);

    }

    public static <T> T getValue(String key, Function<String,T> getValue, Long timeout) {
        T o = (T) timedCache.get(key, false);
        if (o == null) {
            o = getValue.apply(key);
            if (o != null && StringUtils.isNotBlank(o.toString())) {
                if (timeout != null && timeout > 0) {
                    timedCache.put(key, o, timeout);
                } else {
                    timedCache.put(key, o);
                }
                log.info("::{}:: 本地缓存设置：{}", key , JSONObject.toJSONString(o));
            }
        } else {
            log.info("::{}:: 本地缓存获取：{}", key, JSONObject.toJSONString(o));
        }
        return o;
    }

    public static <T> T getValue(String key,String field, BiFunction<String,String,T> getValue) {
        return getValue(key,field, getValue, null);

    }

    public static <T> T getValue(String key,String field, BiFunction<String,String,T> getValue, Long timeout) {
        T o = (T) timedCache.get(key + field, false);
        if (o == null) {
            o = getValue.apply(key,field);
            if (o != null && StringUtils.isNotBlank(o.toString())) {
                if (timeout != null && timeout > 0) {
                    timedCache.put(key + field, o, timeout);
                } else {
                    timedCache.put(key + field, o);
                }
                log.info("::{}:: 本地缓存设置：{}", key + field, JSONObject.toJSONString(o));
            }
        } else {
            log.info("::{}:: 本地缓存获取：{}", key + field, JSONObject.toJSONString(o));
        }
        return o;
    }
}
