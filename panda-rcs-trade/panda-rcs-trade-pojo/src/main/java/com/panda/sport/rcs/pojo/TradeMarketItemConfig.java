package com.panda.sport.rcs.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 风控模板margin/最大最小赔率范围配置
 */
@Data
public class TradeMarketItemConfig implements Serializable {

    // 标准赛事ID
    private Long standardMatchInfoId;

    //标准玩法ID
    private Long standardCategoryId;

    //子玩法ID
    private Long childStandardCategoryId;

    //盘口位置
    private Integer placeNum;

    //盘口类型.属于赛前盘或者滚球盘.1:赛前盘;0:滚球盘
    private Integer marketType;

    //margin值
    private BigDecimal margin;

    //最大赔率
    private BigDecimal maxOddsValue;

    //最小赔率
    private BigDecimal minOddsValue;
}