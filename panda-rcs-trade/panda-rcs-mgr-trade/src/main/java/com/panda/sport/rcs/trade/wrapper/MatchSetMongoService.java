package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.mongo.*;

import java.util.List;
import java.util.Map;

public interface MatchSetMongoService {

    void matchLevelSnap(MatchMarketLiveBean match, Map<Long, List<MatchSetVo>> map);

    void categoryLevelSnap(MarketCategory category, Map<Long, List<MatchSetVo>> map);

    void marketLevelSnap(MatchMarketVo matchMarketVo, Map<Integer, List<MatchSetVo>> map, Integer marketPlaceNum);

    Map<Long, List<MatchSetVo>> queryMatchLevelSnap(List<MatchMarketLiveBean> matchs);

    Map<Long, List<MatchSetVo>> queryCategoryLevelSnap(Long matchId);

    Map<Integer, List<MatchSetVo>> queryMarketLevelSnap(Long matchId, Long categoryId);

    void updateMatchSet(MatchSetVo matchSetVo);

    List<MarketConfigMongo> queryMarketConfig(Long matchId);

    void upsertMatchSetMongo(MatchSetVo matchSetVo);

    void updateMarketConfig(List<MarketConfigMongo> marketConfigMongos, Long matchId, Long categoryId, MatchMarketVo matchMarketVo);

    List<MarketCategory> getCategoriesTradeType(Long matchId, Integer matchSnapshot);

    /**
     * 获取赛前15分钟玩法操盘类型
     *
     * @param matchId
     * @return
     */
    Map<Long, Integer> getCategorySnapshotTradeType(Long matchId);

    /**
     * 获取赛前15分钟玩法集操盘类型
     *
     * @param matchId
     * @return
     */
    Map<Long, Integer> getCategorySetSnapshotTradeType(Long matchId);

    void upsertMatchCategorySetMongo(MatchCategorySetVo vo);

}
