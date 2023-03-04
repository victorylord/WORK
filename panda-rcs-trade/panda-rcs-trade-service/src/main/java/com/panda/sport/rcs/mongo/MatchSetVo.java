package com.panda.sport.rcs.mongo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  赛前十五分钟预设置
 */
@Document(collection = "match_set")
@Data
public class MatchSetVo implements Serializable {

    @Field(value = "id")
    private Long id;

    private Integer tradeLevel;
    /**
     * 赛事ID
     */
    private Long matchId;

    private Long categorySetId;
    /**
     * 玩法ID
     */
    private Long categoryId;
    /**
     * 盘口ID
     */
    private Long marketId;

    /**
     * 盘口位置
     */
    private Integer marketPlaceNum;
    /**
     * 玩法ID集合
     */
    private List<Long> categoryIdList;
    /**
     * 方法No
     *
     * @see com.panda.sport.rcs.enums.MatchSetEnum
     */
    private Integer methodNo;
    /**
     * 参数值
     */
    private String paramValue;
    /**
     * 入参参数
     */
    private String jsonParams;
    /**
     * 更新时间
     */
    private String updateTime;

}
