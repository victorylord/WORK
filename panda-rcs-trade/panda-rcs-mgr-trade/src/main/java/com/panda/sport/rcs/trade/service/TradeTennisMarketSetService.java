package com.panda.sport.rcs.trade.service;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.DataSourceTypeEnum;
import com.panda.sport.rcs.enums.MarketKindEnum;
import com.panda.sport.rcs.enums.TraderLevelEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketOddsMapper;
import com.panda.sport.rcs.mapper.sub.RcsMatchMarketConfigSubMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.impl.MatchTradeConfigServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description   //网球操盘
 * @Param
 * @Author  sean
 * @Date   2021/9/4
 * @return
 **/
@Service
@Slf4j
public class TradeTennisMarketSetService {
    @Autowired
    private IRcsMatchMarketConfigService rcsMatchMarketConfigService;
    @Autowired
    RcsMatchMarketConfigSubMapper rcsMatchMarketConfigSubMapper;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardSportMarketOddsMapper standardSportMarketOddsMapper;
    @Autowired
    private MatchTradeConfigServiceImpl matchTradeConfigServiceImpl;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private TradeMarketSetServiceImpl tradeMarketSetServiceImpl;
    @Autowired
    RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;
    @Autowired
    private TradeBasketBallMarketServiceImpl tradeBasketBallMarketService;
    @Autowired
    private TradeSubPlayCommonService tradeSubPlayCommonService;

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;
    @Autowired
    private TradeCommonService tradeCommonService;

    /**
     * @Description   //查询盘口配置
     * @Param [config]
     * @Author  sean
     * @Date   2021/9/4
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    public RcsMatchMarketConfig queryMatchMarketConfig(RcsMatchMarketConfig config) {
        // 查询数据库配置
        if (StringUtils.isBlank(config.getSubPlayId())){
            config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
        }
        RcsMatchMarketConfig marketConfig = rcsMatchMarketConfigSubMapper.queryMatchMarketConfigSub(config);
        if (ObjectUtils.isEmpty(marketConfig)){
            RcsTournamentTemplatePlayMargain matchConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
            if (ObjectUtils.isEmpty(matchConfig)){
                throw new RcsServiceException("玩法未开售，不能新增盘口");
            }
            // 从联赛配置获取
            marketConfig = rcsMatchMarketConfigService.getRcsMatchMarketConfigByConfig(config,config.getSportId().intValue());
        }
        //添加盘口设置
        marketConfig.setMarketId(config.getMarketId());
        //添加盘口设置
        marketConfig.setSportId(config.getSportId());
        // 默认盘口类型
        marketConfig.setMarketType(config.getMarketType());
        //状态取盘口的
        marketConfig.setMarketStatus(tradeVerificationService.getMarketIndexStatus(marketConfig,config.getSportId().longValue()));

        //状态玩法集状态
        marketConfig.setPlaySetCodeStatus(tradeStatusService.getPlaySetCodeStatus(config.getSportId().longValue(),config.getMatchId(),config.getPlayId()));

        //是否使用数据源1：手动；0：使用数据源。 没有配置即使用数据源
        marketConfig.setDataSource(rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId()).longValue());
        //如果有盘口值 需要设置盘口值  取实时值
        RcsStandardMarketDTO market = standardSportMarketOddsMapper.queryMarketInfoAndOddsByMarketId(marketConfig);
        if ((!ObjectUtils.isEmpty(market)) && (!CollectionUtils.isEmpty(market.getMarketOddsList()))) {
            //设置赔率列表和margin
            List<Map<String, Object>> maps = matchTradeConfigServiceImpl.getOddsList(market.getMarketOddsList(),marketConfig);
            marketConfig.setOddsList(maps);
            //设置盘口值
            try {
                marketConfig.setHomeMarketValue(new BigDecimal(market.getAddition1()));
            }catch (Exception e){
                marketConfig.setHomeMarketValue(BigDecimal.ZERO);
            }
            // 设置第三方状态
            Integer thirdMarketSourceStatus = market.getThirdMarketSourceStatus();
            if ((!ObjectUtils.isEmpty(market.getThirdMarketSourceStatus()) && 0 == market.getThirdMarketSourceStatus())){
                thirdMarketSourceStatus = market.getPaStatus();
            }
            marketConfig.setThirdMarketSourceStatus(thirdMarketSourceStatus);
            // 设置子玩法
            marketConfig.setSubPlayId(market.getChildStandardCategoryId().toString());
            log.info("::{}::,该盘口有赔率数据:{}," , CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()), marketConfig.getMarketId());
        }
        // 获取联赛最大值
        if (ObjectUtils.isEmpty(marketConfig.getMaxBetAmount())){
            BigDecimal amount = rcsMatchMarketConfigService.getBetMax(config);
            marketConfig.setMaxBetAmount(amount);
        }
        // 赛事状态
        Integer lastMatchStatus = rcsTradeConfigService.getLatestStatusByLevel(config.getMatchId(), TraderLevelEnum.MATCH, config.getMatchId());
        marketConfig.setOperateMatchStatus(lastMatchStatus);

        // 获取事件
        String eventCode = redisClient.get(String.format("rcs:task:match:event:%s", config.getMatchId()));
        marketConfig.setEventCode(eventCode);

        // 获取比分
        marketConfig.setSportId(config.getSportId().intValue());
//        String score = rcsMatchMarketConfigService.getScoreByPlayId(marketConfig);
//        marketConfig.setScore(score);
        return marketConfig;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMatchMarketConfig(RcsMatchMarketConfig config) {
        Integer marketIndex = config.getMarketIndex();
        if(config.getOddsList().size() == 3){
            config.setAwayAutoChangeRate("0");
        }
        String subPlayId = config.getSubPlayId();
        if (ObjectUtils.isEmpty(config.getMarketId())){
            config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
            // 切换操盘方式
            tradeSubPlayCommonService.changeTradeType(config,config.getSportId().longValue());
        }

        //是否使用数据源1：手动；0：使用数据源。 没有配置即使用数据源
        Integer dataSource = rcsTradeConfigService.getDataSource(config.getMatchId(), config.getPlayId());
        RcsTournamentTemplatePlayMargain templateMatchConfig = tradeVerificationService.queryTournamentTemplateConfig(config);
        //自动手动放进去
        config.setDataSource(dataSource.longValue());
        // 校验数据
        BigDecimal margin = tradeVerificationService.getConfigMargin(config);
        log.info("::{}::,margin = {}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),margin.toPlainString());

        tradeVerificationService.verifyData(config,templateMatchConfig,margin,config.getSportId().intValue());
        // 是否关联
        tradeMarketSetServiceImpl.setRelevanceTypeConfig(config);
        config.setOddsType(tradeVerificationService.getBasketBallUnderOddsType(config.getOddsList().get(NumberUtils.INTEGER_ZERO).get("oddsType").toString()));
        List<RcsMatchMarketConfig> list = tradeMarketSetServiceImpl.getRcsMatchMarketConfigs(config,templateMatchConfig);
        RcsMatchMarketMarginConfig matchMarketMarginConfig = rcsMatchMarketConfigService.getMarketWaterDiff(config);
        // 获取原始赔率
        RcsMatchMarketConfig cof = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
        cof.setMargin(margin);
        Map<String,Object> map = tradeMarketSetServiceImpl.queryOriginalOdds(cof);
        // 清除平衡值
        tradeMarketSetServiceImpl.clearOddsAndMarketBalance(config, map);
        // 获取水差和
        List<MatchMarketPlaceConfig> placeConfigs = tradeMarketSetServiceImpl.getPlaceConfigs(config,list,matchMarketMarginConfig);
        //超过限制玩法封盘
        tradeMarketSetServiceImpl.waterOverLimitClosePlay(config, templateMatchConfig, placeConfigs);
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
                tradeMarketSetServiceImpl.setMarketValue(config, playAllMarketList, markets);
                // 新增盘口不需要校验
                Boolean result = tradeVerificationService.marketOddsVerification(playAllMarketList);
                if (!ObjectUtils.isEmpty(config.getMarketId()) && NumberUtils.INTEGER_ONE.intValue() != config.getRelevanceType() && !result) {
                    throw new RcsServiceException("赔率不符合规则");
                }
            }
            if (!ObjectUtils.isEmpty(marketHeadGap)
                    && DataSourceTypeEnum.MANUAL.getValue().intValue() != config.getDataSource()) {
                // 自动模式 A+ 模式，直接发送盘口差
                tradeMarketSetServiceImpl.setMarketHeadGap(config, marketHeadGap);
                config.setMarketHeadGap(marketHeadGap);
                tradeOddsCommonService.updateMatchMarketHeadGap(config);
            }
            templateMatchConfig.setSubPlayId(config.getSubPlayId());
            RcsTournamentTemplatePlayMargain templateConfig = JSONObject.parseObject(JSONObject.toJSONString(templateMatchConfig),RcsTournamentTemplatePlayMargain.class);
            if (config.getMarketIndex().intValue() != NumberUtils.INTEGER_ONE){
                templateConfig = null;
            }
            if (ObjectUtils.isEmpty(config.getMarketId())){
                config.setSubPlayId(subPlayId);
            }
            // 封装赛事数据
            MatchOddsConfig matchConfig = tradeMarketSetServiceImpl.buildMatchOddsConfig(config, marketHeadGap,NumberUtils.INTEGER_ONE,templateConfig,placeConfigs,margin);
            //水差超过限制需要封盘
            //tradeMarketSetServiceImpl.setPlayStatus(matchConfig,templateMatchConfig,placeConfigs);
            // 发送消息
            tradeMarketSetServiceImpl.sendMatchOddsMessage(matchConfig);
        }
        config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
        tradeOddsCommonService.updateMarketConfig(config);
        // 修订位置水差
        if (CollectionUtils.isNotEmpty(placeConfigs) && MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
            tradeOddsCommonService.updateMatchMarketWaters(config,placeConfigs);
        }

        rcsTournamentOperateMarketService.sendRcsDataMq(null, config.getPlayId()+"", marketIndex+"", config.getMatchId()+"",SubPlayUtil.getWebSubPlayId(config),config.getMaxSingleBetAmount());
    }
    /**
     * @Description   //两项盘欧赔水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/10/16
     * @return void
     **/

    public String updateEUMarketOddsOrWater(RcsMatchMarketConfig config) {
        return tradeBasketBallMarketService.updateEUMarketOddsOrWater(config);
    }
    /**
     * @Description   //MY水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/10/16
     * @return java.lang.String
     **/
    public String updateMarketAutoRatio(RcsMatchMarketConfig config) {
        return tradeMarketSetServiceImpl.updateMarketAutoRatio(config);
    }

    public String updateMarketHeadGap(RcsMatchMarketConfig config,String lang) {
        return tradeMarketSetServiceImpl.updateMarketHeadGap(config,lang);
    }

    public String reductionWater(RcsMatchMarketConfig config) {
        return tradeMarketSetServiceImpl.reductionWater(config);
    }

    public Map<String, Object> queryOriginalOdds(RcsMatchMarketConfig config) {
        return tradeMarketSetServiceImpl.queryOriginalOdds(config);
    }
}
