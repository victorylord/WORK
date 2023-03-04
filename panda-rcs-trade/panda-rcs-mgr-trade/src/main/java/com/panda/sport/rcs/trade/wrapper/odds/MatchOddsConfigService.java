package com.panda.sport.rcs.trade.wrapper.odds;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.TradeEnum;
import com.panda.sport.rcs.enums.TradeStatusEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchPlayConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.service.BuildMarketService;
import com.panda.sport.rcs.trade.service.MarketBuildService;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;

import com.panda.sport.rcs.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchOddsConfigService {

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private BasketBallMatchOddsConfigSubService basketBallMatchOddsConfigSubService;

    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private MarketStatusService marketStatusService;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private FootBallMatchOddsConfigSubService footBallMatchOddsConfigSubService;
    @Autowired
    private MatchOddsConfigCommonService matchOddsConfigCommonService;
    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private MarketBuildService marketBuildService;
    @Autowired
    private BuildMarketService buildMarketService;

    @Autowired
    private RedisClient redisClient;

    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketOddsApi tradeMarketOddsApi;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;

    public void matchOddsConfig(MatchOddsConfig matchConfig) {

        Long matchId = Long.parseLong(matchConfig.getMatchId());

        StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
        if (matchInfo == null) {
            log.warn("::{}::赛事不存在",matchId);
            return;
        }
        Long sportId = matchInfo.getSportId();

        matchConfig.setSportId(sportId);
        // 盘口类型，1-赛前盘，0-滚球盘
//        Integer matchType = NumberUtils.INTEGER_ONE;
//        if (RcsConstant.isLive(matchInfo.getMatchStatus())) {
//            matchType = NumberUtils.INTEGER_ZERO;
//        }
        Integer matchType = (!ObjectUtils.isEmpty(matchInfo.getOddsLive()) && matchInfo.getOddsLive() == 1 ? NumberUtils.INTEGER_ZERO : NumberUtils.INTEGER_ONE);

        List<MatchPlayConfig> playConfigList = matchConfig.getPlayConfigList();
        if (CollectionUtils.isEmpty(playConfigList)) {
            log.warn("::{}::玩法配置为空",matchId);
            return;
        }
        Integer matchStatus = null;
        for (MatchPlayConfig playConfig : playConfigList) {
            try {
                Long playId = Long.parseLong(playConfig.getPlayId());
                Integer playTradeMode = playConfig.getTradeStatus();
                // 操盘手切换M模式，小于等于三项的盘口投注项激活
                boolean isActive = TradeEnum.isManual(playTradeMode) && YesNoEnum.isYes(matchConfig.getOperateSource());
                if (!TradeEnum.checkTradeType(playTradeMode)) {
                    playTradeMode = rcsTradeConfigService.getDataSource(matchId, playId);
                }
                if (TradeEnum.isAuto(playTradeMode)) {
                    log.warn("::{}::玩法自动操盘，不在计算赔率，只计算手动操盘赔率：categoryId={}", matchId, playConfig.getPlayId());
                    continue;
                }
                if (SportIdEnum.isBasketball(matchInfo.getSportId()) && checkSource(matchConfig.getMessageSource())) {
                    if (TradeEnum.isAutoAdd(playTradeMode)) {
                        BuildMarketConfigDto config = getBuildMarketConfig(matchId, playId, playConfig);
                        //2022/06/20 add by 篮球单双玩法增加A+操盘模式
                        if(TradeConstant.BASKETBALL_SINGLE_DOUBLE_PLAY.contains(playId)){
                            marketBuildService.buildMarketSingleOperList(matchId, playId, matchConfig.getLinkId(), config);
                        }else{
                            marketBuildService.autoPlusBuildMarket(matchId, playId, matchConfig.getLinkId(), config);
                        }
                        continue;
                    }
                    if (TradeEnum.isLinkage(playTradeMode)) {
                        buildMarketService.switchLinkageBuildMarket(sportId, matchId, playId, false, false, false, matchConfig.getLinkId());
                        continue;
                    }
                }
                RcsMatchMarketConfig config = new RcsMatchMarketConfig(matchId, playId);
                config.setMatchType(matchType);

                // 先只适配足球X玩法
                if (SportIdEnum.isFootball(matchInfo.getSportId()) && TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(Integer.parseInt(playConfig.getPlayId()))) {
                    footBallMatchOddsConfigSubService.matchOddsConfig(matchConfig,playConfig,matchInfo,matchType,isActive);
                    continue;
                }
                // 先只适配篮球X玩法
                if (SportIdEnum.isBasketball(matchInfo.getSportId()) && TradeConstant.BASKETBALL_X_PLAYS.contains(Integer.parseInt(playConfig.getPlayId()))) {
                    basketBallMatchOddsConfigSubService.matchOddsConfig(playConfig,matchInfo,matchType,isActive,matchConfig);
                    continue;
                }
                //List<RcsStandardMarketDTO> playAllMarketList = standardSportMarketMapper.selectMarketOddsByMarketIds(config);
                List<RcsStandardMarketDTO> playAllMarketList = tradeOddsCommonService.getMatchPlayOdds(config);
                log.info("::{}::tradeOddsCommonService.getMatchPlayOdds数据，playAllMarketList={}", CommonUtil.getRequestId(),JSONObject.toJSONString(playAllMarketList));
                //M模式读取分时节点配置构建盘口
                playAllMarketList = matchOddsMarket(matchInfo, playAllMarketList, playConfig, matchType,matchConfig);
                log.info("::{}::matchOddsMarket数据，playAllMarketList={}", CommonUtil.getRequestId(),JSONObject.toJSONString(playAllMarketList));
                //更新附加字段
                matchOddsConfigCommonService.updateAdditons(matchConfig, matchInfo, playAllMarketList);
                log.info("::{}::matchOddsConfigCommonService.updateAdditons数据，matchConfig={},matchInfo={},playAllMarketList={}", CommonUtil.getRequestId(),JSONObject.toJSONString(matchConfig),JSONObject.toJSONString(matchInfo),JSONObject.toJSONString(playAllMarketList));
                // 水差计算
                matchOddsMarketOdds(matchInfo, playAllMarketList, playConfig.getPlaceConfig(),playConfig);
                log.info("::{}::水差计算数据，matchInfo={},playAllMarketList={},playConfig.getPlaceConfig={},playConfig={}", CommonUtil.getRequestId(),JSONObject.toJSONString(matchInfo),JSONObject.toJSONString(playAllMarketList),JSONObject.toJSONString(playConfig.getPlaceConfig()),JSONObject.toJSONString(playConfig));
                // 封盘处理
                matchOddsConfigCommonService.closeMarket(matchInfo, playConfig,null);

                if (matchStatus == null) {
                    matchStatus = rcsTradeConfigService.getMatchStatus(matchId);
                }
                List<StandardMarketDTO> marketList = JSONArray.parseArray(JSONArray.toJSONString(playAllMarketList), StandardMarketDTO.class);

                matchOddsConfigCommonService.setActiveLessThreeOddsType(matchType, isActive, playAllMarketList);
                //发送到融合
                log.info("::{}::配置变化，数据下发，：{}",matchId, JSONObject.toJSONString(marketList));
                // 处理状态
                tradeStatusService.handlePushStatus(matchInfo.getSportId(), matchId, playId, marketList, matchStatus, playTradeMode, playConfig.getAutoCloseFlag(), 0, 0);
                // 修订赔率
                matchOddsConfigCommonService.caluSpecialOddsBySpread(config, marketList);
                // 推送赔率
                matchOddsConfigCommonService.putTradeMarketOdds(matchInfo, marketList);
            } catch (Exception e) {
                log.error("::{}::RCS_TRADE_MATCH_ODDS_CONFIG:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            }
        }
    }

    private boolean checkSource(String messageSource) {
        return "timeout".equalsIgnoreCase(messageSource) ||
                "timeout_over".equalsIgnoreCase(messageSource) ||
                "syncJob".equalsIgnoreCase(messageSource);
    }

    private BuildMarketConfigDto getBuildMarketConfig(Long matchId, Long playId, MatchPlayConfig playConfig) {
        RcsTournamentTemplatePlayMargain playMargin = playConfig.getRcsTournamentTemplatePlayMargain();
        List<MatchMarketPlaceConfig> placeConfig = playConfig.getPlaceConfig();
        Map<Integer, BigDecimal> spreadMap;
        if (CollectionUtils.isNotEmpty(placeConfig)) {
            spreadMap = placeConfig.stream().collect(Collectors.toMap(MatchMarketPlaceConfig::getPlaceNum, bean -> CommonUtils.toBigDecimal(bean.getSpread(), new BigDecimal("0.2"))));
        } else {
            spreadMap = playConfig.getPlaceSpreadMap();
        }
        if (CollectionUtils.isEmpty(spreadMap)) {
            spreadMap = Maps.newHashMap();
        }
        if (!spreadMap.containsKey(NumberUtils.INTEGER_ONE)) {
            spreadMap.put(NumberUtils.INTEGER_ONE, new BigDecimal("0.2"));
        }
        BuildMarketConfigDto config = new BuildMarketConfigDto();
        config.setMatchId(matchId);
        config.setPlayId(playId);
        config.setMatchType(playMargin.getMatchType());
        config.setMarketType(playMargin.getMarketType());
//        config.setMarketHeadGap();
        config.setMarketCount(playMargin.getMarketCount());
        config.setMarketNearDiff(playMargin.getMarketNearDiff());
        config.setMarketNearOddsDiff(playMargin.getMarketNearOddsDiff());
        config.setMarketAdjustRange(playMargin.getMarketAdjustRange());
        config.setPlaceSpreadMap(spreadMap);
//        config.setPlaceWaterDiffMap();
        return config;
    }

    /**
     * 计算投注项级别的数据
     *
     * @param @param matchId
     * @param @param oddsList
     * @param @param placeConfig    设定文件
     * @return void    返回类型
     * @throws
     * @Title: matchOddsMarketOdds
     * @Description: TODO
     */
    private void matchOddsMarketOdds(StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> oddsList, List<MatchMarketPlaceConfig> placeConfig,MatchPlayConfig playConfig) {
        if (CollectionUtils.isEmpty(placeConfig)) {
            log.warn("::{}::位置配置参数为空，不重新构建投注项数据:{}",matchInfo.getId(), JSONObject.toJSONString(placeConfig));
            return;
        }

        Map<Integer, MatchMarketPlaceConfig> placeConfigMap = placeConfig.stream().collect(Collectors.toMap(bean -> bean.getPlaceNum(), bean -> bean));
        oddsList.forEach(bean -> {
            if (!placeConfigMap.containsKey(bean.getPlaceNum())) {
                return;
            }

            MatchMarketPlaceConfig placeNumConfig = placeConfigMap.get(bean.getPlaceNum());

            //计算spread
            matchOddsConfigCommonService.matchOddsMarketSpread(matchInfo,bean, placeNumConfig);

            // : 计算位置水差，暂无，可以先空着
            matchOddsConfigCommonService.matchOddsMarketDiffOdds(bean, placeNumConfig,playConfig,matchInfo.getId());

            //TODO : 计算最大最小值，和位置状态以及三方数据源状态
            matchOddsMarketFinalOdds(bean, placeNumConfig);
        });
    }

    private void matchOddsMarketFinalOdds(StandardMarketDTO bean, MatchMarketPlaceConfig placeNumConfig) {
    }

    /**
     * @param matchType 计算盘口级别的配置
     * @param @param    matchId
     * @param @param    oddsList
     * @param @param    marketHeadGap    设定文件
     * @return void    返回类型
     * @throws
     * @Title: matchOddsMarketHeadGap
     * @Description: TODO
     */
    private List<RcsStandardMarketDTO> matchOddsMarket(StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> oddsList, MatchPlayConfig matchPlayConfig, Integer matchType,MatchOddsConfig matchConfig) {
        if (matchPlayConfig == null) return null;
    	// 新增盘口找不到盘口数据
        oddsList = oddsList.stream().filter(e -> e.getMarketOddsList().size() > 0).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(oddsList)){
            matchOddsConfigCommonService.initOddsList(oddsList,matchPlayConfig,matchType,matchPlayConfig.getPlayId());
        }

        //获取玩法模板分时margin
        RcsTournamentTemplatePlayMargain buildConfig = matchPlayConfig.getRcsTournamentTemplatePlayMargain();
        if (buildConfig != null && StringUtils.isBlank(matchPlayConfig.getMarketHeadGap()) && CollectionUtils.isNotEmpty(oddsList)) {
            if (CollectionUtils.isNotEmpty(matchPlayConfig.getPlaceSpreadMap())) {
                oddsList = matchOddsConfigCommonService.buildMarketList(matchInfo, oddsList, buildConfig, matchPlayConfig.getPlaceSpreadMap());
            } else {
                //构建当前玩法符合条件的所有盘口数据
                oddsList = matchOddsConfigCommonService.buildCurrentMarketList(matchInfo, oddsList, buildConfig, matchType);
            }
        }
        // 计算盘口差
        if (!StringUtils.isBlank(matchPlayConfig.getMarketHeadGap())) {
            //当前盘口差  与  设置的盘口对比， 计算差值，得到新的赔率数据
            matchOddsConfigCommonService.setMarketValueAndMarketDiffValue(matchInfo, oddsList, matchPlayConfig,matchConfig,matchPlayConfig.getPlayId());
            log.info("::{}::计算盘口差后的赔率={}",CommonUtil.getRequestId(),JSONObject.toJSONString(oddsList));
        }
        // 设置水差和赔率
        matchOddsConfigCommonService.setMarketOdds(matchInfo.getId(), oddsList, matchPlayConfig, matchType);
        if (CollectionUtils.isNotEmpty(oddsList)){
            Integer marketSource = oddsList.get(0).getMarketSource();
            oddsList.forEach(e -> e.setMarketSource(marketSource));
        }
        log.info("::{}::计算matchOddsMarket后的赔率={}", CommonUtil.getRequestId(),JSONObject.toJSONString(oddsList));
        return oddsList;
    }

}
