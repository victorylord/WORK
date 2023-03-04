package com.panda.sport.rcs.trade.param;

import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  玩法margain值
 * @Date: 2020-05-12 19:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplatePlayMargainParam extends RcsTournamentTemplatePlayMargain {
    /**
     * 分时margin
     */
    private List<TournamentTemplatePlayMargainRefParam> playMargainRefParamList;

    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;

    /**
     * 異動前資料
     */
    private TournamentTemplatePlayMargainParam beforeParams;

    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * sportId
     */
    private Integer sportId;

    /**
     * 球队
     */
    private List<MatchTeamInfo> teamList;

    /**
     *  赛事管理id
     */
    private String matchManageId;
}
