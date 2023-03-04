package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2020-03-05 16:38
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Component
public interface RcsTradeConfigMapper extends BaseMapper<RcsTradeConfig> {

    /**
     * @return com.panda.sport.rcs.pojo.RcsTradeConfig
     * @Description //TODO
     * @Param [matchId, playId, marketId]
     * @Author kimi
     * @Date 2020/3/6
     **/
    RcsTradeConfig selectRcsTradeConfig(@Param("matchId") String matchId, @Param("playId") String playId, @Param("marketId") String marketId);

    RcsTradeConfig getRcsTradeConfig(@Param("config") RcsTradeConfig rcsTradeConfig);

    /**
     * 获取操盘模式
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合，为空查询所有
     * @return
     */
    List<RcsTradeConfig> getTradeMode(@Param("matchId") String matchId, @Param("playIds") Collection<String> playIds);

    /**
     * 获取赛事下玩法的状态
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合，为空查询赛事下所有玩法的状态
     * @return
     * @author Paca
     */
    List<RcsTradeConfig> getPlayStatus(@Param("matchId") String matchId, @Param("playIds") Collection<String> playIds);

    /**
     * 通过玩法ID获取赛事下玩法集的状态
     *
     * @param matchId 赛事ID
     * @param playId  玩法ID
     * @return
     * @author Paca
     */
    RcsTradeConfig getPlaySetStatusByPlayId(@Param("matchId") String matchId, @Param("playId") Long playId);

    /**
     * 获取赛事下盘口位置的状态
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合，为空查询赛事下所有玩法下的位置状态
     * @return
     * @author Paca
     */
    List<RcsTradeConfig> getMarketPlaceStatus(@Param("matchId") String matchId, @Param("playIds") Collection<String> playIds);

    /**
     * 获取子玩法位置状态
     *
     * @param matchId
     * @param playIds
     * @param subPlayIds
     * @return
     */
    List<RcsTradeConfig> getSubPlayPlaceStatus(@Param("matchId") String matchId, @Param("playIds") Collection<String> playIds, @Param("subPlayIds") Collection<Long> subPlayIds);

    /**
     * 根据赛事获取赛事下非开的状态
     *
     * @param matchId
     * @return
     * @author Waldkir
     */
    List<RcsTradeConfig> getNotOpenStatusByMatchId(@Param("matchId") String matchId);

    /**
     * 根据赛事Id查询当前赛事状态
     *
     * @param matchId
     * @return
     */
    Integer selectStatusByMatchId(@Param("matchId") String matchId);

    /**
     * @param matchId
     * @return
     */
    List<RcsTradeConfig> getRcsTradeConfigStatusByMatchId(@Param("matchIdList") List<Integer> matchId);

    /**
     * 查询客户端玩法集显示配置
     * @param matchId
     * @return
     */

    List<RcsTradeConfig> queryCategoryShow(@Param("matchIds") List<Long> matchId,@Param("liveOdds")Integer liveOdds);


    void updateClientShow(@Param("config") RcsTradeConfig rcsTradeConfig);
}
