package com.panda.sport.rcs.trade.wrapper.tourTemplate;

import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplateAcceptConfigSettle;
import com.panda.sport.rcs.trade.param.TournamentTemplateUpdateParam;
import com.panda.sport.rcs.trade.param.UpdateTournamentLevelParam;
import com.panda.sport.rcs.trade.vo.tourTemplate.templateToBusiness.MatchTournamentTemplateVo;

public interface TournamentTemplatePushService {

    /**
     * 推送变更联赛等级
     *
     * @param param
     */
    void putTournamentLevel(UpdateTournamentLevelParam param);

    /**
     * 发送mq推送更新比分源数据
     *
     * @param param
     */
    void putTournamentTemplateMatchScoreSourceData(TournamentTemplateUpdateParam param);

    /**
     * 发送mq推送联赛模板赛事和事件数据
     *
     * @param param
     */
    void putTournamentTemplateMatchEventData(TournamentTemplateUpdateParam param);

    /**
     * 发送mq推送联赛模板玩法数据
     *
     * @param param
     */
    void putTournamentTemplatePlayData(TournamentTemplateUpdateParam param);

    /**
     * 赛事模板同步联赛模板新增玩法到开售列表
     *
     * @param param
     */
    void putMatchSyncTourTempPlayData(TournamentTemplateUpdateParam param);

    /**
     * 全量发送mq推送联赛模板结算接拒单数据至业务
     *
     * @param param
     */
    void putTournamentTemplateSettleData(TournamentTemplateUpdateParam param);
    /**
     * 增量发送mq推送联赛模板结算接拒单数据至业务
     *
     * @param param
     */
    void putTournamentTemplateSettleDataByIncrement(RcsTournamentTemplateAcceptConfigSettle param);

    /**
     * 获取接拒结算事件数据
     * @param template
     * @return
     */
    MatchTournamentTemplateVo getTourTempAcceptSettleConfig(RcsTournamentTemplate template);
}
