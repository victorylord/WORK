package com.panda.sport.rcs.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.dto.I18nItemDTO;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.message.StandardMarketMessage;
import com.panda.merge.dto.message.StandardMarketOddsMessage;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.dto.StandardMarketPlaceDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.utils
 * @Description : Market 工具类
 * @Author : Paca
 * @Date : 2020-07-16 13:51
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
public class MarketUtils {

    /**
     * 是否自动操盘
     *
     * @param tradeType
     * @return
     */
    public static boolean isAuto(Integer tradeType) {
        return TradeEnum.isAuto(tradeType);
    }

    public static StandardMarketDTO toStandardMarketDTO(StandardMarketPlaceDto market) {
        StandardMarketDTO marketDTO = new StandardMarketDTO();
        marketDTO.setMarketCategoryId(market.getMarketCategoryId());
        marketDTO.setChildStandardCategoryId(market.getChildMarketCategoryId());
        marketDTO.setMarketSource(market.getMarketSource());
        marketDTO.setMarketType(market.getMarketType());
//        marketDTO.setTradeType();
        marketDTO.setOddsValue(market.getOddsValue());
        marketDTO.setOddsName(market.getOddsName());
        marketDTO.setOrderType(market.getOrderType());
        marketDTO.setAddition1(market.getAddition1());
        marketDTO.setAddition2(market.getAddition2());
        marketDTO.setAddition3(market.getAddition3());
        marketDTO.setAddition4(market.getAddition4());
        marketDTO.setAddition5(market.getAddition5());
        marketDTO.setDataSourceCode(market.getDataSourceCode());
        marketDTO.setStatus(market.getStatus());
        marketDTO.setThirdMarketSourceStatus(market.getThirdMarketSourceStatus());
        marketDTO.setPlaceNumStatus(market.getPlaceNumStatus());
        marketDTO.setScopeId(market.getScopeId());
        marketDTO.setThirdMarketSourceId(market.getThirdMarketSourceId());
        marketDTO.setRemark(market.getRemark());
        marketDTO.setPlaceNum(market.getPlaceNum());
        marketDTO.setMarketHeadGap(market.getMarketHeadGap2());
        marketDTO.setModifyTime(System.currentTimeMillis());
        marketDTO.setI18nNames(convertI18nNames(market.getI18nNames()));
        List<StandardSportMarketOdds> marketOddsList = market.getMarketOddsList();
        if (CollectionUtils.isNotEmpty(marketOddsList)) {
            List<StandardMarketOddsDTO> list = marketOddsList.stream().map(MarketUtils::toStandardMarketOddsDTO).collect(Collectors.toList());
            marketDTO.setMarketOddsList(list);
        }
        return marketDTO;
    }

    public static StandardMarketDTO toStandardMarketDTO(StandardSportMarket market) {
        StandardMarketDTO marketDTO = new StandardMarketDTO();
        marketDTO.setMarketCategoryId(market.getMarketCategoryId());
        marketDTO.setChildStandardCategoryId(market.getChildMarketCategoryId());
        marketDTO.setMarketSource(market.getMarketSource());
        marketDTO.setMarketType(market.getMarketType());
//        marketDTO.setTradeType();
        marketDTO.setOddsValue(market.getOddsValue());
        marketDTO.setOddsName(market.getOddsName());
        marketDTO.setOrderType(market.getOrderType());
        marketDTO.setAddition1(market.getAddition1());
        marketDTO.setAddition2(market.getAddition2());
        marketDTO.setAddition3(market.getAddition3());
        marketDTO.setAddition4(market.getAddition4());
        marketDTO.setAddition5(market.getAddition5());
        marketDTO.setDataSourceCode(market.getDataSourceCode());
        marketDTO.setStatus(market.getStatus());
        marketDTO.setThirdMarketSourceStatus(market.getThirdMarketSourceStatus());
        marketDTO.setPlaceNumStatus(market.getPlaceNumStatus());
        marketDTO.setEndEdStatus(market.getEndEdStatus());
        marketDTO.setScopeId(market.getScopeId());
        marketDTO.setThirdMarketSourceId(market.getThirdMarketSourceId());
        marketDTO.setRemark(market.getRemark());
        marketDTO.setPlaceNum(market.getPlaceNum());
        marketDTO.setMarketHeadGap(market.getMarketHeadGap2());
        marketDTO.setModifyTime(System.currentTimeMillis());
        marketDTO.setNameCode(market.getNameCode());
        marketDTO.setI18nNames(convertI18nNames(market.getI18nNames()));
        List<StandardSportMarketOdds> marketOddsList = market.getMarketOddsList();
        if (CollectionUtils.isNotEmpty(marketOddsList)) {
            List<StandardMarketOddsDTO> list = marketOddsList.stream().map(MarketUtils::toStandardMarketOddsDTO).collect(Collectors.toList());
            marketDTO.setMarketOddsList(list);
        }
        return marketDTO;
    }

    public static StandardMarketOddsDTO toStandardMarketOddsDTO(StandardSportMarketOdds marketOdds) {
        StandardMarketOddsDTO marketOddsDTO = new StandardMarketOddsDTO();
        marketOddsDTO.setActive(marketOdds.getActive());
        marketOddsDTO.setSettlementResultText(marketOdds.getSettlementResultText());
        marketOddsDTO.setSettlementResult(marketOdds.getSettlementResult());
        marketOddsDTO.setBetSettlementCertainty(marketOdds.getBetSettlementCertainty());
        marketOddsDTO.setOddsType(marketOdds.getOddsType());
        marketOddsDTO.setAddition1(marketOdds.getAddition1());
        marketOddsDTO.setAddition2(marketOdds.getAddition2());
        marketOddsDTO.setAddition3(marketOdds.getAddition3());
        marketOddsDTO.setAddition4(marketOdds.getAddition4());
        marketOddsDTO.setAddition5(marketOdds.getAddition5());
        marketOddsDTO.setName(marketOdds.getName());
        marketOddsDTO.setNameExpressionValue(marketOdds.getNameExpressionValue());
        marketOddsDTO.setMargin(marketOdds.getMargin() != null ? marketOdds.getMargin().doubleValue() : null);
        marketOddsDTO.setMarketDiffValue(marketOdds.getMarketDiffValue() != null ? marketOdds.getMarketDiffValue().doubleValue() : null);
        marketOddsDTO.setOddsValue(marketOdds.getOddsValue());
        marketOddsDTO.setOddsFieldsTemplateId(marketOdds.getOddsFieldsTempletId());
        marketOddsDTO.setOriginalOddsValue(marketOdds.getOriginalOddsValue());
        marketOddsDTO.setTargetSide(marketOdds.getTargetSide());
        marketOddsDTO.setOrderOdds(marketOdds.getOrderOdds());
        marketOddsDTO.setDataSourceCode(marketOdds.getDataSourceCode());
        marketOddsDTO.setThirdOddsFieldSourceId(marketOdds.getThirdOddsFieldSourceId());
        marketOddsDTO.setRemark(marketOdds.getRemark());
        marketOddsDTO.setModifyTime(System.currentTimeMillis());
        marketOddsDTO.setI18nNames(convertI18nNames(marketOdds.getI18nNames()));

        return marketOddsDTO;
    }

    public static List<I18nItemDTO> convertI18nNames(String i18nNames) {
        if (StringUtils.isBlank(i18nNames) || "null".equalsIgnoreCase(i18nNames)) {
            return null;
        }
        try {
            return JsonFormatUtils.fromJsonArray(i18nNames, I18nItemDTO.class);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * 生成盘口值列表，只支持篮球让球盘和大小盘
     *
     * @param playId         玩法ID
     * @param marketCount    最大盘口数
     * @param mainMv         主盘盘口值
     * @param marketNearDiff 相邻盘口值差
     * @return
     */
    public static List<BigDecimal> generateMarketValues(Long playId, int marketCount, BigDecimal mainMv, BigDecimal marketNearDiff) {
        // 全场让球玩法，初始盘口值是0时
        if (Basketball.Main.FULL_TIME.getHandicap().equals(playId) && BigDecimal.ZERO.compareTo(mainMv) == 0) {
            mainMv = RcsConstant.SPECIAL_MARKET_VALUE;
        }
        int count = marketCount + 3;
        // 奇数附加盘
        List<BigDecimal> oddMvList = new ArrayList<>(count);
        // 偶数附加盘
        List<BigDecimal> evenMvList = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            BigDecimal marketDiff = marketNearDiff.multiply(new BigDecimal(i));
            BigDecimal oddMv;
            BigDecimal evenMv;
            if (Basketball.isHandicap(playId)) {
                // 让分
                oddMv = mainMv.subtract(marketDiff).stripTrailingZeros();
                evenMv = mainMv.add(marketDiff).stripTrailingZeros();
            } else {
                // 大小
                oddMv = mainMv.add(marketDiff).stripTrailingZeros();
                evenMv = mainMv.subtract(marketDiff).stripTrailingZeros();
            }
            oddMvList.add(oddMv);
            evenMvList.add(evenMv);
        }

        // 是否存在特值（±0.5、0）标志
        boolean flag = false;
        // 全场让球玩法，初始盘口值是±0.5时，换成0计算
        if (Basketball.Main.FULL_TIME.getHandicap().equals(playId) && isSpecialMv(mainMv)) {
            mainMv = BigDecimal.ZERO;
            flag = true;
        }
        List<BigDecimal> mvList = new ArrayList<>(marketCount + 1);
        mvList.add(mainMv.stripTrailingZeros());
        BigDecimal preOddMv = mainMv;
        BigDecimal preEvenMv = mainMv;
        while (mvList.size() < marketCount + 1) {
            BigDecimal oddMv = getMv(oddMvList, playId, flag);
            if (Basketball.Main.FULL_TIME.getHandicap().equals(playId)) {
                // 前一个值为±1，后一个值不能为0
                if (BigDecimal.ONE.compareTo(preOddMv.abs()) == 0 && BigDecimal.ZERO.compareTo(oddMv) == 0) {
                    oddMv = getMv(oddMvList, playId, flag);
                }
                if (isSpecialMv(oddMv)) {
                    flag = true;
                }
            }
            mvList.add(oddMv);
            preOddMv = oddMv;

            BigDecimal evenMv = getMv(evenMvList, playId, flag);
            if (Basketball.Main.FULL_TIME.getHandicap().equals(playId)) {
                if (BigDecimal.ONE.compareTo(preEvenMv.abs()) == 0 && BigDecimal.ZERO.compareTo(evenMv) == 0) {
                    evenMv = getMv(evenMvList, playId, flag);
                }
                if (isSpecialMv(evenMv)) {
                    flag = true;
                }
            }
            mvList.add(evenMv);
            preEvenMv = evenMv;
        }

        mvList = mvList.subList(0, marketCount);
        log.info("生成盘口值列表：playId={}, marketCount={}, mainMv={}, marketNearDiff={}, mvList={}", playId, marketCount, mainMv, marketNearDiff, mvList);
        return mvList;
    }



    /**
     * 新增生成盘口值列表，网球和乒乓球
     *
     * @param playId         玩法ID
     * @param marketCount    最大盘口数
     * @param mainMv         主盘盘口值
     * @param marketNearDiff 相邻盘口值差
     * @return
     */
    public static List<BigDecimal> generateMarketValuesTennisAndPong(Long playId, int marketCount, BigDecimal mainMv, BigDecimal marketNearDiff) {
        // 全场让球玩法，初始盘口值是0时
        if ((Tennis.pointsPlays(playId) || PingPong.pointsPlays(playId)) && BigDecimal.ZERO.compareTo(mainMv) == 0) {
            mainMv = RcsConstant.SPECIAL_MARKET_VALUE;
        }
        int count = marketCount + 3;
        // 奇数附加盘
        List<BigDecimal> oddMvList = new ArrayList<>(count);
        // 偶数附加盘
        List<BigDecimal> evenMvList = new ArrayList<>(count);
        for (int i = 1; i <= count; i++) {
            BigDecimal marketDiff = marketNearDiff.multiply(new BigDecimal(i));
            BigDecimal oddMv;
            BigDecimal evenMv;
            if (Tennis.pointsPlays(playId) || PingPong.pointsPlays(playId) || IceHockey.pointsPlays(playId)) {
                // 让分
                oddMv = mainMv.subtract(marketDiff).stripTrailingZeros();
                evenMv = mainMv.add(marketDiff).stripTrailingZeros();
            } else {
                // 大小
                oddMv = mainMv.add(marketDiff).stripTrailingZeros();
                evenMv = mainMv.subtract(marketDiff).stripTrailingZeros();
                if(IceHockey.BsPlays(playId) && evenMv.compareTo(BigDecimal.ZERO) < 0){
                    break;
                }
            }
            oddMvList.add(oddMv);
            evenMvList.add(evenMv);
        }

        // 是否存在特值（±0.5、0）标志
        boolean flag = false;
        // 全场让球玩法，初始盘口值是±0.5时，换成0计算
        if ((Tennis.pointsPlays(playId) || PingPong.pointsPlays(playId)) && isSpecialMv(mainMv)) {
            mainMv = BigDecimal.ZERO;
            flag = true;
        }
        List<BigDecimal> mvList = new ArrayList<>(marketCount + 1);
        mvList.add(mainMv.stripTrailingZeros());
        BigDecimal preOddMv = mainMv;
        BigDecimal preEvenMv = mainMv;
        while (mvList.size() < marketCount + 1) {
            BigDecimal oddMv = getTenisAndPongMv(oddMvList, playId, flag);
            if ((Tennis.pointsPlays(playId) || PingPong.pointsPlays(playId))) {
                // 前一个值为±1，后一个值不能为0
                if (BigDecimal.ONE.compareTo(preOddMv.abs()) == 0 && BigDecimal.ZERO.compareTo(oddMv) == 0) {
                    oddMv = getTenisAndPongMv(oddMvList, playId, flag);
                }
                if (isSpecialMv(oddMv)) {
                    flag = true;
                }
            }
            mvList.add(oddMv);
            preOddMv = oddMv;

            BigDecimal evenMv = getTenisAndPongMv(evenMvList, playId, flag);
            if ((Tennis.pointsPlays(playId) || PingPong.pointsPlays(playId))) {
                if (BigDecimal.ONE.compareTo(preEvenMv.abs()) == 0 && BigDecimal.ZERO.compareTo(evenMv) == 0) {
                    evenMv = getTenisAndPongMv(evenMvList, playId, flag);
                }
                if (isSpecialMv(evenMv)) {
                    flag = true;
                }
            }
            mvList.add(evenMv);
            preEvenMv = evenMv;
        }

        mvList = mvList.subList(0, marketCount);
        log.info("生成盘口值列表：playId={}, marketCount={}, mainMv={}, marketNearDiff={}, mvList={}", playId, marketCount, mainMv, marketNearDiff, mvList);
        return mvList;
    }

    /**
     * 大小盘玩法生成盘口值列表
     *
     * @param marketCount    最大盘口数
     * @param mainMv         主盘盘口值
     * @param marketNearDiff 相邻盘口值差
     * @return
     */
    public static List<BigDecimal> generateTotalMvList(int marketCount, BigDecimal mainMv, BigDecimal marketNearDiff) {
        List<BigDecimal> mvList = new ArrayList<>(marketCount * 2 + 1);
        mvList.add(mainMv.stripTrailingZeros());
        if (marketCount == 1) {
            return mvList;
        }
        for (int i = 1; i <= marketCount; i++) {
            BigDecimal marketDiff = marketNearDiff.multiply(new BigDecimal(i));
            // 奇数附加盘
            BigDecimal oddMv = mainMv.add(marketDiff).stripTrailingZeros();
            // 偶数附加盘
            BigDecimal evenMv = mainMv.subtract(marketDiff).stripTrailingZeros();
            mvList.add(oddMv);
            mvList.add(evenMv);
        }
        mvList = mvList.subList(0, marketCount);
        log.info("大小盘玩法生成盘口值列表：marketCount={}, mainMv={}, marketNearDiff={}, mvList={}", marketCount, mainMv, marketNearDiff, mvList);
        return mvList;
    }

    private static BigDecimal getMv(List<BigDecimal> mvList, Long playId, boolean flag) {
        int i = 0;
        while (i++ < 3) {
            BigDecimal mv = mvList.remove(0);
            if (Basketball.Main.FULL_TIME.getHandicap().equals(playId)) {
                if (isSpecialMv(mv)) {
                    if (!flag) {
                        return BigDecimal.ZERO;
                    }
                } else {
                    return mv;
                }
            } else {
                return mv;
            }
        }
        return mvList.remove(0);
    }
    private static BigDecimal getTenisAndPongMv(List<BigDecimal> mvList, Long playId, boolean flag) {
        int i = 0;
        while (i++ < 3) {
            BigDecimal mv = mvList.remove(0);
            if (Tennis.pointsPlays(playId) || PingPong.pointsPlays(playId)) {
                if (isSpecialMv(mv)) {
                    if (!flag) {
                        return BigDecimal.ZERO;
                    }
                } else {
                    return mv;
                }
            } else {
                return mv;
            }
        }
        return mvList.remove(0);
    }

    public static List<Map<String, BigDecimal>> generateMalayOddsList(Long playId, int marketCount, Map<Integer, BigDecimal> placeSpreadMap, BigDecimal marketNearOddsDiff) {
        BigDecimal mainSpread = placeSpreadMap.get(NumberUtils.INTEGER_ONE);
        List<Map<String, BigDecimal>> malayOddsList = new ArrayList<>(marketCount);
        for (int i = 0; i < marketCount; i++) {
            BigDecimal spread = placeSpreadMap.getOrDefault(i + 1, mainSpread);
            // 1 - spread / 2
            BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), 6, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_DOWN);
            BigDecimal homeOdds = malayOdds;
            BigDecimal awayOdds = malayOdds;
            if (i > 0) {
                BigDecimal factor = (i % 2 == 1) ? new BigDecimal(i / 2 + 1) : new BigDecimal(i / 2).negate();
                BigDecimal upOdds = malayOdds.add(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
                homeOdds = OddsConvertUtils.checkMalayOdds(upOdds);
                BigDecimal downOdds = malayOdds.subtract(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
                awayOdds = OddsConvertUtils.checkMalayOdds(downOdds);
            }
            Map<String, BigDecimal> map = Maps.newHashMap();
            map.put(RcsConstant.HOME_POSITION, homeOdds);
            map.put(RcsConstant.AWAY_POSITION, awayOdds);
            malayOddsList.add(map);
        }
        log.info("生成赔率列表：playId={}, marketCount={}, placeSpreadMap={}, marketNearOddsDiff={}, malayOddsList={}", playId, marketCount, placeSpreadMap, marketNearOddsDiff, malayOddsList);
        return malayOddsList;
    }

    private static boolean isSpecialMv(BigDecimal mv) {
        if (mv == null) {
            return false;
        }
        return RcsConstant.SPECIAL_MARKET_VALUE.compareTo(mv.abs()) == 0 || BigDecimal.ZERO.compareTo(mv) == 0;
    }

    /**
     * 是否封盘校验
     * 只要出现0或±0.5的球头，独赢玩法自动封盘
     *
     * @param marketValues
     * @return
     */
    public static boolean isSealCheck(List<BigDecimal> marketValues) {
        if (CollectionUtils.isEmpty(marketValues)) {
            return false;
        }
        return marketValues.stream().anyMatch(MarketUtils::isSpecialMv);
    }

    public static String getNameExpressionValue(String oddsType, BigDecimal marketValue) {
        if (marketValue == null) {
            return null;
        }
        if (OddsTypeEnum.OVER.equalsIgnoreCase(oddsType) || OddsTypeEnum.UNDER.equalsIgnoreCase(oddsType)) {
            // 大小盘
            return marketValue.toPlainString();
        }
        if (OddsTypeEnum.HOME.equals(oddsType)) {
            // 主
            return marketValue.toPlainString();
        }
        if (OddsTypeEnum.AWAY.equals(oddsType)) {
            // 客
            return marketValue.negate().toPlainString();
        }
        return null;
    }

    public static boolean checkMarket(StandardSportMarket market) {
        if (market == null) {
            log.warn("::{}::没有盘口信息",market.getStandardMatchInfoId());
            return false;
        }
        return checkMarket(market, market.getMarketOddsList());
    }

    public static boolean checkMarket(StandardSportMarket market, List<StandardSportMarketOdds> marketOddsList) {
        if (market == null) {
            log.warn("::{}::没有盘口信息",market.getStandardMatchInfoId());
            return false;
        }
        Long matchId = market.getStandardMatchInfoId();
        Long playId = market.getMarketCategoryId();
        Long subPlayId = market.getChildMarketCategoryId();
        Integer placeNum = market.getPlaceNum();
        Long marketId = market.getId();
        Integer sourceStatus = market.getThirdMarketSourceStatus();
        if (!TradeStatusEnum.isOpen(sourceStatus) && !TradeStatusEnum.isSeal(sourceStatus)) {
            log.warn("::{}::盘口三方源状态不是开和封：playId={},subPlayId={},placeNum={},marketId={}", matchId, playId, subPlayId, placeNum, marketId);
            return false;
        }
        if (TradeStatusEnum.isDisable(market.getPaStatus())) {
            log.warn("::{}::盘口已禁用：playId={},subPlayId={},placeNum={},marketId={}", matchId, playId, subPlayId, placeNum, marketId);
            return false;
        }
        if (CollectionUtils.isEmpty(marketOddsList)) {
            log.warn("::{}::盘口没有投注项：playId={},subPlayId={},placeNum={},marketId={}", matchId, playId, subPlayId, placeNum, marketId);
            return false;
        }
        boolean isAllZero = true;
        for (StandardSportMarketOdds marketOdds : marketOddsList) {
            if (marketOdds.getOddsValue() != null && marketOdds.getOddsValue() > 0) {
                isAllZero = false;
            }
        }
        if (isAllZero) {
            log.warn("::{}::盘口所有投注项赔率都是0：playId={},subPlayId={},placeNum={},marketId={}", matchId, playId, subPlayId, placeNum, marketId);
            return false;
        }
        return true;
    }

    /**
     * 构建盘口
     *
     * @param playId      玩法ID
     * @param subPlayId   子玩法ID
     * @param marketType  盘口类型，0-滚球，1-早盘
     * @param placeNum    盘口位置
     * @param marketValue 盘口值
     * @return
     */
    public static StandardMarketDTO buildStandardMarket(Long playId, Long subPlayId, Integer marketType, Integer placeNum, BigDecimal marketValue) {
        StandardMarketDTO market = new StandardMarketDTO();
//        market.setId();
        market.setMarketCategoryId(playId);
        market.setMarketType(marketType);
//        market.setMarketSource();
//        market.setTradeType();
        market.setOddsValue(marketValue.toPlainString());
//        market.setOddsName();
//        market.setOrderType();
        market.setAddition1(marketValue.toPlainString());
        market.setAddition2(null);
        market.setAddition3(null);
        market.setAddition4(null);
        market.setAddition5(null);
        market.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
        market.setStatus(TradeStatusEnum.OPEN.getStatus());
        market.setThirdMarketSourceStatus(TradeStatusEnum.OPEN.getStatus());
        market.setPlaceNumStatus(TradeStatusEnum.OPEN.getStatus());
        market.setScopeId(null);
        market.setThirdMarketSourceId(null);
        market.setRemark(null);
        market.setPlaceNum(placeNum);
        market.setMarketHeadGap(null);
        market.setModifyTime(System.currentTimeMillis());
//        market.setNameCode();
//        market.setI18nNames();
//        market.setLinkageMode();
//        market.setMarketOddsList();
        market.setChildStandardCategoryId(subPlayId);
        return market;
    }

    /**
     * 构建投注项
     *
     * @param oddsType    投注项类型
     * @param oddsValue   赔率，乘以100000后的赔率
     * @param waterDiff   水差
     * @param marketValue 盘口值
     * @return
     */
    public static StandardMarketOddsDTO buildStandardMarketOdds(String oddsType, Integer oddsValue, BigDecimal waterDiff, BigDecimal marketValue) {
        StandardMarketOddsDTO marketOdds = new StandardMarketOddsDTO();
//        marketOdds.setId();
        marketOdds.setThirdSourceActive(1);
        marketOdds.setActive(1);
//        marketOdds.setSettlementResultText();
//        marketOdds.setSettlementResult();
//        marketOdds.setBetSettlementCertainty();
        marketOdds.setOddsType(oddsType);
        marketOdds.setAddition1(null);
        marketOdds.setAddition2(null);
        marketOdds.setAddition3(null);
        marketOdds.setAddition4(null);
        marketOdds.setAddition5(null);
//        marketOdds.setName();
        marketOdds.setNameExpressionValue(getNameExpressionValue(oddsType, marketValue));
        marketOdds.setMargin(null);
        marketOdds.setProbability(null);
        marketOdds.setAnchor(null);
        marketOdds.setMarginProbabilityOdds(null);
        marketOdds.setProbabilityOdds(null);
        if (waterDiff != null) {
            if (OddsTypeEnum.isHomeOddsType(oddsType)) {
                marketOdds.setMarketDiffValue(waterDiff.negate().doubleValue());
            } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
                marketOdds.setMarketDiffValue(waterDiff.doubleValue());
            }
        }
        marketOdds.setOddsValue(oddsValue);
        marketOdds.setOddsFieldsTemplateId(null);
        marketOdds.setOriginalOddsValue(oddsValue);
//        marketOdds.setTargetSide();
        if (OddsTypeEnum.isHomeOddsType(oddsType)) {
            marketOdds.setOrderOdds(1);
        } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
            marketOdds.setOrderOdds(2);
        }
        marketOdds.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
//        marketOdds.setThirdOddsFieldSourceId();
//        marketOdds.setRemark();
        marketOdds.setModifyTime(System.currentTimeMillis());
//        marketOdds.setNameCode();
//        marketOdds.setI18nNames();
        return marketOdds;
    }

    /**
     * 构建盘口
     *
     * @param playId        玩法ID
     * @param subPlayId     子玩法ID
     * @param matchType     0-滚球，1-早盘
     * @param placeNum      位置
     * @param marketValue   盘口值
     * @param marketHeadGap 盘口差
     * @return
     */
    public static StandardMarketDTO buildStandardMarket(Long playId, Long subPlayId, Integer matchType, Integer placeNum, BigDecimal marketValue, BigDecimal marketHeadGap) {
        StandardMarketDTO market = new StandardMarketDTO();
        market.setMarketCategoryId(playId);
        market.setMarketType(matchType);
        if (marketValue != null) {
            market.setOddsValue(marketValue.toPlainString());
            market.setAddition1(marketValue.toPlainString());
        }
        if (Lists.newArrayList(145L, 146L).contains(playId)) {
            long section = subPlayId - playId * 100;
            market.setAddition2(String.valueOf(section));
        }
        market.setAddition3(null);
        market.setAddition4(null);
        market.setAddition5(null);
        market.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
        market.setStatus(TradeStatusEnum.OPEN.getStatus());
        market.setThirdMarketSourceStatus(TradeStatusEnum.OPEN.getStatus());
        market.setPlaceNumStatus(TradeStatusEnum.OPEN.getStatus());
        market.setPlaceNum(placeNum);
        if (marketHeadGap != null) {
            market.setMarketHeadGap(marketHeadGap.doubleValue());
        }
        market.setModifyTime(System.currentTimeMillis());
        market.setChildStandardCategoryId(subPlayId);
        market.setEndEdStatus(0);
        return market;
    }

    /**
     * 构建投注项
     *
     * @param oddsType    投注项类型
     * @param waterDiff   水差
     * @param marketValue 盘口值
     * @return
     */
    public static StandardMarketOddsDTO buildStandardMarketOdds(String oddsType, BigDecimal waterDiff, BigDecimal marketValue) {
        StandardMarketOddsDTO marketOdds = new StandardMarketOddsDTO();
        marketOdds.setThirdSourceActive(1);
        marketOdds.setActive(1);
        marketOdds.setOddsType(oddsType);
        if (marketValue != null) {
            marketOdds.setNameExpressionValue(getNameExpressionValue(oddsType, marketValue));
        }
        if (waterDiff != null) {
            if (OddsTypeEnum.isHomeOddsType(oddsType)) {
                marketOdds.setMarketDiffValue(waterDiff.negate().doubleValue());
            } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
                marketOdds.setMarketDiffValue(waterDiff.doubleValue());
            }
        }
        if (OddsTypeEnum.isHomeOddsType(oddsType)) {
            marketOdds.setOrderOdds(1);
            marketOdds.setTargetSide(RcsConstant.HOME);
        } else if (OddsTypeEnum.isAwayOddsType(oddsType)) {
            marketOdds.setOrderOdds(2);
            marketOdds.setTargetSide(RcsConstant.AWAY);
        }
        marketOdds.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
        marketOdds.setModifyTime(System.currentTimeMillis());
        return marketOdds;
    }

    public static boolean checkMarket(StandardMarketMessage market) {
        if (market == null) {
            log.warn("::{}::没有盘口信息", CommonUtils.getLinkId());
            return false;
        }
        return checkMarket(market, market.getMarketOddsList());
    }

    public static boolean checkMarket(StandardMarketMessage market, List<StandardMarketOddsMessage> marketOddsList) {
        if (market == null) {
            log.warn("::{}::没有盘口信息",CommonUtils.getLinkId());
            return false;
        }
        Long playId = market.getMarketCategoryId();
        Long subPlayId = market.getChildMarketCategoryId();
        Integer placeNum = market.getPlaceNum();
        Long marketId = market.getId();
        Integer sourceStatus = market.getThirdMarketSourceStatus();
        if (!TradeStatusEnum.isOpen(sourceStatus) && !TradeStatusEnum.isSeal(sourceStatus)) {
            log.warn("::{}::盘口三方源状态不是开和封：playId={},subPlayId={},placeNum={},marketId={}",CommonUtils.getLinkId(), playId, subPlayId, placeNum, marketId);
            return false;
        }
        if (TradeStatusEnum.isDisable(market.getPaStatus())) {
            log.warn("::{}::盘口已禁用：playId={},subPlayId={},placeNum={},marketId={}",CommonUtils.getLinkId(), playId, subPlayId, placeNum, marketId);
            return false;
        }
        if (CollectionUtils.isEmpty(marketOddsList)) {
            log.warn("::{}::盘口没有投注项：playId={},subPlayId={},placeNum={},marketId={}",CommonUtils.getLinkId(), playId, subPlayId, placeNum, marketId);
            return false;
        }
        boolean isAllZero = true;
        for (StandardMarketOddsMessage marketOdds : marketOddsList) {
            if (marketOdds.getPaOddsValue() != null && marketOdds.getPaOddsValue() > 0) {
                isAllZero = false;
            }
        }
        if (isAllZero) {
            log.warn("::{}::盘口所有投注项赔率都是0：playId={},subPlayId={},placeNum={},marketId={}",CommonUtils.getLinkId(), playId, subPlayId, placeNum, marketId);
            return false;
        }
        return true;
    }

}
