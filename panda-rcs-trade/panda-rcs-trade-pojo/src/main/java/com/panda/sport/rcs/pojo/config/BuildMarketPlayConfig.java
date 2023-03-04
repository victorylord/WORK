package com.panda.sport.rcs.pojo.config;

import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.utils.i18n.CommonUtils;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

/**
 * @Project Name : rcs-parent
 * @Package Name : rcs-parent
 * @Description : 构建盘口玩法配置
 * @Author : Paca
 * @Date : 2022-05-13 20:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BuildMarketPlayConfig implements Serializable {

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
     * 0-滚球，1-赛前
     */
    private Integer matchType;
    /**
     * MY-马来盘，EU-欧洲盘，Other-其他
     */
    private String marketType;
    /**
     * 最大盘口数
     */
    private Integer marketCount;
    /**
     * 相邻盘口差值
     */
    private BigDecimal marketNearDiff;
    /**
     * 相邻盘口赔率差值
     */
    private BigDecimal marketNearOddsDiff;
    /**
     * 盘口调整幅度
     */
    private BigDecimal marketAdjustRange;

    /**
     * 时间值
     */
    private Integer timeVal;
    /**
     * spread
     */
    private String spread;
    /**
     * 暂停spread
     */
    private String pauseSpread;

    /**
     * 盘口差
     */
    private BigDecimal marketHeadGap;

    /**
     * 位置spread
     */
    private Map<Integer, BigDecimal> placeSpreadMap;
    /**
     * 位置水差
     */
    private Map<Integer, BigDecimal> placeWaterDiffMap;

    public Integer getMarketCount() {
        if (marketCount == null) {
            return 1;
        }
        return marketCount;
    }

    public BigDecimal getMarketNearDiff() {
        if (marketNearDiff == null) {
            return BigDecimal.ONE;
        }
        return marketNearDiff;
    }

    public BigDecimal getMarketNearOddsDiff() {
        if (marketNearOddsDiff == null) {
            return new BigDecimal("0.15");
        }
        return marketNearOddsDiff;
    }

    public BigDecimal getMarketAdjustRange() {
        if (marketAdjustRange == null) {
            return BigDecimal.ONE;
        }
        return marketAdjustRange;
    }

    public BigDecimal getSpreadValue() {
        return CommonUtils.toBigDecimal(spread, TradeConstant.DEFAULT_SPREAD);
    }

    public BigDecimal getPauseSpreadValue() {
        return CommonUtils.toBigDecimal(pauseSpread, TradeConstant.DEFAULT_SPREAD);
    }

    public BigDecimal getMarketHeadGap() {
        if (marketHeadGap == null) {
            return BigDecimal.ZERO;
        }
        return marketHeadGap;
    }
}
