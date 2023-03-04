package com.panda.sport.rcs.trade.param;

import com.panda.sport.rcs.vo.PageQuery;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  联赛模板查询类
 * @Date: 2020-05-10 20:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplateListParam extends PageQuery {
    /**
     * 赛事种类
     **/
    private Integer sportId;
    /**
     * 联赛级别
     **/
    private Integer tournamentLevel;
    /**
     * 联赛id
     **/
    private Long tournamentId;
    /**
     * 联赛名称
     **/
    private String tournamentName;
    /**
     * 父联赛的id
     */
    private String fatherTournamentId;
}
