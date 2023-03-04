package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketConfigApi;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.TradeMarketAutoDiffConfigDTO;
import com.panda.merge.dto.TradeMarketAutoDiffConfigItemDTO;
import com.panda.merge.dto.TradeMarketHeadGapConfigDTO;
import com.panda.merge.dto.TradeMarketUiConfigDTO;
import com.panda.merge.dto.TradePlaceNumAutoDiffConfigDTO;
import com.panda.merge.dto.TradePlaceNumAutoDiffConfigItemDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.mapper.statistics.RcsMatchPlayConfigMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.mq.utils.ProducerSendMessageUtils;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchPlayConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoDetailService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.impl.MatchTradeConfigServiceImpl;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.utils.CommonUtils;
import com.panda.sport.rcs.utils.DataRealtimeApiUtils;
import com.panda.sport.rcs.utils.MarketUtils;
import com.panda.sport.rcs.utils.OddsConvertUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.enums.PlayStateEnum.e;

/**
 * @author :  Sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.service
 * @Description :  操盘服务类
 * @Date: 2020-08-13 14:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class TradeMarketSetServiceImpl {
    @Autowired
    private TradeSubPlayCommonService tradeSubPlayCommonService;
    @Autowired
    private MatchTradeConfigServiceImpl matchTradeConfigServiceImpl;
    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketOddsApi tradeMarketOddsApi;
    @Reference(check = false, lazy = true, retries = 3, timeout = 5000)
    private ITradeMarketConfigApi tradeMarketConfigApi;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService rcsTournamentTemplatePlayMargainService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;
    @Autowired
    MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    private RcsMatchPlayConfigMapper rcsMatchPlayConfigMapper;
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    private TradeVerificationService tradeVerificationService;

    public static List<Long> DYNAMIC_NO_ABS = Arrays.asList(18L,38L,87L,97L,182L,186L,173L,177L,199L,254L,244L,245L,246L,250L,251L,252L,274L,276L,
            281L,282L,284L,285L,286L,287L,288L,289L,290L,291L,292L);
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private MatchStatisticsInfoDetailService matchStatisticsInfoDetailService;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private TradeStatusService tradeStatusService;

    @Autowired
    private RedisClient redisClient;
    @Autowired
    private ProducerSendMessageUtils producerSendMessageUtils;
    private List<Integer> MATCH_ALIVE_STATUS = Lists.newArrayList(1, 2, 10);
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    /**
     * @return void
     * @Description //手动推送赔率到融合
     * @Param [config]
     * @Author Sean
     * @Date 14:32 2020/8/13
     **/
    public void updateMarketOdds(RcsMatchMarketConfig config) {
        log.info("::{}::点击加减赔率参数{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), JSONObject.toJSONString(config));
        // 获取盘口配置信息
        RcsMatchMarketConfig marketConfig = rcsMatchMarketConfigService.getRcsMatchMarketConfig(config);
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        if (MarketUtils.isAuto(dataSource)) {
            // 自动操盘不能修改赔率
            throw new RcsServiceException("自动操盘不能修改赔率");
        }
        config.setMargin(marketConfig.getMargin());
        config.setDataSource(dataSource.longValue());
        // 设置赔率
        setNewOdds(config);
        // 校验赔率
        tradeVerificationService.checkMaxAndMinOdds(marketConfig, config);
        // 封装赛事数据
        MatchOddsConfig matchConfig = buildMatchOddsConfig(config, null,NumberUtils.INTEGER_ZERO,null,null,null);
        // 发送消息
        sendMatchOddsMessage(matchConfig);
    }


    /**
     * @return void
     * @Description //设置新赔率
     * @Param [config]
     * @Author Sean
     * @Date 16:30 2020/8/13
     **/
    private void setNewOdds(RcsMatchMarketConfig config) {
        //如果有盘口值 需要设置盘口值  取实时值
        List<StandardSportMarketOdds> oddsList = tradeOddsCommonService.getMatchMarketOdds(config);
        if (CollectionUtils.isEmpty(oddsList)) {
            log.error("::{}::该盘口没数据:{}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), config.getMarketId());
            throw new RcsServiceException("该盘口没数据");
        }
        config.setOddsChange(config.getOddsChange().multiply(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE)));
        if (ObjectUtils.isEmpty(config.getMatchType())) {
            StandardMatchInfo info = standardMatchInfoMapper.selectById(config.getMatchId());
            if (ObjectUtils.isEmpty(info)) {
                throw new RcsServiceException("没有该赛事" + config.getMatchId());
            }
            if (MATCH_ALIVE_STATUS.contains(info.getMatchStatus())) {
                config.setMatchType(NumberUtils.INTEGER_ZERO);
            } else {
                config.setMatchType(NumberUtils.INTEGER_ONE);
            }
        }
        // 设置赔率
        setOdds(oddsList, config);
    }

    // 查询盘口配置
    public RcsMatchMarketConfig queryMatchMarketConfig(RcsMatchMarketConfig config) {
        if (TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue())){
            if ((!ObjectUtils.isEmpty(config.getMarketId())) && ObjectUtils.isEmpty(config.getSubPlayId())){
                throw new RcsServiceException("参数玩法需要子玩法id");
            }
            if (ObjectUtils.isEmpty(config.getMarketId()) && !TradeConstant.BASKETBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue())){
                throw new RcsServiceException("不允许新增该参数玩法");
            }
        }
        // 查询数据库配置
        RcsMatchMarketConfig marketConfig = rcsMatchMarketConfigService.queryMatchMarketConfigNew(config);
        log.info("::{}::,数据库盘口原有配置{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(marketConfig));
        if (ObjectUtils.isEmpty(marketConfig)){
            RcsMatchMarketConfig rcsMatchMarketConfig = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
            rcsMatchMarketConfig.setMatchType(rcsMatchMarketConfig.getMatchType().intValue() == NumberUtils.INTEGER_TWO ? NumberUtils.INTEGER_ZERO : rcsMatchMarketConfig.getMatchType());
            RcsTournamentTemplatePlayMargain matchConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(rcsMatchMarketConfig);
            if (ObjectUtils.isEmpty(matchConfig)) {
                throw new RcsServiceException("玩法未开售，不能新增盘口");
            }
            // 从联赛配置获取
            marketConfig = rcsMatchMarketConfigService.getRcsMatchMarketConfigByConfig(config,NumberUtils.INTEGER_TWO);
            log.info("::{}::,数据库盘口为空config={}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(marketConfig));
        }
        String awayAutoChangeRate = rcsMatchMarketConfigService.queryPlaceWaterDiff(config);
        marketConfig.setAwayAutoChangeRate(awayAutoChangeRate);
        //添加盘口设置
        marketConfig.setMarketId(config.getMarketId());
        // 默认盘口类型
        marketConfig.setMarketType(ObjectUtils.isEmpty(marketConfig.getMarketType()) ? config.getMarketType() : marketConfig.getMarketType());
        //状态取盘口的
        marketConfig.setMarketStatus(tradeVerificationService.getMarketIndexStatus(marketConfig,SportIdEnum.BASKETBALL.getId()));
        //自动手动放进去
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        //是否使用数据源1：手动；0：使用数据源。 没有配置即使用数据源
        marketConfig.setDataSource(dataSource.longValue());
        //如果有盘口值 需要设置盘口值  取实时值
        RcsStandardMarketDTO market = standardSportMarketOddsMapper.queryMarketInfoAndOddsByMarketId(marketConfig);
        log.info("::{}::获取盘口值数据：{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), JSONObject.toJSONString(market));
        if ((!ObjectUtils.isEmpty(market)) && (!CollectionUtils.isEmpty(market.getMarketOddsList()))) {
            //设置赔率列表和margin
            List<Map<String, Object>> maps = matchTradeConfigServiceImpl.getOddsList(market.getMarketOddsList(),marketConfig);
            marketConfig.setOddsList(maps);
            //设置盘口值
            tradeVerificationService.setMarketValue(marketConfig, market.getAddition1());
            // 设置第三方状态
            Integer thirdMarketSourceStatus = market.getThirdMarketSourceStatus();
            if ((!ObjectUtils.isEmpty(market.getThirdMarketSourceStatus()) && 0 == market.getThirdMarketSourceStatus())){
                thirdMarketSourceStatus = market.getPaStatus();
            }
            marketConfig.setThirdMarketSourceStatus(thirdMarketSourceStatus);
            // 设置子玩法
            marketConfig.setSubPlayId(ObjectUtils.isEmpty(market.getChildStandardCategoryId()) ? market.getMarketCategoryId().toString():market.getChildStandardCategoryId().toString());
            log.info("::{}::,该盘口有赔率数据:{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),marketConfig.getMarketId());
        }
        // 获取联赛最大值
        if (ObjectUtils.isEmpty(marketConfig.getMaxBetAmount())) {
            BigDecimal amount = rcsMatchMarketConfigService.getBetMax(config);
            marketConfig.setMaxBetAmount(amount);
        }
        Integer lastMatchStatus = rcsTradeConfigService.getLatestStatusByLevel(config.getMatchId(), TraderLevelEnum.MATCH, config.getMatchId());
        marketConfig.setOperateMatchStatus(lastMatchStatus);
        String redisKey = String.format("rcs:task:match:event:%s", config.getMatchId());
        String eventCode = redisClient.get(redisKey);
        marketConfig.setEventCode(eventCode);
        log.info("::{}::,eventCode:{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),eventCode);
        // 获取比分
        String score = rcsMatchMarketConfigService.getScoreByPlayId(marketConfig);
        marketConfig.setScore(score);
        return marketConfig;
    }

    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //修改盘口配置
     * @Param [config]
     * @Author Sean
     * @Date 10:58 2020/10/4
     **/
    @Transactional(rollbackFor = Exception.class)
    public RcsMatchMarketConfig updateMatchMarketConfig(RcsMatchMarketConfig config) {
        //盘口位置
        Integer marketIndex = config.getMarketIndex();
        if(config.getOddsList().size() == 3){
            config.setAwayAutoChangeRate("0");
        }
        if (ObjectUtils.isEmpty(config.getMarketId())){
            config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
            // 切换操盘方式
            tradeSubPlayCommonService.changeTradeType(config,SportIdEnum.BASKETBALL.getId());

            //新增盘口，如果是+-0.5，需要重置到0
            if ("39".equals(String.valueOf(config.getPlayId())) && config.getAwayMarketValue().subtract(config.getHomeMarketValue()).abs().compareTo(new BigDecimal("0.5")) == 0) {
                config.setAwayMarketValue(BigDecimal.ZERO);
                config.setHomeMarketValue(BigDecimal.ZERO);
            }
        }
        RcsTournamentTemplatePlayMargain templateMatchConfig = tradeVerificationService.queryTournamentTemplateConfig(config);
        //自动手动放进去
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        //是否使用数据源1：手动；0：使用数据源。 没有配置即使用数据源
        config.setDataSource(dataSource.longValue());
        // 校验数据
        BigDecimal margin = tradeVerificationService.getConfigMargin(config);
        log.info("::{}::,margin = {}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),margin.toPlainString());

        tradeVerificationService.verifyData(config,templateMatchConfig,margin);
        // 是否关联
        setRelevanceTypeConfig(config);
        config.setOddsType(tradeVerificationService.getBasketBallUnderOddsType(config.getOddsList().get(NumberUtils.INTEGER_ZERO).get("oddsType").toString()));
        List<RcsMatchMarketConfig> list = getRcsMatchMarketConfigs(config,templateMatchConfig);
        RcsMatchMarketMarginConfig matchMarketMarginConfig = rcsMatchMarketConfigService.getBasketballWaterDiff(config);
        // 获取原始赔率
        RcsMatchMarketConfig cof = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
        cof.setMargin(margin);
        Map<String, Object> map = queryOriginalOdds(cof);
        // 清除平衡值
        clearOddsAndMarketBalance(config, map);
        // 获取水差和
        List<MatchMarketPlaceConfig> placeConfigs = getPlaceConfigs(config, list, matchMarketMarginConfig);
        //超过限制玩法封盘
        waterOverLimitClosePlay(config, templateMatchConfig, placeConfigs);
        // 发送配置到数据中心
        tradeVerificationService.sendMarketConfigToDataCenter(config,placeConfigs,margin);
        // 非自动逻辑
        if (config.getDataSource().intValue() != DataSourceTypeEnum.AUTOMATIC.getValue().intValue()) {
            // 更新状态
            tradeCommonService.updatePlaceStatus(config.getMatchId(), config.getPlayId(), config.getMarketIndex(), config.getMarketStatus(), config.getSubPlayId());
            config.setHomeMarketValue(config.getAwayMarketValue().subtract(config.getHomeMarketValue()).stripTrailingZeros());
            BigDecimal marketHeadGap = null;
            // 跟盘口计算盘口差
            if (!ObjectUtils.isEmpty(config.getMarketId())){
                List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
                playAllMarketList = playAllMarketList.stream().filter(e -> e.getChildStandardCategoryId().toString().equalsIgnoreCase(config.getSubPlayId())).collect(Collectors.toList());
                RcsStandardMarketDTO market = playAllMarketList.stream().filter(e -> e.getPlaceNum() == 1).findFirst().get();
                marketHeadGap = TradeCommonService.getMarketHeadGapByValue(config,JSONObject.parseObject(JSONObject.toJSONString(market),StandardSportMarket.class));
                List<StandardSportMarket> markets = Lists.newArrayList();
                if (!ObjectUtils.isEmpty(marketHeadGap)) {
                    markets = TradeVerificationService.createMarketValueNoOdds(new BigDecimal(market.getAddition1()), templateMatchConfig, market.getAddition5(), config.getPlayId().toString());
                }
                // 设置盘口和赔率
                setMarketValue(config, playAllMarketList, markets);
                // 新增盘口不需要校验
                Boolean result = tradeVerificationService.marketOddsVerification(playAllMarketList);
                if (!ObjectUtils.isEmpty(config.getMarketId()) && NumberUtils.INTEGER_ONE.intValue() != config.getRelevanceType() && !result) {
                    throw new RcsServiceException("赔率不符合规则");
                }
            }
            if (!ObjectUtils.isEmpty(marketHeadGap)
                    && DataSourceTypeEnum.MANUAL.getValue().intValue() != config.getDataSource()) {
                // 自动模式 A+ 模式，直接发送盘口差
                setMarketHeadGap(config, marketHeadGap);
                config.setMarketHeadGap(marketHeadGap);
                tradeOddsCommonService.updateMatchMarketHeadGap(config);
            }
            templateMatchConfig.setSubPlayId(config.getSubPlayId());
            RcsTournamentTemplatePlayMargain templateConfig = JSONObject.parseObject(JSONObject.toJSONString(templateMatchConfig),RcsTournamentTemplatePlayMargain.class);
            if (config.getMarketIndex().intValue() != NumberUtils.INTEGER_ONE){
                templateConfig = null;
            }
            if (ObjectUtils.isEmpty(config.getMarketId())){
                config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
            }
            // 封装赛事数据
            MatchOddsConfig matchConfig = buildMatchOddsConfig(config, marketHeadGap,NumberUtils.INTEGER_ONE,templateConfig,placeConfigs,margin);
            //水差超过限制需要封盘
            setPlayStatus(matchConfig,templateMatchConfig,placeConfigs);
            // 发送消息
            sendMatchOddsMessage(matchConfig);
        }
        config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
        tradeOddsCommonService.updateMarketConfig(config);
        // 修订位置水差
        if (CollectionUtils.isNotEmpty(placeConfigs) && (!TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(config.getPlayId().intValue()))){
            tradeOddsCommonService.updateMatchMarketWaters(config,placeConfigs);
        }
        log.info("盘口位置变更：{}",JSON.toJSONString(placeConfigs));
        rcsTournamentOperateMarketService.sendRcsDataMq(null, config.getPlayId()+"", marketIndex+"", config.getMatchId()+"",SubPlayUtil.getWebSubPlayId(config),config.getMaxSingleBetAmount());
        return null;
    }

    /**
     * @return void
     * @Description //超过限制玩法封盘
     * @Param [config, templateMatchConfig, placeConfigs,flag]
     * @Author sean
     * @Date 2021/4/23
     **/
    public void waterOverLimitClosePlay(RcsMatchMarketConfig config, RcsTournamentTemplatePlayMargain templateMatchConfig, List<MatchMarketPlaceConfig> placeConfigs) {
        log.info("RcsMatchMarketConfig={}", config);
        log.info("templateMatchConfig={}", templateMatchConfig);
        log.info("placeConfigs={}", JsonFormatUtils.toJson(placeConfigs));
        if (config.getDataSource().intValue() == DataSourceTypeEnum.AUTOMATIC.getValue().intValue() && MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
            for (MatchMarketPlaceConfig placeConfig : placeConfigs){
                if (placeConfig.getPlaceMarketDiff().abs().compareTo(templateMatchConfig.getOddsMaxValue()) == NumberUtils.INTEGER_ONE){
                    // 水差超过限制封盘
                    MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
                            .setTradeLevel(TradeLevelEnum.PLAY.getLevel())
                            .setMatchId(config.getMatchId())
                            .setCategoryId(config.getPlayId())
                            .setIsPushOdds(YesNoEnum.N.getValue())
                            .setLinkedType(LinkedTypeEnum.TRADE_OVER_LIMIT.getCode())
                            .setRemark(LinkedTypeEnum.TRADE_OVER_LIMIT.getRemark())
                            .setMarketStatus(NumberUtils.INTEGER_ONE);
                    if (StringUtils.isNotBlank(placeConfig.getSubPlayId())){
                        vo.setSubPlayId(Long.parseLong(placeConfig.getSubPlayId()));
                    }
                    log.info("::{}::水差超过限制封盘=>{}，位置水差{}，跳水最大值{}",config.getMatchId(), JSONObject.toJSONString(vo),placeConfig.getPlaceMarketDiff(),templateMatchConfig.getOddsMaxValue());
                    config.setMarketStatus(vo.getMarketStatus());
                    tradeStatusService.updateTradeStatus(vo);
                    return;
                }
            }
        }
    }

    /**
     * @return void
     * @Description //清除平衡值
     * @Param [config, matchMarketMarginConfig, map]
     * @Author sean
     * @Date 2021/4/23
     **/
    public void clearOddsAndMarketBalance(RcsMatchMarketConfig config, Map<String, Object> map) {
        // 是否清除平衡值
        Boolean isOddsChange = !ObjectUtils.isEmpty(config.getMarketId()) && tradeVerificationService.isOddsChange(config, config.getSportId(),map);
        // 关闭跳盘需要清跳盘平衡值
        if (NumberUtils.INTEGER_ZERO.intValue() == config.getIsOpenJumpMarket() || isOddsChange){
            // 清除跳赔平衡值
            balanceService.clearAllBalance(BalanceTypeEnum.JUMP_ODDS.getType(), config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
            if (TradeConstant.OTHER_CAN_TRADE_SPORT.contains(config.getSportId()) || SportIdEnum.BASKETBALL.isYes(config.getSportId())){
                // 清除跳盘平衡值
                balanceService.clearAllBalance(BalanceTypeEnum.JUMP_MARKET.getType(), config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
            }
        }
    }

    /**
     * @return void
     * @Description //设置盘口值
     * @Param [config, playAllMarketList, markets]
     * @Author sean
     * @Date 2021/3/19
     **/
    public void setMarketValue(RcsMatchMarketConfig config, List<RcsStandardMarketDTO> playAllMarketList, List<StandardSportMarket> markets) {
        for (RcsStandardMarketDTO marketDTO : playAllMarketList) {
            if (CollectionUtils.isNotEmpty(markets)) {
                for (StandardSportMarket sportMarket : markets) {
                    if (sportMarket.getPlaceNum().intValue() == marketDTO.getPlaceNum()) {
                        marketDTO.setAddition1(sportMarket.getAddition1());
                        if (config.getMarketIndex().intValue() == marketDTO.getPlaceNum()) {
                            setMarketOddsValue(config, marketDTO);
                        }
                        break;
                    }
                }
            } else {
                if (config.getMarketIndex().intValue() == marketDTO.getPlaceNum()) {
                    setMarketOddsValue(config, marketDTO);
                    marketDTO.setAddition1(config.getHomeMarketValue().stripTrailingZeros().toPlainString());
                    break;
                }
            }
        }
    }

    /**
     * @return void
     * @Description //设置盘口赔率
     * @Param [config, marketDTO]
     * @Author sean
     * @Date 2021/3/19
     **/
    private void setMarketOddsValue(RcsMatchMarketConfig config, RcsStandardMarketDTO marketDTO) {
        String ov = "";
        for (StandardMarketOddsDTO odds : marketDTO.getMarketOddsList()) {
            for (Map<String, Object> m : config.getOddsList()) {
                if (odds.getOddsType().equalsIgnoreCase(m.get("oddsType").toString())) {
                    ov = m.get("fieldOddsValue").toString();
                    if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                        ov = rcsOddsConvertMappingService.getEUOdds(m.get("fieldOddsValue").toString());
                    }
                    odds.setOddsValue(new BigDecimal(ov).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
                }
            }
        }
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig>
     * @Description //获取水差和
     * @Param [config, list]
     * @Author sean
     * @Date 2021/1/24
     **/
    public List<MatchMarketPlaceConfig> getPlaceConfigs(RcsMatchMarketConfig config, List<RcsMatchMarketConfig> list,RcsMatchMarketMarginConfig marketMarginConfig) {
        config.setAwayAutoChangeRate(ObjectUtils.isEmpty(config.getAwayAutoChangeRate()) ? NumberUtils.DOUBLE_ZERO.toString() : config.getAwayAutoChangeRate());
        List<MatchMarketPlaceConfig> placeConfigs = Lists.newArrayList();
        // 关联水差，位置水差不用记录
        if (NumberUtils.INTEGER_ONE.intValue() == config.getRelevanceType()) {
            // 获取整个玩法的水差
            BigDecimal water = new BigDecimal(config.getAwayAutoChangeRate()).subtract(new BigDecimal(marketMarginConfig.getAwayAutoChangeRate()));
            placeConfigs = rcsMatchMarketConfigService.queryPlaceAllWaterDiff(config,water,list,Boolean.FALSE);
        }else {
            MatchMarketPlaceConfig placeConfig = new MatchMarketPlaceConfig();
            placeConfig.setPlaceNum(config.getMarketIndex());
            placeConfig.setPlaceMarketDiff(new BigDecimal(config.getAwayAutoChangeRate()));
            placeConfigs.add(placeConfig);
        }
        for (MatchMarketPlaceConfig placeConfig : placeConfigs){
            placeConfig.setSubPlayId(config.getSubPlayId());
            if (!ObjectUtils.isEmpty(config.getMarketStatus()) && placeConfig.getPlaceNum().intValue() == config.getMarketIndex()){
                placeConfig.setStatus(tradeStatusService.getStatus(config.getSportId().longValue(),config.getMatchId(),config.getPlayId(),config.getMarketStatus()));
            }
        }
        return placeConfigs;
    }

    /**
     * @return void
     * @Description //是否关联水差
     * @Param [config]
     * @Author sean
     * @Date 2021/1/10
     **/
    public void setRelevanceTypeConfig(RcsMatchMarketConfig config) {
        String key = RedisKey.getRelevanceTypeKey(config.getMatchId(),config.getPlayId());
        String relevanceType = redisClient.hGet(key,config.getSubPlayId());
        log.info("::{}::playId={},key={},relevanceType={}",CommonUtil.getRequestId(config.getMatchId()), config.getPlayId(), key,relevanceType);
        if (StringUtils.isBlank(relevanceType) && !TradeConstant.FOOTBALL_X_A2_PLAYS.contains(config.getPlayId().intValue())){
            QueryWrapper<RcsMatchPlayConfig> queryWrapper = new QueryWrapper<>();
            queryWrapper.lambda().eq(RcsMatchPlayConfig ::getMatchId,config.getMatchId());
            queryWrapper.lambda().eq(RcsMatchPlayConfig ::getPlayId,config.getPlayId());
            if (TradeConstant.OTHER_CAN_TRADE_SPORT.contains(config.getSportId())){
                queryWrapper.lambda().eq(RcsMatchPlayConfig ::getSubPlayId,config.getSubPlayId());
            }
           List <RcsMatchPlayConfig> playConfigs = rcsMatchPlayConfigMapper.selectList(queryWrapper);
            if (CollectionUtils.isNotEmpty(playConfigs) &&
                    !ObjectUtils.isEmpty(playConfigs.get(0).getRelevanceType())){
                relevanceType = playConfigs.get(0).getRelevanceType().toString();
            }
        }
        config.setRelevanceType(StringUtils.isNotBlank(relevanceType)?Integer.parseInt(relevanceType):NumberUtils.INTEGER_ONE);
    }
    /**
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     * @Description //修改盘口差
     * @Param [config]
     * @Author Sean
     * @Date 10:58 2020/10/6
     **/
    @Transactional(rollbackFor = Exception.class)
    public String updateMarketHeadGap(RcsMatchMarketConfig config,String lang) {
        tradeSubPlayCommonService.setSubPlayId(config);
        // 是否关联
        setRelevanceTypeConfig(config);
        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainService.getRcsTournamentTemplateConfig(config);
        return updateMarketHeadGap(config, rcsTournamentTemplatePlayMargin, YesNoEnum.N.getValue(),lang);
    }

    public static void main(String[] args) {
//        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = new RcsTournamentTemplatePlayMargain();
//        rcsTournamentTemplatePlayMargin.setMarketCount(7);
//        rcsTournamentTemplatePlayMargin.setMarketNearDiff(BigDecimal.ONE);
//        // 球头改变独赢关盘
//        BigDecimal marketAdjustRange = new BigDecimal("1");
//        rcsTournamentTemplatePlayMargin.setMarketAdjustRange(marketAdjustRange);
//        // 主盘口值不同盘口差计算方式改变 -1==加盘口差，1==减盘口差；
//        StandardSportMarket market = new StandardSportMarket();
//        market.setAddition1("-2.5");
//        market.setAddition5("1.5");
//        market.setMarketHeadGap(new BigDecimal(1));
//        BigDecimal marketValue = new BigDecimal(market.getAddition1());
//        RcsMatchMarketConfig config = new RcsMatchMarketConfig();
//        config.setMarketHeadGap(new BigDecimal("-5"));
//        config.setPlayId(18L);
//        // 原始盘口值
//        BigDecimal marketHeadGap = ObjectUtils.isEmpty(market.getMarketHeadGap()) ? new BigDecimal(NumberUtils.DOUBLE_ZERO) : market.getMarketHeadGap();
//        // 原始盘口值<=0 + 就是加盘口差，- 就是减盘口差;原始盘口值>0,+ 就是减盘口差，- 就是加盘口差
//        if (marketValue.compareTo(new BigDecimal(NumberUtils.DOUBLE_ZERO)) < NumberUtils.INTEGER_ZERO) {
//            config.setMarketHeadGap(config.getMarketHeadGap().multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)));
//        }
//        BigDecimal totalChange = marketAdjustRange.multiply(config.getMarketHeadGap());
//        marketHeadGap = marketHeadGap.add(totalChange);
//
//        BigDecimal newMarketValue = TradeVerificationService.getNewMainMarketValue(marketValue,totalChange,marketAdjustRange,market.getAddition5(),config.getPlayId()).stripTrailingZeros();
//        List<StandardSportMarket> markets = TradeVerificationService.createMarketValueNoOdds(newMarketValue, rcsTournamentTemplatePlayMargin, market.getAddition5(),config.getPlayId().toString());
//        log.info("新盘口差={},新盘口值={},markets={}", marketHeadGap, newMarketValue,JSONObject.toJSONString(markets));
    }
    private String updateMarketHeadGap(RcsMatchMarketConfig config, RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin,Integer isJumpMarket,String lang) {
        tradeSubPlayCommonService.hasSubPlayId(config);
        rcsTournamentTemplatePlayMargin.setSubPlayId(config.getSubPlayId());
        // 球头改变独赢关盘
        BigDecimal marketAdjustRange = rcsTournamentTemplatePlayMargin.getMarketAdjustRange();
        // 查询主盘口
        StandardSportMarket market = tradeOddsCommonService.queryMarketInfo(config);
        if (ObjectUtils.isEmpty(market) || StringUtils.isBlank(market.getAddition1())) {
            log.error("::{}::盘口值为空",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), JSONObject.toJSONString(config));
            throw new RcsServiceException("盘口值为空" + JSONObject.toJSONString(config));
        }
        // 主盘口值不同盘口差计算方式改变 -1==加盘口差，1==减盘口差；
        BigDecimal marketValue = new BigDecimal(market.getAddition1());
        // 原始盘口值
        BigDecimal marketHeadGap = ObjectUtils.isEmpty(market.getMarketHeadGap()) ? new BigDecimal(NumberUtils.DOUBLE_ZERO) : market.getMarketHeadGap();
        // 原始盘口值<=0 + 就是加盘口差，- 就是减盘口差;原始盘口值>0,+ 就是减盘口差，- 就是加盘口差
        if (marketValue.compareTo(new BigDecimal(NumberUtils.DOUBLE_ZERO)) <= NumberUtils.INTEGER_ZERO) {
            config.setMarketHeadGap(config.getMarketHeadGap().multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)));
        }
        BigDecimal totalChange = marketAdjustRange.multiply(config.getMarketHeadGap());
        marketHeadGap = marketHeadGap.add(totalChange);

        BigDecimal newMarketValue = TradeVerificationService.getNewMainMarketValue(marketValue, totalChange, marketAdjustRange, market.getAddition5(), config.getPlayId()).stripTrailingZeros();
        log.info("::{}::,新盘口差={},新盘口值={}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),marketHeadGap, newMarketValue);

        if(SportIdEnum.isIceHockey(config.getSportId().longValue()) && Lists.newArrayList(2L, 295L).contains(config.getPlayId()) && RcsConstant.SPECIAL_MARKET_VALUE.compareTo(newMarketValue) > 0){
            throw new RcsServiceException("大小玩法盘口值不能小于0.5");
        }

        // bug 33008 棒球145,246玩法增加球头不能小于0校验
        if (DYNAMIC_NO_ABS.contains(config.getPlayId()) && newMarketValue.compareTo(BigDecimal.ZERO) <= -1){
            if(lang.equals("en")){
                throw new RcsServiceException(" Size playing method: the opening value cannot be less than 0 ");
            }else{
                throw new RcsServiceException("大小玩法盘口值不能小于0");
            }

        }
        if (YesNoEnum.isYes(isJumpMarket) && "live".equals(config.getBetStage()) && RcsConstant.MAIN_BASKETBALL_TOTAL.contains(config.getPlayId())) {
            // 滚球大小玩法，需要校验比分，盘口值必须大于比分之和
            int scoreSum = getBasketballScoreSum(config.getMatchId(), config.getPlayId());
            log.info("::{}::,当前比分之和：{}" ,CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), scoreSum);
            if (newMarketValue.compareTo(new BigDecimal(scoreSum)) <= 0) {
                throw new RcsServiceException(String.format("大小盘盘口值小于等于当前比分之和，跳盘失败：newMarketValue=%s,scoreSum=%s", newMarketValue, scoreSum));
            }
        }
        // 发送盘口差
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        // 校验赔率
        if (DataSourceTypeEnum.AUTOMATIC.getValue().intValue() != dataSource &&
                NumberUtils.INTEGER_ONE.intValue() != config.getRelevanceType()) {
            List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
            // 计算子玩法
            playAllMarketList = playAllMarketList.stream().filter(e -> e.getChildStandardCategoryId().toString().equalsIgnoreCase(config.getSubPlayId())).collect(Collectors.toList());

            List<StandardSportMarket> markets = TradeVerificationService.createMarketValueNoOdds(newMarketValue, rcsTournamentTemplatePlayMargin, market.getAddition5(),config.getPlayId().toString());
            for (StandardSportMarket sportMarket : markets) {
                for (RcsStandardMarketDTO marketDTO : playAllMarketList) {
                    if (sportMarket.getPlaceNum().intValue() == marketDTO.getPlaceNum()) {
                        marketDTO.setAddition1(sportMarket.getAddition1());
                        break;
                    }
                }
            }
            Boolean result = tradeVerificationService.marketOddsVerification(playAllMarketList);
            if (!result) {
                throw new RcsServiceException(TradeConstant.ODDS_RULE_ERROR);
            }
        }
        config.setDataSource(dataSource.longValue());
        // 手动不记录盘口差
        if (DataSourceTypeEnum.MANUAL.getValue().intValue() != dataSource) {
            // 自动模式 A+ 模式，直接发送盘口差
            setMarketHeadGap(config, marketHeadGap);
            config.setMarketHeadGap(marketHeadGap);
            tradeOddsCommonService.updateMatchMarketHeadGap(config);
        }
        // 其他模式发送赔率
        if (DataSourceTypeEnum.AUTOMATIC.getValue().intValue() != dataSource) {
            // 设置盘口值发送融合
            MatchOddsConfig matchConfig = buildMatchOddsConfig(config, marketHeadGap, NumberUtils.INTEGER_ONE, rcsTournamentTemplatePlayMargin, null,null);
            // 发送消息
            sendMatchOddsMessage(matchConfig);
        }
        if (YesNoEnum.isNo(isJumpMarket)) {
            // 清除跳盘累值
            delJumpMarketValue(config.getMatchId(), config.getPlayId());
            // 清除跳盘平衡值
            balanceService.clearAllBalance(config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
        }
        return null;
    }

    /**
     * @return void
     * @Description //发送盘口差
     * @Param [config, list, marketHeadGap]
     * @Author Sean
     * @Date 16:48 2020/10/9
     **/
    public void setMarketHeadGap(RcsMatchMarketConfig config, BigDecimal marketHeadGap) {
        //  调融合api
        TradeMarketHeadGapConfigDTO dto = new TradeMarketHeadGapConfigDTO();
        dto.setMarketType(config.getMatchType());
        dto.setMarketHeadGap(marketHeadGap.doubleValue());
        dto.setStandardCategoryId(config.getPlayId());
        dto.setStandardMatchInfoId(config.getMatchId());
        dto.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
        Response<String> response = DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                Response rs = tradeMarketConfigApi.putTradeMarketHeadGapConfig(request);
                return rs;
            }
        });

    }

    /**
     * @return void
     * @Description // 修改赔率
     * @Param [config]
     * @Author Sean
     * @Date 10:24 2020/10/7
     **/
    public void updateMarketOddsValue(RcsMatchMarketConfig config) {
        config.setMarketIndex(1);
        RcsMatchMarketConfig marketConfig = rcsMatchMarketConfigService.getRcsMatchMarketConfig(config);
        log.info("::{}::,config={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(marketConfig));
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        if (MarketUtils.isAuto(dataSource)) {
            // 自动操盘不能修改赔率
            throw new RcsServiceException("自动操盘不能修改赔率");
        }
        //如果有盘口值 需要设置盘口值  取实时值
        List<StandardSportMarketOdds> oddsList = tradeOddsCommonService.getMatchMarketOdds(config);
        if (CollectionUtils.isEmpty(oddsList)) {
            log.error("::{}::该盘口没数据:{}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), JSONObject.toJSONString(config));
            throw new RcsServiceException("该盘口没数据");
        }
        // 计算赔率
        config.setMaxOdds(marketConfig.getMaxOdds());
        config.setMinOdds(marketConfig.getMinOdds());
        BigDecimal margin = tradeVerificationService.getConfigMargin(marketConfig);
        config.setMargin(margin);
        // 设置新赔率
        if (!TradeConstant.OTHER_PLAY.equalsIgnoreCase(config.getPlayType())) {
            // 主玩法
            setOdds(oddsList,config);
        }else {
            // 其他玩法
            setOtherPlayOdds(oddsList, config);
        }
        // 封装赛事数据
        config.setDataSource(dataSource.longValue());
        MatchOddsConfig matchConfig = buildMatchOddsConfig(config, null,NumberUtils.INTEGER_ZERO,null,null,margin);
        // 发送消息
        sendMatchOddsMessage(matchConfig);
        //
    }

    /**
     * @return void
     * @Description //设置其他玩法赔率
     * @Param [oddsList, config]
     * @Author Sean
     * @Date 10:57 2020/10/27
     **/
    private void setOtherPlayOdds(List<StandardSportMarketOdds> oddsList, RcsMatchMarketConfig config) {
        for (StandardSportMarketOdds odds:oddsList){
            if (odds.getOddsType().equalsIgnoreCase(config.getOddsType())){
                odds.setActive(config.getActive());
                if (!ObjectUtils.isEmpty(config.getMargin())){
                    odds.setMargin(config.getMargin());
                }
                if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                    String oddsChange = rcsOddsConvertMappingService.getEUOdds(config.getOddsChange().toPlainString());
                    odds.setOddsValue(new BigDecimal(oddsChange).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
                    odds.setOriginalOddsValue(odds.getOddsValue());
                } else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())) {
                    odds.setOddsValue(config.getOddsChange().multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
                    odds.setOriginalOddsValue(odds.getOddsValue());
                }
            }else {
                if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())) {
                    BigDecimal uppderOdds = config.getOddsChange().add(config.getMargin());
                    if (uppderOdds.doubleValue() >= 1){
                        uppderOdds = BigDecimal.valueOf(NumberUtils.INTEGER_TWO).subtract(uppderOdds);
                    }else {
                        uppderOdds = uppderOdds.multiply(BigDecimal.valueOf(NumberUtils.LONG_MINUS_ONE));
                    }
                    uppderOdds = MarginUtils.checkMyOdds(uppderOdds);
                    String oddsChange = rcsOddsConvertMappingService.getEUOdds(uppderOdds.toPlainString());
                    odds.setOddsValue(new BigDecimal(oddsChange).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue());
                    odds.setOriginalOddsValue(odds.getOddsValue());
                }
            }
        }
        List<Map<String, Object>> oddsMap = matchTradeConfigServiceImpl.getOddsList(config, oddsList);
        config.setOddsList(oddsMap);
    }

    /**
     * @return java.util.List<java.util.Map < java.lang.String, java.lang.Object>>
     * @Description //根据联赛配置计算需要变动的赔率
     * @Param [oddsList, config]
     * @Author Sean
     * @Date 11:45 2020/10/7
     **/
    private void setOdds(List<StandardSportMarketOdds> oddsList, RcsMatchMarketConfig config) {
        // 1.查询变化幅度
        if (NumberUtils.INTEGER_TWO.intValue() == config.getMatchType()) {
            config.setMatchType(NumberUtils.INTEGER_ZERO);
        }
        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (ObjectUtils.isEmpty(rcsTournamentTemplatePlayMargin) ||
                ObjectUtils.isEmpty(rcsTournamentTemplatePlayMargin.getOddsAdjustRange())) {
            throw new RcsServiceException("没有找到联赛配置");
        }
        BigDecimal oddsAdjustRange = rcsTournamentTemplatePlayMargin.getOddsAdjustRange();
        // 赔率变化
        config.setOddsChange(oddsAdjustRange.multiply(config.getOddsChange()));
        //设置赔率列表和margin
        List<Map<String, Object>> maps = matchTradeConfigServiceImpl.getOddsList(config, oddsList);
        List<Map<String, Object>> odds = MarginUtils.calculationOddsByMargin(maps, config.getMarketType(), config);
        config.setOddsList(odds);
        log.info("::{}::,修改后的赔率={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(odds));
    }

    /**
     * @return void
     * @Description // 修改水差
     * @Param [config]
     * @Author Sean
     * @Date 14:43 2020/10/7
     **/
    @Transactional(rollbackFor = Exception.class)
    public String updateMarketAutoRatio(RcsMatchMarketConfig config) {
        tradeSubPlayCommonService.setSubPlayId(config);
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        // 获取水差
        RcsMatchMarketMarginConfig marketMarginConfig = rcsMatchMarketConfigService.getMarketWaterDiff(config);
        if (ObjectUtils.isEmpty(marketMarginConfig)) {
            throw new RcsServiceException("赛事不存在");
        }
        /*if (!MarketUtils.isAuto(dataSource) &&
                NumberUtils.INTEGER_TWO.intValue() != marketMarginConfig.getSportId()) {
            throw new RcsServiceException("暂不支持该操作");
        }*/
        //根据marketId查询对应盘口所有赔率
        List<StandardSportMarketOdds> oddsVoList = tradeOddsCommonService.getMatchMarketOdds(config);
        if (CollectionUtils.isEmpty(oddsVoList)) {
            throw new RcsServiceException("没有赔率数据");
        }
        // 获取受让方
        String oddsType = "";
        if (NumberUtils.INTEGER_ONE.intValue() == marketMarginConfig.getSportId()) {
            config.setRelevanceType(NumberUtils.INTEGER_ZERO);
            oddsType = tradeVerificationService.getOddsType(oddsVoList);
        }else{
            // 是否关联
            setRelevanceTypeConfig(config);
            // 获取玩法投注项
            oddsType = tradeVerificationService.getBasketBallUnderOddsType(oddsVoList.get(NumberUtils.INTEGER_ZERO).getOddsType());
        }
        RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin = rcsTournamentTemplatePlayMargainService.getRcsTournamentTemplateConfig(config);
        // 计算水差或者margin
        String msg = setAutoRatioAndMargin(config, marketMarginConfig, oddsType, rcsTournamentTemplatePlayMargin);
        config.setOddsType(oddsType);
        config.setDataSource(dataSource.longValue());
        List<RcsMatchMarketConfig> list = Lists.newArrayList();
        // 发送水差到融合
        if (MarketUtils.isAuto(dataSource) && NumberUtils.INTEGER_ONE.intValue() == marketMarginConfig.getSportId()){
            log.info("::{}::,足球水差推送",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()));
            sendWaterToDataCenter(config);
            // 保存水差
            rcsMatchMarketConfigMapper.insertOrUpdateMarketMarginConfig(config);
        } else {
            //网球和乒乓球和乒乓球手动调赔及自动跳水、即赔率变化超过±0.3时不需要自动封盘
            //网球：153-全场独赢（Margin）、154-全场让盘（spread）、155-全场让局（spread）、202、总局数（spread）
            //乒乓球：153-全场独赢（Margin）、172-全场让分（spread）、173-全场总分（spread）
            Boolean flag = tradeVerificationService.tennisAndPingPongNewPlayNoRadioLimit(marketMarginConfig.getSportId(),(config.getPlayId()));
            // 超过水差先提示
            if (!MarketUtils.isAuto(dataSource) && StringUtils.isNotBlank(msg)){
                if (!flag){
                    if (ObjectUtils.isEmpty(config.getActive()) || NumberUtils.INTEGER_ZERO.intValue() == config.getActive()){
                        return msg;
                    }
                }
            }

            //网球乒乓球读rcs_match_market_config_sub配置
            if(flag && !MarketUtils.isAuto(dataSource)){
                List<RcsMatchMarketConfigSub> configSubList = tradeOddsCommonService.getMatchMarketSubConfigs(config,rcsTournamentTemplatePlayMargin);
                list = JSONArray.parseArray(JSONArray.toJSONString(configSubList),RcsMatchMarketConfig.class);
            }else{
                list = getRcsMatchMarketConfigs(config,rcsTournamentTemplatePlayMargin);
            }
            if (CollectionUtils.isEmpty(list)){
                throw new RcsServiceException("联赛模板还没有生效，稍后重试");
            }

            // 获取水差和
            List<MatchMarketPlaceConfig> placeConfigs = getPlaceConfigs(config,list,marketMarginConfig);
            //封盘
            waterOverLimitClosePlay(config, rcsTournamentTemplatePlayMargin, placeConfigs);
            // 修订位置水差
            if (!MarketUtils.isAuto(dataSource)) {
                Boolean result = Boolean.TRUE;
                try {
                    result = tradeVerificationService.marketOddsVerification(config, placeConfigs);
                } catch (RcsServiceException e) {
                    return e.getErrorMassage();
                }
                if (NumberUtils.INTEGER_ONE.intValue() != config.getRelevanceType() && !result) {
                    // 所有盘口赔率校验
                    throw new RcsServiceException(TradeConstant.ODDS_RULE_ERROR);
                }
                // 封装赛事数据
                MatchOddsConfig matchConfig = buildMatchOddsConfig(config, null, NumberUtils.INTEGER_ONE, null,placeConfigs,null);
                //水差超过限制需要封盘
                if (!flag || SportIdEnum.isIceHockey(marketMarginConfig.getSportId())){
                    setPlayStatus(matchConfig, rcsTournamentTemplatePlayMargin, placeConfigs);
                }
                // 发送消息
                sendMatchOddsMessage(matchConfig);
            }
            // 篮球推送水差
            sendBasketBallWaterToDataCenter(config,placeConfigs);
            tradeOddsCommonService.updateMatchMarketWaters(config,placeConfigs);
            // 清除跳盘平衡值
            balanceService.clearAllBalance(config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
        }
        return msg;
    }

    /**
     * @return void
     * @Description //玩法封盘
     * @Param [matchConfig, rcsTournamentTemplatePlayMargin, placeConfigs]
     * @Author sean
     * @Date 2021/4/24
     **/
    private void setPlayStatus(MatchOddsConfig matchConfig, RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin, List<MatchMarketPlaceConfig> placeConfigs) {
        for (MatchPlayConfig playConfig : matchConfig.getPlayConfigList()) {
            for (MatchMarketPlaceConfig marketPlaceConfig : placeConfigs) {
                if (marketPlaceConfig.getPlaceMarketDiff().abs().compareTo(rcsTournamentTemplatePlayMargin.getOddsMaxValue()) == NumberUtils.INTEGER_ONE) {
                    playConfig.setStatus(LinkedTypeEnum.TRADE_OVER_LIMIT.getCode());
                    return;
                }
            }
        }
    }

    /**
     * @return void
     * @Description //篮球水差推送
     * @Param [config]
     * @Author sean
     * @Date 2021/1/13
     **/
    public void sendBasketBallWaterToDataCenter(RcsMatchMarketConfig config,List<MatchMarketPlaceConfig> placeConfigs) {
        log.info("::{}::,篮球水差推送",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()));
        if (NumberUtils.INTEGER_ONE.intValue() == config.getRelevanceType()){
            TradeMarketUiConfigDTO dto = tradeVerificationService.createRequestDto(config);
            if (CollectionUtils.isNotEmpty(placeConfigs)) {
                tradeVerificationService.updatePlaceWater(config, dto, placeConfigs);
            }
            Response response = DataRealtimeApiUtils.handleApi(dto, new DataRealtimeApiUtils.ApiCall() {
                @Override
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketConfigApi.putTradeMarketUiConfig(request);
                }
            });
        }else if (!SportIdEnum.isFootball(config.getSportId())){
            TradePlaceNumAutoDiffConfigDTO diffConfigDTO = new TradePlaceNumAutoDiffConfigDTO();
            diffConfigDTO.setMatchId(config.getMatchId());
            TradePlaceNumAutoDiffConfigItemDTO configItemDTO = new TradePlaceNumAutoDiffConfigItemDTO();
            configItemDTO.setMarketCategoryId(config.getPlayId());
            configItemDTO.setPlaceNum(config.getMarketIndex());
            configItemDTO.setOddType(config.getOddsType());
            configItemDTO.setDiffValue(Double.parseDouble(config.getAwayAutoChangeRate()));
            if (StringUtils.isNotBlank(config.getSubPlayId())){
                configItemDTO.setChildStandardCategoryId(Long.parseLong(config.getSubPlayId()));
            }
            diffConfigDTO.setDiffConfigs(configItemDTO);
            Response response = DataRealtimeApiUtils.handleApi(diffConfigDTO, new DataRealtimeApiUtils.ApiCall() {
                @Override
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketConfigApi.putTradePlaceNumAutoDiffConfig(request);
                }
            });
        }else if (SportIdEnum.isFootball(config.getSportId())){
            // 足球水差
            sendWaterToDataCenter(config);
        }
    }

    /**
     * @return java.lang.String
     * @Description //计算水差或者margin
     * @Param [config, marketMarginConfig]
     * @Author sean
     * @Date 2020/12/3
     **/
    private String setAutoRatioAndMargin(RcsMatchMarketConfig config, RcsMatchMarketMarginConfig marketMarginConfig, String oddsType, RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin) {
        // 球头改变独赢关盘
        BigDecimal oddsAdjustRange = rcsTournamentTemplatePlayMargin.getOddsAdjustRange();
        oddsAdjustRange = oddsAdjustRange.multiply(config.getOddsChange());
        BigDecimal autoRatio = new BigDecimal(marketMarginConfig.getAwayAutoChangeRate());
        String msg = setAutoRatio(config, autoRatio, oddsAdjustRange, oddsType, rcsTournamentTemplatePlayMargin.getOddsMaxValue());
        return msg;
    }

    /**
     * @return void
     * @Description //发送水差到数据中心
     * @Param [config]
     * @Author Sean
     * @Date 15:25 2020/10/7
     **/
    public void sendWaterToDataCenter(RcsMatchMarketConfig config) {
        TradeMarketAutoDiffConfigDTO bean = new TradeMarketAutoDiffConfigDTO();
        bean.setMatchId(config.getMatchId());
        List<TradeMarketAutoDiffConfigItemDTO> diffConfigs = new ArrayList<>();
        if (config.getAwayAutoChangeRate() != null) {
            String oddType = config.getOddsType();
            if (oddType != null) {
                diffConfigs.add(tradeVerificationService.buildMarketAutoDiffConfigBean(config.getPlayId(), config.getMarketId(), oddType, Double.parseDouble(config.getAwayAutoChangeRate()),config.getSubPlayId()));
                // 处理滚球时让球方和受让方变换的问题
                if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(oddType)) {
                    diffConfigs.add(tradeVerificationService.buildMarketAutoDiffConfigBean(config.getPlayId(), config.getMarketId(), BaseConstants.ODD_TYPE_2, NumberUtils.DOUBLE_ZERO,config.getSubPlayId()));
                } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(oddType)) {
                    diffConfigs.add(tradeVerificationService.buildMarketAutoDiffConfigBean(config.getPlayId(), config.getMarketId(), BaseConstants.ODD_TYPE_1, NumberUtils.DOUBLE_ZERO,config.getSubPlayId()));
                }
            }
        }
        if (diffConfigs.size() > 0) {
            bean.setDiffConfigs(diffConfigs);
            Response response = DataRealtimeApiUtils.handleApi(bean, new DataRealtimeApiUtils.ApiCall() {
                @Override
                public <R> Response<R> callApi(Request request) {
                    return tradeMarketConfigApi.putTradeMarketAutoDiffConfig(request);
                }
            });
        }
    }

    /**
     * @return void
     * @Description 计算自动水差
     * @Param [config]
     * @Author Sean
     * @Date 15:01 2020/10/7
     **/
    public String setAutoRatio(RcsMatchMarketConfig config, BigDecimal autoRatio, BigDecimal oddsAdjustRange, String oddsType, BigDecimal oddsMaxValue) {
        String msg = null;
        if (StringUtils.isNotEmpty(oddsType)) {
            if (StringUtils.isEmpty(config.getOddsType())) {
                if (ObjectUtils.isEmpty(config.getOddsChange())) {
                    config.setOddsChange(new BigDecimal(NumberUtils.DOUBLE_ZERO));
                }
                autoRatio = config.getOddsChange().divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE), NumberUtils.INTEGER_TWO, BigDecimal.ROUND_DOWN);
            } else {
                if (oddsType.equalsIgnoreCase(config.getOddsType())) {
                    autoRatio = autoRatio.add(oddsAdjustRange);
                } else {
                    autoRatio = autoRatio.subtract(oddsAdjustRange);
                }
            }
            if (autoRatio.abs().compareTo(oddsMaxValue) == NumberUtils.INTEGER_ONE){
                msg = String.format(TradeConstant.ODDS_OUT_OF_LIMIT,oddsMaxValue.negate().toPlainString(),oddsMaxValue.toPlainString(),autoRatio.toPlainString());
            }
            config.setAwayAutoChangeRate(autoRatio.toString());
            log.info("::{}::,计算水差完成config={}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(config));
        }
        return msg;
    }

    /**
     * @return com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig
     * @Description //封装赛事数据
     * @Param [config, marketHeadGap]
     * @Author sean
     * @Date 2020/12/3
     **/
    public MatchOddsConfig buildMatchOddsConfig(RcsMatchMarketConfig config,BigDecimal marketHeadGap,Integer oddsReplaceOrder,RcsTournamentTemplatePlayMargain tournamentMatchConfig,List<MatchMarketPlaceConfig> placeConfigs,BigDecimal margin) {
        MatchOddsConfig matchConfig = new MatchOddsConfig();
        // 封装赛事数据
        matchConfig.setMatchType(config.getMatchType());
        matchConfig.setMatchId(config.getMatchId().toString());
        // 封装玩法数据
        List<MatchPlayConfig> playConfigList = buildMatchPlayConfigs(config, marketHeadGap,oddsReplaceOrder,tournamentMatchConfig,placeConfigs,margin);
        matchConfig.setPlayConfigList(playConfigList);
        try {
            Integer userId = TradeUserUtils.getUserId();
            matchConfig.setUserId(userId);
        } catch (Exception e) {
            log.warn("::{}::没有获取到用户id",config.getMatchId());
        }
        return matchConfig;
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.MatchPlayConfig>
     * @Description //封装玩法数据
     * @Param [config, subtract]
     * @Author sean
     * @Date 2020/12/3
     **/
    private List<MatchPlayConfig> buildMatchPlayConfigs(RcsMatchMarketConfig config, BigDecimal subtract,Integer oddsReplaceOrder,RcsTournamentTemplatePlayMargain matchConfig,List<MatchMarketPlaceConfig> placeConfigs,BigDecimal margin) {
        List<MatchPlayConfig> playConfigList = Lists.newArrayList();
        MatchPlayConfig playConfig = new MatchPlayConfig();
        if (!ObjectUtils.isEmpty(subtract)) {
            playConfig.setMarketHeadGap(subtract.toPlainString());
        }
        playConfig.setPlayId(config.getPlayId().toString());
        playConfig.setTradeStatus(config.getDataSource().intValue());
        playConfig.setMarketType(config.getMarketType());

        // 封装盘口数据
        List<RcsStandardMarketDTO> marketList = buildRcsStandardMarketDTOS(config,oddsReplaceOrder,margin);
        playConfig.setMarketList(marketList);
        if (CollectionUtils.isNotEmpty(marketList) && CollectionUtils.isNotEmpty(placeConfigs)) {
            placeConfigs = placeConfigs.stream().filter(e -> e.getPlaceNum().intValue() != config.getMarketIndex()).collect(Collectors.toList());
        }
        // 设置玩法水差
        playConfig.setPlaceConfig(placeConfigs);
        // 新增盘口需要盘口差
        playConfig.setRcsTournamentTemplatePlayMargain(matchConfig);
        playConfigList.add(playConfig);
        return playConfigList;
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO>
     * @Description //封装盘口数据
     * @Param [config]
     * @Author sean
     * @Date 2020/12/3
     **/
    private List<RcsStandardMarketDTO> buildRcsStandardMarketDTOS(RcsMatchMarketConfig config,Integer oddsReplaceOrder,BigDecimal margin) {
        List<RcsStandardMarketDTO> marketList = Lists.newArrayList();
        RcsStandardMarketDTO dto = new RcsStandardMarketDTO();
        List<StandardMarketOddsDTO> oddsDTOList = Lists.newArrayList();
        if (CollectionUtils.isNotEmpty(config.getOddsList())) {
            for (Map<String, Object> map : config.getOddsList()) {
                StandardMarketOddsDTO oddsDTO = new StandardMarketOddsDTO();
                Integer oddsValue = tradeVerificationService.getOddsFromMapList(config.getMarketType(), map);
                oddsDTO.setOddsType(map.get("oddsType").toString());
                if (oddsDTO.getOddsType().equalsIgnoreCase(tradeVerificationService.getBasketBallUnderOddsType(oddsDTO.getOddsType()))) {
                    config.setAwayAutoChangeRate(ObjectUtils.isEmpty(config.getAwayAutoChangeRate()) ? NumberUtils.DOUBLE_ZERO.toString() : config.getAwayAutoChangeRate());
                    oddsDTO.setMarketDiffValue(Double.parseDouble(config.getAwayAutoChangeRate()));
                }
                oddsDTO.setOddsValue(oddsValue);
                if (!ObjectUtils.isEmpty(margin)){
                    oddsDTO.setMargin(margin.doubleValue());
                }
                oddsDTO.setActive(ObjectUtils.isEmpty(map.get("active")) ? 1 : Integer.parseInt(map.get("active").toString()));
                oddsDTO.setOriginalOddsValue(oddsValue);
                oddsDTO.setDataSourceCode("PA");
                oddsDTOList.add(oddsDTO);
            }
            dto.setPlaceNum(config.getMarketIndex());
            dto.setAddition1(config.getHomeMarketValue().toPlainString());
            tradeCommonService.setAdditionForXPlay(config,dto);
            dto.setId(ObjectUtils.isEmpty(config.getMarketId()) ? null : config.getMarketId().toString());
            dto.setMarketOddsList(oddsDTOList);
            dto.setOddsReplaceOrder(oddsReplaceOrder);
            dto.setChildStandardCategoryId(Long.parseLong(SubPlayUtil.getWebSubPlayId(config)));
            marketList.add(dto);
        }
        return marketList;
    }

    /**
     * @return void
     * @Description // 发送消息
     * @Param [matchConfig]
     * @Author sean
     * @Date 2020/12/6
     **/
    public void sendMatchOddsMessage(MatchOddsConfig matchConfig) {
        String key = CommonUtil.getRequestId();
        matchConfig.setLinkId(key);
        log.info("::{}::linkId:{}",CommonUtil.getRequestId(matchConfig.getMatchId(),matchConfig.getSportId()),key);
        producerSendMessageUtils.sendMessage(TradeConstant.RCS_TRADE_MATCH_ODDS_CONFIG, null, key, matchConfig);
    }

    /**
     * @return void
     * @Description //还原水差
     * @Param [config]
     * @Author sean
     * @Date 2021/1/13
     **/
    @Transactional(rollbackFor = Exception.class)
    public String reductionWater(RcsMatchMarketConfig config) {
        tradeSubPlayCommonService.setSubPlayId(config);
        // 是否关联
        setRelevanceTypeConfig(config);
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        config.setDataSource(dataSource.longValue());
        config.setAwayAutoChangeRate(NumberUtils.DOUBLE_ZERO.toString());
        //根据marketId查询对应盘口所有赔率
        List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
        if (CollectionUtils.isEmpty(playAllMarketList)) {
            throw new RcsServiceException("没有赔率数据");
        }
        // 获取玩法投注项
        String oddsType = tradeVerificationService.getBasketBallUnderOddsType(playAllMarketList.get(NumberUtils.INTEGER_ZERO).getMarketOddsList().get(NumberUtils.INTEGER_ZERO).getOddsType());
        config.setOddsType(oddsType);
        List<RcsMatchMarketConfig> list = Lists.newArrayList();
        // 保存水差
        List<MatchMarketPlaceConfig> placeConfigs = Lists.newArrayList();
        if (NumberUtils.INTEGER_ONE.intValue() == config.getRelevanceType()){
            RcsTournamentTemplatePlayMargain matchConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
            list = getRcsMatchMarketConfigs(config,matchConfig);
            if (CollectionUtils.isEmpty(list)){
                throw new RcsServiceException("联赛模板还没有生效，稍后重试");
            }
            placeConfigs = rcsMatchMarketConfigService.queryPlaceAllWaterDiff(config,null,list,Boolean.TRUE);

        }else {
            MatchMarketPlaceConfig placeConfig = new MatchMarketPlaceConfig();
            placeConfig.setPlaceNum(config.getMarketIndex());
            placeConfig.setPlaceMarketDiff(new BigDecimal(config.getAwayAutoChangeRate()));
            placeConfigs.add(placeConfig);
        }
        for (MatchMarketPlaceConfig placeConfig : placeConfigs){
            placeConfig.setSubPlayId(config.getSubPlayId());
        }
        tradeOddsCommonService.updateMatchMarketWaters(config,placeConfigs);
        // 非自动模式
        if (!MarketUtils.isAuto(dataSource)) {
            // 所有盘口赔率校验
            Boolean result = Boolean.TRUE;
            try {
                tradeVerificationService.marketOddsVerification(config, placeConfigs);
            } catch (RcsServiceException e) {
                return e.getErrorMassage();
            }
            if (NumberUtils.INTEGER_ONE.intValue() != config.getRelevanceType() && !result) {
                throw new RcsServiceException(TradeConstant.ODDS_RULE_ERROR);
            }
            // 封装赛事数据
            MatchOddsConfig matchConfig = buildMatchOddsConfig(config, null, NumberUtils.INTEGER_ONE, null,placeConfigs,null);
            // 发送消息
            sendMatchOddsMessage(matchConfig);
        }
        sendBasketBallWaterToDataCenter(config, placeConfigs);
        // 清除跳盘平衡值
        balanceService.clearAllBalance( config.getSportId().longValue(), config.getMatchId(), config.getPlayId(), config.getDateExpect(),config.getSubPlayId());
        return null;
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchMarketConfig>
     * @Description //得到全部配置
     * @Param [config]
     * @Author sean
     * @Date 2021/1/24
     **/
    public List<RcsMatchMarketConfig> getRcsMatchMarketConfigs(RcsMatchMarketConfig config,RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargin) {
        List<RcsMatchMarketConfig> listConfig = Lists.newArrayList();
        List<RcsMatchMarketConfig> configs = Lists.newArrayList();
        if (TradeConstant.BASKETBALL_X_PLAYS.contains(config.getPlayId().intValue()) ||
            TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(config.getPlayId().intValue()) ||
           !(SportIdEnum.isBasketball(config.getSportId()) || SportIdEnum.isFootball(config.getSportId()))) {
            List<RcsMatchMarketConfigSub> configSubList = tradeOddsCommonService.getMatchMarketSubConfigs(config,rcsTournamentTemplatePlayMargin);
            RcsMatchMarketConfig matchMarketConfig = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
            log.info("configSubList:{},matchMarketConfig:{}", JSONObject.toJSONString(configSubList), JSONObject.toJSONString(matchMarketConfig));

            if (CollectionUtils.isEmpty(configSubList) || configSubList.size() < rcsTournamentTemplatePlayMargin.getMarketCount()){
                List<Integer> indexs =  Lists.newArrayList();
                if (CollectionUtils.isNotEmpty(configSubList)){
                    indexs = configSubList.stream().map(e -> e.getMarketIndex()).collect(Collectors.toList());
                }
                for (int i=1;i<=rcsTournamentTemplatePlayMargin.getMarketCount();i++){
                    if (!indexs.contains(i)){
                        matchMarketConfig.setMarketIndex(i);
                        rcsMatchMarketConfigMapper.initMarketConfig(matchMarketConfig);
                    }
                }
                redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG,config.getMatchId(),config.getPlayId(),config.getSubPlayId()));
                configSubList = tradeOddsCommonService.getMatchMarketSubConfigs(config,rcsTournamentTemplatePlayMargin);
                log.info("configSubListTwo:{}", JSONObject.toJSONString(configSubList));
            }
            listConfig = JSONArray.parseArray(JSONArray.toJSONString(configSubList),RcsMatchMarketConfig.class);
            log.info("subListConfig:{}", JSONObject.toJSONString(listConfig));

        }else {
            listConfig = tradeOddsCommonService.getMatchMarketConfigs(config,rcsTournamentTemplatePlayMargin);
        }
        for (RcsMatchMarketConfig c : listConfig){
            if (c.getMarketIndex().intValue() <= rcsTournamentTemplatePlayMargin.getMarketCount()){
                configs.add(c);
            }
        }
        log.info("::{}::,水差配置={}", config.getMarketId(),JSONObject.toJSONString(configs));
        return configs;
    }

    public Map<String, Object> queryOriginalOdds(RcsMatchMarketConfig config) {
        tradeSubPlayCommonService.hasSubPlayId(config);
        Long playId = config.getPlayId();
        if (tradeStatusService.isLinkage(config.getMatchId(), playId)) {
            String subPlayId = config.getSubPlayId();
            if (StringUtils.isBlank(subPlayId)) {
                subPlayId = String.valueOf(playId);
            }
            List<StandardSportMarketOdds> oddsList = standardSportMarketOddsMapper.queryStandardSportMarketOdds(config.getMatchId(), playId, subPlayId, config.getMarketIndex());
            if (CollectionUtils.isNotEmpty(oddsList)) {
                String homeOddsValue = null;
                String awayOddsValue = null;
                for (StandardSportMarketOdds odds : oddsList) {
                    if (OddsTypeEnum.isHomeOddsType(odds.getOddsType())) {
                        homeOddsValue = rcsOddsConvertMappingService.getMyOdds(odds.getOriginalOddsValue());
                    } else if (OddsTypeEnum.isAwayOddsType(odds.getOddsType())) {
                        awayOddsValue = rcsOddsConvertMappingService.getMyOdds(odds.getOriginalOddsValue());
                    }
                }
                BigDecimal oldSpread = OddsConvertUtils.calSpreadByMalayOdds(homeOddsValue, awayOddsValue);
                BigDecimal spread = config.getMargin();
                if (spread != null && spread.compareTo(oldSpread) != 0) {
                    BigDecimal change = spread.subtract(oldSpread).divide(new BigDecimal(NumberUtils.INTEGER_TWO), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal homeOdds = CommonUtils.toBigDecimal(homeOddsValue, BigDecimal.ONE).subtract(change);
                    homeOddsValue = OddsConvertUtils.checkMalayOdds(homeOdds).toPlainString();
                    BigDecimal awayOdds = CommonUtils.toBigDecimal(awayOddsValue, BigDecimal.ONE).subtract(change);
                    awayOddsValue = OddsConvertUtils.checkMalayOdds(awayOdds).toPlainString();
                }
                Map<String, Object> map = Maps.newHashMap();
                map.put("homeOddsValue", homeOddsValue);
                map.put("awayOddsValue", awayOddsValue);
                return map;
            }
        }
        config.setMatchType(NumberUtils.INTEGER_TWO.intValue() == config.getMatchType().intValue() ? NumberUtils.INTEGER_ZERO : config.getMatchType());
        RcsTournamentTemplatePlayMargain tournamentConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (ObjectUtils.isEmpty(tournamentConfig)) {
            throw new RcsServiceException("联赛没有配置，无法查询原始赔率");
        }
        String redisKey = String.format("rcs:task:match:event:%s", config.getMatchId());
        String eventCode = redisClient.get(redisKey);
        log.info("::{}::事件编码,key={},eventCode={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),redisKey,eventCode);
        String margin = config.getMargin().toPlainString();
        if ("timeout".equalsIgnoreCase(eventCode) && config.getTimeOutMargin() != null) {
            margin = config.getTimeOutMargin().toPlainString();
        }

        BigDecimal marketNearOddsDiff = Optional.ofNullable(tournamentConfig.getMarketNearOddsDiff()).orElse(new BigDecimal("0.15"));
        BigDecimal spread = CommonUtils.toBigDecimal(margin, new BigDecimal("0.2"));
        BigDecimal malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), 6, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_DOWN);

        BigDecimal factor = ((config.getMarketIndex() - 1) % 2 == 1) ? new BigDecimal((config.getMarketIndex() - 1) / 2 + 1) : new BigDecimal((config.getMarketIndex() - 1) / 2).negate();
        // 上盘赔率
        BigDecimal upOdds = malayOdds.add(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
        upOdds = OddsConvertUtils.checkMalayOdds(upOdds);
        // 下盘赔率
        BigDecimal downOdds = malayOdds.subtract(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
        downOdds = OddsConvertUtils.checkMalayOdds(downOdds);

        Map<String, Object> map = Maps.newHashMap();
        map.put("homeOddsValue", upOdds);
        map.put("awayOddsValue", downOdds);
        return map;
    }

    private void delJumpMarketValue(Long matchId, Long playId) {
        // 清除跳盘累值
        String preKey = RedisKey.getJumpMarketValueKey(matchId, playId, "pre");
        String liveKey = RedisKey.getJumpMarketValueKey(matchId, playId, "live");
        redisClient.delete(preKey);
        redisClient.delete(liveKey);
    }

    private int getBasketballScoreSum(Long matchId, Long playId) {
        ScoreTypeEnum scoreTypeEnum = ScoreTypeEnum.getScoreTypeEnum(2L, playId);
        if (scoreTypeEnum == null) {
            return 0;
        }
        MatchStatisticsInfoDetail scoreInfo = matchStatisticsInfoDetailService.getByScoreType(matchId, scoreTypeEnum);
        if (scoreInfo == null) {
            return 0;
        }
        return scoreInfo.getT1() + scoreInfo.getT2();
    }

}
