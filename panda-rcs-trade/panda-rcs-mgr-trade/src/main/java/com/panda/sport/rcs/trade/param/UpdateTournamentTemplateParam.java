package com.panda.sport.rcs.trade.param;

import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  TODO
 * @Date: 2020-05-23 16:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class UpdateTournamentTemplateParam {
    /**
     * 联赛id
     */
    private Long id;
    /**
     * 早盘模板id
     */
    private Long preLemplateId;
    /**
     * 滚球模板id
     */
    private Long LiveLemplateId;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 时间戳
     */
    private Long timestamp;
    /**
     * 是否热门，1:是 0:否
     */
    private Integer isPopular;
    /**
     * 综合球类接单延迟时间
     */
    private Integer orderDelayTime;
    /**
     * 早盘模板名称
     */
    private String preLemplateName;
    /**
     * 滚球模板名称
     */
    private String liveLemplateName;
    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;
    /**
     * 異動前參數
     */
    private UpdateTournamentTemplateParam beforeParams;
    /**
     * sportId
     */
    private Integer sportId;
}
