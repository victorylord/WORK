package com.panda.sport.rcs.pojo.config;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 盘口构建配置
 * @Author : Paca
 * @Date : 2021-01-21 14:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MarketBuildConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 玩法ID
     */
    private Long playId;

    /**
     * 盘口位置
     */
    private Integer placeNum;

    /**
     * margin 或 spread
     */
    private BigDecimal margin;

    /**
     * 暂停margin 或 暂停spread
     */
    private BigDecimal timeOutMargin;

    /**
     * 位置水差
     */
    private BigDecimal placeWaterDiff;

    /**
     * 盘口差
     */
    private BigDecimal marketHeadGap;

    public BigDecimal getPlaceWaterDiff() {
        if (placeWaterDiff == null) {
            return BigDecimal.ZERO;
        }
        return placeWaterDiff;
    }

    @Deprecated
    public BigDecimal getPlayWaterDiff() {
        return BigDecimal.ZERO;
    }

    public BigDecimal getMarketHeadGap() {
        if (marketHeadGap == null) {
            return BigDecimal.ZERO;
        }
        return marketHeadGap;
    }
}
