package com.panda.sport.rcs.trade.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author carver
 * 盘口期望值
 */

@Data
public class MarketProfitVo {
    /**
     * 比分
     */
    private Integer score;

    /**
     * 期望值
     */
    private BigDecimal profitValue;
}
