package com.panda.sport.rcs.pojo.tourTemplate;


import lombok.Data;

import java.io.Serializable;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  po模板设置承载类
 * @Date: 2022-03-05 14:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class AoParameterTemplateReq implements Serializable {

    private static final long serialVersionUID = 4288793447642155970L;
    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 模板id
     */
    private Long templateId;
    /**
     * ao数据源配置
     */
    private String aoConfigValue;
    /**
     * 赛种
     */
    public Integer sportId;
    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;
}
