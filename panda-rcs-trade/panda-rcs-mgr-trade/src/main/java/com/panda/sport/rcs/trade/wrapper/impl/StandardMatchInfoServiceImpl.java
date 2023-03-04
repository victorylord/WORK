package com.panda.sport.rcs.trade.wrapper.impl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.MatchConstants;
import com.panda.sport.rcs.core.db.annotation.Master;
import com.panda.sport.rcs.mapper.RcsMatchOrderAcceptConfigMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.pojo.RcsTradeConfig;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.cache.RcsCacheContant;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.RcsTradeConfigService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.trade.wrapper.StandardSportTeamService;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsMatchDimensionStatisticsService;
import com.panda.sport.rcs.vo.*;
import com.panda.sport.rcs.vo.statistics.RcsMatchDimensionStatisticsVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Service
@Slf4j
public class StandardMatchInfoServiceImpl extends ServiceImpl<StandardMatchInfoMapper, StandardMatchInfo> implements StandardMatchInfoService {

    @Autowired
    StandardMatchInfoMapper standardMatchInfoMapper;

    @Autowired
    StandardSportTeamService standardSportTeamService;

	@Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    @Autowired
    private RcsMatchDimensionStatisticsService matchDimensionStatisticsService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    RcsMatchOrderAcceptConfigMapper rcsMatchOrderAcceptConfigMapper;

    @Autowired
    private RcsTradeConfigService rcsTradeConfigService;
    final static String VS = " VS ";

    /**
     * @MethodName: getOtherEarlyTime
     * @Description: 得到其它早盘开始时间和结束时间
     * @Param:
     * @Return:
     * @Author: Vector
     * @Date: 2019/11/5
     **/
    public static Long[] getOtherEarlyTime() {
        Calendar instance = Calendar.getInstance();
        instance.setTime(new Date());
        if (instance.get(Calendar.HOUR_OF_DAY) < 12) {
            instance.add(Calendar.DATE, 6);
        } else {
            instance.add(Calendar.DATE, 7);
        }
        instance.set(Calendar.HOUR_OF_DAY, 12);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);
        long time1 = instance.getTime().getTime();
        instance.add(Calendar.DATE, 7);
        instance.add(Calendar.MILLISECOND, -1);
        long time2 = instance.getTime().getTime();
        return new Long[]{time1, time2};
    }

    public void initParams(MarketLiveOddsQueryVo marketLiveOddsQueryVo){
        if (marketLiveOddsQueryVo.getBeginTime() != null) {
            long endTimeLong = DateUtils.addNDay(marketLiveOddsQueryVo.getBeginTime(), 1).getTime();
            marketLiveOddsQueryVo.setBeginTimeMillis(marketLiveOddsQueryVo.getBeginTime().getTime());
            marketLiveOddsQueryVo.setEndTimeMillis(endTimeLong - 1L);
        }
        marketLiveOddsQueryVo.setCurrentTimeMillis(System.currentTimeMillis());
        //其它早盘
        if (marketLiveOddsQueryVo.getIsOtherEarly() != null && marketLiveOddsQueryVo.getIsOtherEarly().longValue() == 1) {
            Long[] otherEarlyTime = getOtherEarlyTime();
            marketLiveOddsQueryVo.setBeginTimeMillis(otherEarlyTime[0]);
            marketLiveOddsQueryVo.setEndTimeMillis(otherEarlyTime[1]);
        }
    }

    /**
     * 组合条件查询数据库赛事数据
     *
     * @param marketLiveOddsQueryVo
     * @return
     */
    @Override
    @Master
    public List<StandardMatchInfo> queryMatches(MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        initParams(marketLiveOddsQueryVo);

        List<StandardMatchInfo> list = standardMatchInfoMapper.selectPageByCondition(marketLiveOddsQueryVo);
        return list;
    }

    @Override
    public List<Long> selectByMap(Map<String, Object> map) {
        MarketLiveOddsQueryVo marketLiveOddsQueryVo = new MarketLiveOddsQueryVo();
        if (!Strings.isNullOrEmpty(marketLiveOddsQueryVo.getMatchDate())) {
            Date matchDate = DateUtils.dateStrToDate(marketLiveOddsQueryVo.getMatchDate() + " 12:00:00");
            marketLiveOddsQueryVo.setBeginTime(matchDate);
        }
        marketLiveOddsQueryVo.setOperateMatchStatus(1);
        if (map.get("liveOddBusiness") != null) {
            marketLiveOddsQueryVo.setLiveOddBusiness(Integer.parseInt(map.get("liveOddBusiness").toString()));
        }
        if (map.get("matchManageId") != null) {
            marketLiveOddsQueryVo.setMatchId(Long.parseLong(map.get("matchManageId").toString()));
        }
        if (map.get("tournamentLevel") != null) {
            marketLiveOddsQueryVo.setTournamentLevel(Integer.parseInt(map.get("tournamentLevel").toString()));
        }
        if (map.get("sportId") != null) {
            marketLiveOddsQueryVo.setMatchId(Long.parseLong(map.get("sportId").toString()));
        }
        List<StandardMatchInfo> standardMatchInfos = queryMatches(marketLiveOddsQueryVo);
        ArrayList<Long> longs = new ArrayList<>();
        for (StandardMatchInfo standardMatchInfo : standardMatchInfos) {
            longs.add(standardMatchInfo.getId());
        }
        return longs;
    }

    @Override
    public List<TournamentMatchInfoVo> selectMacthInfo(long tournamentId,String dateTime) {
        List<TournamentMatchInfoVo> tournamentMatchInfoVos = standardMatchInfoMapper.selectMacthInfo(tournamentId,dateTime);
        int i = 1;
        List<Integer> matchIdList=new ArrayList<>();
        for (TournamentMatchInfoVo model: tournamentMatchInfoVos) {
            model.setSortId(i++);
            model.setTotalValue(model.getTotalValue()/100);
            model.setSettledProfitValue(model.getSettledProfitValue()/100);
            model.setSettledRealTimeValue(model.getSettledRealTimeValue()/100);
            model.setScore(Arrays.asList(1,2,10).contains(model.getMatchStatus())?model.getScore():null);
            List<SportTeam> sportTeams = standardSportTeamService.queryTeamsByMatchId(model.getId());
            if (!CollectionUtils.isEmpty(sportTeams)) {
                List<MatchMarketLiveOddsVo.MatchMarketTeamVo> teamList = Lists.newArrayListWithCapacity(sportTeams.size());
                for (SportTeam sourceTeam : sportTeams) {
                    MatchMarketLiveOddsVo.MatchMarketTeamVo team = new MatchMarketLiveOddsVo.MatchMarketTeamVo();
                    team.setMatchPosition(sourceTeam.getMatchPosition());
                    team.setNameCode(sourceTeam.getNameCode());
                    // 国际化
                    team.setNames(rcsLanguageInternationService.getCachedNamesMapByCode(sourceTeam.getNameCode()));
                    teamList.add(team);
                }
                model.setTeamList(teamList);
            }
            Long[] matchIds= {model.getId()};
            Long startTime = System.currentTimeMillis()-3600000;
            //近一小时货量
            List<RcsMatchDimensionStatisticsVo> rcsMatchDimensionStatisticsVos = matchDimensionStatisticsService.searchNearlyOneHourRealTimeValue(matchIds,startTime);
            if (!CollectionUtils.isEmpty(rcsMatchDimensionStatisticsVos)) {
                model.setTotalValueOneHour(rcsMatchDimensionStatisticsVos.get(0).getRealTimeValue());
            }
            matchIdList.add(model.getId().intValue());
        }

        if (!CollectionUtils.isEmpty(matchIdList)){
            HashMap<Integer, RcsTradeConfig> rcsTradeConfigStatusByMatchId = rcsTradeConfigService.getRcsTradeConfigStatusByMatchId(matchIdList);
            if (!CollectionUtils.isEmpty(tournamentMatchInfoVos)){
                for (TournamentMatchInfoVo tournamentMatchInfoVo:tournamentMatchInfoVos){
                    int id = tournamentMatchInfoVo.getId().intValue();
                    RcsTradeConfig rcsTradeConfig = rcsTradeConfigStatusByMatchId.get(id);
                    if (rcsTradeConfig==null){
                        tournamentMatchInfoVo.setOperateMatchStatus(0);
                    }else {
                        tournamentMatchInfoVo.setOperateMatchStatus(rcsTradeConfig.getStatus());
                    }
                }
            }
        }
        return tournamentMatchInfoVos;
    }



    @Override
    public List<TournamentVoBySport> getTournamentList(Long sportId, Long beginTime, Long endTime,Integer type) {
        return standardMatchInfoMapper.getTournamentList(sportId, beginTime, endTime, type);
    }


    @Override
    public List<TeamVo> selectTeamNameByMatchId(Long matchId) {
        return standardMatchInfoMapper.selectTeamNameByMatchId(matchId);
    }

    @Override
    public Integer getGrounderNumber() {
        return standardMatchInfoMapper.getGrounderNumber();
    }

    @Override
    public List<BaseMatchInfoVo> queryMatchsByTournamentList(Map<String, Object> map) {
        Date matchStartDate = getDate(map);
        Query query = new Query();
        Criteria criteria = Criteria.where("sportId").is(map.get("sportId"));
        if (!ObjectUtils.isEmpty(map.get("list"))){
            List<Integer> list = (List<Integer>)map.get("list");
            criteria.and("standardTournamentId").in(list);
        }
        if(!ObjectUtils.isEmpty(map.get("matchDate"))){
            if (NumberUtils.INTEGER_ZERO.intValue() == Integer.parseInt(map.get("matchDate").toString())){
                criteria.and("matchStartTime")
                        .gte(DateUtils.transferLongToDateStrings(matchStartDate.getTime()))
                        .lt(DateUtils.transferLongToDateStrings(DateUtils.addNDay(matchStartDate,2).getTime()));
            }
            if (NumberUtils.INTEGER_ONE.intValue() == Integer.parseInt(map.get("matchDate").toString())){
                criteria.and("matchStartTime").gte(DateUtils.transferLongToDateStrings(DateUtils.addNDay(matchStartDate,2).getTime()));
            }
            if (NumberUtils.LONG_MINUS_ONE.intValue() == Integer.parseInt(map.get("matchDate").toString())){
                criteria.and("matchStartTime").gte(DateUtils.transferLongToDateStrings(matchStartDate.getTime()));
            }
        }
        query.addCriteria(criteria);
        List<BaseMatchInfoVo> list = mongoTemplate.find(query, BaseMatchInfoVo.class,"match_market_live");
        return list;
    }

    @Override
    public List<BaseMatchInfoVo> queryManualTradeMatch(Map<String, Object> map) {
        getQueryMap(map);
        log.info("::{}::查询手动抄盘sql参数{}", CommonUtil.getRequestId(), JSONObject.toJSONString(map));
        List<BaseMatchInfoVo> list = rcsMatchOrderAcceptConfigMapper.queryManualTradeMatch(map);
        List<BaseMatchInfoVo> matchs = new ArrayList<>();
        if (list.size() == 0 ){
            return matchs;
        }
        BaseMatchInfoVo match = null;
        Map<Integer,BaseMatchInfoVo> mapMatch = new HashMap<>();
        String home = "";
        String away = "";
        StringBuffer buffer = null;
        for (BaseMatchInfoVo vo : list){
            match = mapMatch.get(vo.getMatchId());
            if(ObjectUtils.isEmpty(match)){
                mapMatch.put(vo.getMatchId(),vo);
            }else {
                if (match.getMatchPosition().equalsIgnoreCase(MatchConstants.HOME)){
                    home = match.getMatchInfo();
                    away = vo.getMatchInfo();
                }else if(vo.getMatchPosition().equalsIgnoreCase(MatchConstants.HOME)){
                    home = vo.getMatchInfo();
                    away = match.getMatchInfo();
                }else {
                    continue;
                }
                buffer = new StringBuffer();
                buffer.append(home).append(VS).append(away);
                match.setMatchInfo(buffer.toString());
                matchs.add(match);
            }
        }
        return matchs;
    }

    private Date getDate(Map<String, Object> map) {
        Date date = new Date();
        String dateStr = DateUtils.DateToString(date);
        Date matchDate = DateUtils.dateStrToDate(dateStr + " 12:00:00");
        Date matchStartDate = null;
        if (date.getTime() > matchDate.getTime()) {
            matchStartDate = matchDate;
        } else {
            matchStartDate = DateUtils.addNDay(matchDate, -1);
        }
        //滚球今日和全部统计赛事，往前移一天
        if (!ObjectUtils.isEmpty(map.get("matchDate"))) {
            if (NumberUtils.INTEGER_ZERO.intValue() == Integer.parseInt(map.get("matchDate").toString())) {
                matchStartDate = DateUtils.addNDay(matchStartDate, -1);
            }
            if (NumberUtils.LONG_MINUS_ONE.intValue() == Integer.parseInt(map.get("matchDate").toString())) {
                matchStartDate = DateUtils.addNDay(matchStartDate, -1);
            }
        }
        return matchStartDate;
    }

    @Override
    public StandardMatchInfo selectById(Long id) {
        return standardMatchInfoMapper.selectById(id);
    }

    @Override
    public List<BaseMatchInfoVo> queryTournamentList(Map<String, Object> map) {
        getQueryMap(map);
        List<BaseMatchInfoVo> list = standardMatchInfoMapper.queryTournamentList(map);
        return list;
    }

    @Override
    public List<BaseMatchInfoVo> queryBetTournamentList(Map<String, Object> map) {
        List<BaseMatchInfoVo> list = standardMatchInfoMapper.queryTournamentList(map);
        if(!CollectionUtils.isEmpty(list)){
            List<Long> codes = list.stream().map(e -> e.getNameCode()).collect(Collectors.toList());
            Map<String, List<I18nItemVo>> language = rcsLanguageInternationService.getCachedNamesByCode(codes);
            for (BaseMatchInfoVo obj : list ) {
                if(!CollectionUtils.isEmpty(language.get(obj.getNameCode().toString()))){
                    obj.setTournamentNames(language.get(obj.getNameCode().toString()));
                }
            }
        }
        return list;
    }

    @Override
    public Map<Long, Integer> queryOddLiveMap(List<Long> matchIds) {
        if(CollectionUtils.isEmpty(matchIds)) return null;
        QueryWrapper<StandardMatchInfo> wrapper = new QueryWrapper<>();
        wrapper.lambda().in(StandardMatchInfo::getId,matchIds);
        List<StandardMatchInfo> standardMatchInfos = baseMapper.selectList(wrapper);
        Map<Long, Integer> oddLiveMap =null;
        if(!CollectionUtils.isEmpty(standardMatchInfos)){
            oddLiveMap = standardMatchInfos.stream().collect(Collectors.toMap(StandardMatchInfo::getId, StandardMatchInfo::getOddsLive));
        }
        return oddLiveMap;
    }

    private void getQueryMap(Map<String, Object> map) {
        Date matchStartDate = getDate(map);
        if (!ObjectUtils.isEmpty(map.get("matchDate"))) {
            if (NumberUtils.INTEGER_ZERO.intValue() == Integer.parseInt(map.get("matchDate").toString())) {
                map.put("startDate", matchStartDate.getTime());
                map.put("endDate", DateUtils.addNDay(matchStartDate, 2).getTime());
            }
            if (NumberUtils.INTEGER_ONE.intValue() == Integer.parseInt(map.get("matchDate").toString())) {
                map.put("startDate", DateUtils.addNDay(matchStartDate, 2).getTime());
            }
            if (NumberUtils.LONG_MINUS_ONE.intValue() == Integer.parseInt(map.get("matchDate").toString())) {
                map.put("startDate", matchStartDate.getTime());
            }
        }
    }

    @Override
    public List<BaseMatchInfoVo> queryBetMatchsByTournamentList(Map<String, Object> map) {
        Query query = new Query();
        Criteria criteria = new Criteria();
        Long time = System.currentTimeMillis();
        List<Integer> tournamentIds = (List<Integer>) map.get("list");
        criteria.and("standardTournamentId").in(tournamentIds);
        /*
            1、matchType = 0 查询全部 0,1,2,10或者空
            1、matchType = 1 查询早盘 0或者空
            1、matchType = 2 查询滚球 1,2,10
         */
        if (NumberUtils.INTEGER_ZERO.intValue() == Integer.valueOf(map.get("matchType").toString())) {
            criteria.and("matchStatus").in(Arrays.asList(0, 1, 2, 7, 10));
        }
        if (NumberUtils.INTEGER_ONE.intValue() == Integer.valueOf(map.get("matchType").toString())) {
            criteria.and("matchStatus").is(0);
            criteria.and("preMatchBusiness").is(1);
        }
        if (NumberUtils.INTEGER_TWO.intValue() == Integer.valueOf(map.get("matchType").toString())) {
            criteria.and("matchStatus").in(Arrays.asList(1, 2, 10));
            criteria.and("liveOddBusiness").is(1);
        }
        criteria.orOperator(
                new Criteria().and("preMatchBusiness").is(1).and("matchStatus").nin(Arrays.asList(1,2,3,4,10)).and("matchStartTime").gt(DateUtils.transferLongToDateStrings(time))
                ,new Criteria().and("sportId").in(Arrays.asList(1L, 2L, 8L, 10L)).and("liveOddBusiness").is(1).and("matchStatus").in(Arrays.asList(1, 2, 10)).and("matchStartTime").gt(DateUtils.transferLongToDateStrings(time-4 * 60 * 60 * 1000))
                ,new Criteria().and("sportId").is(7L).and("liveOddBusiness").is(1).and("matchStatus").in(Arrays.asList(1, 2, 10)).and("matchStartTime").gt(DateUtils.transferLongToDateStrings(time-7 * 24 * 60 * 60 * 1000))
                ,new Criteria().and("sportId").nin(1L, 2L,7L, 8L, 10L).and("liveOddBusiness").is(1).and("matchStatus").in(Arrays.asList(1, 2, 10)).and("matchStartTime").gt(DateUtils.transferLongToDateStrings(time- 24 * 60 * 60 * 1000))
        );
        query.addCriteria(criteria);
        List<BaseMatchInfoVo> list = mongoTemplate.find(query, BaseMatchInfoVo.class, "match_market_live");
        list.stream().forEach(m->{
            String home = "";
            String away = "";
            for(MatchMarketLiveOddsVo.MatchMarketTeamVo t :m.getTeamList()){
                if(t.getMatchPosition().equalsIgnoreCase("home")){
                    if("en".equalsIgnoreCase(map.get("lang").toString())){
                        home = t.getNames().get("en");
                    }else{
                        home = t.getNames().get("zs");
                    }

                }
                if(t.getMatchPosition().equalsIgnoreCase("away")){
                    if("en".equalsIgnoreCase(map.get("lang").toString())){
                        away = t.getNames().get("en");
                    }else{
                        away = t.getNames().get("zs");
                    }

                }
            }
            m.setMatchInfo(home +" VS "+ away);
        });

        return list;
    }

    @Override
    public Integer selectMatchStatusById(Long id) {
        return standardMatchInfoMapper.selectMatchStatusById(id);
    }

    @Override
    public String getMatchDateExpect(Long matchId) {
        return RcsCacheContant.MATCH_DATE_EXPECT_CACHE.get(matchId, id -> {
            StandardMatchInfo matchInfo = this.getById(matchId);
            Long beginTime;
            if (matchInfo != null) {
                beginTime = matchInfo.getBeginTime();
            } else {
                beginTime = System.currentTimeMillis();
            }
            return DateUtils.getDateExpect(beginTime);
        });
    }

}
