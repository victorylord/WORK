package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.TradeMarketStatusConfigDTO;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.ScoreTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.statistics.RcsMatchPlayConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.dto.StandardMarketPlaceDto;
import com.panda.sport.rcs.trade.enums.MatchTypeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailService;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.trade.wrapper.TradeService;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.vo.MarketDisableVO;
import com.panda.sport.rcs.vo.MatchMarketTradeTypeVo;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;
import com.panda.sport.rcs.vo.trade.WaterDiffRelevanceReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcException;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘服务实现类
 * @Author : Paca
 * @Date : 2020-11-06 12:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class TradeServiceImpl implements TradeService {

    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    private MatchStatisticsInfoDetailService matchStatisticsInfoDetailService;

    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsMatchPlayConfigMapper rcsMatchPlayConfigMapper;

    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService tournamentTemplatePlayMarginService;
    @Autowired
    private RcsStandardSportMarketSellService rcsStandardSportMarketSellService;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RedisUtils redisUtils;

    @Override
    public List<StandardSportMarket> marketDisableList(MarketDisableVO vo) {
        Long sportId = vo.getSportId();
        Long matchId = vo.getMatchId();
        Long playId = vo.getPlayId();
        Integer marketType = vo.getMarketType();
        List<StandardSportMarket> list = standardSportMarketService.list(matchId, playId);
        StandardMatchInfo info = standardMatchInfoService.getById(matchId);
        RcsStandardSportMarketSell sellInfo = rcsStandardSportMarketSellService.selectStandardMarketSellVo(matchId);
        if (info != null && sellInfo != null) {
            Map<Long, String> dataSourceMap = tournamentTemplatePlayMarginService.queryDataSource(matchId);
            if (RcsConstant.isLive(info) && StringUtils.isNotBlank(sellInfo.getLiveMatchDataProviderCode())) {
                String liveDataSourceCode = dataSourceMap.getOrDefault(playId, sellInfo.getLiveMatchDataProviderCode());
                // 滚球
                list = list.stream().filter(bean -> StringUtils.equals(liveDataSourceCode, bean.getDataSourceCode()) || "PA".equals(bean.getDataSourceCode())).collect(Collectors.toList());
            } else {
                // 早盘
                String preDataSourceCode = dataSourceMap.getOrDefault(playId, sellInfo.getPreMatchDataProviderCode());
                list = list.stream().filter(bean -> StringUtils.equals(preDataSourceCode, bean.getDataSourceCode()) || "PA".equals(bean.getDataSourceCode())).collect(Collectors.toList());
            }
        }

        if (CollectionUtils.isNotEmpty(list)) {
            if (RcsConstant.HANDICAP.contains(playId)) {
                if (MatchTypeEnum.isLive(marketType) && sportId == 1L) {
                    // 比分M:N，让球/分盘，M - N = A1 - A2
                    MatchStatisticsInfoDetail detail = getScore(sportId, matchId, playId);
                    list = list.stream().filter(market -> (detail.getT1() - detail.getT2()) == (new BigDecimal(market.getAddition1()).subtract(new BigDecimal(market.getAddition2())).doubleValue())).collect(Collectors.toList());
                }
                list = list.stream().sorted(Comparator.comparingDouble(o -> Double.valueOf(o.getAddition1()))).collect(Collectors.toList());
            }
            if (RcsConstant.TOTAL.contains(playId)) {
                if (MatchTypeEnum.isLive(marketType)) {
                    // 比分M:N，大小盘盘口值 A1 > M + N + 0.25
                    MatchStatisticsInfoDetail detail = getScore(sportId, matchId, playId);
                    list = list.stream().filter(market -> Double.parseDouble(market.getAddition1()) > detail.getT1() + detail.getT2() + 0.25).collect(Collectors.toList());
                }
                list = list.stream().sorted((o1, o2) -> Double.valueOf(o2.getAddition1()).compareTo(Double.valueOf(o1.getAddition1()))).collect(Collectors.toList());
            }
            List<Long> marketIdList = list.stream().map(StandardSportMarket::getId).collect(Collectors.toList());
            Map<Long, List<StandardSportMarketOdds>> oddsMap = standardSportMarketOddsService.listAndGroup(marketIdList);

            Map<Long, StandardMarketPlaceDto> statusMap = Maps.newHashMap();
            List<StandardMarketPlaceDto> marketList = standardSportMarketMapper.selectMarketPlaceInfo(matchId, Arrays.asList(playId), null);
            if (CollectionUtils.isNotEmpty(marketList)) {
                statusMap = marketList.stream().collect(Collectors.toMap(StandardMarketPlaceDto::getMarketId, bean -> bean));
            }

            for (StandardSportMarket market : list) {
                Long marketId = market.getId();
                market.setMarketOddsList(oddsMap.get(marketId));
                if (statusMap.containsKey(marketId)) {
                    StandardMarketPlaceDto marketPlace = statusMap.get(marketId);
                    if (marketPlace != null && marketPlace.getPlaceNum() != null && marketPlace.getPlaceNum() == 1) {
                        market.setMainFlag(1);
                    } else {
                        market.setMainFlag(0);
                    }
                }/* else {
                    if (!TradeStatusEnum.isDisable(market.getThirdMarketSourceStatus())) {
                        market.setStatus(TradeStatusEnum.CLOSE.getStatus());
                        market.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
                    }
                }*/
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String marketDisable(MarketDisableVO marketDisableVO) {
        Long matchId = marketDisableVO.getMatchId();
        Long playId = marketDisableVO.getPlayId();
        Long marketId = Long.valueOf(marketDisableVO.getMarketId());
        Integer dataSource = rcsTradeConfigService.getDataSource(matchId, playId);
        if (!TradeEnum.isAuto(dataSource)) {
            throw new RcsServiceException("只有A模式才支持弃用或启用盘口");
        }
        Integer status = TradeStatusEnum.disableFlagConvert(marketDisableVO.getDisableFlag());
        boolean result = standardSportMarketService.updatePaStatus(marketId, status);
        if (!result) {
            log.warn("::{}::盘口弃用修改数据库状态失败，status=" + status,matchId);
        }
        String linkId = CommonUtils.getLinkId("disable_" + status);
        TradeMarketStatusConfigDTO configDTO = new TradeMarketStatusConfigDTO();
        configDTO.setStandardMatchInfoId(matchId);
        configDTO.setStandardCategoryId(playId);
        configDTO.setRelationMarketId(marketId);
        configDTO.setMarketType(marketDisableVO.getMarketType());
        configDTO.setAddtion(marketDisableVO.getMarketValue());
        configDTO.setMarketStatus(status);
        putTradeMarketStatusConfig(linkId, configDTO);
        return linkId;
    }

    private MatchStatisticsInfoDetail getScore(Long sportId, Long matchId, Long playId) {
        ScoreTypeEnum scoreTypeEnum = ScoreTypeEnum.getScoreTypeEnum(sportId, playId);
        if (scoreTypeEnum != null) {
            return matchStatisticsInfoDetailService.getByScoreType(matchId, scoreTypeEnum);
        }
        MatchStatisticsInfoDetail result = new MatchStatisticsInfoDetail();
        result.setT1(0);
        result.setT2(0);
        return result;
    }

    private Response putTradeMarketStatusConfig(String linkId, TradeMarketStatusConfigDTO tradeMarketStatusConfigDTO) {
        try {
            return DataRealtimeApiUtils.handleApi(linkId, tradeMarketStatusConfigDTO, new DataRealtimeApiUtils.ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketConfigApi.putTradeMarketStatusConfig(request);
                }
            });
        } catch (RpcException | RcsServiceException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException("调用接口 ITradeMarketConfigApi.putTradeMarketStatusConfig 出错", e);
        }
    }

    @Override
    public String waterDiffRelevance(WaterDiffRelevanceReqVo reqVo) {
        reqVo.paramCheck();
        Long sportId = reqVo.getSportId();
        Long matchId = reqVo.getMatchId();
        Long playId = reqVo.getPlayId();
        Long subPlayId = reqVo.getSubPlayId();
        Integer relevanceType = reqVo.getRelevanceType();
        String linkId = CommonUtils.getLinkId("waterDiffRelevance");
        String key = RedisKey.getRelevanceTypeKey(matchId, playId);
        redisUtils.hset(key, String.valueOf(subPlayId), String.valueOf(relevanceType));
        redisUtils.expire(key, 180L, TimeUnit.DAYS);
        log.info("::{}::缓存水差关联状态：key={},subPlayId={},relevanceType={}", CommonUtil.getRequestId(matchId,playId), key, subPlayId, relevanceType);
        if ((SportIdEnum.isBasketball(sportId) && !Lists.newArrayList(145L, 146L).contains(playId)) || SportIdEnum.isFootball(sportId)) {
//            String key = RedisKey.getRelevanceTypeKey(matchId, playId);
//            redisUtils.hset(key, String.valueOf(subPlayId), String.valueOf(relevanceType));
//            log.info("缓存水差关联状态：key={},subPlayId={},relevanceType={}", key, subPlayId, relevanceType);
//        } else {
//            RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(reqVo),RcsMatchMarketConfig.class);
//            rcsMatchPlayConfigMapper.insertOrUpdateMarketHeadGap(config);
            String tag = matchId + "_" + playId + "_" + relevanceType;
            // 更新MongoDB
            MatchMarketTradeTypeVo mongo = new MatchMarketTradeTypeVo()
                    .setSportId(sportId)
                    .setMatchId(matchId)
                    .setCategoryId(playId)
                    .setRelevanceType(relevanceType);
            if (!ObjectUtils.isEmpty(subPlayId)){
                mongo.setSubPlayId(subPlayId);
            }
            mongo.setLinkId(linkId);
            producerSendMessageUtils.sendMessage(MqConstants.MARKET_CONGIG_UPDTAE_TOPIC, tag, linkId, mongo);
        }
        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(reqVo),RcsMatchMarketConfig.class);
        config.setSubPlayId(StringUtils.isBlank(config.getSubPlayId()) ? playId.toString() :config.getSubPlayId());
        rcsMatchPlayConfigMapper.insertOrUpdateMarketHeadGap(config);
        // 推送WS
        MatchStatusAndDataSuorceVo vo = new MatchStatusAndDataSuorceVo()
                .setLinkId(linkId)
                .setSportId(sportId)
                .setMatchId(matchId)
                .setPlayId(playId)
                .setSubPlayId(subPlayId)
                .setRelevanceType(relevanceType);
        producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, linkId, vo);
        return linkId;
    }
}
