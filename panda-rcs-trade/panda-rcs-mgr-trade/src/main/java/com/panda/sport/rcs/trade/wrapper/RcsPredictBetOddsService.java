package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;

import java.util.List;
import java.util.Map;

/**
 * 盘口位置统计表
 *
 * @author Enzo
 * @since 2020-10-04
 */

public interface RcsPredictBetOddsService extends IService<RcsPredictBetOdds> {
    /**
     * 查询位置实货量
     *
     * @param matchIds
     * @param seriesType 
     * @return
     */
    Map<Long, Map<String, List<RcsPredictBetOdds>>> queryBetOdds(List<Long> matchIds, Integer dataType, Integer seriesType);

    /**
     * 查询位置实货量
     *
     * @param matchId
     * @return
     */
    Map<String, List<RcsPredictBetOdds>> queryPlaceBetNums(Long matchId,Integer seriesType);
}
