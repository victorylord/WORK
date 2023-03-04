package com.panda.sport.rcs.pojo;

import com.panda.sport.rcs.pojo.statistics.RcsMatchDimensionStatistics;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @ClassName MatchPeriod
 * @Description: TODO
 * @Author Vector
 * @Date 2019/11/19
 **/
@Data
public class MatchBetChange extends RcsMatchDimensionStatistics {
    /**
     * 全场比分
     */
    private String score;
    /**
     * 第一階段比分
     */
    private String set1Score;
    /**
     * 半场比分
     */
    private String periodScore;

    /**
     * 階段
     */
    private Integer period;
    /**
     * 开赛后的时间. 单位:秒.例如:3分钟11秒,则该值是 191
     */
    private Integer secondsMatchStart;

    /**
     * 比赛开始时间. 比赛开始时间 UTC时间
     */
    private Long beginTime;

    /**
     * 近一小时货量
     */
    private BigDecimal totalValueOneHour;

    private Integer matchPeriodId;

}