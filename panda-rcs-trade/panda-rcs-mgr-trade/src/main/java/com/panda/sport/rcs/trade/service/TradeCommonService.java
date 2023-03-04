package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.google.common.collect.Lists;
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
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.DataSourceTypeEnum;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketProbabilityConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
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
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import com.panda.sport.rcs.vo.OddsValueVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
public class TradeCommonService {
    @Autowired
    MarketStatusService marketStatusService;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketOddsApi tradeMarketOddsApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private IOutrightTradeConfigApi outrightTradeConfigApi;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private MatchTradeConfigServiceImpl matchTradeConfigService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    StandardSportMarketService standardSportMarketService;
    @Autowired
    private MarketBuildService marketBuildService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private OddsRangeService oddsRangeService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    /**
     * @return void
     * @Description //计算赔率
     * @Param [oddsList, config]
     * @Author sean
     * @Date 2021/2/4
     **/
    public void caluFootBallOddsBySpread(List<StandardMarketOddsDTO> oddsList, RcsMatchMarketConfig config) {
        log.info("::{}::,caluFootBallOddsBySpread start oddsList={},config={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(oddsList), JSONObject.toJSONString(config));
        if (CollectionUtils.isNotEmpty(oddsList) && StringUtils.isNotBlank(config.getOddsType())) {
            StandardMarketOddsDTO odds = oddsList.stream().filter(e -> e.getOddsType().equalsIgnoreCase(config.getOddsType())).findFirst().get();
            String oddsValue = rcsOddsConvertMappingService.getMyOdds(odds.getOddsValue());
            oddsValue = new BigDecimal(oddsValue).add(config.getOddsChange()).toPlainString();
            BigDecimal changeOdds = MarginUtils.checkMyOdds(new BigDecimal(oddsValue));
            BigDecimal uppderOdds = changeOdds.add(config.getMargin());
            if (uppderOdds.doubleValue() >= 1) {
                uppderOdds = BigDecimal.valueOf(NumberUtils.INTEGER_TWO).subtract(uppderOdds);
            } else {
                uppderOdds = uppderOdds.multiply(BigDecimal.valueOf(NumberUtils.LONG_MINUS_ONE));
            }
            uppderOdds = MarginUtils.checkMyOdds(uppderOdds);
            for (StandardMarketOddsDTO odd : oddsList) {
                if (!ObjectUtils.isEmpty(config.getMargin())) {
                    odd.setMargin(config.getMargin().doubleValue());
                }
                if (odd.getOddsType().equalsIgnoreCase(config.getOddsType())) {
                    odd.setOddsValue(rcsOddsConvertMappingService.getEUOddsInteger(changeOdds.toPlainString()));
                } else {
                    odd.setOddsValue(rcsOddsConvertMappingService.getEUOddsInteger(uppderOdds.toPlainString()));
                }
                odd.setOriginalOddsValue(odd.getOddsValue());
            }
        }
        log.info("::{}::,caluFootBallOddsBySpread end oddsList={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(oddsList));
    }

    public void caluBasketBallEuOddsByMargin(List<StandardSportMarketOdds> oddsList, RcsMatchMarketConfig config) {
        if (CollectionUtils.isNotEmpty(oddsList)) {
            oddsList = oddsList.stream().sorted(Comparator.comparing(StandardSportMarketOdds::getOddsValue)).collect(Collectors.toList());
            StandardSportMarketOdds odds = oddsList.get(NumberUtils.INTEGER_ZERO);
            if (!odds.getOddsType().equalsIgnoreCase(config.getOddsType())) {
                config.setOddsChange(config.getOddsChange().multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)));
            }
            // 从小赔率推算大赔率
            oddsList.forEach(e -> {
                if (e.getOddsType().equalsIgnoreCase(odds.getOddsType())) {
                    e.setOddsValue(config.getOddsChange().multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).add(new BigDecimal(e.getOddsValue())).intValue());
                    odds.setOddsValue(e.getOddsValue());
                } else {
                    BigDecimal bigOddsMargin = config.getMargin().subtract(
                            new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE).divide(
                                    new BigDecimal(odds.getOddsValue()).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_HALF_UP));
                    e.setOddsValue(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE).divide(bigOddsMargin, NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
                }
            });
            //设置赔率列表和margin
            List<Map<String, Object>> maps = matchTradeConfigService.getOddsList(config, oddsList);
            config.setOddsList(maps);
        }
    }


    /**
     * @return void
     * @Description //校验最大最小赔率
     * @Param [limitMarketConfig, config]
     * @Author sean
     * @Date 2021/2/4
     **/
    public String checkMaxAndMinOdds(RcsMatchMarketConfig config, List<StandardMarketOddsDTO> oddsList) {
        String msg = null;
        if (CollectionUtils.isEmpty(oddsList) && CollectionUtils.isEmpty(config.getOddsList())) {
            return msg;
        }
        String maxOdds = config.getMaxOdds().toPlainString();
        String minOdds = config.getMinOdds().toPlainString();
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
            maxOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(config.getMaxOdds().toPlainString());
            minOdds = rcsOddsConvertMappingService.minEUOddsByMYOdds(config.getMinOdds().toPlainString());
        }
        if (CollectionUtils.isNotEmpty(oddsList)) {
            for (StandardMarketOddsDTO odds : oddsList) {
                if (odds.getOddsValue().intValue() > new BigDecimal(maxOdds).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue() ||
                        odds.getOddsValue().intValue() < new BigDecimal(minOdds).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue()) {
                    msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT,
                            config.getMinOdds().toPlainString(),
                            config.getMaxOdds().toPlainString(),
                            new BigDecimal(odds.getOddsValue()).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN).toPlainString());
                    break;
                }
            }
        } else if (CollectionUtils.isNotEmpty(config.getOddsList())) {
            for (Map<String, Object> map : config.getOddsList()) {
                if (ObjectUtils.isEmpty(map.get("fieldOddsValue"))) {
                    throw new RcsServiceException("列表赔率不能为空");
                } else if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                    String fieldOddsValue = rcsOddsConvertMappingService.getEUOdds(map.get("fieldOddsValue").toString());

                    if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(maxOdds)) >= 0 ||
                            new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(minOdds)) <= 0) {
                        msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT, config.getMinOdds().toPlainString(), config.getMaxOdds().toPlainString(), map.get("fieldOddsValue").toString());
                        break;
                    }
                } else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {

                    if (new BigDecimal(map.get("fieldOddsValue").toString()).compareTo(config.getMaxOdds()) >= 0 ||
                            new BigDecimal(map.get("fieldOddsValue").toString()).compareTo(config.getMinOdds()) <= 0) {
                        msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT, config.getMinOdds().toPlainString(), config.getMaxOdds().toPlainString(), map.get("fieldOddsValue").toString());
                        break;
                    }
                }
            }
        }
        return msg;
    }

    /**
     * @return void
     * @Description //计算水差
     * @Param [oddsList, config]
     * @Author sean
     * @Date 2021/2/4
     **/
    public String caluBasketBallEuWater(List<StandardSportMarketOdds> oddsList, RcsMatchMarketConfig config, RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin) {
        String msg = null;
        if (CollectionUtils.isNotEmpty(oddsList)) {
            oddsList = oddsList.stream().sorted(Comparator.comparing(StandardSportMarketOdds::getOddsValue)).collect(Collectors.toList());
            StandardSportMarketOdds odds = oddsList.get(NumberUtils.INTEGER_ZERO);
            config.setAwayAutoChangeRate(StringUtils.isEmpty(config.getAwayAutoChangeRate()) ? NumberUtils.DOUBLE_ZERO.toString() : config.getAwayAutoChangeRate());
            BigDecimal oddsAdjustRange = config.getOddsChange();
            BigDecimal lodds = new BigDecimal(odds.getOddsValue()).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN);
            BigDecimal oodds = BigDecimal.ZERO;
            if (odds.getOddsType().equalsIgnoreCase(config.getOddsType())){
                oodds = lodds.add(oddsAdjustRange);
            }else {
                oodds = lodds.subtract(oddsAdjustRange);
            }
            BigDecimal bp = new BigDecimal(1).divide(lodds,4,BigDecimal.ROUND_DOWN);
            BigDecimal ap = new BigDecimal(1).divide(oodds,4,BigDecimal.ROUND_DOWN);
            BigDecimal water = bp.subtract(ap);

            if (odds.getOddsType().equalsIgnoreCase(tradeVerificationService.getBasketBallUnderOddsType(config.getOddsType()))) {
                config.setAwayAutoChangeRate(new BigDecimal(config.getAwayAutoChangeRate()).subtract(water).toPlainString());
            } else {
                config.setAwayAutoChangeRate(new BigDecimal(config.getAwayAutoChangeRate()).add(water).toPlainString());
            }
            if (new BigDecimal(config.getAwayAutoChangeRate()).abs().compareTo(rcsTournamentTemplatePlayMargin.getOddsMaxValue()) == NumberUtils.INTEGER_ONE) {
                msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT,
                        rcsTournamentTemplatePlayMargin.getOddsMaxValue().negate().toPlainString(),
                        rcsTournamentTemplatePlayMargin.getOddsMaxValue().toPlainString(),
                        config.getAwayAutoChangeRate());
            }
        }
        return msg;
    }

    /**
     * @return void
     * @Description //获取联赛配置
     * @Param [config]
     * @Author sean
     * @Date 2021/2/4
     **/
    public void getAndSetOddsChange(RcsMatchMarketConfig config) {
        if (StringUtils.isEmpty(config.getOddsType())) return;
        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (ObjectUtils.isEmpty(rcsTournamentTemplatePlayMargin) ||
                ObjectUtils.isEmpty(rcsTournamentTemplatePlayMargin.getOddsAdjustRange())) {
            throw new RcsServiceException("没有找到联赛配置");
        }
        config.setIsSpecialPumping(rcsTournamentTemplatePlayMargin.getIsSpecialPumping());
        config.setSpecialOddsInterval(rcsTournamentTemplatePlayMargin.getSpecialOddsInterval());
        // 赔率变化
        config.setOddsChange(rcsTournamentTemplatePlayMargin.getOddsAdjustRange().multiply(config.getOddsChange()));
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarketOdds>
     * @Description //获取赔率
     * @Param [config]
     * @Author sean
     * @Date 2021/2/4
     **/
    public List<StandardSportMarketOdds> getStandardSportMarketOdds(RcsMatchMarketConfig config) {
        //如果有盘口值 需要设置盘口值  取实时值
        List<StandardSportMarketOdds> oddsList = tradeOddsCommonService.getMatchMarketOdds(config);
        log.info("::{}::,该盘口数据:{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(oddsList));
        if (CollectionUtils.isEmpty(oddsList)) {
            throw new RcsServiceException("该盘口没数据");
        }
        return oddsList;
    }

    /**
     * @return java.lang.String
     * @Description //校验赔率和更新状态
     * @Param [config]
     * @Author sean
     * @Date 2021/2/5
     **/
    public String getCheckOddsLimitAndUpdateStatus(RcsMatchMarketConfig config, List<StandardMarketOddsDTO> oddsList) {
        // 校验赔率
        String msg = checkMaxAndMinOdds(config, oddsList);
        if (StringUtils.isNotBlank(msg)) {
            if (ObjectUtils.isEmpty(config.getActive())) {
                return msg;
            }
//            else {
//                marketStatusService.updatePlaceStatusNotOdds(config.getMatchId(), config.getPlayId(), config.getMarketIndex(), NumberUtils.INTEGER_ONE, LinkedTypeEnum.DEFAULT.getCode());
//            }
        }
        return null;
    }

    /**
     * @return void
     * @Description 计算自动水差
     * @Param [config]
     * @Author Sean
     * @Date 15:01 2020/10/7
     **/
    public String calculateWater(RcsMatchMarketConfig config, BigDecimal autoRatio, BigDecimal oddsAdjustRange, String oddsType) {
        String msg = null;
        if (StringUtils.isNotEmpty(oddsType)) {
            if (StringUtils.isEmpty(config.getOddsType())) {
                config.setOddsChange(ObjectUtils.isEmpty(config.getOddsChange()) ? new BigDecimal(NumberUtils.DOUBLE_ZERO) : config.getOddsChange());
                autoRatio = config.getOddsChange().divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN);
            } else {
                if (oddsType.equalsIgnoreCase(config.getOddsType())) {
                    autoRatio = autoRatio.add(oddsAdjustRange);
                } else {
                    autoRatio = autoRatio.subtract(oddsAdjustRange);
                }
            }
            if (autoRatio.compareTo(new BigDecimal(TradeConstant.DEFAULT_AUTO_RATIO_MAX)) == 1) {
                msg = TradeConstant.WATER_OUT_OF_LIMIT.replace("xx", autoRatio.toPlainString());
                autoRatio = new BigDecimal(TradeConstant.DEFAULT_AUTO_RATIO_MAX);
            } else if (autoRatio.compareTo(new BigDecimal(TradeConstant.DEFAULT_AUTO_RATIO_MIN)) == -1) {
                msg = TradeConstant.WATER_OUT_OF_LIMIT.replace("xx", autoRatio.toPlainString());
                autoRatio = new BigDecimal(TradeConstant.DEFAULT_AUTO_RATIO_MIN);
            }
            config.setAwayAutoChangeRate(autoRatio.toPlainString());
            log.info("::{}::计算水差完成config={}", CommonUtil.getRequestId(), JSONObject.toJSONString(config));
        }
        return msg;
    }

    /**
     * 调用融合RPC接口，操盘标准盘口及赔率数据处理
     *
     * @param config
     * @param marketList
     * @return
     */
    public void putTradeMarketOdds(RcsMatchMarketConfig config, List<StandardMarketDTO> marketList,Integer matchType) {
        StandardMatchMarketDTO standardMatchMarketDTO = new StandardMatchMarketDTO();
        standardMatchMarketDTO.setStandardMatchInfoId(config.getMatchId());
        if (!ObjectUtils.isEmpty(matchType) && 2 == matchType){
            standardMatchMarketDTO.setMatchType(NumberUtils.INTEGER_ONE);
        }else {
            marketList.stream().collect(Collectors.groupingBy(StandardMarketDTO::getMarketCategoryId)).forEach((playId, list) -> {
                tradeStatusService.handlePushStatus(SportIdEnum.FOOTBALL.getId(), config.getMatchId(), playId, list, null, config.getDataSource().intValue(), 0, 0, 0);
            });
        }
        standardMatchMarketDTO.setMarketList(marketList);
        updateMarketType(config.getMatchId(), marketList);
        if (config.getMarketBuildFlag()) {
            log.info("::{}::,足球附加盘构建开始：marketList={},config={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSON.toJSONString(marketList), JSON.toJSONString(config));
            BigDecimal spread = config.getMargin();
            List<StandardMarketDTO> marketDTOList = marketBuildService.footballBuildMarket(config.getMatchId(), config.getPlayId(), marketList, config, spread);
            for (StandardMarketDTO market : marketDTOList){
                config.setOddsType(tradeVerificationService.getOddsType(market));
                if (!ObjectUtils.isEmpty(config.getIsSpecialPumping()) && config.getIsSpecialPumping() == 1){
                    oddsRangeService.caluSpecialOddsBySpread(market.getMarketOddsList(),config);
                }
            }
            standardMatchMarketDTO.setMarketList(marketDTOList);
            String linkId = CommonUtils.getLinkId("football");
            DataRealtimeApiUtils.handleApi(linkId, standardMatchMarketDTO, new DataRealtimeApiUtils.ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketOddsApi.putTradeMarketOdds(request);
                }
            });
        } else {
            DataRealtimeApiUtils.handleApi(standardMatchMarketDTO, new DataRealtimeApiUtils.ApiCall() {
                @Override
                @Trace
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketOddsApi.putTradeMarketOdds(request);
                }
            });
        }
    }

    /**
     * 处理marketType数据
     */
    public void updateMarketType(Long matchId, List<StandardMarketDTO> list) {
        if (list == null || list.size() <= 0) {
            return;
        }

        StandardMatchInfo info = standardMatchInfoMapper.selectById(matchId);
        if (info == null) {
            return;
        }

        //滚球状态,更新marketType
        if (RcsConstant.LIVE_MATCH_STATUS.contains(info.getMatchStatus())) {
            list.forEach(bean -> {
                bean.setMarketType(0);
            });
        }


    }

    /**
     * @return java.util.List<com.panda.merge.dto.StandardMarketDTO>
     * @Description //更新赔率
     * @Param [config, oddsList]
     * @Author sean
     * @Date 2021/2/5
     **/
    public RcsStandardMarketDTO setOddsValue(RcsMatchMarketConfig conf, List<RcsStandardMarketDTO> oddsList, BigDecimal margin) {
        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(conf), RcsMatchMarketConfig.class);
        config.setMargin(margin);
        RcsStandardMarketDTO market = null;
        if (ObjectUtils.isEmpty(config) && CollectionUtils.isEmpty(config.getOddsList())) {
            log.info("::{}::,足球改赔率入参为空",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()));
            return market;
        }
        if (CollectionUtils.isNotEmpty(oddsList)) {
            for (RcsStandardMarketDTO m : oddsList) {
                if (!ObjectUtils.isEmpty(config.getMarketId()) &&
                        m.getId().equalsIgnoreCase(config.getMarketId().toString())) {

                    market = JSONObject.parseObject(JSONObject.toJSONString(m), RcsStandardMarketDTO.class);
                    // 设置赔率
                    setOddsByMap(config, market);
                    break;
                }
                if (ObjectUtils.isEmpty(config.getMarketId()) && TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(conf.getPlayId().intValue())){
                    market = initOddsList(config);
                    break;
                }
            }
        } else {
            market = initOddsList(config);
        }
        log.info("::{}::,setOddsValue end market={}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(market));
        return market;
    }

    /**
     * @return void
     * @Description //跟map设置赔率
     * @Param [config, e]
     * @Author sean
     * @Date 2021/2/7
     **/
    private void setOddsByMap(RcsMatchMarketConfig config, StandardMarketDTO market) {
        market.getMarketOddsList().forEach(e -> {
            for (Map<String, Object> map : config.getOddsList()) {
                if (e.getOddsType().equalsIgnoreCase(map.get("oddsType").toString())) {
                    Integer oddsValue = tradeVerificationService.getOddsFromMapList(config.getMarketType(), map);
                    e.setOddsValue(oddsValue);
                    if (!ObjectUtils.isEmpty(config.getMargin())) {
                        e.setMargin(config.getMargin().doubleValue());
                    }
                    e.setActive(NumberUtils.INTEGER_ONE);
                    e.setAnchor(ObjectUtils.isEmpty(map.get("anchor")) ? null : Integer.parseInt(map.get("anchor").toString()));
                    e.setOriginalOddsValue(oddsValue);
                    e.setDataSourceCode("PA");
                }
                if ((TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) ||
//                        TradeConstant.FOOTBALL_SECONDARY_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) ||
                        TradeConstant.FOOTBALL_SECONDARY_HANDICAP_PLAYS.contains(config.getPlayId().intValue())) &&
                        BaseConstants.ODD_TYPE_2.equalsIgnoreCase(map.get("oddsType").toString())) {
                    e.setNameExpressionValue(new BigDecimal(market.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).stripTrailingZeros().toPlainString());
                }
                if (TradeConstant.FOOTBALL_X_A5_PLAYS.contains(config.getPlayId().intValue())  &&
                        BaseConstants.ODD_TYPE_2.equalsIgnoreCase(map.get("oddsType").toString())) {
                    e.setNameExpressionValue(new BigDecimal(market.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).stripTrailingZeros().toPlainString());
                }
            }
        });
        market.setStatus(config.getMarketStatus());
        market.setPlaceNum(config.getMarketIndex());
        market.setPlaceNumStatus(config.getMarketStatus());
        if (TradeConstant.FOOTBALL_OVER_UNDER_PLAYS.contains(config.getPlayId().intValue()) ||
                TradeConstant.FOOTBALL_SECONDARY_ADDITION1_PLAYS.contains(config.getPlayId().intValue())) {
            market.setAddition1(config.getHomeMarketValue().toPlainString());
        }
        if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue())
        ) {
            market.setAddition2(config.getHomeMarketValue().toPlainString());
            market.setAddition1(config.getHomeMarketValue().toPlainString());
        }
        if (TradeConstant.FOOTBALL_SORCE_PLAYS.contains(config.getPlayId().intValue())) {
            if (StringUtils.isBlank(config.getScore())) {
                config.setScore("0:0");
            }
            market.setAddition1(config.getScore().split(":")[0]);
            market.setAddition2(config.getScore().split(":")[1]);
        }
        if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
            if (TradeConstant.FOOTBALL_X_A1_A2_A3_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition1(config.getHomeMarketValue().toPlainString());
            }
        }
        // 更新基准分
        updateBenchmarkScore(config, market);
    }

    /**
     * @return void
     * @Description //更新基准分
     * @Param [config, market]
     * @Author sean
     * @Date 2021/2/20
     **/
    public void updateBenchmarkScore(RcsMatchMarketConfig config, StandardMarketDTO market) {
        if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue())) {
            config.setScore(StringUtils.isBlank(config.getScore()) ? "0:0" : config.getScore());
            String[] scores = config.getScore().split(":");
            if(334 != config.getPlayId().intValue()){
                String addition2 = config.getHomeMarketValue()
                        .add(new BigDecimal(Integer.parseInt(scores[1])))
                        .subtract(new BigDecimal(Integer.parseInt(scores[0])))
                        .stripTrailingZeros().toPlainString();
                market.setAddition2(addition2);
            }
            market.setAddition3(scores[0]);
            market.setAddition4(scores[1]);
            if (TradeConstant.FOOTBALL_X_A5_PLAYS.contains(config.getPlayId().intValue()) && ObjectUtils.isEmpty(config.getMarketId())) {
                String[] ids = config.getSubPlayId().split("-");
                market.setAddition5(Integer.parseInt(ids[2]) - 14 + "," + ids[2]);
            }
        }
    }

    /**
     * @return void
     * @Description //验证数据的正确性
     * @Param [config]
     * @Author kimi
     * @Date 2020/3/6
     **/
    public String verifyData(RcsMatchMarketConfig config, BigDecimal margin,RcsTournamentTemplatePlayMargain buildConfig) {
        // 1、赔率最大最小验证
        if (config.getMaxOdds() == null || config.getMinOdds() == null) {
            throw new RcsServiceException("最大最小赔率不能为空");
        } else if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
            String maxOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(config.getMaxOdds().toPlainString());
            String minOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(config.getMinOdds().toPlainString());
            if (new BigDecimal(maxOdds).compareTo(new BigDecimal(minOdds)) < 0) {
                throw new RcsServiceException("马赔最小赔率比最大赔率大");
            }
        } else if (config.getMaxOdds().compareTo(config.getMinOdds()) < 0) {
            throw new RcsServiceException("欧赔最小赔率比最大赔率大");
        }
        // 2、校验赔率列表
        if (CollectionUtils.isEmpty(config.getOddsList())) {
            throw new RcsServiceException("赔率列表不能为空");
        }
        if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType()) &&
                (!MarketUtils.isAuto(config.getDataSource().intValue()))){
            for (Map<String,Object> map : config.getOddsList()){
                if (Double.parseDouble(map.get("fieldOddsValue").toString()) < 1.01){
                    throw new RcsServiceException("赔率不能小于1.01");
                }
            }
        }
        // 3、校验赔率合法性
        String msg = null;
        if (DataSourceTypeEnum.MANUAL.getValue().intValue() == config.getDataSource().intValue()) {
            msg = checkMaxAndMinOdds(config, null);
        }
        //4： 验证magin值
        if (ObjectUtils.isEmpty(config.getIsSpecialPumping()) || NumberUtils.INTEGER_ZERO.intValue() == config.getIsSpecialPumping()){
            boolean convert = MarginUtils.convert(config.getOddsList(), config.getDataSource(), config.getMarketType(), margin, config.getPlayId());
            if (!convert) {
                throw new RcsServiceException("margin校验没通过");
            }
        }
        buildConfig.setOddsMaxValue(ObjectUtils.isEmpty(buildConfig.getOddsMaxValue()) ? new BigDecimal(TradeConstant.DEFAULT_AUTO_RATIO_MAX) : buildConfig.getOddsMaxValue());
//         5、校验水差设置
        if (DataSourceTypeEnum.AUTOMATIC.getValue() == config.getDataSource().intValue() &&
                StringUtils.isNotBlank(config.getAwayAutoChangeRate()) &&
                config.getOddsList().size() == 2 &&
                new BigDecimal(config.getAwayAutoChangeRate()).abs().compareTo(buildConfig.getOddsMaxValue()) == NumberUtils.INTEGER_ONE) {
            if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
                if (new BigDecimal(config.getAwayAutoChangeRate()).doubleValue() > NumberUtils.DOUBLE_ZERO) {
                    config.setAwayAutoChangeRate(buildConfig.getOddsMaxValue().toPlainString());
                } else {
                    config.setAwayAutoChangeRate(buildConfig.getOddsMaxValue().negate().toPlainString());
                }
            }else {
                config.setMarketStatus(NumberUtils.INTEGER_ONE);
            }
            msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT,
                    buildConfig.getOddsMaxValue().negate().toPlainString(),
                    buildConfig.getOddsMaxValue().toPlainString(),
                    config.getAwayAutoChangeRate());
        }
        // 6、校验数据范围
        tradeVerificationService.checkDataRange(config,null);
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(config.getMatchId());
        Integer matchType = NumberUtils.INTEGER_ONE;
        if (RcsConstant.LIVE_MATCH_STATUS.contains(standardMatchInfo.getMatchStatus())) {
            matchType = NumberUtils.INTEGER_ZERO;
        }
        config.setMatchType(matchType);
        // 9、阶段校验
        if (ObjectUtils.isEmpty(config.getMarketId()) && NumberUtils.INTEGER_ZERO.intValue() == config.getMatchType()) {
            if (ObjectUtils.isEmpty(standardMatchInfo) ||
                    ObjectUtils.isEmpty(standardMatchInfo.getMatchPeriodId()) ||
                    (!FootBallPlayEnum.getPeriod(config.getPlayId()).contains(standardMatchInfo.getMatchPeriodId().intValue()))) {
                throw new RcsServiceException("当前比赛阶段不能新增该玩法盘口");
            }
//            if (TradeConstant.FOOTBALL_X_A3_PLAYS.contains(config.getPlayId().intValue())) {
//                Long matchTime = standardMatchInfo.getBeginTime();
//                String[] ids = subPlayId.split("-");
//                Double time = Double.parseDouble(ids[2]);
//                if (System.currentTimeMillis() - matchTime >= time * 60 * 1000){
//                    throw new RcsServiceException("当前比赛阶段不能新增该玩法盘口");
//                }
//            }
        }
        //最大投注金额校验
        BigDecimal maxAmount = Optional.ofNullable(config.getMaxBetAmount()).orElse(new BigDecimal("10000000"));
        if (ObjectUtils.isEmpty(config.getMaxSingleBetAmount()) ||
                ObjectUtils.isEmpty(config.getMaxBetAmount()) ||
                config.getMaxSingleBetAmount().longValue() > maxAmount.longValue()) {
            throw new RcsServiceException("最大投注金额不能大于联赛配置");
        }
        // 7、 基准分
        if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) &&
                NumberUtils.INTEGER_ZERO.intValue() == config.getMatchType() &&
                ObjectUtils.isEmpty(config.getMarketId())) {
            if (StringUtils.isBlank(config.getScore())) {
                throw new RcsServiceException("基准分玩法，比分不能为空");
            }
            String benchmarks = matchStatisticsInfoService.queryCurrentScoreByPlayId(config);
            if (ObjectUtils.isEmpty(config.getMarketId())) {
                Integer score = Integer.parseInt(config.getScore().split(":")[0]) - Integer.parseInt(config.getScore().split(":")[1]);
                Integer benchmark = Integer.parseInt(benchmarks.split(":")[0]) - Integer.parseInt(benchmarks.split(":")[1]);
                if (score - benchmark != NumberUtils.INTEGER_ZERO) {
                    throw new RcsServiceException("当前比分和基准分不符");
                }
            }
        }
        // 8、 大小球
        if (TradeConstant.FOOTBALL_OVER_UNDER_PLAYS.contains(config.getPlayId().intValue()) &&
                NumberUtils.INTEGER_ZERO.intValue() == config.getMatchType()) {
            if (StringUtils.isBlank(config.getScore())) {
                throw new RcsServiceException("基准分玩法，比分不能为空");
            }
            Integer score = Integer.parseInt(config.getScore().split(":")[0]) + Integer.parseInt(config.getScore().split(":")[1]);
            if (score >= config.getAwayMarketValue().subtract(config.getHomeMarketValue()).doubleValue()) {
                throw new RcsServiceException("当前比分大于等于盘口值");
            }
        }
        // 9、 大小球球头校验 aden说负数直接转成正数
        if (TradeConstant.FOOTBALL_HEAD_CHECK_PLAYS.contains(config.getPlayId().intValue())) {
            if (0 >= config.getAwayMarketValue().subtract(config.getHomeMarketValue()).doubleValue()) {
                config.setAwayMarketValue(config.getAwayMarketValue().subtract(config.getHomeMarketValue()).abs());
            }
        }
        return msg;
    }

    /**
     * @return java.util.List<com.panda.merge.dto.StandardMarketDTO>
     * @Description //构建盘口数据
     * @Param [matchPlayConfig]
     * @Author sean
     * @Date 2020/12/9
     **/
    private RcsStandardMarketDTO initOddsList(RcsMatchMarketConfig config) {
        RcsStandardMarketDTO market = null;
        if (CollectionUtils.isNotEmpty(config.getOddsList())) {
            market = new RcsStandardMarketDTO();
            market.setMarketCategoryId(config.getPlayId());
            market.setMarketType(config.getMatchType());
            market.setPlaceNum(NumberUtils.INTEGER_ONE);
            market.setTradeType(NumberUtils.INTEGER_ONE);
            market.setPlaceNumStatus(config.getMarketStatus());
            if (TradeConstant.FOOTBALL_SECONDARY_ADDITION1_PLAYS.contains(config.getPlayId().intValue())) {
                market.setAddition1(config.getHomeMarketValue().toPlainString());
            }
            if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) ||
                    TradeConstant.FOOTBALL_OVER_UNDER_PLAYS.contains(config.getPlayId().intValue())
//                    TradeConstant.FOOTBALL_SECONDARY_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue())
            ) {
                market.setAddition1(config.getHomeMarketValue().toPlainString());
                market.setAddition2(config.getHomeMarketValue().toPlainString());
            }
//            if (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue())) {
//                market.setAddition2(config.getHomeMarketValue().toPlainString());
//                if (StringUtils.isBlank(config.getScore())) {
//                    config.setScore("0:0");
//                }
//                market.setAddition3(config.getScore().split(":")[0]);
//                market.setAddition4(config.getScore().split(":")[1]);
//            }
            if (TradeConstant.FOOTBALL_SORCE_PLAYS.contains(config.getPlayId().intValue())) {
                if (StringUtils.isBlank(config.getScore())) {
                    config.setScore("0:0");
                }
                market.setAddition1(config.getScore().split(":")[0]);
                market.setAddition2(config.getScore().split(":")[1]);
            }
            //子玩法设置参数
            setAdditionForXPlay(config, market);

            market.setDataSourceCode(TradeConstant.DATA_SOURCE_CODE_PA);
//            market.setStatus(NumberUtils.INTEGER_ZERO);
            market.setThirdMarketSourceStatus(NumberUtils.INTEGER_ZERO);
            List<StandardMarketOddsDTO> marketOddsList = Lists.newArrayList();
            List<StandardMarketOddsDTO> oddsFieldsTempletIds = standardSportMarketMapper.selectOddsFieldsTempletId(config.getPlayId());
            // 基准分玩法
            updateBenchmarkScore(config, market);
            for (Map<String, Object> map : config.getOddsList()) {
                StandardMarketOddsDTO dto = new StandardMarketOddsDTO();
                dto.setDataSourceCode(TradeConstant.DATA_SOURCE_CODE_PA);
                dto.setActive(NumberUtils.INTEGER_ONE);
                Integer oddsValue = tradeVerificationService.getOddsFromMapList(config.getMarketType(), map);
                dto.setOddsValue(oddsValue);
                if (!ObjectUtils.isEmpty(config.getMargin())) {
                    dto.setMargin(config.getMargin().doubleValue());
                }
                dto.setAnchor(ObjectUtils.isEmpty(map.get("anchor")) ? 0 : Integer.parseInt(map.get("anchor").toString()));
                dto.setOriginalOddsValue(oddsValue);
                dto.setOddsType(map.get("oddsType").toString());
                dto.setNameExpressionValue(market.getAddition1());
                Long oddsFieldsTemplateId = getOddsFieldsTemplateId(oddsFieldsTempletIds, dto, config.getPlayId());
                dto.setOddsFieldsTemplateId(oddsFieldsTemplateId);
                if ((TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) ||
//                        TradeConstant.FOOTBALL_SECONDARY_BENCHMARK_SCORE_PLAYS.contains(config.getPlayId().intValue()) ||
                        TradeConstant.FOOTBALL_SECONDARY_HANDICAP_PLAYS.contains(config.getPlayId().intValue())) &&
                        BaseConstants.ODD_TYPE_2.equalsIgnoreCase(map.get("oddsType").toString())) {
                    dto.setNameExpressionValue(new BigDecimal(market.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).stripTrailingZeros().toPlainString());
                }
                if (TradeConstant.FOOTBALL_X_A5_PLAYS.contains(config.getPlayId().intValue()) &&
                        BaseConstants.ODD_TYPE_2.equalsIgnoreCase(map.get("oddsType").toString())) {
                    dto.setNameExpressionValue(new BigDecimal(market.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).stripTrailingZeros().toPlainString());
                }
                marketOddsList.add(dto);
            }
            market.setMarketOddsList(marketOddsList);
        }
        log.info("::{}::初始化的赔率列表={}",CommonUtil.getRequestId(market.getId(),market.getMarketCategoryId()), JSONObject.toJSONString(market));
        return market;
    }

    public void setAdditionForXPlay(RcsMatchMarketConfig config, RcsStandardMarketDTO market) {
        // 带X玩法设置
        if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
            String[] ads = config.getSubPlayId().split("-");
            if (TradeConstant.FOOTBALL_X_A1_PLAYS.contains(config.getPlayId().intValue()) ||
                    TradeConstant.BASKETBALL_X_SCORCE_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition1(ads[1]);
            }
            if (TradeConstant.FOOTBALL_X_A1_A2_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition1(ads[1]);
                market.setAddition2(ads[2]);
            }
            if (TradeConstant.FOOTBALL_X_A2_A1_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition1(ads[2]);
                market.setAddition2(ads[1]);
            }
            if (TradeConstant.FOOTBALL_X_A1_A2_A3_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition1(config.getHomeMarketValue().stripTrailingZeros().toPlainString());
            }
            if (TradeConstant.FOOTBALL_X_A2_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition1(ads[2]);
                market.setAddition2(ads[1]);
            }
//                if (TradeConstant.FOOTBALL_X_A1_A2_PLAYS.contains(config.getPlayId().intValue())){
//                    market.setAddition2(ads[2]);
//                }
//                if (TradeConstant.FOOTBALL_X_A1_A2_A3_PLAYS.contains(config.getPlayId().intValue()) ||
//                        TradeConstant.FOOTBALL_X_A2_A3_PLAYS.contains(config.getPlayId().intValue())){
//                    market.setAddition2(market.getAddition1());
//                }
//                if (TradeConstant.FOOTBALL_X_A2_A1_PLAYS.contains(config.getPlayId().intValue())){
//                    market.setAddition2(ads[1]);
//                }
            if (TradeConstant.FOOTBALL_X_A3_PLAYS.contains(config.getPlayId().intValue())){
                market.setAddition2(new BigDecimal(ads[1]).add(BigDecimal.ONE).stripTrailingZeros().toPlainString());
                market.setAddition3(ads[2]);
            }
        }
    }

    /**
     * @return java.lang.Long
     * @Description //设置投注项名称
     * @Param [dto, oddsDTO]
     * @Author sean
     * @Date 2020/12/20
     **/
    public Long getOddsFieldsTemplateId(List<StandardMarketOddsDTO> oddsFieldsTempletIds, StandardMarketOddsDTO oddsDTO, Long playId) {
        if (CollectionUtils.isEmpty(oddsFieldsTempletIds)) {
            log.warn("::{}::投注项模板id数据错误",oddsDTO.getMatchId());
        }
        if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_NONE.contains(playId.intValue())) {
            if (TradeConstant.ODD_TYPE_NONE.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_ONE);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ZERO).getOddsFieldsTemplateId();
            } else if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_TWO);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ONE).getOddsFieldsTemplateId();
            } else {
                oddsDTO.setOrderOdds(oddsFieldsTempletIds.size());
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_TWO).getOddsFieldsTemplateId();
            }
        }
        if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_NUMBER.contains(playId.intValue())) {
            if (TradeConstant.ODD_TYPE_5_6.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    TradeConstant.ODD_TYPE_0_8.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_ONE);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ZERO).getOddsFieldsTemplateId();
            } else if (TradeConstant.ODD_TYPE_7.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    TradeConstant.ODD_TYPE_9_11.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_TWO);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ONE).getOddsFieldsTemplateId();
            } else {
                oddsDTO.setOrderOdds(oddsFieldsTempletIds.size());
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_TWO).getOddsFieldsTemplateId();
            }
        }
        if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_1X.contains(playId.intValue())) {
            if (TradeConstant.ODD_TYPE_1.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_ONE);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ZERO).getOddsFieldsTemplateId();
            } else {
                oddsDTO.setOrderOdds(oddsFieldsTempletIds.size());
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ONE).getOddsFieldsTemplateId();
            }
        }
        if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_X2.contains(playId.intValue())) {
            if (TradeConstant.ODD_TYPE_X.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_ONE);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ZERO).getOddsFieldsTemplateId();
            } else {
                oddsDTO.setOrderOdds(oddsFieldsTempletIds.size());
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ONE).getOddsFieldsTemplateId();
            }
        }
        if (oddsFieldsTempletIds.size() == 2) {
            if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    BaseConstants.ODD_TYPE_YES.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_ONE);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ZERO).getOddsFieldsTemplateId();
            } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    BaseConstants.ODD_TYPE_NO.equalsIgnoreCase(oddsDTO.getOddsType()) ||
                    BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(oddsFieldsTempletIds.size());
                return oddsFieldsTempletIds.get(oddsFieldsTempletIds.size() - NumberUtils.INTEGER_ONE).getOddsFieldsTemplateId();

            }
        } else if (oddsFieldsTempletIds.size() == 3) {
            if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_1.contains(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_ONE);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ZERO).getOddsFieldsTemplateId();
            } else if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_2.contains(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(NumberUtils.INTEGER_TWO);
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_ONE).getOddsFieldsTemplateId();
            } else if (TradeConstant.FOOTBALL_ODDS_TYPE_ORDER_3.contains(oddsDTO.getOddsType())) {
                oddsDTO.setOrderOdds(oddsFieldsTempletIds.size());
                return oddsFieldsTempletIds.get(NumberUtils.INTEGER_TWO).getOddsFieldsTemplateId();
            }
        }
        return NumberUtils.LONG_ZERO;
    }

    /**
     * @return void
     * @Description //组装盘口数据
     * @Param [config, playAllMarketList, list]
     * @Author sean
     * @Date 2021/2/21
     **/
    public List<StandardMarketDTO> packageMarketList(RcsMatchMarketConfig config, List<RcsStandardMarketDTO> playAllMarketList, RcsStandardMarketDTO market) {
        log.info("::{}::,config:{}，playAllMarketList：{}，market：{}",CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(config),JSONObject.toJSONString(playAllMarketList),JSONObject.toJSONString(market));
        market.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
        List<RcsStandardMarketDTO> marketList = Lists.newArrayList(market);
        List<RcsStandardMarketDTO> marketLists = Lists.newArrayList();
        market.setThirdMarketSourceStatus(NumberUtils.INTEGER_ZERO);
        Integer oldPlaceNum = Integer.MAX_VALUE;
        RcsStandardMarketDTO oldMarket = null;

        for (RcsStandardMarketDTO e : playAllMarketList){
            if (!SubPlayUtil.getRongHeSubPlayId(e).equalsIgnoreCase(SubPlayUtil.getRongHeSubPlayId(market))){
                marketLists.add(e);
                continue;
            }
                e.setThirdMarketSourceStatus(NumberUtils.INTEGER_ZERO);
            if(TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(e.getMarketCategoryId().intValue()) ) {
                e.setAddition3("0");
                e.setAddition4("0");
            }
            if("27".equalsIgnoreCase(e.getMarketCategoryId().toString()) || "29".equalsIgnoreCase(e.getMarketCategoryId().toString())) {
                if (StringUtils.isNotBlank(config.getScore())){
                    e.setAddition1(config.getScore().split(":")[0]);
                    e.setAddition2(config.getScore().split(":")[1]);
                } else {
                    e.setAddition1("0");
                    e.setAddition2("0");
                }
            }

            if ((!ObjectUtils.isEmpty(e.getAddition1())) &&
                    e.getAddition1().equalsIgnoreCase(market.getAddition1())
            ) {
                oldPlaceNum = e.getPlaceNum();
            } else {
                if (ObjectUtils.isEmpty(e.getAddition1())){
                    continue;
                }
                marketList.add(e);

                if ((!ObjectUtils.isEmpty(config.getMarketId())) && e.getId().equalsIgnoreCase(config.getMarketId().toString())) {
                    e.setStatus(NumberUtils.INTEGER_TWO);
                    e.setPlaceNumStatus(NumberUtils.INTEGER_TWO);
                    oldMarket = e;
                }

            }
            if (config.getMatchType().intValue() == NumberUtils.INTEGER_ZERO
                    && TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(e.getMarketCategoryId().intValue())
            ) {
                e.setAddition3(market.getAddition3());
                e.setAddition4(market.getAddition4());
            }
        }

        //重置排序 ,修改的盘口，不在查询列表中,直接原始位置的盘口重置到最后
        if (oldMarket != null && oldMarket.getPlaceNum() > oldPlaceNum) {//oldMarket是空的，可以理解为数据有问题
            //将oldMarket.getPlaceNum()后的一个盘口位置减2
            for (RcsStandardMarketDTO bean : marketList) {
                if (oldMarket.getPlaceNum() + 1 == bean.getPlaceNum() && SubPlayUtil.getRongHeSubPlayId(bean).equalsIgnoreCase(SubPlayUtil.getRongHeSubPlayId(market))) {
                    bean.setPlaceNum(bean.getPlaceNum() - 2);
                }
            }
        }

        if (oldMarket != null) {
            oldMarket.setPlaceNum(Integer.MAX_VALUE);
            updatePlaceStatus(config.getMatchId(), oldMarket.getMarketCategoryId(), marketList.size(), NumberUtils.INTEGER_TWO,config.getSubPlayId());
        }

        log.info("::{}::,重置之后排序：{}", CommonUtil.getRequestId(config.getMatchId(),config.getPlayId()),JSONObject.toJSONString(marketList));

        Collections.sort(marketList, (a, b) -> {
            return a.getPlaceNum() - b.getPlaceNum();
        });
        for (int i = 1; i <= marketList.size(); i++) {
            if (SubPlayUtil.getRongHeSubPlayId(marketList.get(i - 1)).equalsIgnoreCase(SubPlayUtil.getRongHeSubPlayId(market))){
                marketList.get(i - 1).setPlaceNum(i);
            }
        }
//        }
        marketLists.addAll(marketList);
        return JSONArray.parseArray(JSONArray.toJSONString(marketLists), StandardMarketDTO.class);
    }

    public void updatePlaceStatus(Long matchId, Long playId, int placeNum, Integer status,String subPlayId) {
        MarketStatusUpdateVO marketStatusUpdateVO = new MarketStatusUpdateVO();
        marketStatusUpdateVO.setMatchId(matchId);
        marketStatusUpdateVO.setCategoryId(playId);
        marketStatusUpdateVO.setTradeLevel(TradeLevelEnum.MARKET.getLevel());
        marketStatusUpdateVO.setMarketPlaceNum(placeNum);
        marketStatusUpdateVO.setMarketStatus(status);
        marketStatusUpdateVO.setIsPushOdds(0);
        // 带参数玩法需要子玩法id
        if (TradeConstant.FOOTBALL_X_NO_INSERT_PLAYS.contains(playId.intValue()) ||
            TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(playId.intValue()) ||
            TradeConstant.BASKETBALL_X_PLAYS.contains(playId.intValue())){
            marketStatusUpdateVO.setSubPlayId(Long.parseLong(subPlayId));
        }
        tradeStatusService.updateTradeStatus(marketStatusUpdateVO);
    }
    public void updatePlayStatus(Long matchId, Long playId, Integer status,String subPlayId) {
        MarketStatusUpdateVO marketStatusUpdateVO = new MarketStatusUpdateVO();
        marketStatusUpdateVO.setMatchId(matchId);
        marketStatusUpdateVO.setCategoryId(playId);
        marketStatusUpdateVO.setTradeLevel(TradeLevelEnum.PLAY.getLevel());
        marketStatusUpdateVO.setMarketStatus(status);
        marketStatusUpdateVO.setIsPushOdds(1);
        // 带参数玩法需要子玩法id
        if (StringUtils.isNotBlank(subPlayId)){
            marketStatusUpdateVO.setSubPlayId(Long.parseLong(subPlayId));
        }
        tradeStatusService.updateTradeStatus(marketStatusUpdateVO);
    }


    /**
     * @return void
     * @Description //设置margin
     * @Param [config]
     * @Author sean
     * @Date 2021/2/26
     **/
    public void setMarginFromMap(RcsMatchMarketConfig config) {
        if (!org.springframework.util.CollectionUtils.isEmpty(config.getOddsList())) {
            for (Map<String, Object> map : config.getOddsList()) {
                if (map.containsKey("nameExpressionValue")) {
                    map.put("nameExpressionValue", NameExpressionValueUtils.getTextParseToNumber(String.valueOf(map.get("nameExpressionValue"))));
                }

                if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {
                    if ((ObjectUtils.isEmpty(map.get("margin")) || StringUtils.isBlank(map.get("margin").toString().trim()))) {
                        throw new RcsServiceException("margin 不能设置成空字符串");
                    }
                    if (!ObjectUtils.isEmpty(map.get("oddsType"))) {
                        BigDecimal margin = new BigDecimal(map.get("margin").toString());
                        if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(map.get("oddsType").toString())) {
                            config.setHomeMargin(margin);
                            continue;
                        }
                        if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(map.get("oddsType").toString())) {
                            config.setAwayMargin(margin);
                            continue;
                        }
                        if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(map.get("oddsType").toString())) {
                            config.setTieMargin(margin);
                            continue;
                        }
                    }
                }
            }
        }
    }

    /**
     * @return java.math.BigDecimal
     * @Description //跟当前盘口计算盘口差
     * @Param [config]
     * @Author sean
     * @Date 2021/3/11
     **/
    public static BigDecimal getMarketHeadGapByValue(RcsMatchMarketConfig config, StandardSportMarket market) {
        BigDecimal headGap = null;
        if ((!ObjectUtils.isEmpty(market))
                && (!StringUtils.isBlank(market.getAddition1()))
                && config.getMarketIndex().intValue() == NumberUtils.INTEGER_ONE) {

            BigDecimal beforeMarketValue = new BigDecimal(market.getAddition1());
            BigDecimal beforeMarketHeadGap = ObjectUtils.isEmpty(market.getMarketHeadGap()) ? BigDecimal.ZERO : market.getMarketHeadGap();
            headGap = config.getHomeMarketValue().subtract(beforeMarketValue).add(beforeMarketHeadGap);
            ;
            if (TradeConstant.MAIN_BASKETBALL_TOTAL_HANDICAP.toString().equalsIgnoreCase(config.getPlayId().toString())) {
                if (config.getHomeMarketValue().multiply(beforeMarketValue).doubleValue() < 0) {
                    headGap = (config.getHomeMarketValue().subtract(beforeMarketValue)).abs().subtract(BigDecimal.ONE)
                            .multiply((config.getHomeMarketValue().subtract(beforeMarketValue))
                                    .divide((config.getHomeMarketValue().subtract(beforeMarketValue)).abs()))
                            .add(beforeMarketHeadGap);
                } else if (config.getHomeMarketValue().multiply(beforeMarketValue).doubleValue() == 0) {
                    if (config.getHomeMarketValue().doubleValue() != 0 && beforeMarketValue.doubleValue() != 0) {
                        // 支持页面传0的情况
                        if (config.getHomeMarketValue().doubleValue() == 0) {
                            headGap = (beforeMarketValue.abs().subtract(new BigDecimal("0.5")))
                                    .multiply(beforeMarketValue.divide(beforeMarketValue.abs()))
                                    .multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE))
                                    .add(beforeMarketHeadGap);
                        } else {
                            // 页面不支持传O，所在这里0只能是数据库的0
                            headGap = (config.getHomeMarketValue().abs().subtract(new BigDecimal("0.5")))
                                    .multiply(config.getHomeMarketValue().divide(config.getHomeMarketValue().abs()))
                                    .add(beforeMarketHeadGap);
                        }
                    }
                }
            }
        }
        return headGap;
    }

    /**
     * @return void
     * @Description //margin 水差等811需求参数
     * @Param [gapConfigDTO]
     * @Author sean
     * @Date 2021/5/2
     **/
    public void putTradeMarginGap(TradeMarketMarginGapConfigDTO gapConfigDTO) {
        DataRealtimeApiUtils.handleApi(gapConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeMarketMarginGapConfig(request);
            }
        });
    }

    /**
     * @return com.panda.merge.dto.StandardMarketOddsDTO
     * @Description //获取没有描点的投注项
     * @Param [marketOddsList]
     * @Author sean
     * @Date 2021/5/4
     **/
    public StandardMarketOddsDTO getNotAnchor(List<StandardMarketOddsDTO> marketOddsList) {
        log.info("::{}::获取没有描点的投注项 -----> getNotAnchor = {}",CommonUtil.getRequestId(), JSONObject.toJSONString(marketOddsList));
        StandardMarketOddsDTO dto = marketOddsList.get(NumberUtils.INTEGER_ZERO);
        // 两项盘 赔率小的是描点，相同赔率主队是描点
        if (marketOddsList.size() == 2) {
            StandardMarketOddsDTO dto1 = marketOddsList.get(NumberUtils.INTEGER_ZERO);
            StandardMarketOddsDTO dto2 = marketOddsList.get(NumberUtils.INTEGER_ONE);
            if (dto1.getOddsValue() > dto2.getOddsValue()) {
                dto = dto1;
            }
            if (dto1.getOddsValue() < dto2.getOddsValue()) {
                dto = dto2;
            }
            if (dto1.getOddsValue().intValue() == dto2.getOddsValue()) {
                if (dto1.getOddsType().equalsIgnoreCase(tradeVerificationService.getBasketBallUnderOddsType(dto1.getOddsType()))) {
                    dto = dto2;
                } else {
                    dto = dto1;
                }
            }
        } else {
            // 三项盘通过字段区分
            for (StandardMarketOddsDTO oddsDTO : marketOddsList) {
                if (ObjectUtils.isEmpty(oddsDTO.getAnchor()) || oddsDTO.getAnchor().intValue() == 0) {
                    dto = oddsDTO;
                    break;
                }
            }
        }
        log.info("::{}::获取没有描点的投注项 -----> end getNotAnchor = {}",CommonUtil.getRequestId(), JSONObject.toJSONString(dto));
        return dto;
    }

    /**
     * @return void
     * @Description //margin算法计算赔率
     * @Param [bean, placeNumConfig]
     * @Author sean
     * @Date 2021/5/4
     **/
    public void calculationOddsByMargin(RcsStandardMarketDTO bean, BigDecimal margin) {
        log.info("::{}::,calculationOddsByMargin 开始 bean={}，margin={}",CommonUtil.getRequestId(bean.getId()),JSONObject.toJSONString(bean),margin.toPlainString());
        if (!TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(bean.getMarketCategoryId().intValue())){
            return;
        }
        // 获取
        StandardMarketOddsDTO odd = getNotAnchor(bean.getMarketOddsList());

        // 根据概率算另一边的赔率
        // 独赢margin算法
        BigDecimal totalMargin = BigDecimal.ZERO;
        BigDecimal anchorMargin = BigDecimal.ZERO;
        for (StandardMarketOddsDTO odds : bean.getMarketOddsList()){
            if (odds.getOddsValue() == 0){
                continue;
            }
            // 算概率
            BigDecimal probability = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE)
                    .divide(new BigDecimal(odds.getOddsValue()).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN),
                            NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN);
            // 汇总概率
            totalMargin = totalMargin.add(probability);
            // 描点margin
            if (!odds.getOddsType().equalsIgnoreCase(odd.getOddsType())) {
                anchorMargin = anchorMargin.add(probability);
            }
            if (!ObjectUtils.isEmpty(margin)) {
                odds.setMargin(margin.doubleValue());
            }
        }
        // 根据描点修订赔率
        ifProbabilityOverResetOdds(bean, margin, odd, totalMargin, anchorMargin);
        log.info("::{}::,calculationOddsByMargin 结束 bean={}", CommonUtil.getRequestId(bean.getId()),JSONObject.toJSONString(bean));
    }

    /**
     * @return void
     * @Description // 根据margin差计算margin
     * @Param [bean, margin]
     * @Author sean
     * @Date 2021/5/23
     **/
    public void calculationOddsByMarginDiff(RcsStandardMarketDTO bean, BigDecimal margin, BigDecimal oldMargin) {
        log.info("::{}::,calculationOddsByMarginDiff margin改变 开始 bean={}，margin={}", CommonUtil.getRequestId(bean.getParentId()),JSONObject.toJSONString(bean), margin.toPlainString());
        // 获取
        StandardMarketOddsDTO odd = getNotAnchor(bean.getMarketOddsList());
        // 独赢margin算法
        BigDecimal totalMargin = BigDecimal.ZERO;
        BigDecimal anchorMargin = BigDecimal.ZERO;

        for (StandardMarketOddsDTO odds : bean.getMarketOddsList()){
            if (odds.getOddsValue() ==0){
                continue;
            }
            // 算概率
            BigDecimal probability = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE)
                    .divide(new BigDecimal(odds.getOddsValue()).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN),
                            NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN);

            BigDecimal marginDiff = (margin
                    .subtract(ObjectUtils.isEmpty(oldMargin) ? BigDecimal.valueOf(110) : oldMargin))
                    .divide(new BigDecimal(bean.getMarketOddsList().size()), 2, BigDecimal.ROUND_DOWN);

            probability = probability.add(marginDiff);
            // 汇总概率
            totalMargin = totalMargin.add(probability);
            // 描点margin
            if (!odds.getOddsType().equalsIgnoreCase(odd.getOddsType())) {
                anchorMargin = anchorMargin.add(probability);
            }
            odds.setMargin(margin.doubleValue());
            odds.setOddsValue(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE)
                    .divide(probability, NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN)
                    .multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
        }
        // 根据描点修订赔率
//        ifProbabilityOverResetOdds(bean, margin, odd, totalMargin, anchorMargin);
        log.info("::{}::,calculationOddsByMarginDiff 结束 bean={}",CommonUtil.getRequestId(bean.getParentId()),JSONObject.toJSONString(bean));
    }

    /**
     * @return void
     * @Description //根据描点修订赔率
     * @Param [bean, margin, odd, totalMargin, anchorMargin]
     * @Author sean
     * @Date 2021/5/23
     **/
    private void ifProbabilityOverResetOdds(RcsStandardMarketDTO bean, BigDecimal margin, StandardMarketOddsDTO odd, BigDecimal totalMargin, BigDecimal anchorMargin) {
        //是否超过margin范围 超过范围需要调整
        if (totalMargin.subtract(margin).abs().compareTo(new BigDecimal(NumberUtils.INTEGER_TWO)) == 1) {
            for (StandardMarketOddsDTO odds : bean.getMarketOddsList()) {
                if (odds.getOddsType().equalsIgnoreCase(odd.getOddsType())) {
                    BigDecimal probability = BigDecimal.ZERO;
                    if (totalMargin.compareTo(margin) == 1) {
                        probability = margin.add(new BigDecimal(NumberUtils.INTEGER_TWO)).subtract(anchorMargin);
                    } else {
                        probability = margin.subtract(new BigDecimal(NumberUtils.INTEGER_TWO)).subtract(anchorMargin);
                    }
                    BigDecimal oddsValue = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE).divide(probability, NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN);
                    odds.setOddsValue(oddsValue.multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
                    break;
                }
            }
        }
    }

    public void setDefaultAnchor(List<MarketMarginGapDtlDTO> marginGapDtlDTOList) {
        if (CollectionUtils.isNotEmpty(marginGapDtlDTOList)) {
            if (marginGapDtlDTOList.size() != 3) {
                return;
            }
            Integer count = 0;
            for (MarketMarginGapDtlDTO odds : marginGapDtlDTOList) {
                count += ObjectUtils.isEmpty(odds.getAnchor()) ? 0 : odds.getAnchor();
            }
            if (count != 2) {
                for (int i = 0; i < marginGapDtlDTOList.size(); i++) {
                    marginGapDtlDTOList.get(i).setAnchor(1);
                    if (i == 2) {
                        if (!(marginGapDtlDTOList.get(i).getOddsType().equalsIgnoreCase(BaseConstants.ODD_TYPE_X))) {
                            marginGapDtlDTOList.get(i).setAnchor(0);
                        } else {
                            marginGapDtlDTOList.get(i - 1).setAnchor(0);
                        }
                    }
                }
            }
            log.info("::{}::,设置自动描点={}", CommonUtil.getRequestId(),JSONObject.toJSONString(marginGapDtlDTOList));
        }
    }

    public void setDefaultAnchor(RcsStandardMarketDTO bean) {
        if (!ObjectUtils.isEmpty(bean) && CollectionUtils.isNotEmpty(bean.getMarketOddsList())) {
            if (bean.getMarketOddsList().size() != 3) {
                return;
            }
            List<StandardMarketOddsDTO> oddsVoList = bean.getMarketOddsList().stream().sorted(Comparator.comparing(StandardMarketOddsDTO::getOddsValue)).collect(Collectors.toList());
            Integer count = 0;
            for (StandardMarketOddsDTO odds : oddsVoList) {
                count += com.baomidou.mybatisplus.core.toolkit.ObjectUtils.isEmpty(odds.getAnchor()) ? 0 : odds.getAnchor();
            }
            if (count != 2) {
                for (int i = 0; i < oddsVoList.size(); i++) {
                    oddsVoList.get(i).setAnchor(1);
                    if (i == 2) {
                        if (!(oddsVoList.get(i).getOddsType().equalsIgnoreCase(BaseConstants.ODD_TYPE_X))) {
                            oddsVoList.get(i).setAnchor(0);
                        } else {
                            oddsVoList.get(i - 1).setAnchor(0);
                        }
                    }
                }
            }
            bean.setMarketOddsList(oddsVoList);
            log.info("::{}::,设置自动描点={}", CommonUtil.getRequestId(bean.getParentId()),JSONObject.toJSONString(bean));
        }
    }

    public static void main(String[] args) {
        TradeCommonService commonService = new TradeCommonService();
        RcsMatchMarketConfig config = JSONObject.parseObject("{\"awayAutoChangeRate\":\"0\",\"awayLevelFirstOddsRate\":0.03,\"awayLevelSecondOddsRate\":0.03,\"awayMarketValue\":0,\"awayMultiOddsRate\":0.03,\"awaySingleOddsRate\":0.03,\"balanceOption\":1,\"createTime\":1628096042000,\"dataSource\":1,\"homeLevelFirstMaxAmount\":1495,\"homeLevelFirstOddsRate\":0.03,\"homeLevelSecondMaxAmount\":2995,\"homeLevelSecondOddsRate\":0.03,\"homeMarketValue\":1,\"homeMultiMaxAmount\":2995,\"homeMultiOddsRate\":0.03,\"homeSingleMaxAmount\":1495,\"homeSingleOddsRate\":0.03,\"id\":18248489,\"isMultipleJumpMarket\":1,\"isMultipleJumpOdds\":1,\"isOpenJumpMarket\":1,\"isOpenJumpOdds\":1,\"margin\":0.2,\"marketBuildFlag\":false,\"marketIndex\":1,\"marketStatus\":1,\"marketType\":\"MY\",\"matchId\":2539222,\"matchType\":1,\"maxBetAmount\":1500,\"maxOdds\":-0.1,\"maxSingleBetAmount\":1500,\"minOdds\":0.01,\"oddChangeRule\":0,\"oddsList\":[{\"active\":\"\",\"dataSourceCode\":\"\",\"fieldOddsValue\":\"0.90\",\"margin\":10,\"nameExpressionValue\":\"1.00\",\"originalOddsValue\":0,\"probabilityOdds\":0,\"oddsType\":\"Over\",\"pdfieldOddsValue\":\"0.90\",\"hkfieldOddsValue\":0.8999999999999999},{\"active\":\"\",\"dataSourceCode\":\"\",\"fieldOddsValue\":\"0.90\",\"margin\":10,\"nameExpressionValue\":\"1.00\",\"originalOddsValue\":0,\"probabilityOdds\":0,\"oddsType\":\"Under\",\"pdfieldOddsValue\":\"0.90\",\"hkfieldOddsValue\":0.8999999999999999}],\"oddsType\":\"Under\",\"placeWaterDiff\":0,\"playId\":233,\"relevanceType\":0,\"subPlayId\":\"23306\",\"updateTime\":1628096430000}",RcsMatchMarketConfig.class);
        List<RcsStandardMarketDTO> playAllMarketList = JSONArray.parseArray("[{\"addition1\":\"1.25\",\"addition2\":\"46\",\"addition3\":\"60\",\"childStandardCategoryId\":23304,\"dataSourceCode\":\"PA\",\"id\":\"141050334492532124\",\"marketCategoryId\":233,\"marketOddsList\":[{\"active\":1,\"dataSourceCode\":\"PA\",\"id\":\"140431242418935151\",\"margin\":0.2,\"nameExpressionValue\":\"1.25\",\"oddsFieldsTemplateId\":737,\"oddsType\":\"Over\",\"oddsValue\":200000,\"orderOdds\":1,\"originalOddsValue\":200000,\"thirdOddsFieldSourceId\":\"1422769738732056578\"},{\"active\":1,\"dataSourceCode\":\"PA\",\"id\":\"149573821811384712\",\"margin\":0.2,\"nameExpressionValue\":\"1.25\",\"oddsFieldsTemplateId\":738,\"oddsType\":\"Under\",\"oddsValue\":180000,\"orderOdds\":2,\"originalOddsValue\":180000,\"thirdOddsFieldSourceId\":\"1422769738740445185\"}],\"marketType\":1,\"numberOfAddition1\":1.25,\"placeNum\":1,\"status\":1,\"thirdMarketSourceStatus\":0},{\"addition1\":\"0.75\",\"addition2\":\"61\",\"addition3\":\"75\",\"childStandardCategoryId\":23305,\"dataSourceCode\":\"PA\",\"id\":\"148028565828703140\",\"marketCategoryId\":233,\"marketOddsList\":[{\"active\":1,\"dataSourceCode\":\"PA\",\"id\":\"144642493759709946\",\"margin\":0.2,\"nameExpressionValue\":\"0.75\",\"oddsFieldsTemplateId\":737,\"oddsType\":\"Over\",\"oddsValue\":203000,\"orderOdds\":1,\"originalOddsValue\":203000,\"thirdOddsFieldSourceId\":\"1422769738765611009\"},{\"active\":1,\"dataSourceCode\":\"PA\",\"id\":\"149152029018604203\",\"margin\":0.2,\"nameExpressionValue\":\"0.75\",\"oddsFieldsTemplateId\":738,\"oddsType\":\"Under\",\"oddsValue\":177000,\"orderOdds\":2,\"originalOddsValue\":177000,\"thirdOddsFieldSourceId\":\"1422769738773999617\"}],\"marketType\":1,\"numberOfAddition1\":0.75,\"placeNum\":1,\"status\":1,\"thirdMarketSourceStatus\":0}]",RcsStandardMarketDTO.class);
        RcsStandardMarketDTO market = JSONObject.parseObject("{\"addition1\":\"1\",\"addition2\":\"76\",\"addition3\":\"90\",\"dataSourceCode\":\"PA\",\"marketCategoryId\":233,\"marketOddsList\":[{\"active\":1,\"anchor\":0,\"dataSourceCode\":\"PA\",\"margin\":0.2,\"nameExpressionValue\":\"1\",\"oddsFieldsTemplateId\":737,\"oddsType\":\"Over\",\"oddsValue\":190000,\"orderOdds\":1,\"originalOddsValue\":190000},{\"active\":1,\"anchor\":0,\"dataSourceCode\":\"PA\",\"margin\":0.2,\"nameExpressionValue\":\"1\",\"oddsFieldsTemplateId\":738,\"oddsType\":\"Under\",\"oddsValue\":190000,\"orderOdds\":2,\"originalOddsValue\":190000}],\"marketType\":1,\"numberOfAddition1\":1.0,\"placeNum\":1,\"placeNumStatus\":1,\"status\":0,\"thirdMarketSourceStatus\":0,\"tradeType\":1}",RcsStandardMarketDTO.class);
        List<StandardMarketDTO> list = commonService.packageMarketList(config,playAllMarketList,market);
//        config.setMarketId(1L);
//        config.setMargin(new BigDecimal(116));
//        RcsStandardMarketDTO market = new RcsStandardMarketDTO();
//        List<StandardMarketOddsDTO> marketOddsList = JSONArray.parseArray("[{\"active\":1,\"addition1\":\"0\",\"addition2\":\"\",\"addition3\":\"\",\"addition4\":\"\",\"anchor\":0,\"dataSourceCode\":\"PA\",\"margin\":110.0,\"marketDiffValue\":0.0,\"name\":\"安道尔\",\"oddsFieldsTemplateId\":226,\"oddsType\":\"2\",\"oddsValue\":231000,\"orderOdds\":2,\"originalOddsValue\":261254,\"probabilityOdds\":231000,\"thirdOddsFieldSourceId\":\"1394997909921378305\"},{\"active\":1,\"addition1\":\"0\",\"addition2\":\"\",\"addition3\":\"\",\"addition4\":\"\",\"anchor\":1,\"dataSourceCode\":\"PA\",\"margin\":110.0,\"marketDiffValue\":2.0,\"name\":\"尤文图特俱乐部\",\"oddsFieldsTemplateId\":225,\"oddsType\":\"1\",\"oddsValue\":153000,\"orderOdds\":1,\"originalOddsValue\":162012,\"probabilityOdds\":149000,\"thirdOddsFieldSourceId\":\"1394997909933961217\"}]", StandardMarketOddsDTO.class);
//        market.setMarketOddsList(marketOddsList);
//        market.setId(config.getMarketId().toString());
//        new TradeCommonService().getNotAnchor(marketOddsList);
        System.out.println(JSONObject.toJSONString(list));
    }

    /**
     * @return void
     * @Description //赔率变化情况概率差和水差
     * @Param [config, oddsList]
     * @Author sean
     * @Date 2021/5/23
     **/
    public void ifOddsChangeAndClearConfig(RcsMatchMarketConfig config, List<OddsValueVo> oddsValueList, List<StandardSportMarketOdds> oddsList, Long sportId) {
        // 多项盘清投注项概率差和平衡值
        List<RcsMatchMarketProbabilityConfig> ps = Lists.newArrayList();
        List<Map<String, Object>> maps = Lists.newArrayList();
        for (StandardSportMarketOdds odds : oddsList) {
            if (CollectionUtils.isNotEmpty(oddsValueList)) {
                for (OddsValueVo vo : oddsValueList) {
                    Map<String, Object> map = Maps.newHashMap();
                    map.put("value", vo.getValue());
                    map.put("oddsType", vo.getOddsType());
                    maps.add(map);
                }
            } else if (CollectionUtils.isNotEmpty(config.getOddsList())) {
                maps = config.getOddsList();
            }
            for (Map<String, Object> map : maps) {
                if (ObjectUtils.isEmpty(map.get("value"))) {
                    map.put("value", map.get("fieldOddsValue"));
                }
                Integer oddsValue = new BigDecimal(map.get("value").toString()).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
                if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                    oddsValue = rcsOddsConvertMappingService.getEUOddsInteger(map.get("value").toString());
                }
                if (odds.getOddsType().equalsIgnoreCase(map.get("oddsType").toString()) &&
                        odds.getOddsValue().intValue() != oddsValue) {
                    RcsMatchMarketProbabilityConfig probabilityConfig = new RcsMatchMarketProbabilityConfig(config.getMatchId(), config.getPlayId(), config.getMarketId());
                    probabilityConfig.setOddsType(odds.getOddsType());
                    probabilityConfig.setProbability(BigDecimal.ZERO);
                    ps.add(probabilityConfig);
                    break;
                }
            }
        }
        if (CollectionUtils.isNotEmpty(ps)){

            List<ClearSubDTO> configs = Lists.newArrayList();
            for (RcsMatchMarketProbabilityConfig c:ps){
                ClearSubDTO matchMarketConfig = new ClearSubDTO();
                matchMarketConfig.setMatchId(config.getMatchId());
                matchMarketConfig.setPlayId(config.getPlayId());
                matchMarketConfig.setMarketId(config.getMarketId());
                matchMarketConfig.setOddsType(c.getOddsType());
                configs.add(matchMarketConfig);
            }
            ClearDTO clearDTO = new ClearDTO();
            clearDTO.setType(0);
            clearDTO.setClearType(2);
            clearDTO.setMatchId(config.getMatchId());
            clearDTO.setList(configs);
            producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG", null, UuidUtils.generateUuid(), clearDTO);
        }
    }

    /**
     * 调用融合接口，冠军赛事，操盘方式切换
     *
     * @param config
     */
    public void championMatchTradeType(MarketStatusUpdateVO config) {
        OutrightTradeTypeConfigDTO outrightTradeTypeConfigDTO = new OutrightTradeTypeConfigDTO();
        outrightTradeTypeConfigDTO.setStandardMatchId(config.getMatchId());
        outrightTradeTypeConfigDTO.setStandardMarketId(Long.valueOf(config.getMarketId()));
        outrightTradeTypeConfigDTO.setTradeType(config.getTradeType());
        if (config.getTradeLevel().intValue() == 3) {
            //盘口值转换成融合需要的值
            config.setTradeLevel(0);
        }
        outrightTradeTypeConfigDTO.setLevel(config.getTradeLevel());
        String linkId = CommonUtils.getLinkId("championTradeType");
        DataRealtimeApiUtils.handleApi(linkId, outrightTradeTypeConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return outrightTradeConfigApi.putOutrightTradeTypeConfig(request);
            }
        });
    }


    /**
     * 调用融合接口，冠军赛事，盘口开关封锁
     *
     * @param config
     */
    public void championMatchTradeStatus(MarketStatusUpdateVO config) {
        OutrightTradeMarketConfigDTO outrightTradeTypeConfigDTO = new OutrightTradeMarketConfigDTO();
        outrightTradeTypeConfigDTO.setStandardMatchId(config.getMatchId());
        outrightTradeTypeConfigDTO.setStandardMarketId(Long.valueOf(config.getMarketId()));
        outrightTradeTypeConfigDTO.setMarketStatus(config.getMarketStatus());
        String linkId = CommonUtils.getLinkId("championTradeStatus");
        DataRealtimeApiUtils.handleApi(linkId, outrightTradeTypeConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return outrightTradeConfigApi.putOutrightTradeMarketConfig(request);
            }
        });
    }

    /**
     * 调用融合接口，冠军赛事，投注项开关封锁
     *
     * @param config
     */
    public void championMatchTradeBetItemStatus(MarketStatusUpdateVO config) {
        OutrightTradeOddsConfigDTO outrightTradeOddsConfigDTO = new OutrightTradeOddsConfigDTO();
        outrightTradeOddsConfigDTO.setStandardMatchId(config.getMatchId());
        outrightTradeOddsConfigDTO.setStandardMarketId(Long.valueOf(config.getMarketId()));
        outrightTradeOddsConfigDTO.setStandardMarketOddsId(Long.valueOf(config.getOddsId()));
        outrightTradeOddsConfigDTO.setOddsStatus(config.getMarketStatus());
        String linkId = CommonUtils.getLinkId("championTradeBetItemStatus");
        DataRealtimeApiUtils.handleApi(linkId, outrightTradeOddsConfigDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return outrightTradeConfigApi.putOutrightTradeOddsConfig(request);
            }
        });
    }
    /**
     * @Description   //封装国际化
     * @Param [market]
     * @Author  sean
     * @Date   2021/6/25
     * @return void
     **/
    public void setI18nName(StandardMarketDTO market) {
        List<Map<String,String>> map = standardSportMarketMapper.queryMarketI18nNames(market.getId());
        List<I18nItemDTO> mI18nNames = Lists.newArrayList();
        try{
            for (Map<String,String> m :map){
                for (StandardMarketOddsDTO dto : market.getMarketOddsList()){
                    if ((!ObjectUtils.isEmpty(m.get("i18n_names"))) && (!m.get("i18n_names").equalsIgnoreCase("null"))){
                        List<I18nItemDTO> i18nNames = JSONArray.parseArray(m.get("i18n_names"),I18nItemDTO.class);
                        dto.setI18nNames(i18nNames);
                    }
                }
                if (CollectionUtils.isEmpty(mI18nNames)){
                    mI18nNames = JSONArray.parseArray(m.get("mNames"),I18nItemDTO.class);
                }
            }
            market.setI18nNames(mI18nNames);
        }catch (Exception e){
            log.error("::{}::没有国际化信息{}", CommonUtil.getRequestId(),e.getMessage(), e);
        }
    }

}
