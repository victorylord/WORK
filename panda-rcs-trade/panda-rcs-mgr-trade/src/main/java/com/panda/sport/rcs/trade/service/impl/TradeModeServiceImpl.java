package com.panda.sport.rcs.trade.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.panda.merge.dto.MarketPlaceDtlDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.UpdateTradeTypeDTO;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.MqConstants;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.MqConstant;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.dto.StandardMarketPlaceDto;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.service.ApiService;
import com.panda.sport.rcs.trade.service.BuildMarketService;
import com.panda.sport.rcs.trade.service.LinkageCommonService;
import com.panda.sport.rcs.trade.service.MarketBuildService;
import com.panda.sport.rcs.trade.service.TradeModeService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.MatchMarketTradeTypeVo;
import com.panda.sport.rcs.vo.MatchStatusAndDataSuorceVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘模式服务
 * @Author : Paca
 * @Date : 2021-11-15 12:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class TradeModeServiceImpl implements TradeModeService {

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private MarketBuildService marketBuildService;
    @Autowired
    private BuildMarketService buildMarketService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private LinkageCommonService linkageCommonService;

    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RedisUtils redisUtils;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String updateTradeMode(MarketStatusUpdateVO updateVO) {
        CommonUtils.mdcPutIfAbsent();
        updateVO.updateTradeModeParamCheck();
        // 获取运动种类ID
        Long sportId = getSportId(updateVO);
        String linkId = updateVO.generateLinkId("mode");
        log.info("::{}::修改操盘类型开始：入参={}", linkId, JSON.toJSONString(updateVO));

        Integer tradeLevel = updateVO.getTradeLevel();
        Integer tradeType = updateVO.getTradeType();
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        List<Long> playIdList = updateVO.getCategoryIdList();

        if (updateVO.getIsMts() && !TradeEnum.isAuto(tradeType)) {
            throw new RcsServiceException("MTS赛事只能是A模式！");
        }
        if (TradeEnum.isAutoAdd(tradeType)) {
            if (SportIdEnum.BASKETBALL.isNo(sportId) || !Basketball.isAutoPlus(playId)) {
                throw new RcsServiceException(SportIdEnum.getBySportId(sportId).getName() + "玩法[" + playId + "]不支持A+模式");
            }
        }
        if (TradeEnum.isLinkage(tradeType)) {
            if (SportIdEnum.BASKETBALL.isNo(sportId) || !Basketball.isLinkage(playId)) {
                throw new RcsServiceException(SportIdEnum.getBySportId(sportId).getName() + "玩法[" + playId + "]不支持L模式");
            }
        }
        if (!TradeEnum.isAuto(tradeType) && TradeLevelEnum.isPlayLevel(tradeLevel)) {
            boolean isSell = rcsMatchMarketConfigService.playIsSell(matchId, playId);
            if (!isSell) {
                if (TradeEnum.isLinkage(tradeType)) {
                    handleUnsoldLinkage(updateVO);
                }
                throw new RcsServiceException("玩法[" + playId + "]未开售，不能切换" + TradeEnum.getByTradeType(tradeType).getMode() + "模式");
            }
        }

        // 切换A/A+之前，旧模式盘口先关盘
        switchAutoModeClose(updateVO);

        // 保存操盘类型配置
        saveTradeTypeConfig(updateVO);

        List<MarketPlaceDtlDTO> placeStatusList = null;
        if (YesNoEnum.isYes(updateVO.getIsSeal())) {
            // 操盘模式切换封盘
            MarketStatusUpdateVO updateVO2 = JSON.parseObject(JSON.toJSONString(updateVO), MarketStatusUpdateVO.class);
            placeStatusList = tradeStatusService.updateTradeModeSeal(updateVO2, linkId);
        }
        List<Long> playIds = null;
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            playIds = Lists.newArrayList(updateVO.getCategoryId());
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            playIds = updateVO.getCategoryIdList();
        }

        // 操盘模式标志缓存
        tradeModeCache(updateVO);
        linkageFlagCache(updateVO);
        // 不管切到任何模式，删除A+模式缓存信息
        delAutoPlusCache(updateVO);

        UpdateTradeTypeDTO config = new UpdateTradeTypeDTO();
        config.setSportId(sportId);
        config.setMatchId(matchId);
        config.setPlayIds(playIds);
        config.setTradeType(tradeType);
        config.setPlaceNumStatusList(placeStatusList);
        // 调用融合RPC接口
        apiService.putTradeTypeConfig(config, linkId);
        // 切换到非L模式成功，需要删除L模式缓存
        delLinkageCache(updateVO);

        log.info("::{}::开始推送赔率{}", linkId,JSON.toJSONString(updateVO));

        if (YesNoEnum.isYes(updateVO.getIsPushOdds())) {
            boolean pushOddsFlag = false;
            //篮球
            if (SportIdEnum.BASKETBALL.isYes(sportId)) {
                if (TradeEnum.isManual(tradeType) && TradeLevelEnum.isPlayLevel(tradeLevel)) {
                    if (Basketball.isManualBuildMarket(playId)) {
                        // 篮球让分、大小和单双  spread
                        pushOddsFlag = true;
                        manualBuildMarketList(sportId, matchId, playId, linkId);
                    } else if (Basketball.Secondary.PLAYER.getPlayIds().contains(playId) || Lists.newArrayList(145L, 146L).contains(playId)) {
                        // 球员玩法
                        pushOddsFlag = true;
                        marketBuildService.playerBuildMarket(matchId, playId, linkId);
                    }
                } else if (TradeEnum.isLinkage(tradeType) && TradeLevelEnum.isPlayLevel(tradeLevel)) {
                    // 联动模式
                    pushOddsFlag = true;
//                    marketBuildService.linkageBuildMarket(matchId, playId, linkId, true);
                    buildMarketService.switchLinkageBuildMarket(sportId, matchId, playId, true, true, true, linkId);
                }
            }
            //网球
            else if(SportIdEnum.TENNIS.isYes(sportId)){
                if (TradeEnum.isManual(tradeType) && TradeLevelEnum.isPlayLevel(tradeLevel)) {
                    if (Tennis.isManualBuildMarket(playId)) {
                        pushOddsFlag = true;
                        manualBuildMarketList(sportId, matchId, playId, linkId);
                    }
                }
            }
            //乒乓球
            else if(SportIdEnum.PING_PONG.isYes(sportId)){
                if (TradeEnum.isManual(tradeType) && TradeLevelEnum.isPlayLevel(tradeLevel)) {
                    if (PingPong.isManualBuildMarket(playId)) {
                        pushOddsFlag = true;
                        manualBuildMarketList(sportId, matchId, playId, linkId);
                    }
                }
            }
            //冰球
            else if(SportIdEnum.ICE_HOCKEY.isYes(sportId)){
                if (TradeEnum.isManual(tradeType) && TradeLevelEnum.isPlayLevel(tradeLevel)) {
                    if (IceHockey.isManualBuildMarket(playId)) {
                        pushOddsFlag = true;
                        manualBuildMarketList(sportId, matchId, playId, linkId);
                    }
                }
            }
            // 切换到A+，由super_a推赔率
            if (!pushOddsFlag && !TradeEnum.isAutoAdd(tradeType)) {
                if (CollectionUtils.isNotEmpty(playIds)) {
                    Map<Long, Integer> tradeModeMap = Maps.newHashMap();
                    playIds.forEach(pId -> tradeModeMap.put(pId, tradeType));
                    //推送赔率
                    apiService.pushOdds(updateVO, playIds, null, tradeModeMap, linkId);
                }
            }
        }
        log.info("::{}::开始进行清概率差和MQ操作:{}", linkId,JSON.toJSONString(updateVO));

        // 清概率差等
        footballClear(tradeLevel, sportId, matchId, playId, playIdList, linkId);

        // 发送MQ，推送WS到前端
        tradeTypePushWS(updateVO);

        updateVO.setNewFlag(1);
        // 发送MQ，更新MongoDB
        updateMongoDB(updateVO);

        return linkId;
    }

    /**
     * 切换到A/A+模式之前，现有非A模式盘口数据源关盘
     *
     * @param updateVO
     */
    private void switchAutoModeClose(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        Long matchId = updateVO.getMatchId();
        Integer matchType = updateVO.getMatchType();
        Integer tradeType = updateVO.getTradeType();
        if (!TradeEnum.isAuto(tradeType) && !TradeEnum.isAutoAdd(tradeType)) {
            return;
        }
        List<Long> playIds = null;
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            playIds = Lists.newArrayList(updateVO.getCategoryId());
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            playIds = updateVO.getCategoryIdList();
        }
        if (CollectionUtils.isEmpty(playIds)) {
            return;
        }
        Map<Long, Integer> tradeModeMap = rcsTradeConfigService.getTradeMode(matchId, playIds);
        if (CollectionUtils.isEmpty(tradeModeMap)) {
            // 都是A模式
            return;
        }
        // 所有非A模式玩法
        List<Long> notAutoPlayIds = new ArrayList<>(tradeModeMap.size());
        tradeModeMap.forEach((playId, tradeMode) -> {
            if (!TradeEnum.isAuto(tradeMode)) {
                notAutoPlayIds.add(playId);
            }
        });
        if (CollectionUtils.isEmpty(notAutoPlayIds)) {
            // 都是A模式
            return;
        }
        Map<Long, List<StandardSportMarket>> marketMap = standardSportMarketService.getEffectiveMarket(matchId, notAutoPlayIds);
        if (CollectionUtils.isEmpty(marketMap)) {
            return;
        }
        List<StandardMarketDTO> marketList = Lists.newArrayList();
        marketMap.values().forEach(values -> values.forEach(market -> {
            StandardMarketDTO marketDTO = MarketUtils.toStandardMarketDTO(market);
            marketDTO.setStatus(TradeStatusEnum.CLOSE.getStatus());
            marketDTO.setPlaceNumStatus(TradeStatusEnum.CLOSE.getStatus());
            marketDTO.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
            if (matchType != null) {
                marketDTO.setMarketType(matchType);
            }
            marketList.add(marketDTO);
        }));
        apiService.putTradeMarketOdds(matchId, marketList, updateVO.getLinkId() + "_close");
    }

    private void footballClear(Integer tradeLevel, Long sportId, Long matchId, Long playId, List<Long> playIds, String linkId) {
        // 清概率差等
        if (SportIdEnum.FOOTBALL.isNo(sportId) || !IceHockey.threeItemsWinAlonePlays(playId)) {
            return;
        }
        List<ClearSubDTO> list = Lists.newArrayList();
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            ClearSubDTO config = new ClearSubDTO();
            config.setMatchId(matchId);
            config.setPlayId(playId);
            list.add(config);
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            list = playIds.stream().map(pId -> {
                ClearSubDTO config = new ClearSubDTO();
                config.setMatchId(matchId);
                config.setPlayId(pId);
                return config;
            }).collect(Collectors.toList());
        }
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setType(0);
        clearDTO.setMatchId(matchId);
        clearDTO.setList(list);
        clearDTO.setGlobalId(linkId + "_clear");
        producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", tradeLevel + "_" + sportId + "_" + matchId, clearDTO.getGlobalId(), clearDTO);
    }

    private void updateMongoDB(MarketStatusUpdateVO marketStatusUpdateVO) {
        Integer tradeLevel = marketStatusUpdateVO.getTradeLevel();
        Long sportId = marketStatusUpdateVO.getSportId();
        Long matchId = marketStatusUpdateVO.getMatchId();
        Long playId = marketStatusUpdateVO.getCategoryId();
        List<Long> playIdList = marketStatusUpdateVO.getCategoryIdList();
        Integer tradeMode = marketStatusUpdateVO.getTradeType();
        MatchMarketTradeTypeVo tradeTypeVo = new MatchMarketTradeTypeVo()
                .setSportId(sportId)
                .setMatchId(matchId)
                .setCategorySetId(marketStatusUpdateVO.getCategorySetId())
                .setCategoryId(playId)
                .setPlayIds(playIdList)
                .setTradeType(tradeMode)
                .setLevel(tradeLevel)
                .setStatus(marketStatusUpdateVO.getMarketStatus())
                .setNewFlag(marketStatusUpdateVO.getNewFlag())
                .setAutoCount(marketStatusUpdateVO.getAutoCount())
                .setManualCount(marketStatusUpdateVO.getManualCount())
                .setAutoAddCount(marketStatusUpdateVO.getAutoAddCount());
        String linkId = marketStatusUpdateVO.getLinkId() + "_MongoDB";
        tradeTypeVo.setLinkId(linkId);
        producerSendMessageUtils.sendMessage(MqConstants.MARKET_CONGIG_UPDTAE_TOPIC, matchId.toString(), linkId, tradeTypeVo);
    }

    /**
     * 修改操盘类型推送WS
     *
     * @param marketStatusUpdateVO
     */
    private void tradeTypePushWS(MarketStatusUpdateVO marketStatusUpdateVO) {
        Integer tradeLevel = marketStatusUpdateVO.getTradeLevel();
        Long matchId = marketStatusUpdateVO.getMatchId();
        Long categoryId = marketStatusUpdateVO.getCategoryId();
        Long categorySetId = marketStatusUpdateVO.getCategorySetId();
        List<Long> categoryIdList = marketStatusUpdateVO.getCategoryIdList();
        Integer tradeType = marketStatusUpdateVO.getTradeType();

        // 获取赛事状态，0-自动，1-手动，2、智能模式
        List<Integer> matchDataSource = new ArrayList<>();

        Query query = new Query();
        Criteria criteria = new Criteria();
        criteria.and("matchId").is(matchId.toString());
        criteria.and("matchMarketVoList.oddsFieldsList.id").gt(0L);
        //数据源展示开、封状态
        criteria.and("matchMarketVoList.thirdMarketSourceStatus").in(Arrays.asList(0, 1));
        query.addCriteria(criteria);

        List<MarketCategory> categoryList = mongoTemplate.find(query, MarketCategory.class);
        int autoCount = "0".equals(String.valueOf(marketStatusUpdateVO.getTradeType())) ? 1 : 0;
        int manualCount = "1".equals(String.valueOf(marketStatusUpdateVO.getTradeType())) ? 1 : 0;
        int autoAddCount = "2".equals(String.valueOf(marketStatusUpdateVO.getTradeType())) ? 1 : 0;
        for (MarketCategory category : categoryList) {
            if (null == category.getTradeType()) category.setTradeType(TradeEnum.AUTO.getCode());
            if (TradeLevelEnum.isPlayLevel(tradeLevel) && categoryId.equals(category.getId())) {
                category.setTradeType(tradeType);
            }
            if ((TradeLevelEnum.isPlaySetLevel(tradeLevel) || TradeLevelEnum.isBatchPlayLevel(tradeLevel))
                    && categoryIdList.contains(category.getId())) {
                category.setTradeType(tradeType);
            }
            if (TradeEnum.isAuto(category.getTradeType())) {
                autoCount++;
            } else if (TradeEnum.isManual(category.getTradeType())) {
                manualCount++;
            } else if (TradeEnum.isAutoAdd(category.getTradeType())) {
                autoAddCount++;
            }
        }
        if (autoCount > 0) matchDataSource.add(TradeEnum.AUTO.getCode());
        if (manualCount > 0) matchDataSource.add(TradeEnum.MANUAD.getCode());
        if (autoAddCount > 0) matchDataSource.add(TradeEnum.AUTOADD.getCode());

        marketStatusUpdateVO.setAutoCount(autoCount);
        marketStatusUpdateVO.setManualCount(manualCount);
        marketStatusUpdateVO.setAutoAddCount(autoAddCount);

        MatchStatusAndDataSuorceVo vo = new MatchStatusAndDataSuorceVo();
        vo.setLinkId(marketStatusUpdateVO.getLinkId() + "_WS");
        vo.setMatchId(matchId);
        vo.setMatchDataSource(matchDataSource);
        vo.setLevel(tradeLevel);
        vo.setDataSource(tradeType);
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            vo.setId(categoryId.toString());
        } else if (TradeLevelEnum.isPlaySetLevel(tradeLevel)) {
            vo.setId(categorySetId.toString());
            vo.setCategoryIdList(categoryIdList);
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            vo.setCategoryIdList(categoryIdList);
        } else {
            // 其他不支持
            return;
        }
        producerSendMessageUtils.sendMessage(MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TOPIC, MqConstants.DATA_SOURCE_DURING_GAME_PLAY_TAG, vo.getLinkId(), vo);
    }

    private void saveTradeTypeConfig(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        String matchId = updateVO.getMatchId().toString();
        Integer tradeType = updateVO.getTradeType();
        String user = String.valueOf(updateVO.getUpdateUserId());
        Integer linkedType;
        if (updateVO.getLinkedType() == null) {
            linkedType = LinkedTypeEnum.TRADE_MODE.getCode();
        } else {
            linkedType = updateVO.getLinkedType();
        }
        List<Long> playIds = null;
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            playIds = Lists.newArrayList(updateVO.getCategoryId());
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            playIds = updateVO.getCategoryIdList();
        }
        if (CollectionUtils.isEmpty(playIds)) {
            return;
        }
        List<RcsTradeConfig> configList = playIds.stream().map(playId -> {
            RcsTradeConfig config = new RcsTradeConfig();
            config.setMatchId(matchId);
            config.setTraderLevel(TraderLevelEnum.PLAY.getLevel());
            config.setTargerData(playId.toString());
            config.setDataSource(tradeType);
            if (YesNoEnum.isYes(updateVO.getIsSeal())) {
                config.setStatus(TradeStatusEnum.SEAL.getStatus());
            }
            config.setSourceType(linkedType);
            config.setUpdateUser(user);
            return config;
        }).collect(Collectors.toList());
        // 保存玩法操盘方式配置
        rcsTradeConfigService.saveBatch(configList);
    }

    /**
     * 操盘模式缓存
     *
     * @param updateVO
     */
    private void tradeModeCache(MarketStatusUpdateVO updateVO) {
        Integer tradeLevel = updateVO.getTradeLevel();
        String key = RedisKey.getTradeModeKey(updateVO.getMatchId());
        List<Long> playIds = null;
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            playIds = Lists.newArrayList(updateVO.getCategoryId());
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            playIds = updateVO.getCategoryIdList();
        }
        if (CollectionUtils.isEmpty(playIds)) {
            return;
        }
        Map<String, String> hash = Maps.newHashMap();
        playIds.forEach(playId -> hash.put(String.valueOf(playId), String.valueOf(updateVO.getTradeType())));
        redisUtils.hmset(key, hash);
        redisUtils.expire(key, 30L, TimeUnit.DAYS);
        log.info("::{}::操盘模式缓存：key={},hashMap={}", CommonUtil.getRequestId(), key, JSON.toJSONString(hash));
    }

    private Map<String, String> autoPlusFlagCache(MarketStatusUpdateVO updateVO) {
        Map<String, String> oldHash = Maps.newHashMap();
        Integer tradeLevel = updateVO.getTradeLevel();
        Integer tradeMode = updateVO.getTradeType();
        Long matchId = updateVO.getMatchId();
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            Long playId = updateVO.getCategoryId();
            String key = String.format(RedisKey.AUTO_PLUS_SWITCH_FLAG, matchId, playId);
            String oldValue = redisUtils.get(key);
            if (StringUtils.isNotBlank(oldValue)) {
                oldHash.put(key, oldValue);
            }
            if (TradeEnum.isAutoAdd(tradeMode)) {
                redisUtils.set(key, "1");
                redisUtils.expire(key, 30L, TimeUnit.DAYS);
            } else {
                redisUtils.del(key);
            }
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            List<Long> playIdList = updateVO.getCategoryIdList();
            playIdList.forEach(playId -> {
                String key = String.format(RedisKey.AUTO_PLUS_SWITCH_FLAG, matchId, playId);
                String oldValue = redisUtils.get(key);
                if (StringUtils.isNotBlank(oldValue)) {
                    oldHash.put(key, oldValue);
                }
                if (TradeEnum.isAutoAdd(tradeMode)) {
                    redisUtils.set(key, "1");
                    redisUtils.expire(key, 30L, TimeUnit.DAYS);
                } else {
                    redisUtils.del(key);
                }
            });
        }
        log.info("::{}::A+模式缓存：updateVO={},oldHash={}",CommonUtil.getRequestId(), JSON.toJSONString(updateVO), JSON.toJSONString(oldHash));
        return oldHash;
    }

    private Map<Long, String> linkageFlagCache(MarketStatusUpdateVO updateVO) {
        Map<Long, String> oldHash = Maps.newHashMap();
        if (SportIdEnum.BASKETBALL.isNo(updateVO.getSportId())) {
            return oldHash;
        }
        Integer tradeLevel = updateVO.getTradeLevel();
        Integer tradeMode = updateVO.getTradeType();
        Long matchId = updateVO.getMatchId();
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            Long playId = updateVO.getCategoryId();
            oldHash.put(playId, linkageFlagCache(matchId, playId, tradeMode));
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            updateVO.getCategoryIdList().forEach(playId -> oldHash.put(playId, linkageFlagCache(matchId, playId, tradeMode)));
        }
        log.info("::{}::L模式缓存：updateVO={},oldHash={}",CommonUtil.getRequestId(), JSON.toJSONString(updateVO), JSON.toJSONString(oldHash));
        return oldHash;
    }

    private String linkageFlagCache(Long matchId, Long playId, Integer tradeMode) {
        String key = String.format(RedisKey.LINKAGE_SWITCH_FLAG, matchId, playId);
        String oldValue = redisUtils.get(key);
        if (TradeEnum.isLinkage(tradeMode)) {
            redisUtils.set(key, String.valueOf(tradeMode));
            log.info("::{}::L模式缓存：set,key={},value={}",CommonUtil.getRequestId(), key, tradeMode);
            redisUtils.expire(key, 30L, TimeUnit.DAYS);
        } else {
            redisUtils.del(key);
            log.info("::{}::L模式缓存：del,key={}",CommonUtil.getRequestId(), key);
        }
        if (StringUtils.isNotBlank(oldValue)) {
            return oldValue;
        }
        return "";
    }

    private void delAutoPlusCache(MarketStatusUpdateVO updateVO) {
        if (SportIdEnum.BASKETBALL.isNo(updateVO.getSportId())) {
            return;
        }
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        String key = RedisKey.MainMarket.getAutoPlusMainMarketInfoKey(matchId);
        String hashKey = RedisKey.MainMarket.getAutoPlusMainMarketInfoHashKey(playId, playId);
        redisUtils.hdel(key, hashKey);
        log.info("::{}::A+模式缓存删除：key={},hashKey={}",CommonUtil.getRequestId(), key, hashKey);
    }

    private void delLinkageCache(MarketStatusUpdateVO updateVO) {
        if (SportIdEnum.BASKETBALL.isNo(updateVO.getSportId())) {
            return;
        }
        Integer tradeMode = updateVO.getTradeType();
        if (TradeEnum.isLinkage(tradeMode)) {
            return;
        }
        log.info("::{}::L模式缓存删除：updateVO={}",CommonUtil.getRequestId(), JSON.toJSONString(updateVO));
        Integer tradeLevel = updateVO.getTradeLevel();
        // 从L模式切换到其它模式，需要清除缓存
        Long matchId = updateVO.getMatchId();
        if (TradeLevelEnum.isPlayLevel(tradeLevel)) {
            Long playId = updateVO.getCategoryId();
            delLinkageCache(matchId, playId);
        } else if (TradeLevelEnum.isBatchPlayLevel(tradeLevel)) {
            List<Long> playIdList = updateVO.getCategoryIdList();
            playIdList.forEach(playId -> delLinkageCache(matchId, playId));
        }
    }

    private void delLinkageCache(Long matchId, Long playId) {
        Basketball.Linkage linkage = Basketball.Linkage.getByTargetPlayId(playId);
        if (linkage == null) {
            return;
        }
        Long anotherPlayId = linkage.getTotalT1().equals(playId) ? linkage.getTotalT2() : linkage.getTotalT1();
        if (!tradeStatusService.isLinkage(matchId, anotherPlayId)) {
            linkageCommonService.delLinkageCache(matchId, linkage);
        }
    }

    private void manualBuildMarketList(Long sportId, Long matchId, Long playId, String linkId) {
        log.info("::{}::开始构建盘口信息:赛事{}，球种{}，玩法{}",linkId, matchId,sportId,playId);
        StandardMarketPlaceDto mainMarket = standardSportMarketService.getMainMarketPlaceInfo(matchId, playId);
        if (mainMarket == null) {
            log.info("::{}::未获取到主盘口位置信息，放弃构建：matchId={},playId={}",linkId, matchId, playId);
            return;
        }
        //处理Addition1值为null导致空指针异常
        if(StringUtils.isEmpty(mainMarket.getAddition1())){
            mainMarket.setAddition1("0");
        }
        List<StandardSportMarketOdds> mainMarketOddsList = standardSportMarketOddsService.list(mainMarket.getMarketId());
        BuildMarketConfigDto config = rcsMatchMarketConfigService.getBuildMarketConfig(matchId, playId);
        List<BigDecimal> marketValueList = Lists.newArrayList();

        //篮球
        if(SportIdEnum.isBasketball(sportId)) {
            if (Basketball.isHandicapOrTotal(playId)) {
                BigDecimal mainMv = new BigDecimal(mainMarket.getAddition1());
                marketValueList = MarketUtils.generateMarketValues(playId, config.getMarketCount(), mainMv, config.getMarketNearDiff());
            }
        }
        //网球 & 乒乓球
        else if(SportIdEnum.isTennis(sportId) || SportIdEnum.isPingpong(sportId) || SportIdEnum.isIceHockey(sportId)){
            BigDecimal mainMv = new BigDecimal(mainMarket.getAddition1());
            if(SportIdEnum.isIceHockey(sportId) && Lists.newArrayList(2L,259L).contains(playId) && RcsConstant.SPECIAL_MARKET_VALUE.compareTo(mainMv) == 0){
                config.setMarketCount(1);
            }
            marketValueList = MarketUtils.generateMarketValuesTennisAndPong(playId, config.getMarketCount(), mainMv, config.getMarketNearDiff());
        }
        List<StandardMarketDTO> marketList = buildMarketList(sportId, mainMarket, mainMarketOddsList, config, marketValueList);
        if (CollectionUtils.isNotEmpty(marketList)) {
            if (Basketball.Main.getHandicapPlayIds().contains(playId) && MarketUtils.isSealCheck(marketValueList)) {
                // 让球玩法只要出现0或±0.5的球头，独赢玩法自动封盘，独赢玩法盘口级封盘
                WinSeal(matchId, Basketball.Main.getWinAloneByHandicap(playId), linkId);
            }
//            CommonUtils.sleep(TimeUnit.MILLISECONDS, 500L);
            apiService.putTradeMarketOdds(matchId, marketList, linkId);
        }
    }

    private void WinSeal(Long matchId, Long winPlayId, String linkId) {
        // 独赢玩法盘口级封盘
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tradeLevel", TradeLevelEnum.MARKET.getLevel());
        jsonObject.put("sportId", 2L);
        jsonObject.put("matchId", matchId);
        jsonObject.put("playId", winPlayId);
        jsonObject.put("placeNum", 1);
        jsonObject.put("status", TradeStatusEnum.SEAL.getStatus());
        jsonObject.put("linkedType", LinkedTypeEnum.MANUAL.getCode());
        jsonObject.put("remark", "让球出现0或±0.5的球头，独赢封盘");
        Request<JSONObject> request = new Request<>();
        request.setData(jsonObject);
        request.setLinkId(linkId + "_M");
        request.setDataSourceTime(System.currentTimeMillis());
        producerSendMessageUtils.sendMessage(MqConstant.Topic.RCS_TRADE_UPDATE_MARKET_STATUS, matchId + "_M", request.getLinkId(), request);
    }

    /**
     * 构建附加盘口信息
     *
     * @param mainMarket         主盘口信息
     * @param mainMarketOddsList 主盘口赔率信息
     * @param config             盘口构建参数
     * @param marketValueList    盘口值集合
     * @return
     * @author Paca
     */
    private List<StandardMarketDTO> buildMarketList(Long sportId, StandardMarketPlaceDto mainMarket, List<StandardSportMarketOdds> mainMarketOddsList,
                                                    BuildMarketConfigDto config, List<BigDecimal> marketValueList) {
        Long matchId = mainMarket.getStandardMatchInfoId();
        Long playId = mainMarket.getMarketCategoryId();
        BigDecimal playWaterDiff = BigDecimal.ZERO;
        Map<Integer, BigDecimal> placeWaterDiffMap = config.getPlaceWaterDiffMap();
        Map<Integer, BigDecimal> placeSpreadMap = config.getPlaceSpreadMap();
        // 盘口类型，1-赛前盘，0-滚球盘
        Integer matchType = config.getMatchType();
        // 最大盘口数
        Integer marketCount = config.getMarketCount();
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = config.getMarketNearOddsDiff();
        // 主盘 spread
        BigDecimal mainSpread = placeSpreadMap.get(NumberUtils.INTEGER_ONE);
        Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(sportId, matchId, playId, null);
        Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);

        List<StandardMarketDTO> marketList = new ArrayList<>(marketCount);
        for (int i = 0; i < marketCount; i++) {
            if (Basketball.isOddEven(playId) && i > 0) {
                break;
            }
            BigDecimal marketValue = null;
            if (!Basketball.isOddEven(playId)) {
                marketValue = marketValueList.get(i).stripTrailingZeros();
            }
            int placeNum = i + 1;
            // 1 - spread / 2
            BigDecimal spread = placeSpreadMap.getOrDefault(placeNum, mainSpread);
            BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), 6, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_DOWN);
            Integer placeStatus = placeStatusMap.getOrDefault(placeNum, TradeStatusEnum.OPEN.getStatus());
            BigDecimal homeOdds = malayOdds;
            BigDecimal awayOdds = malayOdds;
            if (i > 0) {
                BigDecimal factor = (i % 2 == 1) ? new BigDecimal(i / 2 + 1) : new BigDecimal(i / 2).negate();
                BigDecimal upOdds = malayOdds.add(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
                homeOdds = OddsConvertUtils.checkMalayOdds(upOdds);
                BigDecimal downOdds = malayOdds.subtract(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
                awayOdds = OddsConvertUtils.checkMalayOdds(downOdds);
            }

            // 水差 = 玩法水差 + 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            BigDecimal waterDiff = playWaterDiff.add(placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO));
            homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds.subtract(waterDiff));
            awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds.add(waterDiff));

            List<StandardMarketOddsDTO> marketOddsDTOList = null;
            if (CollectionUtils.isNotEmpty(mainMarketOddsList)) {
                marketOddsDTOList = new ArrayList<>(mainMarketOddsList.size());
                for (StandardSportMarketOdds marketOdds : mainMarketOddsList) {
                    String oddsType = marketOdds.getOddsType();
                    StandardMarketOddsDTO marketOddsDTO = MarketUtils.toStandardMarketOddsDTO(marketOdds);
                    marketOddsDTO.setActive(1);
                    marketOddsDTO.setMarketDiffValue(null);
                    if (OddsTypeEnum.isHomeOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(homeOdds));
                        marketOddsDTO.setMarketDiffValue(waterDiff.negate().doubleValue());
                    } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(awayOdds));
                        marketOddsDTO.setMarketDiffValue(waterDiff.doubleValue());
                    }
                    marketOddsDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                    if (marketValue != null) {
                        marketOddsDTO.setNameExpressionValue(MarketUtils.getNameExpressionValue(oddsType, marketValue));
                    }
                    marketOddsDTO.setRemark("操盘切换M模式构建盘口");
                    marketOddsDTOList.add(marketOddsDTO);
                }
            }

            StandardMarketDTO standardMarketDTO = MarketUtils.toStandardMarketDTO(mainMarket);
            standardMarketDTO.setMarketType(matchType);
            if (!TradeStatusEnum.isOpen(matchStatus)) {
                standardMarketDTO.setStatus(matchStatus);
            } else {
                standardMarketDTO.setStatus(placeStatus);
            }
            log.info("::{}::M模式构建盘口盘口状态,盘口Id:={},玩法Id:={},placeNum:={},盘口状态:={}","数据商盘口源状态:={}", matchId, standardMarketDTO.getId(),
                    standardMarketDTO.getMarketCategoryId(),placeNum, standardMarketDTO.getStatus(),standardMarketDTO.getThirdMarketSourceStatus());

            standardMarketDTO.setPlaceNumStatus(placeStatus);
            standardMarketDTO.setRemark("操盘切换M模式构建盘口");
            standardMarketDTO.setPlaceNum(placeNum);
            standardMarketDTO.setMarketOddsList(marketOddsDTOList);
            standardMarketDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            if (marketValue != null) {
                standardMarketDTO.setOddsValue(marketValue.toPlainString());
                standardMarketDTO.setAddition1(marketValue.toPlainString());
                if (RcsConstant.BENCHMARK_SCORE.contains(playId)) {
                    // 基准分
                    standardMarketDTO.setAddition2(marketValue.toPlainString());
                }
            }
            marketList.add(standardMarketDTO);
        }
        return marketList;
    }

    private int malayOddsToOddsValue(BigDecimal malayOdds) {
        BigDecimal euOdds = new BigDecimal(rcsOddsConvertMappingService.getEUOdds(malayOdds.toPlainString()));
        return euOdds.multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
    }

    private void handleUnsoldLinkage(MarketStatusUpdateVO updateVO) {
        Long matchId = updateVO.getMatchId();
        Long playId = updateVO.getCategoryId();
        Integer matchType = updateVO.getMatchType();
        Integer linkedType = updateVO.getLinkedType();
        if (LinkedTypeEnum.DEFAULT_L.getCode().equals(linkedType) &&
                NumberUtils.INTEGER_ZERO.equals(matchType) && tradeStatusService.isLinkage(matchId, playId)) {
            // 早盘到滚球默认切换L模式
            linkageCommonService.delLinkageFlag(matchId, playId);
            MarketStatusUpdateVO vo = new MarketStatusUpdateVO();
            vo.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
            vo.setSportId(updateVO.getSportId());
            vo.setMatchType(matchType);
            vo.setMatchId(matchId);
            vo.setCategoryId(playId);
            vo.setMarketStatus(TradeStatusEnum.CLOSE.getStatus());
            vo.setSourceCloseFlag(YesNoEnum.Y.getValue());
            vo.setLinkedType(linkedType);
            String linkId = tradeStatusService.updateTradeStatus(vo);
            log.info("::{}::滚球默认切换L模式，玩法未开售关盘：matchId={},playId={}", linkId,matchId, playId);
            delLinkageCache(matchId, playId);
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

    @Override
    public void basketballPlaySaleSwitchLinkage(Long sportId, Long matchId, List<Long> playIdList, LinkedTypeEnum linkedTypeEnum) {
        if (SportIdEnum.BASKETBALL.isNo(sportId) || CollectionUtils.isEmpty(playIdList)) {
            return;
        }
        log.info("::{}::篮球玩法开售切换L模式：sportId={},matchId={},playIdList={},linkedTypeEnum={}",CommonUtil.getRequestId(), sportId, matchId, playIdList, linkedTypeEnum);
        StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
        if (matchInfo == null) {
            log.warn("::{}::赛事不存在:{}", CommonUtil.getRequestId(), matchId);
            return;
        }
        int matchType = RcsConstant.getMatchType(matchInfo);
        boolean isMts = RcsConstant.isMts(matchInfo);
        if (isMts) {
            log.info("::{}::MTS赛事不能切换L模式",CommonUtil.getRequestId());
            return;
        }
        // 玩法切换成L
        Set<Long> playIds = Sets.newHashSet();
        for (Basketball.Linkage linkage : Basketball.Linkage.values()) {
            if (playIdList.contains(linkage.getTotalT1())) {
                playIds.add(linkage.getTotalT1());
            }
            if (playIdList.contains(linkage.getTotalT2())) {
                playIds.add(linkage.getTotalT2());
            }
        }
        if (org.springframework.util.CollectionUtils.isEmpty(playIds)) {
            return;
        }
        String uuid = CommonUtils.getUUID();
        for (Long playId : playIds) {
            MarketStatusUpdateVO updateVO = new MarketStatusUpdateVO();
            updateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
            updateVO.setSportId(sportId);
            updateVO.setMatchId(matchId);
            updateVO.setCategoryId(playId);
            updateVO.setTradeType(TradeEnum.LINKAGE.getCode());
            updateVO.setLinkedType(linkedTypeEnum.getCode());
            updateVO.setRemark("篮球玩法开售切换L模式：" + uuid);
            updateVO.setIsSeal(YesNoEnum.N.getValue());
            updateVO.setMatchType(matchType);
            updateVO.setIsMts(isMts);
            String tag = matchId + "_" + playId;
            String key = tag + "_" + uuid;
            producerSendMessageUtils.sendMessage(MqConstant.Topic.RCS_MARKET_TRADE_TYPE, tag, key, updateVO);
        }
    }
}
