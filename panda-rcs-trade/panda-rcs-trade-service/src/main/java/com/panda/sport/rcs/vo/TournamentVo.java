package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  联赛信息
 * @Date: 2019-10-15
 */
@Data
public class TournamentVo implements Serializable {

    private Long id;
    private Long sportId;
    private String tournamentLevel;
    private String tournamentInfo;
    private String code;
    private String value;

}
