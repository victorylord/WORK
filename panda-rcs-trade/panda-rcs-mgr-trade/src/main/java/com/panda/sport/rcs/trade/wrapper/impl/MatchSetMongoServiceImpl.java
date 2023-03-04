package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.common.NumberUtils;
import com.panda.sport.rcs.enums.MatchSetEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.mongo.*;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.MatchSetMongoService;
import com.panda.sport.rcs.trade.wrapper.MongoService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.trade.wrapper.impl
 * @Description : 盘口状态服务实现类
 * @Author : Enzo
 * @Date : 2020-08-08 11:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class MatchSetMongoServiceImpl implements MatchSetMongoService {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private MongoService mongoService;

    @Autowired
    private MarketStatusService marketStatusService;

    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;

    @Override
    public void matchLevelSnap(MatchMarketLiveBean match, Map<Long, List<MatchSetVo>> map) {
        if (CollectionUtils.isEmpty(map)) return;
        List<MatchSetVo> matchSetVos = map.get(match.getMatchId());
        if (CollectionUtils.isEmpty(matchSetVos)) return;
        for (MatchSetVo matchSetVo : matchSetVos) {
            if (MatchSetEnum.UPDTAE_MARKET_STATUS.getCode().equals(matchSetVo.getMethodNo()))
                match.setOperateMatchStatus(Integer.parseInt(matchSetVo.getParamValue()));
            if (MatchSetEnum.UPDATE_MARKET_TRADETYPE.getCode().equals(matchSetVo.getMethodNo()))
                match.setTradeType(JSONObject.parseArray(matchSetVo.getParamValue(), Integer.class));
            if (MatchSetEnum.UPDATE_RISKMANAGER_CODE.getCode().equals(matchSetVo.getMethodNo()))
                match.setRiskManagerCode(matchSetVo.getParamValue());
        }

    }

    @Override
    public void categoryLevelSnap(MarketCategory category, Map<Long, List<MatchSetVo>> map) {
        if (CollectionUtils.isEmpty(map)) return;
        List<MatchSetVo> matchSetVos = map.get(category.getId());
        if (CollectionUtils.isEmpty(matchSetVos)) return;
        for (MatchSetVo matchSetVo : matchSetVos) {
            if (MatchSetEnum.UPDTAE_MARKET_STATUS.getCode().equals(matchSetVo.getMethodNo()))
                category.setStatus(Integer.parseInt(matchSetVo.getParamValue()));
            if (MatchSetEnum.UPDATE_MARKET_TRADETYPE.getCode().equals(matchSetVo.getMethodNo()))
                category.setTradeType(Integer.parseInt(matchSetVo.getParamValue()));
        }
    }

    @Override
    public void marketLevelSnap(MatchMarketVo matchMarketVo, Map<Integer, List<MatchSetVo>> map, Integer marketPlaceNum) {
        if (CollectionUtils.isEmpty(map)) return;
        List<MatchSetVo> matchSetVos = map.get(marketPlaceNum);
        if (CollectionUtils.isEmpty(matchSetVos)) return;
        for (MatchSetVo matchSetVo : matchSetVos) {
            if (MatchSetEnum.UPDTAE_MARKET_STATUS.getCode().equals(matchSetVo.getMethodNo()))
                matchMarketVo.setStatus(Integer.parseInt(matchSetVo.getParamValue()));
        }
    }


    @Override
    public Map<Long, List<MatchSetVo>> queryCategoryLevelSnap(Long matchId) {
        Criteria criteria = Criteria.where("matchId").is(matchId).and("tradeLevel").is(TraderLevelEnum.PLAY.getLevel());
        List<MatchSetVo> matchSetVos = mongoTemplate.find(new Query().addCriteria(criteria), MatchSetVo.class);
        if (CollectionUtils.isEmpty(matchSetVos)) return null;
        Map<Long, List<MatchSetVo>> result = matchSetVos.stream().filter(filter -> null != filter.getCategoryId()).collect(Collectors.groupingBy(setVo -> setVo.getCategoryId()));
        return result;
    }

    @Override
    public Map<Long, List<MatchSetVo>> queryMatchLevelSnap(List<MatchMarketLiveBean> matchs) {
        List<Long> matchIds = matchs.stream().map(map -> map.getMatchId()).collect(Collectors.toList());
        Criteria criteria = Criteria.where("matchId").in(matchIds).and("tradeLevel").is(TraderLevelEnum.MATCH.getLevel());
        List<MatchSetVo> matchSetVos = mongoTemplate.find(new Query().addCriteria(criteria), MatchSetVo.class);
        if (CollectionUtils.isEmpty(matchSetVos)) return null;
        Map<Long, List<MatchSetVo>> result = matchSetVos.stream().filter(filter -> null != filter.getMatchId()).collect(Collectors.groupingBy(setVo -> setVo.getMatchId()));
        return result;
    }

    @Override
    public Map<Integer, List<MatchSetVo>> queryMarketLevelSnap(Long matchId, Long categoryId) {
        Criteria criteria = Criteria.where("matchId").is(matchId).and("categoryId").is(categoryId).and("tradeLevel").is(TraderLevelEnum.MARKET.getLevel());
        List<MatchSetVo> matchSetVos = mongoTemplate.find(new Query().addCriteria(criteria), MatchSetVo.class);
        if (CollectionUtils.isEmpty(matchSetVos)) return null;
        Map<Integer, List<MatchSetVo>> result = matchSetVos.stream().filter(filter -> null != filter.getMarketPlaceNum()).collect(Collectors.groupingBy(setVo -> setVo.getMarketPlaceNum()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateMatchSet(MatchSetVo matchSetVo) {
        try {
            if (matchSetVo == null) return;
            if (MatchSetEnum.UPDTAE_MARKET_STATUS.getCode().equals(matchSetVo.getMethodNo()) ||
                    MatchSetEnum.UPDATE_MARKET_TRADETYPE.getCode().equals(matchSetVo.getMethodNo())) {
                if (TraderLevelEnum.MATCH.getLevel().equals(matchSetVo.getTradeLevel())) {
                    upsertMatchSetMongo(matchSetVo);
                    upsertCategorySetMatchSet(matchSetVo);
                    upsertCategoryMatchSet(matchSetVo);
                } else if (TraderLevelEnum.PLAY.getLevel().equals(matchSetVo.getTradeLevel())) {
                    upsertCategoryMatchSet(matchSetVo);
                } else if (TraderLevelEnum.MARKET.getLevel().equals(matchSetVo.getTradeLevel())) {
                    upsertMatchSetMongo(matchSetVo);
                }
            }
        } catch (Exception e) {
            log.error("更新失败", e);
        }
    }

    @Override
    public void updateMarketConfig(List<MarketConfigMongo> marketConfigMongos, Long matchId, Long categoryId, MatchMarketVo matchMarketVo) {
        if (CollectionUtils.isEmpty(marketConfigMongos) || null == matchId || null == categoryId || null == matchMarketVo)
            return;
        MarketConfigMongo marketConfigMongo = marketConfigMongos.stream().filter(filter -> matchId.equals(filter.getMatchId()) && categoryId.equals(filter.getPlayId()) && String.valueOf(matchMarketVo.getId()).equals(filter.getMarketId())).findFirst().orElse(null);
        if (null == marketConfigMongo) return;
        List<Map<String, Object>> oddsList = marketConfigMongo.getOddsList();
        if(CollectionUtils.isEmpty(oddsList))return;
        Map<String, List<Map<String, Object>>> collect = oddsList.stream().collect(Collectors.groupingBy((e -> e.get("id").toString())));
        //获取配置里面的赔率
        if(CollectionUtils.isEmpty(collect))return;
        matchMarketVo.getOddsFieldsList().stream().forEach(oddsVo -> {
            if (null != collect.get(String.valueOf(oddsVo.getId()))) {
                Map<String, Object> stringObjectMap = collect.get(String.valueOf(oddsVo.getId())).get(0);
                String fieldOddsValue = stringObjectMap.get("fieldOddsValue").toString();
                oddsVo.setFieldOddsValue(fieldOddsValue);
                String nextLevelOdds = rcsOddsConvertMappingService.getNextLevelOdds(new BigDecimal(fieldOddsValue).divide(new BigDecimal("100000"), 2, RoundingMode.DOWN).toPlainString());
                oddsVo.setNextLevelOddsValue(nextLevelOdds);
            }
        });
        if (marketConfigMongo.getHomeAutoChangeRate() != null)
            matchMarketVo.setWaterValue(NumberUtils.getBigDecimal(marketConfigMongo.getHomeAutoChangeRate()).multiply(NumberUtils.getBigDecimal(100)));
        if (marketConfigMongo.getAwayAutoChangeRate() != null)
            matchMarketVo.setWaterValue(NumberUtils.getBigDecimal(marketConfigMongo.getAwayAutoChangeRate()).multiply(NumberUtils.getBigDecimal(100)));
        if (marketConfigMongo.getMargin() != null)
            matchMarketVo.setMarginValue(marketConfigMongo.getMargin());
    }

    @Override
    public List<MarketCategory> getCategoriesTradeType(Long matchId, Integer matchSnapshot) {
        Query query = new Query();
        query.fields().include("matchId");
        query.fields().include("sportId");
        query.fields().include("id");
        query.fields().include("tradeType");
        query.addCriteria(Criteria.where("matchId").is(String.valueOf(matchId)));
        List<MarketCategory> marketCategories = mongoTemplate.find(query, MarketCategory.class);
        if (CollectionUtils.isEmpty(marketCategories)) return null;
        if (matchSnapshot == 1) {
            //获取玩法配置自动手动
            Map<Long, Integer> SnapshotTradeType = getCategorySnapshotTradeType(matchId);
            for (MarketCategory category : marketCategories) {
                if (null == category.getTradeType()) category.setTradeType(0);
                if (!CollectionUtils.isEmpty(SnapshotTradeType)) {
                    category.setTradeType(SnapshotTradeType.get(category.getId()));
                }
            }
        }
        return marketCategories;
    }

    @Override
    public List<MarketConfigMongo> queryMarketConfig(Long matchId) {
        Criteria criteria = Criteria.where("matchId").is(matchId);
        List<MarketConfigMongo> marketConfigMongos = mongoTemplate.find(new Query().addCriteria(criteria), MarketConfigMongo.class);
        return marketConfigMongos;
    }

    @Override
    public void upsertMatchSetMongo(MatchSetVo matchSetVo) {
        Map map = new HashMap();
        map.put("matchId", matchSetVo.getMatchId());
        map.put("methodNo", matchSetVo.getMethodNo());
        map.put("tradeLevel", matchSetVo.getTradeLevel());
        if (matchSetVo.getCategorySetId() != null) map.put("categorySetId", matchSetVo.getCategorySetId());
        if (matchSetVo.getCategoryId() != null) map.put("categoryId", matchSetVo.getCategoryId());
        if (matchSetVo.getMarketPlaceNum() != null) map.put("marketPlaceNum", matchSetVo.getMarketPlaceNum());
        if (matchSetVo.getMarketId() != null) map.put("marketId", matchSetVo.getMarketId());
        mongoService.upsert(map, "match_set", matchSetVo);

    }

    void upsertCategoryMatchSet(MatchSetVo matchSetVo) {
        Criteria criteria = Criteria.where("matchId").is(String.valueOf(matchSetVo.getMatchId()));
        if (null != matchSetVo.getCategoryId()) criteria.and("id").is(matchSetVo.getCategoryId());
        List<MarketCategory> marketCategories = mongoTemplate.find(new Query().addCriteria(criteria), MarketCategory.class);
        if (CollectionUtils.isEmpty(marketCategories)) return;
        for (MarketCategory category : marketCategories) {
            matchSetVo.setCategoryId(category.getId());
            matchSetVo.setTradeLevel(TraderLevelEnum.PLAY.getLevel());
            upsertMatchSetMongo(matchSetVo);
            List<MatchMarketVo> matchMarketVoList = category.getMatchMarketVoList();
            if (CollectionUtils.isEmpty(matchMarketVoList)) continue;
            matchMarketVoList.stream().forEach(matchMarketVo -> {
                matchSetVo.setMarketId(matchMarketVo.getMarketId());
                matchSetVo.setTradeLevel(TraderLevelEnum.MARKET.getLevel());
                upsertMatchSetMongo(matchSetVo);
            });
        }
    }

    void upsertCategorySetMatchSet(MatchSetVo matchSetVo) {
        Criteria criteria = Criteria.where("matchId").is(String.valueOf(matchSetVo.getMatchId()));
        List<CategoryCollection> categoryCollections = mongoTemplate.find(new Query().addCriteria(criteria), CategoryCollection.class);
        for (CategoryCollection categoryCollection : categoryCollections) {
            matchSetVo.setCategorySetId(categoryCollection.getId());
            matchSetVo.setMethodNo(TraderLevelEnum.STATE.getLevel());
            upsertMatchSetMongo(matchSetVo);
        }
    }

    @Override
    public Map<Long, Integer> getCategorySnapshotTradeType(Long matchId) {
        return getSnapshotTradeType(matchId, TraderLevelEnum.PLAY);
    }

    @Override
    public Map<Long, Integer> getCategorySetSnapshotTradeType(Long matchId) {
        return getSnapshotTradeType(matchId, TraderLevelEnum.STATE);
    }

    @Override
    public void upsertMatchCategorySetMongo(MatchCategorySetVo vo) {
        Map map = new HashMap();

        map.put("matchId", vo.getMatchId());
        map.put("sportId", vo.getSportId());
        map.put("categorySetId", vo.getCategorySetId());
        map.put("liveOdds", vo.getLiveOdds());

        mongoService.upsert(map, "match_categorySet", vo);
    }


    private Map<Long, Integer> getSnapshotTradeType(Long matchId, TraderLevelEnum tradeLevelEnum) {
        Map<Long, Integer> result = Maps.newHashMap();
        Criteria criteria = Criteria.where("matchId").is(matchId)
                .and("tradeLevel").is(tradeLevelEnum.getLevel())
                .and("methodNo").is(MatchSetEnum.UPDATE_MARKET_TRADETYPE.getCode());
        List<MatchSetVo> snapshotConfigList = mongoTemplate.find(new Query().addCriteria(criteria), MatchSetVo.class);
        if (CollectionUtils.isEmpty(snapshotConfigList)) {
            return result;
        }
        Map<Long, List<MatchSetVo>> groupMap;
        if (TradeLevelEnum.isPlaySetLevel(tradeLevelEnum.getLevel())) {
            groupMap = snapshotConfigList.stream().collect(Collectors.groupingBy(MatchSetVo::getCategorySetId));
        } else if (TradeLevelEnum.isPlayLevel(tradeLevelEnum.getLevel())) {
            groupMap = snapshotConfigList.stream().collect(Collectors.groupingBy(MatchSetVo::getCategoryId));
        } else {
            return result;
        }
        groupMap.forEach((categoryId, value) -> {
            // 取最新的配置
            MatchSetVo config = value.stream().max(Comparator.comparing(MatchSetVo::getUpdateTime)).get();
            Integer tradeType = Integer.valueOf(config.getParamValue());
            result.put(categoryId, tradeType);
        });
        return result;
    }

}



