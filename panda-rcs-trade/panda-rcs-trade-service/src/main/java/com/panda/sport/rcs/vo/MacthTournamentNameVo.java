package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-06 13:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MacthTournamentNameVo {
    /**
     * @Description 联赛id
     **/
    private Long id;
    /**
     * @Description 联赛编码
     **/
    private Long nameCode;
    /**
     * @Description 联赛名字类型
     **/
    private String languageType;
    /**
     * @Description 联赛名字
     **/
    private String text;
}
