package com.panda.sport.rcs.pojo.config;

import com.panda.sport.rcs.pojo.constants.TradeConstant;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Project Name : rcs-parent
 * @Package Name : rcs-parent
 * @Description : 构建盘口位置配置
 * @Author : Paca
 * @Date : 2022-05-14 13:22
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BuildMarketPlaceConfig implements Serializable {

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
     * spread
     */
    private BigDecimal spread;
    /**
     * 暂停spread
     */
    private BigDecimal pauseSpread;
    /**
     * 位置水差
     */
    private BigDecimal placeWaterDiff;

    public BigDecimal getSpread() {
        if (spread == null) {
            return TradeConstant.DEFAULT_SPREAD;
        }
        return spread;
    }

    public BigDecimal getPauseSpread() {
        if (pauseSpread == null) {
            return getSpread();
        }
        return pauseSpread;
    }

    public BigDecimal getPlaceWaterDiff() {
        if (placeWaterDiff == null) {
            return BigDecimal.ZERO;
        }
        return placeWaterDiff;
    }
}
