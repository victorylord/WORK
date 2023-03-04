package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.MatchSetEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mongo.MatchMarketVo;
import com.panda.sport.rcs.mongo.MatchSetVo;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.vo.LiveStandardMarketMessageVO;
import com.panda.sport.rcs.trade.vo.LiveStandardMatchMarketMessageVO;
import com.panda.sport.rcs.trade.wrapper.CategoryService;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.MatchSetMongoService;
import com.panda.sport.rcs.trade.wrapper.RcsMarketCategorySetRelationService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.MqConstant.MARKET_ODDS_UPDTAE_SNAPSHOT_TOPIC;
import static com.panda.sport.rcs.constants.MqConstant.MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.trade.wrapper.impl
 * @Description : 盘口状态服务实现类
 * @Author : Paca
 * @Date : 2020-07-17 11:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class MarketStatusServiceImpl implements MarketStatusService {

    @Autowired
    private RcsMarketCategorySetRelationService rcsMarketCategorySetRelationService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private MatchSetMongoService matchSetMongoService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeModeService tradeModeService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    public void updateSnapshotTradeType(MarketStatusUpdateVO marketStatusUpdateVO) {
        marketStatusUpdateVO.updateTradeModeParamCheck();
        log.info("::{}::修改操盘类型开始：{}",marketStatusUpdateVO.getLinkId(), JsonFormatUtils.toJson(marketStatusUpdateVO));

        Long matchId = marketStatusUpdateVO.getMatchId();
        Integer tradeLevel = marketStatusUpdateVO.getTradeLevel();
        Integer tradeType = marketStatusUpdateVO.getTradeType();
        Long categoryId = marketStatusUpdateVO.getCategoryId();
        Long categorySetId = marketStatusUpdateVO.getCategorySetId();
        if (TradeLevelEnum.isPlayLevel(tradeLevel) || TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            // 盘口级别不更新MongoDB
            // 获取运动种类ID
            getSportId(marketStatusUpdateVO);
        }
        // 玩法集、玩法，手动自动切换封盘封盘
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            categoryAutoToManual(matchId, categoryId);
        } else if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            // 通过玩法集ID查询下面所有玩法ID
            List<Long> categoryIdList = rcsMarketCategorySetRelationService.getCategoryIdByCategorySetId(categorySetId);
            categoryIdList.forEach(id -> categoryAutoToManual(matchId, id));
        }

        //插入执行参数
        MatchSetVo matchSetVo = new MatchSetVo();
        matchSetVo.setJsonParams(JsonFormatUtils.toJson(marketStatusUpdateVO));
        if (null != categorySetId)
            matchSetVo.setCategorySetId(categorySetId);
        if (null != categoryId)
            matchSetVo.setCategoryId(categoryId);
        if (null != marketStatusUpdateVO.getMarketId())
            matchSetVo.setMarketId(Long.parseLong(marketStatusUpdateVO.getMarketId()));
        matchSetVo.setMatchId(matchId);
        matchSetVo.setMethodNo(MatchSetEnum.UPDATE_MARKET_TRADETYPE.getCode());
        matchSetVo.setTradeLevel(marketStatusUpdateVO.getTradeLevel());
        matchSetVo.setParamValue(String.valueOf(tradeType));
        matchSetVo.setUpdateTime(DateUtils.getCurrentTime());
        matchSetMongoService.upsertMatchSetMongo(matchSetVo);

        // 发送MQ，推送WS到前端
        pushSnapWS(marketStatusUpdateVO);

        List<Long> longs = new ArrayList<>();
        if (TraderLevelEnum.STATE.getLevel().equals(marketStatusUpdateVO.getTradeLevel())) {
            longs = categoryService.categoryIds(marketStatusUpdateVO.getSportId(), categorySetId);
            if (!CollectionUtils.isEmpty(longs)) {
                longs.stream().forEach(model -> {
                    matchSetVo.setTradeLevel(TraderLevelEnum.PLAY.getLevel());
                    matchSetVo.setCategoryId(model);
                    matchSetVo.setCategorySetId(null);
                    matchSetVo.setJsonParams(null);
                    matchSetMongoService.upsertMatchSetMongo(matchSetVo);
                });
            }
        } else {
            longs.add(categoryId);
        }
        if (!CollectionUtils.isEmpty(longs)) {
            matchSetVo.setCategorySetId(null);
            matchSetVo.setCategoryId(null);
            matchSetVo.setJsonParams(null);
            matchSetVo.setTradeLevel(TraderLevelEnum.MATCH.getLevel());
            List<Integer> matchDataSource = getMatchDataSource(matchId, longs, tradeType);
            matchSetVo.setParamValue(JSONObject.toJSONString(matchDataSource));
            matchSetMongoService.upsertMatchSetMongo(matchSetVo);
        }

    }

    /**
     * 赛前 玩法下开的盘口变成封，关、封、锁的盘口保留原样
     *
     * @param matchId
     * @param categoryId
     */
    private void categoryAutoToManual(Long matchId, Long categoryId) {
        Map<Integer, Integer> snapshotMarketPlaceStatusMap = getSnapshotMarketPlaceStatus(matchId, categoryId);
        Map<String, String> hash = Maps.newHashMap();
        // 盘口位置，最多10个位置
        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
            Integer status = snapshotMarketPlaceStatusMap.getOrDefault(placeNum, TradeStatusEnum.SEAL.getStatus());
            if (TradeStatusEnum.isOpen(status)) {
                // 开的盘口变成封，关，封，锁的盘口保留原样
                status = TradeStatusEnum.SEAL.getStatus();
            }
            hash.put(String.valueOf(placeNum), String.valueOf(status));
        }
        String key = RedisKey.getSnapshotStatusConfigKey(matchId, categoryId);
        redisUtils.hmset(key, hash);
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
    }

    @Override
    public void updatSnapshotStatus(MarketStatusUpdateVO marketStatusUpdateVO) {
        marketStatusUpdateVO.updateStatusParamCheck();
        log.info("::{}::修改盘口状态开始：{}",marketStatusUpdateVO.getLinkId(), JsonFormatUtils.toJson(marketStatusUpdateVO));
        Integer tradeLevel = marketStatusUpdateVO.getTradeLevel();
        MatchSetVo matchSetVo = new MatchSetVo();
        if (null != marketStatusUpdateVO.getCategorySetId())
            matchSetVo.setCategorySetId(marketStatusUpdateVO.getCategorySetId());
        if (null != marketStatusUpdateVO.getCategoryId())
            matchSetVo.setCategoryId(marketStatusUpdateVO.getCategoryId());
        if (null != marketStatusUpdateVO.getMarketPlaceNum())
            matchSetVo.setMarketPlaceNum(marketStatusUpdateVO.getMarketPlaceNum());
        if (CollectionUtils.isNotEmpty(marketStatusUpdateVO.getCategoryIdList())) {
            matchSetVo.setCategoryIdList(marketStatusUpdateVO.getCategoryIdList());
        }
        matchSetVo.setJsonParams(JsonFormatUtils.toJson(marketStatusUpdateVO));
        matchSetVo.setMatchId(marketStatusUpdateVO.getMatchId());
        matchSetVo.setMethodNo(MatchSetEnum.UPDTAE_MARKET_STATUS.getCode());
        matchSetVo.setTradeLevel(marketStatusUpdateVO.getTradeLevel());
        matchSetVo.setParamValue(String.valueOf(marketStatusUpdateVO.getMarketStatus()));
        matchSetVo.setUpdateTime(DateUtils.getCurrentTime());
        matchSetMongoService.upsertMatchSetMongo(matchSetVo);

        // 状态缓存redis
        snapshotStatusCache(marketStatusUpdateVO);
        // 发送MQ，推送WS到前端
        if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            marketStatusUpdateVO.getCategoryIdList().forEach(playId -> {
                marketStatusUpdateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
                marketStatusUpdateVO.setCategoryId(playId);
                pushSnapWS(marketStatusUpdateVO);
            });
        } else {
            pushSnapWS(marketStatusUpdateVO);
        }
    }

    private void snapshotStatusCache(MarketStatusUpdateVO marketStatusUpdateVO) {
        Integer tradeLevel = marketStatusUpdateVO.getTradeLevel();
        Long matchId = marketStatusUpdateVO.getMatchId();
        Long categoryId = marketStatusUpdateVO.getCategoryId();
        Long categorySetId = marketStatusUpdateVO.getCategorySetId();
        Integer marketPlaceNum = marketStatusUpdateVO.getMarketPlaceNum();
        Integer marketStatus = marketStatusUpdateVO.getMarketStatus();
        String key = RedisKey.getSnapshotStatusConfigKey(matchId, categoryId);
        if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            redisUtils.hset(key, marketPlaceNum.toString(), marketStatus.toString());
            redisUtils.expire(key, 180L, TimeUnit.DAYS);
        } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            MatchSetVo categoryConfig = new MatchSetVo();
            categoryConfig.setParamValue(marketStatus.toString());
            categoryConfig.setUpdateTime(DateUtils.getCurrentTime());
            handleCategoryStatus(matchId, categoryId, categoryConfig, null);
        } else if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            MatchSetVo categorySetConfig = new MatchSetVo();
            categorySetConfig.setParamValue(marketStatus.toString());
            categorySetConfig.setUpdateTime(DateUtils.getCurrentTime());
            handleCategorySetStatus(matchId, categorySetId, categorySetConfig);
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            marketStatusUpdateVO.getCategoryIdList().forEach(playId -> {
                MatchSetVo categoryConfig = new MatchSetVo();
                categoryConfig.setParamValue(marketStatus.toString());
                categoryConfig.setUpdateTime(DateUtils.getCurrentTime());
                handleCategoryStatus(matchId, playId, categoryConfig, null);
            });
        }
    }

    private void handleCategorySetStatus(Long matchId, Long categorySetId, MatchSetVo categorySetConfig) {
        // 通过玩法集ID查询所有下级玩法ID
        List<Long> categoryIdList = rcsMarketCategorySetRelationService.getCategoryIdByCategorySetId(categorySetId);
        if (CollectionUtils.isEmpty(categoryIdList)) {
            log.warn("::{}::玩法集下无玩法：categorySetId={}", matchId, categorySetId);
            return;
        }
        // key=玩法ID,value=status
        Map<Long, MatchSetVo> categoryStatusMap = getCategoryLatestStatusMap(matchId);
        boolean isNotEmpty = !CollectionUtils.isEmpty(categoryStatusMap);

        for (Long categoryId : categoryIdList) {
            MatchSetVo categoryConfig = null;
            if (isNotEmpty && categoryStatusMap.containsKey(categoryId)) {
                categoryConfig = categoryStatusMap.get(categoryId);
            }
            handleCategoryStatus(matchId, categoryId, categoryConfig, categorySetConfig);
        }
    }

    private void handleCategoryStatus(Long matchId, Long categoryId, MatchSetVo categoryConfig, MatchSetVo categorySetConfig) {
        if (categoryConfig == null) {
            categoryConfig = new MatchSetVo();
            categoryConfig.setParamValue(TradeStatusEnum.OPEN.getStatus() + "");
            categoryConfig.setUpdateTime("2019-01-01 00:00:00");
        }
        if (categorySetConfig == null) {
            categorySetConfig = new MatchSetVo();
            categorySetConfig.setParamValue(TradeStatusEnum.OPEN.getStatus() + "");
            categorySetConfig.setUpdateTime("2018-01-01 00:00:00");
        }
        String key = RedisKey.getSnapshotStatusConfigKey(matchId, categoryId);
        // 查询玩法下所有盘口位置最新状态配置
        Map<Integer, MatchSetVo> marketPlaceConfigMap = getMarketPlaceLatestStatusMap(matchId, categoryId);
        Map<String, String> map = Maps.newHashMap();
        // 盘口位置，最多10个位置
        for (int placeNum = 1; placeNum <= RcsConstant.DEFAULT_MARKET_PLACE_AMOUNT; placeNum++) {
            MatchSetVo marketPlaceConfig;
            if (marketPlaceConfigMap.containsKey(placeNum)) {
                marketPlaceConfig = marketPlaceConfigMap.get(placeNum);
            } else {
                marketPlaceConfig = new MatchSetVo();
                marketPlaceConfig.setParamValue(TradeStatusEnum.OPEN.getStatus() + "");
                marketPlaceConfig.setUpdateTime("2020-01-01 00:00:00");
            }
            String marketPlaceStatus = getMarketPlaceStatusOfRcs(marketPlaceConfig, categoryConfig, categorySetConfig);
            map.put(placeNum + "", marketPlaceStatus);
        }
        redisUtils.hmset(key, map);
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
    }

    private String getMarketPlaceStatusOfRcs(final MatchSetVo marketPlaceConfig, final MatchSetVo categoryConfig, final MatchSetVo categorySetConfig) {
        final String marketPlaceStatus = marketPlaceConfig.getParamValue();
        final String categoryStatus = categoryConfig.getParamValue();
        final String categorySetStatus = categorySetConfig.getParamValue();
        final String marketPlaceConfigTime = marketPlaceConfig.getUpdateTime();
        final String categoryConfigTime = categoryConfig.getUpdateTime();
        final String categorySetConfigTime = categorySetConfig.getUpdateTime();
        if (!TradeStatusEnum.isOpen(CommonUtils.toInteger(marketPlaceStatus, TradeStatusEnum.OPEN.getStatus()))) {
            // 盘口位置状态 非开
            return marketPlaceStatus;
        }
        if (marketPlaceConfigTime.compareTo(categoryConfigTime) > 0 &&
                marketPlaceConfigTime.compareTo(categorySetConfigTime) > 0) {
            // 盘口位置状态最后操作
            return marketPlaceStatus;
        }
        if (categorySetConfigTime.compareTo(marketPlaceConfigTime) > 0 &&
                marketPlaceConfigTime.compareTo(categoryConfigTime) > 0) {
            // 先操作玩法状态，再操作盘口位置状态 开，最后操作玩法集状态，取玩法集状态
            return categorySetStatus;
        }
        if (categorySetConfigTime.compareTo(categoryConfigTime) > 0 &&
                categoryConfigTime.compareTo(marketPlaceConfigTime) > 0 &&
                TradeStatusEnum.isOpen(CommonUtils.toInteger(categoryStatus, TradeStatusEnum.OPEN.getStatus()))) {
            // 先操作盘口位置状态 开，再操作玩法状态 开，最后操作玩法集状态，取玩法集状态
            return categorySetStatus;
        } else {
            return categoryStatus;
        }
    }

    private Long getSportId(MarketStatusUpdateVO updateVO) {
        Long matchId = updateVO.getMatchId();
        if (updateVO.getSportId() == null || updateVO.getMatchType() == null || updateVO.getIsMts() == null) {
            StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
            if (matchInfo == null) {
                throw new RcsServiceException("赛事不存在：" + matchId);
            }
            updateVO.setSportId(matchInfo.getSportId());
            updateVO.setIsMts(RcsConstant.isMts(matchInfo));
            updateVO.setMatchType(RcsConstant.getMatchType(matchInfo));
        }
        return updateVO.getSportId();
    }

    /**
     * 发送MQ，推送WS到前端
     *
     * @param marketStatusUpdateVO
     */
    private void pushSnapWS(MarketStatusUpdateVO marketStatusUpdateVO) {
        Integer tradeLevel = marketStatusUpdateVO.getTradeLevel();
        Long matchId = marketStatusUpdateVO.getMatchId();
        Integer tradeType = marketStatusUpdateVO.getTradeType();
        Integer marketStatus = marketStatusUpdateVO.getMarketStatus();
        MatchStatusAndDataSuorceVo mqVO = new MatchStatusAndDataSuorceVo()
                .setMatchId(matchId)
                .setLevel(tradeLevel)
                .setDataSource(tradeType)
                .setStatus(marketStatus);  // TODO 状态和消息队列需要对应

        if (null != marketStatusUpdateVO.getMarketStatus()) {
            sendStatusMq(marketStatusUpdateVO, mqVO);
        } else if (null != marketStatusUpdateVO.getTradeType()) {
            List<Long> longs = new ArrayList<>();
            if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
                longs = categoryService.categoryIds(marketStatusUpdateVO.getSportId(), marketStatusUpdateVO.getCategorySetId());
                mqVO.setCategoryIdList(longs);
                mqVO.setId(String.valueOf(marketStatusUpdateVO.getCategorySetId()));
                producerSendMessageUtils.sendMessage(MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC, mqVO);
            } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
                mqVO.setId(String.valueOf(marketStatusUpdateVO.getCategoryId()));
                longs.add(marketStatusUpdateVO.getCategoryId());
            }
            List<Integer> matchDataSource = getMatchDataSource(matchId, longs, marketStatusUpdateVO.getTradeType());
            mqVO.setMatchDataSource(matchDataSource);
            producerSendMessageUtils.sendMessage(MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC, mqVO);

            // 手动自动切换封盘
//            if(TradeTypeEnum.MANUAD.getCode().equals(marketStatusUpdateVO.getTradeType())){
//            }
            marketStatusUpdateVO.setTradeType(null);
            marketStatusUpdateVO.setMarketStatus(TradeStatusEnum.SEAL.getStatus());
            sendStatusMq(marketStatusUpdateVO, mqVO);
        }

        log.info("::{}::赛前十五分钟——修改操盘类型或者盘口状态，推送WS到前端完成：{}",marketStatusUpdateVO.getLinkId(), JsonFormatUtils.toJson(mqVO));
    }

    void sendCategoryStatus(LiveStandardMatchMarketMessageVO marketMessageVO, MarketCategory marketCategory, Integer status) {
        List<MatchMarketVo> matchMarketVoList = marketCategory.getMatchMarketVoList();
        Long matchId = Long.parseLong(marketCategory.getMatchId());
        Map<Integer, Integer> snapshotMarketPlaceStatus = getSnapshotMarketPlaceStatus(matchId, marketCategory.getId());
        if (!CollectionUtils.isEmpty(matchMarketVoList)) {
            matchMarketVoList.stream().forEach(marketVo -> {
                Integer placeNum = marketVo.getPlaceNum();
                LiveStandardMarketMessageVO messageVO = new LiveStandardMarketMessageVO();
                messageVO.setId(marketVo.getId());
                messageVO.setPlaceNumStatus(marketVo.getStatus());
                messageVO.setOddsMetric(marketVo.getPlaceNum());
                messageVO.setMarketCategoryId(marketCategory.getId());
                messageVO.setStandardMatchInfoId(Long.parseLong(marketCategory.getMatchId()));
                messageVO.setThirdMarketSourceStatus(marketVo.getStatus());
                if (!CollectionUtils.isEmpty(snapshotMarketPlaceStatus) && snapshotMarketPlaceStatus.containsKey(placeNum)) {
                    messageVO.setPlaceNumStatus(snapshotMarketPlaceStatus.get(placeNum));
                }
                marketMessageVO.getMarketList().add(messageVO);
            });
        }

    }

    void sendStatusMq(MarketStatusUpdateVO vo, MatchStatusAndDataSuorceVo mqVO) {
        Integer tradeLevel = vo.getTradeLevel();
        LiveStandardMatchMarketMessageVO marketMessageVO = new LiveStandardMatchMarketMessageVO();
        marketMessageVO.setStandardMatchInfoId(vo.getMatchId());
        marketMessageVO.setModifyTime(System.currentTimeMillis());
        marketMessageVO.setStatus(vo.getMarketStatus());
        marketMessageVO.setMarketList(new ArrayList<LiveStandardMarketMessageVO>());
        if (TradeLevelEnum.isMatchLevel(tradeLevel)) {
            producerSendMessageUtils.sendMessage(MATCH_SNAPSHOT_MARKET_UPDATE_TOPIC, mqVO);
        } else if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            List<MarketCategory> marketCategoryList = getMarketCategoryList(null, vo.getMatchId(), vo.getCategorySetId());
            if (CollectionUtils.isEmpty(marketCategoryList)) {
                marketCategoryList.stream().forEach(marketCategory -> {
                    sendCategoryStatus(marketMessageVO, marketCategory, vo.getMarketStatus());
                });
            }
        } else if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            MarketCategory one = mongoTemplate.findOne(new Query().addCriteria(Criteria.where("matchId").is(String.valueOf(vo.getMatchId())).and("id").is(vo.getCategoryId())), MarketCategory.class);
            if (one != null) {
                sendCategoryStatus(marketMessageVO, one, vo.getMarketStatus());
            }
        } else if (TradeLevelEnum.isMarketLevel(tradeLevel)) {
            LiveStandardMarketMessageVO messageVO = new LiveStandardMarketMessageVO();
            //messageVO.setId(Long.parseLong(vo.getMarketId()));
            messageVO.setPlaceNumStatus(vo.getMarketStatus());
            messageVO.setOddsMetric(vo.getMarketPlaceNum());
            messageVO.setPlaceNum(vo.getMarketPlaceNum());
            messageVO.setMarketCategoryId(vo.getCategoryId());
            messageVO.setStandardMatchInfoId(vo.getMatchId());
            messageVO.setThirdMarketSourceStatus(TradeStatusEnum.OPEN.getStatus());
            marketMessageVO.getMarketList().add(messageVO);
        }
        if (!TradeLevelEnum.isMatchLevel(tradeLevel)) {
            producerSendMessageUtils.sendMessage(MARKET_ODDS_UPDTAE_SNAPSHOT_TOPIC, String.valueOf(tradeLevel), String.valueOf(vo.getMatchId()), JsonFormatUtils.toJson(marketMessageVO));
        }
    }

    private List<MarketCategory> getMarketCategoryList(final Long sportId, final Long matchId, final Long categorySetId) {
        Criteria criteria = Criteria.where("matchId").is(matchId.toString());
        if (categorySetId != null && categorySetId > 0) {
            List<Long> categoryIds = rcsMarketCategorySetRelationService.getCategoryIdByCategorySetId(categorySetId);
            criteria.and("id").in(categoryIds);
        }
        Query query = new Query().addCriteria(criteria);
        return mongoTemplate.find(query, MarketCategory.class);
    }

    List<Integer> getMatchDataSource(Long matchId, List<Long> longs, Integer tradeType) {
        List<Integer> matchDataSource = new ArrayList<>();
        List<MarketCategory> marketCategories = matchSetMongoService.getCategoriesTradeType(matchId, 1);
        if (!CollectionUtils.isEmpty(marketCategories)) {
            Integer autoCount = 0;
            Integer manualCount = 0;
            Integer autoAddCount = 0;
            for (MarketCategory bean : marketCategories) {
                if (longs.contains(bean.getId()))
                    bean.setTradeType(tradeType);
                if (null == bean.getTradeType())
                    bean.setTradeType(0);
                if (TradeEnum.AUTO.getCode().equals(bean.getTradeType())) {
                    autoCount++;
                } else if (TradeEnum.MANUAD.getCode().equals(bean.getTradeType())) {
                    manualCount++;
                } else if (TradeEnum.AUTOADD.getCode().equals(bean.getTradeType())) {
                    autoAddCount++;
                }
            }
            if (autoCount > 0) matchDataSource.add(TradeEnum.AUTO.getCode());
            if (manualCount > 0) matchDataSource.add(TradeEnum.MANUAD.getCode());
            if (autoAddCount > 0) matchDataSource.add(TradeEnum.AUTOADD.getCode());
        }
        return matchDataSource;
    }

    private Map<Integer, MatchSetVo> getMarketPlaceLatestStatusMap(Long matchId, Long categoryId) {
        Map<Integer, MatchSetVo> result = Maps.newHashMap();
        Criteria criteria = Criteria.where("matchId").is(matchId)
                .and("categoryId").is(categoryId)
                .and("tradeLevel").is(TraderLevelEnum.MARKET.getLevel())
                .and("methodNo").is(MatchSetEnum.UPDTAE_MARKET_STATUS.getCode());
        List<MatchSetVo> snapshotConfigList = mongoTemplate.find(new Query().addCriteria(criteria), MatchSetVo.class);
        if (CollectionUtils.isEmpty(snapshotConfigList)) {
            return result;
        }
        Map<Integer, List<MatchSetVo>> listMap = snapshotConfigList.stream().collect(Collectors.groupingBy(MatchSetVo::getMarketPlaceNum));
        listMap.forEach((key, value) -> {
            // 取最新的配置
            MatchSetVo config = value.stream().max(Comparator.comparing(MatchSetVo::getUpdateTime)).get();
            result.put(key, config);
        });
        return result;
    }

    private Map<Long, MatchSetVo> getCategoryLatestStatusMap(Long matchId) {
        Map<Long, MatchSetVo> result = Maps.newHashMap();
        Criteria criteria = Criteria.where("matchId").is(matchId)
                .and("tradeLevel").is(TraderLevelEnum.PLAY.getLevel())
                .and("methodNo").is(MatchSetEnum.UPDTAE_MARKET_STATUS.getCode());
        List<MatchSetVo> snapshotConfigList = mongoTemplate.find(new Query().addCriteria(criteria), MatchSetVo.class);
        if (CollectionUtils.isEmpty(snapshotConfigList)) {
            return result;
        }
        Map<Long, List<MatchSetVo>> listMap = snapshotConfigList.stream().collect(Collectors.groupingBy(MatchSetVo::getCategoryId));
        listMap.forEach((key, value) -> {
            // 取最新的配置
            MatchSetVo config = value.stream().max(Comparator.comparing(MatchSetVo::getUpdateTime)).get();
            result.put(key, config);
        });
        return result;
    }

    @Override
    public Map<Integer, Integer> getSnapshotMarketPlaceStatus(Long matchId, Long categoryId) {
        String key = RedisKey.getSnapshotStatusConfigKey(matchId, categoryId);
        Map<String, String> map = redisUtils.hgetAll(key);
        if (CollectionUtils.isEmpty(map)) {
            return Maps.newHashMap();
        }
        Map<Integer, Integer> marketPlaceStatusMap = Maps.newHashMap();
        map.forEach((k, v) -> marketPlaceStatusMap.put(CommonUtils.toInteger(k, 0), CommonUtils.toInteger(v, TradeStatusEnum.OPEN.getStatus())));
        return marketPlaceStatusMap;
    }

}
