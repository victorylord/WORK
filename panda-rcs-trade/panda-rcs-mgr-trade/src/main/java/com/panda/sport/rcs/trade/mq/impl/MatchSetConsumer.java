package com.panda.sport.rcs.trade.mq.impl;

import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.MatchSetEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.mapper.RcsLockMapper;
import com.panda.sport.rcs.mongo.MatchSetVo;
import com.panda.sport.rcs.pojo.RcsMatchConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.mq.RcsConsumer;
import com.panda.sport.rcs.trade.param.RcsMatchConfigParam;
import com.panda.sport.rcs.trade.service.TradeMarketSetServiceImpl;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.service.impl.TradeModeServiceImpl;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.MarketViewService;
import com.panda.sport.rcs.trade.wrapper.MatchTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchConfigService;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.UpdateOddsValueVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * 赛前十五分钟设置
 *
 * @author enzo
 */
@Component
@Slf4j
@RocketMQMessageListener(
        topic = "MONGODB_MATCH_SET",
        consumerGroup = "RCS_TRADE_MONGODB_MATCH_SET",
        messageModel = MessageModel.CLUSTERING,
        consumeMode = ConsumeMode.CONCURRENTLY)
public class MatchSetConsumer extends RcsConsumer<Long> {

    @Autowired
    MongoTemplate mongoTemplate;

    @Autowired
    private RcsLockMapper lockMapper;

    @Autowired
    private MarketViewService marketViewService;

    @Autowired
    private RcsMatchConfigService rcsMatchConfigService;

    @Autowired
    private MarketStatusService marketStatusService;

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeModeServiceImpl tradeModeService;

    @Autowired
    TradeMarketSetServiceImpl tradeMarketSetService;

    @Autowired
    private MatchTradeConfigService matchTradeConfigService;

    @Override
    protected String getTopic() {
        return "MONGODB_MATCH_SET";
    }

    @Override
    public Boolean handleMs(Long matchId) {
        try {
            if (matchId == null) return true;
            log.info("::{}::赛事,进去滚球设置生效", matchId);

            String lock = String.format("RCS_TRADE_MONGODB_MATCH_SET_%s", matchId);
            if (lockMapper.saveLock(lock) <= 0) {
                log.warn("::{}::赛事生效方法只执行一次，lock:{}",matchId,lock);
                return true;
            }

            Query query = new Query();
            query.addCriteria(Criteria.where("matchId").is(matchId));
            query.with(Sort.by(Sort.Order.asc("updateTime")));
            //查询前十五分钟所有配置
            List<MatchSetVo> matchSetVos = mongoTemplate.find(query, MatchSetVo.class);

            if (CollectionUtils.isEmpty(matchSetVos)) {
//                tradeStatusService.switchLive(matchId, true);
                return false;
            }
            List<MatchSetVo> matchStatusSet = matchSetVos.stream().filter(filter -> MatchSetEnum.UPDTAE_MARKET_STATUS.getCode().equals(filter.getMethodNo()) &&
                    TradeLevelEnum.MATCH.getLevel().equals(filter.getTradeLevel())).collect(Collectors.toList());
            if (CollectionUtils.isEmpty(matchStatusSet)) {
//                tradeStatusService.switchLive(matchId, true);
            } else {
//                tradeStatusService.switchLive(matchId, false);
            }

            List<MatchSetVo> collect = matchSetVos.stream().filter(filter -> StringUtils.isNotBlank(filter.getJsonParams())).collect(Collectors.collectingAndThen(Collectors.toCollection
                    (() -> new TreeSet<>(Comparator.comparing(MatchSetVo::getJsonParams))), ArrayList::new));


            if (CollectionUtils.isEmpty(collect)) return false;

            //查询前十五分钟赛事级别状态配置
            collect.forEach(matchSetVo -> {
                try {
                    MatchSetEnum matchSetEnum = MatchSetEnum.getMatchSet(matchSetVo.getMethodNo());
                    switch (matchSetEnum) {
//                        case UPDTAE_MARKET_STATUS:
//                            MarketStatusUpdateVO statusUpdateVO = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), MarketStatusUpdateVO.class);
//                            statusUpdateVO.setMatchSnapshot(0);
//                            statusUpdateVO.setLinkedType(LinkedTypeEnum.FIFTEEN.getCode());
//                            tradeStatusService.updateTradeStatus(statusUpdateVO);
//                            break;
//                        case UPDATE_MARKET_TRADETYPE:
//                            MarketStatusUpdateVO tradeTypeUpdateVO = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), MarketStatusUpdateVO.class);
//                            tradeTypeUpdateVO.setMatchSnapshot(0);
//                            tradeTypeUpdateVO.setLinkedType(LinkedTypeEnum.FIFTEEN.getCode());
//                            tradeModeService.updateTradeMode(tradeTypeUpdateVO);
//                            break;
                        case UPDATE_ODDS_VALUE:
                            UpdateOddsValueVo updateOddsValueVo = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), UpdateOddsValueVo.class);
                            marketViewService.updateOddsValue(updateOddsValueVo);
                            break;
                        case UPDTAE_MATCHCONFIG:
                            RcsMatchConfig matchConfig = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), RcsMatchConfig.class);
                            rcsMatchConfigService.updateRcsMatchConfig(matchConfig);
                            break;
                        case UPDTAE_MARKETCONFIG:
                            RcsMatchMarketConfig matchMarketConfig = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), RcsMatchMarketConfig.class);
                            marketViewService.updateMatchMarketConfig(matchMarketConfig);
                            break;
                        case UPDATE_RISKMANAGER_CODE:
                            RcsMatchConfigParam matchConfigParam = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), RcsMatchConfigParam.class);
                            matchTradeConfigService.updateRiskManagerCode(matchConfigParam);
                            break;
                        case UPDTAE_MARKET_ODDS_VALUE:
                            RcsMatchMarketConfig config = JsonFormatUtils.fromJson(matchSetVo.getJsonParams(), RcsMatchMarketConfig.class);
                            tradeMarketSetService.updateMarketOdds(config);
                            break;
                        default:
                            break;
                    }

                } catch (Exception e) {
                    log.error("::{}::MatchSetConsumer异常{}", CommonUtil.getRequestId(matchSetVo.getMatchId()), e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            log.error("::{}::MatchSetConsumer异常{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
        }
        return true;
    }

//    void matchStatusSeal(Long matchId) {
//        //赛事封盘
//        MarketStatusUpdateVO matchStatusUp = new MarketStatusUpdateVO()
//                .setMatchId(matchId)
//                .setTradeLevel(TradeLevelEnum.MATCH.getLevel())
//                .setMarketStatus(MarketStatusEnum.SEAL.getState())
//                .setLinkedType(LinkedTypeEnum.LIVE.getCode());
//        marketStatusService.updateMarketStatus(matchStatusUp);
//        log.info("MatchSetConsumer赛事自动封盘" + JsonFormatUtils.toJson(matchStatusUp));
//    }
//
//    void matchLiveUp(Long matchId, boolean matchStatusSeal) {
//        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, null);
//
//        List<Long> switchErrorPlayList = new ArrayList<Long>();
//        if (!CollectionUtils.isEmpty(tradeModeMap)) {
//            // 所有玩法切自动
//            List<Long> notAutoPlayIds = new ArrayList<>(tradeModeMap.size());
//            tradeModeMap.forEach((k, v) -> {
//                if (!TradeEnum.isAuto(v)) {
//                    notAutoPlayIds.add(k);
//                }
//            });
//            if (!CollectionUtils.isEmpty(notAutoPlayIds)) {
//                MarketStatusUpdateVO tradeTypeUp = new MarketStatusUpdateVO()
//                        .setMatchId(matchId)
//                        .setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel())
//                        .setTradeType(TradeTypeEnum.AUTO.getCode())
//                        .setCategoryIdList(notAutoPlayIds)
//                        .setLinkedType(LinkedTypeEnum.LIVE.getCode());
//                String linkId = marketStatusService.updateMarketTradeType(tradeTypeUp);
//                log.info("MatchSetConsumer赛事所有玩法切自动:linkId=" + linkId);
//
//                //有玩法切换失败，说明是新增的，需要对当前玩法封盘
//                if(tradeTypeUp.getSwitchErrorPlayList() != null && tradeTypeUp.getSwitchErrorPlayList().size() > 0 ) {
//                	switchErrorPlayList = tradeTypeUp.getSwitchErrorPlayList();
//                	JSONObject obj = new JSONObject().fluentPut("tradeLevel", TradeLevelEnum.BATCH_PLAY.getLevel())
//                            .fluentPut("matchId", matchId)
//                            .fluentPut("playIdList", tradeTypeUp.getSwitchErrorPlayList())
//                             .fluentPut("status", NumberUtils.INTEGER_ONE.toString())
//                             .fluentPut("linkedType", 1)
//                             .fluentPut("remark", "早盘进滚球切换失败玩法封盘");
//
//					String linkIdCloseStatus = linkId + "_close";
//					Request<JSONObject> request = new Request<>();
//					request.setData(obj);
//					request.setLinkId(linkIdCloseStatus);
//					request.setDataSourceTime(System.currentTimeMillis());
//
//					List<MarketPlaceDtlDTO> list = new ArrayList<>(switchErrorPlayList.size());
//					switchErrorPlayList.forEach(id -> {
//						MarketPlaceDtlDTO dto = new MarketPlaceDtlDTO();
//		                dto.setStandardCategoryId(id);
//		                dto.setPlaceNum(NumberUtils.INTEGER_MINUS_ONE);
//		                dto.setPlaceNumStatus(TradeStatusEnum.SEAL.getStatus().toString());
//		                list.add(dto);
//					});
//					// 向上游推送位置状态
//		            marketStatusService.putTradeMarketPlaceConfig(matchId, list, "_live");
//
//					producerSendMessageUtils.sendMessage("RCS_TRADE_UPDATE_MARKET_STATUS", String.valueOf(matchId),
//							linkIdCloseStatus, request);
//                }
//            }
//        }
//
//        //赛事封盘
//        if (matchStatusSeal) {
//            matchStatusSeal(matchId);
//        }
//        // 所有未开盘的状态
//        List<RcsTradeConfig> notOpenList = rcsTradeConfigService.getNotOpenStatusByMatchId(matchId);
//        if (!CollectionUtils.isEmpty(notOpenList)) {
//            List<Long> notOpenPlayIds = new ArrayList<>(notOpenList.size());
//            for (RcsTradeConfig config : notOpenList) {
//                Integer traderLevel = config.getTraderLevel();
//                if (TradeLevelEnum.isPlaySetLevel(traderLevel)) {
//                    // 玩法集开
//                    MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
//                            .setTradeLevel(traderLevel)
//                            .setMatchId(matchId)
//                            .setCategorySetId(NumberUtils.toLong(config.getTargerData()))
//                            .setMarketStatus(TradeStatusEnum.OPEN.getStatus())
//                            .setLinkedType(LinkedTypeEnum.LIVE.getCode());
//                    String linkId = marketStatusService.updateMarketStatus(vo);
//                    log.info("MatchSetConsumer玩法集开:linkId=" + linkId);
//                } else if (TradeLevelEnum.isPlayLevel(traderLevel)) {
//                    notOpenPlayIds.add(NumberUtils.toLong(config.getTargerData()));
//                } else if (TradeLevelEnum.isMarketLevel(traderLevel)) {
//                	if(!switchErrorPlayList.contains(NumberUtils.toLong(config.getAddition1()))) {
//                		// 盘口位置开
//                		MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
//                                .setTradeLevel(traderLevel)
//                                .setMatchId(matchId)
//                                .setCategoryId(NumberUtils.toLong(config.getAddition1()))
//                                .setMarketPlaceNum(NumberUtils.toInt(config.getTargerData()))
//                                .setMarketStatus(TradeStatusEnum.OPEN.getStatus())
//                                .setLinkedType(LinkedTypeEnum.LIVE.getCode());
//                        String linkId = marketStatusService.updateMarketStatus(vo);
//                        log.info("MatchSetConsumer盘口位置开:linkId=" + linkId);
//                	}
//                }
//            }
//            notOpenPlayIds.removeAll(switchErrorPlayList);
//            if (!CollectionUtils.isEmpty(notOpenPlayIds)) {
//                // 玩法开
//                MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
//                        .setTradeLevel(TradeLevelEnum.BATCH_PLAY.getLevel())
//                        .setMatchId(matchId)
//                        .setCategoryIdList(notOpenPlayIds)
//                        .setMarketStatus(TradeStatusEnum.OPEN.getStatus())
//                        .setLinkedType(LinkedTypeEnum.LIVE.getCode());
//                String linkId = marketStatusService.updateMarketStatus(vo);
//                log.info("MatchSetConsumer玩法开:linkId=" + linkId);
//            }
//        }
//
//        Set<Long> playIds = tradeModeMap.keySet();
//        if (!CollectionUtils.isEmpty(playIds)) {
//            List<MarketPlaceDtlDTO> list = new ArrayList<>(playIds.size());
//            playIds.removeAll(switchErrorPlayList);//过掉失败的盘口
//            for (Long playId : playIds) {
//                String key = RedisKey.getMarketPlaceStatusConfigKey(matchId, playId);
//                // 删除缓存
//                redisClient.delete(key);
//                MarketPlaceDtlDTO dto = new MarketPlaceDtlDTO();
//                dto.setStandardCategoryId(playId);
//                dto.setPlaceNum(NumberUtils.INTEGER_MINUS_ONE);
//                dto.setPlaceNumStatus(TradeStatusEnum.OPEN.getStatus().toString());
//                list.add(dto);
//            }
//            // 向上游推送位置状态
//            marketStatusService.putTradeMarketPlaceConfig(matchId, list, "_live");
//            log.info("MatchSetConsumer赛事所有盘口开盘matchId:{},list:{}", matchId, JsonFormatUtils.toJson(list));
//        }
//    }
}
