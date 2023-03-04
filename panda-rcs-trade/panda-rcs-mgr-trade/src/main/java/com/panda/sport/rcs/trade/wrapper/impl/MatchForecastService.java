package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.cache.redis.RedisUtils;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.pojo.MatchPeriod;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsMatchCollection;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.profit.enums.ProfitPlayIdEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.ForecastSortUtils;
import com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo;
import com.panda.sport.rcs.vo.statistics.RcsPredictForecastVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * <p>
 * 多语言 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
@Slf4j
public class MatchForecastService {
    @Autowired
    private RcsPredictForecastMapper rcsPredictForecastMapper;
    @Autowired
    private RcsPredictBetStatisMapper rcsPredictBetStatisMapper;
    @Autowired
    private RcsMatchCollectionMapper rcsMatchCollectionMapper;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsLanguageInternationMapper rcsLanguageInternationMapper;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private MatchStatisticsInfoDetailMapper matchStatisticsInfoDetailMapper;
    private static final String AWAY = "away";
    private static final String MATCH_NAMES = "trade:forcecast:match:info:%s";

    public Map<String, Object> queryFuturesVolumeByPlayId(RcsPredictBetStatisVo vo, Integer userId) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            Long matchId = queryMatchId(vo, userId);
            vo.setMatchId(matchId);
            if (ObjectUtils.isEmpty(vo.getPlayId())) {
                vo.setPlayId(0);
            }
            if (ObjectUtils.isEmpty(vo.getPlayType())) {
                vo.setPlayType(0);
            }
            List<RcsPredictBetStatisVo> list = rcsPredictBetStatisMapper.queryBetPlayStatis(vo);
            if (CollectionUtils.isNotEmpty(list)) {
                if (ProfitPlayIdEnum.OverUnder.getCode().intValue() == vo.getPlayId() ||
                    ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue() == vo.getPlayId() ||
                    NumberUtils.INTEGER_TWO.intValue() == vo.getPlayType()) {
                    list = this.formatFuturesVolumetList(list, Boolean.FALSE);
                    list = list.stream().sorted(
                        Comparator.comparing(RcsPredictBetStatisVo::getMarketValueCurrentNumber, Comparator.reverseOrder())
                            .thenComparing(RcsPredictBetStatisVo::getPlayOptionsNumber)
                    ).collect(Collectors.toList());
                } else if (NumberUtils.INTEGER_ZERO.intValue() == vo.getISBenchmarkScore().intValue()) {
                    list = this.formatFuturesVolumetList(list, Boolean.FALSE);
                    list = list.stream().sorted(
                        Comparator.comparing(RcsPredictBetStatisVo::getMarketValueCompleteNumber, Comparator.reverseOrder())
                            .thenComparing(RcsPredictBetStatisVo::getPlayOptionsNumber)
                    ).collect(Collectors.toList());
                } else if (NumberUtils.INTEGER_ONE.intValue() == vo.getISBenchmarkScore().intValue()) {
                    list = this.formatFuturesVolumetList(list, Boolean.TRUE);
                    list = list.stream().sorted(
                        Comparator.comparing(RcsPredictBetStatisVo::getBetScoreNumber)
                            .thenComparing(RcsPredictBetStatisVo::getMarketValueCompleteNumber, Comparator.reverseOrder())
                            .thenComparing(RcsPredictBetStatisVo::getMarketValueCurrentNumber, Comparator.reverseOrder())
                            .thenComparing(RcsPredictBetStatisVo::getPlayOptionsNumber)
                    ).collect(Collectors.toList());
                }
                Map<String, Object> nameMap = getMatchMarketTeamVos(vo.getMatchId());
                for (RcsPredictBetStatisVo bet : list) {
                    if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(bet.getOddsItem()) ||
                        BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(bet.getOddsItem())) {
                        bet.setTeamNames((Map<String, String>) nameMap.get("homeName"));
                    } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(bet.getOddsItem()) ||
                        BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(bet.getOddsItem())) {
                        bet.setTeamNames((Map<String, String>) nameMap.get("awayName"));
                    }
                    if(StringUtils.isBlank(bet.getMarketValueComplete())) bet.setMarketValueComplete(bet.getMarketValueCurrent());
                    bet.setTournamentNames((Map<String, String>) nameMap.get("tournamentNames"));
                    bet.setMatchStartTime((String) map.get("matchStartTime"));
                    bet.setMarketCreatedTime(DateUtils.changeDateToString(new Date(Long.parseLong(bet.getMarketCreatedTime()))));
                }
                map.put("list", list);
            }
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            throw new RcsServiceException("服务异常，请稍后重试");
        }
        return map;
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo>
     * @Description 补全缺失投注项的数据
     * @Param [list, iSBenchmarkScore]
     * @Author Sean
     * @Date 14:08 2020/7/24
     **/
    private List<RcsPredictBetStatisVo> formatFuturesVolumetList(List<RcsPredictBetStatisVo> list, Boolean iSBenchmarkScore) {
        List<RcsPredictBetStatisVo> futuresVolumet = Lists.newArrayList();
        Map<String, List<RcsPredictBetStatisVo>> detailMap = Maps.newHashMap();
        if (iSBenchmarkScore) {
            detailMap = list.stream().collect(Collectors.groupingBy(e -> ForecastSortUtils.fetchGroupKey(e.getBetScore(),e.getMarketValueComplete(), e.getMarketValueCurrent())));
        } else {
            detailMap = list.stream().collect(Collectors.groupingBy(e -> ForecastSortUtils.fetchGroupKey(ObjectUtils.isEmpty(e.getMarketValueComplete()) ? e.getMarketValueCurrent() : e.getMarketValueComplete())));
        }
        for (Map.Entry<String, List<RcsPredictBetStatisVo>> map : detailMap.entrySet()) {
            List<RcsPredictBetStatisVo> futuresVolumetList = map.getValue();
            if (futuresVolumetList.size() == 1) {
                RcsPredictBetStatisVo vo = initFuturesVolumet(futuresVolumetList.get(0));
                futuresVolumetList.add(vo);
            }
            futuresVolumet.addAll(futuresVolumetList);
        }
        return futuresVolumet;
    }

    /**
     * @return com.panda.sport.rcs.vo.statistics.RcsPredictBetStatisVo
     * @Description 初始化一条空数据
     * @Param [vo]
     * @Author Sean
     * @Date 13:56 2020/7/24
     **/
    private RcsPredictBetStatisVo initFuturesVolumet(RcsPredictBetStatisVo vo) {
        RcsPredictBetStatisVo vo1 = JSONObject.parseObject(JSONObject.toJSONString(vo), RcsPredictBetStatisVo.class);
        vo1.setAverageOdds("0");
        vo1.setBetNum(0L);
        vo1.setBetAmount(BigDecimal.ZERO);
        vo1.setOddsSum(BigDecimal.ZERO);
        if (BaseConstants.ODD_TYPE_1.equalsIgnoreCase(vo.getOddsItem())) {
            vo1.setOddsItem(BaseConstants.ODD_TYPE_2);
        } else if (BaseConstants.ODD_TYPE_2.equalsIgnoreCase(vo.getOddsItem())) {
            vo1.setOddsItem(BaseConstants.ODD_TYPE_1);
        }
        if (BaseConstants.ODD_TYPE_OVER.equalsIgnoreCase(vo.getOddsItem())) {
            vo1.setOddsItem(BaseConstants.ODD_TYPE_UNDER);
        }
        if (BaseConstants.ODD_TYPE_UNDER.equalsIgnoreCase(vo.getOddsItem())) {
            vo1.setOddsItem(BaseConstants.ODD_TYPE_OVER);
        }
        return vo1;
    }

    private Long queryMatchId(RcsPredictBetStatisVo vo, Integer userId) {
        Long matchId = vo.getMatchId();
        if (ObjectUtils.isEmpty(matchId)) {
            RcsPredictForecastVo forecast = new RcsPredictForecastVo();
            forecast.setIsFavorite(NumberUtils.INTEGER_ONE);
            forecast.setSportId(vo.getSportId());
            List<StandardMatchInfo> matchList = getMatchs(forecast, userId);
            if (CollectionUtils.isNotEmpty(matchList)) {
                matchId = matchList.get(0).getId();
            } else {
                log.warn("::{}::用户：{}没有收藏赛事",matchId, userId);
            }
        }
        return matchId;
    }

    public Map<String, Object> queryPlayForecast(RcsPredictForecastVo vo) {
        Map<String, Object> map = Maps.newHashMap();
        try {
            QueryWrapper<RcsPredictForecastVo> wrapper = new QueryWrapper();
            wrapper.lambda().eq(RcsPredictForecastVo::getMatchId, vo.getMatchId());
            if (ObjectUtils.isNotEmpty(vo.getPlayId())) {
                wrapper.lambda().eq(RcsPredictForecastVo::getPlayId, vo.getPlayId());
            } else if (ObjectUtils.isNotEmpty(vo.getPlayType())) {
                if (NumberUtils.INTEGER_ONE.intValue() == vo.getPlayType()) {
                    wrapper.lambda().in(RcsPredictForecastVo::getPlayId, ProfitPlayIdEnum.Halftime_Handicap.getCode().intValue(), ProfitPlayIdEnum.Handicap.getCode().intValue());
                } else if (NumberUtils.INTEGER_TWO.intValue() == vo.getPlayType()) {
                    wrapper.lambda().in(RcsPredictForecastVo::getPlayId, ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue(), ProfitPlayIdEnum.OverUnder.getCode().intValue());
                }
            }
            List<RcsPredictForecastVo> list = rcsPredictForecastMapper.selectList(wrapper);
            if (ObjectUtils.isEmpty(vo.getPlayId())) {
                vo.setPlayId(0);
            }
            if (ObjectUtils.isEmpty(vo.getPlayType())) {
                vo.setPlayType(0);
            }
            if (CollectionUtils.isNotEmpty(list)) {
                Map<String, List<RcsPredictForecastVo>> detailMap = Maps.newHashMap();
                if (ProfitPlayIdEnum.Halftime_Handicap.getCode().intValue() == vo.getPlayId() ||
                    ProfitPlayIdEnum.Handicap.getCode().intValue() == vo.getPlayId() ||
                    NumberUtils.INTEGER_ONE.intValue() == vo.getPlayType()) {
                    detailMap = list.stream()
                        .collect(Collectors.groupingBy(d -> ForecastSortUtils.fetchGroupKey(d.getBetScore(), d.getMarketValueComplete(), d.getMarketValueCurrent())));
                } else if (ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue() == vo.getPlayId() ||
                    ProfitPlayIdEnum.OverUnder.getCode().intValue() == vo.getPlayId() ||
                    NumberUtils.INTEGER_TWO.intValue() == vo.getPlayType()) {
                    detailMap = list.stream()
                        .collect(Collectors.groupingBy(d -> ForecastSortUtils.fetchGroupKey(d.getBetScore(), d.getMarketValueCurrent())));
                }
                List<JSONObject> forecastList = formatRcsPredictForecastList(detailMap, vo);
                map.put("list", forecastList);
            } else {
                log.warn("::{}::该玩法:{}没有forecast数据",vo.getMatchId(), JSONObject.toJSONString(vo));
            }
        } catch (Exception e) {
            log.error("::{}::{}",CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("服务异常，请稍后重试");
        }
        return map;
    }

    public Map<String, Object> queryMatchForecast(RcsPredictForecastVo vo, Integer userId, Integer pageNumber, Integer pageSize) {
        Map<String, Object> mapList = Maps.newHashMap();
        try {
            List<StandardMatchInfo> matchList = getMatchs(vo, userId);
            Map<String, Object> map = getRcsPredictForecastVoList(vo, matchList, pageNumber, pageSize);
            List<RcsPredictForecastVo> list = (List<RcsPredictForecastVo>) map.get("list");
            if (CollectionUtils.isNotEmpty(list)) {
                Map<String, List<RcsPredictForecastVo>> detailMap = list.stream()
                    .collect(Collectors.groupingBy(d -> ForecastSortUtils.fetchGroupKey(d.getMatchId().toString())));

                mapList = this.formatMatchForecastList(detailMap, matchList, vo);
                mapList.put("total", map.get("total"));
            } else {
                log.warn("::{}::用户：{}，没有查到forecast数据，条件：{}",vo.getMatchId(), userId, JSONObject.toJSONString(vo));
            }
        } catch (Exception e) {
            log.error("::{}::{}",CommonUtil.getRequestId(),e.getMessage(), e);
            throw new RcsServiceException("服务异常，请稍后重试");
        }
        return mapList;
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description //格式化返回数据，补齐和按页面要求排序
     * @Param [detailMap, matchList]
     * @Author Sean
     * @Date 10:38 2020/7/28
     **/
    private Map<String, Object> formatMatchForecastList(Map<String, List<RcsPredictForecastVo>> detailMap, List<StandardMatchInfo> matchList, RcsPredictForecastVo vo) {
        Map<String, Object> map = Maps.newHashMap();
        // 让球list
        List<Map<String, Object>> overUnderList = Lists.newArrayList();
        // 大小球list
        List<Map<String, Object>> handicapList = Lists.newArrayList();
        // 比赛信息List
        List<Map<String, Object>> matchInfoList = Lists.newArrayList();

        for (StandardMatchInfo match : matchList) {
            List<RcsPredictForecastVo> forecastVoList = detailMap.get(match.getId().toString());
            if (CollectionUtils.isEmpty(forecastVoList)) {
                continue;
            }
            // 获取全场和半场比分
            Map<String, String> betScore = queryMatchBetScore(match);
            // 只返回对应的半场和全场数据
            RcsPredictForecastVo totalForecast = listToVO(forecastVoList, 1);
            RcsPredictForecastVo halfForecast = listToVO(forecastVoList, 2);

            //forecast列表加比分
            totalForecast.getOverUnderMap().put("betScore", betScore.get("betScore"));
            totalForecast.getHandicapMap().put("betScore", betScore.get("betScore"));
            halfForecast.getOverUnderMap().put("betScore", betScore.get("halfBetScore"));
            halfForecast.getHandicapMap().put("betScore", betScore.get("halfBetScore"));
            totalForecast.getOverUnderMap().put("standard", true);
            totalForecast.getHandicapMap().put("standard", true);
            halfForecast.getOverUnderMap().put("standard", true);
            halfForecast.getHandicapMap().put("standard", true);

            Map<String, Object> matchMap = getMatchMarketTeamVos(match.getId());
            matchMap.put("matchId", totalForecast.getMatchId());
            matchMap.put("playPhaseType", 1);
            matchMap.put("standard", true);
            matchMap.put("value", match.getMatchPeriodId());
            Map<String, Object> halfMatchMap = Maps.newHashMap();
            halfMatchMap.put("homeName", matchMap.get("homeName"));
            halfMatchMap.put("awayName", matchMap.get("awayName"));
            halfMatchMap.put("tournamentNames", matchMap.get("tournamentNames"));
            halfMatchMap.put("matchStartTime", matchMap.get("matchStartTime"));
            halfMatchMap.put("matchStatus",matchMap.get("matchStatus"));
            halfMatchMap.put("secondsMatchStart", matchMap.get("secondsMatchStart"));
            halfMatchMap.put("matchId", totalForecast.getMatchId());
            halfMatchMap.put("value", match.getMatchPeriodId());
            halfMatchMap.put("playPhaseType", 2);
            halfMatchMap.put("standard", true);
            matchMap.put("betScore", betScore.get("betScore"));
            halfMatchMap.put("betScore", betScore.get("halfBetScore"));

            if (ObjectUtils.isEmpty(vo.getPlayPhaseType())) {
                overUnderList.add(totalForecast.getOverUnderMap());
                overUnderList.add(halfForecast.getOverUnderMap());
                handicapList.add(totalForecast.getHandicapMap());
                handicapList.add(halfForecast.getHandicapMap());
                matchInfoList.add(matchMap);
                matchInfoList.add(halfMatchMap);
            } else if (NumberUtils.INTEGER_ONE.intValue() == vo.getPlayPhaseType().intValue()) {
                overUnderList.add(totalForecast.getOverUnderMap());
                handicapList.add(totalForecast.getHandicapMap());
                matchInfoList.add(matchMap);
            } else if (NumberUtils.INTEGER_TWO.intValue() == vo.getPlayPhaseType().intValue()) {
                overUnderList.add(halfForecast.getOverUnderMap());
                handicapList.add(halfForecast.getHandicapMap());
                matchInfoList.add(halfMatchMap);
            }
        }
        map.put("match", matchInfoList);
        map.put("overUnder", overUnderList);
        map.put("handicap", handicapList);
        return map;
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.String>
     * @Description //根据赛事id获取上半场和全场比分
     * @Param [match]
     * @Author Sean
     * @Date 17:18 2020/7/31
     **/
    private Map<String, String> queryMatchBetScore(StandardMatchInfo match) {
//        String betScore = "0:0";
//        String halfBetScore = "0:0";
        Map<String, String> map = Maps.newHashMap();
//        //先获取当前比分
//        MatchStatisticsInfoDetail matchStatisticsInfoDetail = matchStatisticsInfoDetailMapper.selectMatchScore(match.getId());
//        if (matchStatisticsInfoDetail!=null){
//            betScore=matchStatisticsInfoDetail.getT1()+":"+matchStatisticsInfoDetail.getT2();
////            halfBetScore=matchStatisticsInfoDetail.getT1()+":"+matchStatisticsInfoDetail.getT2();
//        }
//        //如果上半场有比分赋值进去
//        QueryWrapper<MatchPeriod> wrapper = new QueryWrapper<>();
//        wrapper.lambda().eq(MatchPeriod::getStandardMatchId, match.getId());
//        wrapper.lambda().in(MatchPeriod::getPeriod, 6);
//        wrapper.lambda().eq(MatchPeriod::getType, 1);
//        List<MatchPeriod> list = matchPeriodMapper.selectList(wrapper);
//        if (!CollectionUtils.isEmpty(list)){
//            MatchPeriod matchPeriod = list.get(0);
//            if (matchPeriod.getScore()!=null){
//                halfBetScore=matchPeriod.getScore();
//            }
//        }
//        if (ObjectUtils.isNotEmpty(match.getMatchPeriodId()) &&
//            NumberUtils.INTEGER_ZERO.intValue() == match.getMatchPeriodId()) {
//            halfBetScore = "0:0";
//        }
        String betScore = getCurrentScore(match.getId());
        String halfBetScore = getHalfScore(match.getId());
        map.put("betScore", betScore);
        map.put("halfBetScore", halfBetScore);
        return map;
    }

    /**
     * 获取上半场的比分
     * @param matchId
     * @return
     */

    public String getHalfScore(Long matchId){
        LambdaQueryWrapper<MatchStatisticsInfoDetail> lambdaQueryWrapper = new LambdaQueryWrapper<MatchStatisticsInfoDetail>();
        lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getStandardMatchId, matchId);
        lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 1);
        lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "set_score");
        lambdaQueryWrapper.last(" limit 1");
        MatchStatisticsInfoDetail result = matchStatisticsInfoDetailMapper.selectOne(lambdaQueryWrapper);
        if (result == null) {
            return "0:0";
        }
        Integer t1 = result.getT1() == null ? 0 : result.getT1();
        Integer t2 = result.getT2() == null ? 0 : result.getT2();
        log.info("::{}::矩阵查询上半场比分:{}:{}", matchId, t1, t2);
        return t1 + ":" + t2;
    }

    public String getCurrentScore(Long matchId){
        MatchStatisticsInfoDetail matchStatisticsInfoDetail = matchStatisticsInfoDetailMapper.selectScore(matchId.intValue());
        if (matchStatisticsInfoDetail==null){
            return "0:0";
        }else {
            return matchStatisticsInfoDetail.getT1()+":"+matchStatisticsInfoDetail.getT2();
        }
    }


    private List<JSONObject> formatRcsPredictForecastList(Map<String, List<RcsPredictForecastVo>> detailMap, RcsPredictForecastVo vo) {
        StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(vo.getMatchId());
        List<RcsPredictForecastVo> forecastList = Lists.newArrayList();
        List<JSONObject> forecastJsonList = Lists.newArrayList();
        for (Map.Entry<String, List<RcsPredictForecastVo>> m : detailMap.entrySet()) {
            List<RcsPredictForecastVo> forecasts = m.getValue();
            Integer playPhaseTypeGroup = 0;
            if (NumberUtils.INTEGER_ZERO.intValue() != vo.getPlayId()) {
                playPhaseTypeGroup = Integer.valueOf(vo.getPlayPhaseTypeGroup());
            }
//            if (NumberUtils.INTEGER_ONE.intValue() == vo.getPlayType()){
//                playPhaseTypeGroup = 1;
//            }else if ( NumberUtils.INTEGER_TWO.intValue() == vo.getPlayType()){
//                playPhaseTypeGroup = 2;
//            }
            RcsPredictForecastVo forecast = listToVO(forecasts, playPhaseTypeGroup);
            forecastList.add(forecast);
        }
        forecastList = forecastList.stream().sorted(
            Comparator.comparing(RcsPredictForecastVo::getBetScoreNumber)
                .thenComparing(RcsPredictForecastVo::getMarketValueCompleteNumber, Comparator.reverseOrder())
                .thenComparing(RcsPredictForecastVo::getMarketValueCurrentNumber, Comparator.reverseOrder())
                .thenComparing(RcsPredictForecastVo::getPlayPhaseTypeGroup)
        ).collect(Collectors.toList());
        for (RcsPredictForecastVo forecastVo : forecastList) {
            // 获取全场和半场比分
            StandardMatchInfo match = new StandardMatchInfo();
            match.setId(forecastVo.getMatchId());
            Map<String,String> betScore = queryMatchBetScore(match);
            Map<String, Object> map = getMatchMarketTeamVos(vo.getMatchId());

            JSONObject json = new JSONObject();
            if (ProfitPlayIdEnum.OverUnder.getCode().intValue() == vo.getPlayId() ||
                ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue() == vo.getPlayId() ||
                NumberUtils.INTEGER_TWO.intValue() == vo.getPlayType()) {
                json = JSONObject.parseObject(JSONObject.toJSONString(forecastVo.getOverUnderMap()));
            } else if (ProfitPlayIdEnum.Handicap.getCode().intValue() == vo.getPlayId() ||
                ProfitPlayIdEnum.Halftime_Handicap.getCode().intValue() == vo.getPlayId() ||
                NumberUtils.INTEGER_ONE.intValue() == vo.getPlayType()) {
                json = JSONObject.parseObject(JSONObject.toJSONString(forecastVo.getHandicapMap()));
            }
            json.put("matchStartTime", map.get("matchStartTime"));
            json.put("betScore", forecastVo.getBetScore());
            json.put("marketValueComplete", ObjectUtils.isEmpty(forecastVo.getMarketValueComplete()) ? forecastVo.getMarketValueCurrent() :forecastVo.getMarketValueComplete());
            json.put("marketValueCurrent", forecastVo.getMarketValueCurrent());
            json.put("matchPeriodId", standardMatchInfo.getMatchPeriodId());
            //全场和半场比分
            json.put("matchBetScore",betScore.get("betScore"));
            json.put("halfBetScore",betScore.get("halfBetScore"));
            forecastJsonList.add(json);
        }
        return forecastJsonList;
    }

    /**
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictForecastVo>
     * @Description 查询赛事forecast
     * @Param [vo, matchIds]
     * @Author Sean
     * @Date 10:23 2020/7/23
     **/
    private Map<String, Object> getRcsPredictForecastVoList(RcsPredictForecastVo vo, List<StandardMatchInfo> matchList, Integer pageNumber, Integer pageSize) {
        Map<String, Object> map = Maps.newHashMap();
        List<Long> matchIds = matchList.stream().map(e -> e.getId()).collect(Collectors.toList());
        List<RcsPredictForecastVo> list = Lists.newArrayList();
        Integer total = 0;
        if (CollectionUtils.isNotEmpty(matchIds)) {
            vo.setMatchIds(matchIds);
            list = rcsPredictForecastMapper.queryMatchForecast(vo, pageNumber, pageSize);
            total = rcsPredictForecastMapper.queryMatchForecastCount(vo);
        }
        map.put("total", total);
        map.put("list", list);
        return map;
    }

    /**
     * @return java.util.List<java.lang.Long>
     * @Description 查询用户收藏的或者手动收入还没有比赛结束的赛事id
     * @Param [vo, userId]
     * @Author Sean
     * @Date 10:12 2020/7/23
     **/
    private List<StandardMatchInfo> getMatchs(RcsPredictForecastVo vo, Integer userId) {
        List<StandardMatchInfo> matchInfoList = Lists.newArrayList();
        List<Long> matchIds = Lists.newArrayList();
        boolean isQuery = false;
        if (ObjectUtils.isNotEmpty(vo.getIsFavorite()) && NumberUtils.INTEGER_ONE.intValue() == vo.getIsFavorite()) {
            QueryWrapper<RcsMatchCollection> collectionQueryWrapper = new QueryWrapper<>();
            collectionQueryWrapper.lambda().eq(RcsMatchCollection::getUserId, userId);
            collectionQueryWrapper.lambda().eq(RcsMatchCollection::getSportId, vo.getSportId());
            collectionQueryWrapper.lambda().eq(RcsMatchCollection::getType, 1);
            collectionQueryWrapper.lambda().eq(RcsMatchCollection::getStatus, 1);
            List<RcsMatchCollection> matchs = rcsMatchCollectionMapper.selectList(collectionQueryWrapper);
            if (CollectionUtils.isNotEmpty(matchs)) {
                matchIds = matchs.stream().map(e -> e.getMatchId()).collect(Collectors.toList());
            }
            List<Long> longs = rcsMatchCollectionMapper.selectStandardMatchInfoId(userId, vo.getSportId());
            matchIds.addAll(longs);
        } else {
            matchIds = vo.getMatchIds();
            isQuery = true;
        }
        if (CollectionUtils.isNotEmpty(matchIds)) {
            QueryWrapper<StandardMatchInfo> matchDictQueryWrapper = new QueryWrapper<>();
            if(isQuery) {
            	matchDictQueryWrapper.lambda().in(StandardMatchInfo::getMatchManageId, JSONObject.parseObject(JSONObject.toJSONString(matchIds),new TypeReference<List<String>>() {}));
            }else {
            	matchDictQueryWrapper.lambda().ne(StandardMatchInfo::getMatchPeriodId, 999);
            	matchDictQueryWrapper.lambda().in(StandardMatchInfo::getId, matchIds);
            }
            matchDictQueryWrapper.lambda().eq(StandardMatchInfo::getSportId, 1);
            if (vo.getMatchType()!=null){
                if (vo.getMatchType()==1){
                    matchDictQueryWrapper.lambda().eq(StandardMatchInfo::getMatchPeriodId,0);
                    matchDictQueryWrapper.lambda().ne(StandardMatchInfo::getMatchStatus,3);
                    matchDictQueryWrapper.lambda().ne(StandardMatchInfo::getMatchStatus,4);
                }else {
                    matchDictQueryWrapper.lambda().ne(StandardMatchInfo::getMatchPeriodId,0);
                    matchDictQueryWrapper.lambda().ne(StandardMatchInfo::getMatchStatus,3);
                    matchDictQueryWrapper.lambda().ne(StandardMatchInfo::getMatchStatus,4);
//                    matchDictQueryWrapper.lambda().eq(StandardMatchInfo::getLiveOddBusiness,1);
                }
            }
            matchDictQueryWrapper.lambda().orderBy(Boolean.TRUE, Boolean.TRUE, StandardMatchInfo::getBeginTime, StandardMatchInfo::getId);
            matchInfoList = standardMatchInfoMapper.selectList(matchDictQueryWrapper);
        }
        return matchInfoList;
    }

    /**
     * @return com.panda.sport.rcs.vo.statistics.RcsPredictForecastVo
     * @Description // 列表转换成map方便前端展示
     * @Param [vos, playPhaseType]
     * @Author Sean
     * @Date 20:19 2020/7/31
     **/
    private RcsPredictForecastVo listToVO(List<RcsPredictForecastVo> vos, Integer playPhaseType) {
        Map<String, Object> overUnderMap = Maps.newHashMap();
        Map<String, Object> handicapMap = Maps.newHashMap();
        RcsPredictForecastVo forecast = new RcsPredictForecastVo();
        if (CollectionUtils.isNotEmpty(vos)) {
            for (RcsPredictForecastVo vo : vos) {
                if (NumberUtils.INTEGER_ONE.intValue() == playPhaseType) {
                    if (ProfitPlayIdEnum.OverUnder.getCode().intValue() == vo.getPlayId()) {
                        overUnderMap.put(vo.getForecastScore().toString(), totalProfitAmount(overUnderMap, vo));
                        overUnderMap.put("playId", vo.getPlayId());
                    } else if (ProfitPlayIdEnum.Handicap.getCode().intValue() == vo.getPlayId()) {
                        handicapMap.put(vo.getForecastScore().toString(), totalProfitAmount(handicapMap, vo));
                        handicapMap.put("playId", vo.getPlayId());
                    }
                    overUnderMap.put("matchId", vo.getMatchId());
                    overUnderMap.put("playPhaseType", 1);
                    handicapMap.put("matchId", vo.getMatchId());
                    handicapMap.put("playPhaseType", 1);
                } else if (NumberUtils.INTEGER_TWO.intValue() == playPhaseType) {
                    if (ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue() == vo.getPlayId()) {
                        overUnderMap.put(vo.getForecastScore().toString(), totalProfitAmount(overUnderMap, vo));
                        overUnderMap.put("playId", vo.getPlayId());
                    } else if (ProfitPlayIdEnum.Halftime_Handicap.getCode().intValue() == vo.getPlayId()) {
                        handicapMap.put(vo.getForecastScore().toString(), totalProfitAmount(handicapMap, vo));
                        handicapMap.put("playId", vo.getPlayId());
                    }
                    overUnderMap.put("matchId", vo.getMatchId());
                    overUnderMap.put("playPhaseType", 2);
                    handicapMap.put("matchId", vo.getMatchId());
                    handicapMap.put("playPhaseType", 2);
                } else if (ProfitPlayIdEnum.OverUnder.getCode().intValue() == vo.getPlayId() ||
                    ProfitPlayIdEnum.Halftime_OverUnder.getCode().intValue() == vo.getPlayId()) {
                    overUnderMap.put(vo.getForecastScore().toString(), totalProfitAmount(overUnderMap, vo));
                    overUnderMap.put("matchId", vo.getMatchId());
                } else if (ProfitPlayIdEnum.Handicap.getCode().intValue() == vo.getPlayId() ||
                    ProfitPlayIdEnum.Halftime_Handicap.getCode().intValue() == vo.getPlayId()) {
                    handicapMap.put(vo.getForecastScore().toString(), totalProfitAmount(handicapMap, vo));
                    handicapMap.put("matchId", vo.getMatchId());
                }
            }
            forecast.setMatchId(vos.get(0).getMatchId());
            forecast.setPlayId(vos.get(0).getPlayId());
            forecast.setBetScore(vos.get(0).getBetScore());
            forecast.setMarketValueComplete(vos.get(0).getMarketValueComplete());
            forecast.setMarketValueCurrent(vos.get(0).getMarketValueCurrent());
        }
        forecast.setOverUnderMap(overUnderMap);
        forecast.setHandicapMap(handicapMap);
        return forecast;
    }

    /**
     * @return java.lang.String
     * @Description //金额汇总
     * @Param [map, vo]
     * @Author Sean
     * @Date 20:20 2020/7/31
     **/
    private String totalProfitAmount(Map<String, Object> map, RcsPredictForecastVo vo) {
        Object profitAmount = map.get(vo.getForecastScore().toString());
        BigDecimal total = BigDecimal.ZERO;
        if (ObjectUtils.isEmpty(profitAmount)) {
            profitAmount = "0";
        }
        total = new BigDecimal(profitAmount.toString()).add(vo.getProfitAmount());
        return total.toString();
    }

    /**
     * @return java.util.Map<java.lang.String, java.lang.Object>
     * @Description //获取球队和联赛国际化信息
     * @Param [matchId]
     * @Author Sean
     * @Date 20:18 2020/7/31
     **/
    public Map<String, Object> getMatchMarketTeamVos(Long matchId) {
        Map<String, Object> nameMap = Maps.newHashMap();


        Map<String, String> neams = redisUtils.hgetAll(String.format(MATCH_NAMES,matchId));
        String match = neams.get("teams");
        String tournament = neams.get("tournament");
        List<Map<String, String>> teams = null;
        List<Map<String, String>> tournamentName = null;
        if (StringUtils.isNotBlank(match)){
            teams = (List<Map<String, String>>)JSONObject.parse(match);
            log.info("::{}::teams = {}",CommonUtil.getRequestId(),JSONObject.toJSONString(teams));
        }else {
            teams = rcsLanguageInternationMapper.queryTeamNameByMatchId(matchId);
            if (ObjectUtils.isNotEmpty(teams)){
                redisUtils.hset(String.format(MATCH_NAMES,matchId),"teams",JSONObject.toJSONString(teams));
                redisUtils.expire(String.format(MATCH_NAMES,matchId),7, TimeUnit.DAYS);
            }
        }
        if (StringUtils.isNotBlank(tournament)){
            tournamentName = (List<Map<String, String>>)JSONObject.parse(tournament);
            log.info("::{}::teams = {}", CommonUtil.getRequestId(),JSONObject.toJSONString(tournamentName));
        }else {
            tournamentName = rcsLanguageInternationMapper.queryTournamentNameByMatchId(matchId);
            if (ObjectUtils.isNotEmpty(tournamentName)){
                redisUtils.hset(String.format(MATCH_NAMES,matchId),"tournament",JSONObject.toJSONString(tournamentName));
                redisUtils.expire(String.format(MATCH_NAMES,matchId),7, TimeUnit.DAYS);
            }
        }

//        List<Map<String, String>> teams = languageInternationMapper.queryTeamNameByMatchId(matchId);
//        List<Map<String, String>> tournamentName = languageInternationMapper.queryTournamentNameByMatchId(matchId);
        Map<String, String> homeNames = Maps.newHashMap();
        Map<String, String> awayNames = Maps.newHashMap();
        Map<String, String> tournamentNames = Maps.newHashMap();
        Long matchStartTime = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(teams)) {
            Map<String, List<Map<String, String>>> teamMap = teams.stream().collect(Collectors.groupingBy(e -> e.get("matchPosition")));
            for (Map.Entry<String, List<Map<String, String>>> map : teamMap.entrySet()) {
                List<Map<String, String>> teamName = map.getValue();
                if (BaseConstants.ODD_TYPE_HOME.equalsIgnoreCase(map.getKey())) {
                    homeNames = teamName.stream().collect(Collectors.toMap(e -> e.get("languageType"), e -> e.get("text")));
                } else if (AWAY.equalsIgnoreCase(map.getKey())) {
                    awayNames = teamName.stream().collect(Collectors.toMap(e -> e.get("languageType"), e -> e.get("text")));
                }
            }
            matchStartTime = ObjectUtils.isNotEmpty(teams.get(0).get("beginTime")) ? Long.parseLong(teams.get(0).get("beginTime")) : System.currentTimeMillis();
        }
        if (CollectionUtils.isNotEmpty(tournamentName)) {
            tournamentNames = tournamentName.stream().collect(Collectors.toMap(e -> e.get("languageType"), e -> e.get("text")));
        }
        nameMap.put("matchStartTime", ObjectUtils.isEmpty(matchStartTime) ? DateUtils.changeDateToString(new Date()) : DateUtils.changeDateToString(new Date(matchStartTime)));

        nameMap.put("homeName", homeNames);
        nameMap.put("awayName", awayNames);
        nameMap.put("tournamentNames", tournamentNames);

        if(CollectionUtils.isNotEmpty(teams)){
            nameMap.put("secondsMatchStart",teams.get(0).get("secondsMatchStart"));
            nameMap.put("matchStatus",teams.get(0).get("matchStatus"));
        }
        return nameMap;
    }


    public static void main(String[] args) {
//        Map<String,String> m1 = Maps.newHashMap();
//        m1.put("languageType","en");
//        m1.put("text","a");
//        Map<String,String> m2 = Maps.newHashMap();
//        m2.put("languageType","en");
//        m2.put("text","a");
//        List<Map<String, String>> list = Lists.newArrayList(m1,m2);
//        String m = JSONObject.toJSONString(list);
//        List<Map<String, String>> l = (List<Map<String, String>>)JSONObject.parse(m);
//        System.out.println(l.size());

//        Date d = new Date(1595734655662L);
//        System.out.println(DateUtils.changeDateToString(new Date(1595734655662L)));

//        List<StandardMatchInfo> list = Lists.newArrayList();
//        list.stream().filter(e -> e.getMatchStatus() >10).map(e -> e.getId()).collect(Collectors.toList());
//        System.out.println(list.size());
//        RcsPredictBetStatisVo vo = new RcsPredictBetStatisVo();
//        vo.setPlayId(4);
//        System.out.println(ProfitPlayIdEnum.Handicap.getCode().intValue() == vo.getPlayId());
//        Query query = new Query();
//        Criteria criteria = new Criteria();
//        criteria.and("matchId").is(1);
//        query.addCriteria(criteria);
//        query.fields().include("teamList");
//        query.toString();
    }
}
