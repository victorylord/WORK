package com.panda.sport.rcs.trade.wrapper;

import com.panda.merge.dto.Response;
import com.panda.sport.rcs.mongo.MarketConfigMongo;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.statistics.RcsMatchDimensionStatistics;
import com.panda.sport.rcs.trade.param.RcsMatchConfigParam;
import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;

import java.util.List;
import java.util.Map;

public interface MarketViewService {
    /**
     * 更新同联赛赛事实货量
     *
     * @param matchDimensionStatistics
     */
    boolean updateMatchBetChange(RcsMatchDimensionStatistics matchDimensionStatistics);

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //更新盘口数据
     * @Param [config]
     * @Author kimi
     * @Date 2020/2/17
     **/
    RcsMatchMarketConfig updateMatchMarketConfig(RcsMatchMarketConfig config);

    /**
     * @return
     * @Description: 查询滚球数量
     * @Author: Vector
     */
    Long getTraderMatchCount(MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    /**
     * 联赛正在开售滚球的有几场
     *
     * @param standardTournamentId
     * @return
     */
    Long getRollNum(Long standardTournamentId);

    /**
     * 获取滚球数量
     *
     * @return
     */
    Long getLiveNum(MarketLiveOddsQueryVo queryVo);

    /**
     * @Description: 查询联赛下拉列表
     * @auther: Enzo
     * @date: 2020/9/11 15:42
    **/
    public List<MatchMarketLiveBean> queryTournaments(MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    /**
     * @return void
     * @Description //次要玩法更新赔率
     * @Param [updateOddsValueVo]
     * @Author kimi
     * @Date 2020/3/10
     **/
    void updateOddsValue(UpdateOddsValueVo updateOddsValueVo);
    /**
     * @Description 次要玩法修改赔率
     * @auther: Enzo
     * @date: 2020/9/11 15:34
    **/
    void updateSnapOddsValue(UpdateOddsValueVo updateOddsValueVo);

     /**
      * @Description 快速修改赔率---赛前十五分钟
      * @auther: Enzo
      * @date: 2020/9/11 15:33
     **/
    void updateMarketOddsSnap(RcsMatchMarketConfig config);
    /**
     * 查询首页赛事查询列表
     *
     * @param marketLiveOddsQueryVo
     * @return
     */
    List<Map<String, Object>> getSelectMatchs(MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    /**
     * 无盘口数据做新增
     *
     * @param config
     */
//    void addMatchMarketConfig(RcsMatchMarketConfig config);


//    void updateRcsPlayConfigSendMsg(RcsPlayConfig config);

    /**
     * 更新操盘方式
     *
     * @param config
     * @return
     */
    void checkChangeMTS(RcsMatchConfigParam config);

    /**
     * 更新操盘方式
     *
     * @param config
     * @return
     */
    Response updateRiskManagerCodeByDataManager(RcsMatchConfigParam config);

    /**
     * @Description 修改操盘方式---赛前十五分钟
     * @auther: Enzo
     * @date: 2020/9/11 15:36
    **/
    void updateRiskManagerCodeSnapshot(RcsMatchConfigParam config);

    /**
     * @Description 修改盘口配置--赛前十五分钟
     * @auther: Enzo
     * @date: 2020/9/11 15:35
    **/
    RcsMatchMarketConfig updateMatchMarketMongo(RcsMatchMarketConfig config);

//    RcsMatchMarketConfig getMarketConfig(RcsMatchMarketConfig config);

    /**
     * @Description 查询前十五分钟盘口配置
     * @auther: Enzo
     * @date: 2020/9/11 15:35
    **/
    MarketConfigMongo getMarketConfigMongo(RcsMatchMarketConfig config);
}
