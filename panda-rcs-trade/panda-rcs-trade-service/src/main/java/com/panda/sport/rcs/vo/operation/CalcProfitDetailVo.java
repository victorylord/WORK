package com.panda.sport.rcs.vo.operation;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.vo.operation
 * @Description :  期望值
 * @Date: 2019-12-10 16:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class CalcProfitDetailVo {
    /**
     * 盘口Id
     */
    private Long marketId;
    /**
     * 赔率Id
     */
    private Long oddsId;
    /**
     * 下注总金额
     */
    private BigDecimal betAmount;
    /**
     * 最大赔付金额
     */
    private BigDecimal paiAmount;
    /**
     * 盘口值
     */
    private Integer marketValue;
    /**
     * 主客队：1注队2客队
     */
    private String oddsType;
}
