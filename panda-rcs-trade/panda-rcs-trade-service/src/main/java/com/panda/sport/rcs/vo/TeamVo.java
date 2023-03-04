package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-11-19 19:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TeamVo {
    /**
     * @Description //赛事id
     * @return
     **/
    private Long id;
    /**
     * @Description //战队名字
     * @return
     **/
    private String text;
    /**
     * @Description //是否是主队或者客队
     * @return
     **/
    private String matchPosition;
}
