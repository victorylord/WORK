package com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness;

import lombok.Data;

@Data
public class TournamentTemplateAcceptEventSettleVo {
    /**
     * id
     */
    private Long id;
    /**
     * safety:安全事件 danger:危险事件 closing:封盘事件 reject:拒单事件
     */
    private String eventType;
    /**
     * 事件编码
     */
    private String eventCode;
    /**
     * 状态：1：选中 0：没有选中
     */
    private Integer status;
}
