package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Maps;
import com.panda.merge.api.IOutrightTradeConfigApi;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.dto.I18nItemDTO;
import com.panda.merge.dto.MarketMarginGapDtlDTO;
import com.panda.merge.dto.OutrightTradeMarketConfigDTO;
import com.panda.merge.dto.OutrightTradeOddsConfigDTO;
import com.panda.merge.dto.OutrightTradeTypeConfigDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.StandardMatchMarketDTO;
import com.panda.merge.dto.TradeMarketMarginGapConfigDTO;
import com.panda.merge.dto.message.StandardMarketMessage;
import com.panda.merge.dto.message.StandardMarketOddsMessage;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.DataSourceTypeEnum;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsMatchMarketMarginConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.mapper.statistics.RcsMatchPlayConfigMapper;
import com.panda.sport.rcs.mapper.sub.RcsMatchMarketConfigSubMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketProbabilityConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.enums.FootBallPlayEnum;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.util.NameExpressionValueUtils;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.trade.wrapper.impl.MatchTradeConfigServiceImpl;
import com.panda.sport.rcs.trade.wrapper.impl.StandardSportMarketServiceImpl;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.OddsValueVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.curator.shaded.com.google.common.collect.Lists;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description //操盘的一些校验
 * @Param
 * @Author sean
 * @Date 2021/1/9
 * @return
 **/
@Service
@Slf4j
public class TradeOddsCommonService {
    @Autowired
    private RedisClient redisClient;
    @AutowiredTradeConstant.REDIS_MATCH_MARKET_ODDS_NEW
    private TradeVerificationService tradeVerificationService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    RcsMatchMarketConfigSubMapper rcsMatchMarketConfigSubMapper;
    @Autowired
    RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    RcsMatchMarketMarginConfigMapper rcsMatchMarketMarginConfigMapper;
    @Autowired
    RcsMatchPlayConfigMapper rcsMatchPlayConfigMapper;
    @Autowired
    StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    StandardSportMarketService standardSportMarketService;
    // 子玩法配置
    private static final String REDIS_MATCH_MARKET_SUB_SECOND_CONFIG = "rcs:redis:match:market:sub:second:config:%s:%s:%s";
    //public static String REDIS_MATCH_MARKET_SUB_CONFIG_PLAY = "rcs:redis:match:market:sub:config:%s:%s";
    // 玩法配置
    private static final String REDIS_MATCH_MARKET_SECOND_CONFIG = "rcs:redis:match:market:second:config:%s:%s";
    /**
     * @return void
     * @Description //计算赔率
     * @Param [oddsList, config]
     * @Author sean
     * @Date 2021/2/4
     **/
    public List<RcsStandardMarketDTO> getMatchPlayOdds(RcsMatchMarketConfig config) {
        log.info("::{}::,,config={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(config));

        String result = redisClient.get(String.format(TradeConstant.REDIS_MATCH_MARKET_ODDS_NEW, config.getPlayId().toString(),config.getMatchId()));
        List<StandardMarketMessage> markets = null;
        if(StringUtils.isNotBlank(result)){
            markets = JSONArray.parseArray(result,StandardMarketMessage.class);
        }
        List<RcsStandardMarketDTO> playAllMarketList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(markets)) {
            for (StandardMarketMessage market : markets) {
                if (market.getThirdMarketSourceStatus() >= 2) {
                    continue;
                }
                market.setOddsMetric(market.getPlaceNum());
                List<StandardMarketOddsMessage> marketOddsList = market.getMarketOddsList();
                if (!org.springframework.util.CollectionUtils.isEmpty(marketOddsList)) {
                    marketOddsList.forEach(e -> {
                        e.setOddsValue(e.getPaOddsValue());
                        e.setNameExpressionValue(NameExpressionValueUtils.getNameExpressionValue(config.getPlayId().intValue(), e.getOddsType(), market.getAddition1()));
                    });
                }
                RcsStandardMarketDTO m = JSONObject.parseObject(JSONObject.toJSONString(market), RcsStandardMarketDTO.class);
                m.setChildStandardCategoryId(market.getChildMarketCategoryId());
                playAllMarketList.add(m);
            }
            log.info("::{}::,odds from redis ,playAllMarketList={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(playAllMarketList));
        }
        if (CollectionUtils.isEmpty(playAllMarketList)) {
            playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
            log.info("::{}::,odds from DB,playAllMarketList={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(playAllMarketList));
        }
        if (CollectionUtils.isNotEmpty(playAllMarketList)) {
            playAllMarketList = playAllMarketList.stream().filter(e -> CollectionUtils.isNotEmpty(e.getMarketOddsList())).sorted(Comparator.comparing(RcsStandardMarketDTO::getPlaceNum)).collect(Collectors.toList());
        }
        return playAllMarketList;
    }

    public List<StandardSportMarketOdds> getMatchMarketOdds(RcsMatchMarketConfig config) {
        List<StandardSportMarketOdds> oddsVoList = Lists.newArrayList();
        List<RcsStandardMarketDTO> playOdds = getMatchPlayOdds(config);
        if (CollectionUtils.isNotEmpty(playOdds)) {
            for (RcsStandardMarketDTO marketDTO : playOdds) {
                if (marketDTO.getId().equalsIgnoreCase(config.getMarketId().toString()) && CollectionUtils.isNotEmpty(marketDTO.getMarketOddsList())) {
                    for (StandardMarketOddsDTO oddsDTO : marketDTO.getMarketOddsList()) {
                        StandardSportMarketOdds odds = JSONObject.parseObject(JSONObject.toJSONString(oddsDTO), StandardSportMarketOdds.class);
                        odds.setAddition1(marketDTO.getAddition1());
                        odds.setOrderOdds(marketDTO.getThirdMarketSourceStatus());
                        odds.setOriginalOddsValue(ObjectUtils.isEmpty(oddsDTO.getOriginalOddsValue()) ? oddsDTO.getOddsValue() : oddsDTO.getOriginalOddsValue());
                    }

                }
            }
        }
        if (CollectionUtils.isEmpty(oddsVoList)) {
            oddsVoList = standardSportMarketOddsMapper.queryMarketInfoAndOdds(config);
        }
        return oddsVoList;
    }

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //子玩法配置
     * @Param [config]
     * @Author sean
     * @Date 2021/12/26
     **/
    public RcsMatchMarketConfig getMatchMarketSubConfig(RcsMatchMarketConfig config) {
        RcsMatchMarketConfig matchMarketConfig = null;
        String marketConfig = redisClient.hGet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), config.getMarketIndex().toString());
        if (StringUtils.isNotBlank(marketConfig)) {
            matchMarketConfig = JSONObject.parseObject(marketConfig, RcsMatchMarketConfig.class);
            clearConfigAttribute(matchMarketConfig);
        }
        if (ObjectUtils.isEmpty(matchMarketConfig) || ObjectUtils.isEmpty(matchMarketConfig.getMargin())) {
            matchMarketConfig = rcsMatchMarketConfigSubMapper.queryMatchMarketConfigSub(config);
        }
        return matchMarketConfig;
    }

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //玩法配置
     * @Param [config]
     * @Author sean
     * @Date 2021/12/26
     **/
    public RcsMatchMarketConfig getMatchMarketConfig(RcsMatchMarketConfig config) {
        RcsMatchMarketConfig matchMarketConfig = null;
        String marketConfig = redisClient.hGet(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, config.getMatchId(), config.getPlayId()), config.getMarketIndex().toString());
        if (StringUtils.isNotBlank(marketConfig)) {
            matchMarketConfig = JSONObject.parseObject(marketConfig, RcsMatchMarketConfig.class);
            clearConfigAttribute(matchMarketConfig);
        }
        if (ObjectUtils.isEmpty(matchMarketConfig) || ObjectUtils.isEmpty(matchMarketConfig.getMargin())) {
            QueryWrapper<RcsMatchMarketConfig> wrapper = new QueryWrapper<>();
            wrapper.lambda().eq(RcsMatchMarketConfig::getMatchId, config.getMatchId());
            wrapper.lambda().eq(RcsMatchMarketConfig::getPlayId, config.getPlayId());
            wrapper.lambda().eq(RcsMatchMarketConfig::getMarketIndex, config.getMarketIndex());
            matchMarketConfig = rcsMatchMarketConfigMapper.selectOne(wrapper);
        }
        return matchMarketConfig;
    }

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //子玩法配置
     * @Param [config]
     * @Author sean
     * @Date 2021/12/26
     **/
    public List<RcsMatchMarketConfigSub> getMatchMarketSubConfigs(RcsMatchMarketConfig config, RcsTournamentTemplatePlayMargain templatePlayMargin) {
        List<RcsMatchMarketConfigSub> list = Lists.newLinkedList();
        Map<String, String> marketConfig = (Map<String, String>) redisClient.hGetAllToObj(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()));
        if (!ObjectUtils.isEmpty(marketConfig)) {
            for (String key : marketConfig.keySet()) {
                list.add(JSONObject.parseObject(marketConfig.get(key), RcsMatchMarketConfigSub.class));
            }
        }
        if (CollectionUtils.isEmpty(list) || list.size() < templatePlayMargin.getMarketCount()) {
            list = rcsMatchMarketConfigSubMapper.queryMatchMarketConfigSubList(config);
            if (CollectionUtils.isNotEmpty(list)) {
                for (RcsMatchMarketConfigSub configSub : list) {
                    redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), configSub.getMarketIndex().toString(), JSONObject.toJSONString(configSub));
                }
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), 60 * 10);
            }
        }
        log.info("::{}::,子玩法水差={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(list));
        return list;
    }

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //玩法配置
     * @Param [config]
     * @Author sean
     * @Date 2021/12/26
     **/
    public List<RcsMatchMarketConfig> getMatchMarketConfigs(RcsMatchMarketConfig config, RcsTournamentTemplatePlayMargain templatePlayMargin) {
        List<RcsMatchMarketConfig> listConfig = Lists.newArrayList();
        Map<String, String> marketConfig = (Map<String, String>) redisClient.hGetAllToObj(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, config.getMatchId(), config.getPlayId()));
        if (!ObjectUtils.isEmpty(marketConfig)) {
            for (String key : marketConfig.keySet()) {
                listConfig.add(JSONObject.parseObject(marketConfig.get(key), RcsMatchMarketConfig.class));
            }
        }

        if (CollectionUtils.isEmpty(listConfig) || listConfig.size() < templatePlayMargin.getMarketCount()) {
            QueryWrapper<RcsMatchMarketConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(RcsMatchMarketConfig::getMatchId, config.getMatchId()).eq(RcsMatchMarketConfig::getPlayId, config.getPlayId());
            queryWrapper.lambda().orderByAsc(RcsMatchMarketConfig::getMarketIndex);
            listConfig = rcsMatchMarketConfigMapper.selectList(queryWrapper);
        }
        log.info("::{}::,玩法水差={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()), JSONObject.toJSONString(listConfig));
        return listConfig;
    }

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //玩法配置
     * @Param [config]
     * @Author sean
     * @Date 2021/12/26
     **/
    public RcsMatchMarketMarginConfig getFootballWaterDiff(RcsMatchMarketConfig config) {
        RcsMatchMarketMarginConfig rcsMatchMarketMarginConfig = null;
        if (ObjectUtils.isEmpty(config.getMarketId())) {
            return rcsMatchMarketMarginConfig;
        }
        String marketConfig = redisClient.hGet(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER, config.getMatchId()), config.getMarketId().toString());
        if (StringUtils.isNotBlank(marketConfig)) {
            rcsMatchMarketMarginConfig = JSONObject.parseObject(marketConfig, RcsMatchMarketMarginConfig.class);
        } else {
            QueryWrapper<RcsMatchMarketMarginConfig> queryWrapper = new QueryWrapper();
            queryWrapper.lambda().eq(RcsMatchMarketMarginConfig::getMarketId, config.getMarketId());
            rcsMatchMarketMarginConfig = rcsMatchMarketMarginConfigMapper.selectOne(queryWrapper);
        }
        return rcsMatchMarketMarginConfig;
    }

    /**
     * @return void
     * @Description //更新水差
     * @Param [config]
     * @Author sean
     * @Date 2021/12/26
     **/
    public void updateRedisWater(RcsMatchMarketConfig config) {
        if (SportIdEnum.isFootball(config.getSportId())) {
            if (!ObjectUtils.isEmpty(config.getMarketId()) && MarketUtils.isAuto(config.getDataSource().intValue())) {
                rcsMatchMarketConfigMapper.insertOrUpdateMarketMarginConfig(config);
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER, config.getMatchId()), config.getMarketId().toString(), JSONObject.toJSONString(config));
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER, config.getMatchId()), 60 * 10);
            }
        } else if (SportIdEnum.isBasketball(config.getSportId())) {
            if (!TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue())) {
                rcsMatchMarketConfigMapper.updatePlaceConfig(config);
                RcsMatchMarketConfig marketConfig = getMatchMarketConfig(config);
                marketConfig.setAwayAutoChangeRate(config.getAwayAutoChangeRate());
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, config.getMatchId(), config.getPlayId()), config.getMarketIndex().toString(), JSONObject.toJSONString(marketConfig));
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, config.getMatchId(), config.getPlayId()), 60 * 10);
            }
        } else {
            rcsMatchMarketConfigMapper.updateMatchMarketSubWater(config);
            RcsMatchMarketConfig marketConfig = getMatchMarketSubConfig(config);
            marketConfig.setAwayAutoChangeRate(config.getAwayAutoChangeRate());
            redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), config.getMarketIndex().toString(), JSONObject.toJSONString(marketConfig));
            redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), 60 * 10);
        }
    }

    public void updateMarketConfig(RcsMatchMarketConfig conf) {
        if (SportIdEnum.isFootball(conf.getSportId())) {
            if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(conf.getPlayId().intValue())) {
                rcsMatchMarketConfigSubMapper.insertOrUpdateMarketConfig(conf);
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), 60 * 10);
            } else {
                rcsMatchMarketConfigMapper.insertOrUpdateMarketConfig(conf);
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, conf.getMatchId(), conf.getPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, conf.getMatchId(), conf.getPlayId()), 60 * 10);
            }
        } else if (SportIdEnum.isBasketball(conf.getSportId())) {
            if (TradeConstant.BASKETBALL_X_PLAYS.contains(conf.getPlayId().intValue())) {
                rcsMatchMarketConfigSubMapper.insertOrUpdateMarketConfig(conf);
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), 60 * 10);
                this.setRedisValue(conf);
            } else {
                rcsMatchMarketConfigMapper.insertOrUpdateMarketConfig(conf);
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, conf.getMatchId(), conf.getPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
                redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, conf.getMatchId(), conf.getPlayId()), 60 * 10);
                redisClient.hSet(String.format(REDIS_MATCH_MARKET_SECOND_CONFIG, conf.getMatchId(), conf.getPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
                redisClient.expireKey(String.format(REDIS_MATCH_MARKET_SECOND_CONFIG, conf.getMatchId(), conf.getPlayId()), 60 * 60 * 24 * 2);
            }
        }
        else {
            rcsMatchMarketConfigSubMapper.insertOrUpdateMarketConfig(conf);
            redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
            redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), 60 * 10);
            this.setRedisValue(conf);
        }
    }

    private void setRedisValue(RcsMatchMarketConfig conf) {
        redisClient.hSet(String.format(REDIS_MATCH_MARKET_SUB_SECOND_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), conf.getMarketIndex().toString(), JSONObject.toJSONString(conf));
        redisClient.expireKey(String.format(REDIS_MATCH_MARKET_SUB_SECOND_CONFIG, conf.getMatchId(), conf.getPlayId(), conf.getSubPlayId()), 60 * 60 * 24 * 2);
    }

    public void updateMatchMarketWaters(RcsMatchMarketConfig config, List<MatchMarketPlaceConfig> placeConfigs) {
        if ((SportIdEnum.isBasketball(config.getSportId()) && (!TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue())))) {
            rcsMatchMarketConfigMapper.updateMatchMarketWaters(config, placeConfigs);
            for (MatchMarketPlaceConfig placeConfig : placeConfigs) {
                config.setMarketIndex(placeConfig.getPlaceNum());
                RcsMatchMarketConfig marketConfig = getMatchMarketConfig(config);
                marketConfig.setAwayAutoChangeRate(placeConfig.getPlaceMarketDiff().toPlainString());
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, config.getMatchId(), config.getPlayId()), config.getMarketIndex().toString(), JSONObject.toJSONString(marketConfig));
            }
            redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG, config.getMatchId(), config.getPlayId()), 60 * 10);
        } else {
            rcsMatchMarketConfigMapper.updateMatchMarketSubWaters(config, placeConfigs);
            for (MatchMarketPlaceConfig placeConfig : placeConfigs) {
                config.setMarketIndex(placeConfig.getPlaceNum());
                RcsMatchMarketConfig marketConfig = getMatchMarketSubConfig(config);
                marketConfig.setAwayAutoChangeRate(placeConfig.getPlaceMarketDiff().toPlainString());
                redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), config.getMarketIndex().toString(), JSONObject.toJSONString(marketConfig));
            }
            redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), 60 * 10);
        }
    }

    public void updateMatchMarketHeadGap(RcsMatchMarketConfig config) {
        rcsMatchPlayConfigMapper.insertOrUpdateMarketHeadGap(config);
        redisClient.hSet(String.format(TradeConstant.REDIS_MATCH_MARKET_HEAD_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), config.getMarketIndex().toString(), JSONObject.toJSONString(config));
        redisClient.expireKey(String.format(TradeConstant.REDIS_MATCH_MARKET_HEAD_CONFIG, config.getMatchId(), config.getPlayId(), config.getSubPlayId()), 60 * 10);

    }

    public StandardSportMarket queryMarketInfo(RcsMatchMarketConfig config) {
        List<RcsStandardMarketDTO> playOdds = getMatchPlayOdds(config);
        StandardSportMarket market = null;
        if (CollectionUtils.isNotEmpty(playOdds)) {
            for (RcsStandardMarketDTO marketDTO : playOdds) {
                if (marketDTO.getPlaceNum() == config.getMarketIndex().intValue() && marketDTO.getChildStandardCategoryId().toString().equalsIgnoreCase(config.getSubPlayId())) {
                    market = JSONObject.parseObject(JSONObject.toJSONString(marketDTO), StandardSportMarket.class);
                    break;
                }
            }
        } else {
            market = standardSportMarketService.selectMainMarketInfo(config.getMatchId(), config.getPlayId(), config.getSubPlayId());
        }
        return market;
    }

    /**
     * @return void
     * @Description //清除后台填充的字段
     * @Param [config]
     * @Author sean
     * @Date 2022/1/22
     **/
    static void clearConfigAttribute(RcsMatchMarketConfig config) {
        config.setAwayMarketValue(null);
        config.setHomeMarketValue(null);
        config.setOddsList(null);
        config.setBuildConfig(null);
        config.setMarketBuildFlag(false);
    }
}
