package com.panda.sport.rcs.trade.param;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  日志请求类
 * @Date: 2020-05-13 21:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class LogParam {
    /**
     * 修改类型
     */
    private String type;
    /**
     * 修改前内容
     */
    private String preUpdateContent;

    /**
     * 修改后内容
     */
    private String updateContent;
    /**
     * 用户名
     */
    private String userName;
    /**
     * 更新时间
     */
    private Long updateTime;
}
