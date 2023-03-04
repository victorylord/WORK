package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-11-29 17:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentVoBySport {
    /**
     * @Description 联赛Id
     **/

    private Long standardTournamentId;
    /**
     * @Description 联赛名字
     **/
    private String text;
}
