package com.panda.sport.rcs.trade.param;

import com.panda.sport.rcs.pojo.MatchTeamInfo;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptEvent;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateEvent;
import com.panda.sport.rcs.trade.vo.tourTemplate.TournamentTemplateVo;
import groovy.transform.EqualsAndHashCode;
import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo.tourTemplate
 * @Description :  联赛模板
 * @Date: 2020-05-12 19:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class TournamentTemplateUpdateParam extends RcsTournamentTemplate {
    /**
     * 传给融合的操盘模式  PA or MTS
     */
    private String riskManagerCode;
    /**
     * @Description 事件配置
     * @Param 事件/结算审核时间
     * @Author toney
     * @Date 19:47 2020/5/12
     * @return
     **/
    private List<RcsTournamentTemplateEvent> templateEventList;
    /**
     * @Description 玩法margain配置
     * @Param
     * @Author toney
     * @Date 19:48 2020/5/12
     * @return
     **/
    private List<TournamentTemplatePlayMargainParam> playMargainList;

    /**
     * 滚球接拒单配置
     * 危险事件/安全事件/封盘事件/拒单事件
     */
    private List<RcsTournamentTemplateAcceptConfig> acceptConfigList;

    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;

    /**
     * 異動前資料
     */
    private TournamentTemplateVo beforeParams;

    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 球队
     */
    private List<MatchTeamInfo> teamList;

    /**
     *  赛事管理id
     */
    private String matchManageId;
}
