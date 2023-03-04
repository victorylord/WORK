package com.panda.sport.rcs.mongo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * <p>
 * 足球玩法forecast
 * </p>
 *
 * @author author
 * @since 2021-02-19
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class PredictForecastVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 1：早盘 2滚球
     */
    private Integer matchType;

    /**
     * 玩法ID
     */
    private Long playId;

    /**
     * 比分-12到12之间
     */
    private Integer score;

    /**
     * 期望值
     */
    private BigDecimal profitValue;

}
