package com.panda.sport.rcs.pojo.danger;

import lombok.Data;

@Data
public class RcsDangerTeam {

    private Long id;

    private Long teamId;

    private Integer riskLevel;

    private Integer status;

    private Long createTime;

}
