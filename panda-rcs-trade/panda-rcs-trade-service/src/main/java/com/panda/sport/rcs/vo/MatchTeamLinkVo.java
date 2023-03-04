package com.panda.sport.rcs.vo;

import lombok.Data;

@Data
public class MatchTeamLinkVo {

    private Long teamId;

    private Long matchId;

    private String homeAway;

}
