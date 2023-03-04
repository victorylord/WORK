package com.panda.sport.rcs.pojo.dto;

import lombok.Data;

/**
 * @author kwon
 * @version V1.0
 * @Description 数据缓存对象
 * @date 2022年10月29日18:50:47
 */
@Data
public class CategoryCacheDataDTO {

    /**
     * 缓存VALUE
     */
    private String cacheValue;


    /**
     * 创建时间
     */
    private Long createTime;

    public CategoryCacheDataDTO(String cacheValue, Long createTime){
        this.cacheValue = cacheValue;
        this.createTime = createTime;
    }
}
