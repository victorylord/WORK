package com.panda.sport.rcs.trade.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

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
public class RcsUserConfigExtReqVo {
    /**
     * 用户id
     */
    private Long userId;

    /**
     * 商户编码
     */
    private String merchantCode;

    /**
     * 赔率分组动态风控开关 0关 1开
     */
    private Integer tagMarketLevelStatus;
}
