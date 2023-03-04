package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-03-05 16:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsTradeConfigService extends IService<RcsTradeConfig> {

    /**
     * 根据等级获取最新的状态
     *
     * @param matchId
     * @param tradeLevelEnum
     * @param targetId
     * @return
     */
    Integer getLatestStatusByLevel(Long matchId, TraderLevelEnum tradeLevelEnum, Long targetId);

    /**
     * 获取操盘类型
     *
     * @param matchId
     * @param playId
     * @return
     */
    Integer getDataSource(Long matchId, Long playId);

    /**
     * 获取操盘模式
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合，为空查询所有
     * @return
     */
    Map<Long, Integer> getTradeMode(Long matchId, Collection<Long> playIds);

    /**
     * 获取非自动操盘所有玩法
     *
     * @param matchId
     * @return
     */
    List<Long> getNotAutoPlayIds(Long matchId);

    /**
     * 获取赛事状态配置
     *
     * @param matchId
     * @return
     * @author Paca
     */
    RcsTradeConfig getMatchStatusConfig(Long matchId);

    /**
     * 获取赛事状态
     *
     * @param matchId
     * @return
     * @author Paca
     */
    Integer getMatchStatus(Long matchId);

    /**
     * 批量获取玩法状态配置
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合
     * @return
     * @author Paca
     */
    Map<Long, RcsTradeConfig> getPlayStatus(Long matchId, Collection<Long> playIds);

    /**
     * 通过玩法ID获取玩法集状态配置
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     * @author Paca
     */
    RcsTradeConfig getPlaySetStatusByPlayId(Long matchId, Long playId);

    /**
     * 获取玩法下所有盘口位置状态配置
     *
     * @param matchId
     * @param playId
     * @return
     * @author Paca
     */
    Map<Integer, RcsTradeConfig> getMarketPlaceStatus(Long matchId, Long playId);

    /**
     * 批量获取玩法下所有盘口位置状态配置
     *
     * @param matchId
     * @param playIds
     * @return
     * @author Paca
     */
    Map<Long, Map<Integer, RcsTradeConfig>> getMarketPlaceStatus(Long matchId, Collection<Long> playIds);

    /**
     * 获取子玩法位置状态
     *
     * @param matchId
     * @param playId
     * @param subPlayId
     * @return
     */
    Map<Integer, RcsTradeConfig> getSubPlayPlaceStatus(Long matchId, Long playId, Long subPlayId);

    /**
     * 获取子玩法位置状态
     *
     * @param matchId
     * @param playIds
     * @param subPlayIds
     * @return
     */
    Map<Long, Map<Long, Map<Integer, RcsTradeConfig>>> getSubPlayPlaceStatus(Long matchId, Collection<Long> playIds, Collection<Long> subPlayIds);

    /**
     * 根据赛事获取赛事下非开的状态
     *
     * @param matchId
     * @return
     * @author Waldkir
     */
    List<RcsTradeConfig> getNotOpenStatusByMatchId(Long matchId);

    /**
     * 保存操盘状态配置
     *
     * @param updateVO
     * @author Paca
     */
    void saveTradeStatusConfig(MarketStatusUpdateVO updateVO);

    /**
     * @Description //是否MTS操盘
     * @Param [matchId]
     * @Author sean
     * @Date 2020/11/15
     **/
    void isMtsTrade(RcsMatchMarketConfig config,Integer sportId);

    /**
     * @param matchId
     * @return
     */
    HashMap<Integer, RcsTradeConfig> getRcsTradeConfigStatusByMatchId(List<Integer> matchId);

    /**
     * 冠军赛事，操盘方式切换
     *
     * @param config
     */
    void championMatchTradeType(MarketStatusUpdateVO config);

    /**
     * 冠军赛事，开关封锁
     *
     * @param config
     */
    void championMatchTradeStatus(MarketStatusUpdateVO config);

    /**
     * 客户端玩法集展示
     * @param vo
     */
    void updateShow(MarketStatusUpdateVO vo);

    Map<String, Integer> queryCategoryShow(List<Long> matchIds,Integer liveOdds);

}
