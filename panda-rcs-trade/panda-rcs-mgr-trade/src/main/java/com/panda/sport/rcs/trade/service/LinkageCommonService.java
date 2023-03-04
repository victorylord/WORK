package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.message.StandardMarketMessage;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.BalanceTypeEnum;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.EventCodeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.pojo.config.MarketBuildPlayConfig;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 联动模式公共服务
 * @Author : Paca
 * @Date : 2021-09-30 19:27
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class LinkageCommonService {

    public static final String MARKET_VALUE = "MARKET_VALUE";
    public static final String ODDS_CHANGE = "ODDS_CHANGE";

    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;

    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private BalanceService balanceService;

    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 生成联动模式主玩法状态标志，用来判断状态是否变更
     *
     * @param market
     * @return
     */
    public String generateStatusSign(StandardMarketMessage market) {
        return String.format("%s,%s,%s,%s", market.getThirdMarketSourceStatus(), market.getPaStatus(), market.getPlaceNumStatus(), market.getStatus());
    }

    /**
     * 参考 {@link #generateStatusSign(StandardMarketMessage)}
     *
     * @param market
     * @return
     */
    public String generateStatusSign(StandardSportMarket market) {
        return String.format("%s,%s,%s,%s", market.getThirdMarketSourceStatus(), market.getPaStatus(), market.getPlaceNumStatus(), market.getStatus());
    }

    /**
     * 生成联动模式主玩法盘口标志，用来判断盘口是否变更
     *
     * @param dataSourceTime
     * @param market
     * @return
     */
    public String generateMarketSign(Long dataSourceTime, StandardMarketMessage market, String linkId) {
        String marketValue = market.getAddition1();
        String marketType = String.valueOf(market.getMarketType());
        String statusSign = generateStatusSign(market);
        return String.format("%s_%s_%s_%s_%s", dataSourceTime, marketValue, statusSign, marketType, linkId);
    }

    /**
     * 参考 {@link #generateMarketSign(Long, StandardMarketMessage, String)}
     *
     * @param dataSourceTime
     * @param market
     * @param linkId
     * @return
     */
    public String generateMarketSign(Long dataSourceTime, StandardSportMarket market, String linkId) {
        String marketValue = market.getAddition1();
        String marketType = String.valueOf(market.getMarketType());
        String statusSign = generateStatusSign(market);
        return String.format("%s_%s_%s_%s_%s", dataSourceTime, marketValue, statusSign, marketType, linkId);
    }

    /**
     * <a href="http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=32369340">联动模式</a>
     * <br/>
     * 通过 让分盘口值 和 大小盘口值 计算 联动玩法的盘口值和赔率偏差值
     *
     * @param matchId        赛事ID
     * @param playId         联动玩法ID
     * @param handicapMainMv 让分盘口值
     * @param totalMainMv    大小盘口值
     * @return
     */
    public Map<String, BigDecimal> calMarketValueAndOddsChange(Long matchId, Long playId, BigDecimal handicapMainMv, BigDecimal totalMainMv) {
        BigDecimal mv1 = totalMainMv.subtract(handicapMainMv).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP).stripTrailingZeros();
        BigDecimal mv2 = totalMainMv.add(handicapMainMv).divide(new BigDecimal("2"), 2, RoundingMode.HALF_UP).stripTrailingZeros();
        // 当前切换L模式的玩法的主盘口值
        BigDecimal currentMainMv;
        // 是否主让
        boolean isHomeHandicap = handicapMainMv.compareTo(BigDecimal.ZERO) <= 0;
        // 是否主队玩法
        boolean isHomePlay = Basketball.Linkage.isTargetHomePlay(playId);
        boolean flag = (isHomeHandicap && isHomePlay) || (!isHomeHandicap && !isHomePlay);
        if (flag) {
            // 让球方取大数
            currentMainMv = mv1.compareTo(mv2) >= 0 ? mv1 : mv2;
        } else {
            // 受让方取小数
            currentMainMv = mv1.compareTo(mv2) <= 0 ? mv1 : mv2;
        }

        // 盘口值不为0.5时，需要特殊处理
        BigDecimal oddsChange = BigDecimal.ZERO;
        int point = currentMainMv.remainder(BigDecimal.ONE).multiply(new BigDecimal("100")).intValue();
        if (point == 0) {
//            currentMainMv = currentMainMv.add(new BigDecimal("0.5"));
//            oddsChange = new BigDecimal("0.08");
        } else if (point == 25) {
            currentMainMv = currentMainMv.add(new BigDecimal("0.25"));
            oddsChange = new BigDecimal("0.04");
        } else if (point == 75) {
            currentMainMv = currentMainMv.subtract(new BigDecimal("0.25"));
            oddsChange = new BigDecimal("-0.04");
        }
        Map<String, BigDecimal> resultMap = Maps.newHashMap();
        resultMap.put(MARKET_VALUE, currentMainMv);
        resultMap.put(ODDS_CHANGE, oddsChange);
        log.info("::{}::计算联动玩法的盘口值和赔率偏差值：playId={},handicapMainMv={},totalMainMv={},resultMap={}", CommonUtil.getRequestId(matchId), playId, handicapMainMv, totalMainMv, resultMap);
        return resultMap;
    }

    /**
     * 获取主盘口信息
     *
     * @param matchId
     * @param linkages
     * @param isCache
     * @return
     */
    public Map<Long, StandardSportMarket> getMainMarketInfo(Long matchId, List<Basketball.Linkage> linkages, boolean isCache) {
        log.info("::{}::获取主盘口信息：linkages={},isCache={}",CommonUtil.getRequestId(matchId), JSON.toJSONString(linkages), isCache);
        Map<Long, StandardSportMarket> resultMap = Maps.newHashMap();
        List<Long> playIds = Lists.newArrayList();
        for (Basketball.Linkage linkage : linkages) {
            Long handicapPlayId = linkage.getHandicap();
            Long totalPlayId = linkage.getTotal();
            StandardSportMarket handicapMarket = getMainMarketInfoFromRedis(matchId, handicapPlayId);
            if (handicapMarket == null) {
                playIds.add(handicapPlayId);
            } else {
                resultMap.put(handicapPlayId, handicapMarket);
            }
            StandardSportMarket totalMarket = getMainMarketInfoFromRedis(matchId, totalPlayId);
            if (totalMarket == null) {
                playIds.add(totalPlayId);
            } else {
                resultMap.put(totalPlayId, totalMarket);
            }
        }
        if (CollectionUtils.isNotEmpty(playIds)) {
            Map<Long, Map<Long, StandardSportMarket>> playMarketMap = standardSportMarketService.listMainMarketInfo(matchId, playIds);
            List<Long> marketIds = Lists.newArrayList();
            playMarketMap.values().forEach(subPlayMap -> subPlayMap.values().forEach(market -> marketIds.add(market.getId())));
            Map<Long, List<StandardSportMarketOdds>> marketOddsMap = standardSportMarketOddsService.listAndGroup(marketIds);
            for (Long playId : playIds) {
                if (!resultMap.containsKey(playId)) {
                    StandardSportMarket market = handleMainMarketInfo(matchId, playId, playId, playMarketMap, marketOddsMap, isCache);
                    if (market != null) {
                        resultMap.put(playId, market);
                    }
                }
            }
        }
        return resultMap;
    }

    /**
     * 获取主盘口信息
     *
     * @param matchId
     * @param linkage
     * @param isCache
     * @return
     */
    public Map<Long, StandardSportMarket> getMainMarketInfo(Long matchId, Basketball.Linkage linkage, boolean isCache) {
        log.info("::{}::获取主盘口信息：matchId={},linkage={},isCache={}",CommonUtil.getRequestId(matchId), JSON.toJSONString(linkage), isCache);
        Long handicapPlayId = linkage.getHandicap();
        Long totalPlayId = linkage.getTotal();
        List<Long> playIds = Lists.newArrayList();
        StandardSportMarket handicapMarket = getMainMarketInfoFromRedis(matchId, handicapPlayId);
        if (handicapMarket == null) {
            playIds.add(handicapPlayId);
        }
        StandardSportMarket totalMarket = getMainMarketInfoFromRedis(matchId, totalPlayId);
        if (totalMarket == null) {
            playIds.add(totalPlayId);
        }
        if (CollectionUtils.isNotEmpty(playIds)) {
            Map<Long, Map<Long, StandardSportMarket>> playMarketMap = standardSportMarketService.listMainMarketInfo(matchId, playIds);
            List<Long> marketIds = Lists.newArrayList();
            playMarketMap.values().forEach(subPlayMap -> subPlayMap.values().forEach(market -> marketIds.add(market.getId())));
            Map<Long, List<StandardSportMarketOdds>> marketOddsMap = standardSportMarketOddsService.listAndGroup(marketIds);
            if (handicapMarket == null) {
                handicapMarket = handleMainMarketInfo(matchId, handicapPlayId, handicapPlayId, playMarketMap, marketOddsMap, isCache);
            }
            if (totalMarket == null) {
                totalMarket = handleMainMarketInfo(matchId, totalPlayId, totalPlayId, playMarketMap, marketOddsMap, isCache);
            }
        }
        Map<Long, StandardSportMarket> resultMap = Maps.newHashMap();
        resultMap.put(handicapPlayId, handicapMarket);
        resultMap.put(totalPlayId, totalMarket);
        return resultMap;
    }

    /**
     * 从数据库查询结果中获取主盘口信息
     *
     * @param matchId       赛事ID
     * @param playId        玩法ID
     * @param subPlayId     子玩法ID
     * @param playMarketMap 盘口信息
     * @param marketOddsMap 投注项信息
     * @param isCache       是否设置缓存，初次需要缓存
     * @return
     */
    private StandardSportMarket handleMainMarketInfo(Long matchId, Long playId, Long subPlayId, Map<Long, Map<Long, StandardSportMarket>> playMarketMap, Map<Long, List<StandardSportMarketOdds>> marketOddsMap, boolean isCache) {
        Map<Long, StandardSportMarket> subPlayMarketMap = playMarketMap.get(playId);
        if (CollectionUtils.isNotEmpty(subPlayMarketMap)) {
            StandardSportMarket market = subPlayMarketMap.get(subPlayId);
            if (market != null) {
                List<StandardSportMarketOdds> marketOddsList = marketOddsMap.get(market.getId());
                if (CollectionUtils.isNotEmpty(marketOddsList)) {
                    market.setMarketOddsList(marketOddsList);
                }
                if (isCache) {
                    String marketSign = generateMarketSign(System.currentTimeMillis(), market, "init");
                    setMarketSignToRedis(matchId, playId, marketSign);
//                    setMarketInfoToRedis(matchId, playId, market);
                }
            }
            return market;
        }
        return null;
    }

    /**
     * 获取联动主玩法（让分玩法、总分玩法）的状态
     *
     * @param matchId
     * @param market
     * @return
     */
    public Integer getMarketStatus(Long matchId, StandardSportMarket market) {
        log.info("::{}::获取联动主玩法状态：matchId={},market={}",CommonUtil.getRequestId(matchId), matchId, JSON.toJSONString(market));
        if (market == null) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        Integer sourceStatus = market.getThirdMarketSourceStatus();
        if (TradeStatusEnum.isClose(sourceStatus) || TradeStatusEnum.isSeal(sourceStatus)) {
            return sourceStatus;
        }
        Integer paStatus = market.getPaStatus();
        if (TradeStatusEnum.isClose(paStatus) || TradeStatusEnum.isSeal(paStatus)) {
            return paStatus;
        }
        if (TradeStatusEnum.isDisable(paStatus)) {
            return TradeStatusEnum.CLOSE.getStatus();
        }
        Integer placeStatus = market.getPlaceNumStatus();
        if (placeStatus == null) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        return placeStatus;
    }

    /**
     * 通过让分玩法状态和总分玩法状态获取联动玩法状态
     *
     * @param matchStatus
     * @param handicapStatus
     * @param totalStatus
     * @return
     */
    public Integer getStatus(Integer matchStatus, Integer handicapStatus, Integer totalStatus) {
        if (!TradeStatusEnum.isOpen(matchStatus)) {
            return matchStatus;
        }
        if (TradeStatusEnum.isClose(handicapStatus) || TradeStatusEnum.isClose(totalStatus)) {
            return TradeStatusEnum.CLOSE.getStatus();
        }
        if (TradeStatusEnum.isSeal(handicapStatus) || TradeStatusEnum.isSeal(totalStatus)) {
            return TradeStatusEnum.SEAL.getStatus();
        }
        if (TradeStatusEnum.isLock(handicapStatus) || TradeStatusEnum.isLock(totalStatus)) {
            return TradeStatusEnum.LOCK.getStatus();
        }
        return TradeStatusEnum.OPEN.getStatus();
    }

    /**
     * 获取联动状态
     *
     * @param matchId
     * @param handicapMainMarket
     * @param totalMainMarket
     * @return
     */
    public Integer getLinkageStatus(Long matchId, StandardSportMarket handicapMainMarket, StandardSportMarket totalMainMarket) {
        Integer handicapStatus = getMarketStatus(matchId, handicapMainMarket);
        Integer totalStatus = getMarketStatus(matchId, totalMainMarket);
        Integer status = TradeStatusEnum.OPEN.getStatus();
        if (TradeStatusEnum.isClose(handicapStatus) || TradeStatusEnum.isClose(totalStatus)) {
            status = TradeStatusEnum.CLOSE.getStatus();
        } else if (TradeStatusEnum.isSeal(handicapStatus) || TradeStatusEnum.isSeal(totalStatus)) {
            status = TradeStatusEnum.SEAL.getStatus();
        } else if (TradeStatusEnum.isLock(handicapStatus) || TradeStatusEnum.isLock(totalStatus)) {
            status = TradeStatusEnum.LOCK.getStatus();
        }
        log.info("::{}::获取联动状态：matchId={},handicapStatus={},totalStatus={},status={}",CommonUtil.getRequestId(matchId), matchId, handicapStatus, totalStatus, status);
        return status;
    }

    /**
     * 获取联动数据源状态
     *
     * @param handicapMainMarket
     * @param totalMainMarket
     * @return
     */
    public Integer getLinkageSourceStatus(StandardSportMarket handicapMainMarket, StandardSportMarket totalMainMarket) {
        Integer sourceStatus;
        if (isSourceClose(handicapMainMarket) || isSourceClose(totalMainMarket)) {
            sourceStatus = TradeStatusEnum.CLOSE.getStatus();
        } else {
            sourceStatus = TradeStatusEnum.OPEN.getStatus();
        }
        log.info("::{}::获取联动数据源状态：sourceStatus=" + sourceStatus,CommonUtil.getRequestId(handicapMainMarket.getStandardMatchInfoId()));
        return sourceStatus;
    }

    private boolean isSourceClose(StandardSportMarket market) {
        if (market == null) {
            return false;
        }
        return TradeStatusEnum.isClose(market.getThirdMarketSourceStatus()) || TradeStatusEnum.isDisable(market.getPaStatus());
    }

    /**
     * 缓存主盘口信息
     *
     * @param matchId
     * @param playId
     * @param market
     */
//    public void setMarketInfoToRedis(Long matchId, Long playId, StandardSportMarket market) {
//        if (market != null) {
//            if (TradeStatusEnum.isClose(market.getThirdMarketSourceStatus()) || TradeStatusEnum.isDisable(market.getPaStatus())) {
//                // 联动主玩法关盘，删除多余信息
//                market.setMarketOddsList(null);
//                delMarketSignFromRedis(matchId, playId);
//            }
//        }
//        String key = RedisKey.getLinkageModeMarketKey(matchId, playId);
//        redisUtils.set(key, JSON.toJSONString(market));
//        redisUtils.expire(key, 180L, TimeUnit.DAYS);
//    }

    /**
     * 参考 {@link #setMarketInfoToRedis(Long, Long, StandardSportMarket)}
     *
     * @param matchId
     * @param playId
     * @param market
     */
//    public void setMarketInfoToRedis(Long matchId, Long playId, StandardMarketMessage market) {
//        if (market != null) {
//            if (TradeStatusEnum.isClose(market.getThirdMarketSourceStatus()) || TradeStatusEnum.isDisable(market.getPaStatus())) {
//                // 联动主玩法关盘，删除多余信息
//                market.setMarketOddsList(null);
//                delMarketSignFromRedis(matchId, playId);
//            }
//        }
//        String key = RedisKey.getLinkageModeMarketKey(matchId, playId);
//        redisUtils.set(key, JSON.toJSONString(market));
//        redisUtils.expire(key, 180L, TimeUnit.DAYS);
//    }

    /**
     * 获取主盘口信息
     *
     * @param matchId
     * @param playId
     * @return
     */
    private StandardSportMarket getMainMarketInfoFromRedis(Long matchId, Long playId) {
        StandardSportMarket market = null;
        String key = RedisKey.MainMarket.getLinkageMainMarketInfoKey(matchId);
        String hashKey = RedisKey.MainMarket.getLinkageMainMarketInfoHashKey(playId, playId);
        String value = redisUtils.hget(key, hashKey);
        log.info("::{}::获取主盘口信息：key={},hashKey={},value={}", CommonUtil.getRequestId(matchId,playId), key, hashKey, value);
        if (StringUtils.isNotBlank(value)) {
            market = JSON.parseObject(value, StandardSportMarket.class);
        }
        return market;
    }

    /**
     * 获取主盘口信息
     *
     * @param matchId
     * @param playId
     * @return
     */
    public StandardMarketMessage getMainMarketInfo(Long matchId, Long playId) {
        String key = RedisKey.MainMarket.getLinkageMainMarketInfoKey(matchId);
        String hashKey = RedisKey.MainMarket.getLinkageMainMarketInfoHashKey(playId, playId);
        String value = redisUtils.hget(key, hashKey);
        log.info("::{}::获取联动主盘口信息：key={},hashKey={},value={}",CommonUtil.getRequestId(matchId,playId), key, hashKey, value);
        if (StringUtils.isNotBlank(value)) {
            return JSON.parseObject(value, StandardMarketMessage.class);
        }
        return null;
    }

    private Integer getMainMarketStatus(StandardMarketMessage market) {
        if (market == null) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        Integer sourceStatus = market.getThirdMarketSourceStatus();
        if (TradeStatusEnum.isClose(sourceStatus) || TradeStatusEnum.isSeal(sourceStatus)) {
            return sourceStatus;
        }
        Integer paStatus = market.getPaStatus();
        if (TradeStatusEnum.isClose(paStatus) || TradeStatusEnum.isSeal(paStatus)) {
            return paStatus;
        }
        if (TradeStatusEnum.isDisable(paStatus)) {
            return TradeStatusEnum.CLOSE.getStatus();
        }
        Integer placeStatus = market.getPlaceNumStatus();
        if (placeStatus == null) {
            return TradeStatusEnum.OPEN.getStatus();
        }
        return placeStatus;
    }

    public Integer getLinkageStatus(Long matchId, StandardMarketMessage handicapMainMarket, StandardMarketMessage totalMainMarket) {
        Long handicapPlayId = handicapMainMarket != null ? handicapMainMarket.getMarketCategoryId() : null;
        Long totalPlayId = totalMainMarket != null ? totalMainMarket.getMarketCategoryId() : null;
        Integer handicapStatus = getMainMarketStatus(handicapMainMarket);
        Integer totalStatus = getMainMarketStatus(totalMainMarket);
        Integer status = TradeStatusEnum.OPEN.getStatus();
        if (TradeStatusEnum.isClose(handicapStatus) || TradeStatusEnum.isClose(totalStatus)) {
            status = TradeStatusEnum.CLOSE.getStatus();
        } else if (TradeStatusEnum.isSeal(handicapStatus) || TradeStatusEnum.isSeal(totalStatus)) {
            status = TradeStatusEnum.SEAL.getStatus();
        } else if (TradeStatusEnum.isLock(handicapStatus) || TradeStatusEnum.isLock(totalStatus)) {
            status = TradeStatusEnum.LOCK.getStatus();
        }
        log.info("::{}::获取联动状态：matchId={},handicapPlayId={},handicapStatus={},totalPlayId={},totalStatus={},status={}",CommonUtil.getRequestId(matchId), matchId, handicapPlayId, handicapStatus, totalPlayId, totalStatus, status);
        return status;
    }

    public Integer getLinkageSourceStatus(Long matchId, StandardMarketMessage handicapMainMarket, StandardMarketMessage totalMainMarket) {
        Long handicapPlayId = handicapMainMarket != null ? handicapMainMarket.getMarketCategoryId() : null;
        Long totalPlayId = totalMainMarket != null ? totalMainMarket.getMarketCategoryId() : null;
        boolean handicapSourceClose = isSourceClose(handicapMainMarket);
        boolean totalSourceClose = isSourceClose(totalMainMarket);
        Integer sourceStatus = (handicapSourceClose || totalSourceClose) ? TradeStatusEnum.CLOSE.getStatus() : TradeStatusEnum.OPEN.getStatus();
        log.info("::{}::获取联动数据源状态：matchId={},handicapPlayId={},handicapSourceClose={},totalPlayId={},totalSourceClose={},sourceStatus={}",CommonUtil.getRequestId(matchId), matchId, handicapPlayId, handicapSourceClose, totalPlayId, totalSourceClose, sourceStatus);
        return sourceStatus;
    }

    private boolean isSourceClose(StandardMarketMessage market) {
        if (market == null) {
            return false;
        }
        return TradeStatusEnum.isClose(market.getThirdMarketSourceStatus()) || TradeStatusEnum.isDisable(market.getPaStatus());
    }

    /**
     * 删除主盘口信息
     *
     * @param matchId
     * @param playId
     */
//    private void delMainMarketInfoFromRedis(Long matchId, Long playId) {
//        String key = RedisKey.getLinkageModeMarketKey(matchId, playId);
//        redisUtils.del(key);
//    }

    /**
     * 缓存盘口标志
     *
     * @param matchId
     * @param playId
     * @param marketSign
     */
    public void setMarketSignToRedis(Long matchId, Long playId, String marketSign) {
        String key = RedisKey.getLinkageModeSignKey(matchId, playId);
        redisUtils.set(key, marketSign);
        redisUtils.expire(key, 7L, TimeUnit.DAYS);
    }

    /**
     * 获取盘口标志
     *
     * @param matchId
     * @param playId
     * @return
     */
    public String getMarketSignFromRedis(Long matchId, Long playId) {
        String key = RedisKey.getLinkageModeSignKey(matchId, playId);
        return redisUtils.get(key);
    }

    /**
     * 删除盘口标志
     *
     * @param matchId
     * @param playId
     */
    public void delMarketSignFromRedis(Long matchId, Long playId) {
        String key = RedisKey.getLinkageModeSignKey(matchId, playId);
        redisUtils.del(key);
    }

    /**
     * 删除链接模式缓存
     *
     * @param matchId
     * @param linkage
     */
    public void delLinkageCache(Long matchId, Basketball.Linkage linkage) {
        if (Basketball.Linkage.getSectionList().contains(linkage)) {
            Basketball.Linkage.getSectionList().forEach(one -> {
                delLinkageCache(matchId, one.getHandicap());
                delLinkageCache(matchId, one.getTotal());
            });
            return;
        }
        delLinkageCache(matchId, linkage.getHandicap());
        delLinkageCache(matchId, linkage.getTotal());
    }

    /**
     * 删除链接模式缓存
     *
     * @param matchId
     * @param playId
     */
    public void delLinkageCache(Long matchId, Long playId) {
        delMarketSignFromRedis(matchId, playId);
//        delMainMarketInfoFromRedis(matchId, playId);
    }

    /**
     * 删除联动模式标志
     *
     * @param matchId
     * @param playId
     */
    public void delLinkageFlag(Long matchId, Long playId) {
        String key = String.format(RedisKey.LINKAGE_SWITCH_FLAG, matchId, playId);
        redisUtils.del(key);
    }

    /**
     * 获取带X玩法构建配置
     *
     * @param matchId
     * @param playId
     * @param subPlayId
     * @param playConfigMap
     * @param marketConfigMap
     * @return
     */
    public BuildMarketConfigDto getBuildMarketConfig(Long matchId, Long playId, Long subPlayId, Map<Long, RcsMatchPlayConfig> playConfigMap, Map<Integer, Map<Long, RcsMatchMarketConfigSub>> marketConfigMap) {
        BuildMarketConfigDto configDto = new BuildMarketConfigDto();
        configDto.setMatchId(matchId);
        configDto.setPlayId(playId);
        // 最大盘口数
        Integer marketCount = 1;
        // 相邻盘口差值
        BigDecimal marketNearDiff = BigDecimal.ONE;
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = new BigDecimal("0.15");
        // 盘口调整幅度
        BigDecimal marketAdjustRange = BigDecimal.ONE;
        // 主盘 spread
        final BigDecimal mainSpread;
        MarketBuildPlayConfig playConfig = rcsMatchMarketConfigMapper.queryMarketBuildPlayConfig(matchId, playId.intValue());
        log.info("::{}::构建盘口，玩法配置：matchId={},playId={},playConfig={}",CommonUtil.getRequestId(matchId,playId), matchId, playId, JSON.toJSONString(playConfig));
        if (playConfig != null) {
            configDto.setMatchType(playConfig.getMatchType());
            configDto.setMarketType(playConfig.getMarketType());
            marketCount = Optional.ofNullable(playConfig.getMarketCount()).orElse(marketCount);
            marketNearDiff = Optional.ofNullable(playConfig.getMarketNearDiff()).orElse(marketNearDiff);
            marketNearOddsDiff = Optional.ofNullable(playConfig.getMarketNearOddsDiff()).orElse(marketNearOddsDiff);
            marketAdjustRange = Optional.ofNullable(playConfig.getMarketAdjustRange()).orElse(marketAdjustRange);
            mainSpread = CommonUtils.toBigDecimal(playConfig.getSpread(), new BigDecimal("0.2"));
        } else {
            mainSpread = new BigDecimal("0.2");
        }
        Map<Integer, BigDecimal> spreadMap = Maps.newHashMap();
        spreadMap.put(NumberUtils.INTEGER_ONE, mainSpread);
        // 盘口差
        BigDecimal marketHeadGap = BigDecimal.ZERO;
        // 位置水差
        Map<Integer, BigDecimal> placeWaterDiffMap = Maps.newHashMap();
        RcsMatchPlayConfig subPlayConfig = playConfigMap.get(subPlayId);
        if (subPlayConfig == null) {
            subPlayConfig = playConfigMap.get(NumberUtils.LONG_MINUS_ONE);
        }
        if (subPlayConfig != null) {
            marketHeadGap = Optional.ofNullable(subPlayConfig.getMarketHeadGap()).orElse(BigDecimal.ZERO);
        }
        if (CollectionUtils.isNotEmpty(marketConfigMap)) {
            boolean isTimeout = isTimeout(matchId);
            marketConfigMap.forEach((placeNum, subPlayMarketMap) -> {
                RcsMatchMarketConfigSub marketConfig = subPlayMarketMap.get(subPlayId);
                if (marketConfig == null) {
                    marketConfig = subPlayMarketMap.get(NumberUtils.LONG_MINUS_ONE);
                }
                if (marketConfig != null) {
                    placeWaterDiffMap.put(placeNum, CommonUtils.toBigDecimal(marketConfig.getAwayAutoChangeRate()));
                    if (isTimeout) {
                        // 比赛暂停取暂停spread
                        spreadMap.put(placeNum, Optional.ofNullable(marketConfig.getTimeOutMargin()).orElse(mainSpread));
                    } else {
                        spreadMap.put(placeNum, Optional.ofNullable(marketConfig.getMargin()).orElse(mainSpread));
                    }
                }
            });
        }
        if (configDto.getMatchType() == null) {
            StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
            if (matchInfo != null) {
                configDto.setMatchType(RcsConstant.isLive(matchInfo) ? 0 : 1);
            }
        }
        configDto.setMarketHeadGap(marketHeadGap);
        configDto.setMarketCount(marketCount);
        configDto.setMarketNearDiff(marketNearDiff);
        configDto.setMarketNearOddsDiff(marketNearOddsDiff);
        configDto.setMarketAdjustRange(marketAdjustRange);
        configDto.setPlaceSpreadMap(spreadMap);
        configDto.setPlaceWaterDiffMap(placeWaterDiffMap);
        log.info("::{}::构建盘口配置：matchId={},playId={},subPlayId={},config={}",CommonUtil.getRequestId(matchId,playId), matchId, subPlayId, playId, JSON.toJSONString(configDto));
        return configDto;
    }

    private boolean isTimeout(Long matchId) {
        String redisKey = String.format("rcs:task:match:event:%s", matchId);
        String eventCode = redisUtils.get(redisKey);
        log.info("::{}::构建盘口，事件编码，是否超时：matchId={},eventCode={}",CommonUtil.getRequestId(matchId), matchId, eventCode);
        return EventCodeEnum.isTimeout(eventCode);
    }

    /**
     * 篮球清除跳盘平衡值、跳赔平衡值、水差
     *
     * @param matchId
     * @param playId
     * @param subPlayId
     * @param linkId
     */
    public void basketballClear(Long matchId, Long playId, Long subPlayId, String linkId) {
        String subPlay = subPlayId == null ? null : subPlayId.toString();
        if (StringUtils.isBlank(linkId)) {
            linkId = CommonUtils.getLinkId();
        }
        String dateExpect = standardMatchInfoService.getMatchDateExpect(matchId);
        // 清除跳盘平衡值
        balanceService.clearAllBalance(BalanceTypeEnum.JUMP_MARKET.getType(), SportIdEnum.BASKETBALL.getId(), matchId, playId, dateExpect, subPlay);
        // 清除跳赔平衡值
        balanceService.clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), SportIdEnum.BASKETBALL.getId(), matchId, playId, dateExpect, subPlay);
        // 清除水差
        ClearSubDTO clearSubDTO = new ClearSubDTO();
        clearSubDTO.setMatchId(matchId);
        clearSubDTO.setPlayId(playId);
        clearSubDTO.setSubPlayId(subPlay);
        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setGlobalId(CommonUtils.getLinkId("linkage"));
        clearDTO.setSportId(SportIdEnum.BASKETBALL.getId());
        clearDTO.setType(0);
        clearDTO.setClearType(7);
        clearDTO.setMatchId(matchId);
        clearDTO.setList(Lists.newArrayList(clearSubDTO));
        producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", linkId, clearDTO.getGlobalId(), clearDTO);
    }

    public void clearBalance(Long sportId, Long matchId, Long playId, Long subPlayId) {
        String subPlay = subPlayId == null ? null : subPlayId.toString();
        String dateExpect = standardMatchInfoService.getMatchDateExpect(matchId);
        // 清除跳盘平衡值
        balanceService.clearAllBalance(BalanceTypeEnum.JUMP_MARKET.getType(), sportId, matchId, playId, dateExpect, subPlay);
        // 清除跳赔平衡值
        balanceService.clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), sportId, matchId, playId, dateExpect, subPlay);
    }

    public void clearWaterDiff(Long sportId, Long matchId, Long playId, Long subPlayId) {
        String subPlay = subPlayId == null ? null : subPlayId.toString();
        // 清除水差
        ClearSubDTO clearSubDTO = new ClearSubDTO();
        clearSubDTO.setMatchId(matchId);
        clearSubDTO.setPlayId(playId);
        clearSubDTO.setSubPlayId(subPlay);
        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setGlobalId(CommonUtils.getLinkId("linkage"));
        clearDTO.setSportId(sportId);
        clearDTO.setType(0);
        clearDTO.setClearType(7);
        clearDTO.setMatchId(matchId);
        clearDTO.setList(Lists.newArrayList(clearSubDTO));
        String tag = String.format("%s_%s_%s", matchId, playId, subPlay);
        producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", tag, clearDTO.getGlobalId(), clearDTO);
    }

    public Integer getMatchStatus(Long sportId, Long matchId, Long playId) {
        Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        if (RcsConstant.isPlaceholderPlay(sportId, playId) && TradeStatusEnum.isOpen(matchStatus)) {
            matchStatus = tradeStatusService.getPlaceholderMainPlayStatusFromRedis(matchId, playId);
        }
        return matchStatus;
    }

    public Integer getMatchStatus(Long matchId) {
        // 赛事状态缓存
        String key = RedisKey.getMatchTradeStatusKey(matchId);
        String value = redisUtils.get(key);
        if (StringUtils.isNotBlank(value)) {
            return NumberUtils.toInt(value);
        }
        return rcsTradeConfigService.getMatchStatus(matchId);
    }
}