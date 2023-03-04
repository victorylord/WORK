package com.panda.sport.rcs.trade.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.trade.wrapper.RcsMatchPlayConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.trade.wrapper.config.RcsMatchMarketConfigSubService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <a href="http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=32369340">联动模式</a>
 *
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 联动模式盘口构建
 * @Author : Paca
 * @Date : 2021-10-03 15:24
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class LinkageMarketBuildService {

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardSportMarketService standardSportMarketService;
    @Autowired
    private RcsMatchPlayConfigService rcsMatchPlayConfigService;
    @Autowired
    private RcsMatchMarketConfigSubService rcsMatchMarketConfigSubService;

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private ApiService apiService;
    @Autowired
    private MarketBuildService marketBuildService;
    @Autowired
    private LinkageCommonService linkageCommonService;

    public boolean sectionPlayLinkageBuildMarket(Long matchId, Long playId, String linkId, boolean isClear) {
        List<Basketball.Linkage> linkages = Basketball.Linkage.getSectionList();
        Map<Long, StandardSportMarket> mainMarketMap = linkageCommonService.getMainMarketInfo(matchId, linkages, true);
        Integer matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
        if (RcsConstant.isPlaceholderPlay(SportIdEnum.BASKETBALL.getId(), playId) && TradeStatusEnum.isOpen(matchStatus)) {
            matchStatus = tradeStatusService.getPlaceholderMainPlayStatusFromRedis(matchId, playId);
        }
        List<StandardMarketDTO> marketList = Lists.newArrayList();
        List<Long> subPlayIds = Lists.newArrayList();
        Map<Long, RcsMatchPlayConfig> playConfigMap = rcsMatchPlayConfigService.getByPlayId(matchId, playId);
        Map<Integer, Map<Long, RcsMatchMarketConfigSub>> marketConfigMap = rcsMatchMarketConfigSubService.getByPlayId(matchId, playId);
        for (Basketball.Linkage linkage : linkages) {
            String addition2 = linkage.getAddition2();
            long subPlayId = playId * 100 + NumberUtils.toInt(addition2);
            if (isClear) {
                // 清除跳盘平衡值、跳赔平衡值、水差
                linkageCommonService.basketballClear(matchId, playId, subPlayId, linkId);
            }
            StandardSportMarket handicapMainMarket = mainMarketMap.get(linkage.getHandicap());
            StandardSportMarket totalMainMarket = mainMarketMap.get(linkage.getTotal());
            if (MarketUtils.checkMarket(handicapMainMarket) && MarketUtils.checkMarket(totalMainMarket)) {
                BigDecimal handicapMainMv = CommonUtils.toBigDecimal(handicapMainMarket.getAddition1());
                BigDecimal totalMainMv = CommonUtils.toBigDecimal(totalMainMarket.getAddition1());
                Map<String, BigDecimal> map = linkageCommonService.calMarketValueAndOddsChange(matchId, playId, handicapMainMv, totalMainMv);
                BigDecimal currentMainMv = map.get(LinkageCommonService.MARKET_VALUE);
                BigDecimal oddsChange = map.get(LinkageCommonService.ODDS_CHANGE);
                BuildMarketConfigDto config = linkageCommonService.getBuildMarketConfig(matchId, playId, subPlayId, playConfigMap, marketConfigMap);
                List<BigDecimal> marketValues = MarketUtils.generateTotalMvList(config.getMarketCount(), currentMainMv, config.getMarketNearDiff());
                List<StandardMarketDTO> marketDTOList = sectionPlayTotalBuild(playId, subPlayId, config, marketValues, false, oddsChange);
                if (CollectionUtils.isNotEmpty(marketDTOList)) {
                    Integer status = linkageCommonService.getLinkageStatus(matchId, handicapMainMarket, totalMainMarket);
                    Integer sourceStatus = linkageCommonService.getLinkageSourceStatus(handicapMainMarket, totalMainMarket);
                    Map<Integer, Integer> placeStatusMap = tradeStatusService.getPlaceStatusFromRedis(SportIdEnum.BASKETBALL.getId(), matchId, playId, subPlayId);
                    for (StandardMarketDTO market : marketDTOList) {
                        market.setAddition2(addition2);
                        market.setChildStandardCategoryId(subPlayId);
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
                    marketList.addAll(marketDTOList);
                }
            } else {
                subPlayIds.add(subPlayId);
            }
        }
        marketBuildService.handleMarketOdds(playId, marketList);
        if (CollectionUtils.isNotEmpty(subPlayIds)) {
            // 不存在赔率时，L联动模式关盘，数据源关盘关盘
            List<StandardSportMarket> standardSportMarkets = standardSportMarketService.queryMarketInfo(matchId, playId);
            if (CollectionUtils.isNotEmpty(standardSportMarkets)) {
                List<StandardMarketDTO> marketDTOList = standardSportMarkets.stream().filter(market -> subPlayIds.contains(market.getChildMarketCategoryId())).map(market -> {
                    StandardMarketDTO marketDTO = MarketUtils.toStandardMarketDTO(market);
                    marketDTO.setThirdMarketSourceStatus(TradeStatusEnum.CLOSE.getStatus());
                    marketDTO.setPlaceNumStatus(TradeStatusEnum.CLOSE.getStatus());
                    marketDTO.setStatus(TradeStatusEnum.CLOSE.getStatus());
                    marketDTO.setRemark("L模式无赔率关盘");
                    return marketDTO;
                }).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(marketDTOList)) {
                    marketList.addAll(marketDTOList);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(marketList)) {
            apiService.putTradeMarketOdds(matchId, marketList, linkId);
            return true;
        }
        return false;
    }

    public List<StandardMarketDTO> sectionPlayTotalBuild(Long playId, Long subPlayId, BuildMarketConfigDto config, List<BigDecimal> marketValues, boolean waterDiffFlag, BigDecimal oddsChange) {
        // 最大盘口数
        Integer marketCount = config.getMarketCount();
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = config.getMarketNearOddsDiff();
        Map<Integer, BigDecimal> placeWaterDiffMap = config.getPlaceWaterDiffMap();
        Map<Integer, BigDecimal> placeSpreadMap = config.getPlaceSpreadMap();
        // 主盘 spread
        BigDecimal mainSpread = placeSpreadMap.get(NumberUtils.INTEGER_ONE);
        String remark = "L模式构建盘口";
        List<StandardMarketDTO> marketList = new ArrayList<>(marketCount);
        for (int placeNum = 1; placeNum <= marketCount; placeNum++) {
            BigDecimal marketValue = marketValues.get(placeNum - 1).stripTrailingZeros();
            BigDecimal spread = placeSpreadMap.getOrDefault(placeNum, mainSpread);
            // 水差 = 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            BigDecimal waterDiff = placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO);
            Map<String, Integer> oddsMap = marketBuildService.calOddsValue(placeNum, spread, waterDiff, waterDiffFlag, marketNearOddsDiff, oddsChange);
            StandardMarketOddsDTO overOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.OVER, oddsMap.get(RcsConstant.HOME_POSITION), waterDiff, marketValue);
            overOdds.setRemark(remark);
            StandardMarketOddsDTO upperOdds = MarketUtils.buildStandardMarketOdds(OddsTypeEnum.UNDER, oddsMap.get(RcsConstant.AWAY_POSITION), waterDiff, marketValue);
            upperOdds.setRemark(remark);
            List<StandardMarketOddsDTO> marketOddsList = Lists.newArrayList(overOdds, upperOdds);

            StandardMarketDTO market = MarketUtils.buildStandardMarket(playId, subPlayId, config.getMatchType(), placeNum, marketValue);
            market.setAddition5(marketValue.toPlainString());
            market.setMarketOddsList(marketOddsList);
            market.setRemark(remark);
            marketList.add(market);
        }
        return marketList;
    }
}