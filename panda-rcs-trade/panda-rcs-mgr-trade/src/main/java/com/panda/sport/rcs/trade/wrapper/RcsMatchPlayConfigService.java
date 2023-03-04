package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.impl
 * @Description :  TODO
 * @Date: 2019-11-08 20:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

public interface RcsMatchPlayConfigService extends IService<RcsMatchPlayConfig> {

    RcsMatchPlayConfig selectRcsMatchPlayConfig(RcsMatchMarketConfig config);

    void insertOrUpdateMarketHeadGap(RcsMatchMarketConfig config);
    /**
     * @Description   //获取玩法水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/14
     * @return java.lang.String
     **/
    String getPlayWaterDiff(RcsMatchMarketConfig config);

    /**
     * 清除盘口差
     *
     * @param matchId
     * @param playIds
     */
    void clearMarketHeadGap(Long matchId, Collection<Long> playIds);

    /**
     * 获取所有玩法和子玩法配置
     *
     * @param matchId
     * @param playId
     * @return
     */
    Map<Long, RcsMatchPlayConfig> getByPlayId(Long matchId, Long playId);

    /**
     * 获取玩法配置
     *
     * @param matchId
     * @param playIds
     * @return
     */
    Map<Long, Map<Long, RcsMatchPlayConfig>> getByPlayIds(Long matchId, Collection<Long> playIds);

    Map<String, Integer> queryRelevanceType(List<Long> matchIds);

}
