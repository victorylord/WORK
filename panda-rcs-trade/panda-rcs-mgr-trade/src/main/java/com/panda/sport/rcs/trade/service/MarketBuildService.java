package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.common.enums.DataSourceCodeEnum;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.message.StandardMarketMessage;
import com.panda.merge.dto.message.StandardMarketOddsMessage;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsOddsConvertMappingMyMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.pojo.config.BuildMarketPlaceConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketOddsService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.BigDecimalUtils;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 盘口构建
 * @Author : Paca
 * @Date : 2021-03-19 20:13
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class MarketBuildService {

    private static final int ODDS_SCALE = 2;

    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private StandardSportMarketOddsService standardSportMarketOddsService;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private LinkageCommonService linkageCommonService;
    @Autowired
    private LinkageMarketBuildService linkageMarketBuildService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private RcsOddsConvertMappingMyMapper rcsOddsConvertMappingMyMapper;
    private Map<Double, String> oddsMap;
    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;

    public boolean linkageBuildMarket(Long matchId, Long playId, String linkId, boolean isClear) {
        if (playId == 145L || playId == 146L) {
            return linkageMarketBuildService.sectionPlayLinkageBuildMarket(matchId, playId, linkId, isClear);
        }
        Basketball.Linkage linkage = Basketball.Linkage.getByTargetPlayId(playId);
        if (linkage == null) {
            return false;
        }
        if (isClear) {
            // 清除跳盘平衡值、跳赔平衡值、水差
            linkageCommonService.basketballClear(matchId, playId, null, linkId);
        }
        Map<Long, StandardSportMarket> mainMarketInfoMap = linkageCommonService.getMainMarketInfo(matchId, linkage, true);
        StandardSportMarket handicapMainMarket = mainMarketInfoMap.get(linkage.getHandicap());
        StandardSportMarket totalMainMarket = mainMarketInfoMap.get(linkage.getTotal());
        List<StandardMarketDTO> marketDTOList;
        if (MarketUtils.checkMarket(handicapMainMarket) && MarketUtils.checkMarket(totalMainMarket)) {
            Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
            marketDTOList = linkageBuildMarket(matchId, playId, handicapMainMarket, totalMainMarket, matchStatus);
        } else {
            // 不存在赔率时，L联动模式关盘，数据源关盘关盘
            List<StandardSportMarket> marketList = standardSportMarketService.queryMarketInfo(matchId, playId);
            if (CollectionUtils.isEmpty(marketList)) {
                return false;
            }
            marketDTOList = marketList.stream().map(market -> {
                StandardMarketDTO marketDTO = MarketUtils.toStandardMarketDTO(market);
                marketDTO.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
                marketDTO.setPlaceNumStatus(TradeStatusEnum.CLOSE.getStatus());
                marketDTO.setStatus(TradeStatusEnum.CLOSE.getStatus());
                marketDTO.setRemark("L模式无赔率关盘");
                return marketDTO;
            }).collect(Collectors.toList());
        }
        if (CollectionUtils.isNotEmpty(marketDTOList)) {
            apiService.putTradeMarketOdds(matchId, marketDTOList, linkId);
            return true;
        }
        return false;
    }

    public void playerBuildMarket(Long matchId, Long playId, String linkId) {
        log.info("::{}::球员玩法切换M模式：matchId={},playId={}",linkId, matchId, playId);
        List<StandardSportMarket> marketList = standardSportMarketService.queryMarketInfo(matchId, playId);
        if (CollectionUtils.isEmpty(marketList)) {
            log.warn("::{}::未查询到盘口信息：matchId={},playId={}",linkId, matchId, playId);
            return;
        }
        Set<Long> marketIdList = marketList.stream().filter(market -> NumberUtils.INTEGER_ONE.equals(market.getPlaceNum())).map(StandardSportMarket::getId).collect(Collectors.toSet());
        Map<Long, List<StandardSportMarketOdds>> marketOddsGroupMap = standardSportMarketOddsService.listAndGroup(marketIdList);
        marketList = marketList.stream().peek(market -> {
            List<StandardSportMarketOdds> marketOddsList = marketOddsGroupMap.get(market.getId());
            market.setMarketOddsList(marketOddsList);
        }).filter(MarketUtils::checkMarket).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(marketList)) {
            log.warn("::{}::未查询到盘口信息：matchId={},playId={}",linkId, matchId, playId);
            return;
        }
        Map<Long, RcsMatchMarketConfigSub> subPlayConfigMap = rcsMatchMarketConfigService.getSubPlayConfig(matchId, playId, 1);
        RcsMatchMarketConfigSub defaultConfigSub = subPlayConfigMap.get(-1L);
        List<StandardMarketDTO> marketDTOList = marketList.stream().map(market -> {
            Long subPlayId = market.getChildMarketCategoryId();
            BigDecimal spread = new BigDecimal("0.2");
            // 水差 = 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            final BigDecimal waterDiff;
            RcsMatchMarketConfigSub configSub = subPlayConfigMap.getOrDefault(subPlayId, defaultConfigSub);
            if (configSub != null) {
                if (configSub.getMargin() != null) {
                    spread = configSub.getMargin();
                }
                waterDiff = CommonUtils.toBigDecimal(configSub.getAwayAutoChangeRate(), BigDecimal.ZERO);
            } else {
                waterDiff = BigDecimal.ZERO;
            }
            BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), ODDS_SCALE, RoundingMode.HALF_UP)).setScale(ODDS_SCALE, RoundingMode.DOWN);
            BigDecimal homeOdds = OddsConvertUtils.checkMalayOdds(malayOdds.subtract(waterDiff));
            BigDecimal awayOdds = OddsConvertUtils.checkMalayOdds(malayOdds.add(waterDiff));
            // 转换上游入参
            List<StandardMarketOddsDTO> marketOddsDTOList = market.getMarketOddsList().stream().map(marketOdds -> {
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
                // 原始赔率
                marketOddsDTO.setOriginalOddsValue(marketOddsDTO.getOddsValue());
                marketOddsDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                return marketOddsDTO;
            }).collect(Collectors.toList());
            StandardMarketDTO marketDTO = MarketUtils.toStandardMarketDTO(market);
            marketDTO.setMarketOddsList(marketOddsDTOList);
            marketDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            return marketDTO;
        }).collect(Collectors.toList());
        tradeStatusService.handlePushStatus(SportIdEnum.BASKETBALL.getId(), matchId, playId, marketDTOList, null, TradeEnum.MANUAD.getCode(), 0, 1, 0);
        if (CollectionUtils.isNotEmpty(marketDTOList)) {
            apiService.putTradeMarketOdds(matchId, marketDTOList, linkId);
        }
    }

    public List<StandardMarketDTO> linkageBuildMarket(Long matchId, Long playId, StandardSportMarket handicapMainMarket, StandardSportMarket totalMainMarket, Integer matchStatus) {
        BigDecimal handicapMainMv = CommonUtils.toBigDecimal(handicapMainMarket.getAddition1());
        BigDecimal totalMainMv = CommonUtils.toBigDecimal(totalMainMarket.getAddition1());
        Map<String, BigDecimal> map = linkageCommonService.calMarketValueAndOddsChange(matchId, playId, handicapMainMv, totalMainMv);
        BigDecimal currentMainMv = map.get(LinkageCommonService.MARKET_VALUE);
        BigDecimal oddsChange = map.get(LinkageCommonService.ODDS_CHANGE);
        BuildMarketConfigDto config = rcsMatchMarketConfigService.getBuildMarketConfig(matchId, playId);
        List<BigDecimal> marketValues = MarketUtils.generateTotalMvList(config.getMarketCount(), currentMainMv, config.getMarketNearDiff());
        // 将 大小玩法ID 改成 球队玩法ID
        totalMainMarket.setMarketCategoryId(playId);
        // 盘口差设为 0
        totalMainMarket.setMarketHeadGap(null);
        List<StandardMarketDTO> marketDTOList = buildTotal(totalMainMarket, config, marketValues, false, oddsChange);
        if (CollectionUtils.isNotEmpty(marketDTOList)) {
            if (matchStatus == null) {
                matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
            }
            Integer status = linkageCommonService.getLinkageStatus(matchId, handicapMainMarket, totalMainMarket);
            Integer sourceStatus = linkageCommonService.getLinkageSourceStatus(handicapMainMarket, totalMainMarket);
            Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.BASKETBALL.getId(), matchId, playId, null);
            for (StandardMarketDTO market : marketDTOList) {
                Integer placeNum = market.getPlaceNum();
                Integer placeStatus = placeStatusMap.getOrDefault(placeNum, TradeStatusEnum.OPEN.getStatus());
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
            }
        }
        handleMarketOdds(playId, marketDTOList);
        return marketDTOList;
    }

    /**
     * 构建大小玩法类盘口
     *
     * @param mainMarket    主盘口信息，包括投注项信息
     * @param config        盘口构建配置
     * @param marketValues  盘口值集合
     * @param waterDiffFlag 是否计算水差标志
     * @param oddsChange    赔率微调值，马来赔，上盘加上该值，下盘减去该值
     * @return
     */
    private List<StandardMarketDTO> buildTotal(StandardSportMarket mainMarket, BuildMarketConfigDto config, List<BigDecimal> marketValues, boolean waterDiffFlag, BigDecimal oddsChange) {
        List<StandardSportMarketOdds> mainMarketOddsList = mainMarket.getMarketOddsList();
        // 最大盘口数
        Integer marketCount = config.getMarketCount();
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = config.getMarketNearOddsDiff();
        Map<Integer, BigDecimal> placeWaterDiffMap = config.getPlaceWaterDiffMap();
        Map<Integer, BigDecimal> placeSpreadMap = config.getPlaceSpreadMap();
        // 主盘 spread
        BigDecimal mainSpread = placeSpreadMap.get(NumberUtils.INTEGER_ONE);
        List<StandardMarketDTO> marketList = new ArrayList<>(marketCount);
        for (int placeNum = 1; placeNum <= marketCount; placeNum++) {
            BigDecimal marketValue = marketValues.get(placeNum - 1).stripTrailingZeros();
            BigDecimal spread = placeSpreadMap.getOrDefault(placeNum, mainSpread);
            // 水差 = 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            BigDecimal waterDiff = placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO);
            Map<String, Integer> oddsMap = calOddsValue(placeNum, spread, waterDiff, waterDiffFlag, marketNearOddsDiff, oddsChange);
            List<StandardMarketOddsDTO> marketOddsDTOList = null;
            if (CollectionUtils.isNotEmpty(mainMarketOddsList)) {
                marketOddsDTOList = new ArrayList<>(mainMarketOddsList.size());
                for (StandardSportMarketOdds marketOdds : mainMarketOddsList) {
                    String oddsType = marketOdds.getOddsType();
                    StandardMarketOddsDTO marketOddsDTO = MarketUtils.toStandardMarketOddsDTO(marketOdds);
                    marketOddsDTO.setActive(1);
                    marketOddsDTO.setMarketDiffValue(null);
                    if (OddsTypeEnum.isHomeOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(oddsMap.get(RcsConstant.HOME_POSITION));
                        marketOddsDTO.setMarketDiffValue(waterDiff.negate().doubleValue());
                    } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(oddsMap.get(RcsConstant.AWAY_POSITION));
                        marketOddsDTO.setMarketDiffValue(waterDiff.doubleValue());
                    }
                    // 原始赔率
                    marketOddsDTO.setOriginalOddsValue(marketOddsDTO.getOddsValue());
                    marketOddsDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                    marketOddsDTO.setNameExpressionValue(MarketUtils.getNameExpressionValue(oddsType, marketValue));
                    marketOddsDTO.setRemark("切换模式构建盘口");
                    marketOddsDTOList.add(marketOddsDTO);
                }
            }

            StandardMarketDTO standardMarketDTO = MarketUtils.toStandardMarketDTO(mainMarket);
            standardMarketDTO.setMarketOddsList(marketOddsDTOList);
            if (config.getMatchType() != null) {
                standardMarketDTO.setMarketType(config.getMatchType());
            }
            standardMarketDTO.setPlaceNum(placeNum);
            standardMarketDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            standardMarketDTO.setOddsValue(marketValue.toPlainString());
            standardMarketDTO.setAddition1(marketValue.toPlainString());
            standardMarketDTO.setAddition5(null);
            standardMarketDTO.setRemark("切换模式构建盘口");
            marketList.add(standardMarketDTO);
        }
        return marketList;
    }

    public Map<String, Integer> calOddsValue(int placeNum, BigDecimal spread, BigDecimal waterDiff, boolean waterDiffFlag, BigDecimal marketNearOddsDiff, BigDecimal oddsChange) {
        // 1 - ( spread / 2 )
        BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), ODDS_SCALE, RoundingMode.HALF_UP)).setScale(ODDS_SCALE, RoundingMode.DOWN);
        BigDecimal homeOdds = malayOdds.add(oddsChange);
        homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds);
        BigDecimal awayOdds = malayOdds.subtract(oddsChange);
        awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds);
        if (placeNum > 1) {
            BigDecimal factor = (placeNum % 2 == 0) ? new BigDecimal(placeNum / 2) : new BigDecimal(placeNum / 2).negate();
            homeOdds = homeOdds.add(factor.multiply(marketNearOddsDiff)).setScale(ODDS_SCALE, RoundingMode.DOWN);
            homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds);
            awayOdds = awayOdds.subtract(factor.multiply(marketNearOddsDiff)).setScale(ODDS_SCALE, RoundingMode.DOWN);
            awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds);
        }
        if (waterDiffFlag) {
            // 水差 = 玩法水差 + 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds.subtract(waterDiff));
            awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds.add(waterDiff));
        }
        Map<String, Integer> map = Maps.newHashMap();
        map.put(RcsConstant.HOME_POSITION, malayOddsToOddsValue(homeOdds));
        map.put(RcsConstant.AWAY_POSITION, malayOddsToOddsValue(awayOdds));
        return map;
    }

    private int malayOddsToOddsValue(BigDecimal malayOdds) {
        BigDecimal euOdds = new BigDecimal(rcsOddsConvertMappingService.getEUOdds(malayOdds.toPlainString()));
        return euOdds.multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
    }

    public void handleMarketOdds(Long playId, List<StandardMarketDTO> marketList) {
        if (CollectionUtils.isEmpty(marketList)) {
            return;
        }
        List<StandardMarketOddsDTO> list = standardSportMarketMapper.selectOddsFieldsTempletId(playId);
        if (CollectionUtils.isEmpty(list)) {
            return;
        }
        marketList.forEach(marketDTO -> {
            List<StandardMarketOddsDTO> marketOddsList = marketDTO.getMarketOddsList();
            if (CollectionUtils.isEmpty(marketOddsList)) {
                return;
            }
            marketOddsList.forEach(marketOdds -> {
                marketOdds.setMarketDiffValue(null);
                if (OddsTypeEnum.isHomeOddsType(marketOdds.getOddsType())) {
                    marketOdds.setOrderOdds(1);
                    if (list.size() > 0) {
                        marketOdds.setOddsFieldsTemplateId(list.get(0).getOddsFieldsTemplateId());
                    }
                } else if (OddsTypeEnum.isAwayOddsType(marketOdds.getOddsType())) {
                    marketOdds.setOrderOdds(2);
                    if (list.size() > 1) {
                        marketOdds.setOddsFieldsTemplateId(list.get(1).getOddsFieldsTemplateId());
                    }
                }
            });
        });
    }

    public List<StandardMarketDTO> footballBuildMarket(Long matchId, Long playId, List<StandardMarketDTO> marketList, RcsMatchMarketConfig config, BigDecimal spread) {
        if (CollectionUtils.isEmpty(marketList)) {
            log.warn("::{}::盘口列表为空",matchId);
            return marketList;
        }
        StandardMarketDTO mainMarket = marketList.get(0);
        if (!CommonUtils.isNumber(mainMarket.getAddition1())) {
            log.warn("::{}::盘口值不是数字",matchId);
            return marketList;
        }
        Long subPlayId = mainMarket.getChildStandardCategoryId();
        BigDecimal mainMv = new BigDecimal(mainMarket.getAddition1()).stripTrailingZeros();
        Integer marketType = mainMarket.getMarketType();
        List<StandardMarketOddsDTO> mainMarketOddsList = mainMarket.getMarketOddsList();
        RcsTournamentTemplatePlayMargain buildConfig = config.getBuildConfig();
        Integer marketCount = Optional.ofNullable(buildConfig.getMarketCount()).orElse(1);
        BigDecimal manualMarketNearDiff = Optional.ofNullable(buildConfig.getManualMarketNearDiff()).orElse(new BigDecimal("0.25"));
        BigDecimal manualMarketNearOddsDiff = Optional.ofNullable(buildConfig.getManualMarketNearOddsDiff()).orElse(new BigDecimal("0.25"));
        // 1 - ( spread / 2 )
        BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), ODDS_SCALE, BigDecimal.ROUND_DOWN));
        BigDecimal[] sm = spread.divideAndRemainder(new BigDecimal("0.02"));
        Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.FOOTBALL.getId(), matchId, playId, subPlayId);
        Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        List<StandardMarketDTO> marketDTOList = new ArrayList<>(marketCount);
        for (int i = 0; i < marketCount; i++) {
            int placeNum = i + 1;
            Integer placeStatus = placeStatusMap.getOrDefault(placeNum, TradeStatusEnum.OPEN.getStatus());
            StandardMarketDTO marketDTO;
            BigDecimal marketValue;
            BigDecimal homeOdds;
            BigDecimal awayOdds;
            if (i == 0) {
                marketDTO = mainMarket;
                marketValue = mainMv;
                homeOdds = malayOdds;
//                awayOdds = MarginUtils.caluOddsBySpread(malayOdds,spread);
//                awayOdds = malayOdds;
            } else {
                marketDTO = JSON.parseObject(JSON.toJSONString(mainMarket), StandardMarketDTO.class);
                marketValue = mainMv.add(manualMarketNearDiff.multiply(new BigDecimal(i))).stripTrailingZeros();
                homeOdds = malayOdds.add(manualMarketNearOddsDiff.multiply(new BigDecimal(i)));
//                awayOdds = malayOdds.subtract(manualMarketNearOddsDiff.multiply(new BigDecimal(i)));
//                awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds);
            }
            if (Lists.newArrayList(4, 19, 113, 121, 128, 130, 306, 308).contains(config.getPlayId().intValue()) && marketValue.doubleValue() < 0 && sm[1].doubleValue() > 0) {
                homeOdds = homeOdds.add(new BigDecimal("0.01"));
            }
            homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds);
            awayOdds = MarginUtils.caluOddsBySpread(homeOdds, spread);

            List<StandardMarketOddsDTO> marketOddsDTOList = null;
            if (CollectionUtils.isNotEmpty(mainMarketOddsList)) {
                marketOddsDTOList = new ArrayList<>(mainMarketOddsList.size());
                for (StandardMarketOddsDTO marketOdds : mainMarketOddsList) {
                    StandardMarketOddsDTO marketOddsDTO = JSON.parseObject(JSON.toJSONString(marketOdds), StandardMarketOddsDTO.class);
                    String oddsType = marketOddsDTO.getOddsType();
                    marketOddsDTO.setActive(1);
                    marketOddsDTO.setMarketDiffValue(null);
                    if (OddsTypeEnum.isHomeOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(homeOdds));
                    } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(awayOdds));
                    }
//                    // 处理单数抽水问题
//                    if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(oddsType) && marketValue.doubleValue() > 0) {
//                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(awayOdds));
//                    } else{
//                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(homeOdds));
//                    }
                    // 原始赔率
                    marketOddsDTO.setOriginalOddsValue(marketOddsDTO.getOddsValue());
                    marketOddsDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                    marketOddsDTO.setNameExpressionValue(MarketUtils.getNameExpressionValue(oddsType, marketValue));
                    marketOddsDTO.setRemark("足球构建附加盘");
                    marketOddsDTOList.add(marketOddsDTO);
                }
            }

            marketDTO.setOddsValue(marketValue.toPlainString());
            marketDTO.setAddition1(marketValue.toPlainString());
            if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(playId.intValue())) {
                // 足球基准分玩法
                if (marketType != null && marketType == 0) {
                    // 滚球
                    String[] scores = config.getScore().split(":");
                    if(334 != playId.intValue()){
                        String addition2 = marketValue.add(CommonUtils.toBigDecimal(scores[1])).subtract(CommonUtils.toBigDecimal(scores[0])).stripTrailingZeros().toPlainString();
                        marketDTO.setAddition2(addition2);
                    }else{
                        marketDTO.setAddition2(marketValue.toPlainString());
                    }
                    marketDTO.setAddition3(scores[0]);
                    marketDTO.setAddition4(scores[1]);
                } else {
                    // 早盘
                    marketDTO.setAddition2(marketValue.toPlainString());
                    marketDTO.setAddition3("0");
                    marketDTO.setAddition4("0");
                }
            }
            marketDTO.setPlaceNum(placeNum);
            marketDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            marketDTO.setThirdMarketSourceStatus(TradeStatusEnum.OPEN.getStatus());
            marketDTO.setPlaceNumStatus(placeStatus);
            if (TradeStatusEnum.isOpen(matchStatus)) {
                marketDTO.setStatus(placeStatus);
            } else {
                marketDTO.setStatus(matchStatus);
            }
            marketDTO.setRemark("足球构建附加盘");
            marketDTO.setModifyTime(System.currentTimeMillis());
            marketDTO.setMarketOddsList(marketOddsDTOList);
            marketDTOList.add(marketDTO);
        }
        return marketDTOList;
    }

    public void autoPlusBuildMarket(Long matchId, Long playId, String linkId, BuildMarketConfigDto config) {
        String key = RedisKey.MainMarket.getAutoPlusMainMarketInfoKey(matchId);
        String hashKey = RedisKey.MainMarket.getAutoPlusMainMarketInfoHashKey(playId, playId);
        String value = redisUtils.hget(key, hashKey);
        log.info("::{}::获取A+模式数据源主盘口信息：key={},hashKey={},value={}",linkId, key, hashKey, value);
        if (StringUtils.isBlank(value)) {
            log.info("::{}::未获取A+模式数据源主盘口信息：key={},hashKey={},value={}",linkId, matchId, playId, linkId);
            return;
        }
        BuildMarketConfigDto configDb = rcsMatchMarketConfigService.getBuildMarketConfig(matchId, playId);
        config.setMarketHeadGap(configDb.getMarketHeadGap());
        config.setPlaceWaterDiffMap(configDb.getPlaceWaterDiffMap());
        StandardMarketMessage market = JSON.parseObject(value, StandardMarketMessage.class);
        Long subPlayId = market.getChildMarketCategoryId();
        List<StandardMarketOddsMessage> marketOddsList = market.getMarketOddsList();
        market.setMarketOddsList(null);
        Map<Integer, BigDecimal> placeWaterDiffMap = config.getPlaceWaterDiffMap();
        Map<Integer, BigDecimal> placeSpreadMap = config.getPlaceSpreadMap();
        // 数据源主盘口值
        BigDecimal mainMv = new BigDecimal(market.getAddition1()).stripTrailingZeros();
        Integer marketCount = config.getMarketCount();
        // 相邻盘口差值
        BigDecimal marketNearDiff = config.getMarketNearDiff();
        // 主盘口值 + 盘口差
        BigDecimal newMainMv = getNewMainMv(playId, mainMv, config.getMarketHeadGap().stripTrailingZeros(), config.getMarketAdjustRange().stripTrailingZeros());
        List<BigDecimal> marketValueList = MarketUtils.generateMarketValues(playId, marketCount, newMainMv, marketNearDiff);
        List<Map<String, BigDecimal>> malayOddsList = MarketUtils.generateMalayOddsList(playId, marketCount, placeSpreadMap, config.getMarketNearOddsDiff());
        for (int i = 0; i < malayOddsList.size(); i++) {
            int placeNum = i + 1;
            Map<String, BigDecimal> malayOddsMap = malayOddsList.get(i);
            // 水差 = 位置水差，上盘赔率减水差，下盘赔率加水差
            BigDecimal waterDiff = placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO);
            BigDecimal homeOdds = malayOddsMap.get(RcsConstant.HOME_POSITION).subtract(waterDiff);
            BigDecimal awayOdds = malayOddsMap.get(RcsConstant.AWAY_POSITION).add(waterDiff);
            malayOddsMap.put(RcsConstant.HOME_POSITION, OddsConvertUtils.checkMalayOdds(homeOdds));
            malayOddsMap.put(RcsConstant.AWAY_POSITION, OddsConvertUtils.checkMalayOdds(awayOdds));
            malayOddsMap.put("home_market_diff_value", waterDiff.negate());
            malayOddsMap.put("away_market_diff_value", waterDiff);
        }
        log.info("::{}::赔率计算水差后：{}",linkId, malayOddsList);
        Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.BASKETBALL.getId(), matchId, playId, subPlayId);
        List<StandardMarketDTO> marketList = new ArrayList<>(marketCount);
        for (int i = 0; i < marketCount; i++) {
            int placeNum = i + 1;
            Integer placeStatus = placeStatusMap.getOrDefault(placeNum, TradeStatusEnum.OPEN.getStatus());
            BigDecimal marketValue = marketValueList.get(i).stripTrailingZeros();
            Map<String, BigDecimal> malayOddsMap = malayOddsList.get(i);
            StandardMarketDTO newMarket = JSON.parseObject(JSON.toJSONString(market), StandardMarketDTO.class);
            newMarket.setOddsValue(marketValue.toPlainString());
            newMarket.setAddition1(marketValue.toPlainString());
            if (RcsConstant.BENCHMARK_SCORE.contains(playId)) {
                // 基准分
                newMarket.setAddition2(marketValue.toPlainString());
            }
            if (Basketball.Main.getHandicapPlayIds().contains(playId)) {
                newMarket.setAddition5(market.getAddition1());
            }
            newMarket.setPlaceNum(placeNum);
            newMarket.setMarketHeadGap(config.getMarketHeadGap().doubleValue());
            Integer status = market.getStatus();
            if (TradeStatusEnum.isOpen(market.getThirdMarketSourceStatus())) {
                status = placeStatusMap.getOrDefault(placeNum, status);
            }
            newMarket.setStatus(status);
            newMarket.setPlaceNumStatus(placeStatus);
            newMarket.setThirdMarketSourceStatus(market.getThirdMarketSourceStatus());
            newMarket.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            log.info("::{}::A+模式构建盘口(让分-大小),盘口Id:={},玩法Id:={},placeNum:={},盘口状态:={}","数据商盘口源状态:={}",
                    linkId, market.getId(), market.getMarketCategoryId(),placeNum, status,newMarket.getThirdMarketSourceStatus());

            if (CollectionUtils.isNotEmpty(marketOddsList)) {
                List<StandardMarketOddsDTO> newMarketOddsList = new ArrayList<>(marketOddsList.size());
                for (StandardMarketOddsMessage marketOdds : marketOddsList) {
                    String oddsType = marketOdds.getOddsType();
                    StandardMarketOddsDTO marketOddsDTO = JSON.parseObject(JSON.toJSONString(marketOdds), StandardMarketOddsDTO.class);
                    marketOddsDTO.setMarketDiffValue(null);
                    marketOddsDTO.setNameExpressionValue(MarketUtils.getNameExpressionValue(oddsType, marketValue));
                    if (OddsTypeEnum.isHomeOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(malayOddsMap.get(RcsConstant.HOME_POSITION)));
                        BigDecimal marketDiffValue = malayOddsMap.get("home_market_diff_value");
                        if (marketDiffValue != null) {
                            marketOddsDTO.setMarketDiffValue(marketDiffValue.doubleValue());
                        }
                    } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
                        marketOddsDTO.setOddsValue(malayOddsToOddsValue(malayOddsMap.get(RcsConstant.AWAY_POSITION)));
                        BigDecimal marketDiffValue = malayOddsMap.get("away_market_diff_value");
                        if (marketDiffValue != null) {
                            marketOddsDTO.setMarketDiffValue(marketDiffValue.doubleValue());
                        }
                    }
                    marketOddsDTO.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                    newMarketOddsList.add(marketOddsDTO);
                }
                newMarket.setMarketOddsList(newMarketOddsList);
            } else {
                newMarket.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
            }
            marketList.add(newMarket);
        }
        // 调用融合RPC接口
        apiService.putTradeMarketOdds(matchId, marketList, linkId + "_APlus");

        if (Basketball.Main.getHandicapPlayIds().contains(playId) && MarketUtils.isSealCheck(marketValueList)) {
            // 让球玩法只要出现0或±0.5的球头，独赢玩法自动封盘，独赢玩法盘口级封盘
            basketballWinSeal(matchId, Basketball.Main.getWinAloneByHandicap(playId), linkId);
        }
    }

    /**
     * 构建单双A+模式盘口
     * @param matchId
     * @param playId
     * @param linkId
     * @param config
     */
    public void buildMarketSingleOperList(Long matchId, Long playId, String linkId, BuildMarketConfigDto config) {
        String key = RedisKey.MainMarket.getAutoPlusMainMarketInfoKey(matchId);
        String hashKey = RedisKey.MainMarket.getAutoPlusMainMarketInfoHashKey(playId, playId);
        String value = redisUtils.hget(key, hashKey);
        log.info("::{}::获取A+模式数据源主盘口信息：key={},hashKey={},value={}",linkId, key, hashKey, value);
        if (StringUtils.isBlank(value)) {
            log.info("::{}::未获取A+模式数据源主盘口信息：key={},hashKey={},value={}",linkId, matchId, playId, linkId);
            return;
        }

        // 位置spread
        Map<Integer, BigDecimal> placeSpreadMap = Maps.newHashMap();
        placeSpreadMap.put(NumberUtils.INTEGER_ONE, RcsConstant.DEFAULT_SPREAD);
        // 位置水差
        Map<Integer, BigDecimal> placeWaterDiffMap = Maps.newHashMap();
        placeWaterDiffMap.put(NumberUtils.INTEGER_ONE, BigDecimal.ZERO);

        List<BuildMarketPlaceConfig> placeConfigList = rcsMatchMarketConfigMapper.getBuildMarketPlaceConfig(matchId, playId);
        log.info("::{}::A+模式构建盘口，实时位置配置：matchId={},playId={},placeConfigList={}",linkId, matchId, playId, JSON.toJSONString(placeConfigList));
        if (CollectionUtils.isNotEmpty(placeConfigList)) {
            placeConfigList.forEach(placeConfig -> {
                placeWaterDiffMap.put(placeConfig.getPlaceNum(), placeConfig.getPlaceWaterDiff());
                log.info("::{}::A+模式构建盘口，位置水差：matchId={},playId={},placeSpreadMap={}",linkId, matchId, playId, JSON.toJSONString(placeWaterDiffMap));
            });
        }

        //设置位置水差
        config.setPlaceWaterDiffMap(placeWaterDiffMap);
        StandardMarketMessage market = JSON.parseObject(value, StandardMarketMessage.class);
        log.info("::{}::A+模式构建盘口，数据源主盘口信息：matchId={},playId={},market={}",linkId, matchId, playId, JSON.toJSONString(market));

        //子玩法ID
        Long subPlayId = market.getChildMarketCategoryId();
        //盘口投注项
        List<StandardMarketOddsMessage> marketOddsList = market.getMarketOddsList();
        //单双玩法固定一个盘口
        int placeNum = 1;
        // 构建盘口基本信息
        StandardMarketDTO standardMarketDTO = MarketUtils.buildStandardMarket(playId, playId, config.getMatchType(), placeNum, null, null);
        // 位置状态，默认开
        Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.BASKETBALL.getId(), matchId, playId, subPlayId);
        //操盘状态
        Integer placeStatus = placeStatusMap.getOrDefault(placeNum, TradeStatusEnum.OPEN.getStatus());
        standardMarketDTO.setPlaceNumStatus(placeStatus);
        //三方盘口源状态
        Integer sourceStatus = market.getThirdMarketSourceStatus();
        standardMarketDTO.setThirdMarketSourceStatus(sourceStatus);
        // 水差 = 位置水差，上盘赔率减水差，下盘赔率加水差
        BigDecimal waterDiff = config.getPlaceWaterDiffMap().getOrDefault(NumberUtils.INTEGER_ONE, BigDecimal.ZERO);
        // 主盘 spread
        BigDecimal spread = config.getPlaceSpreadMap().get(NumberUtils.INTEGER_ONE);
        //盘口状态状态判断
        if(!TradeStatusEnum.isOpen(sourceStatus)){
            standardMarketDTO.setStatus(sourceStatus);
        }else {
            standardMarketDTO.setStatus(placeStatus);
        }
        log.info("::{}::A+模式构建盘口(单双),盘口Id:={},玩法Id:={},placeNum:={},盘口状态:={}","数据商盘口源状态:={}",
                linkId, market.getId(), market.getMarketCategoryId(),placeNum, standardMarketDTO.getStatus(),standardMarketDTO.getThirdMarketSourceStatus());

        List<StandardMarketDTO> marketList = Lists.newArrayList();

        if (CollectionUtils.isNotEmpty(marketOddsList)) {
            // 标准投注项模板ID，重要，否则前端不能投注
            Map<String, Long> oddsFieldsTemplateIdMap = marketOddsList.stream().collect(Collectors.toMap(StandardMarketOddsMessage::getOddsType, StandardMarketOddsMessage::getOddsFieldsTemplateId));
            // 计算初始赔率
            // 1 - ( spread / 2 )
            BigDecimal myOdds = BigDecimalUtils.calMyOddsBySpread(spread);

            // 位置水差计算，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            BigDecimal homeOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.subtract(myOdds, waterDiff));
            BigDecimal awayOdds = OddsConvertUtils.checkMalayOdds(BigDecimalUtils.ROUND_DOWN_2.add(myOdds, waterDiff));

            // 构建上盘赔率信息
            StandardMarketOddsDTO homeMarketOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.ODD, waterDiff, null);
            homeMarketOdds.setOddsValue(getConvertedOdds(homeOdds));
            homeMarketOdds.setOriginalOddsValue(getConvertedOdds(myOdds));
            homeMarketOdds.setOddsFieldsTemplateId(oddsFieldsTemplateIdMap.get(OddsTypeEnum.ODD));

            // 构建下盘赔率信息
            StandardMarketOddsDTO awayMarketOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.EVEN, waterDiff, null);
            awayMarketOdds.setOddsValue(getConvertedOdds(awayOdds));
            awayMarketOdds.setOriginalOddsValue(getConvertedOdds(myOdds));
            awayMarketOdds.setOddsFieldsTemplateId(oddsFieldsTemplateIdMap.get(OddsTypeEnum.EVEN));
            standardMarketDTO.setMarketOddsList(Lists.newArrayList(homeMarketOdds, awayMarketOdds));
            log.info("::{}::计算后的盘口信息marketOddsList：{}", linkId,standardMarketDTO.getMarketOddsList());
        }else {
            standardMarketDTO.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
        }
        marketList.add(standardMarketDTO);

        // 调用融合RPC接口
        apiService.putTradeMarketOdds(matchId, marketList, linkId + "_APlus");
    }

    private int getConvertedOdds(BigDecimal malayOdds) {
        malayOdds = OddsConvertUtils.checkMalayOdds(malayOdds);
        if (MapUtils.isEmpty(oddsMap)) {
            QueryWrapper<RcsOddsConvertMappingMy> queryWrapper = new QueryWrapper<>();
            List<RcsOddsConvertMappingMy> list = rcsOddsConvertMappingMyMapper.selectList(queryWrapper);
            oddsMap = list.stream().collect(Collectors.toMap(e -> Double.parseDouble(e.getMalaysia()), RcsOddsConvertMappingMy::getEurope));
        }
        String fieldOddsValue = oddsMap.get(malayOdds.doubleValue());
        if (StringUtils.isBlank(fieldOddsValue)) {
            fieldOddsValue = NumberUtils.INTEGER_ZERO.toString();
        }
        return new BigDecimal(fieldOddsValue).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
    }

    private void basketballWinSeal(Long matchId, Long winPlayId, String linkId) {
        // 独赢玩法盘口级封盘
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("tradeLevel", TradeLevelEnum.MARKET.getLevel());
        jsonObject.put("sportId", SportIdEnum.BASKETBALL.getId());
        jsonObject.put("matchId", matchId);
        jsonObject.put("playId", winPlayId);
        jsonObject.put("placeNum", NumberUtils.INTEGER_ONE);
        jsonObject.put("status", TradeStatusEnum.SEAL.getStatus());
        jsonObject.put("linkedType", LinkedTypeEnum.AUTO_PLUS.getCode());
        jsonObject.put("remark", "让球出现0或±0.5的球头，独赢封盘");
        Request<JSONObject> request = new Request<>();
        request.setData(jsonObject);
        request.setLinkId(linkId + "_APlus");
        request.setDataSourceTime(System.currentTimeMillis());
        producerSendMessageUtils.sendMessage("RCS_TRADE_UPDATE_MARKET_STATUS", matchId + "_APlus", request.getLinkId(), request);
    }

    private BigDecimal getNewMainMv(Long playId, BigDecimal mainMv, BigDecimal marketHeadGap, BigDecimal marketAdjustRange) {
        if (marketHeadGap.compareTo(BigDecimal.ZERO) == 0) {
            // 盘口差为0
            return mainMv;
        }
        // 主盘口值 + 盘口差
        BigDecimal newMainMv = mainMv.add(marketHeadGap).stripTrailingZeros();
        if (!Basketball.Main.FULL_TIME.getHandicap().equals(playId)) {
            // 不是全场让分
            return newMainMv;
        }

        if (marketAdjustRange.compareTo(RcsConstant.SPECIAL_MARKET_VALUE) == 0) {
            // 盘口调整幅度 0.5
            if (newMainMv.compareTo(BigDecimal.ZERO) == 0) {
                newMainMv = mainMv.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.ONE.negate() : BigDecimal.ONE;
            } else {
                if (mainMv.multiply(newMainMv).compareTo(BigDecimal.ZERO) < 0 &&
                        newMainMv.abs().compareTo(RcsConstant.SPECIAL_MARKET_VALUE) == 0) {
                    newMainMv = newMainMv.compareTo(BigDecimal.ZERO) > 0 ? newMainMv.add(RcsConstant.SPECIAL_MARKET_VALUE) : newMainMv.subtract(RcsConstant.SPECIAL_MARKET_VALUE);
                }
            }
        } else {
            if (mainMv.multiply(newMainMv).compareTo(BigDecimal.ZERO) < 0 &&
                    newMainMv.abs().compareTo(RcsConstant.SPECIAL_MARKET_VALUE) == 0) {
                // 全场让球，球头改变，且改变后球头是±0.5
                if (isInteger(marketAdjustRange)) {
                    newMainMv = newMainMv.compareTo(BigDecimal.ZERO) > 0 ? newMainMv.add(BigDecimal.ONE) : newMainMv.subtract(BigDecimal.ONE);
                } else {
                    newMainMv = newMainMv.compareTo(BigDecimal.ZERO) > 0 ? newMainMv.add(RcsConstant.SPECIAL_MARKET_VALUE) : newMainMv.subtract(RcsConstant.SPECIAL_MARKET_VALUE);
                }
            }
        }
        return newMainMv;
    }

    private boolean isInteger(BigDecimal value) {
        try {
            // 1 2 3 4 5 6 ...
            value.intValueExact();
            return true;
        } catch (Exception e) {
            // 0.5 1.5 2.5 3.5 4.5 5.5 6.5 ...
            return false;
        }
    }
}