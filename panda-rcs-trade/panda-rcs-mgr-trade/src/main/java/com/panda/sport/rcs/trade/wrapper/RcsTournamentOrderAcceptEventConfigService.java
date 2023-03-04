package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptEventConfig;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-02-01 16:14
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsTournamentOrderAcceptEventConfigService {
    /**
     * @return void
     * @Description //批量插入
     * @Param [list]
     * @Author kimi
     * @Date 2020/2/1
     **/
    void insertRcsTournamentOrderAcceptEventConfigs(List<RcsTournamentOrderAcceptEventConfig> list);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptEventConfig>
     * @Description //根据赛事id查询
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    List<RcsTournamentOrderAcceptEventConfig> selectRcsTournamentOrderAcceptEventConfigs(Long matchId);

    /**
     * @return void
     * @Description //批量更新
     * @Param [list]
     * @Author kimi
     * @Date 2020/2/1
     **/
    void updateRcsTournamentOrderAcceptEventConfigs(List<RcsTournamentOrderAcceptEventConfig> list);
}
