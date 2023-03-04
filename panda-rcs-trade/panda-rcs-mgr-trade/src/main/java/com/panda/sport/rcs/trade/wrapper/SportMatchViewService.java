package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.mongo.*;
import com.panda.sport.rcs.pojo.dto.QueryPreLiveMatchDto;
import com.panda.sport.rcs.trade.utils.mongopage.PageResult;
import com.panda.sport.rcs.trade.vo.ChangePersonLiableVo;
import com.panda.sport.rcs.vo.CategoryConVo;
import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;
import com.panda.sport.rcs.vo.ThirdMarketVo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;
import java.util.Map;

public interface SportMatchViewService {

    /**
     * 查询主界面玩法id列表
     * @param categoryConVo
     * @return
     */
    List<Long> queryMainCategorySetIds(CategoryConVo categoryConVo);

    /**
     * 查询赛事id列表
     * @param marketLiveOddsQueryVo
     * @return
     */
    List<Long> queryMatchIdList(MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    /**
     * 查询赛事玩法盘口赔率
     * @param marketLiveOddsQueryVo
     * @return
     */
    PageResult<MatchMarketLiveBean> queryMatchList(MarketLiveOddsQueryVo marketLiveOddsQueryVo);

    /**
     * 查询赛事玩法盘口赔率
     * @param marketLiveOddsQueryVo
     * @return
     */
    PageResult<MatchMarketLiveBean> queryMatchList(MarketLiveOddsQueryVo marketLiveOddsQueryVo, Criteria criteria);
    /**
     * 给forcast构建查询mongo赛事条件
     * @param queryVo
     * @return
     */
    Criteria buildMongoCriteria(MarketLiveOddsQueryVo queryVo, Integer matchType);

    /**
     * 查询mongo赛事条件
     * @param queryVo
     * @return
     */
    Criteria buildMongoCriteria(MarketLiveOddsQueryVo queryVo);

    Query buildMongoQuery(MarketLiveOddsQueryVo queryVo);

    /**
     * 查询足球赛前赛事收藏
     * @param matchs
     * @param tradeId
     * @return
     */
    List<MatchMarketLiveBean> existList(List<MatchMarketLiveBean> matchs, Long tradeId);


    /**
     * 及时注单联赛查询条件
     * @param queryVo
     * @return
     */
    Criteria timelyCriteria(MarketLiveOddsQueryVo queryVo);


    /**
     * 修改操盘手
     * @param vo
     */
    void updateTrader(ChangePersonLiableVo vo);

    /**
     * 修改赛事置顶
     * @param matchTop
     */
    void updateMatchTop(MatchTop matchTop);


    void updateWarningSign(Long matchId,Long categoryId );


    boolean isOwnTrade(Long matchId,Integer traderId);

    /**
     * 查询自选赛事
     * @param queryVo
     * @return
     */
    List<Long> queryMyselfMatchs(MarketLiveOddsQueryVo queryVo);


    PageResult<MatchMarketLiveBean> queryMyselfMatch(MarketLiveOddsQueryVo queryVo);

    List<MatchMarketLiveBean> tansferMatchInfo(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo);


    List<StandardTxThirdMarketPlayDTO> queryMultiOdds(MarketLiveOddsQueryVo queryVo);

    Map<String,List<ThirdMarketVo>> getMultiOdds(MarketLiveOddsQueryVo queryVo);

}
