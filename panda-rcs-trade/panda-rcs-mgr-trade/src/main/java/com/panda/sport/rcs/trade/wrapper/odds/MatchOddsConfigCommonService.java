package com.panda.sport.rcs.trade.wrapper.odds;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.merge.api.ITradeMarketOddsApi;
import com.panda.merge.common.enums.DataSourceCodeEnum;
import com.panda.merge.dto.Request;
import com.panda.merge.dto.Response;
import com.panda.merge.dto.StandardMarketDTO;
import com.panda.merge.dto.StandardMarketOddsDTO;
import com.panda.merge.dto.StandardMatchMarketDTO;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.*;
import com.panda.sport.rcs.mapper.RcsMatchMarketConfigMapper;
import com.panda.sport.rcs.mapper.StandardSportMarketMapper;
import com.panda.sport.rcs.mapper.tourTemplate.RcsTournamentTemplatePlayMargainMapper;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.StandardSportMarket;
import com.panda.sport.rcs.pojo.config.MarketBuildConfig;
import com.panda.sport.rcs.pojo.constants.TradeConstant;
import com.panda.sport.rcs.pojo.dto.odds.MatchMarketPlaceConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchOddsConfig;
import com.panda.sport.rcs.pojo.dto.odds.MatchPlayConfig;
import com.panda.sport.rcs.pojo.dto.odds.RcsStandardMarketDTO;
import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplatePlayMargain;
import com.panda.sport.rcs.trade.enums.BasketBallPlayIdScoreTypeEnum;
import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.trade.service.OddsRangeService;
import com.panda.sport.rcs.trade.service.TradeCommonService;
import com.panda.sport.rcs.trade.service.TradeStatusService;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.util.MarginUtils;
import com.panda.sport.rcs.trade.wrapper.MarketStatusService;
import com.panda.sport.rcs.trade.wrapper.RcsOddsConvertMappingService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.utils.*;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MatchOddsConfigCommonService {

    @Autowired
    private TradeStatusService tradeStatusService;
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;

    @Autowired
    private StandardSportMarketMapper standardSportMarketMapper;
    @Autowired
    private RcsMatchMarketConfigMapper rcsMatchMarketConfigMapper;

    @Autowired
    private RcsOddsConvertMappingService rcsOddsConvertMappingService;
    @Autowired
    private MarketStatusService marketStatusService;
    @Autowired
    private TradeVerificationService tradeVerificationService;
    @Autowired
    private TradeCommonService tradeCommonService;
    @Autowired
    private OddsRangeService oddsRangeService;
    @Autowired
    private RcsTournamentTemplatePlayMargainMapper rcsTournamentTemplatePlayMargainMapper;

    @Autowired
    private RedisClient redisClient;

    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ITradeMarketOddsApi tradeMarketOddsApi;

    public static final String DEFAULT_DATA_SOURCE = "PA";
    public static final List<String> BARSKETBALL_HANDICAP_PLAY = Lists.newArrayList("39","58","19","64","46","143","52");
    private static List<Integer> SINGLE_WIN_PLAYS = Arrays.asList(37,41,43,48,54,60,66,142);
    private static List<Integer> ODDS_EVEN_PLAYS = Arrays.asList(40,59,42,65,47,53,75);
    private static List<Long> BARSKETBALL_BENCHMARK_PLAYS = Arrays.asList(4L,19L,143L);

    public void updateAdditons(MatchOddsConfig matchConfig, StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> playAllMarketList) {
        for (RcsStandardMarketDTO dto : playAllMarketList){
            if (SINGLE_WIN_PLAYS.contains(dto.getMarketCategoryId().intValue()) ||
                ODDS_EVEN_PLAYS.contains(dto.getMarketCategoryId().intValue())){
                dto.setAddition1(null);
                dto.setAddition2(null);
                dto.setAddition5(null);
            }
            dto.setDataSourceCode(DEFAULT_DATA_SOURCE);
            if (CollectionUtils.isNotEmpty(dto.getMarketOddsList())){
                for (StandardMarketOddsDTO o : dto.getMarketOddsList()){
                    o.setDataSourceCode(DEFAULT_DATA_SOURCE);
                }
            }
            // 篮球addition2置空
            if("2".equals(String.valueOf(matchInfo.getSportId())) &&
                    !BARSKETBALL_BENCHMARK_PLAYS.contains(dto.getMarketCategoryId())) dto.setAddition2(null);
            //如果足球有比分，需要计算add1,add3,add4
           if("1".equals(String.valueOf(matchInfo.getSportId()))) updateAddition1(matchConfig,dto);
        }
    }

    /**
     * 如果有比分，就重新构建add1 ，add3，add4
    * @Title: updateAddition1
    * @Description: TODO
    * @param @param matchConfig
    * @param @param dto    设定文件
    * @return void    返回类型
    * @throws
     */
    public void updateAddition1(MatchOddsConfig matchConfig, RcsStandardMarketDTO dto) {
    	try {
    		if(matchConfig.getScoreMap() == null || matchConfig.getScoreMap().size() <= 0 ) return;

        	if(!TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(dto.getMarketCategoryId().intValue())) return ;

        	String score = null;
        	//比分类 todo 15分钟玩法待确认
        	if(matchConfig.getScoreMap().containsKey("1") && (TradeConstant.FOOTBALL_BENCHMARK_SCORE_PLAYS.contains(dto.getMarketCategoryId().intValue()))) {
        		score = matchConfig.getScoreMap().get("1");
        	}

        	if(score == null ) return ;

            String add1 = dto.getAddition1();
            String home = score.split(":")[0];
            String away = score.split(":")[1];
            if(334 != dto.getMarketCategoryId().intValue()){
                String add2 = CommonUtils.toBigDecimal(add1).add(CommonUtils.toBigDecimal(away)).subtract(CommonUtils.toBigDecimal(home)).stripTrailingZeros().toPlainString();
                dto.setAddition2(add2);
            }else{
                dto.setAddition2(add1);
            }
            dto.setAddition3(home);
            dto.setAddition4(away);
    	}catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
    	}
	}

	/**
     * @Description   //位置封盘
     * @Param [matchId, playConfig]
     * @Author  sean
     * @Date   2021/1/22
     * @return void
     **/
    public void closeMarket(StandardMatchInfo matchInfo, MatchPlayConfig playConfig,String subPlayId) {
        if (!ObjectUtils.isEmpty(playConfig.getStatus())){
            // 跳赔，只更新状态不推赔率，推送赔率在后面进行
            MarketStatusUpdateVO vo = new MarketStatusUpdateVO()
                    .setSportId(matchInfo.getSportId())
                    .setTradeLevel(TradeLevelEnum.PLAY.getLevel())
                    .setMatchId(matchInfo.getId())
                    .setCategoryId(Long.parseLong(playConfig.getPlayId()))
                    .setIsPushOdds(YesNoEnum.N.getValue());
            if (playConfig.getStatus().intValue() == LinkedTypeEnum.JUMP_LIMIT.getCode()){
                vo.setLinkedType(LinkedTypeEnum.JUMP_LIMIT.getCode()).setRemark(LinkedTypeEnum.JUMP_LIMIT.getRemark()).setMarketStatus(NumberUtils.INTEGER_ONE);
            }else if (playConfig.getStatus().intValue() == LinkedTypeEnum.JUMP_ODDS.getCode()){
                vo.setLinkedType(LinkedTypeEnum.JUMP_ODDS.getCode()).setRemark(LinkedTypeEnum.JUMP_ODDS.getRemark()).setMarketStatus(NumberUtils.INTEGER_ONE);
            }else if (playConfig.getStatus().intValue() == LinkedTypeEnum.TRADE_OVER_LIMIT.getCode()){
                vo.setLinkedType(LinkedTypeEnum.TRADE_OVER_LIMIT.getCode()).setRemark(LinkedTypeEnum.TRADE_OVER_LIMIT.getRemark()).setMarketStatus(NumberUtils.INTEGER_ONE);
            }
            if (StringUtils.isNotBlank(subPlayId)){
                vo.setSubPlayId(Long.parseLong(subPlayId));
            }
            tradeStatusService.updateTradeStatus(vo);
        }
    }

    /**
     * 调用融合RPC接口，操盘标准盘口及赔率数据处理
     *
     * @param matchInfo
     * @param marketList
     * @return
     */
    public void putTradeMarketOdds(StandardMatchInfo matchInfo, List<StandardMarketDTO> marketList) {
        if (CollectionUtils.isEmpty(marketList)){
            log.info("::{}::没有赔率不推送",CommonUtil.getRequestId());
            return;
        }
        marketList.stream().collect(Collectors.groupingBy(StandardMarketDTO::getMarketCategoryId)).forEach((playId, list) -> {
            tradeStatusService.handlePushStatus(matchInfo.getSportId(), matchInfo.getId(), playId, list, null, null, 0, 0, 0);
        });
        StandardMatchMarketDTO standardMatchMarketDTO = new StandardMatchMarketDTO();
        standardMatchMarketDTO.setStandardMatchInfoId(matchInfo.getId());
        standardMatchMarketDTO.setMarketList(marketList);
        DataRealtimeApiUtils.handleApi(standardMatchMarketDTO, new DataRealtimeApiUtils.ApiCall() {
            @Override
            @Trace
            public <R> Response<R> callApi(Request request) {
                return tradeMarketOddsApi.putTradeMarketOdds(request);
            }
        });
    }

    /**
     * @param matchType 构建当前玩法符合条件的所有盘口数据
     *                  根据盘口数实时计算对应的盘口
     * @param @param    matchId
     * @param @param    oddsList    设定文件
     * @return void    返回类型
     * @throws
     * @Title: matchOddsSpread
     * @Description: TODO
     */
    public List<RcsStandardMarketDTO> buildCurrentMarketList(StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> playAllMarketList, RcsTournamentTemplatePlayMargain tournamentConfig, Integer matchType) {
        Long sportId = matchInfo.getSportId();
        List<RcsStandardMarketDTO> resultList = new ArrayList<>();
        // 足球不走这个逻辑
        if (NumberUtils.INTEGER_ONE.intValue() == sportId){
            log.info("::{}::足球不构建盘口数matchInfo={}",CommonUtil.getRequestId(),JSONObject.toJSONString(matchInfo));
            return playAllMarketList;
        }
        // margin是null的，暂时不支持新增
        if (tournamentConfig.getMargain() == null) {
            log.warn("::{}::margin 是空的，不做新增盘口处理！：{}",CommonUtil.getRequestId(),JSONObject.toJSONString(tournamentConfig));
            return playAllMarketList;
        }
        Integer marketCount = Optional.ofNullable(tournamentConfig.getMarketCount()).orElse(3);
        // 相邻盘口差值
        BigDecimal marketNearDiff = Optional.ofNullable(tournamentConfig.getMarketNearDiff()).orElse(BigDecimal.ONE);
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = Optional.ofNullable(tournamentConfig.getMarketNearOddsDiff()).orElse(new BigDecimal("0.15"));
        BigDecimal spread = CommonUtils.toBigDecimal(tournamentConfig.getMargain(), new BigDecimal("0.2"));

        RcsStandardMarketDTO mainMarket = playAllMarketList.get(0);
        Long playId = mainMarket.getMarketCategoryId();

        if (!Basketball.isHandicapOrTotal(playId) && !Tennis.isManualBuildMarket(playId) && !PingPong.isManualBuildMarket(playId)
                && !IceHockey.isManualBuildMarket(playId)) {
            return playAllMarketList;
        }
        if (marketCount > 1 && !CommonUtils.isNumber(mainMarket.getAddition1())) {
            log.info("::{}::主盘口值不是数字，放弃构建！playAllMarketList:" + JSONObject.toJSONString(playAllMarketList),CommonUtil.getRequestId());
            return playAllMarketList;
        }
        BigDecimal marketValue = new BigDecimal(mainMarket.getAddition1());
        List<BigDecimal> marketValueList = null;
        //篮球构建盘口值
        if(SportIdEnum.isBasketball(sportId)){
            marketValueList = MarketUtils.generateMarketValues(playId, marketCount, marketValue, marketNearDiff);
        }else if(SportIdEnum.isTennis(sportId) || SportIdEnum.isPingpong(sportId) || SportIdEnum.isIceHockey(sportId)){
            //网球、乒乓球 构建盘口值
            if(SportIdEnum.isIceHockey(sportId) && Lists.newArrayList(2L,295L).contains(playId) && RcsConstant.SPECIAL_MARKET_VALUE.compareTo(marketValue) == 0){
                marketCount = 1;
            }
            marketValueList = MarketUtils.generateMarketValuesTennisAndPong(playId, marketCount, marketValue, marketNearDiff);
        }
        // 玩法水差
        BigDecimal playWaterDiff = BigDecimal.ZERO;
        // 位置水差
        Map<Integer, BigDecimal> placeWaterDiffMap = Maps.newHashMap();
        // 盘口变多，查询水差，构建新盘口
        List<MarketBuildConfig> marketBuildConfigList = new ArrayList<>();
        if (Tennis.isManualBuildMarket(playId) || PingPong.isManualBuildMarket(playId) || IceHockey.isManualBuildMarket(playId)) {
            marketBuildConfigList = rcsMatchMarketConfigMapper.listMarketBuildSubConfig(matchInfo.getId(), playId);
        } else {
            marketBuildConfigList = rcsMatchMarketConfigMapper.listMarketBuildConfig(matchInfo.getId(), playId);
        }
        if (CollectionUtils.isNotEmpty(marketBuildConfigList)) {
            placeWaterDiffMap = marketBuildConfigList.stream().collect(Collectors.toMap(MarketBuildConfig::getPlaceNum, MarketBuildConfig::getPlaceWaterDiff));
        }

        BigDecimal malayOdds = null;
        for (int i = 0; i < marketCount; i++) {
            int placeNum = i + 1;
            if (i == 0) {
                malayOdds = BigDecimal.ONE.subtract(spread.divide(new BigDecimal("2"), 6, BigDecimal.ROUND_HALF_UP)).setScale(2, BigDecimal.ROUND_DOWN);
            }
            BigDecimal homeOdds = malayOdds;
            BigDecimal awayOdds = malayOdds;
            if (i > 0) {
                BigDecimal factor = (i % 2 == 1) ? new BigDecimal(i / 2 + 1) : new BigDecimal(i / 2).negate();
                BigDecimal upOdds = malayOdds.add(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
                homeOdds = OddsConvertUtils.checkMalayOdds(upOdds);
                BigDecimal downOdds = malayOdds.subtract(factor.multiply(marketNearOddsDiff)).setScale(2, BigDecimal.ROUND_DOWN);
                awayOdds = OddsConvertUtils.checkMalayOdds(downOdds);
            }
            // 水差 = 玩法水差 + 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            BigDecimal waterDiff = playWaterDiff.add(placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO));
            homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds.subtract(waterDiff));
            awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds.add(waterDiff));
            int homeOddsValue = malayOddsToOddsValue(homeOdds);
            int awayOddsValue = malayOddsToOddsValue(awayOdds);

            BigDecimal mv = marketValueList.get(i);
            RcsStandardMarketDTO addMarket = JSONObject.parseObject(JSONObject.toJSONString(mainMarket), RcsStandardMarketDTO.class);
            addMarket.setAddition1(mv.toPlainString());
            addMarket.setOddsValue(mv.toPlainString());
            if (playId == 4L || playId == 19L || playId == 143L) {
                addMarket.setAddition2(mv.toPlainString());
            }
            addMarket.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            addMarket.setScopeId(mainMarket.getScopeId() != null ? mainMarket.getScopeId() : "");
            addMarket.setPlaceNum(placeNum);
            if (matchType != null) addMarket.setMarketType(matchType);
            addMarket.getMarketOddsList().forEach(bean -> {
                bean.setMarketDiffValue(null);
                bean.setMargin(spread.doubleValue());
                if (OddsTypeEnum.isHome(bean.getOddsType()) || OddsTypeEnum.isOver(bean.getOddsType())) {
                    bean.setOddsValue(homeOddsValue);
                } else if (OddsTypeEnum.isAway(bean.getOddsType()) || OddsTypeEnum.isUnder(bean.getOddsType())) {
                    bean.setOddsValue(awayOddsValue);
                    bean.setMarketDiffValue(waterDiff.doubleValue());
                }
            });
            resultList.add(addMarket);
        }

        log.info("::{}::构建新的数据：playAllMarketList：{}，resultList：{},tournamentConfig:{}",CommonUtil.getRequestId()
                , JSONObject.toJSONString(playAllMarketList), JSONObject.toJSONString(resultList), JSONObject.toJSONString(tournamentConfig));
        return resultList;
    }

    public List<RcsStandardMarketDTO> buildMarketList(StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> playAllMarketList, RcsTournamentTemplatePlayMargain playTemplateConfig, Map<Integer, BigDecimal> placeSpreadMap) {
        Long sportId = matchInfo.getSportId();
        Long matchId = matchInfo.getId();
        // 盘口类型，1-赛前盘，0-滚球盘
        Integer matchType = RcsConstant.isLive(matchInfo.getMatchStatus()) ? 0 : 1;
        if (!SportIdEnum.isBasketball(sportId) && !SportIdEnum.isTennis(sportId) && !SportIdEnum.isPingpong(sportId) && !SportIdEnum.isIceHockey(sportId)) {
            log.warn("::{}::只有篮球、乒乓球、网球、冰球 才构建盘口：sportId={}", matchId, sportId);
            return playAllMarketList;
        }
        RcsStandardMarketDTO mainMarket = playAllMarketList.get(0);
        Long playId = mainMarket.getMarketCategoryId();
        if (!Basketball.Main.isHandicapOrTotal(playId) && !Tennis.isManualBuildMarket(playId) && !PingPong.isManualBuildMarket(playId)) {
            log.warn("::{}::不是主要玩法让分和大小，放弃构建：playId={}", matchId, playId);
            return playAllMarketList;
        }
        String addition1 = mainMarket.getAddition1();
        if (!CommonUtils.isNumber(addition1)) {
            log.warn("::{}::主盘口值不是数字，放弃构建：playId={},addition1={}", matchId, playId, addition1);
            return playAllMarketList;
        }

        Integer marketCount = Optional.ofNullable(playTemplateConfig.getMarketCount()).orElse(1);
        // 相邻盘口差值
        BigDecimal marketNearDiff = Optional.ofNullable(playTemplateConfig.getMarketNearDiff()).orElse(BigDecimal.ONE);
        // 相邻盘口赔率差值
        BigDecimal marketNearOddsDiff = Optional.ofNullable(playTemplateConfig.getMarketNearOddsDiff()).orElse(new BigDecimal("0.15"));
        // 主盘 spread
        final BigDecimal mainSpread = CommonUtils.toBigDecimal(playTemplateConfig.getMargain(), new BigDecimal("0.2"));

        List<BigDecimal> marketValueList = null;
        //篮球构建盘口值
        if (SportIdEnum.isBasketball(sportId)){
            marketValueList = MarketUtils.generateMarketValues(playId, marketCount, new BigDecimal(addition1), marketNearDiff);
        }else if (SportIdEnum.isTennis(sportId) || SportIdEnum.isPingpong(sportId) || SportIdEnum.isIceHockey(sportId)){
            //网球、乒乓球构建盘口值
            marketValueList = MarketUtils.generateMarketValuesTennisAndPong(playId, marketCount, new BigDecimal(addition1), marketNearDiff);
        }
        // 玩法水差
        BigDecimal playWaterDiff = BigDecimal.ZERO;
        // 位置水差
        Map<Integer, BigDecimal> placeWaterDiffMap = Maps.newHashMap();
        // 盘口变多，查询水差，构建新盘口
        List<MarketBuildConfig> marketBuildConfigList = new ArrayList<>();
        if(Tennis.isManualBuildMarket(playId) || PingPong.isManualBuildMarket(playId) || IceHockey.isManualBuildMarket(playId)){
            marketBuildConfigList = rcsMatchMarketConfigMapper.listMarketBuildSubConfig(matchId, playId);
        }else{
            marketBuildConfigList = rcsMatchMarketConfigMapper.listMarketBuildConfig(matchId, playId);
        }
        if (CollectionUtils.isNotEmpty(marketBuildConfigList)) {
            playWaterDiff = marketBuildConfigList.get(0).getPlayWaterDiff();
            placeWaterDiffMap = marketBuildConfigList.stream().collect(Collectors.toMap(MarketBuildConfig::getPlaceNum, MarketBuildConfig::getPlaceWaterDiff));
        }

        List<RcsStandardMarketDTO> resultList = new ArrayList<>();
        for (int i = 0; i < marketCount; i++) {
            int placeNum = i + 1;
            BigDecimal spread = placeSpreadMap.getOrDefault(placeNum, mainSpread);
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
            // 水差 = 玩法水差 + 位置水差，篮球水差固定在下盘，上盘赔率减水差，下盘赔率加水差
            BigDecimal waterDiff = playWaterDiff.add(placeWaterDiffMap.getOrDefault(placeNum, BigDecimal.ZERO));
            homeOdds = OddsConvertUtils.checkMalayOdds(homeOdds.subtract(waterDiff));
            awayOdds = OddsConvertUtils.checkMalayOdds(awayOdds.add(waterDiff));
            int homeOddsValue = malayOddsToOddsValue(homeOdds);
            int awayOddsValue = malayOddsToOddsValue(awayOdds);

            BigDecimal mv = marketValueList.get(i).stripTrailingZeros();
            RcsStandardMarketDTO newMarket = JSONObject.parseObject(JSONObject.toJSONString(mainMarket), RcsStandardMarketDTO.class);
            newMarket.setMarketType(matchType);
            newMarket.setAddition1(mv.toPlainString());
            newMarket.setOddsValue(mv.toPlainString());
            if (RcsConstant.BENCHMARK_SCORE.contains(playId)) {
                // 基准分
                newMarket.setAddition2(mv.toPlainString());
            }
            newMarket.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
            newMarket.setScopeId(mainMarket.getScopeId());
            newMarket.setPlaceNum(placeNum);

            newMarket.getMarketOddsList().forEach(bean -> {
                bean.setMarketDiffValue(null);
                bean.setMargin(mainSpread.doubleValue());
                if (OddsTypeEnum.isHomeOddsType(bean.getOddsType())) {
                    bean.setOddsValue(homeOddsValue);
                } else if (OddsTypeEnum.isAwayOddsType(bean.getOddsType())) {
                    bean.setOddsValue(awayOddsValue);
                    bean.setMarketDiffValue(waterDiff.doubleValue());
                }
                bean.setDataSourceCode(BaseConstants.DATA_SOURCE_CODE);
                bean.setNameExpressionValue(MarketUtils.getNameExpressionValue(bean.getOddsType(), mv));
            });

            resultList.add(newMarket);
        }

        log.info("::{}::构建新的数据：playAllMarketList：{}，resultList：{},tournamentConfig:{}",CommonUtil.getRequestId()
                , JSONObject.toJSONString(playAllMarketList), JSONObject.toJSONString(resultList), JSONObject.toJSONString(playTemplateConfig));
        return resultList;
    }

    private int malayOddsToOddsValue(BigDecimal malayOdds) {
        BigDecimal euOdds = new BigDecimal(rcsOddsConvertMappingService.getEUOdds(malayOdds.toPlainString()));
        return euOdds.multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
    }

    /**
     * 计算盘口的spread
     *
     * @param @param bean
     * @param @param placeNumConfig    设定文件
     * @return void    返回类型
     * @throws
     * @Title: matchOddsMarketSpread
     * @Description: TODO
     */
    public void matchOddsMarketSpread(StandardMatchInfo matchInfo,RcsStandardMarketDTO bean, MatchMarketPlaceConfig placeNumConfig) {
        log.info("::{}::计算盘口的spread matchInfo ={},bean={},placeNumConfig={}",CommonUtil.getRequestId(),JSONObject.toJSONString(matchInfo),JSONObject.toJSONString(bean),JSONObject.toJSONString(placeNumConfig));
        if (placeNumConfig == null || placeNumConfig.getSpread() == null) {
            return;
        }

        if (bean.getMarketOddsList().size() > 3 ) {
            log.warn("::{}::当前不是两项盘 ，不重做spread或者margin计算：{}",matchInfo.getId(), JSONObject.toJSONString(bean));
            return;
        }
        if (TradeConstant.BASKETBALL_MAIN_EU_PLAYS.contains(bean.getMarketCategoryId().intValue()) ||
            TradeConstant.FOOTBALL_X_EU_PLAYS.contains(bean.getMarketCategoryId().intValue()) ||
             TradeConstant.BASKETBALL_X_EU_PLAYS.contains(bean.getMarketCategoryId().intValue()) ||
                Tennis.capotPlays(bean.getMarketCategoryId()) ||
                PingPong.capotPlays(bean.getMarketCategoryId()) ||
                IceHockey.capotPlays(bean.getMarketCategoryId()) ||
                bean.getMarketOddsList().size() == 3){
            tradeCommonService.setDefaultAnchor(bean);
            // margin算法计算赔率
            tradeCommonService.calculationOddsByMarginDiff(bean, new BigDecimal(placeNumConfig.getSpread()), new BigDecimal(placeNumConfig.getOldMargin()));
        }else {
            StandardMarketOddsDTO downOdds = null;
            StandardMarketOddsDTO upperOdds = null;
            for (StandardMarketOddsDTO oddsBean : bean.getMarketOddsList()) {
                if (getOddsType(bean.getAddition1(), oddsBean.getOddsType()).equals(oddsBean.getOddsType())) {
                    downOdds = oddsBean;
                } else {
                    upperOdds = oddsBean;
                }
            }

            if (downOdds == null) {
                log.warn("::{}::受让方获取失败，请检查：{}",matchInfo.getId(), JSONObject.toJSONString(bean));
                return;
            }
            StandardMarketOddsDTO maxSpread = getMaxSpread(downOdds,upperOdds);
            String downOddValue = rcsOddsConvertMappingService.getMyOdds(downOdds.getOddsValue());
            String upperOddValue = rcsOddsConvertMappingService.getMyOdds(upperOdds.getOddsValue());
            BigDecimal oldSpread = caleOldSpread(downOddValue,upperOddValue);
            if (SportIdEnum.isFootball(matchInfo.getSportId()) && !ObjectUtils.isEmpty(placeNumConfig.getOldMargin())){
                oldSpread = new BigDecimal(placeNumConfig.getOldMargin());
            }
            BigDecimal spreadDiff = oldSpread.subtract(new BigDecimal(placeNumConfig.getSpread()));
//            BigDecimal oldSpread = new BigDecimal(downOdds.getMargin());
            if (spreadDiff.doubleValue() == 0){
                return;
            }

            BigDecimal changeSpread = spreadDiff.divide(new BigDecimal("2"), NumberUtils.INTEGER_TWO , BigDecimal.ROUND_DOWN);

            if (!maxSpread.getOddsType().equalsIgnoreCase(downOdds.getOddsType())){
                downOddValue = new BigDecimal(downOddValue).add(spreadDiff).subtract(changeSpread.multiply(new BigDecimal(NumberUtils.INTEGER_TWO))).toPlainString();
            }
            downOddValue = new BigDecimal(downOddValue).add(changeSpread).toPlainString();
            downOddValue = MarginUtils.checkMyOdds(new BigDecimal(downOddValue)).toPlainString();

//            BigDecimal flag = new BigDecimal(downOddValue).add(new BigDecimal(placeNumConfig.getSpread()));
//
//            if (flag.doubleValue() >= 1) {
//                upperOddValue = (new BigDecimal(2)).subtract(flag).toPlainString();
//            } else {
//                upperOddValue = flag.multiply(new BigDecimal("-1")).toPlainString();
//            }
            upperOddValue = MarginUtils.caluOddsBySpread(new BigDecimal(downOddValue),new BigDecimal(placeNumConfig.getSpread())).toPlainString();

            Integer downOddsValNew = new BigDecimal(rcsOddsConvertMappingService.getEUOdds(downOddValue)).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
            downOdds.setOddsValue(downOddsValNew);
            if (!ObjectUtils.isEmpty(placeNumConfig.getSpread())){
                downOdds.setMargin(Double.parseDouble(placeNumConfig.getSpread()));
            }

            Integer upperOddsVal = new BigDecimal(rcsOddsConvertMappingService.getEUOdds(upperOddValue)).multiply(new BigDecimal(BaseConstants.MULTIPLE_VALUE)).intValue();
            upperOdds.setOddsValue(upperOddsVal);
            if (!ObjectUtils.isEmpty(placeNumConfig.getSpread())){
                upperOdds.setMargin(Double.parseDouble(placeNumConfig.getSpread()));
            }

            if (placeNumConfig.getStatus() != null) bean.setStatus(placeNumConfig.getStatus());
            if (placeNumConfig.getThirdDataSourceStatus() != null)
                bean.setThirdMarketSourceStatus(placeNumConfig.getThirdDataSourceStatus());
        }
    }
    /**
     * @Description   //获取多的spread一方
     * @Param [downOdds, upperOdds]
     * @Author  sean
     * @Date   2021/9/3
     * @return com.panda.merge.dto.StandardMarketOddsDTO
     **/
    private StandardMarketOddsDTO getMaxSpread(StandardMarketOddsDTO downOdds, StandardMarketOddsDTO upperOdds) {
        if (downOdds.getOddsValue() >= upperOdds.getOddsValue()){
            return upperOdds;
        }
        return downOdds;
    }

    /**
    * @Title: caleOldSpread
    * @Description: 计算旧spread
    * @param @param downOddValue
    * @param @param upperOddValue
    * @param @return    设定文件
    * @return BigDecimal    返回类型
    * @throws
     */
	public BigDecimal caleOldSpread(String downOddValue, String upperOddValue) {
		if(new BigDecimal(downOddValue).multiply(new BigDecimal(upperOddValue)).compareTo(BigDecimal.ZERO) > 0 ) {
			return new BigDecimal("2").subtract(new BigDecimal(downOddValue)).subtract(new BigDecimal(upperOddValue));
		}else {
			return new BigDecimal(downOddValue).add(new BigDecimal(upperOddValue)).abs();
		}
	}

    /**
     * @return java.lang.String
     * @Description //获取受让方
     * @Param [market, config]
     * @Author Sean
     * @Date 15:09 2020/10/7
     **/
    public String getOddsType(String addition1, String currentOddsType) {
        log.info("::{}::根据投注项获取调配盘，addition1:{},currentOddsType：{}",CommonUtil.getRequestId(), addition1, currentOddsType);
        String oddsType = BaseConstants.ODD_TYPE_2;

        if (BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(currentOddsType) ||
                BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(currentOddsType)) {
            oddsType = BaseConstants.ODD_TYPE_UNDER;
        } else if (BaseConstants.ODD_TYPE_ODD.equalsIgnoreCase(currentOddsType) ||
                BaseConstants.ODD_TYPE_EVEN.equalsIgnoreCase(currentOddsType)) {
            oddsType = BaseConstants.ODD_TYPE_EVEN;
        }else if (BaseConstants.ODD_TYPE_YES.equalsIgnoreCase(currentOddsType) ||
                BaseConstants.ODD_TYPE_NO.equalsIgnoreCase(currentOddsType)) {
            oddsType = BaseConstants.ODD_TYPE_NO;
        } else if (StringUtils.isNotBlank(addition1)) {
            BigDecimal value = new BigDecimal(addition1);
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                oddsType = BaseConstants.ODD_TYPE_2;
            } else {
                oddsType = BaseConstants.ODD_TYPE_1;
            }
        }

        return oddsType;
    }
    /**
     * @Description   //构建盘口数据
     * @Param [matchPlayConfig]
     * @Author  sean
     * @Date   2020/12/9
     * @return java.util.List<com.panda.merge.dto.StandardMarketDTO>
     **/
    public void initOddsList(List<RcsStandardMarketDTO> oddsList,MatchPlayConfig matchPlayConfig,Integer matchType,String subPlayId) {
        if (CollectionUtils.isNotEmpty(matchPlayConfig.getMarketList())){
            RcsStandardMarketDTO marketDTO = matchPlayConfig.getMarketList().get(NumberUtils.INTEGER_ZERO);
            RcsStandardMarketDTO dto = new RcsStandardMarketDTO();
            dto.setMarketCategoryId(Long.parseLong(matchPlayConfig.getPlayId()));
            dto.setChildStandardCategoryId(Long.parseLong(subPlayId));
            dto.setMarketType(matchType);
            dto.setOddsReplaceOrder(NumberUtils.INTEGER_TWO);
            dto.setPlaceNum(NumberUtils.INTEGER_ONE);
            dto.setTradeType(NumberUtils.INTEGER_ONE);
            dto.setAddition1(StringUtils.isBlank(marketDTO.getAddition1()) ? NumberUtils.INTEGER_ZERO.toString() : marketDTO.getAddition1());
            dto.setAddition2(marketDTO.getAddition2());
            dto.setAddition3(marketDTO.getAddition3());
            dto.setDataSourceCode(DEFAULT_DATA_SOURCE);
            dto.setStatus(NumberUtils.INTEGER_ZERO);
            dto.setThirdMarketSourceStatus(NumberUtils.INTEGER_ZERO);
            // 原始盘口值
            dto.setAddition5("0.5");
            for (StandardMarketOddsDTO oddsDTO : marketDTO.getMarketOddsList()){
                oddsDTO.setDataSourceCode(DEFAULT_DATA_SOURCE);
                oddsDTO.setActive(NumberUtils.INTEGER_ONE);
                oddsDTO.setNameExpressionValue(marketDTO.getAddition1());

                Long oddsFieldsTemplateId = getOddsFieldsTemplateId(dto,oddsDTO);
                oddsDTO.setOddsFieldsTemplateId(oddsFieldsTemplateId);
                //2129兼容网球、乒乓球 让分玩法154 155 172
                if (BARSKETBALL_HANDICAP_PLAY.contains(matchPlayConfig.getPlayId()) || Tennis.pointsPlays(Long.parseLong(matchPlayConfig.getPlayId())) || PingPong.pointsPlays(Long.parseLong(matchPlayConfig.getPlayId()))){
                    dto.setAddition2(dto.getAddition1());
                    if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(oddsDTO.getOddsType())){
                        BigDecimal marketValue = new BigDecimal(dto.getAddition1()).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE));
                        if (marketValue.doubleValue() == marketValue.intValue()){
                            oddsDTO.setNameExpressionValue(marketValue.intValue()+"");
                        }else {
                            oddsDTO.setNameExpressionValue(marketValue.doubleValue()+"");
                        }
                    }
                }
            }
            dto.setMarketOddsList(marketDTO.getMarketOddsList());
            oddsList.add(dto);
        }
        log.info("::{}::初始化的赔率列表={}",CommonUtil.getRequestId(),JSONObject.toJSONString(oddsList));
    }
    /**
     * @Description   //设置投注项名称
     * @Param [dto, oddsDTO]
     * @Author  sean
     * @Date   2020/12/20
     * @return java.lang.Long
     **/
    private Long getOddsFieldsTemplateId(RcsStandardMarketDTO dto, StandardMarketOddsDTO oddsDTO) {
        List<StandardMarketOddsDTO> oddsFieldsTempletIds = standardSportMarketMapper.selectOddsFieldsTempletId(dto.getMarketCategoryId());
        return tradeCommonService.getOddsFieldsTemplateId(oddsFieldsTempletIds,oddsDTO,dto.getMarketCategoryId());
    }

    /**
     * @return java.lang.Boolean
     * @Description //设置盘口值和盘口差
     * @Param [oddsList, matchPlayConfig]
     * @Author sean
     * @Date 2020/12/2
     **/
    public void setMarketValueAndMarketDiffValue(StandardMatchInfo matchInfo, List<RcsStandardMarketDTO> oddsList, MatchPlayConfig matchPlayConfig,MatchOddsConfig matchConfig,String subPlayId) {
        // 根据盘口值获取水差值
        List<StandardSportMarket> markets = Lists.newArrayList();
        BigDecimal marketOrgValue = BigDecimal.ZERO;

        for (RcsStandardMarketDTO dto : oddsList) {
            if (StringUtils.isNotBlank(dto.getAddition1()) && dto.getChildStandardCategoryId().toString().equalsIgnoreCase(subPlayId)) {
                // 盘口值=原始盘口值+盘口差，所以新盘口值=当前盘口值-原盘口差+当前盘口差
                BigDecimal marketHeadGap = ObjectUtils.isEmpty(dto.getMarketHeadGap()) ? new BigDecimal(NumberUtils.DOUBLE_ZERO) : new BigDecimal(dto.getMarketHeadGap());
                BigDecimal totalChange = new BigDecimal(matchPlayConfig.getMarketHeadGap()).subtract(marketHeadGap);
                marketOrgValue = StringUtils.isNotBlank(dto.getAddition5()) ? new BigDecimal(dto.getAddition5()) : new BigDecimal(dto.getAddition1());
                RcsTournamentTemplatePlayMargain rcsTournamentTemplatePlayMargain = matchPlayConfig.getRcsTournamentTemplatePlayMargain();
                BigDecimal marketAdjustRange = rcsTournamentTemplatePlayMargain.getMarketAdjustRange();
                // 原始盘口值
                BigDecimal newMarketValue = TradeVerificationService.getNewMainMarketValue(new BigDecimal(dto.getAddition1()),totalChange,marketAdjustRange,marketOrgValue.toPlainString(),Long.parseLong(matchPlayConfig.getPlayId())).stripTrailingZeros();
                // 球头改变独赢需要封盘 或出现0 +- 0.5封盘
                if ((new BigDecimal(dto.getAddition1()).multiply(newMarketValue).compareTo(new BigDecimal(NumberUtils.DOUBLE_ZERO)) < NumberUtils.DOUBLE_ZERO &&
                        dto.getPlaceNum().intValue() == NumberUtils.INTEGER_ONE)) {
                    log.info("::{}::跳球头独赢封盘={}",CommonUtil.getRequestId(),JSONObject.toJSONString(dto));
                    // 独赢封盘
                    tradeVerificationService.singleWinPlayCloseMarket(matchInfo, Long.parseLong(matchPlayConfig.getPlayId()),matchConfig.getUserId());
                }
                markets = TradeVerificationService.createMarketValueNoOdds(newMarketValue, matchPlayConfig.getRcsTournamentTemplatePlayMargain(), marketOrgValue.toPlainString(),matchPlayConfig.getPlayId());
                break;
            }
        }
        if (CollectionUtils.isNotEmpty(markets)){
            for (StandardSportMarket market : markets){
                if (new BigDecimal(market.getAddition1()).abs().doubleValue() < NumberUtils.INTEGER_ONE){
                    log.info("::{}::0.5球头独赢封盘={}",CommonUtil.getRequestId(),JSONObject.toJSONString(market));
                    // 独赢封盘
                    tradeVerificationService.singleWinPlayCloseMarket(matchInfo, Long.parseLong(matchPlayConfig.getPlayId()),matchConfig.getUserId());
                }
                market.setChildMarketCategoryId(StringUtils.isNotBlank(subPlayId) ? Long.parseLong(subPlayId) : Long.parseLong(matchPlayConfig.getPlayId()));
                for (RcsStandardMarketDTO marketDTO : oddsList){
                    if (market.getPlaceNum().intValue() == marketDTO.getPlaceNum() &&
                            market.getChildMarketCategoryId().longValue() == marketDTO.getChildStandardCategoryId().longValue()){
                        marketDTO.setAddition1(market.getAddition1());
                        if (!TradeConstant.BASKETBALL_X_PLAYS.contains(marketDTO.getMarketCategoryId().intValue())){
                            marketDTO.setAddition2(marketDTO.getAddition1());
                        }
                        marketDTO.setAddition5(marketOrgValue.toPlainString());
                        marketDTO.setMarketHeadGap(new BigDecimal(matchPlayConfig.getMarketHeadGap()).doubleValue());
                        setNameExpressionValue(marketDTO,market.getAddition1());
                        break;
                    }
                }
            }
        }
    }
    /**
     * @Description   //设置投注项
     * @Param [marketDTO]
     * @Author  sean
     * @Date   2021/1/17
     * @return void
     **/
    private void setNameExpressionValue(RcsStandardMarketDTO marketDTO,String addition1) {
        for (StandardMarketOddsDTO oddsDTO : marketDTO.getMarketOddsList()){
//            if (oddsDTO.getOddsType().equalsIgnoreCase(BaseConstants.ODD_TYPE_1)){
//                oddsDTO.setNameExpressionValue(addition1);
//            }else
                if (oddsDTO.getOddsType().equalsIgnoreCase(BaseConstants.ODD_TYPE_2)){
                oddsDTO.setNameExpressionValue(new BigDecimal(addition1).multiply(new BigDecimal(NumberUtils.INTEGER_MINUS_ONE)).toPlainString());
            }else {
                    oddsDTO.setNameExpressionValue(addition1);
                }
        }
    }
    /**
     * @return void
     * @Description //设置水差和赔率
     * @Param [oddsList, matchPlayConfig, configs]
     * @Author sean
     * @Date 2020/12/2
     **/
    public void setMarketOdds(Long matchId, List<RcsStandardMarketDTO> oddsList, MatchPlayConfig matchPlayConfig, Integer matchType) {
        if (CollectionUtils.isEmpty(matchPlayConfig.getMarketList()) ||
                CollectionUtils.isEmpty(matchPlayConfig.getMarketList().get(NumberUtils.INTEGER_ZERO).getMarketOddsList())) {
            log.info("::{}::没有需要替换的赔率={}",CommonUtil.getRequestId(), JSONObject.toJSONString(matchPlayConfig));
            return;
        }
        for (RcsStandardMarketDTO dto : oddsList) {
        	dto.setMarketType(matchType);
            RcsStandardMarketDTO marketDTO = JSONObject.parseObject(JSONObject.toJSONString(dto),RcsStandardMarketDTO.class);
            List<StandardMarketOddsDTO> list = Lists.newArrayList();
            for (RcsStandardMarketDTO d : matchPlayConfig.getMarketList()) {
                if (d.getOddsReplaceOrder().intValue() == NumberUtils.INTEGER_ZERO &&
                        d.getId().equalsIgnoreCase(dto.getId())) {
                    d.setThirdMarketSourceId("");
                    list = setOddsAndMarketValue(matchId, marketDTO, d);
                    dto.setMarketOddsList(list);
                    return;
                }else if (d.getOddsReplaceOrder().intValue() == NumberUtils.INTEGER_ONE &&
                        d.getPlaceNum().intValue() == dto.getPlaceNum() && d.getChildStandardCategoryId().longValue() == dto.getChildStandardCategoryId()) {
                    d.setThirdMarketSourceId("");
                    list = setOddsAndMarketValue(matchId, marketDTO, d);
                    dto.setMarketOddsList(list);
                    return;
                }else if(d.getOddsReplaceOrder().intValue() == NumberUtils.INTEGER_TWO &&
                        new BigDecimal(d.getAddition1()).compareTo(new BigDecimal(dto.getAddition1())) == NumberUtils.INTEGER_ZERO) {
                    d.setThirdMarketSourceId("");
                    list = setOddsAndMarketValue(matchId, marketDTO, d);
                    dto.setMarketOddsList(list);
                    return;
                }
            }
        }
        log.info("::{}::setMarketOdds={}",CommonUtil.getRequestId(),JSONObject.toJSONString(oddsList));
    }

    /**
     * @return void
     * @Description //设置赔率和盘口值
     * @Param [dto, dto1]
     * @Author sean
     * @Date 2020/12/2
     **/
    private List<StandardMarketOddsDTO> setOddsAndMarketValue(Long matchId, StandardMarketDTO dto, RcsStandardMarketDTO newDto) {
        Long playId = dto.getMarketCategoryId();
        boolean isLinkage = Basketball.isLinkage(playId) && isLinkage(matchId, playId);
        List<StandardMarketOddsDTO> list = Lists.newArrayList();
        for (StandardMarketOddsDTO d : dto.getMarketOddsList()) {
            for (StandardMarketOddsDTO nd : newDto.getMarketOddsList()) {
                if (d.getOddsType().equalsIgnoreCase(nd.getOddsType())) {
                    d.setMarketDiffValue(nd.getMarketDiffValue());
                    d.setOddsValue(nd.getOddsValue());
                    d.setActive(nd.getActive());
                    if (!isLinkage) {
                        d.setOriginalOddsValue(nd.getOriginalOddsValue());
                    }
                    d.setDataSourceCode(DEFAULT_DATA_SOURCE);
                    if (!ObjectUtils.isEmpty(nd.getMargin())){
                        d.setMargin(nd.getMargin().doubleValue());
                    }
                    list.add(d);
                    break;
                }
            }
        }
        return list;
    }

    private boolean isLinkage(Long matchId, Long playId) {
        String key = String.format(RedisKey.LINKAGE_SWITCH_FLAG, matchId, playId);
        String linkageFlag = redisClient.get(key);
        return StringUtils.isNotBlank(linkageFlag);
    }

    public void setActiveLessThreeOddsType(Integer matchType, boolean isActive, List<RcsStandardMarketDTO> playAllMarketList) {
        if (CollectionUtils.isNotEmpty(playAllMarketList)) {
            final Integer matchTypeTemp = matchType;
            playAllMarketList.forEach(market -> {
                market.setMarketType(matchTypeTemp);
                List<StandardMarketOddsDTO> marketOddsList = market.getMarketOddsList();
                // 操盘手切换M模式，小于等于三项的盘口投注项激活
                if (CollectionUtils.isNotEmpty(marketOddsList) && marketOddsList.size() <= 3 && isActive) {
                    marketOddsList.forEach(marketOdds -> marketOdds.setActive(YesNoEnum.Y.getValue()));
                }
            });
        }
    }
    /**
     * @Description   //根据投注项赔率计算margin
     * @Param [bean]
     * @Author  sean
     * @Date   2021/7/8
     * @return java.lang.String
     **/
    public String getMarginFormOddsValue(RcsStandardMarketDTO bean) {
        BigDecimal margin = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(bean.getMarketOddsList())){
            margin = calculationMarginByOdds(bean.getMarketOddsList());
        }
        return margin.toPlainString();
    }

    public static BigDecimal calculationMarginByOdds(List<StandardMarketOddsDTO> oddsList) {
        BigDecimal margin = new BigDecimal(0);
        BigDecimal bigDecimal = new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE);
        for (StandardMarketOddsDTO odds : oddsList) {
            Integer fieldOddsValue = odds.getOddsValue();
            if (fieldOddsValue > 0) {
                BigDecimal oddsMargin = bigDecimal
                        .divide(new BigDecimal(fieldOddsValue).divide(new BigDecimal(BaseConstants.MULTIPLE_VALUE),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_DOWN),NumberUtils.INTEGER_TWO,BigDecimal.ROUND_HALF_DOWN);
                margin = margin.add(oddsMargin);
            }
        }
        margin = margin.setScale(0,BigDecimal.ROUND_HALF_UP);
        return margin;
    }
    /**
     * @Description   //根据水差计算赔率
     * @Param [bean, placeNumConfig, playConfig,playDiff]
     * @Author  sean
     * @Date   2021/1/10
     * @return void
     **/
    public void matchOddsMarketDiffOdds(RcsStandardMarketDTO bean, MatchMarketPlaceConfig placeNumConfig, MatchPlayConfig playConfig,Long matchId) {
        if (ObjectUtils.isEmpty(placeNumConfig) || placeNumConfig.getPlaceMarketDiff() == null) {
            log.info("::{}::玩法水差配置为空", CommonUtil.getRequestId());
            return;
        }
//        bean.setChildStandardCategoryId(Long.parseLong(placeNumConfig.getSubPlayId()));
        String oddsType = tradeVerificationService.getBasketBallUnderOddsType(bean.getMarketOddsList().get(NumberUtils.INTEGER_ZERO).getOddsType());
        tradeVerificationService.calculationOddsByWaterDiff(playConfig.getMarketType(),oddsType,bean,placeNumConfig,matchId);
    }
    /**
     * @Description   //修订赔率
     * @Param [config, marketList]
     * @Author  seanS
     * @Date   2021/8/18
     * @return void
     **/
    public void caluSpecialOddsBySpread(RcsMatchMarketConfig config, List<StandardMarketDTO> marketList) {
        RcsTournamentTemplatePlayMargain matchTemplateConfig = rcsTournamentTemplatePlayMargainMapper.queryTournamentAdjustRangeByPlayId(config);
        if (!ObjectUtils.isEmpty(matchTemplateConfig) &&
                !ObjectUtils.isEmpty(matchTemplateConfig.getIsSpecialPumping()) &&
                matchTemplateConfig.getIsSpecialPumping() == 1){
            config.setIsSpecialPumping(matchTemplateConfig.getIsSpecialPumping());
            config.setSpecialOddsInterval(matchTemplateConfig.getSpecialOddsInterval());
            for (StandardMarketDTO marketDTO : marketList){
                config.setOddsType(tradeVerificationService.getOddsType(marketDTO));
                oddsRangeService.caluSpecialOddsBySpread(marketDTO.getMarketOddsList(),config);
            }
        }
    }
}
