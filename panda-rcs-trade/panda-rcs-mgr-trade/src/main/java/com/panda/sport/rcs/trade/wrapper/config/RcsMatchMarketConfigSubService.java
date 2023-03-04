package com.panda.sport.rcs.trade.wrapper.config;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;

import java.util.Collection;
import java.util.Map;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 赛事设置子表
 * @Author : Paca
 * @Date : 2021-09-28 15:29
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMatchMarketConfigSubService extends IService<RcsMatchMarketConfigSub> {

    /**
     * 获取带X玩法配置
     *
     * @param matchId
     * @param playId
     * @return
     */
    Map<Integer, Map<Long, RcsMatchMarketConfigSub>> getByPlayId(Long matchId, Long playId);

    /**
     * 获取带X玩法的盘口配置
     *
     * @param matchId
     * @param playIds
     * @return 玩法ID.位置.子玩法ID
     */
    Map<Long, Map<Integer, Map<Long, RcsMatchMarketConfigSub>>> getByPlayIds(Long matchId, Collection<Long> playIds);
}
