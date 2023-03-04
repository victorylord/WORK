package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-11-19 19:05
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ScoreVo {
    /**
     * @Description //赛事id
     * @return
     **/
    private Long id;
    /**
     * @Description //赛事比分
     * @return
     **/
    private String score;
    /**
     * @Description //比赛进行了多久
     * @return
     **/
    private Integer secondsMatchStart = 0;
    /**
     * 比赛阶段id. 取自基础表 : match_status.id
     */
    private Long period;
}
