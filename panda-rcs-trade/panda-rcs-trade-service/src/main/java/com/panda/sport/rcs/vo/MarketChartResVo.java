package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MarketChartResVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事Id
     */
    private Long matchId;

    /**
     * 类型：玩法ID
     */
    private Integer playId;


    /**
     * 盘口id
     */
    private Long marketId;

    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;


    /**
     * 投注项id
     */
    private Long playOptionsId;

    /**
     * 投注项标识
     */
    private String playOptions;

    /**
     * 投注金额(分)
     */
    private Long totalBetAmount;

    /**
     * 标签
     */

    private Integer userLevel;


}
