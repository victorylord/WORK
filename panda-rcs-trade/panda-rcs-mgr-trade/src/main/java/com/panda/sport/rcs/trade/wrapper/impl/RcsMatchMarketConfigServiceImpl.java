package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.RcsNewSportWaterConfigMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.mapper.statistics.RcsMatchPlayConfigMapper;
import com.panda.sport.rcs.mapper.sub.RcsMatchMarketConfigSubMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigSub;
import com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.BuildMarketConfigDto;
import com.panda.sport.rcs.pojo.config.BuildMarketPlayConfig;
import com.panda.sport.rcs.pojo.config.MarketBuildConfig;
import com.panda.sport.rcs.pojo.config.MarketBuildPlayConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.utils.SubPlayUtil;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargainRef;
import com.panda.sport.rcs.trade.enums.BasketBallPlayIdScoreTypeEnum;
import com.panda.sport.rcs.trade.mq.impl.ClearMatchMarketConsumer;
import com.panda.sport.rcs.trade.mq.impl.ConsumetUtil;
import com.panda.sport.rcs.trade.service.TradeOddsCommonService;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.IRcsMatchMarketConfigService;
import com.panda.sport.rcs.trade.wrapper.RcsTournamentOperateMarketService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportMarketService;
import com.panda.sport.rcs.utils.CommonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 赛事设置表 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-24
 */
@Service
@Slf4j
public class RcsMatchMarketConfigServiceImpl extends ServiceImpl<RcsMatchMarketConfigMapper, RcsMatchMarketConfig> implements IRcsMatchMarketConfigService {
    @Autowired
    RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;
    @Autowired
    RcsMatchMarketConfigSubMapper rcsMatchMarketConfigSubMapper;
    @Autowired
    RcsTournamentOperateMarketService rcsTournamentOperateMarketService;
    @Autowired
    StandardSportMarketService standardSportMarketService;
    @Autowired
    IRcsMatchMarketConfigService matchMarketConfigService;
    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;
    @Autowired
    private MatchStatisticsInfoDetailMapper matchStatisticsInfoDetailMapper;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    private RcsMatchPlayConfigMapper matchPlayConfigMapper;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    RcsNewSportWaterConfigMapper rcsNewSportWaterConfigMapper;
    @Autowired
    private TradeOddsCommonService tradeOddsCommonService;
    /**
     * 赛事滚球标识
     */
    private final static String MATCH_ODDS_LIVE = "rcs:matchInfo:oddsLive:status:%s";
    @Override
    public void insert(RcsMatchMarketConfig rcsMatchMarketConfig) {
        rcsMatchMarketConfigMapper.insert(rcsMatchMarketConfig);
    }

    @Override
    public RcsMatchMarketConfig getMaxAndMinOddsValue(Long matchId, Long playId) {
        if(!Football.isOddsLimitPlay(playId)){
            return null;
        }
        if(Football.isXOddsTypePlay(playId)){
            return rcsMatchMarketConfigMapper.getSubMaxAndMinOddsValue(matchId, playId);
        }
        return rcsMatchMarketConfigMapper.getMaxAndMinOddsValue(matchId, playId);
    }

    @Override
    public BigDecimal getMarketHeadGap(Long matchId, Long playId) {
        BuildMarketPlayConfig config = rcsMatchMarketConfigMapper.getMarketHeadGap(matchId, playId);
        log.info("::{}::A+模式构建盘口，获取盘口差：playId={},config={}", matchId, playId, JSON.toJSONString(config));
        if (config != null) {
            return config.getMarketHeadGap();
        }
        return BigDecimal.ZERO;
    }
    @Override
    public RcsMatchMarketConfig selectRcsMatchMarketConfig(RcsMatchMarketConfig rcsMatchMarketConfig) {
        QueryWrapper<RcsMatchMarketConfig> wrapper = new QueryWrapper<>();
        wrapper.lambda().eq(RcsMatchMarketConfig::getMatchId, rcsMatchMarketConfig.getMatchId());
        wrapper.lambda().eq(RcsMatchMarketConfig::getPlayId, rcsMatchMarketConfig.getPlayId());
        wrapper.lambda().eq(RcsMatchMarketConfig::getMarketId, rcsMatchMarketConfig.getMarketId());
        wrapper.lambda().eq(RcsMatchMarketConfig::getMarketIndex, rcsMatchMarketConfig.getMarketIndex());
        RcsMatchMarketConfig matchMarketConfig = rcsMatchMarketConfigMapper.selectOne(wrapper);

        return matchMarketConfig;
    }

    @Override
    public RcsMatchMarketConfig queryMatchMarketConfigNew(RcsMatchMarketConfig rcsMatchMarketConfig) {
        RcsMatchMarketConfig matchMarketConfig = null;
        if (TradeConstant.FOOTBALL_X_INSERT_PLAYS.contains(rcsMatchMarketConfig.getPlayId().intValue()) ||
                TradeConstant.BASKETBALL_X_PLAYS.contains(rcsMatchMarketConfig.getPlayId().intValue()) ||
                !(SportIdEnum.isFootball(rcsMatchMarketConfig.getSportId()) || SportIdEnum.isBasketball(rcsMatchMarketConfig.getSportId()))){
            RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(rcsMatchMarketConfig),RcsMatchMarketConfig.class);
            if (ObjectUtils.isEmpty(rcsMatchMarketConfig.getMarketId()) && StringUtils.isNotBlank(config.getSubPlayId())){
                config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
            }
            matchMarketConfig = tradeOddsCommonService.getMatchMarketSubConfig(config);
//            matchMarketConfig = rcsMatchMarketConfigSubMapper.queryMatchMarketConfigSub(config);
        }else {
            matchMarketConfig = tradeOddsCommonService.getMatchMarketConfig(rcsMatchMarketConfig);
        }
        return matchMarketConfig;
    }

    /**
     * 暂时只是查全场让球和上半场让球玩法的当前比分
     */
	@Override
	public Integer queryCurrentTypeScore(Long matchId, Long playId) {
		Integer score = 0;
        try {
        	List<MatchStatisticsInfoDetail> matchStatisticsInfoDetails = standardSportMarketMapper.selectMatchStatisticsInfoDetail(matchId);
            if (!CollectionUtils.isEmpty(matchStatisticsInfoDetails)) {
            	score = matchStatisticsInfoDetails.get(0).getT1() - matchStatisticsInfoDetails.get(0).getT2();
            }
        }catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }

		return score;
	}

    @Override
    public Map<Long, Map<Integer, RcsMatchMarketConfig>> queryConfigs(Long matchId) {
        Map<Long, Map<Integer, RcsMatchMarketConfig>> result = new HashMap<>();
	    QueryWrapper<RcsMatchMarketConfig> wrapper = new QueryWrapper();
        wrapper.lambda().eq(RcsMatchMarketConfig::getMatchId,matchId);
        List<RcsMatchMarketConfig> configs = baseMapper.selectList(wrapper);
        if(!CollectionUtils.isEmpty(configs)){
            Map<Long, List<RcsMatchMarketConfig>> categoryMap = configs.stream().collect(Collectors.groupingBy(RcsMatchMarketConfig::getPlayId));
            for (Map.Entry<Long, List<RcsMatchMarketConfig>> map:categoryMap.entrySet()){
                Map<Integer, RcsMatchMarketConfig> indexMap = map.getValue().stream().collect(Collectors.toMap(e -> e.getMarketIndex(), e -> e));
                result.put(map.getKey(),indexMap);
            }
        }
        return result;
    }
    /**
     * @Description   //获取盘口配置,为空则返回默认值
     * @Param [config]
     * @Author  Sean
     * @Date  15:04 2020/10/9
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    @Override
    public RcsMatchMarketConfig getRcsMatchMarketConfig(RcsMatchMarketConfig config) {
        RcsMatchMarketConfig marketConfig = this.queryMatchMarketConfigNew(config);
        if (ObjectUtils.isEmpty(marketConfig)){
            marketConfig = JSONObject.parseObject(JSONObject.toJSONString(config),RcsMatchMarketConfig.class);
        }
        BigDecimal max = null;
        BigDecimal min = null;
        BigDecimal margin = null;
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
            max = getDefaultValue(marketConfig.getMaxOdds(), TradeConstant.MAX_MY_ODDS);
            min = getDefaultValue(marketConfig.getMinOdds(), TradeConstant.MIN_MY_ODDS);
            margin = getDefaultValue(marketConfig.getMargin(), TradeConstant.DEFAULT_MY_SPREAD);
        }else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())){
            max = getDefaultValue(marketConfig.getMaxOdds(), TradeConstant.MAX_EU_ODDS);
            min = getDefaultValue(marketConfig.getMinOdds(), TradeConstant.MIN_EU_ODDS);
            margin = getDefaultValue(marketConfig.getMargin(), TradeConstant.DEFAULT_EU_MARGIN);
        }
        marketConfig.setMargin(margin);
        marketConfig.setMaxOdds(max);
        marketConfig.setMinOdds(min);
        return marketConfig;
    }

    /**
     * @Description   //从联赛配置获取
     * @Param [config]
     * @Author  sean
     * @Date   2020/12/22
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketConfig
     **/
    @Override
    public RcsMatchMarketConfig getRcsMatchMarketConfigByConfig(RcsMatchMarketConfig config,Integer sportId) {
        RcsMatchMarketConfig marketConfig = new RcsMatchMarketConfig();
        RcsTournamentTemplatePlayMargainRef ref = null;
        if (SportIdEnum.isFootball(sportId)){
            ref = rcsTournamentTemplatePlayMargainMapper.queryFootballMatchConfig(config);
        }else if (SportIdEnum.isBasketball(sportId)){
            ref = rcsTournamentTemplatePlayMargainMapper.queryMatchConfig(config,sportId);
        }else if (SportIdEnum.isSnooker(sportId.longValue())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesBySnooker(config);
        }else if (SportIdEnum.BASEBALL.isYes(sportId.longValue())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByBaseBall(config);
        }else if (SportIdEnum.BADMINTON.isYes(sportId.longValue())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByBadminton(config);
        }else if (SportIdEnum.ICE_HOCKEY.isYes(sportId.longValue())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByIce(config);
        }else {
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByTennis(config);
        }
        if (!ObjectUtils.isEmpty(ref)){
            marketConfig = JSONObject.parseObject(JSONObject.toJSONString(ref),RcsMatchMarketConfig.class);
            marketConfig.setOddChangeRule(ObjectUtils.isEmpty(marketConfig.getOddChangeRule()) ? NumberUtils.INTEGER_ZERO : marketConfig.getOddChangeRule());
            marketConfig.setBalanceOption(ObjectUtils.isEmpty(marketConfig.getBalanceOption()) ? NumberUtils.INTEGER_ZERO : marketConfig.getBalanceOption());
            marketConfig.setMargin(new BigDecimal(ref.getMargain()));
            if (!ObjectUtils.isEmpty(ref.getPauseMargain())){
                marketConfig.setTimeOutMargin(new BigDecimal(ref.getPauseMargain()));
            }
            marketConfig.setMaxSingleBetAmount(ref.getOrderSinglePayVal());
            marketConfig.setTimeOutWaitSeconds(ref.getPauseWaitTime());
            marketConfig.setWaitSeconds(ref.getNormalWaitTime());
            if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())){
                marketConfig.setHomeLevelFirstOddsRate(ref.getMultiOddsRate());
                marketConfig.setHomeLevelFirstMaxAmount(ref.getMultiDiffVal());
            }
            marketConfig.setMaxBetAmount(ObjectUtils.isEmpty(ref.getOrderSinglePayVal()) ? new BigDecimal(TradeConstant.DEFAULT_BET_MAX) : new BigDecimal(ref.getOrderSinglePayVal()));
            // 足球根据比例设置限额
            if (!ObjectUtils.isEmpty(ref.getOrderSinglePayVal()) &&
                    StringUtils.isNotBlank(ref.getViceMarketRatio()) &&
                     config.getMarketIndex() > NumberUtils.INTEGER_ONE.intValue()){
                    JSONArray array = JSONArray.parseArray(ref.getViceMarketRatio());
                    if (!ObjectUtils.isEmpty(array) && (array.size() >= (config.getMarketIndex() - NumberUtils.INTEGER_ONE))) {
                        Object betAmount = array.get(config.getMarketIndex() - NumberUtils.INTEGER_TWO);
                        marketConfig.setMaxBetAmount(BigDecimal.valueOf(getAmountByPlaceRatio(marketConfig.getMaxBetAmount().toPlainString(), betAmount)));
                        if (SportIdEnum.isFootball(sportId)){
                            marketConfig.setHomeLevelFirstMaxAmount(getAmountByPlaceRatio(marketConfig.getHomeLevelFirstMaxAmount().toString(), betAmount));
                            marketConfig.setMaxSingleBetAmount(marketConfig.getMaxBetAmount().longValue());
                            if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
                                marketConfig.setHomeLevelSecondMaxAmount(getAmountByPlaceRatio(marketConfig.getHomeLevelSecondMaxAmount().toString(), betAmount));
                                marketConfig.setHomeSingleMaxAmount(getAmountByPlaceRatio(marketConfig.getHomeSingleMaxAmount().toString(), betAmount));
                                marketConfig.setHomeMultiMaxAmount(getAmountByPlaceRatio(marketConfig.getHomeMultiMaxAmount().toString(), betAmount));
                            }
                        }
                    }
            }
        }
        log.info("::{}::,找到联赛配置={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(marketConfig));
        marketConfig.setPlayId(config.getPlayId());
        marketConfig.setMatchId(config.getMatchId());
        marketConfig.setMarketId(config.getMarketId());
        marketConfig.setMarketType(config.getMarketType());
        marketConfig.setMarketIndex(config.getMarketIndex());
        marketConfig.setMatchType(config.getMatchType());
        return marketConfig;
    }
    /**
     * @Description   //根据比例设置限额值
     * @Param [amount, betAmount]
     * @Author  sean
     * @Date   2021/3/4
     * @return long
     **/
    private long getAmountByPlaceRatio(String amount, Object betAmount) {
        return new BigDecimal(amount).multiply(new BigDecimal(betAmount.toString())).divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE), NumberUtils.INTEGER_ZERO, BigDecimal.ROUND_DOWN).longValue();
    }

    /**
     * @Description   //设置最大可投和margin
     * @Param [marketConfig, matchType]
     * @Author  Sean
     * @Date  17:38 2020/10/11
     * @return void
     **/
    @Override
    public BigDecimal getBetMax(RcsMatchMarketConfig marketConfig) {
        BigDecimal maxBetAmount = null;
        RcsMatchMarketConfig config = JSONObject.parseObject(JSONObject.toJSONString(marketConfig),RcsMatchMarketConfig.class);
        if (NumberUtils.INTEGER_TWO.intValue() == config.getMatchType()){
            config.setMatchType(NumberUtils.INTEGER_ZERO);
        }
        //查询联赛配置
        RcsTournamentTemplatePlayMargainRef ref = null;
        if (SportIdEnum.isSnooker(marketConfig.getSportId().longValue())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesBySnooker(config);
        }else if (SportIdEnum.BASEBALL.isYes(marketConfig.getSportId())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByBaseBall(config);
        }else if (SportIdEnum.BADMINTON.isYes(marketConfig.getSportId().longValue())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByBadminton(config);
        }else if (!SportIdEnum.isFootball(marketConfig.getSportId()) && !SportIdEnum.isBasketball(marketConfig.getSportId())){
            ref = rcsTournamentTemplatePlayMargainMapper.selectAllTemplatesByTennis(config);
        }else if (SportIdEnum.isBasketball(marketConfig.getSportId())){
            ref = rcsTournamentTemplatePlayMargainMapper.queryMatchConfig(config, marketConfig.getSportId());
        }
        if ((!ObjectUtils.isEmpty(ref)) &&
                (!ObjectUtils.isEmpty(ref.getOrderSinglePayVal()))){

            maxBetAmount = new BigDecimal(ref.getOrderSinglePayVal());

            if (config.getMarketIndex() > NumberUtils.INTEGER_ONE.intValue()){
                JSONArray array =  JSONArray.parseArray(ref.getViceMarketRatio());
                if (!ObjectUtils.isEmpty(array) && array.size() >= (config.getMarketIndex() -1)){
                    String betAmount = array.get(config.getMarketIndex() - NumberUtils.INTEGER_TWO).toString();
                    maxBetAmount = maxBetAmount.multiply(new BigDecimal(betAmount)).divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE),NumberUtils.INTEGER_ZERO,BigDecimal.ROUND_DOWN);
                }
            }
        }
        if (ObjectUtils.isEmpty(maxBetAmount)){
            log.info("::{}::联赛配置没有默认100w，config={}",marketConfig.getMarketId(), JSONObject.toJSONString(marketConfig));
            maxBetAmount = new BigDecimal(TradeConstant.DEFAULT_BET_MAX);
        }
        return maxBetAmount;
    }
    /**
     * @Description   //跟玩法获取比分
     * @Param [marketConfig]
     * @Author  sean
     * @Date   2020/11/21
     * @return java.lang.String
     **/
    @Override
    public String getScoreByPlayId(RcsMatchMarketConfig marketConfig) {
        String score = "0:0";
        if (SportIdEnum.isTennis(marketConfig.getSportId())){
            Map<String,Long> map = SubPlayUtil.getTennisPlaysBySubPlayId(marketConfig.getPlayId(),marketConfig.getSubPlayId());
            map.put("matchId",marketConfig.getMatchId());
            score = matchStatisticsInfoDetailMapper.selectTennisScoreByPlayId(map);
        }else {
            score = matchStatisticsInfoDetailMapper.selectScoreByMatchStage(marketConfig.getMatchId(), BasketBallPlayIdScoreTypeEnum.getStageValue(marketConfig.getPlayId()));
        }
        if (StringUtils.isEmpty(score)){
            score = "0:0";
        }
        return score;
    }
    /**
     * @Description   // 获取水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    @Override
    public RcsMatchMarketMarginConfig getMarketWaterDiff(RcsMatchMarketConfig config){
        RcsMatchMarketMarginConfig marketMarginConfig = null;
        if (SportIdEnum.isFootball(config.getSportId())){
            marketMarginConfig = getFootballWaterDiff(config);
        }else if (SportIdEnum.isBasketball(config.getSportId())) {
            marketMarginConfig = getBasketballWaterDiff(config);
        }else if (SportIdEnum.isTennis(config.getSportId()) || SportIdEnum.isPingpong(config.getSportId().longValue()) || SportIdEnum.isIceHockey(config.getSportId().longValue())){
            marketMarginConfig = getBasketballWaterDiff(config);
        }else{
            marketMarginConfig = getTennisWaterDiff(config);
        }
        marketMarginConfig.setSportId(config.getSportId().longValue());
        log.info("::{}::,水差={}", CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(marketMarginConfig));
        return marketMarginConfig;
    }

    private RcsMatchMarketMarginConfig getTennisWaterDiff(RcsMatchMarketConfig config) {
        RcsMatchMarketMarginConfig matchMarketMarginConfig = new RcsMatchMarketMarginConfig();
        RcsMatchMarketConfig marketConfig = tradeOddsCommonService.getMatchMarketSubConfig(config);
        if (!ObjectUtils.isEmpty(marketConfig) &&
                (!ObjectUtils.isEmpty(marketConfig.getAwayAutoChangeRate()))){
            matchMarketMarginConfig.setAwayAutoChangeRate(marketConfig.getAwayAutoChangeRate());
        }else {
            matchMarketMarginConfig.setAwayAutoChangeRate("0");
        }
        return matchMarketMarginConfig;
    }

    /**
     * @Description   //获取篮球水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    @Override
    public RcsMatchMarketMarginConfig getBasketballWaterDiff(RcsMatchMarketConfig config) {
        RcsMatchMarketMarginConfig matchMarketMarginConfig = new RcsMatchMarketMarginConfig();
        String awayAutoChangeRate = queryPlaceWaterDiff(config);
        matchMarketMarginConfig.setAwayAutoChangeRate(awayAutoChangeRate);
//        String playAwayAutoChangeRate = rcsMatchPlayConfigService.getPlayWaterDiff(config);
//        matchMarketMarginConfig.setPlayAwayAutoChangeRate(playAwayAutoChangeRate);
        return matchMarketMarginConfig;
    }
    /**
     * @Description   //获取位置水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return java.lang.String
     **/
    @Override
    public String queryPlaceWaterDiff(RcsMatchMarketConfig config) {
        RcsMatchMarketConfig matchMarketConfig = this.queryMatchMarketConfigNew(config);
        String awayAutoChangeRate = NumberUtils.DOUBLE_ZERO.toString();
        if (!ObjectUtils.isEmpty(matchMarketConfig) &&
                (!ObjectUtils.isEmpty(matchMarketConfig.getAwayAutoChangeRate()))){
            awayAutoChangeRate = matchMarketConfig.getAwayAutoChangeRate();
        }
        return awayAutoChangeRate;
    }

    /**
     * @Description   //足球获取水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/10
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    public RcsMatchMarketMarginConfig getFootballWaterDiff(RcsMatchMarketConfig config) {
//            // 获取水差和margin
//            QueryWrapper<RcsMatchMarketMarginConfig> queryWrapper = new QueryWrapper<>();
//            queryWrapper.lambda().eq(RcsMatchMarketMarginConfig :: getMarketId,config.getMarketId());
//            RcsMatchMarketMarginConfig marketMarginConfig = rcsMatchMarketMarginConfigMapper.selectOne(queryWrapper);
            RcsMatchMarketMarginConfig marketMarginConfig = tradeOddsCommonService.getFootballWaterDiff(config);
        if (ObjectUtils.isEmpty(marketMarginConfig) || StringUtils.isBlank(marketMarginConfig.getAwayAutoChangeRate())){
            marketMarginConfig = new RcsMatchMarketMarginConfig();
            marketMarginConfig.setAwayAutoChangeRate("0");
        }
        return marketMarginConfig;
    }

    /**
     * @Description   //获取margin和水差
     * @Param [config]
     * @Author  sean
     * @Date   2020/12/3
     * @return com.panda.sport.rcs.pojo.RcsMatchMarketMarginConfig
     **/
    @Override
    public RcsMatchMarketMarginConfig getRcsMatchMarketMarginConfig(RcsMatchMarketConfig config) {
        RcsMatchMarketMarginConfig  marketMarginConfig = getFootballWaterDiff(config);
        if (ObjectUtils.isEmpty(marketMarginConfig)){
            marketMarginConfig = new RcsMatchMarketMarginConfig();
            marketMarginConfig.setAwayAutoChangeRate(NumberUtils.INTEGER_ZERO.toString());
            marketMarginConfig.setHomeMargin(new BigDecimal(TradeConstant.DEFAULT_EU_MARGIN));
            marketMarginConfig.setAwayMargin(new BigDecimal(TradeConstant.DEFAULT_EU_MARGIN));
            marketMarginConfig.setMargin(new BigDecimal(TradeConstant.DEFAULT_EU_MARGIN));
        }
        BigDecimal marketMargin = tradeVerificationService.getConfigMargin(config);
        marketMarginConfig.setMargin(marketMargin);
        return marketMarginConfig;
    }
    /**
     * @Description   //获取玩法和位置总水差
     * @Param [config]
     * @Author  sean
     * @Date   2021/1/15
     * @return java.util.List<com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig>
     **/
    @Override
    public List<MatchMarketPlaceConfig> queryPlaceAllWaterDiff(RcsMatchMarketConfig config,BigDecimal water,List<RcsMatchMarketConfig> list,Boolean isClear){
        List<MatchMarketPlaceConfig> configs = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(list)){
            for (RcsMatchMarketConfig matchMarketConfig : list){
                matchMarketConfig.setAwayAutoChangeRate(StringUtils.isEmpty(matchMarketConfig.getAwayAutoChangeRate()) ? "0":matchMarketConfig.getAwayAutoChangeRate());
                MatchMarketPlaceConfig placeConfig = new MatchMarketPlaceConfig();
                placeConfig.setPlaceNum(matchMarketConfig.getMarketIndex());
                if (isClear){
                    placeConfig.setPlaceMarketDiff(BigDecimal.ZERO);
                }else{
                    placeConfig.setPlaceMarketDiff(water.add(new BigDecimal(matchMarketConfig.getAwayAutoChangeRate())));
                }
                configs.add(placeConfig);
            }
        }
        log.info("::{}::,queryPlaceAllWaterDiff ={}",CommonUtil.getRequestId(config.getMatchId(), config.getPlayId()),JSONObject.toJSONString(configs));
        return configs;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void clearConfig(StandardSportMarket market, Long sportId) {
        log.info("::{}::clearConfig",market.getStandardMatchInfoId());
        // 清空配置水差
        rcsMatchMarketConfigMapper.updateMatchMarketMarginConfigByMatch(market);
        redisClient.hashRemove(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER,market.getStandardMatchInfoId()),market.getId().toString());
        if((!ConsumetUtil.getxPlays().contains(market.getMarketCategoryId().intValue()))&& ClearMatchMarketConsumer.FB.contains(sportId.intValue())){
            rcsMatchMarketConfigMapper.updateMatchMarketConfigByMatch(market);
            redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_CONFIG,market.getStandardMatchInfoId(),market.getMarketCategoryId()));
        }
        if(ConsumetUtil.getxPlays().contains(market.getMarketCategoryId().intValue())||ClearMatchMarketConsumer.TETC.contains(sportId.intValue())) {
            rcsMatchMarketConfigSubMapper.updateMatchMarketConfigSubByMatch(market);
            redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_SUB_CONFIG,market.getStandardMatchInfoId(),market.getMarketCategoryId(),market.getChildMarketCategoryId()));
        }
        // 清空盘口差
//        standardSportMarketMapper.clearMarketHeadGapByMatch(market);
        // 清空赔率水差
//        if (!CollectionUtils.isEmpty(ids)){
//            standardSportMarketMapper.clearMarketDiffByMarketIds(ids);
//        }
        // 清空配置的盘口差
        matchPlayConfigMapper.clearMarketHeadGapByMatch(market);
        redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_HEAD_CONFIG,market.getStandardMatchInfoId(),market.getMarketCategoryId(),market.getChildMarketCategoryId()));
    }

    /**
     * @Description   //设置配置值及默认配置
     * @Param [maxOdds, maxMyOdds]
     * @Author  Sean
     * @Date  15:17 2020/10/9
     * @return java.math.BigDecimal
     **/
    private BigDecimal getDefaultValue(BigDecimal maxOdds, String defaultValue) {
        return ObjectUtils.isEmpty(maxOdds) ? new BigDecimal(defaultValue) : maxOdds;
    }
    @Override
    public void getAndSetRcsMatchMarketConfig(RcsMatchMarketConfig config) {
        RcsMatchMarketConfig marketConfig = this.queryMatchMarketConfigNew(config);
        if (ObjectUtils.isEmpty(marketConfig)){
            marketConfig = config;
        }else {
            config.setAwayAutoChangeRate(marketConfig.getAwayAutoChangeRate());
        }
        BigDecimal max = null;
        BigDecimal min = null;
        BigDecimal margin = tradeVerificationService.getConfigMargin(marketConfig);;
        if (MarketKindEnum.Malaysia.getValue().equalsIgnoreCase(config.getMarketType())){
            max = getDefaultValue(marketConfig.getMaxOdds(), TradeConstant.MAX_MY_ODDS);
            min = getDefaultValue(marketConfig.getMinOdds(), TradeConstant.MIN_MY_ODDS);
            marketConfig.setMargin(getDefaultValue(margin, TradeConstant.DEFAULT_MY_SPREAD));
        }else if (MarketKindEnum.Europe.getValue().equalsIgnoreCase(config.getMarketType())){
            max = getDefaultValue(marketConfig.getMaxOdds(), TradeConstant.MAX_EU_ODDS);
            min = getDefaultValue(marketConfig.getMinOdds(), TradeConstant.MIN_EU_ODDS);
            marketConfig.setMargin(getDefaultValue(margin, TradeConstant.DEFAULT_EU_MARGIN));
        }
        config.setMargin(marketConfig.getMargin());
        config.setMaxOdds(max);
        config.setMinOdds(min);
        config.setSubPlayId(SubPlayUtil.getWebSubPlayId(config));
        config.setLinkageMode(marketConfig.getLinkageMode());
    }

    @Override
    public BuildMarketConfigDto getBuildMarketConfig(Long matchId, Long playId) {
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
        MarketBuildPlayConfig playConfig = this.baseMapper.queryMarketBuildPlayConfig(matchId, playId.intValue());
        log.info("::{}::构建盘口，玩法配置：playId={},playConfig={}", matchId, playId, JsonFormatUtils.toJson(playConfig));
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
        List<MarketBuildConfig> marketBuildConfigList = new ArrayList<>();
        if(Tennis.isManualBuildMarket(playId) || PingPong.isManualBuildMarket(playId) || IceHockey.isManualBuildMarket(playId)){
            marketBuildConfigList = this.baseMapper.listMarketBuildSubConfig(matchId, playId);
        }else{
            marketBuildConfigList = this.baseMapper.listMarketBuildConfig(matchId, playId);
        }
        log.info("::{}::构建盘口，位置配置：playId={},placeConfigList={}", matchId, playId, JsonFormatUtils.toJson(marketBuildConfigList));
        if (CollectionUtils.isNotEmpty(marketBuildConfigList)) {
            MarketBuildConfig mainConfig = marketBuildConfigList.get(0);
            marketHeadGap = Optional.ofNullable(mainConfig.getMarketHeadGap()).orElse(BigDecimal.ZERO);
            String redisKey = String.format("rcs:task:match:event:%s", matchId);
            String eventCode = redisClient.get(redisKey);
            log.info("::{}::构建盘口，事件编码：playId={},eventCode={}", matchId, playId, eventCode);
            boolean isTimeout = EventCodeEnum.isTimeout(eventCode);
            marketBuildConfigList.forEach(config -> {
                Integer placeNum = config.getPlaceNum();
                placeWaterDiffMap.put(placeNum, config.getPlaceWaterDiff());
                if (isTimeout) {
                    // 比赛暂停取暂停spread
                    spreadMap.put(placeNum, Optional.ofNullable(config.getTimeOutMargin()).orElse(mainSpread));
                } else {
                    spreadMap.put(placeNum, Optional.ofNullable(config.getMargin()).orElse(mainSpread));
                }
            });
        }
        if (configDto.getMatchType() == null) {
            StandardMatchInfo matchInfo = standardMatchInfoService.getById(matchId);
            if (matchInfo != null) {
                configDto.setMatchType(RcsConstant.isLive(matchInfo.getMatchStatus()) ? 0 : 1);
            }
        }
        configDto.setMarketHeadGap(marketHeadGap);
        configDto.setMarketCount(marketCount);
        configDto.setMarketNearDiff(marketNearDiff);
        configDto.setMarketNearOddsDiff(marketNearOddsDiff);
        configDto.setMarketAdjustRange(marketAdjustRange);
        configDto.setPlaceSpreadMap(spreadMap);
        configDto.setPlaceWaterDiffMap(placeWaterDiffMap);
        log.info("::{}::构建盘口配置：playId={},config={}", matchId, playId, JsonFormatUtils.toJson(configDto));
        return configDto;
    }

    @Override
    public void clearWaterDiff(Long matchId, Collection<Long> playIds) {
        LambdaUpdateWrapper<RcsMatchMarketConfig> wrapper = Wrappers.lambdaUpdate();
        wrapper.eq(RcsMatchMarketConfig::getMatchId, matchId)
                .in(RcsMatchMarketConfig::getPlayId, playIds);
        RcsMatchMarketConfig entity = new RcsMatchMarketConfig();
        entity.setAwayAutoChangeRate("0");
        this.baseMapper.update(entity, wrapper);
    }

    @Override
    public boolean playIsSell(Long matchId, Long playId) {
        String key = String.format(MATCH_ODDS_LIVE, matchId);
        String oddsLiveStr = redisClient.get(key);
        Integer oddsLive = StringUtils.isNotBlank(oddsLiveStr) ? Integer.parseInt(oddsLiveStr) : null;
        Integer pId = this.baseMapper.queryPlayIsSell(matchId, playId.intValue(),oddsLive);
        return pId != null;
    }

    @Override
    public List<Long> queryLinkageSellPlay(Long matchId, Integer matchType) {
        List<Integer> list = this.baseMapper.queryLinkageSellPlay(matchId, matchType);
        if (CollectionUtils.isEmpty(list)) {
            return Lists.newArrayList();
        }
        return list.stream().map(Integer::longValue).collect(Collectors.toList());
    }

    @Override
    public Map<Long, RcsMatchMarketConfigSub> getSubPlayConfig(Long matchId, Long playId, Integer placeNum) {
        LambdaQueryWrapper<RcsMatchMarketConfigSub> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(RcsMatchMarketConfigSub::getMatchId, matchId)
                .eq(RcsMatchMarketConfigSub::getPlayId, playId)
                .eq(RcsMatchMarketConfigSub::getMarketIndex, placeNum)
                .orderByAsc(RcsMatchMarketConfigSub::getId);
        List<RcsMatchMarketConfigSub> list = rcsMatchMarketConfigSubMapper.selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        Map<Long, RcsMatchMarketConfigSub> map = Maps.newHashMapWithExpectedSize(list.size());
        list.forEach(config -> map.put(NumberUtils.toLong(config.getSubPlayId()), config));
        return map;
    }

    @Override
    public void clearNewConfig(StandardSportMarket market, Long sportId) {
        log.info("::{}::clearNewConfig", CommonUtil.getRequestId());
        // 清空配置水差
        rcsMatchMarketConfigMapper.updateMatchMarketMarginConfigByMatch(market);
        redisClient.hashRemove(String.format(TradeConstant.REDIS_MATCH_MARKET_WATER,market.getStandardMatchInfoId()),market.getId().toString());
//        rcsNewSportWaterConfigMapper.clearNewTableWaterDiffV2(market);
        matchPlayConfigMapper.clearMarketHeadGapByMatch(market);
        redisClient.delete(String.format(TradeConstant.REDIS_MATCH_MARKET_HEAD_CONFIG,market.getStandardMatchInfoId(),market.getMarketCategoryId(),market.getChildMarketCategoryId()));
    }
}
