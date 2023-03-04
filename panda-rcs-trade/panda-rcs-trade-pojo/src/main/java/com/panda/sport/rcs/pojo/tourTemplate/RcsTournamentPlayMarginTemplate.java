package com.panda.sport.rcs.pojo.tourTemplate;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.pojo.tourTemplate
 * @Description :  TODO
 * @Date: 2020-08-05 20:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsTournamentPlayMarginTemplate implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
     * sportId
     */
    private Integer sportId;
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 玩法名称
     */
    private String playName;
    /**
     * 等级
     */
    private Long level;
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
     * margain值
     */
    private String margain;
    /**
     * 0 投注额差值 1 投注额/赔付混合差值
     */
    private Integer balanceOption;
    /**
     * 最大赔率
     */
    private BigDecimal maxOdds;
    /**
     * 最小赔率
     */
    private BigDecimal minOdds;
    /**
     * 跳赔规则
     * 0 累计/单枪跳分 1 累计差值跳分
     */
    private Integer oddChangeRule;
    /**
     * 上盘累计限额
     */
    private Long homeMultiMaxAmount;
    /**
     * 上盘单枪限额
     */
    private Long homeSingleMaxAmount;
    /**
     * 上盘单枪赔率变化率
     */
    private BigDecimal homeSingleOddsRate;
    /**
     * 上盘累计赔率变化率
     */
    private BigDecimal homeMultiOddsRate;

    /**
     * 下盘单枪赔率变化率
     */
    private BigDecimal awaySingleOddsRate;

    /**
     * 下盘累计赔率变化率
     */
    private BigDecimal awayMultiOddsRate;
    /**
     * 单注投注/赔付限额
     */
    private Long orderSinglePayVal;
    /**
     * 用户累计赔付限额
     */
    private Long userMultiPayVal;
    /**
     * 累计差值
     */
    private Long multiDiffVal;
    /**
     * 累计概率变化
     */
    private BigDecimal multiOddsRate;
    /**
     * 上盘一级限额
     */
    private Long homeLevelFirstMaxAmount;
    /**
     * 上盘二级限额
     */
    private Long homeLevelSecondMaxAmount;
    /**
     * 上盘一级赔率变化率
     */
    private BigDecimal homeLevelFirstOddsRate;
    /**
     * 上盘二级赔率变化率
     */
    private BigDecimal homeLevelSecondOddsRate;
    /**
     * 下盘一级赔率变化率
     */
    private BigDecimal awayLevelFirstOddsRate;
    /**
     * 下盘二级赔率变化率
     */
    private BigDecimal awayLevelSecondOddsRate;
    /**
     * 暂停margin
     */
    private String pauseMargain;
    /**
     * 常规接单等待时间
     */
    private Integer normalWaitTime;
    /**
     * 暂停接单等待时间
     */
    private Integer pauseWaitTime;
    /**
     * 跳水最大值
     */
    private BigDecimal oddsMaxValue;
    /**
     * 跳盘最大值
     */
    private BigDecimal marketMaxValue;
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
}
