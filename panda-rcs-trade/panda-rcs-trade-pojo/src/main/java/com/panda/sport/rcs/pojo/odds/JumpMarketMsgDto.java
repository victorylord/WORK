package com.panda.sport.rcs.pojo.odds;

import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Project Name : panda-rcs-order-group
 * @Package Name : panda-rcs-order-group
 * @Description : 跳盘消息
 * @Author : Paca
 * @Date : 2021-02-06 12:27
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class JumpMarketMsgDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛种ID
     */
    private Integer sportId;
    /**
     * 联赛ID
     */
    private Long tournamentId;
    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 玩法ID
     */
    private Long playId;
    /**
     * 盘口ID
     */
    private Long marketId;
    /**
     * 盘口位置
     */
    private Integer placeNum;
    /**
     * 盘口值调整幅度
     */
    private BigDecimal marketAdjustRange;
    /**
     * 盘口值调整符号
     */
    private BigDecimal marketAdjustSymbol;
    /**
     * 投注阶段，live-滚球，pre-早盘
     */
    private String betStage;
    /**
     * EU-欧盘，MY-马来盘
     */
    private String marketType;
    /**
     * 赛事账务日
     */
    private String dateExpect;
    /**
     * 投注项类型
     */
    private String oddsType;

    public RcsMatchMarketConfig getRcsMatchMarketConfig() {
        RcsMatchMarketConfig config = new RcsMatchMarketConfig();
        config.setPlayId(playId);
        config.setMatchId(matchId);
        config.setMarketId(marketId);
//        config.setTournamentId(tournamentId);
        // EU-欧盘，MY-马来盘
        config.setMarketType(marketType);
        config.setMarketIndex(placeNum);
        config.setBetStage(betStage);
        // 1-早盘，2-滚球盘
        if ("live".equalsIgnoreCase(betStage)) {
            config.setMatchType(2);
        } else {
            config.setMatchType(1);
        }
        // +：-1，-：+1
        config.setMarketHeadGap(marketAdjustSymbol);
        config.setRelevanceType(1);

        config.setDateExpect(dateExpect);
        return config;
    }
}
