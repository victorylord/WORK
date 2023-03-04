package com.panda.sport.rcs.trade.service;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import com.panda.merge.dto.StandardMarketDTO;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.pojo.dto.ClearDTO;
import com.panda.sport.rcs.pojo.dto.ClearSubDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.nacos.common.util.UuidUtils;
import com.google.common.collect.Lists;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.dto.MarketMarginDtlDTO;
import com.panda.merge.dto.MarketMarginGapDtlDTO;
import com.panda.merge.dto.MarketPlaceDtlDTO;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.TradeMarketAutoDiffConfigDTO;
import com.panda.merge.dto.TradeMarketAutoDiffConfigItemDTO;
import com.panda.merge.dto.TradeMarketConfigItemDTO;
import com.panda.merge.dto.TradeMarketUiConfigDTO;
import com.panda.merge.dto.TradePlaceNumAutoDiffConfigItemDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.trade.enums.BasketBallPlayIdScoreTypeEnum;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

import lombok.extern.slf4j.Slf4j;

/**
 * @Description   //操盘的一些校验
 * @Param
 * @Author  sean
 * @Date   2021/1/9
 * @return
 **/
@Service
@Slf4j
public class TradeVerificationService {
    @Autowired
    MarketStatusService marketStatusService;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;
    //3投注项和多投注项玩法
    public static List<Integer> FOOTBALL_THREE_MANY_ITEM_PLAYS = Arrays.asList(1,3,6,7,8,9,13,14,16,17,20,21,22,23,25,27,28,29,30,31,32,35,36,68,69,70,71,72,73,74,85,95,101,102,103,104,105,106,107,108,111,112,117,119,120,125,126,129,137,141,148,149,150,151,152,222,223,224,225,226,227,228,230,231,235,236,237,238,239,241,310,311,318,319,320,321,322,323,333,340,341,342,343,10001,10002,10003,10004,10005,10006,10007,10008,10009,10010,10011,10012,344,345,346,347,348,349,350,351,353,357,360,363,364,365,366,361,362);

    public void checkMaxAndMinOdds(RcsMatchMarketConfig limitMarketConfig, RcsMatchMarketConfig config) {
        for (Map<String,Object> map : config.getOddsList()){
            if (ObjectUtils.isEmpty(map.get("fieldOddsValue"))){
                throw new RcsServiceException("列表赔率不能为空");
            }else if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
                String maxOdds =  rcsOddsConvertMappingService.maxEUOddsByMYOdds(limitMarketConfig.getMaxOdds().toPlainString());
                String minOdds = rcsOddsConvertMappingService.minEUOddsByMYOdds(limitMarketConfig.getMinOdds().toPlainString());
                String fieldOddsValue = rcsOddsConvertMappingService.getEUOdds(map.get("fieldOddsValue").toString());
                if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(maxOdds)) == 1){
                    throw new RcsServiceException("修改的赔率小于模板设置的最大值，保存失败"+fieldOddsValue);
                }else if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(minOdds)) == -1){
                    throw new RcsServiceException("修改的赔率小于模板设置的最小值，保存失败"+fieldOddsValue);
                }
            }else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())){
                if (new BigDecimal(map.get("fieldOddsValue").toString()).compareTo(config.getMaxOdds()) == 1){
                    throw new RcsServiceException("修改的赔率小于模板设置的最大值，保存失败"+map.get("fieldOddsValue").toString());
                }else if (new BigDecimal(map.get("fieldOddsValue").toString()).compareTo(config.getMinOdds()) == -1){
                    throw new RcsServiceException("修改的赔率小于模板设置的最小值，保存失败"+map.get("fieldOddsValue").toString());
                }
            }
        }
    }
    /**
     * @Description   //设置盘口值
     * @Param [marketConfig, addition1]
     * @Author  Sean
     * @Date  9:26 2020/10/4
     * @return void
     **/
    public void setMarketValue(RcsMatchMarketConfig marketConfig, String addition1) {
        if (org.apache.commons.lang3.StringUtils.isNotBlank(addition1)) {
            if (Double.parseDouble(addition1) >= NumberUtils.DOUBLE_ZERO) {
                marketConfig.setAwayMarketValue(new BigDecimal(addition1));
                marketConfig.setHomeMarketValue(new BigDecimal(NumberUtils.INTEGER_ZERO));
            } else if (Double.parseDouble(addition1) < 0) {
                marketConfig.setHomeMarketValue(new BigDecimal(addition1).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)));
                marketConfig.setAwayMarketValue(new BigDecimal("0"));
            }
        }
    }
    /**
     * @Description   //校验数据合法性
     * @Param [config]
     * @Author  Sean
     * @Date  17:03 2020/10/7
     * @return java.math.BigDecimal
     **/
    public void verifyData(RcsMatchMarketConfig config,RcsTournamentTemplatePlayMargain templateMatchConfig,BigDecimal margin) {
        // 赔率最大最小验证
        String maxOdds = "";
        String minOdds = "";
        //2129需求 网球乒乓球的部分玩法不设0.3水的跳水上限；
        Boolean flag = this.tennisAndPingPongNewPlayNoRadioLimit(config.getSportId().longValue(),config.getPlayId());;
        if (config.getMaxOdds() == null || config.getMinOdds() == null) {
            throw new RcsServiceException("最大最小赔率不能为空");
        }else if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
            maxOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(config.getMaxOdds().toPlainString());
            minOdds = rcsOddsConvertMappingService.minEUOddsByMYOdds(config.getMinOdds().toPlainString());
            if (new BigDecimal(maxOdds).compareTo(new BigDecimal(minOdds)) < 0){
                throw new RcsServiceException("马赔最小赔率"+minOdds+"比最大赔率"+maxOdds+"大");
            }
        }else if (config.getMaxOdds().compareTo(config.getMinOdds()) < 0) {
            throw new RcsServiceException("欧赔最小赔率"+config.getMinOdds()+"比最大赔率"+config.getMaxOdds()+"大");
        }

        StandardMatchInfo info = standardMatchInfoMapper.selectById(config.getMatchId());
        if (info == null) {
            throw new RcsServiceException("赛事不存在"+config.getMatchId());
        }
        Integer matchType = NumberUtils.INTEGER_ONE;
        if (RcsConstant.LIVE_MATCH_STATUS.contains(info.getMatchStatus())) {
            matchType = NumberUtils.INTEGER_ZERO;
        }
        config.setMatchType(matchType);
        // 校验赔率合法性
        List<Map<String, Object>> oddsList = config.getOddsList();
        if (DataSourceTypeEnum.AUTOMATIC.getValue() != config.getDataSource().intValue()) {
            //1：如果是欧赔 赔率小于1不允许进行操作
            if (config.getMarketType().equals(MarketKindEnum.Europe.getValue())) {
                for (Map<String, Object> map : oddsList) {
                    String fieldOddsValue = map.get("fieldOddsValue").toString();
                    if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(NumberUtils.INTEGER_ONE)) <= NumberUtils.INTEGER_ZERO) {
                    		throw new RcsServiceException("欧赔设置不合理"+fieldOddsValue);
               		}
                }
            } else {
                //两项盘
                for (Map<String, Object> map : oddsList) {
                    String fieldOddsValue = map.get("fieldOddsValue").toString();
                    if (new BigDecimal(fieldOddsValue).compareTo(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)) == NumberUtils.INTEGER_ZERO){
                        fieldOddsValue = new BigDecimal(fieldOddsValue).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).toPlainString();
                    }
                    String oddsValue = rcsOddsConvertMappingService.getEUOdds(fieldOddsValue);
                    if (new BigDecimal(oddsValue).compareTo(new BigDecimal(NumberUtils.INTEGER_ZERO)) == 0 ){
                        throw new RcsServiceException("欧赔设置不合理"+fieldOddsValue);
                    }
                }
            }
        }
        //4： 验证magin值 篮球手动模式独赢不校验
        boolean convert = MarginUtils.convert(oddsList, config.getDataSource(), config.getMarketType(), margin,config.getPlayId());
        if (!convert && !flag) {
            throw new RcsServiceException("margin校验没通过");
        }

        if (!flag){
            //水差设置
            if (!TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()) &&
                    config.getAwayAutoChangeRate() != null &&
                    new BigDecimal(config.getAwayAutoChangeRate()).abs()
                            .compareTo(templateMatchConfig.getOddsMaxValue()) == 1) {
                throw new RcsServiceException(String.format(TradeConstant.ODDS_OUT_OF_LIMIT,templateMatchConfig.getOddsMaxValue().negate().toPlainString(),templateMatchConfig.getOddsMaxValue().toPlainString(),config.getAwayAutoChangeRate()));
            }
            //水差设置
            if (config.getOddsList().size() == 2 &&
                    config.getAwayAutoChangeRate() != null &&
                    MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType()) &&
                    DataSourceTypeEnum.AUTOMATIC.getValue().intValue() == config.getDataSource() &&
                    new BigDecimal(config.getAwayAutoChangeRate()).abs()
                            .compareTo(templateMatchConfig.getOddsMaxValue()) == 1) {
                throw new RcsServiceException(String.format(TradeConstant.ODDS_OUT_OF_LIMIT,templateMatchConfig.getOddsMaxValue().negate().toPlainString(),templateMatchConfig.getOddsMaxValue().toPlainString(),config.getAwayAutoChangeRate()));
            }
        }
        this.checkDataRange(config,NumberUtils.INTEGER_TWO);
        //最大投注金额校验
        if(ObjectUtils.isEmpty(config.getMaxSingleBetAmount())){
            throw new RcsServiceException("最大投注金额不能为空");
        }
        //最大投注金额校验和margin
        checkBetMaxAndMargin(config);
    }
    /**
     * @Description   //校验数据合法性
     * @Param [config]
     * @Author  Sean
     * @Date  17:03 2020/10/7
     * @return java.math.BigDecimal
     **/
    public void verifyData(RcsMatchMarketConfig config,RcsTournamentTemplatePlayMargain templateMatchConfig,BigDecimal margin,Integer sportId) {
        verifyData(config,templateMatchConfig,margin);
    }
    /**
     * @Description
     *  1、大、单、是为上盘
     *  2、小、双、否为下盘
     *  4、让球盘
     *      a、让球方为上盘，受让方为下盘
     *      b、让0球时赔率小的为上盘，赔率大的为下盘
     *      c、赔率相同的情况下，主队上盘、客队下盘
     * @Param [oddsVoList, playOptionsId]
     * @Author  Sean
     * @Date  10:34 2020/7/5
     * @return java.lang.String
     **/
    public String getOddsType(StandardMarketDTO market){
        log.info("::{}::根据投注项获取调配盘，market：{}", CommonUtil.getRequestId(market.getId()), JSONObject.toJSONString(market));
        String oddsType = BaseConstants.ODD_TYPE_2;
        if (StringUtils.isNotBlank(market.getAddition1()) &&
                TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(market.getMarketCategoryId().intValue())){
            oddsType = Double.parseDouble(market.getAddition1()) > NumberUtils.INTEGER_ZERO ? BaseConstants.ODD_TYPE_1 : BaseConstants.ODD_TYPE_2;
        }else {
            List<StandardSportMarketOdds> odds = JSONArray.parseArray(JSONArray.toJSONString(market.getMarketOddsList()),StandardSportMarketOdds.class);
            odds.forEach(e -> e.setAddition1(market.getAddition1()));
            oddsType = getOddsType(odds);
        }
        return oddsType;
    }
    /**
     * @Description
     *  1、大、单、是为上盘
     *  2、小、双、否为下盘
     *  4、让球盘
     *      a、让球方为上盘，受让方为下盘
     *      b、让0球时赔率小的为上盘，赔率大的为下盘
     *      c、赔率相同的情况下，主队上盘、客队下盘
     * @Param [oddsVoList, playOptionsId]
     * @Author  Sean
     * @Date  10:34 2020/7/5
     * @return java.lang.String
     **/
    public String getOddsType(List<StandardSportMarketOdds> odds){
        log.info("::{}::根据投注项获取调配盘，config：{}",CommonUtil.getRequestId(), JSONObject.toJSONString(odds));
        String oddsType = BaseConstants.ODD_TYPE_2;
        if (!ObjectUtils.isEmpty(odds)){
            for (StandardSportMarketOdds odd : odds){
                if (BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(odd.getOddsType()) ||
                        BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(odd.getOddsType())){
                    oddsType = BaseConstants.ODD_TYPE_UNDER;
                    break;
                } else if (BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(odd.getOddsType()) ||
                        BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(odd.getOddsType())){
                    oddsType = BaseConstants.ODD_TYPE_EVEN;
                    break;
                }else if (BaseConstants.ODD_TYPE_YES.equalsIgnoreCase(odd.getOddsType()) ||
                        BaseConstants.ODD_TYPE_NO.equalsIgnoreCase(odd.getOddsType())){
                    oddsType = BaseConstants.ODD_TYPE_NO;
                    break;
                }else if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(odd.getOddsType())){
                    oddsType = BaseConstants.ODD_TYPE_X;
                    break;
                }else if (StringUtils.isNotBlank(odd.getAddition1())){
                    BigDecimal value = new BigDecimal(odd.getAddition1());
                    if (value.compareTo(BigDecimal.ZERO) <= 0){
                        oddsType = BaseConstants.ODD_TYPE_2;
                    }else {
                        oddsType = BaseConstants.ODD_TYPE_1;
                    }
                    break;
                }
            }

        }
        return oddsType;
    }
    /**
     * @Description   //获取篮球下盘
     * @Param [marketOddsType]
     * @Author  sean
     * @Date   2021/1/14
     * @return java.lang.String
     **/
    public String getBasketBallUnderOddsType(String marketOddsType){
        log.info("::{}::根据投注项获取调配盘，config：{}",CommonUtil.getRequestId(),JSONObject.toJSONString(marketOddsType));
        String oddsType = BaseConstants.ODD_TYPE_2;
        if (StringUtils.isNotBlank(marketOddsType)){
            if (BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(marketOddsType) ||
                    BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(marketOddsType)){
                oddsType = BaseConstants.ODD_TYPE_UNDER;
            } else if (BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(marketOddsType) ||
                    BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(marketOddsType)){
                oddsType = BaseConstants.ODD_TYPE_EVEN;
            }else if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(marketOddsType) ||
                    BaseConstants.ODD_TYPE_2.equalsIgnoreCase(marketOddsType)){
                oddsType = BaseConstants.ODD_TYPE_2;
            }else if (BaseConstants.ODD_TYPE_YES.equalsIgnoreCase(marketOddsType) ||
                    BaseConstants.ODD_TYPE_NO.equalsIgnoreCase(marketOddsType)){
                oddsType = BaseConstants.ODD_TYPE_NO;
            }
        }
        return oddsType;
    }
    /**
     * @Description   //校验最大可投和margin
     * @Param [marketConfig, matchType]
     * @Author  Sean
     * @Date  17:38 2020/10/11
     * @return void
     **/
    public void checkBetMaxAndMargin(RcsMatchMarketConfig marketConfig) {
        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(marketConfig),RcsMatchMarketConfig.class);
        if (NumberUtils.INTEGER_TWO.intValue() == config.getMatchType()){
            config.setMatchType(NumberUtils.INTEGER_ZERO);
        }
        //查询联赛配置
        RcsTournamentTemplatePlayMargainRef tournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainMapper.queryMarginByPlayId(config);
        if (ObjectUtils.isEmpty(tournamentTemplatePlayMargin)){
            log.info("::{}::联赛配置为空,matchType={},playId={}",CommonUtil.getRequestId(marketConfig.getMatchId()),marketConfig.getMatchType(),marketConfig.getPlayId());
            return;
        }
        if (ObjectUtils.isEmpty(tournamentTemplatePlayMargin.getOrderSinglePayVal()) ||
                ObjectUtils.isEmpty(tournamentTemplatePlayMargin.getMargain())) {
            String msg = "联赛配置为空matchId=%s,playId=%s,matchType=%s";
            throw new RcsServiceException(String.format(msg,config.getMatchId(),config.getPlayId(),config.getMatchType()));
        }else {
            if(config.getMaxSingleBetAmount() > tournamentTemplatePlayMargin.getOrderSinglePayVal()){
                throw new RcsServiceException("最大投注金额不能大于联赛配置");
            }
            if(MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType()) &&
                    (config.getMargin().compareTo(new BigDecimal(tournamentTemplatePlayMargin.getMargain()).multiply(new BigDecimal(TradeConstant.MAX_MARGIN))) == 1) ||
                    (config.getMargin().compareTo(new BigDecimal(tournamentTemplatePlayMargin.getMargain()).multiply(new BigDecimal(TradeConstant.MIN_MARGIN))) == -1)){
                throw new RcsServiceException("新spread不能超出[0.5*最初spread,1.5*最初spread],malaySpread非法，调整无效！");
            }
            if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType()) &&
                    (config.getMargin().compareTo(new BigDecimal(TradeConstant.EU_MAX_MARGIN)) == NumberUtils.INTEGER_ONE ||
                            config.getMargin().compareTo(new BigDecimal(TradeConstant.EU_MIN_MARGIN)) == NumberUtils.INTEGER_MINUS_ONE)){
                throw new RcsServiceException("margin取值范围101-150");
            }
        }
    }
    /**
     * @return void
     * @Description // 校验数据范围
     * @Param [config]
     * @Author Sean
     * @Date 19:40 2020/10/9
     **/
    public void checkDataRange(RcsMatchMarketConfig config,Integer sportId) {
        if (ObjectUtils.isEmpty(config.getBalanceOption())) {
            throw new RcsServiceException("自动跳分机制不能为空");
        }
        //水差/赔率变化率
        if (MarketKindEnum.Malaysia.getValue().equals(config.getMarketType())) {
            if (ObjectUtils.isEmpty(config.getOddChangeRule())) {
                throw new RcsServiceException("平衡值规则不能为空");
            }
            //两项盘
            if (config.getOddChangeRule() == 0) {
                // 单枪累计校验
                checkSingleAndCumulativeParm(config,sportId);
            } else {
                // 一级二级校验
                checkLevelParm(config,sportId);
            }
        } else {
            if (config.getHomeLevelFirstMaxAmount() == null) {
                throw new RcsServiceException("独赢盘一级限额不能为空");
            }
            //独赢盘
            if (config.getHomeLevelFirstOddsRate() == null) {
                throw new RcsServiceException("独赢盘赔率变化率不可以为空");
            } else if (config.getHomeLevelFirstOddsRate().abs().compareTo(new BigDecimal("30")) > 0) {
                throw new RcsServiceException("独赢盘赔率变化率绝对值需要介于0-30");
            }
            config.setHomeLevelFirstOddsRate(config.getHomeLevelFirstOddsRate().abs());
        }
    }
    /**
     * @Description   //单枪累计校验
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/3
     * @return void
     **/
    public void checkSingleAndCumulativeParm(RcsMatchMarketConfig config,Integer sportId){
        //累计/单枪
        if (config.getHomeSingleMaxAmount() == null ||
                config.getHomeMultiMaxAmount() == null) {
            throw new RcsServiceException("两项盘单枪/累计限额不能为空");
        }
        //累计/单枪
        if (config.getAwaySingleOddsRate() == null ||
                config.getAwayMultiOddsRate() == null ||
                config.getHomeSingleOddsRate() == null ||
                config.getHomeMultiOddsRate() == null) {
            throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率不能为空");
        } else if (config.getAwaySingleOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
                || config.getAwayMultiOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
                || config.getHomeSingleOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
                || config.getHomeMultiOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0) {
            throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率绝对值需要介于0-0.15");
        }
        config.setAwaySingleOddsRate(config.getAwaySingleOddsRate().abs());
        config.setAwayMultiOddsRate(config.getAwayMultiOddsRate().abs());
        config.setHomeSingleOddsRate(config.getHomeSingleOddsRate().abs());
        config.setHomeMultiOddsRate(config.getHomeMultiOddsRate().abs());
    }
    /**
     * @Description   //一级二级参数校验
     * @Param [config]
     * @Author  sean
     * @Date   2021/2/3
     * @return void
     **/
    public void checkLevelParm(RcsMatchMarketConfig config,Integer sportId){
        if (config.getHomeLevelFirstMaxAmount() == null ||
                config.getHomeLevelSecondMaxAmount() == null) {
            throw new RcsServiceException("两项盘一级/二级限额不能为空");
        }
        //一级/二级
        if (config.getAwayLevelFirstOddsRate() == null ||
                config.getAwayLevelSecondOddsRate() == null ||
                config.getHomeLevelFirstOddsRate() == null || config.getHomeLevelSecondOddsRate() == null) {
            throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率不能为空");
        } else if (config.getAwayLevelFirstOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
                || config.getAwayLevelSecondOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
                || config.getHomeLevelFirstOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0
                || config.getHomeLevelSecondOddsRate().abs().compareTo(new BigDecimal("0.15")) > 0) {
            throw new RcsServiceException("两项盘自动操盘水差/手动操盘赔率变化率绝对值需要介于0-0.15");
        }
        config.setAwayLevelFirstOddsRate(config.getAwayLevelFirstOddsRate().abs());
        config.setAwayLevelSecondOddsRate(config.getAwayLevelSecondOddsRate().abs());
        config.setHomeLevelFirstOddsRate(config.getHomeLevelFirstOddsRate().abs());
        config.setHomeLevelSecondOddsRate(config.getHomeLevelSecondOddsRate().abs());
    }

    /**
     * @return void
     * @Description //发送盘口配置到融合
     * @Param [config]
     * @Author Sean
     * @Date 11:45 2020/10/21
     **/
    public void sendMarketConfigToDataCenter(RcsMatchMarketConfig conf,List<MatchMarketPlaceConfig> placeConfigs,BigDecimal margin) {

        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(conf),RcsMatchMarketConfig.class);
        config.setMargin(margin);

        TradeMarketUiConfigDTO dto = createRequestDto(config);
        // 设置最大最小赔率
        addOddsLimit(config, dto);
        // 设置水差
//        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(config.getMatchId());

        // 设置margin
        addMarginConfig(config, dto);
        // 811margin 自动才走这里
        addMarginGapConfig(config, dto);

        // 设置水差
        if (TradeEnum.isAuto(config.getDataSource().intValue()) &&
                SportIdEnum.isFootball(config.getSportId()) &&
                (!ObjectUtils.isEmpty(config.getMarketId())) &&
                config.getOddsList().size() == 2) {
            updateWater(config, dto);
        }else if (!SportIdEnum.isFootball(config.getSportId())){
            updatePlaceWater(config, dto,placeConfigs);
        }
        // 设置盘口位置集合
        if (TradeEnum.isAuto(config.getDataSource().intValue())){
            addMarketPlaceList(config, dto);
            Long subPlayId = ObjectUtils.isEmpty(config.getSubPlayId())?null:Long.parseLong(config.getSubPlayId());
            tradeStatusService.updatePlaceStatus(conf.getSportId().longValue(),config.getMatchId(), config.getPlayId(),subPlayId ,config.getMarketIndex(), config.getMarketStatus());
        }
        //推送MQ消息给融合
        updateMarginToDataCenterMQ(config,dto);
        DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketConfigApi.putTradeMarketUiConfig(request);
            }
        });
    }

    private void updateMarginToDataCenterMQ(RcsMatchMarketConfig config, TradeMarketUiConfigDTO apiConfig) {
        log.info("::{}::同步联赛模板Margin数据，config:{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), config);
        Long playId = config.getPlayId();
        Integer sportId = config.getSportId();
        //足球 篮球
        if (!SportIdEnum.isFootball(Long.valueOf(sportId)) && !SportIdEnum.isBasketball(Long.valueOf(sportId))) {
            return;
        }
        List<Integer> footBallMarginPlayList = Stream.of(5, 43, 352, 142).collect(Collectors.toList());
        List<Integer> basketBallMarginPlayList = Stream.of(37, 43, 142, 48, 54, 60, 66).collect(Collectors.toList());
        if (!FOOTBALL_THREE_MANY_ITEM_PLAYS.contains(playId.intValue()) && !footBallMarginPlayList.contains(playId.intValue())
                && !basketBallMarginPlayList.contains(playId.intValue())) {
            return;
        }
        log.info("::{}::同步联赛模板Margin数据，apiConfig:{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), apiConfig);
        TradeMarketItemConfig tradeMarketItemConfig = new TradeMarketItemConfig();
        tradeMarketItemConfig.setStandardMatchInfoId(apiConfig.getStandardMatchInfoId());
        tradeMarketItemConfig.setStandardCategoryId(apiConfig.getStandardCategoryId());
        tradeMarketItemConfig.setChildStandardCategoryId(apiConfig.getChildStandardCategoryId());
        tradeMarketItemConfig.setPlaceNum(apiConfig.getPlaceNum());
        tradeMarketItemConfig.setMarketType(apiConfig.getMarketType());
        tradeMarketItemConfig.setMargin(config.getMargin());
        tradeMarketItemConfig.setMaxOddsValue(config.getMaxOdds());
        tradeMarketItemConfig.setMinOddsValue(config.getMinOdds());
        String linkId = com.panda.sport.rcs.utils.CommonUtils.getLinkId("MARKET_ITEM_MARGIN_CONFIG");
        Request<TradeMarketItemConfig> request = new Request<>();
        request.setData(tradeMarketItemConfig);
        request.setGlobalId(linkId);
        log.info("::{}::同步联赛模板Margin数据，发送到融合:linkId:{} ************ Message:{}", linkId, linkId, JSONObject.toJSON(request));
        producerSendMessageUtils.sendMessage("MARKET_ITEM_MARGIN_CONFIG", linkId, String.valueOf(apiConfig.getStandardMatchInfoId()), JSONObject.toJSON(request));
    }
    /**
     * @Description   // margin和水差等811需求
     * @Param [config, dto, standardMatchInfo]
     * @Author  sean
     * @Date   2021/5/2
     * @return void
     **/
    private void addMarginGapConfig(RcsMatchMarketConfig config, TradeMarketUiConfigDTO dto) {
        List<MarketMarginGapDtlDTO> marginGapDtlDTOList = Lists.newArrayList();
        if (TradeEnum.isAuto(config.getDataSource().intValue()) &&
            MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType()) && config.getOddsList().size() ==3){
            buildMarginGap(config, marginGapDtlDTOList);
            dto.setMarginGapDtlDTOList(marginGapDtlDTOList);
        }
    }
    /**
     * @Description   //组装margin数据
     * @Param [config, marginGapDtlDTOList]
     * @Author  sean
     * @Date   2021/5/7
     * @return void
     **/
    private void buildMarginGap(RcsMatchMarketConfig config, List<MarketMarginGapDtlDTO> marginGapDtlDTOList) {
        for (Map<String, Object> map : config.getOddsList()) {
            MarketMarginGapDtlDTO dtlDTO = JSONObject.parseObject(JSONObject.toJSONString(map), MarketMarginGapDtlDTO.class);
            dtlDTO.setDiffValue(ObjectUtils.isEmpty(map.get("marketDiffValue")) ? null : Double.parseDouble(map.get("marketDiffValue").toString()));
            dtlDTO.setMargin(config.getMargin().doubleValue());
            marginGapDtlDTOList.add(dtlDTO);
        }
    }

    /**
     * @Description   //设置玩法水差
     * @Param [config, dto]
     * @Author  sean
     * @Date   2021/1/13
     * @return void
     **/
    public void updatePlaceWater(RcsMatchMarketConfig config, TradeMarketUiConfigDTO dto,List<MatchMarketPlaceConfig> placeConfigs) {
        List<TradePlaceNumAutoDiffConfigItemDTO> itemDTOS = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(placeConfigs)){
            for (MatchMarketPlaceConfig placeConfig : placeConfigs){
                TradePlaceNumAutoDiffConfigItemDTO itemDTO = new TradePlaceNumAutoDiffConfigItemDTO();
                itemDTO.setOddType(config.getOddsType());
                itemDTO.setPlaceNum(placeConfig.getPlaceNum());
                itemDTO.setMarketCategoryId(config.getPlayId());
                if (StringUtils.isNotBlank(config.getSubPlayId())){
                    itemDTO.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
                }
                itemDTO.setDiffValue(placeConfig.getPlaceMarketDiff().doubleValue());
                itemDTOS.add(itemDTO);
            }
        }
        dto.setPlaceNumDiffConfigs(itemDTOS);
    }

    /**
     * @return void
     * @Description //构建请求参数类
     * @Param [config]
     * @Author Sean
     * @Date 10:47 2020/10/21
     **/
    public TradeMarketUiConfigDTO createRequestDto(RcsMatchMarketConfig config) {

        TradeMarketUiConfigDTO dto = new TradeMarketUiConfigDTO();
        dto.setStandardMatchInfoId(config.getMatchId());
        // 标准赛事类型：0.普通赛事、1.冠军赛事
        String matchType = "";
        if (NumberUtils.INTEGER_TWO.intValue() >= config.getMatchType() ||
                ObjectUtils.isEmpty(config.getMatchType())) {
            matchType = NumberUtils.INTEGER_ZERO.toString();
        } else {
            matchType = NumberUtils.INTEGER_ONE.toString();
        }
        dto.setMatchType(matchType);

        dto.setStandardCategoryId(config.getPlayId());
        dto.setPlaceNum(config.getMarketIndex());
        // 盘口类型. 属于赛前盘或者滚球盘. 1: 赛前盘; 0: 滚球盘.
        if (NumberUtils.INTEGER_TWO.intValue() == config.getMatchType()) {
            dto.setMarketType(NumberUtils.INTEGER_ZERO);
        } else {
            dto.setMarketType(config.getMatchType());
        }
        // 自动默认不均分，手工默认均分
        dto.setLinkageMode(config.getLinkageMode());
        if (ObjectUtils.isEmpty(dto.getLinkageMode())){
            if (MarketUtils.isAuto(config.getDataSource().intValue())){
                dto.setLinkageMode(NumberUtils.INTEGER_ZERO);
            }else {
                dto.setLinkageMode(NumberUtils.INTEGER_ONE);
            }
        }
        if (!ObjectUtils.isEmpty(config.getSubPlayId())){
            dto.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
        }
        return dto;
    }
    /**
     * @return void
     * @Description //设置最大最小赔率
     * @Param [config, dto]
     * @Author Sean
     * @Date 11:57 2020/10/21
     **/
    private void addOddsLimit(RcsMatchMarketConfig config, TradeMarketUiConfigDTO dto) {
        BigDecimal maxOdds = config.getMaxOdds();
        BigDecimal minOdds = config.getMinOdds();
        // 发送最大最小赔率转换成欧赔
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
            maxOdds = config.getMaxOdds().compareTo(new BigDecimal(org.apache.commons.lang3.math.NumberUtils.INTEGER_MINUS_ONE)) == 0 ? new BigDecimal(org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE) : config.getMaxOdds();
            minOdds = config.getMinOdds().compareTo(new BigDecimal(org.apache.commons.lang3.math.NumberUtils.INTEGER_MINUS_ONE)) == 0 ? new BigDecimal(org.apache.commons.lang3.math.NumberUtils.INTEGER_ONE) : config.getMinOdds();
            String maxOddsStr = rcsOddsConvertMappingService.getEUOdds(maxOdds.toPlainString());
            String minOddsStr = rcsOddsConvertMappingService.getEUOdds(minOdds.toPlainString());
            maxOdds = new BigDecimal(maxOddsStr);
            minOdds = new BigDecimal(minOddsStr);
        }
        TradeMarketConfigItemDTO oddsLimit = new TradeMarketConfigItemDTO();
        oddsLimit.setMarketCategoryId(config.getPlayId());
        oddsLimit.setMaxOddsValue(maxOdds.doubleValue());
        oddsLimit.setMinOddsValue(minOdds.doubleValue());
        List<TradeMarketConfigItemDTO> marketConfigs = Lists.newArrayList();
        marketConfigs.add(oddsLimit);
        dto.setMarketConfigs(marketConfigs);
    }

    /**
     * @return void
     * @Description //设置margin
     * @Param [config]
     * @Author Sean
     * @Date 11:57 2020/10/21
     **/
    private void addMarginConfig(RcsMatchMarketConfig config, TradeMarketUiConfigDTO dto) {
        List<MarketMarginDtlDTO> marketMarginDtlDTOList = Lists.newArrayList();
        List<Map<String, Object>> odds = config.getOddsList();
        if (odds == null || odds.size() == 0) {
            log.error("::{}::发送margin值到融合，赔率数据为空，{}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), config);
            return;
        }
        //独赢盘会传多个margin 两项盘只需要一项
//        BigDecimal marketMargin = this.getConfigMargin(config);
        //独赢盘会传多个margin 两项盘只需要一项
        for (Map<String, Object> odd : odds) {
            MarketMarginDtlDTO margin = new MarketMarginDtlDTO();
            margin.setOddsType((String) odd.get("oddsType"));
            if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                if (getOddType(config).equalsIgnoreCase(margin.getOddsType())) {
                    margin.setOddsType(margin.getOddsType());
                    margin.setMargin(config.getMargin().doubleValue());
                    marketMarginDtlDTOList.add(margin);
                    break;
                }
            } else {
//                if (SportIdEnum.isFootball(config.getSportId()) &&
//                        (!TradeConstant.FOOTBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()))){
//                    Boolean ds = (DataSourceTypeEnum.AUTOMATIC.getValue().intValue() == config.getDataSource().intValue());
//                    Double marValue = ds ? (Double.parseDouble(odd.get("margin").toString())) : config.getMargin().doubleValue();
//                    margin.setMargin(marValue);
//                    marketMarginDtlDTOList.add(margin);
//                }else if (SportIdEnum.isBasketball(config.getSportId()) &&
//                        (!TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()))){
                    margin.setMargin(config.getMargin().doubleValue());
                    marketMarginDtlDTOList.add(margin);
//                }
            }
        }
        dto.setMarketMarginDtlDTOList(marketMarginDtlDTOList);
    }
    /**
     * @return void
     * @Description 修改水差
     * @Param [config]
     * @Author kimi
     * @Date 2020/3/18
     **/
    public void updateWater(RcsMatchMarketConfig config, TradeMarketUiConfigDTO dto) {
        TradeMarketAutoDiffConfigDTO bean = new TradeMarketAutoDiffConfigDTO();
        bean.setMatchId(config.getMatchId());
        List<TradeMarketAutoDiffConfigItemDTO> diffConfigs = new ArrayList<>();

        Long marketId = config.getMarketId();
        if (DataSourceTypeEnum.AUTOMATIC.getValue().intValue() == config.getDataSource() &&
                config.getAwayAutoChangeRate() != null) {
            String oddType = getOddType(config);
            if (oddType != null) {
                diffConfigs.add(buildMarketAutoDiffConfigBean(config.getPlayId(), marketId, oddType, Double.parseDouble(config.getAwayAutoChangeRate()),config.getSubPlayId()));
                // 处理滚球时让球方和受让方变换的问题
                if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(oddType)) {
                    diffConfigs.add(buildMarketAutoDiffConfigBean(config.getPlayId(), marketId, BaseConstants.ODD_TYPE_2, NumberUtils.DOUBLE_ZERO,config.getSubPlayId()));
                } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(oddType)) {
                    diffConfigs.add(buildMarketAutoDiffConfigBean(config.getPlayId(), marketId, BaseConstants.ODD_TYPE_1, NumberUtils.DOUBLE_ZERO,config.getSubPlayId()));
                }
            }
            dto.setDiffConfigs(diffConfigs);
        }
    }
    /**
     * @return void
     * @Description //设置盘口位置集合
     * @Param [standardSportMarket, dto]
     * @Author Sean
     * @Date 13:30 2020/10/21
     **/
    private void addMarketPlaceList(RcsMatchMarketConfig config, TradeMarketUiConfigDTO dto) {
        List<MarketPlaceDtlDTO> marketPlaceDtlDTOList = Lists.newArrayList();
        MarketPlaceDtlDTO placeDtlDTO = new MarketPlaceDtlDTO();
        placeDtlDTO.setPlaceNum(config.getMarketIndex());
        placeDtlDTO.setPlaceNumStatus(config.getMarketStatus().toString());
        placeDtlDTO.setStandardCategoryId(config.getPlayId());
        marketPlaceDtlDTOList.add(placeDtlDTO);
//        if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) ||
//            TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue()) ||
//                !(SportIdEnum.isFootball(config.getSportId()) || SportIdEnum.isBasketball(config.getSportId()))){
//        }
        if (!ObjectUtils.isEmpty(config.getSubPlayId())){
            placeDtlDTO.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
        }
        dto.setMarketPlaceDtlDTOList(marketPlaceDtlDTOList);
    }
    /**
     * @return java.lang.String
     * @Description //获取受让方
     * @Param [config]
     * @Author Sean
     * @Date 15:19 2020/10/25
     **/
    public String getOddType(RcsMatchMarketConfig config) {
        Double subs = config.getAwayMarketValue().subtract(config.getHomeMarketValue()).doubleValue();
        for (Map<String, Object> map : config.getOddsList()) {
            if (BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(map.get("oddsType").toString()) ||
                    BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(map.get("oddsType").toString()) ) {
                return BaseConstants.ODD_TYPE_UNDER;
            } else if (BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(map.get("oddsType").toString()) ||
                    BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(map.get("oddsType").toString())) {
                return BaseConstants.ODD_TYPE_EVEN;
            }else if (BaseConstants.ODD_TYPE_YES.equalsIgnoreCase(map.get("oddsType").toString()) ||
                    BaseConstants.ODD_TYPE_NO.equalsIgnoreCase(map.get("oddsType").toString())) {
                return BaseConstants.ODD_TYPE_NO;
            }else if (BaseConstants.ODD_TYPE_X.equalsIgnoreCase(map.get("oddsType").toString())) {
                return BaseConstants.ODD_TYPE_X;
            }
        }
        return subs > 0 ? "1" : "2";
    }
    /**
     * @Description   //组装数据
     * @Param [playId, marketId, oddType, diffVal]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.merge.dto.TradeMarketAutoDiffConfigItemDTO
     **/
    public TradeMarketAutoDiffConfigItemDTO buildMarketAutoDiffConfigBean(Long playId, Long marketId, String oddType, Double diffVal,String subPlayId) {
        TradeMarketAutoDiffConfigItemDTO tradeMarketAutoDiffConfigItemDTO = new TradeMarketAutoDiffConfigItemDTO();
        tradeMarketAutoDiffConfigItemDTO.setMarketCategoryId(playId);
        tradeMarketAutoDiffConfigItemDTO.setMarketId(marketId);
        tradeMarketAutoDiffConfigItemDTO.setOddType(oddType);
        tradeMarketAutoDiffConfigItemDTO.setDiffValue(diffVal);
        tradeMarketAutoDiffConfigItemDTO.setChildStandardCategoryId(Long.parseLong(subPlayId));
        return tradeMarketAutoDiffConfigItemDTO;
    }
    /**
     * @Description   //获取margin
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return java.math.BigDecimal
     **/
    public BigDecimal getConfigMargin(RcsMatchMarketConfig config) {
//        if (!ObjectUtils.isEmpty(config.getIsSpecialPumping()) && config.getIsSpecialPumping() == 1){
//            return config.getMargin();
//        }
        BigDecimal marketMargin = config.getMargin();
        // 暂停设置暂停margin
        String redisKey = String.format("rcs:task:match:event:%s", config.getMatchId());
        String eventCode = redisClient.get(redisKey);
        log.info("::{}::,赛事={}，事件={}",getRequestId(), config.getMatchId(), eventCode);
        if (StringUtils.isNotBlank(eventCode) &&
                TradeConstant.TIMEOUT.equalsIgnoreCase(eventCode) &&
                (!ObjectUtils.isEmpty(config.getTimeOutMargin()))) {
            marketMargin = config.getTimeOutMargin();
        }
        return marketMargin;
    }

    public RcsTournamentTemplatePlayMargain queryTournamentTemplateConfig(RcsMatchMarketConfig config){
        config.setMatchType(NumberUtils.INTEGER_TWO.intValue() == config.getMatchType().intValue() ? NumberUtils.INTEGER_ZERO : config.getMatchType());
        RcsTournamentTemplatePlayMargain matchConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (ObjectUtils.isEmpty(matchConfig)){
            throw new RcsServiceException("玩法未开售，不能新增盘口");
        }
        config.setIsSpecialPumping(matchConfig.getIsSpecialPumping());
        config.setSpecialOddsInterval(matchConfig.getSpecialOddsInterval());
        BigDecimal marketMargin = getConfigMargin(config);
        matchConfig.setMargain(marketMargin.toString());
        return matchConfig;
    }
    /**
     * @Description   // 校验赔率规则
     * @Param [marketList]
     * @Author  sean
     * @Date   2021/1/12
     * @return
     **/
    public Boolean marketOddsVerification(List<RcsStandardMarketDTO> marketList){
        log.info("::{}::,计算盘口值：marketList={}",getRequestId(),JSONObject.toJSONString(marketList));
        if (CollectionUtils.isNotEmpty(marketList) &&
            StringUtils.isNotBlank(marketList.get(NumberUtils.INTEGER_ZERO).getAddition1())){
            if (!(RcsConstant.MAIN_BASKETBALL_HANDICAP.contains(marketList.get(NumberUtils.INTEGER_ZERO).getMarketCategoryId()) ||
                    RcsConstant.MAIN_BASKETBALL_TOTAL.contains(marketList.get(NumberUtils.INTEGER_ZERO).getMarketCategoryId()))){
                return Boolean.TRUE;
            }
            marketList = marketList.stream().sorted(Comparator.comparing(e -> e.getNumberOfAddition1())).collect(Collectors.toList());
            Integer oddsValue = NumberUtils.INTEGER_ZERO;
            for (int i=0;i < marketList.size();i++){
                StandardMarketOddsDTO oddsDTO = marketList.get(i).getMarketOddsList().stream().filter(e -> BaseConstants.ODD_TYPE_1.equalsIgnoreCase(e.getOddsType()) || BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(e.getOddsType())).findFirst().get();
                if (i > NumberUtils.INTEGER_ZERO &&
                        oddsDTO.getOddsValue() >= oddsValue){
                    return Boolean.FALSE;
                }
                oddsValue = oddsDTO.getOddsValue();
            }
        }
        return Boolean.TRUE;
    }
    /**
     * @Description   //格式化盘口值
     * @Param [marketValue]
     * @Author  sean
     * @Date   2021/1/12
     * @return java.lang.String
     **/
    public String formatMarketValue(String marketValue) {
        log.info("::{}::格式化盘口值={}",CommonUtil.getRequestId(),marketValue);
        Double marketValueDouble = new BigDecimal(marketValue).doubleValue();
        Integer marketValueInt = new BigDecimal(marketValue).intValue();
        if (marketValueDouble.doubleValue() == marketValueInt.intValue()){
            return marketValueInt.toString();
        }else {
            return marketValueDouble.toString();
        }
    }
    /**
     * @Description   //map列表转化成赔率
     * @Param [marketType, map]
     * @Author  sean
     * @Date   2021/1/12
     * @return java.lang.Integer
     **/
    public Integer getOddsFromMapList(String marketType, Map<String, Object> map) {
        Integer oddsValue = NumberUtils.INTEGER_ZERO;
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(marketType)){
            oddsValue = rcsOddsConvertMappingService.getEUOddsInteger(map.get("fieldOddsValue").toString());
        }else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(marketType)){
            oddsValue = new BigDecimal(map.get("fieldOddsValue").toString()).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
        }
        return oddsValue;
    }
    /**
     * @Description   //跟盘口赔率校验赔率规则
     * @Param [config,playAwayAutoChangeRate]
     * @Author  sean
     * @Date   2021/1/12
     * @return void
     **/
    public Boolean marketOddsVerification(RcsMatchMarketConfig config,List<MatchMarketPlaceConfig> placeConfigs) {
        log.info("::{}::,计算并校验赔率config={};placeConfigs={}",getRequestId(),JSONObject.toJSONString(config),JSONObject.toJSONString(placeConfigs));
//        List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        List<RcsStandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList),RcsStandardMarketDTO.class);
        if (CollectionUtils.isEmpty(marketList) || marketList.size() == NumberUtils.INTEGER_ONE){
            return Boolean.TRUE;
        }
        // 根据水差产生新赔率
        createNewMarketOdds(marketList, placeConfigs, config.getMarketType(),config.getMatchId());
        if (CollectionUtils.isEmpty(config.getOddsList())){
            // 校验赔率
            checkMaxAndMinOdds(config,marketList);
        }
        return marketOddsVerification(marketList);
    }
    /**
     * @Description   //校验最大最小赔率
     * @Param [limitMarketConfig, config]
     * @Author  sean
     * @Date   2021/2/4
     * @return void
     **/
    public String checkMaxAndMinOdds(RcsMatchMarketConfig config,List<RcsStandardMarketDTO> marketList) {
        String msg = null;
        if (CollectionUtils.isEmpty(marketList) && CollectionUtils.isEmpty(config.getOddsList())) {
            return msg;
        }
        String maxOdds;
        String minOdds;
        if (ObjectUtils.isEmpty(config.getMaxOdds()) || ObjectUtils.isEmpty(config.getMinOdds())){
            RcsMatchMarketConfig matchMarketConfig = rcsMatchMarketConfigService.queryMatchMarketConfigNew(config);
            maxOdds = matchMarketConfig.getMaxOdds().toPlainString();
            minOdds = matchMarketConfig.getMinOdds().toPlainString();
        }else {
             maxOdds = config.getMaxOdds().toPlainString();
             minOdds = config.getMinOdds().toPlainString();
        }
        RcsStandardMarketDTO marketDTO = marketList.stream().filter(e -> e.getPlaceNum().intValue() == config.getMarketIndex()).findFirst().get();
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
            maxOdds = rcsOddsConvertMappingService.maxEUOddsByMYOdds(maxOdds);
            minOdds = rcsOddsConvertMappingService.minEUOddsByMYOdds(minOdds);
        }
        //新增优化单2129的玩法不做水差限制
        Boolean flag =  tennisAndPingPongNewPlayNoRadioLimit(config.getSportId().longValue(),config.getPlayId());
        if (CollectionUtils.isNotEmpty(marketDTO.getMarketOddsList())) {
            for (StandardMarketOddsDTO odds : marketDTO.getMarketOddsList()) {
                if ((odds.getOddsValue().intValue() >= new BigDecimal(maxOdds).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue() ||
                        odds.getOddsValue().intValue() <= new BigDecimal(minOdds).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue()) &&
                    ObjectUtils.isEmpty(config.getActive())) {
                    String oddsValue = rcsOddsConvertMappingService.getMyOdds(odds.getOddsValue());
                    if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {
                        oddsValue = new BigDecimal(odds.getOddsValue()).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN).toPlainString();
                    }
                    msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT,
                            maxOdds,
                            minOdds,
                            oddsValue
                            );
                    if(!flag){
                        throw new RcsServiceException(msg);
                    }
                }
            }
        }
        return msg;
    }
    /**
     * @Description   //根据水差产生新赔率
     * @Param [place, playAllMarketList, playWaterDiff, waterDiff, marketType]
     * @Author  sean
     * @Date   2021/1/14
     * @return void
     **/
    public void createNewMarketOdds(List<RcsStandardMarketDTO> playAllMarketList, List<MatchMarketPlaceConfig> placeConfigs,String marketType,Long matchId) {
        log.info("::{}::,计算水差方法：playAllMarketList={}；placeConfigs={}；marketType={}",getRequestId(),JSONObject.toJSONString(playAllMarketList),JSONObject.toJSONString(placeConfigs),marketType);
        if (CollectionUtils.isEmpty(placeConfigs)){
            return;
        }
        if (CollectionUtils.isEmpty(playAllMarketList) ||
            MarketKindEnum.Europe.getValue().equalsIgnoreCase(marketType)){
            return;
        }
        String oddsType = getBasketBallUnderOddsType(playAllMarketList.get(NumberUtils.INTEGER_ZERO).getMarketOddsList().get(NumberUtils.INTEGER_ZERO).getOddsType());
        for (RcsStandardMarketDTO standardMarketDTO : playAllMarketList){
            for (MatchMarketPlaceConfig placeConfig : placeConfigs){
                if (SubPlayUtil.getRongHeSubPlayId(standardMarketDTO).equalsIgnoreCase(placeConfig.getSubPlayId())){
                    // 根据水差计算赔率
                    calculationOddsByWaterDiff(marketType, oddsType, standardMarketDTO, placeConfig,matchId);
                }
            }
        }
    }
    /**
     * @Description   //根据水差计算赔率
     * @Param [marketType, oddsType, standardMarketDTO, placeConfig]
     * @Author  sean
     * @Date   2021/1/15
     * @return void
     **/
    public void calculationOddsByWaterDiff(String marketType, String oddsType, RcsStandardMarketDTO standardMarketDTO, MatchMarketPlaceConfig placeConfig,Long matchId) {
        log.info("::{}::根据水差计算赔率，marketType={}, oddsType={}, standardMarketDTO={}, placeConfig={},matchId={}",CommonUtil.getRequestId(), marketType, oddsType, JSONObject.toJSONString(standardMarketDTO), JSONObject.toJSONString(placeConfig),matchId);
        if (standardMarketDTO.getPlaceNum().intValue() == placeConfig.getPlaceNum()){
            Long playId = standardMarketDTO.getMarketCategoryId();
            boolean isLinkage = Basketball.isLinkage(playId) && isLinkage(matchId, playId);
            BigDecimal placeMarketDiff = placeConfig.getPlaceMarketDiff();
            StandardMarketOddsDTO dto = standardMarketDTO.getMarketOddsList().stream().filter(e -> e.getOddsType().equalsIgnoreCase(oddsType)).findFirst().get();
            BigDecimal water = new BigDecimal(NumberUtils.DOUBLE_ZERO.toString());
            if (!ObjectUtils.isEmpty(dto) &&
                !ObjectUtils.isEmpty(dto.getMarketDiffValue())){
                water = new BigDecimal(dto.getMarketDiffValue().toString());
            }
            BigDecimal diff = placeMarketDiff.subtract(water);
            for (StandardMarketOddsDTO oddsDTO : standardMarketDTO.getMarketOddsList()){
                Integer oddsValue = null;
                if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(marketType)){
                    String myOdds = rcsOddsConvertMappingService.getMyOdds(oddsDTO.getOddsValue());
                    if (oddsDTO.getOddsType().equalsIgnoreCase(oddsType)){
                        myOdds = new BigDecimal(myOdds).add(diff).toPlainString();
                    }else {
                        myOdds = new BigDecimal(myOdds).subtract(diff).toPlainString();
                    }
                    myOdds = MarginUtils.checkMyOdds(new BigDecimal(myOdds)).toPlainString();
                    oddsValue = rcsOddsConvertMappingService.getEUOddsInteger(myOdds);
                }else {
                    if (oddsDTO.getOddsType().equalsIgnoreCase(oddsType)){
                        oddsValue = new BigDecimal(oddsDTO.getOddsValue())
                                .divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN)
                                .add(diff).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
                    }else {
                        oddsValue = new BigDecimal(oddsDTO.getOddsValue())
                                .divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN)
                                .subtract(diff).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
                    }
                }
                if (oddsDTO.getOddsType().equalsIgnoreCase(oddsType)){
                    oddsDTO.setMarketDiffValue(placeMarketDiff.doubleValue());
                }else {
                    oddsDTO.setMarketDiffValue(null);
                }
                oddsDTO.setOddsValue(oddsValue);
                if (!isLinkage) {
                    oddsDTO.setOriginalOddsValue(oddsValue);
                }
                if (!ObjectUtils.isEmpty(placeConfig.getSpread())){
                    oddsDTO.setMargin(Double.parseDouble(placeConfig.getSpread()));
                }
            }
        }
        log.info("::{}::,calculationOddsByWaterDiff，计算赔率后：{}", CommonUtil.getRequestId(),JSONObject.toJSONString(standardMarketDTO));
    }

    private boolean isLinkage(Long matchId, Long playId) {
        String key = String.format(RedisKey.LINKAGE_SWITCH_FLAG, matchId, playId);
        String linkageFlag = redisClient.get(key);
        return StringUtils.isNotBlank(linkageFlag);
    }
    /**
     * @Description   //从缓存获取盘口状态
     * @Param [rcsMatchMarketConfig, matchId]
     * @Author  Sean
     * @Date  15:53 2020/8/18
     * @return java.lang.Integer
     **/
    public Integer getMarketIndexStatus(RcsMatchMarketConfig rcsMatchMarketConfig,Long sportId) {
        Long subPlayId = ObjectUtils.isEmpty(rcsMatchMarketConfig.getSubPlayId()) ? null : Long.parseLong(rcsMatchMarketConfig.getSubPlayId());
        return tradeStatusService.getPlaceStatusFromRedis(sportId, rcsMatchMarketConfig.getMatchId(), rcsMatchMarketConfig.getPlayId(), subPlayId, rcsMatchMarketConfig.getMarketIndex());
    }
    /**
     * @Description   //独赢封盘
     * @Param [matchInfo, matchPlayConfig]
     * @Author  sean
     * @Date   2020/12/6
     * @return void
     **/
    public void singleWinPlayCloseMarket(StandardMatchInfo matchInfo,Long playId,Integer userId) {
        Long mid = BasketBallPlayIdScoreTypeEnum.getSingleWinePlayId(playId).longValue();
        if (mid.longValue()==NumberUtils.LONG_ZERO || NumberUtils.INTEGER_ONE.intValue() == matchInfo.getSportId()){
            log.info("::{}::玩法不需要独赢封盘，playId={}",CommonUtil.getRequestId(matchInfo.getId(),playId),playId.toString());
            return;
        }
        MarketStatusUpdateVO vo = new MarketStatusUpdateVO();
        vo.setTradeLevel(TradeLevelEnum.MARKET.getLevel());
        vo.setMatchId(matchInfo.getId());
        vo.setCategoryId(BasketBallPlayIdScoreTypeEnum.getSingleWinePlayId(playId).longValue());
        vo.setMarketPlaceNum(NumberUtils.INTEGER_ONE);
        vo.setPlaceNumId(vo.getMatchId() + "_" + vo.getCategoryId() + "_" + vo.getMarketPlaceNum());
        vo.setMarketStatus(TradeStatusEnum.SEAL.getStatus());
        String linkId = tradeStatusService.updateTradeStatus(vo);
        log.info("::{}::球头改变独赢封盘不推赔率，linkId=" + linkId,CommonUtil.getRequestId(matchInfo.getId(),playId));
    }
    /**
     * @Description   //是否清除平衡值
     * @Param [config,matchMarketMarginConfig]
     * @Author  sean
     * @Date   2021/2/4
     * @return void
     **/
    public void clearBalance(RcsMatchMarketConfig config, String water,BigDecimal marketHeadGap,Integer  sportId) {
        if (!ObjectUtils.isEmpty(marketHeadGap)){
            return;
        }
        ClearSubDTO rcsMatchMarketConfig = new ClearSubDTO();
        rcsMatchMarketConfig.setMatchId(config.getMatchId());
        rcsMatchMarketConfig.setPlayId(config.getPlayId());
        rcsMatchMarketConfig.setOddsType(config.getOddsType());
        if (NumberUtils.INTEGER_ONE.intValue() == sportId) {
            rcsMatchMarketConfig.setMarketId(config.getMarketId());
        }
        ArrayList<ClearSubDTO> objects = new ArrayList<ClearSubDTO>();
        objects.add(rcsMatchMarketConfig);
        ClearDTO clearDTO = new ClearDTO();
        clearDTO.setSportId(Long.valueOf(sportId));
        clearDTO.setType(0);
        clearDTO.setClearType(6);
        clearDTO.setMatchId(config.getMatchId());
        clearDTO.setList(objects);
        producerSendMessageUtils.sendMessage("RCS_CLEAR_MATCH_MARKET_TAG",null,getRequestId(),clearDTO);
    }
    /**
     * @Description   //获取请求id
     * @Param []
     * @Author  sean
     * @Date   2021/2/4
     * @return java.lang.String
     **/
    public String getRequestId() {
        return getRequestIdStatic();
    }
    /**
     * @Description   //获取请求id
     * @Param []
     * @Author  sean
     * @Date   2021/2/4
     * @return java.lang.String
     **/
    public static String getRequestIdStatic() {
        String key = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            key = request.getHeader("request-id");
        }catch (Exception e){ }
        if (StringUtils.isBlank(key)){
            key = UuidUtils.generateUuid();
        }
        log.info("::{}::获取请求id=::{}::,",key,key);
        return key;
    }
    public boolean isOddsChange(RcsMatchMarketConfig config,Integer sportId,Map<String,Object> orgMap) {
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        if (CollectionUtils.isNotEmpty(playAllMarketList)){
            RcsStandardMarketDTO dto;
            if(!SportIdEnum.isFootball(sportId)){
                dto = playAllMarketList.stream().filter(e -> e.getPlaceNum().intValue() == config.getMarketIndex()).findFirst().get();
            }else {
                dto = playAllMarketList.stream().filter(e -> e.getId().equalsIgnoreCase(config.getMarketId().toString())).findFirst().get();
            }
            List<StandardMarketOddsDTO> oddsList = dto.getMarketOddsList().stream().sorted(Comparator.comparing(StandardMarketOddsDTO :: getOddsValue)).collect(Collectors.toList());
            StandardMarketOddsDTO odds = oddsList.get(NumberUtils.INTEGER_ZERO);
            Map<String,Object> map = config.getOddsList().stream().filter(e -> odds.getOddsType().equalsIgnoreCase(e.get("oddsType").toString())).findFirst().get();
            Integer oddsValue = new BigDecimal(map.get("fieldOddsValue").toString()).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
            if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
                oddsValue = rcsOddsConvertMappingService.getEUOddsInteger(map.get("fieldOddsValue").toString());
            }
            // 校验盘口差
            if (!MarketUtils.isAuto(config.getDataSource().intValue())){
                if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
                    String oo = orgMap.get("homeOddsValue").toString();
                    if (odds.getOddsType().equalsIgnoreCase(getBasketBallUnderOddsType(odds.getOddsType()))){
                        oo = orgMap.get("awayOddsValue").toString();
                    }
                    BigDecimal water = ObjectUtils.isEmpty(config.getAwayAutoChangeRate()) ? BigDecimal.ZERO : new BigDecimal(config.getAwayAutoChangeRate());
                    if (odds.getOddsType().equalsIgnoreCase(getBasketBallUnderOddsType(odds.getOddsType()))){
                        water = new BigDecimal(oo).add(water);
                    }else {
                        water = new BigDecimal(oo).subtract(water);
                    }
                    water = MarginUtils.checkMyOdds(water);
                    if (water.subtract(new BigDecimal(map.get("fieldOddsValue").toString())).doubleValue() !=0){
                        throw new RcsServiceException("水差值不对");
                    }
                }
                // 全场让球没有0.5盘口，这里小于直接处理成0
                BigDecimal marketValue = config.getAwayMarketValue().subtract(config.getHomeMarketValue());
                if (TradeConstant.MAIN_BASKETBALL_TOTAL_HANDICAP.toString().equalsIgnoreCase(config.getPlayId().toString()) &&
                        marketValue.abs().intValue() < NumberUtils.INTEGER_ONE){
                    marketValue = BigDecimal.ZERO;
                }
                if (StringUtils.isNotBlank(dto.getAddition1()) &&
                        marketValue.subtract(new BigDecimal(dto.getAddition1())).doubleValue() !=0){
                    if (config.getMarketIndex() >1){
                        throw new RcsServiceException("只支持主盘修改盘口值");
                    }
                    return Boolean.TRUE;
                }
            }
            if (oddsValue.intValue() == odds.getOddsValue()){
                return Boolean.FALSE;
            }
        }else {
            throw new RcsServiceException("没有盘口数据");
        }
        return Boolean.TRUE;
    }
    /**
     * @Description   //计算主盘值
     * @Param [addition1, totalChange, marketAdjustRange, addtion5, playId]
     * @Author  sean
     * @Date   2021/3/9
     * @return java.math.BigDecimal
     **/
    public static BigDecimal getNewMainMarketValue(BigDecimal addition1, BigDecimal totalChange,BigDecimal marketAdjustRange,String addtion5,Long playId) {
        log.info("::{}::,addition1={},times={},addtion5={},playId={}",getRequestIdStatic(),addition1.toPlainString(),totalChange.toPlainString(),addtion5,playId);
        if (totalChange.doubleValue() == 0){
            return addition1;
        }
        BigDecimal n1 = addition1.add(totalChange);
        if (TradeConstant.MAIN_BASKETBALL_TOTAL_HANDICAP.toString().equalsIgnoreCase(playId.toString())){
            if (marketAdjustRange.doubleValue() % 1 == 0){
                marketAdjustRange = BigDecimal.ONE;
            }else {
                marketAdjustRange = new BigDecimal("0.5");
            }
            BigDecimal times = totalChange.abs().divide(marketAdjustRange);
            marketAdjustRange = totalChange.divide(times);
            n1 = addition1;
            for (int i=1;i<=times.intValue();i++){
                n1 = getNewMainMarket(n1,marketAdjustRange,addtion5);
            }
        }
        log.info("::{}::,getNewMainMarketValue result = {}",getRequestIdStatic(),n1.toPlainString());
        return n1;
    }
    /**
     * @Description   //计算一次的结果
     * @Param [m1, flag, addtion1]
     * @Author  sean
     * @Date   2021/3/9
     * @return java.math.BigDecimal
     **/
    private static BigDecimal getNewMainMarket(BigDecimal addition1, BigDecimal marketAdjustRange,String addtion5) {
        if (addition1.compareTo(BigDecimal.ZERO) == 0 && new BigDecimal(addtion5).doubleValue()%1 !=0){
            if (marketAdjustRange.doubleValue() > 0){
                addition1 = new BigDecimal("0.5");
            }else {
                addition1 = new BigDecimal("-0.5");
            }
        }
        BigDecimal n1 = addition1.add(marketAdjustRange);
        if (n1.abs().compareTo(new BigDecimal("0.5")) == 0 ){
            if (marketAdjustRange.abs().compareTo(new BigDecimal("0.5")) == 0){
                if (marketAdjustRange.doubleValue() >0){
                    n1 = n1.add(new BigDecimal("0.5"));
                }else {
                    n1 = n1.subtract(new BigDecimal("0.5"));
                }
            }else {
                if (n1.doubleValue() >0){
                    n1 = n1.subtract(new BigDecimal("0.5"));
                }else {
                    n1 = n1.add(new BigDecimal("0.5"));
                }
            }
        }
        return n1;
    }
    /**
     * @Description   //生成新盘口列表
     * @Param [mainMarketValue, template, addtion5, playId]
     * @Author  sean
     * @Date   2021/3/10
     * @return java.util.List<com.panda.sport.rcs.pojo.StandardSportMarket>
     **/
    public static List<StandardSportMarket> createMarketValueNoOdds(BigDecimal mainMarketValue, RcsTournamentTemplatePlayMargain template, String addtion5,String playId) {
        log.info("::{}::,mainMarketValue={},template={},addtion5={},playId={}",CommonUtil.getRequestId(template.getTimeVal(), playId), mainMarketValue.toPlainString(),JSONObject.toJSONString(template),addtion5);
        List<StandardSportMarket> ms = Lists.newArrayList();
        Integer index = NumberUtils.INTEGER_ZERO;
        for (int m=1;m<=template.getMarketCount();m++){
            StandardSportMarket market = new StandardSportMarket();
            market.setPlaceNum(m);
            BigDecimal ad = null;
            if (m%2 == 1){
                ad = mainMarketValue.add(template.getMarketNearDiff().multiply(new BigDecimal(m/2)));
                if (TradeConstant.MAIN_BASKETBALL_TOTAL.contains(Long.parseLong(playId)) || Tennis.isManualBuildMarket(Long.parseLong(playId)) || PingPong.isManualBuildMarket(Long.parseLong(playId))){
                    ad = mainMarketValue.subtract(template.getMarketNearDiff().multiply(new BigDecimal(m/2)));
                }
            }else {
                ad = mainMarketValue.subtract(template.getMarketNearDiff().multiply(new BigDecimal(m/2)));
                if (TradeConstant.MAIN_BASKETBALL_TOTAL.contains(Long.parseLong(playId))  || Tennis.isManualBuildMarket(Long.parseLong(playId)) || PingPong.isManualBuildMarket(Long.parseLong(playId))){
                    ad = mainMarketValue.add(template.getMarketNearDiff().multiply(new BigDecimal(m/2)));
                }
            }
            // 0-->1.5
            if (TradeConstant.MAIN_BASKETBALL_TOTAL_HANDICAP.toString().equalsIgnoreCase(playId) && mainMarketValue.doubleValue() == 0 && m>1 && ( new BigDecimal(addtion5).doubleValue()%1 !=0 || template.getMarketAdjustRange().doubleValue() % 1 !=0)){
                ad = ad.abs().add(new BigDecimal("0.5")).multiply(ad.divide(ad.abs()));
            }
            if (TradeConstant.MAIN_BASKETBALL_TOTAL_HANDICAP.toString().equalsIgnoreCase(playId) && m>1 && ad.abs().compareTo(BigDecimal.ONE) < 0){
                if (ad.abs().compareTo(new BigDecimal("0.5")) == 0 && index == 0){
                    ad = BigDecimal.ZERO;
                    index = m;
                }else if ( index == 0 && ad.compareTo(BigDecimal.ZERO) == 0){
                    if (mainMarketValue.doubleValue() > 0){
                        ad = new BigDecimal(NumberUtils.INTEGER_MINUS_ONE);
                    }else {
                        ad = BigDecimal.ONE;
                    }
                    index = m;
                }
            }
            market.setAddition1(ad.stripTrailingZeros().toPlainString());
            ms.add(market);
        }
        for (StandardSportMarket m : ms){
            if (index.intValue() > 0 && m.getPlaceNum().intValue() > index.intValue() && m.getPlaceNum()%2 == index%2){
                if (new BigDecimal(m.getAddition1()).doubleValue() >0){
                    m.setAddition1(new BigDecimal(m.getAddition1()).add(BigDecimal.ONE).stripTrailingZeros().toPlainString());
                }else{
                    m.setAddition1(new BigDecimal(m.getAddition1()).subtract(BigDecimal.ONE).stripTrailingZeros().toPlainString());
                }
            }
        }
        log.info("::{}::createMarketValueNoOdds markets={}",CommonUtil.getRequestId(),JSONObject.toJSONString(ms));
        return ms;
    }
    public boolean isMarketOddsChange(List<RcsStandardMarketDTO> playAllMarketList,RcsMatchMarketConfig config) {
        if (CollectionUtils.isNotEmpty(playAllMarketList) && (!ObjectUtils.isEmpty(config.getMarketId()))){
            for (RcsStandardMarketDTO market : playAllMarketList){
                if (market.getId().equalsIgnoreCase(config.getMarketId().toString()) && CollectionUtils.isNotEmpty(market.getMarketOddsList())){
                    for (StandardMarketOddsDTO oddsDTO :market.getMarketOddsList()){
                        Map<String,Object> map = config.getOddsList().stream().filter(o -> oddsDTO.getOddsType().equalsIgnoreCase(o.get("oddsType").toString())).findFirst().get();
                        Integer oddsValue = new BigDecimal(map.get("fieldOddsValue").toString()).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
                        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
                            oddsValue = rcsOddsConvertMappingService.getEUOddsInteger(map.get("fieldOddsValue").toString());
                        }
                        if (oddsValue.intValue() == oddsDTO.getOddsValue()){
                            return Boolean.FALSE;
                        }
                    }
                }
            }
        }
        return Boolean.TRUE;
    }

    /**
     * @param sportId 赛种id 网球5 乒乓球8
     * @param playId 玩法ID
     * @return 是否是网球和乒乓球下的玩法类型
     */
    public Boolean tennisAndPingPongNewPlayNoRadioLimit(Long sportId, Long playId){
        Boolean flag = Boolean.FALSE;
        try {
            if(SportIdEnum.TENNIS.isYes(sportId) || SportIdEnum.PING_PONG.isYes(sportId)){
                if (Tennis.isExistPlay(playId) || PingPong.isExistPlay(playId)){
                    flag = Boolean.TRUE;
                }
            }
        } catch (Exception e) {
            log.error("::{}::{}",TradeVerificationService.getRequestIdStatic(),e.getMessage(), e);
        }
        return flag;
    }

}
