package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsMarketNumStatis;

import java.util.List;
import java.util.Map;

/**
 * 盘口位置统计表
 *
 * @author Enzo
 * @since 2020-10-04
 */

public interface RcsMarketNumStatisService extends IService<RcsMarketNumStatis> {
    /**
     * 查询位置实货量
     *
     * @param matchIds
     * @return
     */
    Map<Long, Map<String, List<RcsMarketNumStatis>>> queryPalceBetNums(List<Long> matchIds);

    /**
     * 查询位置实货量
     *
     * @param matchId
     * @return
     */
    Map<Integer, Map<Integer, Map<String, RcsMarketNumStatis>>> queryPlaceBetNums(Long matchId);
}
