package com.panda.sport.rcs.trade.vo.tourTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.panda.sport.rcs.utils.DateFormatSerializer;
import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  玩法margain配置
 * @Date: 2020-05-12 21:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplatePlayMargainVo {
    /**
     * 主键id
     */
    private Long id;
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 玩法名称
     */
    private String playName;
    /**
     * 1：早盘；0：滚球
     */
    private Integer matchType;
    /**
     * MY：马来盘  EU：欧洲盘  Other：其他
     */
    private String marketType;
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
     * 盘口出涨预警
     */
    private Integer marketWarn;
    /**
     * 支持串关，1:是 0:否
     */
    private Integer isSeries;
    /**
     * 副盘限额比列
     */
    private String viceMarketRatio;
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
     * 标记哪些玩法下面有分时margin值，提供给前端标记处理
     */
    private String timeVal;
    /**
     * 针对 斯诺克 可自由新增多个分时节点，提供给前端展示
     */
    private String timeValRef;
    /**
     * 0.5球头相邻盘口赔率分差
     */
    private BigDecimal headMarketNearOddsDiff;
    /**
     * 数据源SR,BC,BG,TX
     */
    private String dataSource;
    /**
     * 跳水最大值
     */
    private BigDecimal oddsMaxValue;
    /**
     * 跳盘最大值
     */
    private BigDecimal marketMaxValue;
    /**
     * 手动操盘相邻盘口差
     */
    private BigDecimal manualMarketNearDiff;
    /**
     * 手动操盘相邻盘口赔率差
     */
    private BigDecimal manualMarketNearOddsDiff;
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
     * 高赔特殊抽水保底投注区间
     */
    private String specialBettingIntervalHigh;
    /**
     * 拒单盘口变动阈值
     */
    private BigDecimal rejectMarketDiff;
    /**
     * 拒单赔率变动阈值
     */
    private BigDecimal rejectOddsDiff;
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
     * 赔率变动接拒开关（0.关 1.开）
     */
    private Integer oddsChangeStatus;
    /**
     * 赔率接拒变动范围
     */
    private BigDecimal oddsChangeValue;
    /**
     * 是否出涨自动封盘（0.关 1.开）
     */
    private Integer ifWarnSuspended;
    /**
     * 预约投注开关 0:关 1:开
     */
    private Integer pendingOrderStatus;
}
