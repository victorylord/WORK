package com.panda.rcs.warning.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  TODO
 * @Date: 2022-07-22 20:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchOperateExListVo {
    private Long matchId;
    private Long eventTime;
    private Long beginTime;
    private Integer sportId;
    private Integer tournamentLevel;
    private Long tourNameCode;
    private String teamNameCode;
    private Long standardTournamentId;
    private Integer matchStatus;
}
