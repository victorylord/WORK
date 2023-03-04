package com.panda.rcs.warning.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  TODO
 * @Date: 2022-07-19 15:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RollBallMatchInfo implements Serializable {
    //开赛时间
    private Long timer;
    private Long matchId;
    private Integer playId;
    //0早盘。1滚球
    private Integer matchType;
    //求种ID
    private Integer sportId;
    //联赛等级
    private Integer tournamentLevel;
    //联赛ID
    private Long standardTournamentId;
    //赛事ID
    private Long matchManageId;

}
