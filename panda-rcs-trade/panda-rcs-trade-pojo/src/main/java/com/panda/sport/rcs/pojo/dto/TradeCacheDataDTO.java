package com.panda.sport.rcs.pojo.dto;

import lombok.Data;

/**
 * @author wealth
 * @version V1.0
 * @Description 数据缓存对象
 * @date 2022-09-25 15:00:00
 */
@Data
public class TradeCacheDataDTO {

    /**
     * 缓存key
     */
    private String cacheKey;

    /**
     * 缓存VALUE
     */
    private String cacheValue;


    /**
     * 创建时间
     */
    private Long createTime;

    public TradeCacheDataDTO(String cacheKey, String cacheValue, Long createTime){
        this.cacheKey = cacheKey;
        this.cacheValue = cacheValue;
        this.createTime = createTime;
    }
}
