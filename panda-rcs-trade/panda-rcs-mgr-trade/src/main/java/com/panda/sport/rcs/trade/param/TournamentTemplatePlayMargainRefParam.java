package com.panda.sport.rcs.trade.param;

import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplatePlayMargainRefVo;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.param
 * @Description :  引用
 * @Date: 2020-05-12 19:36
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TournamentTemplatePlayMargainRefParam extends RcsTournamentTemplatePlayMargainRef {
    /**
     * 1：早盘  0：滚球
     */
    private Integer matchType;
    /**
     * 模板id
     */
    private Long templateId;
    /**
     * 模板名稱
     */
    private String templateName;
    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;
    /**
     * 異動前參數
     */
    private TournamentTemplatePlayMargainRefParam beforeParams;
    /**
     * sportId
     */
    private Integer sportId;
    /**
     * 赛事id
     */
    private Long matchId;
    /**
     * 玩法id
     */
    private Integer playId;
    /**
     * 球队
     */
    private List<MatchTeamInfo> teamList;
    /**
     *  赛事管理id
     */
    private String matchManageId;
    /**
     * 根据type设置（1：设值联赛等级  2：设值联赛id   3：设值赛事id）
     */
    private Long typeVal;
}
