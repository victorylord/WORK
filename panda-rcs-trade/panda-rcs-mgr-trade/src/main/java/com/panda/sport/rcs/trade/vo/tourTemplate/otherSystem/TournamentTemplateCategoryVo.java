package com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class TournamentTemplateCategoryVo implements Serializable {
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 玩法名称
     */
    private String playName;
    /**
     * 是否开售 1：是  0：否
     */
    private Integer isSell;
    /**
     * 足球自动关盘时间设置：6、上半场期间 41、加时赛上半场 7、下半场期间 42、加时赛下半场
     * 篮球自动关盘时间设置：13、第1节 14、第2节 15、第3节 16、第4节 40、加时
     */
    private Integer autoCloseMarket;
    /**
     * 最大盘口数
     */
    private Integer marketCount;
    /**
     * 比赛进程时间
     */
    private Integer matchProgressTime;
    /**
     * 补时时间
     */
    private Integer injuryTime;
    /**
     * 支持串关，1:是 0:否
     */
    private Integer isSeries;
    /**
     * 相邻盘口差值
     */
    private BigDecimal marketNearDiff;
    /**
     * 相邻盘口赔率差值
     */
    private BigDecimal marketNearOddsDiff;
    /**
     * 赔率（水差）变动幅度
     */
    private BigDecimal oddsAdjustRange;
    /**
     * 盘口调整幅度
     */
    private BigDecimal marketAdjustRange;
    /**
     * 数据源SR,BC,BG,TX
     */
    private String dataSource;
    /**
     * 是否特殊抽水1:是 0:否
     */
    private Integer isSpecialPumping;
    /**
     * 特殊抽水赔率区间
     */
    private String specialOddsInterval;
    /**
     * 高赔特殊抽水赔率区间
     */
    private String specialOddsIntervalHigh;
    /**
     * 高赔特殊抽水赔率区间
     */
    private String specialOddsIntervalLow;
    /**
     * 特殊抽水赔率区间状态
     */
    private String specialOddsIntervalStatus;
    /**
     * 最小球头
     */
    private BigDecimal minBallHead;
    /**
     * 最大球头
     */
    private BigDecimal maxBallHead;
    /**
     * 赛制胜场
     */
    private Integer competitionWinValue;
    /**
     * 预约投注开关 0:关 1:开
     */
    private Integer pendingOrderStatus;
}
