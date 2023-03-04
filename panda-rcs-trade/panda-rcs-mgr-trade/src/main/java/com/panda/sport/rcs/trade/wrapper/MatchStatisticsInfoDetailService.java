package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.enums.ScoreTypeEnum;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;

import java.util.List;
import java.util.Map;

public interface MatchStatisticsInfoDetailService {

    MatchStatisticsInfoDetail slectRedScore(Long matchId);

    List<MatchStatisticsInfoDetail> slectListByMatchId(Long matchId);

    /**
     * 通过 比分类型 获取赛事比分
     *
     * @param matchId
     * @param scoreTypeEnum
     * @return
     */
    MatchStatisticsInfoDetail getByScoreType(Long matchId, ScoreTypeEnum scoreTypeEnum);

    /**
     * 获取赛事所有类型比分
     *
     * @param matchId
     * @return
     */
    List<MatchStatisticsInfoDetail> getAllTypeScore(Long matchId);

    /**
     * 通过 比分类型 从 所有类型比分 中获取赛事比分
     *
     * @param list          赛事所有类型比分
     * @param scoreTypeEnum 比分类型
     * @return
     */
    MatchStatisticsInfoDetail getByAllTypeScore(List<MatchStatisticsInfoDetail> list, ScoreTypeEnum scoreTypeEnum);


    Map<String,String> fifteenSoreMap(Long matchId,Long categoryId);
}

