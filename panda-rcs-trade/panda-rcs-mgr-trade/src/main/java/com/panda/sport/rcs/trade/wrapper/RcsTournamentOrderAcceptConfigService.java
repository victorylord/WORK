package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-02-01 13:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsTournamentOrderAcceptConfigService {
    /**
     * @return com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig
     * @Description //获取联赛配置
     * @Param [tournamentId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    RcsTournamentOrderAcceptConfig selectByTournamentId(Long tournamentId);

    /**
     * @return void
     * @Description //插入数据
     * @Param [rcsTournamentOrderAcceptConfig]
     * @Author kimi
     * @Date 2020/2/1
     **/
    void insertByTournamentId(RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig);

    /**
     * @return void
     * @Description //更新数据
     * @Param [rcsTournamentOrderAcceptConfig]
     * @Author kimi
     * @Date 2020/2/1
     **/
    void update(RcsTournamentOrderAcceptConfig rcsTournamentOrderAcceptConfig);

    /**
     * @return void
     * @Description //初始化联赛数据
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/1
     **/
    RcsTournamentOrderAcceptConfig init(Long tournamentId);
}
