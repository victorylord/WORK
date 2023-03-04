package com.panda.sport.rcs.trade.util;

import cn.hutool.cache.CacheUtil;
import cn.hutool.cache.impl.TimedCache;
import com.panda.sport.rcs.pojo.StandardSportMarketCategory;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 玩法集本地缓存工具类
 * @Author: Magic
 * @Description:
 * @Date: create in 2022/09/24 15:45
 */
public class StandardSportMarketCategoryCacheUtils {

    /**
     * 玩法集本地缓存，默认1天
     */
    public static TimedCache<String, StandardSportMarketCategory> timedCache = new TimedCache<>(24 * 60 * 60 * 1000,new ConcurrentHashMap<>());
}
