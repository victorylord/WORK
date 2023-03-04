package com.panda.sport.rcs.trade.vo.tourTemplate;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  TODO
 * @Date: 2020-05-22 17:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateEventVo {
    /**
     * 主键id
     */
    private Long id;

    /**
     * 模板id
     */
    private Long templateId;

    /**
     * 事件编码
     */
    private String eventCode;

    /**
     * 事件描述
     */
    private String eventDesc;

    /**
     * 审核时间
     */
    private Integer eventHandleTime;

    /**
     *结算时间
     */
    private Integer settleHandleTime;

    /**
     * 排序
     */
    private Integer sortNo;
}
