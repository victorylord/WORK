package com.panda.sport.rcs.enums;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.enums
 * @Description :  日志操作类
 * @Date: 2020-05-13 14:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public enum  OperationLogFlagEnum {
    /**
     * 联赛模板
     */
    TOURTEMPLATE("Tour_Template","联赛模板");


    private String code;

    private String name;



    OperationLogFlagEnum(String code,String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }
}
