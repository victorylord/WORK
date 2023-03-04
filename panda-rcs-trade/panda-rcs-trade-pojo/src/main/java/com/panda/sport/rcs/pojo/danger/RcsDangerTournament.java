package com.panda.sport.rcs.pojo.danger;

import lombok.Data;

@Data
public class RcsDangerTournament {

    private Long id;

    private Long tournamentId;

    private Integer riskLevel;

    private Integer status;

    private Long createTime;

}
