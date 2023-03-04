package com.panda.sport.rcs.trade.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * 用戶特殊配置扩展
 *
 * @description:
 * @author: magic
 * @create: 2022-05-14 18:15
 **/
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMerchantUserTagMarketLevelStatusReqVo {
    /**
     * 商户CODE
     */
    private String merchantCode;

    /**
     * 用户限额百分比
     */
    private BigDecimal percentageLimit;

    /**
     * 赔率分组动态风控开关 0关 1开
     */
    private Integer tagMarketLevelStatus;
}
