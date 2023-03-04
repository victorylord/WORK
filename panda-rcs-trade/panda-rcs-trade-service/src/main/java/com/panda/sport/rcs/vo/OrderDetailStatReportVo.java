package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-11-29 16:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OrderDetailStatReportVo implements Serializable {
    /**
     * 投注项ID
     */
    private Long playOptionsId;
    /**
     * 订单数
     */
    private Long betOrderNum;
    /**
     * 期望值
     */
    private Long profitValue;
    /**
     * 赔付金额
     */
    private Long paidAmount;
    /**
     * 总货量
     */
    private Long betAmount;

    /**
     盘口ID
     */
    private Long marketId;
    /**
    盘口值
     */
    private String marketValue;

    /**
     * 投注项类型
     */
    private String oddsType;

    /**
     * 当前赔率
     */
    private String oddsValue;
}
