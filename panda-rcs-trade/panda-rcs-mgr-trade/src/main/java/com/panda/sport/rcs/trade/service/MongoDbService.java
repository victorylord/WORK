package com.panda.sport.rcs.trade.service;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.google.common.collect.Maps;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.BeanCopyUtils;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.enums.SportTypeEnum;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.mongo.MatchTeamVo;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.tourTemplate.IRcsTournamentTemplatePlayMargainService;
import com.panda.sport.rcs.vo.trade.MatchInfoReqVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.panda.sport.rcs.constants.RedisKey.*;
import static com.panda.sport.rcs.constants.RedisKey.EXPRIY_TIME_5_MINS;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : MongoDB
 * @Author : Paca
 * @Date : 2021-02-20 21:22
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Service
public class MongoDbService {
    @Autowired
    private RedisClient redisClient;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private IRcsTournamentTemplatePlayMargainService tournamentTemplatePlayMarginService;

    /**
     * 获取赛事信息
     *
     * @param matchId 赛事ID
     * @return
     */
    public MatchMarketLiveBean getMatchInfo(Long matchId,Integer liveOddBusiness) {
        Query query = new Query().addCriteria(Criteria.where("matchId").is(matchId));
        MatchMarketLiveBean match = mongoTemplate.findOne(query, MatchMarketLiveBean.class);
        if (match == null) return null;
        if (match.getSportId() == null) {
            match.setSecondsMatchStart(0);
        } else {
            Integer sportId = match.getSportId().intValue();
            Integer oddBusiness = match.getLiveOddBusiness();
            Long beginTime = DateUtils.tranferStringToDate(match.getMatchStartTime()).getTime() - System.currentTimeMillis();
            if (beginTime > 0 && null != oddBusiness && 1 == oddBusiness && match.getMatchStatus() == 0) {
                match.setSecondsMatchStart((int) (beginTime / 1000));
                match.setMatchSnapshot(1);
            } else {
                match.setMatchSnapshot(0);
                if (SportTypeEnum.FOOTBALL.getCode().equals(sportId)) {
                    Integer secondsMatchStart = match.getSecondsMatchStart();
                    Long eventTime = match.getEventTime()==null?0:match.getEventTime();
                    // Long time =eventTime > 0 ? (System.currentTimeMillis() - eventTime) / 1000: 0;
                    //当前时间-当前事件时间>10分钟 就不计算差值
                    Long time = (System.currentTimeMillis() - eventTime)>10*60*1000?0: (System.currentTimeMillis() - eventTime) / 1000;
                    Integer secondsTime = match.getSecondsMatchStart() + time.intValue();
                    log.info("::{}::赛事时间" + match.getMatchId() + "当前时间:" + DateUtils.transferLongToDateStrings(System.currentTimeMillis())
                            + "事件编码:" + match.getEventCode() + "事件时间:" + DateUtils.transferLongToDateStrings(eventTime) +
                            "比赛进行时间:" + match.getSecondsMatchStart() + "结果:" + secondsTime,CommonUtil.getRequestId(match.getMatchId()));

                    match.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);

                    if (StringUtils.isNotBlank(match.getEventCode()) && (match.getEventCode().equals("timeout"))) {
                        match.setSecondsMatchStart(secondsMatchStart);
                    }

                    if (Arrays.asList(1, 2, 10).contains(match.getMatchStatus()) && StringUtils.isNotBlank(match.getEventCode())) {
                        String key = String.format(RCS_FOOTBALL_TIME, match.getMatchId(), match.getPeriod());
                        if (StringUtils.isNotBlank(redisClient.get(key))) {
                            MatchMarketLiveBean redisMatch = JsonFormatUtils.fromJson(redisClient.get(key), MatchMarketLiveBean.class);
                            if(redisMatch!=null){
                                int redisTime = redisMatch.getSecondsMatchStart();
                                Integer redisPeriod = redisMatch.getPeriod();
                                Integer redisStatus=  redisMatch.getMatchStatus();

                                if(!match.getMatchStatus().equals(redisStatus)||!match.getPeriod().equals(redisPeriod)){
                                    redisClient.delete(String.format(RCS_FOOTBALL_TIME, match.getMatchId(), redisPeriod));
                                    MatchMarketLiveBean copeMatch = BeanCopyUtils.copyProperties(redisMatch, MatchMarketLiveBean.class);
                                    copeMatch.setSecondsMatchStart(0);
                                    redisClient.setExpiry(key, JsonFormatUtils.toJson(copeMatch), EXPRIY_TIME_5_MINS);
                                }else {
                                    if (match.getSecondsMatchStart() < redisTime) {
                                        match.setSecondsMatchStart(redisTime);
                                    }
                                    redisClient.setExpiry(key, JsonFormatUtils.toJson(match), EXPRIY_TIME_5_MINS);
                                }
                            }
                        }
                        log.info("::{}::赛事时间" + match.getMatchId() + "secondsMatchStart:" + match.getSecondsMatchStart(), CommonUtil.getRequestId(match.getMatchId()));
                    }
                } else if (SportTypeEnum.BASKETBALL.getCode().equals(sportId)) {
                    Integer secondsTime = 0;
                    Integer secondsMatchStart = match.getSecondsMatchStart();
                    Long time = (System.currentTimeMillis() - match.getEventTime()) / 1000;
                    secondsTime = secondsMatchStart - time.intValue();
                    log.info("::{}::赛事时间" + match.getMatchId() + "当前时间:" + DateUtils.transferLongToDateStrings(System.currentTimeMillis())
                            + "事件编码:" + match.getEventCode() + "事件时间:" + DateUtils.transferLongToDateStrings(match.getEventTime()) +
                            "比赛进行时间:" + match.getSecondsMatchStart() + "结果:" + secondsTime,CommonUtil.getRequestId(match.getMatchId()));
                    match.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);


                    if (StringUtils.isNotBlank(match.getEventCode()) && (match.getEventCode().equals("timeout"))) {
                        match.setSecondsMatchStart(secondsMatchStart);
                    }

                    if (Arrays.asList(1, 2, 10).contains(match.getMatchStatus()) && StringUtils.isNotBlank(match.getEventCode())) {
                        String key = String.format(RCS_BASKETBALL_TIME, match.getMatchId(), match.getPeriod());
                        if (StringUtils.isNotBlank(redisClient.get(key))) {
                            int redisTime = Integer.parseInt(redisClient.get(key));
                            if (match.getSecondsMatchStart() > redisTime && redisTime > 0) {
                                if (redisTime > 0) {
                                    match.setSecondsMatchStart(redisTime);
                                } else if (redisTime == 0) {
                                    match.setSecondsMatchStart(0);
                                }
                            }
                        }
                        redisClient.setExpiry(key, match.getSecondsMatchStart(), EXPRIY_TIME_2_HOURS);
                        log.info("::{}::赛事时间" + match.getMatchId() + "secondsMatchStart:" + match.getSecondsMatchStart(), CommonUtil.getRequestId(match.getMatchId()));
                    }
                }
            }
        }
        return match;
    }

    /**
     * 获取赛事信息
     *
     * @param matchInfoReqVo
     * @return
     */
    public MatchMarketLiveBean getMatchInfo(MatchInfoReqVo matchInfoReqVo) {
        Long matchId = matchInfoReqVo.getMatchId();
        Query query = new Query().addCriteria(Criteria.where("matchId").is(matchId));
        MatchMarketLiveBean matchBean = mongoTemplate.findOne(query, MatchMarketLiveBean.class);
        if (matchBean != null) {
            Map<Long, String> dataSourceMap = tournamentTemplatePlayMarginService.queryDataSource(matchId);
            matchBean.setDataSourceMap(dataSourceMap);
            // 判断赛前十五分钟
            Integer liveOddBusiness = matchInfoReqVo.getLiveOddBusiness();
            long beginTime = DateUtils.tranferStringToDate(matchBean.getMatchStartTime()).getTime() - System.currentTimeMillis();
            if (beginTime > 0 && NumberUtils.INTEGER_ONE.equals(liveOddBusiness) && matchBean.getMatchStatus() == 0) {
                matchBean.setSecondsMatchStart((int) (beginTime / 1000));
                matchBean.setMatchSnapshot(1);
            }
            String key = getChuZhangWarnSignKey(matchId, NumberUtils.INTEGER_ONE.equals(liveOddBusiness) ? 0 : 1);
            Map<String, String> chuZhangWarnSignMap = (Map<String, String>) redisClient.hGetAllToObj(key);
            matchBean.setChuZhangWarnSignMap(chuZhangWarnSignMap);
        }
        return matchBean;
    }

    /**
     * 获取玩法信息
     *
     * @param matchId 赛事ID
     * @param playIds 玩法ID集合，可为空
     * @param isOdds  是否过滤无赔率数据
     * @return
     */
    public List<MarketCategory> getPlayInfoList(Long matchId, Collection<Long> playIds, boolean isOdds) {
        Criteria criteria = Criteria.where("matchId").is(matchId.toString());
        if (CollectionUtils.isNotEmpty(playIds)) {
            criteria.and("id").in(playIds);
        }
        // 数据源状态开、封
//        List<Integer> sourceStatusList = Lists.newArrayList(TradeStatusEnum.OPEN.getStatus(), TradeStatusEnum.SEAL.getStatus());
//        criteria.and("matchMarketVoList.thirdMarketSourceStatus").in(sourceStatusList);
        if (isOdds) {
            criteria.and("matchMarketVoList.oddsFieldsList.id").gt(0L);
        }
        Query query = new Query().addCriteria(criteria);
        return mongoTemplate.find(query, MarketCategory.class);
    }

    /**
     * 获取球队信息
     *
     * @param matchInfo 赛事信息
     * @return
     */
    public Map<String, I18nBean> getTeamMap(MatchMarketLiveBean matchInfo) {
        Map<String, I18nBean> teamMap = Maps.newHashMap();
        if (matchInfo == null || CollectionUtils.isEmpty(matchInfo.getTeamList())) {
            teamMap.put(RcsConstant.HOME_POSITION, BeanFactory.getHomeTeam());
            teamMap.put(RcsConstant.AWAY_POSITION, BeanFactory.getAwayTeam());
            return teamMap;
        }
        for (MatchTeamVo team : matchInfo.getTeamList()) {
            String position = team.getMatchPosition();
            Map<String, String> names = team.getNames();
            if (StringUtils.isNotBlank(position) && CollectionUtils.isNotEmpty(names)) {
                teamMap.put(position.toLowerCase(), new I18nBean(names));
            }
        }
        if (!teamMap.containsKey(RcsConstant.HOME_POSITION)) {
            teamMap.put(RcsConstant.HOME_POSITION, BeanFactory.getHomeTeam());
        }
        if (!teamMap.containsKey(RcsConstant.AWAY_POSITION)) {
            teamMap.put(RcsConstant.AWAY_POSITION, BeanFactory.getAwayTeam());
        }
        return teamMap;
    }
}
