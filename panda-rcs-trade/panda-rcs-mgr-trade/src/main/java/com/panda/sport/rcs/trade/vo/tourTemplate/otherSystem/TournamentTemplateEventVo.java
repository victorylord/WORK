package com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem;

import lombok.Data;

import java.io.Serializable;

/**
 * 事件审核数据
 */
@Data
public class TournamentTemplateEventVo implements Serializable {
    /**
     * 事件code
     */
    private String eventCode;

    /**
     * 事件审核时间
     */
    private Integer eventHandleTime;

    /**
     * 结算审核时间
     */
    private Integer settleHandleTime;
}
