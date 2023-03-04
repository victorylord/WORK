package com.panda.sport.rcs.trade.param;

import lombok.Data;

/**
 * @Describtion
 * @Auther jstyDC
 * @Date 2022-05-2022/5/3 16:19
 */
@Data
public class TournamentTemplateUpdatePendingOrderParam {
    /**
     * id
     */
    private Long id;
    /**
     * 商户单场预约赔付限额
     */
    private Long businesPendingOrderPayVal;
    /**
     * 用户单场预约赔付限额
     */
    private Long userPendingOrderPayVal;
    /**
     * 用户预约中笔数
     */
    private Integer userPendingOrderCount;
    /**
     * 预约投注速率
     */
    private Integer pendingOrderRate;
}
