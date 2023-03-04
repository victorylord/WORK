package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.message.StandardMarketMessage;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsMatchPlayConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.trade.wrapper.config.RcsMatchMarketConfigSubService;
import com.panda.sport.rcs.utils.BigDecimalUtils;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 构建盘口服务
 * @Author : Paca
 * @Date : 2021-12-28 21:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class BuildMarketService {

    private static final String HOME_ODDS = "homeOdds";
    private static final String AWAY_ODDS = "awayOdds";
    private static final String HOME_ORIGINAL_ODDS = "homeOriginalOdds";
    private static final String AWAY_ORIGINAL_ODDS = "awayOriginalOdds";

    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private RcsMatchPlayConfigService rcsMatchPlayConfigService;
    @Autowired
    private RcsMatchMarketConfigSubService rcsMatchMarketConfigSubService;

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private LinkageCommonService linkageCommonService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private RedisUtils redisUtils;

    /**
     * 切换L模式构建盘口
     *
     * @param sportId              赛种
     * @param matchId              赛事ID
     * @param playId               玩法ID
     * @param isClearBalance       是否清除平衡值
     * @param isClearWaterDiff     是否清除水差
     * @param isClearMarketHeadGap 是否清除盘口差
     * @param linkId               linkId
     * @return
     */
    public void switchLinkageBuildMarket(Long sportId, Long matchId, Long playId, boolean isClearBalance, boolean isClearWaterDiff, boolean isClearMarketHeadGap, String linkId) {
        if (playId == 145L || playId == 146L) {
            List<StandardMarketDTO> marketList = sectionPlayLinkageBuildMarket(sportId, matchId, playId, isClearBalance, isClearWaterDiff, isClearMarketHeadGap);
            if (CollectionUtils.isNotEmpty(marketList)) {
                apiService.putTradeMarketOdds(matchId, marketList, linkId);
            }
        } else {
            Basketball.Linkage linkage = Basketball.Linkage.getByTargetNormalPlayId(playId);
            if (linkage == null) {
                return;
            }
            List<StandardMarketDTO> marketList = normalPlayLinkageBuildMarket(sportId, matchId, playId, playId, linkage, isClearBalance, isClearWaterDiff, isClearMarketHeadGap);
            if (CollectionUtils.isNotEmpty(marketList)) {
                apiService.putTradeMarketOdds(matchId, marketList, linkId);
            }
        }
    }

    /**
     * L模式构建盘口
     *
     * @param sportId
     * @param matchId
     * @param linkages
     */
    public void linkageBuildMarket(Long sportId, Long matchId, Set<Basketball.Linkage> linkages, boolean pre2live) {
        List<StandardMarketDTO> marketList = Lists.newArrayList();
        Basketball.Linkage sectionLinkage = null;
        for (Basketball.Linkage linkage : linkages) {
            if (Basketball.Linkage.getNormalPlay().contains(linkage)) {
                linkage.getTargetPlayIds().forEach(playId -> {
                    if (tradeStatusService.isLinkage(matchId, playId)) {
                        List<StandardMarketDTO> list = normalPlayLinkageBuildMarket(sportId, matchId, playId, playId, linkage, false, false, false);
                        if (CollectionUtils.isNotEmpty(list)) {
                            marketList.addAll(list);
                        }
                    }
                });
            } else {
                sectionLinkage = linkage;
            }
        }
        if (sectionLinkage != null) {
            sectionLinkage.getTargetPlayIds().forEach(playId -> {
                if (tradeStatusService.isLinkage(matchId, playId)) {
                    List<StandardMarketDTO> list = sectionPlayLinkageBuildMarket(sportId, matchId, playId, false, false, false);
                    if (CollectionUtils.isNotEmpty(list)) {
                        marketList.addAll(list);
                    }
                }
            });
        }
        if (CollectionUtils.isNotEmpty(marketList)) {
            List<StandardMarketDTO> list = marketList;
            if (pre2live) {
                List<Long> playIds = rcsMatchMarketConfigService.queryLinkageSellPlay(matchId, 0);
                log.info("::{}::滚球开售玩法：matchId={},playIds={}", CommonUtil.getRequestId(matchId), playIds);
                list = list.stream().filter(market -> playIds.contains(market.getMarketCategoryId())).collect(Collectors.toList());
            }
            apiService.putTradeMarketOdds(matchId, list, CommonUtils.getLinkId("linkage_mq"));
        }
    }

    /**
     * 常规玩法L模式构建盘口
     *
     * @param sportId              赛种
     * @param matchId              赛事ID
     * @param playId               玩法ID
     * @param subPlayId            子玩法ID
     * @param linkage              联动模式玩法关系
     * @param isClearBalance       是否清除平衡值
     * @param isClearWaterDiff     是否清除水差
     * @param isClearMarketHeadGap 是否清除盘口差
     * @return
     */
    private List<StandardMarketDTO> normalPlayLinkageBuildMarket(Long sportId, Long matchId, Long playId, Long subPlayId, Basketball.Linkage linkage, boolean isClearBalance, boolean isClearWaterDiff, boolean isClearMarketHeadGap) {
        if (isClearBalance) {
            linkageCommonService.clearBalance(sportId, matchId, playId, subPlayId);
        }
        if (isClearWaterDiff) {
            linkageCommonService.clearWaterDiff(sportId, matchId, playId, subPlayId);
        }
        StandardMarketMessage handicapMainMarket = linkageCommonService.getMainMarketInfo(matchId, linkage.getHandicap());
        StandardMarketMessage totalMainMarket = linkageCommonService.getMainMarketInfo(matchId, linkage.getTotal());
        if (MarketUtils.checkMarket(handicapMainMarket) && MarketUtils.checkMarket(totalMainMarket)) {
            Integer matchStatus = getMatchStatus(sportId, matchId, playId);
            BigDecimal handicapMainMv = BigDecimalUtils.toBigDecimal(handicapMainMarket.getAddition1());
            BigDecimal totalMainMv = BigDecimalUtils.toBigDecimal(totalMainMarket.getAddition1());
            Map<String, BigDecimal> map = linkageCommonService.calMarketValueAndOddsChange(matchId, playId, handicapMainMv, totalMainMv);
            BigDecimal currentMainMv = map.get(LinkageCommonService.MARKET_VALUE);
            BigDecimal oddsChange = map.get(LinkageCommonService.ODDS_CHANGE);
            BuildMarketConfigDto config = rcsMatchMarketConfigService.getBuildMarketConfig(matchId, playId);
            if (isClearWaterDiff) {
                config.setPlaceWaterDiffMap(Maps.newHashMap());
            }
            if (isClearMarketHeadGap) {
                config.setMarketHeadGap(BigDecimal.ZERO);
            }
            List<BigDecimal> marketValues = MarketUtils.generateTotalMvList(config.getMarketCount(), currentMainMv, config.getMarketNearDiff());
            List<StandardMarketDTO> marketList = buildTotal(playId, subPlayId, config, marketValues, oddsChange);
            linkageHandleStatus(sportId, matchId, playId, subPlayId, marketList, matchStatus, handicapMainMarket, totalMainMarket);
            return marketList;
        } else {
            // 不存在赔率时，L联动模式关盘，数据源关盘
            List<StandardSportMarket> list = standardSportMarketService.queryMarketInfo(matchId, playId);
            return linkageHandleCloseStatus(list);
        }
    }

    /**
     * 小节玩法L模式构建盘口
     *
     * @param sportId              赛种
     * @param matchId              赛事ID
     * @param playId               玩法ID
     * @param isClearBalance       是否清除平衡值
     * @param isClearWaterDiff     是否清除水差
     * @param isClearMarketHeadGap 是否清除盘口差
     */
    private List<StandardMarketDTO> sectionPlayLinkageBuildMarket(Long sportId, Long matchId, Long playId, boolean isClearBalance, boolean isClearWaterDiff, boolean isClearMarketHeadGap) {
        Integer matchStatus = getMatchStatus(sportId, matchId, playId);
        Map<Long, RcsMatchPlayConfig> playConfigMap = rcsMatchPlayConfigService.getByPlayId(matchId, playId);
        Map<Integer, Map<Long, RcsMatchMarketConfigSub>> marketConfigMap = rcsMatchMarketConfigSubService.getByPlayId(matchId, playId);
        Map<Long, List<StandardSportMarket>> sectionMarketInfoMap = null;
        List<StandardMarketDTO> marketList = Lists.newArrayList();
        for (Basketball.Linkage linkage : Basketball.Linkage.getSectionPlay()) {
            long subPlayId = playId * 100 + NumberUtils.toInt(linkage.getAddition2());
            if (isClearBalance) {
                linkageCommonService.clearBalance(sportId, matchId, playId, subPlayId);
            }
            if (isClearWaterDiff) {
                linkageCommonService.clearWaterDiff(sportId, matchId, playId, subPlayId);
            }
            StandardMarketMessage handicapMainMarket = linkageCommonService.getMainMarketInfo(matchId, linkage.getHandicap());
            StandardMarketMessage totalMainMarket = linkageCommonService.getMainMarketInfo(matchId, linkage.getTotal());
            if (MarketUtils.checkMarket(handicapMainMarket) && MarketUtils.checkMarket(totalMainMarket)) {
                BigDecimal handicapMainMv = BigDecimalUtils.toBigDecimal(handicapMainMarket.getAddition1());
                BigDecimal totalMainMv = BigDecimalUtils.toBigDecimal(totalMainMarket.getAddition1());
                Map<String, BigDecimal> map = linkageCommonService.calMarketValueAndOddsChange(matchId, playId, handicapMainMv, totalMainMv);
                BigDecimal currentMainMv = map.get(LinkageCommonService.MARKET_VALUE);
                BigDecimal oddsChange = map.get(LinkageCommonService.ODDS_CHANGE);
                BuildMarketConfigDto config = linkageCommonService.getBuildMarketConfig(matchId, playId, subPlayId, playConfigMap, marketConfigMap);
                if (isClearWaterDiff) {
                    config.setPlaceWaterDiffMap(Maps.newHashMap());
                }
                if (isClearMarketHeadGap) {
                    config.setMarketHeadGap(BigDecimal.ZERO);
                }
                List<BigDecimal> marketValues = MarketUtils.generateTotalMvList(config.getMarketCount(), currentMainMv, config.getMarketNearDiff());
                List<StandardMarketDTO> list = buildTotal(playId, subPlayId, config, marketValues, oddsChange);
                linkageHandleStatus(sportId, matchId, playId, subPlayId, list, matchStatus, handicapMainMarket, totalMainMarket);
                if (CollectionUtils.isNotEmpty(list)) {
                    marketList.addAll(list);
                }
            } else {
                // 不存在赔率时，L联动模式关盘，数据源关盘
                if (sectionMarketInfoMap == null) {
                    sectionMarketInfoMap = querySectionMarketInfo(matchId, playId);
                }
                if (CollectionUtils.isNotEmpty(sectionMarketInfoMap)) {
                    List<StandardSportMarket> subPlayList = sectionMarketInfoMap.get(subPlayId);
                    List<StandardMarketDTO> list = linkageHandleCloseStatus(subPlayList);
                    if (CollectionUtils.isNotEmpty(list)) {
                        marketList.addAll(list);
                    }
                }
            }
        }
        return marketList;
    }

    /**
     * 构建大小盘
     *
     * @param playId       玩法ID
     * @param subPlayId    子玩法ID
     * @param config       盘口构建配置
     * @param marketValues 盘口值集合
     * @param oddsChange   赔率修正值
     * @return
     */
    private List<StandardMarketDTO> buildTotal(Long playId, Long subPlayId, BuildMarketConfigDto config, List<BigDecimal> marketValues, BigDecimal oddsChange) {
        // 最大盘口数
        Integer marketCount = config.getMarketCount();
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = config.getMarketNearOddsDiff();
        // 盘口差（如不计算盘口差，需要清空）
        BigDecimal marketHeadGap = config.getMarketHeadGap();
        // 位置水差（如不计算水差，需要清空）
        Map<Integer, BigDecimal> placeWaterDiffMap = config.getPlaceWaterDiffMap();
        // 位置spread
        Map<Integer, BigDecimal> placeSpreadMap = config.getPlaceSpreadMap();
        // 主盘 spread
        BigDecimal mainSpread = placeSpreadMap.get(NumberUtils.INTEGER_ONE);
        Map<Integer, Long> oddsFieldsTemplateIdMap = standardSportMarketService.getOddsFieldsTemplateId(playId);
        List<StandardMarketDTO> marketList = Lists.newArrayList();
        for (int placeNum = 1; placeNum <= marketCount; placeNum++) {
            // 原始盘口值
            BigDecimal originalMarketValue = marketValues.get(placeNum - 1);
            // 盘口值
            BigDecimal marketValue = BigDecimalUtils.ROUND_HALF_UP_2.add(originalMarketValue, marketHeadGap).stripTrailingZeros();
            BigDecimal spread = placeSpreadMap.getOrDefault(placeNum, mainSpread);
            BigDecimal waterDiff = placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO);
            Map<String, Integer> oddsValueMap = calOddsValue(placeNum, spread, waterDiff, marketNearOddsDiff, oddsChange);

            // 主队赔率
            StandardMarketOddsDTO overOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.OVER, waterDiff, marketValue);
            overOdds.setOddsValue(oddsValueMap.get(HOME_ODDS));
            overOdds.setOriginalOddsValue(oddsValueMap.get(HOME_ORIGINAL_ODDS));

            // 客队赔率
            StandardMarketOddsDTO upperOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.UNDER, waterDiff, marketValue);
            upperOdds.setOddsValue(oddsValueMap.get(AWAY_ODDS));
            upperOdds.setOriginalOddsValue(oddsValueMap.get(AWAY_ORIGINAL_ODDS));

            if (CollectionUtils.isNotEmpty(oddsFieldsTemplateIdMap)) {
                overOdds.setOddsFieldsTemplateId(oddsFieldsTemplateIdMap.get(overOdds.getOrderOdds()));
                upperOdds.setOddsFieldsTemplateId(oddsFieldsTemplateIdMap.get(upperOdds.getOrderOdds()));
            }

            StandardMarketDTO market = MarketUtils.buildStandardMarket(playId, subPlayId, config.getMatchType(), placeNum, marketValue, marketHeadGap);
            market.setAddition5(originalMarketValue.toPlainString());
            market.setMarketOddsList(Lists.newArrayList(overOdds, upperOdds));
            marketList.add(market);
        }
        return marketList;
    }

    /**
     * 构建单双盘
     *
     * @param playId
     * @param subPlayId
     * @param config
     * @return
     */
    private List<StandardMarketDTO> buildOddEven(Long playId, Long subPlayId, BuildMarketConfigDto config) {
        // 位置水差（如不计算水差，需要清空）
        Map<Integer, BigDecimal> placeWaterDiffMap = config.getPlaceWaterDiffMap();
        // 位置spread
        Map<Integer, BigDecimal> placeSpreadMap = config.getPlaceSpreadMap();
        // 主盘 spread
        BigDecimal mainSpread = placeSpreadMap.get(NumberUtils.INTEGER_ONE);
        Map<Integer, Long> oddsFieldsTemplateIdMap = standardSportMarketService.getOddsFieldsTemplateId(playId);
        Integer placeNum = 1;
        BigDecimal spread = placeSpreadMap.getOrDefault(placeNum, mainSpread);
        BigDecimal waterDiff = placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO);
        Map<String, Integer> oddsValueMap = calOddsValue(placeNum, spread, waterDiff, BigDecimal.ZERO, BigDecimal.ZERO);

        // 主队赔率
        StandardMarketOddsDTO homeOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.ODD, waterDiff, null);
        homeOdds.setOddsValue(oddsValueMap.get(HOME_ODDS));
        homeOdds.setOriginalOddsValue(oddsValueMap.get(HOME_ORIGINAL_ODDS));

        // 客队赔率
        StandardMarketOddsDTO awayOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.EVEN, waterDiff, null);
        awayOdds.setOddsValue(oddsValueMap.get(AWAY_ODDS));
        awayOdds.setOriginalOddsValue(oddsValueMap.get(AWAY_ORIGINAL_ODDS));

        if (CollectionUtils.isNotEmpty(oddsFieldsTemplateIdMap)) {
            homeOdds.setOddsFieldsTemplateId(oddsFieldsTemplateIdMap.get(homeOdds.getOrderOdds()));
            awayOdds.setOddsFieldsTemplateId(oddsFieldsTemplateIdMap.get(awayOdds.getOrderOdds()));
        }

        StandardMarketDTO market = MarketUtils.buildStandardMarket(playId, subPlayId, config.getMatchType(), placeNum, null, null);
        market.setMarketOddsList(Lists.newArrayList(homeOdds, awayOdds));

        return Lists.newArrayList(market);
    }

    /**
     * 计算100000倍欧赔
     *
     * @param placeNum           位置
     * @param spread             位置spread
     * @param waterDiff          位置水差
     * @param marketNearOddsDiff 相邻盘口赔率差
     * @param oddsChange         赔率修正值
     * @return
     */
    private Map<String, Integer> calOddsValue(final int placeNum, final BigDecimal spread, final BigDecimal waterDiff, final BigDecimal marketNearOddsDiff, final BigDecimal oddsChange) {
        // 1 - ( spread / 2 )
        BigDecimal myOdds = BigDecimalUtils.calMyOddsBySpread(spread);
        BigDecimal homeOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.add(myOdds, oddsChange));
        BigDecimal awayOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.subtract(myOdds, oddsChange));
        if (placeNum > 1) {
            // 相邻盘口赔率差，等差数列计算
            BigDecimal factor = (placeNum % 2 == 0) ? new BigDecimal(placeNum / 2) : new BigDecimal(placeNum / 2).negate();
            BigDecimal factorOdds = BigDecimalUtils.ROUND_HALF_UP_2.multiply(factor, marketNearOddsDiff);
            homeOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.add(homeOdds, factorOdds));
            awayOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.subtract(awayOdds, factorOdds));
        }
        Map<String, Integer> map = Maps.newHashMap();
        map.put(HOME_ORIGINAL_ODDS, rcsOddsConvertMappingService.myOddsToOddsValue(homeOdds));
        map.put(AWAY_ORIGINAL_ODDS, rcsOddsConvertMappingService.myOddsToOddsValue(awayOdds));
        // 位置水差计算，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
        homeOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.subtract(homeOdds, waterDiff));
        awayOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.add(awayOdds, waterDiff));
        map.put(HOME_ODDS, rcsOddsConvertMappingService.myOddsToOddsValue(homeOdds));
        map.put(AWAY_ODDS, rcsOddsConvertMappingService.myOddsToOddsValue(awayOdds));
        return map;
    }

    /**
     * 获取赛事状态
     *
     * @param sportId
     * @param matchId
     * @param playId
     * @return
     */
    private Integer getMatchStatus(Long sportId, Long matchId, Long playId) {
        // 赛事状态缓存
        String key = RedisKey.getMatchTradeStatusKey(matchId);
        String value = redisUtils.get(key);
        if (StringUtils.isNotBlank(value)) {
            return NumberUtils.toInt(value);
        }
        Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        if (RcsConstant.isPlaceholderPlay(sportId, playId) && TradeStatusEnum.isOpen(matchStatus)) {
            matchStatus = tradeStatusService.getPlaceholderMainPlayStatusFromRedis(matchId, playId);
        }
        return matchStatus;
    }

    private void linkageHandleStatus(Long sportId, Long matchId, Long playId, Long subPlayId, List<StandardMarketDTO> marketList, Integer matchStatus, StandardMarketMessage handicapMainMarket, StandardMarketMessage totalMainMarket) {
        if (CollectionUtils.isEmpty(marketList)) {
            return;
        }
        Integer status = linkageCommonService.getLinkageStatus(matchId, handicapMainMarket, totalMainMarket);
        Integer sourceStatus = linkageCommonService.getLinkageSourceStatus(matchId, handicapMainMarket, totalMainMarket);
        Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(sportId, matchId, playId, subPlayId);
        marketList.forEach(market -> {
            Integer placeStatus = placeStatusMap.getOrDefault(market.getPlaceNum(), TradeStatusEnum.OPEN.getStatus());
            market.setThirdMarketSourceStatus(sourceStatus);
            market.setPlaceNumStatus(placeStatus);
            market.setStatus(placeStatus);
            if (!TradeStatusEnum.isOpen(matchStatus)) {
                market.setStatus(matchStatus);
            } else {
                // 状态为开时需要联动
                if (TradeStatusEnum.isOpen(placeStatus)) {
                    market.setPlaceNumStatus(status);
                    market.setStatus(status);
                }
            }
        });
    }

    private List<StandardMarketDTO> linkageHandleCloseStatus(List<StandardSportMarket> list) {
        if (CollectionUtils.isEmpty(list)) {
            return null;
        }
        return list.stream().map(market -> {
            StandardMarketDTO marketDTO = MarketUtils.toStandardMarketDTO(market);
            marketDTO.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
            marketDTO.setPlaceNumStatus(TradeStatusEnum.CLOSE.getStatus());
            marketDTO.setStatus(TradeStatusEnum.CLOSE.getStatus());
            marketDTO.setRemark("L模式无赔率关盘");
            return marketDTO;
        }).collect(Collectors.toList());
    }

    private Map<Long, List<StandardSportMarket>> querySectionMarketInfo(Long matchId, Long playId) {
        List<StandardSportMarket> list = standardSportMarketService.queryMarketInfo(matchId, playId);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, List<StandardSportMarket>> resultMap = list.stream().collect(Collectors.groupingBy(StandardSportMarket::getChildMarketCategoryId));
        log.info("::{}::小节玩法盘口信息：" + JSON.toJSONString(resultMap),CommonUtil.getRequestId(matchId));
        return resultMap;
    }

}
