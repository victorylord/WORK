package com.panda.sport.rcs.trade.vo;

import lombok.Data;

import java.util.Map;

@Data
public class MarketInfoVo <T>{
    /**
     * 盘口级别，数字越小优先级越高
     */
    private Integer oddsMetric;
    /**
     * 盘口状态0-5. 0:active, 1:suspended, 2:deactivated, 3:settled, 4:cancelled, 5:handedOver
     */
    private Integer status;
    /**
     * 附加字段1
     */
    private String addition1;
    /**
     * 标准玩法id   standard_sport_market_category.id
     */
    private Long playId;
    /**
     * 盘口id
     */
    private Long marketId;

    /**
     * 存储盘口配置信息
     */
    private Map<String, T> marketInfo;

    /**
     * 1 ：主盘  2：附加盘   3：不显示
     */
    private Integer marketShowType;

    /**
     * 赔率差值
     */
    private Double oddsDifference;
}
