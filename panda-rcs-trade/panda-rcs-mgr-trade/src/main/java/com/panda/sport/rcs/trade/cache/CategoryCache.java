package com.panda.sport.rcs.trade.cache;

import com.panda.sport.rcs.pojo.dto.CategoryCacheDataDTO;
import com.panda.sport.rcs.pojo.dto.TradeCacheDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kwon
 * @version V1.0
 * @Description 玩法集缓存
 * @date 2022年10月29日18:41:33
 */
@Slf4j
@Component
public class CategoryCache {

    /**
     * 数据存储过期时间设置为10分钟
     */
    private final static Long EXPIRED_TIME = 1 * 60 * 1000L;

    /**
     * 赛事列表信息
     * tournamentJsonString
     */
    public static Map<String, CategoryCacheDataDTO> categoryMap = new ConcurrentHashMap<>();

    /**
     * 获取缓存数据
     *
     * @param cacheKey
     * @return
     */
    public static CategoryCacheDataDTO getCategoryCache(String cacheKey){
        if(!categoryMap.containsKey(cacheKey)){
            return null;
        }
        return categoryMap.get(cacheKey);
    }
}
