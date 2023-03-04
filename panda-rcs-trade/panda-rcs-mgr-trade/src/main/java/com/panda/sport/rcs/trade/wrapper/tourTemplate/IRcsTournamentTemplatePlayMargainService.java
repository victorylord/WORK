package com.panda.sport.rcs.trade.wrapper.tourTemplate;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;

import java.util.List;
import java.util.Map;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.tourTemplate
 * @Description :  联赛模板玩法margain配置
 * @Date: 2020-05-10 20:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface IRcsTournamentTemplatePlayMargainService extends IService<RcsTournamentTemplatePlayMargain> {

    /**
     * 获取赛事玩法配置
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     * @author Paca
     */
    RcsTournamentTemplatePlayMargain getMatchPlayTemplateConfig(Long matchId, Integer playId);
    /**
     * @Description   //获取联赛盘口差、水差变化率、赔率变化
     * @Param [config]
     * @Author  Sean
     * @Date  13:38 2020/10/20
     * @return com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef
     **/
    RcsTournamentTemplatePlayMargain getRcsTournamentTemplateConfig(RcsMatchMarketConfig config);

    /**
     * 查询多个赛事下面的玩法数据源
     * @param matchIds
     * @return
     */
    Map<String, String> queryDataSource(List<Long> matchIds);

    /**
     * 查询玩法数据源
     *
     * @param matchId
     * @return
     */
    Map<Long, String> queryDataSource(Long matchId);
}
