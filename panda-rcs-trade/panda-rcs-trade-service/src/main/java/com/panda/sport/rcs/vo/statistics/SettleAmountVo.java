package com.panda.sport.rcs.vo.statistics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  已结算值
 * @Date: 2019-12-04 10:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class SettleAmountVo {
    /**
     * 已结算金额
     */
    private BigDecimal settleBetAmount;
    /**
     * 已结算期望值
     */
    private BigDecimal settleAmount;
    /**
     * 期望值
     * 计算公式：已结算货量-已结算派奖
     */
    public BigDecimal getProfitValue(){
        if(settleAmount == null){
            settleAmount = BigDecimal.ZERO;
        }
        if(settleAmount == null){
            settleAmount = BigDecimal.ZERO;
        }
        return settleBetAmount.
                subtract(settleAmount).
                divide(BigDecimal.valueOf(100));
    }
}
