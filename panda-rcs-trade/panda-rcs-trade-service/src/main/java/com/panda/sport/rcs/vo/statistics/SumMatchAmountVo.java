package com.panda.sport.rcs.vo.statistics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  赛事维度总货量
 * @Date: 2019-12-04 10:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class SumMatchAmountVo {
    /**
     * 下注总金额
     */
    private BigDecimal betAmount;
    /**
     * 下注总注数
     */
    private Long betOrderNums;

    /**
     * 赔率
     */
    private BigDecimal paiAmount;
    /**
     * 已结算金额
     */
    private BigDecimal settleAmount;
    /**
     * 已结算注单总金额
     */
    private BigDecimal settleOrderBetAmount;
}
