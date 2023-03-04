package com.panda.sport.rcs.pojo.config;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Project Name : rcs-parent
 * @Package Name : rcs-parent
 * @Description : 盘口构建玩法配置
 * @Author : Paca
 * @Date : 2021-03-14 16:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MarketBuildPlayConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 玩法ID
     */
    private Integer playId;
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
     * spread值，rcs_tournament_template_play_margain_ref.margain
     */
    private String spread;
}
