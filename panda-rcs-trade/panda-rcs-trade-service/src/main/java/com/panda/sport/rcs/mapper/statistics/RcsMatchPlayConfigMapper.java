package com.panda.sport.rcs.mapper.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.vo.trade.WaterDiffRelevanceReqVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  TODO
 * @Date: 2020-01-15 21:19
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsMatchPlayConfigMapper extends BaseMapper<RcsMatchPlayConfig> {
//    /**
//     * @return void
//     * @Description //根据玩法id进行更新或者插入
//     * @Param [matchId, playId, status, dataSource]
//     * @Author kimi
//     * @Date 2020/1/15
//     **/
//    void inserOrUpdate(@Param("matchId") Long matchId, @Param("playId") Integer playId, @Param("status") Integer status, @Param("dataSource") Integer dataSource);

//    /**
//     * @return void
//     * @Description //TODO
//     * @Param [matchId, playId, status, dataSource]
//     * @Author kimi
//     * @Date 2020/2/18
//     **/
//    void inserOrUpdateList(@Param("matchId") Long matchId, @Param("playIds") List<Integer> playIds, @Param("status") Integer status, @Param("dataSource") Integer dataSource);
    /**
     * @Description   //更新盘口差或者水差
     * @Param [config]
     * @Author  Sean
     * @Date  16:47 2020/10/6
     * @return void
     **/
    void insertOrUpdateMarketHeadGap(@Param("config") RcsMatchMarketConfig config);
    /**
     * @Description   //清空盘口差
     * @Param [standardMatchInfo]
     * @Author  sean
     * @Date   2020/12/12
     * @return void
     **/
    void clearMarketHeadGapByMatch(@Param("match") StandardSportMarket market);

//    /**
//     * 更新水差关联标志
//     *
//     * @param matchId       赛事ID
//     * @param playId        玩法ID
//     * @param relevanceType 水差关联标志，0-不关联，1-关联
//     */
//    void insertOrUpdateRelevanceType(@Param("config") WaterDiffRelevanceReqVo reqVo);
}
