package com.panda.sport.rcs.trade.vo.tourTemplate.otherSystem;

import lombok.Data;

import java.io.Serializable;

/**
 * @author carver
 */
@Data
public class TournamentLevelTemplateVo implements Serializable {
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 1：级别  2：联赛
     */
    private Integer type;
    /**
     * 根据type设置（1：联赛等级  2：联赛id）
     */
    private Long typeVal;
    /**
     * 联赛名称
     */
    private String tournamentName;
    /**
     * 联赛英文名称
     */
    private String tournamentEnglishName;
    /**
     * 盘口类型 1：早盘  0：滚球
     */
    private Integer matchType;
}
