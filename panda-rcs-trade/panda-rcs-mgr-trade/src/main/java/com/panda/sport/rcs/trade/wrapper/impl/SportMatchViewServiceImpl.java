package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Variable;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.common.OddsValueConvertUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplateMapper;
import com.panda.sport.rcs.mongo.*;
import com.panda.sport.rcs.mongo.ScoreVo;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.StandardSportTournament;
import com.panda.sport.rcs.pojo.dto.TemplateNameForMatchDto;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;
import com.panda.sport.rcs.pojo.vo.ThirdDataSourceCodeVo;
import com.panda.sport.rcs.trade.enums.BasketballEnum;
import com.panda.sport.rcs.trade.enums.CategoryShowEnum;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.enums.PeriodEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.NumberConventer;
import com.panda.sport.rcs.trade.utils.mongopage.LookupPipelineOperation;
import com.panda.sport.rcs.trade.utils.mongopage.MongoPageHelper;
import com.panda.sport.rcs.trade.utils.mongopage.PageResult;
import com.panda.sport.rcs.trade.vo.ChangePersonLiableVo;
import com.panda.sport.rcs.trade.vo.tourTemplate.AoBasketBallTemplateConfigEntity;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.utils.BeanCopyAllDeepUtils;
import com.panda.sport.rcs.utils.ListUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import com.panda.sport.rcs.utils.PlayTemplateUtils;
import com.panda.sport.rcs.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RcsConstant.*;
import static com.panda.sport.rcs.constants.RedisKey.*;

@Service("sportMatchViewServiceImpl")
@Slf4j
public class SportMatchViewServiceImpl implements SportMatchViewService {

    static final String MATCH_TEAM_KEY ="rcs:key:match:team:";

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MongoService mongoService;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MongoPageHelper mongoPageHelper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private MarketStatusService marketStatusService;
    @Autowired
    private MatchSetMongoService matchSetMongoService;
    @Autowired
    private RcsMatchCollectionService collectionService;
    @Autowired
    private RcsStandardSportMarketSellService sellService;
    @Autowired
    private RcsMatchCollectionMapper matchCollectionMapper;
    @Autowired
    private RcsTradingAssignmentMapper tradingAssignmentMapper;
    @Autowired
    private RcsPredictBetOddsService predictBetOddsService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private MarketCategorySetService marketCategorySetService;
    @Autowired
    private RcsTradeConfigService tradeConfigService;
    @Autowired
    private RcsMatchPlayConfigService playConfigService;
    @Autowired
    private RcsMatchCollectionMapper rcsMatchCollectionMapper;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService templatePlayMargainService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;

    @Autowired
    private StandardSportTeamMapper standardSportTeamMapper;
    
    @Autowired
    private RcsTournamentTemplateMapper templateMapper;

    public static String MONGOMATCHTOP = "match_top";

    public static String MATCH_QUERY_REDIS = "match:query:set";

    @Override
    public List<Long> queryMainCategorySetIds(CategoryConVo queryVo) {
        //运动种类id
        Long sportId = queryVo.getSportId();

        //玩法集id
        Long categorySetId = queryVo.getId();

        //查询赛种和玩法集对应的全部玩法id
        List<Long> mainCategorySetIds = categoryService.mainCategorySetIds(sportId, categorySetId);

        return mainCategorySetIds;
    }

    @Override
    public List<Long> queryMatchIdList(MarketLiveOddsQueryVo queryVo) {

        //操盘手id
        Long traderId = queryVo.getTradeId();

        //运动种类id
        Long sportId = queryVo.getSportId();

        //赛事类型
        Integer chooseType = queryVo.getChooseType();

        //早盘滚球操盘标识
        Integer oddBusiness = queryVo.getLiveOddBusiness();

        List<Long> returnMatchIdList = null;

        try {

            //查询指派的赛事id列表
            List<Long> traderMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(traderId), oddBusiness);

            //1 滚球所以自选 2 仅自己操盘 3 仅自己收藏 4 早盘的所有赛事
            RcsMatchCollection co = new RcsMatchCollection();
            co.setUserId(traderId);
            co.setSportId(sportId);
            co.setMatchType(queryVo.getLiveOddBusiness() == 0 ? 1 : 2);
            co.setBeginTime(System.currentTimeMillis() - FOUR_HOURS);

            //自己操盘的
            List<Long> traderMyself = sellService.queryTraderMatchIds(co);

            if (chooseType.equals(1)) {
                //1.滚球所以自选
                List<Long> allCollMatchIds = collectionService.queryCollMatchIds(co, traderMatchIds);
                if (!CollectionUtils.isEmpty(traderMyself)) {
                    allCollMatchIds.addAll(traderMyself);
                }
                returnMatchIdList = allCollMatchIds;

            } else if (chooseType.equals(2)) {
                //2.仅自己操盘 = 指派的 + 自己操盘的
                List<Long> allMyselfMatchIds = new ArrayList<>();

                if (!CollectionUtils.isEmpty(traderMatchIds)) {
                    allMyselfMatchIds.addAll(traderMatchIds);
                }
                if (!CollectionUtils.isEmpty(traderMyself)) {
                    allMyselfMatchIds.addAll(traderMyself);
                }
                returnMatchIdList = allMyselfMatchIds;

            } else if (chooseType.equals(3)) {
                //3.仅自己收藏 = 联赛收藏下的+赛事收藏的
                returnMatchIdList = collectionService.queryCollMatchIds(co, null);

            } else if (chooseType.equals(4)) {
                //4.早盘的所有赛事
                List<Long> allPreMatchIds = standardMatchInfoMapper.getAllPreMatchIds();
                allPreMatchIds = CollectionUtils.isEmpty(allPreMatchIds) ? Lists.newArrayList() : allPreMatchIds;
                returnMatchIdList = allPreMatchIds;
            }

        } catch (Exception ex) {
            log.error("::" + CommonUtil.getRequestId(traderId, sportId) + "::queryMatchIdList查询异常", ex);
            return null;
        }

        return returnMatchIdList;
    }

    /**
     * 赛事玩法，盘口赔率
     */
    @Override
    public PageResult<MatchMarketLiveBean> queryMatchList(MarketLiveOddsQueryVo queryVo) {
        // 操盘手ID
        String traderId = queryVo.getTradeId().toString();
        // 体育类型ID
        Long sportId = queryVo.getSportId();
        // mongo查询组装对象
        List<AggregationOperation> commonOperations = new ArrayList<>();
        // 查询条件组装
        Criteria criteria = buildMongoCriteria(queryVo);
        commonOperations.add(Aggregation.match(criteria));

        List<Variable<String>> lets = new ArrayList<>();
        Variable<String> let1 = new Variable<>("trade_id", traderId);
        Variable<String> let2 = new Variable<>("match_id", "$matchId");
        lets.add(let1);
        lets.add(let2);

        Bson pipeline = new BasicDBObject("$match",
                new BasicDBObject("$expr", new BasicDBObject("$and", new BasicDBObject[]{
                        new BasicDBObject("$eq", new String[]{"$traderId", "$$trade_id"}),
                        new BasicDBObject("$eq", new String[]{"$matchId", "$$match_id"})
                })
                ));

        List<Bson> pipelines = new ArrayList<>();
        pipelines.add(pipeline);

        // matchTop表
        LookupPipelineOperation lookup = new LookupPipelineOperation(MONGOMATCHTOP, lets, pipelines, "matchTop");
        //LookupOperation lookup = Aggregation.lookup(MONGOMATCHTOP, "matchId", "matchId", "matchTop");
        commonOperations.add(lookup);

//        UnwindOperation unwind = Aggregation.unwind("matchTop", "arrayIndex", false);
        //commonOperations.add(unwind);

//        ConditionalOperators.Cond topTimeCond = ConditionalOperators.when(Criteria.where("matchTop.topTime").is(0))
//                .thenValueOf("$topTime").otherwiseValueOf("$matchTop.topTime");

        ConditionalOperators.Cond tournamentLevelCond = ConditionalOperators.when(Criteria.where("tournamentLevel").ne(0))
                .thenValueOf("$tournamentLevel").otherwise(99);

        Class<MatchMarketLiveBean> matchMarketLiveBeanClass = MatchMarketLiveBean.class;
        Field[] declaredFields = matchMarketLiveBeanClass.getDeclaredFields();
        List<String> strings = new ArrayList<>();
        for (Field field : declaredFields) {
            strings.add(field.getName());
        }
        ProjectionOperation projectionOperation = Aggregation.project(strings.toArray(new String[0]))
                .and("matchTop.traderId").as("topTraderId")
                .and("matchTop.topTime").as("topTime")
                .and("tournamentLevel").applyCondition(tournamentLevelCond);
        commonOperations.add(projectionOperation);

        SortOperation sort = null;
        if (queryVo.getSortType() == 1) {
            sort = Aggregation.sort(Sort.Direction.DESC, "topTime")
                    .and(Sort.Direction.ASC, "matchStatus")
                    .and(Sort.Direction.ASC, "tournamentLevel")
                    .and(Sort.Direction.ASC, "matchStartTime")
                    .and(Sort.Direction.ASC, "nameConcat")
                    .and(Sort.Direction.ASC, "matchId");

        } else if (queryVo.getSortType() == 2) {
            if (SportIdEnum.isPingpong(sportId)) {
                sort = Aggregation.sort(Sort.Direction.DESC, "topTime")
                        .and(Sort.Direction.ASC, "sort")
                        .and(Sort.Direction.DESC, "matchStatus")
                        .and(Sort.Direction.ASC, "matchStartTime")
                        .and(Sort.Direction.ASC, "tournamentLevel")
                        .and(Sort.Direction.ASC, "nameConcat")
                        .and(Sort.Direction.ASC, "matchId");
            } else {
                sort = Aggregation.sort(Sort.Direction.DESC, "topTime")
                        .and(Sort.Direction.ASC, "matchStatus")
                        .and(Sort.Direction.ASC, "matchStartTime")
                        .and(Sort.Direction.ASC, "tournamentLevel")
                        .and(Sort.Direction.ASC, "nameConcat")
                        .and(Sort.Direction.ASC, "matchId");
            }

        }

        commonOperations.add(sort);
        PageResult<MatchMarketLiveBean> pageResult = mongoPageHelper.pageAggregationQuery(commonOperations, "match_market_live", MatchMarketLiveBean.class, queryVo.getPageSize(), queryVo.getCurrentPage());

        /*List<MatchMarketLiveBean> matchInfos = pageResult.getList();
        if (matchInfos.size() > 0) {
            pageResult.setList(tansferMatchInfo(matchInfos, queryVo));
        }*/
        return pageResult;
    }


    @Override
    public Query buildMongoQuery(MarketLiveOddsQueryVo queryVo) {
        Query query = new Query();
        query.addCriteria(buildMongoCriteria(queryVo));
        //赛事列表排序
        List<Sort.Order> orderList = new ArrayList<Sort.Order>();
        Sort.Order matchSnapshot = new Sort.Order(Sort.Direction.ASC, "matchStatus");
        orderList.add(matchSnapshot);
        Sort.Order matchStartTime = new Sort.Order(Sort.Direction.ASC, "matchStartTime");
        orderList.add(matchStartTime);
        Sort.Order tournamentLevel = new Sort.Order(Sort.Direction.ASC, "tournamentLevel");
        orderList.add(tournamentLevel);
        Sort.Order nameConcat = new Sort.Order(Sort.Direction.ASC, "nameConcat");
        orderList.add(nameConcat);
        query.with(new Sort(orderList));
        return query;
    }

    /**
     * 组装mongo查询条件
     */
    @Override
    public Criteria buildMongoCriteria(MarketLiveOddsQueryVo queryVo) {

        Criteria criteria = new Criteria();
        Long beginTime = System.currentTimeMillis();
        Long sportId = queryVo.getSportId();
        if (sportId != null && sportId > 0) {
            criteria.and("sportId").is(sportId);
        }
        if (queryVo.getTournamentLevel() != null) {
            criteria.and("tournamentLevel").is(queryVo.getTournamentLevel());
        }
        if (queryVo.getStandardTournamentId() != null) {
            criteria.and("standardTournamentId").is(queryVo.getStandardTournamentId());
        }
        if (StringUtils.isNotBlank(queryVo.getTournamentName())) {
            criteria.and("tournamentNames.text").regex(queryVo.getTournamentName());
        }
        if (queryVo.getCategorySetId() != null && SportIdEnum.noMain(queryVo.getSportId(), queryVo.getCategorySetId())) {
            criteria.and("setInfos.catgorySetId").is(queryVo.getCategorySetId());
        }
        if (queryVo.getTournamentIds() != null && queryVo.getTournamentIds().size() > 0) {
            if (!queryVo.getTournamentIds().contains(-1L)) {
                criteria.and("standardTournamentId").in(queryVo.getTournamentIds());
            }
        }
        if (StringUtils.isNotBlank(queryVo.getMatchManageId())) {
            criteria.and("matchManageId").in(Arrays.asList(queryVo.getMatchManageId().split(",")));
        }
        if (queryVo.getTradeType() != null) {
            if (TradeTypeEnum.AUTO.getCode().equals(queryVo.getTradeType())) {
                criteria.and("autoCount").gt(0);
            } else if (TradeTypeEnum.MANUAD.getCode().equals(queryVo.getTradeType())) {
                criteria.and("manualCount").gt(0);
            }
        }
        if (queryVo.getLiveOddBusiness() == null) {
            criteria.and("matchStatus").in(0, 4, 5, 6, 7, 8, 9);
            // 1 早盘开售
            criteria.and("preMatchBusiness").is(1);
            if (queryVo.getOperateMatchStatus() != null) {
                criteria.and("operateMatchStatus").is(queryVo.getOperateMatchStatus());
            }
            criteria.and("matchStartTime").gt(DateUtils.transferLongToDateStrings(beginTime));

        } else if (queryVo.getLiveOddBusiness() == 1) {
            Long overTime = beginTime;
            Long beforeLimitTime = THIRTY_MINS;
            if (SportIdEnum.isFootball(sportId) || SportIdEnum.isBasketball(sportId)) {
                overTime = beginTime - FOUR_HOURS;
                if (SportIdEnum.isFootball(sportId)) {
                    beforeLimitTime = FIFTEEN_MINS;
                } else {
                    beforeLimitTime = THIRTY_MINS;
                }
            } else if (SportIdEnum.isTennis(sportId)) {
                overTime = beginTime - ONE_DAY;
            } else if (SportIdEnum.isBaseBall(sportId)) {
                overTime = beginTime - TWO_DAY;
            } else if (SportIdEnum.isSnooker(sportId)) {
                overTime = beginTime - SEVEN_DAY;
            } else if (SportIdEnum.isPingpong(sportId) || SportIdEnum.isVolleyball(sportId) || SportIdEnum.BADMINTON.isYes(sportId)) {
                overTime = beginTime - FOUR_HOURS;
            } else if (SportIdEnum.isIceHockey(sportId)) {
                overTime = beginTime - FOUR_HOURS;
            }
            Criteria criteria1 = Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(overTime));
            Criteria liveMatchs = Criteria.where("matchStatus").in(1, 2, 10).and("liveOddBusiness").is(1);
            Criteria eventCodeMatchs = Criteria.where("matchStatus").is(6).and("eventCode").ne("").ne(null).and("liveOddBusiness").is(1);
            Criteria preLiveMatchs = Criteria.where("matchStatus").is(0).and("liveOddBusiness").is(1).andOperator(Criteria.where("matchStartTime").lte(DateUtils.transferLongToDateStrings(beginTime)),
                    Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime - THIRTY_MINS)));
            //足球结束赛事1310
            if (SportIdEnum.isFootball(sportId)) {
                Criteria endLiveMatchs = Criteria.where("matchStatus").is(3).and("endTime").gte(beginTime - FIVE_MINS);
                criteria1.orOperator(liveMatchs, eventCodeMatchs, preLiveMatchs, endLiveMatchs);
            } else {
                criteria1.orOperator(liveMatchs, eventCodeMatchs, preLiveMatchs);
            }

            Criteria criteria2 = Criteria.where("liveOddBusiness").is(1);
            Criteria timeBegin = Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime));
            Criteria timeBefore = Criteria.where("matchStartTime").lt(DateUtils.transferLongToDateStrings(beginTime + beforeLimitTime));
            criteria2.andOperator(timeBegin, timeBefore);

            criteria.orOperator(criteria1, criteria2);
        }

        if (!"1".equals(String.valueOf(queryVo.getLiveOddBusiness())) && null != queryVo.getMatchDate()) {
            if (queryVo.getIsOtherEarly() != null && queryVo.getIsOtherEarly() == 1) {//其他招盘
                criteria.and("matchStartDate").gte(queryVo.getMatchDate());
            } else {
                criteria.and("matchStartDate").is(queryVo.getMatchDate());
            }
        }

        //如果是收藏  1 所有自选 2 仅自己操盘 3 仅自己收藏 4 所有赛事
        Criteria ca = new Criteria();
        Integer chooseType = queryVo.getChooseType();
        String tradeId = String.valueOf(queryVo.getTradeId());
        if (chooseType != null) {
            RcsMatchCollection co = new RcsMatchCollection();
            co.setUserId(queryVo.getTradeId());
            co.setSportId(sportId);
            co.setMatchType(queryVo.getLiveOddBusiness() == null ? 1 : 2);
            co.setBeginTime(System.currentTimeMillis() - FOUR_HOURS);

            List<Long> tournamentIds = new ArrayList<>();
            List<Long> matchIds = new ArrayList<>();
            List<Long> noMatchIds = new ArrayList<>();
            List<Long> traderMatchIds = new ArrayList<>();
            if (!chooseType.equals(4)) {
                //收藏联赛ID
                tournamentIds = collectionService.querytourColl(co);

                //查询赛事收藏
                List<RcsMatchCollection> matchColl = rcsMatchCollectionMapper.queryMatchColls(co);
                if (!CollectionUtils.isEmpty(matchColl)) {
                    matchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() == 1).map(map -> map.getMatchId()).collect(Collectors.toList());
                    noMatchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() != 1).map(map -> map.getMatchId()).collect(Collectors.toList());
                }
                //查询指派赛事
                traderMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(queryVo.getTradeId()), queryVo.getLiveOddBusiness() == null ? 0 : 1);
                String trader = "preTraderId";
                if (queryVo.getLiveOddBusiness() != null && queryVo.getLiveOddBusiness() == 1) {
                    trader = "liveTraderId";
                }
                if (chooseType.equals(1)) {
                    if (!CollectionUtils.isEmpty(matchIds)) traderMatchIds.addAll(matchIds);
                    ca.orOperator(Criteria.where(trader).is(tradeId),
                            Criteria.where("standardTournamentId").in(tournamentIds).and("matchId").nin(noMatchIds),
                            Criteria.where("matchId").in(traderMatchIds));
                } else if (chooseType.equals(2)) {
                    ca.orOperator(Criteria.where(trader).is(tradeId), Criteria.where("matchId").in(traderMatchIds));

                } else if (chooseType.equals(3)) {
                    ca.orOperator(Criteria.where("standardTournamentId").in(tournamentIds).and("matchId").nin(noMatchIds),
                            Criteria.where("matchId").in(matchIds));

                }
            }
        }
        return new Criteria().andOperator(criteria, ca);
    }

    @Override
    public PageResult<MatchMarketLiveBean> queryMatchList(MarketLiveOddsQueryVo queryVo, Criteria criteria) {
        String traderId = queryVo.getTradeId().toString();

        List<AggregationOperation> commonOperations = new ArrayList<>();
        MatchOperation match = Aggregation.match(criteria);
        commonOperations.add(match);

        List<Variable<String>> lets = new ArrayList<>();
        Variable<String> let1 = new Variable<>("trade_id", traderId);
        Variable<String> let2 = new Variable<>("match_id", "$matchId");
        lets.add(let1);
        lets.add(let2);

        Bson pipeline = new BasicDBObject("$match",
                new BasicDBObject("$expr", new BasicDBObject("$and", new BasicDBObject[]{
                        new BasicDBObject("$eq", new String[]{"$traderId", "$$trade_id"}),
                        new BasicDBObject("$eq", new String[]{"$matchId", "$$match_id"})
                })
                ));

        List<Bson> pipelines = new ArrayList<>();
        pipelines.add(pipeline);

        LookupPipelineOperation lookup = new LookupPipelineOperation(MONGOMATCHTOP, lets, pipelines, "matchTop");
        //LookupOperation lookup = Aggregation.lookup(MONGOMATCHTOP, "matchId", "matchId", "matchTop");
        commonOperations.add(lookup);

        UnwindOperation unwind = Aggregation.unwind("matchTop", "arrayIndex", false);
        //commonOperations.add(unwind);

        ConditionalOperators.Cond topTimeCond = ConditionalOperators.when(Criteria.where("matchTop.topTime").is(0))
                .thenValueOf("$topTime").otherwiseValueOf("$matchTop.topTime");

        ConditionalOperators.Cond tournamentLevelCond = ConditionalOperators.when(Criteria.where("tournamentLevel").ne(0))
                .thenValueOf("$tournamentLevel").otherwise(99);

        Class<MatchMarketLiveBean> matchMarketLiveBeanClass = MatchMarketLiveBean.class;
        Field[] declaredFields = matchMarketLiveBeanClass.getDeclaredFields();
        List<String> strings = new ArrayList<>();
        for (Field field : declaredFields) {
            strings.add(field.getName());
        }
        ProjectionOperation projectionOperation = Aggregation.project(strings.toArray(new String[0]))
                .and("matchTop.traderId").as("topTraderId")
                .and("matchTop.topTime").as("topTime")
                .and("tournamentLevel").applyCondition(tournamentLevelCond);
        commonOperations.add(projectionOperation);

        SortOperation sort = null;
        if (queryVo.getSortType() == 1) {
            sort = Aggregation.sort(Sort.Direction.DESC, "topTime")
                    .and(Sort.Direction.ASC, "matchStatus")
                    .and(Sort.Direction.ASC, "tournamentLevel")
                    .and(Sort.Direction.ASC, "nameConcat")
                    .and(Sort.Direction.ASC, "matchStartTime");

        } else if (queryVo.getSortType() == 2) {
            sort = Aggregation.sort(Sort.Direction.DESC, "topTime")
                    .and(Sort.Direction.ASC, "matchStatus")
                    .and(Sort.Direction.ASC, "matchStartTime")
                    .and(Sort.Direction.ASC, "tournamentLevel")
                    .and(Sort.Direction.ASC, "nameConcat");
        }

        commonOperations.add(sort);
        PageResult<MatchMarketLiveBean> pageResult = mongoPageHelper.pageAggregationQuery(commonOperations, "match_market_live", MatchMarketLiveBean.class, Integer.MAX_VALUE, 1);

        /*List<MatchMarketLiveBean> matchInfos = pageResult.getList();
        if (matchInfos.size() > 0) {
            pageResult.setList(tansferMatchInfo(matchInfos, queryVo));
        }*/
        return pageResult;
    }


    @Override
    public Criteria buildMongoCriteria(MarketLiveOddsQueryVo queryVo, Integer matchType) {
        Criteria criteria = new Criteria();
        Long beginTime = System.currentTimeMillis();
        Long sportId = queryVo.getSportId();
        if (sportId != null && sportId > 0) {
            criteria.and("sportId").is(sportId);
        }
        if (queryVo.getTournamentLevel() != null) {
            criteria.and("tournamentLevel").is(queryVo.getTournamentLevel());
        }
        if (queryVo.getStandardTournamentId() != null) {
            criteria.and("standardTournamentId").is(queryVo.getStandardTournamentId());
        }
        if (StringUtils.isNotBlank(queryVo.getTournamentName())) {
            criteria.and("tournamentNames.text").regex(queryVo.getTournamentName());
        }
        if (!Arrays.asList(10001L, 20001L, 50001L).contains(queryVo.getCategorySetId()) && queryVo.getCategorySetId() != null) {
            criteria.and("setInfos.catgorySetId").is(queryVo.getCategorySetId());
        }
        if (queryVo.getTournamentIds() != null && queryVo.getTournamentIds().size() > 0) {
            if (!queryVo.getTournamentIds().contains(-1L)) {
                criteria.and("standardTournamentId").in(queryVo.getTournamentIds());
            }
        }
        if (StringUtils.isNotBlank(queryVo.getMatchManageId())) {
            criteria.and("matchManageId").in(Arrays.asList(queryVo.getMatchManageId().split(",")));
        }
        if (queryVo.getTradeType() != null) {
            if (TradeTypeEnum.AUTO.getCode().equals(queryVo.getTradeType())) {
                criteria.and("autoCount").gt(0);
            } else if (TradeTypeEnum.MANUAD.getCode().equals(queryVo.getTradeType())) {
                criteria.and("manualCount").gt(0);
            }
        }

        if (matchType == 2) {
            criteria.and("matchStatus").in(0, 4, 5, 6, 7, 8, 9);
            // 1 早盘开售
            criteria.and("preMatchBusiness").is(1);
            if (queryVo.getOperateMatchStatus() != null) {
                criteria.and("operateMatchStatus").is(queryVo.getOperateMatchStatus());
            }
            criteria.and("matchStartTime").gt(DateUtils.transferLongToDateStrings(beginTime));
        } else if (matchType == 1) {
            Long overTime = beginTime;
            Long beforeLimitTime = THIRTY_MINS;
            if (SportIdEnum.isFootball(sportId) || SportIdEnum.isBasketball(sportId)) {
                overTime = beginTime - FOUR_HOURS;
                if (SportIdEnum.isFootball(sportId)) {
                    beforeLimitTime = RcsConstant.FIFTEEN_MINS;
                } else {
                    beforeLimitTime = THIRTY_MINS;
                }
            } else if (SportIdEnum.isTennis(sportId) || SportIdEnum.isSnooker(sportId)) {
                overTime = beginTime - ONE_DAY;
                beforeLimitTime = THIRTY_MINS;
            }
            Criteria criteria1 = Criteria.where("liveOddBusiness").is(1)
                    .and("matchStartTime").gte(DateUtils.transferLongToDateStrings(overTime));
            Criteria liveMatchs = Criteria.where("matchStatus").in(1, 2, 10);
            Criteria eventCodeMatchs = Criteria.where("matchStatus").is(6).and("eventCode").ne("").ne(null);
            Criteria preLiveMatchs = Criteria.where("matchStatus").is(0).andOperator(Criteria.where("matchStartTime").lte(DateUtils.transferLongToDateStrings(beginTime)),
                    Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime - THIRTY_MINS)));
            criteria1.orOperator(liveMatchs, eventCodeMatchs, preLiveMatchs);

            Criteria criteria2 = Criteria.where("liveOddBusiness").is(1);
            Criteria timeBegin = Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime));
            Criteria timeBefore = Criteria.where("matchStartTime").lt(DateUtils.transferLongToDateStrings(beginTime + beforeLimitTime));
            criteria2.andOperator(timeBegin, timeBefore);

            criteria.orOperator(criteria1, criteria2);
        }

        if (!"1".equals(String.valueOf(queryVo.getLiveOddBusiness())) && null != queryVo.getMatchDate()) {
            if (queryVo.getIsOtherEarly() != null && queryVo.getIsOtherEarly() == 1) {//其他招盘
                criteria.and("matchStartDate").gte(queryVo.getMatchDate());
            } else {
                criteria.and("matchStartDate").is(queryVo.getMatchDate());
            }
        }

        //如果是收藏  1 所有自选 2 仅自己操盘 3 仅自己收藏 4 所有赛事
        Criteria ca = new Criteria();
        Integer chooseType = queryVo.getChooseType();
        String tradeId = String.valueOf(queryVo.getTradeId());
        if (chooseType != null) {
            RcsMatchCollection co = new RcsMatchCollection();
            co.setUserId(queryVo.getTradeId());
            co.setSportId(sportId);
            //matchType为空不设置matchType查询条件
            if (matchType != null) {
                co.setMatchType(matchType != 0 ? 1 : 2);
            }

            co.setBeginTime(System.currentTimeMillis() - FOUR_HOURS);

            List<Long> tournamentIds = new ArrayList<>();
            List<Long> matchIds = new ArrayList<>();
            List<Long> noMatchIds = new ArrayList<>();
            List<Long> traderMatchIds = new ArrayList<>();
            if (!chooseType.equals(4)) {
                //收藏联赛ID
                tournamentIds = collectionService.querytourColl(co);

                //查询赛事收藏
                List<RcsMatchCollection> matchColl = rcsMatchCollectionMapper.queryMatchColls(co);
                if (!CollectionUtils.isEmpty(matchColl)) {
                    matchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() == 1).map(map -> map.getMatchId()).collect(Collectors.toList());
                    noMatchIds = matchColl.stream().filter(filter -> null != filter.getMatchId() && filter.getStatus() != 1).map(map -> map.getMatchId()).collect(Collectors.toList());
                }
                //查询指派赛事
                traderMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(queryVo.getTradeId()), queryVo.getLiveOddBusiness() == null ? 0 : 1);

                if (chooseType.equals(1)) {
                    if (!CollectionUtils.isEmpty(matchIds)) traderMatchIds.addAll(matchIds);

                    ca.orOperator(Criteria.where("preTraderId").is(tradeId),
                            Criteria.where("liveTraderId").is(tradeId),
                            Criteria.where("standardTournamentId").in(tournamentIds).and("matchId").nin(noMatchIds),
                            Criteria.where("matchId").in(traderMatchIds));
                } else if (chooseType.equals(2)) {
                    if (queryVo.getLiveOddBusiness() != null && queryVo.getLiveOddBusiness() == 1) {
                        ca.orOperator(Criteria.where("liveTraderId").is(tradeId), Criteria.where("matchId").in(traderMatchIds));
                    } else {
                        ca.orOperator(Criteria.where("preTraderId").is(tradeId), Criteria.where("matchId").in(traderMatchIds));
                    }
                } else if (chooseType.equals(3)) {
                    ca.orOperator(Criteria.where("standardTournamentId").in(tournamentIds).and("matchId").nin(noMatchIds),
                            Criteria.where("matchId").in(matchIds));

                }
            }
        }
        return new Criteria().andOperator(criteria, ca);
    }

    @Override
    public Criteria timelyCriteria(MarketLiveOddsQueryVo queryVo) {
        Criteria criteria = new Criteria();
        Long beginTime = System.currentTimeMillis();
        if (!CollectionUtils.isEmpty(queryVo.getSportIds())) {
            criteria.and("sportId").in(queryVo.getSportIds());
        }
        if (StringUtils.isNotBlank(queryVo.getTournamentName())) {
            criteria.and("tournamentNames.text").regex(queryVo.getTournamentName());
        }
        if (queryVo.getLiveOddBusiness() == null) {
            criteria.orOperator(Criteria.where("liveOddBusiness").is(1), Criteria.where("preMatchBusiness").is(1));
        } else if (queryVo.getLiveOddBusiness() == 0) {
            criteria.and("matchStatus").in(0, 4, 5, 6, 7, 8, 9);
            // 1 早盘开售
            criteria.and("preMatchBusiness").is(1);
            criteria.and("matchStartTime").gt(DateUtils.transferLongToDateStrings(beginTime));
        } else if (queryVo.getLiveOddBusiness() == 1) {

            criteria.and("liveOddBusiness").is(1)
                    .and("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime - 1000 * 60 * 60 * 4L));
            Criteria liveMatchs = Criteria.where("matchStatus").in(1, 2, 10);
            Criteria eventCodeMatchs = Criteria.where("matchStatus").is(6).and("eventCode").ne("").ne(null);
            Criteria preLiveMatchs = Criteria.where("matchStatus").is(0).andOperator(Criteria.where("matchStartTime").lte(DateUtils.transferLongToDateStrings(beginTime)),
                    Criteria.where("matchStartTime").gte(DateUtils.transferLongToDateStrings(beginTime - 1000 * 60 * 30)));
            criteria.orOperator(liveMatchs, eventCodeMatchs, preLiveMatchs);

        }
        return criteria;
    }

    private Long getMatchTeamId(Long matchId, String homeAway, List<MatchTeamLinkVo> matchTeamLinkLists) {
        if (matchTeamLinkLists == null) {
            return null;
        }

        String matchHomeAwayKey = MATCH_TEAM_KEY.concat(Long.toString(matchId)).concat(":").concat(homeAway);
        String currTeamId = redisClient.get(matchHomeAwayKey);
        if (StringUtils.isNotEmpty(currTeamId)) {
            return Long.parseLong(currTeamId);
        }

        for (MatchTeamLinkVo m : matchTeamLinkLists) {
            if (Long.toString(matchId).concat(homeAway).equals(Long.toString(m.getMatchId()).concat(m.getHomeAway()))) {
                redisClient.set(matchHomeAwayKey, m.getTeamId());
                redisClient.expireKey(matchHomeAwayKey, 3 * 60 * 60);
                return m.getTeamId();
            }
        }
        return null;
    }

    @Override
    public List<MatchMarketLiveBean> tansferMatchInfo(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo) {
        // 显示的盘口类型，默认显示欧盘
        // 体育类型ID
        Long sportId = queryVo.getSportId();
        //玩法集ID
        Long categorySetId = queryVo.getCategorySetId();
        // 赔率类型:欧洲盘：OU，香港盘：HK
        String marketOddsKind = queryVo.getMarketOddsKind();
        // 是否支持滚球:1=支持；0=不支持(变更为是否开滚球，对应siness字段)
        Integer oddBusiness = queryVo.getLiveOddBusiness();
        // 如果为空，默认欧洲盘
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(StringUtils.isNotBlank(marketOddsKind) ? marketOddsKind : "EU");

        // 标准赛事基础信息
        List<MatchMarketLiveBean> matchs = queryMatchs(matchInfos, queryVo);
        List<Long> matchIds = matchs.stream().map(map -> map.getMatchId()).collect(Collectors.toList());
        //根据用户及赛事id查询  rcs_match_user_memo_ref 表，查询是否存在未读备忘录的赛事,用于
        Map<Long, List<String>> needRemindMatchMemos = sellService.getNeedRemindMatchMemos(matchIds, queryVo.getTradeId());
        //所有的赛种都用坑位 类型  1投注项 2坑位
        Integer dataType = 1;
        // 投注项，货量
        if (SportIdEnum.isFootball(sportId) || SportIdEnum.isBasketball(sportId) || SportIdEnum.otherSports().contains(sportId)) {
            dataType = 2;
        }
        Map<Long, Map<String, List<RcsPredictBetOdds>>> betMap = predictBetOddsService.queryBetOdds(matchIds, dataType, queryVo.getSeriesType());
        //查询数据源
        Map<String, String> dataSourceMap = templatePlayMargainService.queryDataSource(matchIds);
        // 查询赛种主要玩法 rcs_code表
        List<CategoryConVo> categorySetVos = categoryService.mainCategory(sportId);
        Map<String, Integer> relevanceTypeMap = playConfigService.queryRelevanceType(matchIds);
        // 查询所有赛事玩法
        Map<String, List<MarketCategory>> categoryMap = getCategoryColls(matchInfos, sportId);
        Map<String, Integer> clientShowMap = null;
        if (SportTypeEnum.FOOTBALL.getCode().equals(sportId.intValue())) {
            //查询客户端显示配置
            clientShowMap = tradeConfigService.queryCategoryShow(matchIds, oddBusiness == null ? 0 : oddBusiness);
        }

        List<MatchTeamLinkVo> matchTeamLinkLists = standardSportTeamMapper.queryTeamByMatchIds(matchIds);

        List<MatchMarketLiveBean> result = new ArrayList<>();
        log.info("::{}::============matchLiveInfos:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(matchs));
        for (MatchMarketLiveBean match : matchs) {
            try {
                Long matchId = match.getMatchId();
                List<MarketConfigMongo> marketConfigMongos = new ArrayList<>();
                Map<Long, List<MatchSetVo>> categorySetMap = new HashMap<>();
                if (1 == match.getMatchSnapshot()) {
                    //查询前十五分钟玩法级别设置
                    categorySetMap = matchSetMongoService.queryCategoryLevelSnap(matchId);
                    //查询前十五分钟赔率设置
                    marketConfigMongos = matchSetMongoService.queryMarketConfig(matchId);
                }

                match.getTeamList().forEach(m -> {
                    m.setId(getMatchTeamId(match.getMatchId(), m.getMatchPosition(), matchTeamLinkLists));
                });

                if (!CollectionUtils.isEmpty(betMap)) {
                    match.setBetMap(betMap.get(matchId));
                }
                List<CategoryConVo> categoryConVos = categorySetVos;
                Integer roundType = match.getRoundType();
                if (SportIdEnum.isTennis(sportId)) {
                    if (!CollectionUtils.isEmpty(categoryConVos) && roundType != null && roundType > 0 && roundType < 5) {
                        categoryConVos = categoryConVos.subList(0, roundType + 1);
                    }
                }
                if (SportIdEnum.isSnooker(sportId)) {
                    if (!CollectionUtils.isEmpty(categoryConVos) && roundType != null && roundType > 0 && roundType < 35) {
                        categoryConVos = categoryConVos.subList(0, roundType + 1);
                    }
                }
                if (SportIdEnum.isBasketball(sportId)) {
                    String key = getChuZhangWarnSignKey(matchId, NumberUtils.INTEGER_ONE.equals(queryVo.getLiveOddBusiness()) ? 0 : 1);
                    Map<String, String> chuZhangWarnSignMap = (Map<String, String>) redisClient.hGetAllToObj(key);
                    match.setChuZhangWarnSignMap(chuZhangWarnSignMap);

                    // 2195 篮球AO赛制展示盘口
                    QueryWrapper<RcsTournamentTemplate> queryWrapper = new QueryWrapper<>();
                    queryWrapper.lambda().eq(RcsTournamentTemplate::getType, 3L);
                    queryWrapper.lambda().eq(RcsTournamentTemplate::getMatchType, isLive(match.getMatchStatus()) ? 0:1);
                    queryWrapper.lambda().eq(RcsTournamentTemplate::getTypeVal, Long.valueOf(matchId));
                    RcsTournamentTemplate tournamentTemplate = templateMapper.selectOne(queryWrapper);
                    if(null != tournamentTemplate && StringUtils.isNotBlank(tournamentTemplate.getAoConfigValue())){
                        AoBasketBallTemplateConfigEntity aoBasketBallTemplateConfigEntity = JSON.parseObject(tournamentTemplate.getAoConfigValue(), AoBasketBallTemplateConfigEntity.class);
                        if(null != aoBasketBallTemplateConfigEntity.getQuarters() && aoBasketBallTemplateConfigEntity.getQuarters() == 2){
                            match.getSetInfos().removeIf(e -> e.getCatgorySetId() != 20001 && e.getCatgorySetId() != 20002 && e.getCatgorySetId() != 20007);
                        }
                    }
                }
                List<MatchMarketLiveBean> sortMatchs = new ArrayList<>();
                //主要玩法集
                for (CategoryConVo conVo : categoryConVos) {
                    List<Long> categoryIds = conVo.categoryIds();
                    List<MarketCategory> categories = null;
                    if (!CollectionUtils.isEmpty(categoryMap)) {
                        categories = categoryMap.get(String.valueOf(matchId));
                    }
                    if (!CollectionUtils.isEmpty(dataSourceMap)) {
                        Map<Long, String> categoryDataSourceMap = dataSourceMap.entrySet().stream().filter(map -> map.getKey().split("_")[0].equals(String.valueOf(matchId))
                                && categoryIds.contains(Long.parseLong(map.getKey().split("_")[1]))
                        ).collect(Collectors.toMap(p -> Long.parseLong(p.getKey().split("_")[1]), p -> p.getValue()));
                        match.setDataSourceMap(categoryDataSourceMap);
                    }
                    if (SportTypeEnum.FOOTBALL.getCode().equals(sportId.intValue()) && !CollectionUtils.isEmpty(clientShowMap)) {
                        Integer clientShow = clientShowMap.get(matchId + "_" + CategoryShowEnum.querySendId(conVo.getId()));
                        match.setClientShow(clientShow == null ? 1 : clientShow);
                    }
                    MatchMarketLiveBean matchBean = BeanCopyAllDeepUtils.copyProperties(match, MatchMarketLiveBean.class);
                    
                    List<MatchCatgorySetVo> setInfos = matchBean.getSetInfos();
                    if (setInfos == null) {
                        continue;
                    }
                    MatchCatgorySetVo setConVo = setInfos.stream().filter(setVo -> conVo.getId().equals(setVo.getCatgorySetId())).findFirst().orElse(null);
                    if (null == setConVo) {
                        continue;
                    }
                    if (CollectionUtils.isEmpty(match.getSetInfos())) {
                        continue;
                    }
                    matchBean.setCategorySetId(conVo.getId());
                    matchBean.setCategoryIds(conVo.categoryIds());
                    Integer sortId = 0;
                    if (SportIdEnum.isFootball(sportId)) {
                        sortId = PeriodEnum.getSort(matchBean.getCategorySetId()) + 1000;
                        matchBean.setPlaySetCode(PeriodEnum.getPlaySetCodeByPlaySetId(conVo.getId()));
                        matchBean.setPlaySetCodeStatus(getPlaySetCodeStatus(matchBean.getMatchId(), matchBean.getPlaySetCode()));
                    } else {
                        sortId = conVo.getId().intValue();
                    }

                    matchBean.setSortId(sortId.longValue());
                    //玩法集页面显示
                    if (queryVo.getCategorySetId().equals(conVo.getId()))
                        matchBean.setSortId(sortId.longValue() - 500L);
                    if (categorySetId.equals(conVo.getId())) {
                        matchBean.setCategorySetShow(true);
                        matchBean.setForecastSetShow(true);
                    } else {
                        matchBean.setCategorySetShow(setConVo.isCategorySetShow());
                    }
                    if (!CollectionUtils.isEmpty(setInfos)) {
                        setInfos.stream().forEach(setVo -> {
                            if (setVo.getCatgorySetId().equals(categorySetId)) {
                                setVo.setCategorySetShow(true);
                                setVo.setForecastSetShow(true);
                            }
                        });
                    }

                    //货量
                    if (!CollectionUtils.isEmpty(matchBean.getBetMap())) {
                        matchBean.setBetMap(Maps.filterKeys(matchBean.getBetMap(), m -> categoryIds.contains(Long.parseLong(m.split("_")[0]))));
                    }

                    List<MarketCategory> categoryList = null;
                    if (!CollectionUtils.isEmpty(categories)) {
                        categoryList = categories.stream().filter(filter -> categoryIds.contains(filter.getId())).collect(Collectors.toList());
                        //取玩法下面盘口数最大值
                        if (!CollectionUtils.isEmpty(categoryList)) {
                            int asInt = 1;
                            if (SportIdEnum.isTennis(sportId)) {
                                List<MarketCategory> categories1 = categoryList.stream().filter(fi -> !fi.getId().equals(168L)).collect(Collectors.toList());
                                if (!CollectionUtils.isEmpty(categories1)) {
                                    asInt = categories1.stream().mapToInt(MarketCategory::getMarketCount).max().getAsInt();
                                }
                            } else {
                                asInt = categoryList.stream().mapToInt(MarketCategory::getMarketCount).max().getAsInt();
                            }
                            matchBean.setMarketCount(asInt);
                        }
                    }

                    List<Long> setIds = match.getSetInfos().stream().map(map -> map.getCatgorySetId()).collect(Collectors.toList());

                    if (!CollectionUtils.isEmpty(categoryList) && setIds.contains(conVo.getId())) {
                        List<MarketCategory> tradeCategories = new ArrayList<>();
                        for (MarketCategory category : categoryList) {
                            MarketCategory marketCategory = BeanCopyAllDeepUtils.copyProperties(category, MarketCategory.class);
                            Long categoryId = marketCategory.getId();
                            //前端需要字段
                            marketCategory.setRollType(conVo.categoryIds().indexOf(categoryId) + 1);

                            Integer marketCount = marketCategory.getMarketCount();
                            marketCategory.setMarketCount(marketCount == null ? 3 : marketCount);
                            if (null == marketCategory.getTradeType()) marketCategory.setTradeType(0);

                            Map<Integer, Integer> snapshotStatusMap = null;
                            if (1 == match.getMatchSnapshot()) {
                                matchSetMongoService.categoryLevelSnap(marketCategory, categorySetMap);
                                snapshotStatusMap = marketStatusService.getSnapshotMarketPlaceStatus(matchId, categoryId);
                            }
                            List<PredictForecastVo> forecast = marketCategory.getForecast();

                            if (!CollectionUtils.isEmpty(forecast) && FOOTBALL_FORCAST_PLAYS.contains(categoryId)) {
                                forecast = forecast.stream().sorted(Comparator.comparing(PredictForecastVo::getScore)).collect(Collectors.toList());
                                marketCategory.setForecast(forecast);
                            }
                            List<MatchMarketVo> matchMarketVoList = marketCategory.getMatchMarketVoList();

                            if (!CollectionUtils.isEmpty(matchMarketVoList)) {
                                //判断次玩法
                                List<MatchMarketVo> childMarketVoList = matchMarketVoList.stream().filter(fi -> StringUtils.isNotBlank(fi.getChildMarketCategoryId())
                                        && (!String.valueOf(fi.getMarketCategoryId()).equals(fi.getChildMarketCategoryId()))).collect(Collectors.toList());
                                marketCategory.setIsChildCategory(!CollectionUtils.isEmpty(childMarketVoList));
                                Iterator<MatchMarketVo> iterator = matchMarketVoList.iterator();
                                while (iterator.hasNext()) {
                                	MatchMarketVo matchMarket = iterator.next();
                                    if (SportIdEnum.otherSports().contains(sportId)) {
                                        if (TENNIS_X_PLAYS.contains(categoryId) || (SportIdEnum.isBaseBall(sportId) && PlayTemplateUtils.BASEBALL_SECTION.contains(matchMarket.getMarketCategoryId()))) {
                                        	// 棒球区间玩法，针对30003，30004，30005玩法集筛选
                                        	if (SportIdEnum.isBaseBall(sportId) && PlayTemplateUtils.BASEBALL_SECTION.contains(matchMarket.getMarketCategoryId())) {
                                        		// 30003:1-3,30004:4-6,30005:7-9
                                        		if (conVo.getId().equals(30003L) && !"1".equals(matchMarket.getAddition2())) {
                                        			iterator.remove();
                                        			continue;
                                        		} else if(conVo.getId().equals(30004L) && !"4".equals(matchMarket.getAddition2())) {
                                        			iterator.remove();
                                        			continue;
                                        		} else if(conVo.getId().equals(30005L) && !"7".equals(matchMarket.getAddition2())) {
                                        			iterator.remove();
                                        			continue;
                                        		}
                                        	}
                                        	
                                            PlayTemplateUtils.handleMarketName(matchMarket, null);
                                        }
                                        if (!CollectionUtils.isEmpty(relevanceTypeMap)) {
                                            Integer relevanceType = relevanceTypeMap.get(matchId + "_" + categoryId + "_" + matchMarket.getChildMarketCategoryId());
                                            matchMarket.setRelevanceType(relevanceType == null ? 1 : relevanceType);
                                        }
                                    }
                                    //前十五分钟设置
                                    if (1 == match.getMatchSnapshot()) {
                                        Integer placeNum = matchMarket.getPlaceNum();
                                        if (!CollectionUtils.isEmpty(snapshotStatusMap) && snapshotStatusMap.containsKey(placeNum)) {
                                            matchMarket.setPlaceNumStatus(snapshotStatusMap.get(placeNum));
                                        }
                                        matchSetMongoService.updateMarketConfig(marketConfigMongos, matchId, categoryId, matchMarket);
                                    }

                                    if (null == matchMarket.getPlaceNum()) matchMarket.setPlaceNum(1000);

                                    if (SportIdEnum.otherSports().contains(sportId)) {
                                        matchMarket.setSetId(otherSetId(matchMarket, sportId));
                                        List<Long> xPlays = TENNIS_X_PLAYS;
                                        if (SportIdEnum.isTennis(sportId)) {
                                            xPlays = TENNIS_X_PLAYS;
                                        } else if (SportIdEnum.isPingpong(sportId) || SportIdEnum.BADMINTON.isYes(sportId)) {
                                            xPlays = PINGPONG_X_PLAYS;
                                        } else if (SportIdEnum.isVolleyball(sportId)) {
                                            xPlays = VOLLEYBALL_X_PLAYS;
                                        } else if (SportIdEnum.isSnooker(sportId)) {
                                            xPlays = SNOOKER_X_PLAYS;
                                        } else if (SportIdEnum.isBaseBall(sportId)) {
                                            xPlays = BASEBALL_X_PLAYS;
                                        } else if (SportIdEnum.isIceHockey(sportId)) {
                                            xPlays = ICE_HOCKEY_X_PLAYS;
                                        }
                                        if (xPlays.contains(categoryId) && !conVo.getId().equals(matchMarket.getSetId())) {
                                            continue;
                                        }
                                    }
                                    if (!CollectionUtils.isEmpty(matchMarket.getOddsFieldsList())) {
                                        matchMarket.getOddsFieldsList().stream().forEach(oddsFields -> {
                                            if (StringUtils.isNotBlank(oddsFields.getFieldOddsValue()) && !"null".equals(oddsFields.getFieldOddsValue())) {
                                                try {
                                                    oddsFields.setFieldOddsOriginValue(Double.valueOf(oddsFields.getFieldOddsValue()).intValue());
                                                    String displayOddsVal = OddsValueConvertUtils.convertAndDefaultDisply(marketKindEnum, oddsFields.getFieldOddsOriginValue());
                                                    oddsFields.setFieldOddsValue(displayOddsVal);
                                                } catch (Exception e) {
                                                    log.error("::" + CommonUtil.getRequestId(matchId) + "::tansferMatchInfo异常,oddsFields.id=" + oddsFields.getId(), e);
                                                    oddsFields.setFieldOddsValue("0");
                                                }
                                            } else {
                                                oddsFields.setFieldOddsValue("0");
                                            }
                                        });

                                    }

                                    if (Arrays.asList(174L, 204L, 159L).contains(categoryId)) {
                                        matchMarket.setMarginValue(OddsConvertUtils.calMarginByOddsList(matchMarket.getOddsFieldsList(), marketKindEnum));
                                    }
                                }

                                if (Arrays.asList(4L, 5L, 7L, 8L, 9L, 10L).contains(sportId) && SportIdEnum.noMain(sportId, conVo.getId())) {
                                    matchMarketVoList = matchMarketVoList.stream().filter(fi -> fi.getSetId() != null && fi.getSetId().equals(conVo.getId())).collect(Collectors.toList());
                                    marketCategory.setMatchMarketVoList(matchMarketVoList);
                                    if (!CollectionUtils.isEmpty(matchMarketVoList)) {
                                        marketCategory.setRelevanceType(matchMarketVoList.get(0).getRelevanceType());
                                    }

                                }

                            }
                            List<ThirdSportMarketMessage> thirdMarketList = marketCategory.getThirdMarketList();
                            if (SportIdEnum.isFootball(sportId) && !CollectionUtils.isEmpty(thirdMarketList)) {
                                for (ThirdSportMarketMessage thirdMarket : thirdMarketList) {
                                    if (!CollectionUtils.isEmpty(thirdMarket.getThirdSportMarketOddsList())) {
                                        thirdMarket.getThirdSportMarketOddsList().stream().forEach(oddsFields -> {
                                            if (oddsFields.getOddsValue() != null) {
                                                try {
                                                    String displayOddsVal = OddsValueConvertUtils.convertAndDefaultDisply(marketKindEnum, oddsFields.getOddsValue());
                                                    oddsFields.setFieldOddsValue(displayOddsVal);
                                                } catch (Exception e) {
                                                    log.error("::" + CommonUtil.getRequestId(matchId, categoryIds, categoryId) + "::tansferMatchInfo异常,oddsFields.id=" + oddsFields.getId(), e);
                                                    oddsFields.setFieldOddsValue("0");
                                                }
                                            } else {
                                                oddsFields.setFieldOddsValue("0");
                                            }
                                        });
                                    }
                                }
                            }

                            tradeCategories.add(marketCategory);
                        }

                        if (null != queryVo.getTradeType()) {
                            tradeCategories = tradeCategories.stream().filter(filter -> queryVo.getTradeType().equals(filter.getTradeType())).collect(Collectors.toList());
                            matchBean.setMarketCategoryList(tradeCategories);
                        } else {
                            matchBean.setMarketCategoryList(tradeCategories);
                        }
                    }
                    matchBean.setMarketCount(matchBean.getMarketCount() == null ? 1 : matchBean.getMarketCount());
                    //设置未读备忘录id
                    matchBean.setMemoIds(needRemindMatchMemos.get(matchBean.getMatchId()));
                    sortMatchs.add(matchBean);
                }
                if (!CollectionUtils.isEmpty(sortMatchs)) {
                    ListUtils.sort(sortMatchs, true, "sortId");
                    result.addAll(sortMatchs);
                }
            } catch (Exception e) {
                log.error("::" + CommonUtil.getRequestId(match.getMatchId()) + "::tansferMatchInfo赛事异常" + JsonFormatUtils.toJson(match), e);
            }

        }
        return result;
    }

    @Override
    public List<StandardTxThirdMarketPlayDTO> queryMultiOdds(MarketLiveOddsQueryVo queryVo) {
        Query thirdQuery = new Query();
        Criteria thirdCriteria = new Criteria();
        Long matchId = queryVo.getMatchId();
        Integer oddBusiness = queryVo.getLiveOddBusiness();
        String weightTedisKey = String.format(REDIS_KEY_MULTI_ODDS_WEIGHT_VALUE, matchId, oddBusiness == 1 ? 0 : 1);
        Object o = redisClient.hGetAllToObj(weightTedisKey);
        List<String> keyList = new ArrayList<String>();
        if (!ObjectUtils.isEmpty(o)) {
            Map<String, Object> map = JsonFormatUtils.fromJson(o.toString(), Map.class);
            List<String> keys = map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            keyList.addAll(keys);
        }
        /*Set set = map.entrySet();
        List<String> keyList = new ArrayList<String>();
        keyList.addAll(set);*/

        List<Long> marketCategoryIds = queryVo.getMarketCategoryIds();
        String marketOddsKind = queryVo.getMarketOddsKind();
        MarketKindEnum marketKindEnum = MarketKindEnum.getMarketKindByValue(StringUtils.isNotBlank(marketOddsKind) ? marketOddsKind : "EU");
        thirdCriteria.and("matchId").is(String.valueOf(matchId)).and("marketCategoryId").in(marketCategoryIds);
        if (CollectionUtils.isEmpty(keyList)) {
            thirdCriteria.and("dataSourceCode").ne("MULTI-Source");
        } else {
            thirdCriteria.and("dataSourceCode").in(keyList);
        }
        //thirdCriteria.and("marketList.thirdMarketSourceStatus").ne(2);
        thirdQuery.addCriteria(thirdCriteria);
        List<StandardTxThirdMarketPlayDTO> thirdPlayMarkets = mongoTemplate.find(thirdQuery, StandardTxThirdMarketPlayDTO.class);
        if (!CollectionUtils.isEmpty(thirdPlayMarkets)) {
            for (StandardTxThirdMarketPlayDTO playDTO : thirdPlayMarkets) {
                List<ThirdSportMarketMessage> marketList = playDTO.getMarketList();
                if (!CollectionUtils.isEmpty(marketList)) {
                    for (ThirdSportMarketMessage thirdMarket : marketList) {
                        if (!CollectionUtils.isEmpty(thirdMarket.getThirdSportMarketOddsList())) {
                            thirdMarket.getThirdSportMarketOddsList().stream().forEach(oddsFields -> {
                                if (oddsFields.getOddsValue() != null) {
                                    try {
                                        String displayOddsVal = OddsValueConvertUtils.convertAndDefaultDisply(marketKindEnum, oddsFields.getOddsValue());
                                        oddsFields.setFieldOddsValue(displayOddsVal);
                                    } catch (Exception e) {
                                        log.error("::" + CommonUtil.getRequestId(matchId) + "::queryMultiOdds异常,oddsFields.id=" + oddsFields.getId(), e);
                                        oddsFields.setFieldOddsValue("0");
                                    }
                                } else {
                                    oddsFields.setFieldOddsValue("0");
                                }
                            });
                        }
                    }
                    if (!Arrays.asList(1L, 17L).contains(playDTO.getMarketCategoryId()))
                        ListUtils.sort(marketList, true, "addition1");
                    List<ThirdSportMarketMessage> messageList = marketList.stream().filter(fi -> fi.getStatus() != null && fi.getStatus() != 2).collect(Collectors.toList());
                    playDTO.setMarketList(messageList);
                }
            }
        }

        return thirdPlayMarkets;
    }


    @Override
    public Map<String, List<ThirdMarketVo>> getMultiOdds(MarketLiveOddsQueryVo queryVo) {
        List<StandardTxThirdMarketPlayDTO> thirdPlayMarkets = queryMultiOdds(queryVo);
        Integer oddBusiness = queryVo.getLiveOddBusiness();
        List<Long> marketCategoryIds = queryVo.getMarketCategoryIds();
        String weightTedisKey = String.format(REDIS_KEY_MULTI_ODDS_WEIGHT_VALUE, queryVo.getMatchId(), oddBusiness == 1 ? 0 : 1);
        Object o = redisClient.hGetAllToObj(weightTedisKey);
        List<String> keyList = new ArrayList<String>();
        if (!ObjectUtils.isEmpty(o)) {
            Map<String, Object> map = JsonFormatUtils.fromJson(o.toString(), Map.class);
            keyList = map.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
            keyList.remove("cautionValue");

        }
        Map<String, List<ThirdMarketVo>> result = new LinkedHashMap<>();
        if (!CollectionUtils.isEmpty(thirdPlayMarkets)) {
            Map<String, List<StandardTxThirdMarketPlayDTO>> thirdMarketMap = thirdPlayMarkets.stream().collect(Collectors.groupingBy(e -> e.getDataSourceCode()));
            for (Map.Entry<String, List<StandardTxThirdMarketPlayDTO>> entry : thirdMarketMap.entrySet()) {
                List<StandardTxThirdMarketPlayDTO> playMarkets = entry.getValue();

                List<ThirdMarketVo> markets = new ArrayList<>();
                for (StandardTxThirdMarketPlayDTO playDTO : playMarkets) {
                    ThirdMarketVo marketVo = new ThirdMarketVo();
                    List<ThirdSportMarketMessage> marketList = playDTO.getMarketList();
                    marketVo.setCategoryId(playDTO.getMarketCategoryId());
                    marketVo.setThirdMarkets(marketList);
                    if (!CollectionUtils.isEmpty(marketList)) {
                        List<String> heads = marketList.stream().filter(fi -> StringUtils.isNotBlank(fi.getAddition1())).map(ThirdSportMarketMessage::getAddition1).distinct().collect(Collectors.toList());
                        marketVo.setMarketHeads(heads);
                    }
                    markets.add(marketVo);
                }
                result.put(entry.getKey(), markets);
                keyList.remove(entry.getKey());
            }
        }


        if (!CollectionUtils.isEmpty(keyList)) {
            for (String dataSourceCode : keyList) {
                Map<Long, List<ThirdSportMarketMessage>> map = new HashMap<>();
                List<ThirdMarketVo> markets = new ArrayList<>();
                for (Long categoryId : marketCategoryIds) {
                    ThirdMarketVo marketVo = new ThirdMarketVo();
                    marketVo.setCategoryId(categoryId);
                    markets.add(marketVo);
                }
                result.put(dataSourceCode,markets);
            }
        }
        return result;
    }


    private Integer getPlaySetCodeStatus(Long matchId, String playSetCode) {
        if (StringUtils.isBlank(playSetCode)) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        String key = getPlaySetCodeStatusKey(matchId);
        String value = redisClient.hGet(key, playSetCode);
        return NumberUtils.toInt(value, TradeStatusEnum.OPEN.getStatus());
    }

    List<MatchMarketLiveBean> queryMatchs(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo) {
    	// 体育类型ID
        Long sportId = queryVo.getSportId();
        // 操盘手ID
        String tradeId = String.valueOf(queryVo.getTradeId());
        // 是否支持滚球:1=支持；0=不支持(变更为是否开滚球，对应siness字段)
        Integer oddBusiness = queryVo.getLiveOddBusiness();
        // 选择类型 1 所有自选 2 仅自己操盘 3 仅自己收藏 4 所有赛事
        Integer chooseType = queryVo.getChooseType();
        if (CollectionUtils.isEmpty(matchInfos)) return new ArrayList<>();
        Map<Long, List<MatchSetVo>> longListMap = matchSetMongoService.queryMatchLevelSnap(matchInfos);

        // 赛事模板的滚球/操盘
        Integer matchTypeTemplate = 1;
        if (oddBusiness != null && oddBusiness == 1) {
        	matchTypeTemplate = 0;
        }
        // 根据赛事id查询联赛模板名，并且国际化
        List<TemplateNameForMatchDto> getTemplateNameForMatch = getTemplateName(sportId, matchInfos.stream().map(MatchMarketLiveBean :: getMatchId).collect(Collectors.toList()), matchTypeTemplate);
        
        //查询指派赛事
        List<Long> tradeMatchIds = tradingAssignmentMapper.queryTradeMatchIds(String.valueOf(tradeId), oddBusiness == null ? 0 : 1);
        for (MatchMarketLiveBean match : matchInfos) {
            try {
                if (null == match.getOperateMatchStatus()) {
                    match.setOperateMatchStatus(MarketStatusEnum.OPEN.getState());
                }
                List<Integer> tradeType = new ArrayList<>();
                if (null != match.getManualCount() && null != match.getAutoCount() && null != match.getAutoAddCount()) {
                    if (match.getAutoCount() > 0) tradeType.add(TradeEnum.AUTO.getCode());
                    if (match.getManualCount() > 0) tradeType.add(TradeEnum.MANUAD.getCode());
                    if (match.getAutoAddCount() > 0) tradeType.add(TradeEnum.AUTOADD.getCode());
                }
                if (CollectionUtils.isEmpty(match.getSetInfos()) || null == match.getCategoryCount() || match.getCategoryCount().equals(0)) {
                    match.setTradeType(Arrays.asList(0));
                }

                List<I18nItemVo> tournamentNames = match.getTournamentNames();
                ArrayList<I18nItemVo> objects = new ArrayList<>();
                if (tournamentNames == null) tournamentNames = new ArrayList<>();
                for (I18nItemVo tournamentName : tournamentNames) {
                    if (tournamentName.getLanguageType().equals("zs") || tournamentName.getLanguageType().equals("en")) {
                        objects.add(tournamentName);
                    }
                }
                match.setTournamentNames(objects);

                List<MatchTeamVo> teamList = match.getTeamList();
                if (!CollectionUtils.isEmpty(teamList)) {
                    for (MatchTeamVo matchTeamVo : teamList) {
                        if (null != matchTeamVo.getNames()) {
                            Map<String, String> names = matchTeamVo.getNames();
                            String zs = StringUtils.isNotBlank(names.get("zs")) ? names.get("zs") : "";
                            String en = StringUtils.isNotBlank(names.get("en")) ? names.get("en") : "";
                            matchTeamVo.setNames(new HashMap<>());
                            matchTeamVo.getNames().put("en", en);
                            matchTeamVo.getNames().put("zs", zs);
                        }
                    }
                }
                //是否自己操盘
                if ((oddBusiness == null && tradeId.equals(match.getPreTraderId()))
                        || (oddBusiness != null && oddBusiness == 1 && tradeId.equals(match.getLiveTraderId()))
                        || (!CollectionUtils.isEmpty(tradeMatchIds) && tradeMatchIds.contains(match.getMatchId())))
                    match.setTraderStatus(true);

                //收藏状态
                if (null != chooseType && chooseType != 4) match.setMatchCollectStatus(true);
                //TODO 后期可删除
                List<MatchCatgorySetVo> setInfos = match.getSetInfos();
                if (!CollectionUtils.isEmpty(setInfos)) {
                    setInfos.stream().forEach(setVo -> {
                        if (StringUtils.isBlank(setVo.getScore()) && !"0:0".equals(match.getScore()) && Arrays.asList(10001L, 20001L).contains(setVo.getCatgorySetId())) {
                            setVo.setScore(match.getScore());
                        }
                    });
                }

                if (match.getTraderNum() == null) match.setTraderNum(1);

                //赛事时间过后，则显示即将开赛
                Long beginTime = DateUtils.tranferStringToDate(match.getMatchStartTime()).getTime() - System.currentTimeMillis();
                /*if (match.getMatchStatus() == 0 && oddBusiness != null && oddBusiness == 1 && beginTime < 0) {
                    match.setMatchStatus(1);
                }*/

                //判断赛前十五分钟
                if (beginTime > 0 && null != oddBusiness && 1 == oddBusiness && match.getMatchStatus() == 0) {
                    match.setSecondsMatchStart((int) (beginTime / 1000));
                    match.setMatchSnapshot(1);
                    if (!CollectionUtils.isEmpty(longListMap)) matchSetMongoService.matchLevelSnap(match, longListMap);
                }

                if (null != match.getTournamentLevel() && match.getTournamentLevel() == 99) {
                    match.setTournamentLevel(0);
                }

                //1631
                String thirdMatchListStr = match.getThirdMatchListStr();
                if (StringUtils.isNotBlank(thirdMatchListStr)) {
                    List<ThirdDataSourceCodeVo> codeVos = JSONArray.parseArray(thirdMatchListStr, ThirdDataSourceCodeVo.class);
                    ThirdDataSourceCodeVo thirdDataSourceCodeVo = codeVos.stream().filter(vo -> "AO".equals(vo.getDataSourceCode())).findFirst().orElse(null);
                    if (null != thirdDataSourceCodeVo) match.setAoId(thirdDataSourceCodeVo.getThirdMatchSourceId());
                }
                
                // set templateName
                Optional<TemplateNameForMatchDto> findFirst = getTemplateNameForMatch.stream().filter(t -> t.getMatchId().equals(match.getMatchId())).findFirst();
                if (findFirst.isPresent()) {
                	match.setTemplateName(findFirst.get().getTemplateName());
                }
            } catch (Exception e) {
                log.error("::" + CommonUtil.getRequestId(match.getMatchId()) + "::queryMatchs赛事异常," + JsonFormatUtils.toJson(match), e);
            }
        }
        //根据赛种特殊处理
        if (sportId.equals(SportIdEnum.FOOTBALL.getId())) {
            footballMatchs(matchInfos, queryVo);
        } else if (sportId.equals(SportIdEnum.BASKETBALL.getId())) {
            basketballMatchs(matchInfos, queryVo);
        } else if (SportIdEnum.isBaseBall(sportId)) {
            baseBallSportMatchs(matchInfos, queryVo);
        } else if (SportIdEnum.isTennis(sportId) || SportIdEnum.isPingpong(sportId) || SportIdEnum.isIceHockey(sportId)
                || SportIdEnum.isVolleyball(sportId) || SportIdEnum.isSnooker(sportId) || SportIdEnum.BADMINTON.isYes(sportId)) {
            otherSportMatchs(matchInfos, queryVo);
        }
        return matchInfos;
    }


    /**
     * 	根据赛事id查询联赛模板名，并且国际化
     * 
     * @param sportId
     * @param matchIds
     * @param matchTypeTemplate
     * @return
     */
    private List<TemplateNameForMatchDto> getTemplateName(Long sportId, List<Long> matchIds, Integer matchTypeTemplate) {
    	// 返回赛事id对应的type=1联赛等级，
    	List<TemplateNameForMatchDto> getTemplateNameForMatch = new ArrayList<>(matchIds.size());
    			
		try {
	    	getTemplateNameForMatch = templateMapper.getTemplateNameForMatch(sportId, matchIds, matchTypeTemplate);
	    	// 当前请求头的lang
	    	ServletRequestAttributes requestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
	    	String lang = requestAttributes.getRequest().getHeader("lang");
			if (StringUtils.isEmpty(lang)) {
				lang = "zs";
			}
	    	// 根据当前请求头lang设置联赛模板名
	    	for (TemplateNameForMatchDto target : getTemplateNameForMatch) {
	    		// 级别联赛，设置联赛名
				if (target.getType() == 1) {
				    if (lang.equals("en")) {
				    	target.setTemplateName(StringUtils.isNotBlank(NumberConventer.GetEN(target.getLevelNum()))?NumberConventer.GetEN(target.getLevelNum()):"无");
	                } else {
	                	// 早盘
	                	if (matchTypeTemplate == 1) {
	                		target.setTemplateName(StringUtils.isNotBlank(NumberConventer.GetCH(target.getLevelNum()))?NumberConventer.GetCH(target.getLevelNum()) + "级联赛早盘模板":"无");
	                	} else {
	                		target.setTemplateName(StringUtils.isNotBlank(NumberConventer.GetCH(target.getLevelNum()))?NumberConventer.GetCH(target.getLevelNum()) + "级联赛滚球模板":"无");
	                	}
	                	
	                }
				}
			}
		} catch (Exception e) {
			log.error("::" + CommonUtil.getRequestId() + "::getTemplateName操盘查询联赛模板名失败" + e.getMessage(), e);
		}
		return getTemplateNameForMatch;
	}

	void footballMatchs(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo) {
        if (CollectionUtils.isEmpty(matchInfos)) return;
        Integer oddBusiness = queryVo.getLiveOddBusiness();
        Integer chooseType = queryVo.getChooseType();
        Long tradeId = queryVo.getTradeId();
        for (MatchMarketLiveBean match : matchInfos) {
            Long beginTime = DateUtils.tranferStringToDate(match.getMatchStartTime()).getTime() - System.currentTimeMillis();
            //非赛前十五分钟赛事处理
            if (!(beginTime > 0 && null != oddBusiness && 1 == oddBusiness && match.getMatchStatus() == 0)) {
                match.setMatchSnapshot(0);
                Integer secondsMatchStart = match.getSecondsMatchStart();
                Long eventTime = match.getEventTime() == null ? 0 : match.getEventTime();
                // Long time =eventTime > 0 ? (System.currentTimeMillis() - eventTime) / 1000: 0;
                //当前时间-当前事件时间>10分钟 就不计算差值
                Long time = (System.currentTimeMillis() - eventTime) > 10 * 60 * 1000 ? 0 : (System.currentTimeMillis() - eventTime) / 1000;
                Integer secondsTime = match.getSecondsMatchStart() + time.intValue();
                /*log.info("::{}::赛事时间" + match.getMatchId() + "当前时间:" + DateUtils.transferLongToDateStrings(System.currentTimeMillis())
                        + "事件编码:" + match.getEventCode() + "事件时间:" + DateUtils.transferLongToDateStrings(eventTime) +
                        "比赛进行时间:" + match.getSecondsMatchStart() + "结果:" + secondsTime,CommonUtil.getRequestId());*/

                match.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);

                if (StringUtils.isNotBlank(match.getEventCode()) && (match.getEventCode().equals("timeout"))) {
                    match.setSecondsMatchStart(secondsMatchStart);
                }

                if (Arrays.asList(1, 2, 10).contains(match.getMatchStatus()) && StringUtils.isNotBlank(match.getEventCode())) {
                    String key = String.format(RCS_FOOTBALL_TIME, match.getMatchId(), match.getPeriod());
                    if (StringUtils.isNotBlank(redisClient.get(key))) {
                        MatchMarketLiveBean redisMatch = JsonFormatUtils.fromJson(redisClient.get(key), MatchMarketLiveBean.class);
                        if (redisMatch != null) {
                            int redisTime = redisMatch.getSecondsMatchStart();
                            Integer redisPeriod = redisMatch.getPeriod();
                            Integer redisStatus = redisMatch.getMatchStatus();

                            if (!match.getMatchStatus().equals(redisStatus) || !match.getPeriod().equals(redisPeriod)) {
                                redisClient.delete(String.format(RCS_FOOTBALL_TIME, match.getMatchId(), redisPeriod));
                                MatchMarketLiveBean copeMatch = BeanCopyUtils.copyProperties(redisMatch, MatchMarketLiveBean.class);
                                copeMatch.setSecondsMatchStart(0);
                                redisClient.setExpiry(key, JsonFormatUtils.toJson(copeMatch), EXPRIY_TIME_5_MINS);
                            } else {
                                if (match.getSecondsMatchStart() < redisTime) {
                                    match.setSecondsMatchStart(redisTime);
                                }
                                redisClient.setExpiry(key, JsonFormatUtils.toJson(match), EXPRIY_TIME_5_MINS);
                            }
                        }
                    }
                    //log.info("::{}::赛事时间" + match.getMatchId() + "secondsMatchStart:" + match.getSecondsMatchStart(),CommonUtil.getRequestId());
                }

            }

            List<MatchCatgorySetVo> setInfos = match.getSetInfos();
            setInfos = footballTransferSetInfos(setInfos);
            ListUtils.sort(setInfos, true, "sort");
            match.setSetInfos(setInfos);
        }
        if (chooseType != null && chooseType == 4) {
            existList(matchInfos, tradeId);
        }

    }

    void basketballMatchs(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo) {
        if (CollectionUtils.isEmpty(matchInfos)) return;

        for (MatchMarketLiveBean match : matchInfos) {
            Integer matchSnapshot = match.getMatchSnapshot();
            if (matchSnapshot == null || matchSnapshot == 0) {
                Integer secondsTime = 0;
                Integer secondsMatchStart = match.getSecondsMatchStart();
                Long time = (System.currentTimeMillis() - match.getEventTime()) / 1000;
                secondsTime = secondsMatchStart - time.intValue();
                /*log.info("::{}::赛事时间" + match.getMatchId() + "当前时间:" + DateUtils.transferLongToDateStrings(System.currentTimeMillis())
                        + "事件编码:" + match.getEventCode() + "事件时间:" + DateUtils.transferLongToDateStrings(match.getEventTime()) +
                        "比赛进行时间:" + match.getSecondsMatchStart() + "结果:" + secondsTime,CommonUtil.getRequestId());*/
                match.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);


                if (StringUtils.isNotBlank(match.getEventCode()) && (match.getEventCode().equals("timeout"))) {
                    match.setSecondsMatchStart(secondsMatchStart);
                }

                if (Arrays.asList(1, 2, 10).contains(match.getMatchStatus()) && StringUtils.isNotBlank(match.getEventCode())) {
                    String key = String.format(RCS_BASKETBALL_TIME, match.getMatchId(), match.getPeriod());
                    if (StringUtils.isNotBlank(redisClient.get(key))) {
                        int redisTime = Integer.parseInt(redisClient.get(key));
                        if (match.getSecondsMatchStart() > redisTime && redisTime > 0) {
                            if (redisTime > 0) {
                                match.setSecondsMatchStart(redisTime);
                            } else if (redisTime == 0) {
                                match.setSecondsMatchStart(0);
                            }
                        }
                    }
                    redisClient.setExpiry(key, match.getSecondsMatchStart(), EXPRIY_TIME_2_HOURS);
                    //log.info("::{}::赛事时间" + match.getMatchId() + "secondsMatchStart:" + match.getSecondsMatchStart(), CommonUtil.getRequestId());
                }
            }
            List<MatchCatgorySetVo> setInfos = match.getSetInfos();

            setInfos = transferSetInfos(setInfos);
            if (!CollectionUtils.isEmpty(setInfos) && match.getPeriod() > 0) {
                BasketballEnum periodEnum = BasketballEnum.getEnumByPeriod(match.getPeriod());
                setInfos.stream().forEach(setVo -> {
                    setVo.setSort(BasketballEnum.getSort(setVo.getCatgorySetId()));
                });
                if (null != periodEnum && periodEnum.getSort() > 0) {
                    setInfos = setInfos.stream().filter(vo -> vo.getSort() >= periodEnum.getSort()).collect(Collectors.toList());
                }
            }
            ListUtils.sort(setInfos, true, "catgorySetId");
            match.setSetInfos(setInfos);
        }
    }

    //棒球盘口有主玩法 ，前五局玩法， 1-3局，4-6局，7-9局
    void baseBallSportMatchs(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo) {
        Long sportId = queryVo.getSportId();
        if (CollectionUtils.isEmpty(matchInfos)) return;
        List<CategoryConVo> categorySetVos = categoryService.mainCategory(sportId);
        for (MatchMarketLiveBean match : matchInfos) {
            List<MatchCatgorySetVo> setInfos = new ArrayList<>();
            if (!CollectionUtils.isEmpty(match.getBaseBallScoreVos())) {
                Map<Integer, BaseBallScoreVo> map = match.getBaseBallScoreVos().stream().collect(Collectors.toMap(BaseBallScoreVo::getPeriod, a -> a));
                if (map.containsKey(0)) {
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30001L).setScore(map.get(0).getMatchScore()));
                } else {
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30001L));
                }
                if (map.containsKey(505)) {
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30002L).setScore(map.get(505).getMatchScore()));
                } else {
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30002L));
                }
                
              setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30003L));
              setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30004L));
              setInfos.add(new MatchCatgorySetVo().setCatgorySetId(30005L));
            } else {
                for (CategoryConVo set : categorySetVos) {
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(set.getId()));
                }
            }
            ListUtils.sort(setInfos, true, "catgorySetId");
            match.setSetInfos(setInfos);
        }

    }

    void otherSportMatchs(List<MatchMarketLiveBean> matchInfos, MarketLiveOddsQueryVo queryVo) {
        Long sportId = queryVo.getSportId();

        if (CollectionUtils.isEmpty(matchInfos)) return;
        for (MatchMarketLiveBean match : matchInfos) {
            if (SportIdEnum.isIceHockey(sportId)) {
                Integer matchSnapshot = match.getMatchSnapshot();
                if (matchSnapshot == null || matchSnapshot == 0) {
                    Integer secondsTime = 0;
                    Integer secondsMatchStart = match.getSecondsMatchStart();
                    Long time = (System.currentTimeMillis() - match.getEventTime()) / 1000;
                    secondsTime = secondsMatchStart - time.intValue();
                    match.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);
            
            
                    if (StringUtils.isNotBlank(match.getEventCode()) && (match.getEventCode().equals("timeout"))) {
                        match.setSecondsMatchStart(secondsMatchStart);
                    }
            
                    if (Arrays.asList(1, 2, 10).contains(match.getMatchStatus()) && StringUtils.isNotBlank(match.getEventCode())) {
                        String key = String.format(RCS_ICE_HOCKEY_TIME, match.getMatchId(), match.getPeriod());
                        if (StringUtils.isNotBlank(redisClient.get(key))) {
                            int redisTime = Integer.parseInt(redisClient.get(key));
                            if (match.getSecondsMatchStart() > redisTime && redisTime > 0) {
                                if (redisTime > 0) {
                                    match.setSecondsMatchStart(redisTime);
                                } else if (redisTime == 0) {
                                    match.setSecondsMatchStart(0);
                                }
                            }
                        }
                        redisClient.setExpiry(key, match.getSecondsMatchStart(), EXPRIY_TIME_2_HOURS);
                        //log.info("::{}::赛事时间" + match.getMatchId() + "secondsMatchStart:" + match.getSecondsMatchStart(), CommonUtil.getRequestId());
                    }
                }
            }
    
            Integer roundType = match.getRoundType();
            List<ScoreVo> scoreVos = match.getScoreVos();
            int start = 8;
            int end = 13;
            if (SportIdEnum.isIceHockey(sportId)) {
                roundType = 3;
            }
            if (SportIdEnum.isSnooker(sportId) || SportIdEnum.isBaseBall(sportId) || SportIdEnum.isIceHockey(sportId)) {
                start = 1;
                end = roundType + 1;
            }

            if (CollectionUtils.isEmpty(scoreVos)) {
                scoreVos = new ArrayList<>();
                for (int i = start; i < end; i++) {
                    ScoreVo vo = new ScoreVo();
                    vo.setPeriod(i);
                    scoreVos.add(vo);
                }
            } else {
                for (int i = start; i < end; i++) {
                    Integer period = i;
                    ScoreVo scoreVo = scoreVos.stream().filter(fi -> fi.getPeriod() != null && fi.getPeriod().equals(period)).findFirst().orElse(null);
                    if (scoreVo == null) {
                        ScoreVo vo = new ScoreVo();
                        vo.setPeriod(i);
                        scoreVos.add(vo);
                    }
                }
            }
            scoreVos = scoreVos.stream().filter(fi -> fi.getPeriod() != null).collect(Collectors.toList());
            ListUtils.sort(scoreVos, true, "period");
            match.setScoreVos(scoreVos);


            List<MatchCatgorySetVo> setInfos = match.getSetInfos();

            List<CategoryConVo> categorySetVos = categoryService.mainCategory(sportId);

            if (CollectionUtils.isEmpty(setInfos)) {
                setInfos = new ArrayList<>();
                for (CategoryConVo set : categorySetVos) {
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(set.getId()));
                }
            } else {
                for (CategoryConVo set : categorySetVos) {
                    MatchCatgorySetVo setVo = setInfos.stream().filter(vo -> vo.getCatgorySetId().equals(set.getId())).findFirst().orElse(null);
                    if (setVo == null) {
                        setInfos = new ArrayList<>(setInfos);
                        setInfos.add(new MatchCatgorySetVo().setCatgorySetId(set.getId()));
                    }
                }
            }
            ListUtils.sort(setInfos, true, "catgorySetId");

            if (!CollectionUtils.isEmpty(setInfos) && roundType != null) {
                Integer limit = 5;
                if (SportIdEnum.isPingpong(sportId) || SportIdEnum.isVolleyball(sportId)) {
                    limit = 8;
                } else if (SportIdEnum.isSnooker(sportId)) {
                    limit = 35;
                }
                if (roundType < limit) {
                    setInfos = setInfos.subList(0, roundType + 1);
                }
            }
            match.setSetInfos(setInfos);
        }
    }

    //过滤玩法集
    List<MatchCatgorySetVo> transferSetInfos(List<MatchCatgorySetVo> setInfos) {
        BasketballEnum[] values = BasketballEnum.values();
        if (CollectionUtils.isEmpty(setInfos)) {
            setInfos = new ArrayList<>();
            for (BasketballEnum b : values) {
                setInfos.add(new MatchCatgorySetVo().setCatgorySetId(b.getCategorySetId()));
            }
        } else {
            for (BasketballEnum b : values) {
                MatchCatgorySetVo setVo = setInfos.stream().filter(vo -> vo.getCatgorySetId().equals(b.getCategorySetId())).findFirst().orElse(null);
                if (setVo == null) {
                    setInfos = new ArrayList<>(setInfos);
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(b.getCategorySetId()));
                }
            }
        }
        return setInfos;
    }


    List<MatchCatgorySetVo> footballTransferSetInfos(List<MatchCatgorySetVo> setInfos) {
        List<Long> footballIds = Arrays.asList(10001L, 10002L, 10003L, 10004L, 10005L);

        if (CollectionUtils.isEmpty(setInfos)) {
            setInfos = new ArrayList<>();
            for (Long id : footballIds) {
                setInfos.add(new MatchCatgorySetVo().setCatgorySetId(id).setSort(PeriodEnum.getSort(id)));
            }
        } else {
            for (Long id : footballIds) {
                MatchCatgorySetVo setVo = setInfos.stream().filter(vo -> vo.getCatgorySetId().equals(id)).findFirst().orElse(null);
                if (setVo == null) {
                    setInfos = new ArrayList<>(setInfos);
                    setInfos.add(new MatchCatgorySetVo().setCatgorySetId(id).setSort(PeriodEnum.getSort(id)));
                }
            }
        }
        return setInfos;
    }


    /**
     * 查询所有赛事玩法
     *
     * @param matchInfos
     * @param sportId
     * @return
     */
    Map<String, List<MarketCategory>> getCategoryColls(List<MatchMarketLiveBean> matchInfos, Long sportId) {
        List<CategoryConVo> categoryConVos = categoryService.mainCategory(sportId);
        List<Long> mianIds = new ArrayList<>();
        categoryConVos.stream().forEach(model -> {
            mianIds.addAll(model.categoryIds());
        });
        List<String> matchIds = matchInfos.stream().map(map -> String.valueOf(map.getMatchId())).collect(Collectors.toList());
        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("matchId").in(matchIds).and("id").in(mianIds);
     /*   criteria.and("matchMarketVoList.oddsFieldsList.id").gt(0L);
        //数据源展示开、封状态
        criteria.and("matchMarketVoList.thirdMarketSourceStatus").in(Arrays.asList(0, 1));*/
        query.addCriteria(criteria);
        List<MarketCategory> marketCategories = mongoTemplate.find(query, MarketCategory.class);
        //查詢三方賠率
        if (SportIdEnum.isFootball(sportId)) {
            Query thirdQuery = new Query();
            Criteria thirdCriteria = new Criteria();
            thirdCriteria.and("matchId").in(matchIds).and("marketCategoryId").in(mianIds).and("dataSourceCode").is("MULTI-Source");
            //thirdCriteria.and("marketList.thirdMarketSourceStatus").ne(2);
            thirdQuery.addCriteria(thirdCriteria);
            List<StandardTxThirdMarketPlayDTO> thirdPlayMarkets = mongoTemplate.find(thirdQuery, StandardTxThirdMarketPlayDTO.class);
            Map<String, List<ThirdSportMarketMessage>> thirdMarketMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(thirdPlayMarkets)) {
                thirdMarketMap = thirdPlayMarkets.stream().collect(Collectors.toMap(e -> e.getMatchId() + "_" + e.getMarketCategoryId(), e -> e.getMarketList(), (v1, v2) -> v1));
            }
            if (CollectionUtils.isEmpty(marketCategories)) {
                return null;
            } else {
                if (!CollectionUtils.isEmpty(thirdMarketMap)) {
                    for (MarketCategory category : marketCategories) {
                        List<ThirdSportMarketMessage> thirdMarkets = thirdMarketMap.get(category.getMatchId() + "_" + category.getId());
                        category.setThirdMarketList(thirdMarkets);
                        /*Map<String, List<ThirdSportMarketOdds>> messageMap = new HashMap<>();
                        if(!CollectionUtils.isEmpty(thirdMarkets)){
                            messageMap = thirdMarkets.stream().collect(Collectors.toMap(e -> e.getRelationMarketId(), e -> e.getThirdSportMarketOddsList(), (v1, v2) -> v1));
                        }
                        if(!CollectionUtils.isEmpty(marketVoList)&&!CollectionUtils.isEmpty(messageMap)){

                            for (MatchMarketVo marketVo:marketVoList) {
                                List<ThirdSportMarketOdds> thirdSportMarketOdds = messageMap.get(String.valueOf(marketVo.getMarketId()));
                                if(!CollectionUtils.isEmpty(thirdSportMarketOdds)){
                                    ListUtils.sort(thirdSportMarketOdds, true, "orderOdds");
                                    marketVo.setThirdSportMarketOddsList(thirdSportMarketOdds);
                                }

                            }
                        }*/
                    }
                }
            }
        }

        Map<String, List<MarketCategory>> mapCategory = marketCategories.stream().collect(Collectors.groupingBy(marketCategory -> marketCategory.getMatchId()));
        return mapCategory;
    }


    @Override
    public List<MatchMarketLiveBean> existList(List<MatchMarketLiveBean> matchs, Long tradeId) {
        if (!CollectionUtils.isEmpty(matchs)) {
            List<Long> matchIds = matchs.stream().map(map -> map.getMatchId()).collect(Collectors.toList());

            //查询收藏赛事
           /* QueryWrapper<RcsMatchCollection> queryWrapper = new QueryWrapper();
            queryWrapper.lambda().eq(RcsMatchCollection::getType, 1)
                    .eq(RcsMatchCollection::getUserId, tradeId)
                    .in(RcsMatchCollection::getMatchId, matchIds);
            List<RcsMatchCollection> matchCollections = matchCollectionMapper.selectList(queryWrapper);
            Map<Long, Integer> matchMap = null;
            if (!CollectionUtils.isEmpty(matchCollections)) {
                matchMap = matchCollections.stream().collect(Collectors.toMap(RcsMatchCollection::getMatchId, RcsMatchCollection::getStatus, (v1, v2) -> v1));
            }*/
            //查询收藏联赛

            List<Long> tournameIds = matchs.stream().map(map -> map.getStandardTournamentId()).collect(Collectors.toList());
            QueryWrapper<RcsMatchCollection> wrapper = new QueryWrapper();
            wrapper.lambda().eq(RcsMatchCollection::getUserId, tradeId)
                    .and(Wrapper -> Wrapper.in(RcsMatchCollection::getMatchId, matchIds).or().in(RcsMatchCollection::getTournamentId, tournameIds));
            List<RcsMatchCollection> matchCollectionList = matchCollectionMapper.selectList(wrapper);
            Map<Long, Integer> tourMap = null;
            Map<Long, Integer> matchMap = null;
            if (!CollectionUtils.isEmpty(matchCollectionList)) {
                matchMap = matchCollectionList.stream().filter(fi -> fi.getType() == 1).collect(Collectors.toMap(RcsMatchCollection::getMatchId, RcsMatchCollection::getStatus, (v1, v2) -> v1));
                tourMap = matchCollectionList.stream().filter(fi -> fi.getType() == 2).collect(Collectors.toMap(RcsMatchCollection::getTournamentId, RcsMatchCollection::getStatus, (v1, v2) -> v1));
            }
            for (MatchMarketLiveBean match : matchs) {
                Long tournamentId = match.getStandardTournamentId();
                Integer matchStatus = null;
                if (!CollectionUtils.isEmpty(matchMap))
                    matchStatus = matchMap.get(match.getMatchId());

                if (matchStatus == null) {
                    if (!CollectionUtils.isEmpty(tourMap)) {
                        matchStatus = tourMap.get(tournamentId) == null ? 0 : tourMap.get(tournamentId) == 1 ? 1 : 0;
                    } else {
                        matchStatus = 0;
                    }
                } else {
                    matchStatus = matchStatus == 1 ? 1 : 0;
                }
                match.setMatchCollectStatus(matchStatus == 1 ? true : false);
            }
        }
        return matchs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateTrader(ChangePersonLiableVo vo) {
        try {
            String tradeId = String.valueOf(vo.getTradeId());
            String traderName = vo.getTraderName();
            Query query = new Query();
            query.addCriteria(Criteria.where("matchId").is(vo.getMatchId().longValue()));
            if (vo != null) {
                Update update = new Update();
                if (vo.getMatchType() == 0) {
                    update.set("preTraderName", traderName);
                    update.set("preTraderId", tradeId);
                } else if (vo.getMatchType() == 1) {
                    update.set("liveTraderName", traderName);
                    update.set("liveTraderId", tradeId);
                    update.set("traderNum", 1);
                }
                mongoTemplate.updateFirst(query, update, MatchMarketLiveBean.class);
            }
        } catch (Exception e) {
            log.error("::" + CommonUtil.getRequestId(vo.getTradeId(), vo.getMatchId()) + "::updateTrader更新操盘手失败" + e.getMessage(), e);
        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMatchTop(MatchTop matchTop) {
        try {
            Long matchId = matchTop.getMatchId();
            Integer topStatus = matchTop.getTopStatus();
            String traderId = matchTop.getTraderId();
            if (topStatus == 1) {
                matchTop.setTopTime(System.currentTimeMillis());
                Map map = new HashMap<>();
                map.put("matchId", matchId);
                map.put("traderId", traderId);
                mongoService.upsert(map, MONGOMATCHTOP, matchTop);
            } else {
                Query query = new Query(Criteria.where("matchId").is(matchId).and("traderId").is(traderId));
                mongoTemplate.remove(query, MatchTop.class);
            }

        } catch (Exception e) {
            log.error("::" + CommonUtil.getRequestId(matchTop.getTraderId(), matchTop.getMatchId()) + "::updateMatchTop更新赛事置顶失败" + e.getMessage(), e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateWarningSign(Long matchId, Long categoryId) {
        try {
            JSONObject json = new JSONObject();
            json.put("standardMatchId", matchId);
            json.put("marketCategoryId", categoryId);
            json.put("sign", false);
            json.put("linkId", UuidUtils.generateUuid());
            producerSendMessageUtils.sendMessage("MATCH_ODDS_WARNING_RISK", String.valueOf(categoryId), String.valueOf(matchId), json);
        } catch (Exception e) {
            log.error("::" + CommonUtil.getRequestId(matchId, categoryId) + "::updateWarningSign失败" + e.getMessage(), e);
        }
    }

    @Override
    public boolean isOwnTrade(Long matchId, Integer traderId) {
        MatchMarketLiveBean matchInfo = marketCategorySetService.getMatchInfo(null, matchId);
        if (matchInfo != null) {
            String liveTraderId = matchInfo.getLiveTraderId();
            List<Integer> userIds = tradingAssignmentMapper.selectUserId(matchId, 1);
            if (traderId.toString().equals(liveTraderId) || userIds.contains(traderId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Long> queryMyselfMatchs(MarketLiveOddsQueryVo queryVo) {
        Query query = buildMongoQuery(queryVo);
        List<MatchMarketLiveBean> marketLiveBeans = mongoTemplate.find(query, MatchMarketLiveBean.class);
        List<Long> matchIds = new ArrayList<>();
        if (!CollectionUtils.isEmpty(marketLiveBeans)) {
            matchIds = marketLiveBeans.stream().map(map -> map.getMatchId()).collect(Collectors.toList());
        }
        return matchIds;
    }

    @Override
    public PageResult<MatchMarketLiveBean> queryMyselfMatch(MarketLiveOddsQueryVo queryVo) {
        Query query = buildMongoQuery(queryVo);
        PageResult<MatchMarketLiveBean> pageResult = mongoPageHelper.pageQuery(query, MatchMarketLiveBean.class, queryVo.getPageSize(), queryVo.getCurrentPage());
        return pageResult;
    }

    Long otherSetId(MatchMarketVo marketVo, Long sportId) {
        Long set = 0L;
        Long categoryId = marketVo.getMarketCategoryId();
        String addition2 = marketVo.getAddition2();
        if (Arrays.asList(162L, 163L, 164L, 165L, 175L, 176L, 177L, 178L, 179L, 253L, 254L, 184L, 185L, 186L, 187L, 189L,262L,263L,264L,268L).contains(categoryId)) {
            addition2 = marketVo.getAddition2();
        } else if (Arrays.asList(261L,168L, 203L, 256L, 255L).contains(categoryId)) {
            addition2 = marketVo.getAddition1();
        }
        if (StringUtils.isNotBlank(addition2)) {
            set = sportId * 10000 + 1 + new Double(Double.parseDouble(addition2)).longValue();
        }
     /*   if("1".equals(addition2))set = 50002L;
        if("2".equals(addition2))set = 50003L;
        if("3".equals(addition2))set = 50004L;
        if("4".equals(addition2))set = 50005L;
        if("5".equals(addition2))set = 50006L;*/
        return set;
    }

}
