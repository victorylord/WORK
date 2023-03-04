package com.panda.rcs.warning.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MatchMonitorMqAndPayBean implements Serializable {
    private Long matchId;
    private Integer categoryId;
    private Long dataSourceTime;
    private Integer marketType;
}
