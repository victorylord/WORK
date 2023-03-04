package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.StandardMatchInfo;
import lombok.Data;
import org.omg.CORBA.INTERNAL;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-11-07 13:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentMatchInfoVo extends StandardMatchInfo {
    /**
     * 总货量
     */
    private long totalValue;
    /**
     * 近一小时货量
     */
    private BigDecimal totalValueOneHour;
    /**
     *总单数
     */
    private long totalOrderNums;
    /**
     *已结算货量
     */
    private long settledRealTimeValue;
    /**
     *已结算盈亏
     */
    private long settledProfitValue;
    /**
     * 全场比分
     */
    private String score;
    /**
     * 半场比分
     */
    private String periodScore;

    private Integer sortId;

    private List<MatchMarketLiveOddsVo.MatchMarketTeamVo> teamList;

    /**
     * 階段
     */
    private Integer period;
    /**
     * 階段一比分
     */
    private String set1Score;
}
