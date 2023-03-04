package com.panda.sport.rcs.vo.statistics;


import lombok.Data;

/**
 * @author  holly
 */

@Data
public class MarketBalanceVo {
    /**
     * 赛事Id
     */
    private Long marketId;
    /**
     * 盘口值
     */
    private String marketValue;
    /**
     * 主队投注额
     */
    private Long homeAmount = 0L;
    /**
     * 客队投注额
     */
    private Long awayAmount = 0L;
    /**
     * 和投注额
     */
    private Long tieAmount = 0L;
    /**
     *  平衡值
     */
    private Long balanceValue = 0L;
}
