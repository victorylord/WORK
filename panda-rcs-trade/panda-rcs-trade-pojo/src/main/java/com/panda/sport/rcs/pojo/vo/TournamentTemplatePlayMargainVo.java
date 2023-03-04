package com.panda.sport.rcs.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

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
     * 0.5球头相邻盘口赔率分差
     */
    private BigDecimal headMarketNearOddsDiff;

    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 数据源
     */
    private String dataSource;
    /**
     * 赔率变动接拒开关（0.关 1.开）
     */
    private Integer oddsChangeStatus;
    /**
     * 赔率接拒变动范围
     */
    private BigDecimal oddsChangeValue;
}
