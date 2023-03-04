package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-26 10:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ConditionVo implements Serializable {

    private Long nameCode;
    private Long id;
    private Long sportId;
    private String text;
    private String languageType;

}
