package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.RedisKey;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.SportTypeEnum;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.utils.mongopage.PageResult;
import com.panda.sport.rcs.trade.wrapper.ITOrderDetailService;
import com.panda.sport.rcs.trade.wrapper.RcsLanguageInternationService;
import com.panda.sport.rcs.trade.wrapper.StandardMatchInfoService;
import com.panda.sport.rcs.vo.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

import static com.panda.sport.rcs.constants.RedisKey.EXPRIY_TIME_2_HOURS;

/**
 * @author :  kimi
 * @Project Name :
 * @Package Name :
 * @Description :  赛事管理
 * @Date: 2019-10-30 15:52
 */
@RestController
@Slf4j
@RequestMapping("/standardMatchInfo")
public class StandardMatchInfoController {
    @Autowired
    private StandardMatchInfoService standardMatchInfoService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private ITOrderDetailService orderDetailService;
    
    @Autowired
    private RedisClient redisClient;
    
	@Autowired
    private RcsLanguageInternationService rcsLanguageInternationService;

    /**
     * @return
     * @Description //获取联赛的所有比赛
     * @Param [] 根据联赛id
     * standardTournamentId 可以为空
     * @Author kimi
     * @Date 2019/11/5
     **/
    @RequestMapping(value = "/getListByStandardTournamentId", method = RequestMethod.GET)
    public HttpResponse<List<TournamentNameVo>> getListByStandardTournamentId(Long sportId, Long standardTournamentId, Long beginTime, Long endTime, Long time) {
        try {
            if (sportId == null) {
                return HttpResponse.fail("体育种类不能为空");
            }
            if(sportId==1){
                if (beginTime == null || endTime == null) {
                    return HttpResponse.fail("时间不能为空");
                }
            }else if(sportId==2){
                if (time == null) {
                    return HttpResponse.fail("时间不能为空");
                }
            }
            //如果是篮球，换算成账务时间；如果是足球直接接收前端传过来的数据
            if(sportId == 2){
                beginTime = DateUtils.getBeginTime(time);
                endTime = DateUtils.getEndTime(time);
            }

            //获取联赛名字
            List<TournamentNameVo> tournamentNameList = standardMatchInfoMapper.selectMatchsByStandardTournamentId(sportId, standardTournamentId, beginTime, endTime);
            //获取战队名字
            List<TeamVo> teamNameList = standardMatchInfoMapper.selectTeamNameByStandardTournamentId(sportId, standardTournamentId, beginTime, endTime);
            if (tournamentNameList == null || tournamentNameList.size() == 0) {
                return HttpResponse.success(null);
            }
            for (TournamentNameVo tournamentNameVo : tournamentNameList) {
                Integer secondsMatchStart = tournamentNameVo.getSecondsMatchStart();
                if (null != tournamentNameVo.getEventTime()) {
                    Integer secondsTime = 0;
                    Long time1 = (System.currentTimeMillis() - tournamentNameVo.getEventTime()) / 1000;
                    if (SportTypeEnum.FOOTBALL.getCode().intValue()==sportId) {
                        tournamentNameVo.setEventTime(tournamentNameVo.getEventTime() > 0 ? time1 : 0);
                        secondsTime = tournamentNameVo.getSecondsMatchStart() + tournamentNameVo.getEventTime().intValue();
                    }
                    if (SportTypeEnum.BASKETBALL.getCode().intValue()==sportId) {
                        secondsTime = tournamentNameVo.getSecondsMatchStart() - time1.intValue();
                    }
                    tournamentNameVo.setSecondsMatchStart(secondsTime > 0 ? secondsTime : 0);
                }
                if (com.panda.sport.rcs.utils.StringUtils.isNotBlank(tournamentNameVo.getEventCode()) && (tournamentNameVo.getEventCode().equals("timeout"))) {
                    tournamentNameVo.setSecondsMatchStart(secondsMatchStart);
                }
                if (SportTypeEnum.BASKETBALL.getCode().intValue()==sportId && Arrays.asList(1, 2, 10).contains(tournamentNameVo.getMatchStatus())) {
                    String key = String.format(RedisKey.RCS_BASKETBALL_TIME, tournamentNameVo.getId(), tournamentNameVo.getPeriod());
                    if (com.panda.sport.rcs.utils.StringUtils.isNotBlank(redisClient.get(key))) {
                        int redisTime = Integer.parseInt(redisClient.get(key));
                        if (tournamentNameVo.getSecondsMatchStart() > redisTime) {
                            tournamentNameVo.setSecondsMatchStart(redisTime);
                        }
                        redisClient.setExpiry(key, tournamentNameVo.getSecondsMatchStart(), EXPRIY_TIME_2_HOURS);
                    }
                }
                if (SportTypeEnum.BASKETBALL.getCode().intValue()==sportId && Arrays.asList(1, 2, 10).contains(tournamentNameVo.getMatchStatus())) {
                    String key = String.format(RedisKey.RCS_BASKETBALL_TIME, tournamentNameVo.getId(), tournamentNameVo.getPeriod());
                    if (com.panda.sport.rcs.utils.StringUtils.isNotBlank(redisClient.get(key))) {
                        int redisTime = Integer.parseInt(redisClient.get(key));
                        if (tournamentNameVo.getSecondsMatchStart() > redisTime) {
                            tournamentNameVo.setSecondsMatchStart(redisTime);
                        }
                        redisClient.setExpiry(key, tournamentNameVo.getSecondsMatchStart(), EXPRIY_TIME_2_HOURS);
                    }
                }
                Integer liveOddBusiness = tournamentNameVo.getLiveOddBusiness();
                if(liveOddBusiness!=null&&liveOddBusiness==1&&tournamentNameVo.getBeginTime()<System.currentTimeMillis()){
                    tournamentNameVo.setMatchStatus(1);
                }
                if (tournamentNameVo.getT1()!=null && tournamentNameVo.getT2()!=null){
                    tournamentNameVo.setScore(tournamentNameVo.getT1()+":"+tournamentNameVo.getT2());
                }
                if (teamNameList != null && teamNameList.size() > 0) {
                    for (TeamVo teamVo : teamNameList) {
                        if (teamVo.getId().equals(tournamentNameVo.getId())) {
                            tournamentNameVo.getTeamVoArrayList().add(teamVo);
                        }
                    }
                }
            }
            return HttpResponse.success(tournamentNameList);
        } catch (Exception e) {
            log.error("::{}::获取联赛的所有比赛:{}", CommonUtil.getRequestId(standardTournamentId), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return
     * @Description //根据赛事id
     *
     * @Param [matchManageId]
     * @Author kimi
     * @Date 2019/11/5
     **/
    @RequestMapping(value = "/getByStandardTournamentId", method = RequestMethod.GET)
    public HttpResponse<TournamentNameVo> getByStandardTournamentId(String matchManageId) {
        try {
            if (matchManageId == null) {
                return HttpResponse.fail("赛事id不能为空");
            }
            //获取联赛名字
            List<TournamentNameVo> tournamentNameList = standardMatchInfoMapper.selectTournamentNameById(matchManageId);
            //获取战队名字
            List<TeamVo> teamNameList = standardMatchInfoMapper.selectTeamNameById(matchManageId);
            if (tournamentNameList == null || tournamentNameList.size() == 0) {
                return HttpResponse.success(null);
            }
            for (TournamentNameVo tournamentNameVo : tournamentNameList) {
                if (tournamentNameVo.getT1()!=null && tournamentNameVo.getT2()!=null){
                    tournamentNameVo.setScore(tournamentNameVo.getT1()+":"+tournamentNameVo.getT2());
                }
                if (teamNameList != null && teamNameList.size() > 0) {
                    for (TeamVo teamVo : teamNameList) {
                        if (teamVo.getId().equals(tournamentNameVo.getId())) {
                            tournamentNameVo.getTeamVoArrayList().add(teamVo);
                        }
                    }
                }
            }
            return HttpResponse.success(tournamentNameList);
        } catch (Exception e) {
            log.error("::{}::getByStandardTournamentId:{}", CommonUtil.getRequestId(matchManageId), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }

    /**
     * @return com.panda.sport.rcs.vo.HttpResponse
     * @Description //获取联赛以开盘的所有联赛id 和名字
     * @Param [sportId]
     * @Author kimi
     * @Date 2019/11/29
     **/
    @RequestMapping(value = "/getTournamentList", method = RequestMethod.GET)
    public HttpResponse<List<TournamentVoBySport>> getTournamentList(Long sportId, Long time, Long beginTime, Long endTime ,Integer type) {
        if (sportId == null) {
            return HttpResponse.fail("体育种类id不能为空");
        }
        if (type == null) {
            type = 0;
        }

        if(sportId==1){
            if (beginTime == null || endTime == null) {
                return HttpResponse.fail("时间不能为空");
            }
        }else if(sportId==2){
            if (time == null) {
                return HttpResponse.fail("时间不能为空");
            }
        }
        //如果是篮球；如果是足球直接接收前端传过来的数据
        if(sportId == 2){
            beginTime = time;
            endTime = time + 1000 * 60 * 60 * 24;
        }
        try {
            return HttpResponse.success(standardMatchInfoService.getTournamentList(sportId, beginTime, endTime, type));
        } catch (Exception e) {
            log.error("::{}::获取联赛以开盘的所有联赛id和名字:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }


    /***
     * 根据联赛ID查询赛事
     * @param tournamentId
     * @return
     */
    @RequestMapping(value = "/getMatchInfos", method = RequestMethod.GET)
    public HttpResponse<TournamentMatchInfoVo> getMatchInfos(Long tournamentId,String dateTime) {
        if (tournamentId == null) {
            return HttpResponse.fail("联赛id不能为空");
        }
        if (dateTime == null) {
            return HttpResponse.fail("日期不能为空");
        }
        try {
            List<TournamentMatchInfoVo> tournamentMatchInfoVos = standardMatchInfoService.selectMacthInfo(tournamentId,dateTime);
            return HttpResponse.success(tournamentMatchInfoVos);
        } catch (Exception e) {
            log.error("::{}::根据联赛ID查询赛事:{}", CommonUtil.getRequestId(tournamentId), e.getMessage(), e);
            return HttpResponse.fail("风控服务器出问题");
        }
    }
    @RequestMapping(value = "queryMatchByTournamentList")
    public HttpResponse<PageResult> queryMatchByTournamentList(@RequestBody Map<String,Object> map){
        if (CollectionUtils.isEmpty(map) ||
                ObjectUtils.isEmpty(map.get("sportId"))){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Lists.newArrayList());
        }
        try {
            List<BaseMatchInfoVo> traderMatchList = standardMatchInfoService.queryMatchsByTournamentList(map);
            return HttpResponse.success(traderMatchList);
        }catch (Exception e){
			log.error("::{}::queryMatchByTournamentList:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("滚球赛事查询失败，请稍后重试");
    }

    @RequestMapping(value = "queryBetMatchByTournamentList")
    public HttpResponse<PageResult> queryBetMatchByTournamentList(@RequestBody Map<String,Object> map, @RequestHeader(value="lang",required = false)String lang){
        if (CollectionUtils.isEmpty(map) ||
//                ObjectUtils.isEmpty(map.get("sportId")) ||
                ObjectUtils.isEmpty(map.get("list")) ||
                ObjectUtils.isEmpty(map.get("matchType"))){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Lists.newArrayList());
        }
        try {
//        	String sportId = String.valueOf(map.get("sportId"));
//        	sportId = com.panda.sport.rcs.utils.StringUtils.filtration(sportId);
//        	map.put("sportId", sportId);
        	map.put("lang",lang);
            List<BaseMatchInfoVo> traderMatchList = standardMatchInfoService.queryBetMatchsByTournamentList(map);
            return HttpResponse.success(traderMatchList);
        }catch (Exception e){
			log.error("::{}::queryBetMatchByTournamentList:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("滚球赛事查询失败，请稍后重试");
    }
    
    @RequestMapping(value = "queryManualTradeMatch")
    public HttpResponse<PageResult> queryManualTradeMatch(@RequestBody Map<String,Object> map){
        log.info("::{}::查询手动操盘的赛事入参:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(map));
        if (CollectionUtils.isEmpty(map) ||
                ObjectUtils.isEmpty(map.get("sportId")) ||
                ObjectUtils.isEmpty(map.get("tradeType"))){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Lists.newArrayList());
        }
        try {
            List<BaseMatchInfoVo> traderMatchList = standardMatchInfoService.queryManualTradeMatch(map);
            return HttpResponse.success(traderMatchList);
        }catch (Exception e){
			log.error("::{}::查询手动操盘的赛事失败:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("查询手动操盘的赛事失败，请稍后重试");
    }
    @RequestMapping(value = "queryOrderItem")
    public HttpResponse<PageResult> queryOrderItem(String orderNo){
        log.info("::{}::根据订单号查询注单列表:{}",CommonUtil.getRequestId(orderNo), orderNo);
        if (StringUtils.isEmpty(orderNo)){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Lists.newArrayList());
        }
        try {
            List<OrderItem> items = orderDetailService.queryOptionValue(orderNo);
            return HttpResponse.success(items);
        }catch (Exception e){
			log.error("::{}::根据订单号查询注单列表:{}", CommonUtil.getRequestId(orderNo), e.getMessage(), e);
        }
        return HttpResponse.fail("查询手动操盘的赛事失败，请稍后重试");
    }

    @RequestMapping(value = "queryOrderByPage")
    public HttpResponse<PageResult> queryOrderByPage(@RequestBody Map<String,Object> map, @RequestHeader(value="lang",required = false)String lang){
        log.info("::{}::根据订单号查询注单列表:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(map));
        map.put("orderStatus", 1);
        if (ObjectUtils.isEmpty(map) ||
              ObjectUtils.isEmpty(map.get("matchIds")) ||
              ObjectUtils.isEmpty(map.get("marketId")) ||
                ObjectUtils.isEmpty(map.get("playIds"))){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Maps.newHashMap());
        }
        if(StringUtils.isBlank(lang)){
            lang = "zs";
        }
        map.put("lang", "$.\""+lang+"\"");
        try {
            Map<String,Object> items = orderDetailService.queryOrderByPage(map);
            return HttpResponse.success(items);
        }catch (Exception e){
			log.error("::{}::根据订单号查询注单列表:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("根据订单号查询注单列表，请稍后重试");
    }

    @RequestMapping(value = "queryTournamentList")
    public HttpResponse<PageResult> queryTournamentList(@RequestBody Map<String,Object> map){
        if (CollectionUtils.isEmpty(map) ||
                ObjectUtils.isEmpty(map.get("matchDate")) ||
                ObjectUtils.isEmpty(map.get("sportId"))){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Lists.newArrayList());
        }
        try {
            List<BaseMatchInfoVo> traderMatchList = standardMatchInfoService.queryTournamentList(map);
            return HttpResponse.success(traderMatchList);
        }catch (Exception e){
			log.error("::{}::滚球赛事查询失败:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("查询赛事信息，请稍后重试");
    }

    @RequestMapping(value = "queryBetTournamentList")
    public HttpResponse<PageResult> queryBetTournamentList(@RequestBody Map<String,Object> map){
        if (CollectionUtils.isEmpty(map) ||
                ObjectUtils.isEmpty(map.get("matchType")) ||
                ObjectUtils.isEmpty(map.get("sportId"))){
            return new HttpResponse(HttpResponse.SUCCESS,"查询参数不足", Lists.newArrayList());
        }
        try {
        	String sportId = String.valueOf(map.get("sportId"));
        	sportId = com.panda.sport.rcs.utils.StringUtils.filtration(sportId);
        	map.put("sportId", sportId);
        	
            List<BaseMatchInfoVo> traderMatchList = standardMatchInfoService.queryBetTournamentList(map);
            List rtnList = Lists.newArrayList();
            if (!CollectionUtils.isEmpty(traderMatchList)) {
                Map<Integer, List<BaseMatchInfoVo>> list = traderMatchList.stream().collect(Collectors.groupingBy(BaseMatchInfoVo::getTournamentLevel));
                for (Map.Entry<Integer, List<BaseMatchInfoVo>> m : list.entrySet()) {
                    Map rtnMap = Maps.newHashMap();
                    rtnMap.put("tournamentLevel", m.getKey());
                    rtnMap.put("trees", m.getValue());
                    rtnList.add(rtnMap);
                }
                //根据联赛等级升序排序
                Collections.sort(rtnList,new Comparator<Map<String, Object>>() {
                    @Override
                    public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                        Integer name1 = Integer.valueOf(o1.get("tournamentLevel").toString()) ;
                        Integer name2 = Integer.valueOf(o2.get("tournamentLevel").toString()) ;
                        return name1.compareTo(name2);
                    }
                });
            }
            return HttpResponse.success(rtnList);
        }catch (Exception e){
			log.error("::{}::滚球赛事查询失败{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.fail("查询赛事信息，请稍后重试");
    }
    @RequestMapping(value = "initBetRecode")
    public HttpResponse<List<JSONObject>> initBetRecode(Long matchId) {
        if (ObjectUtils.isEmpty(matchId) || (!NumberUtils.isNumber(matchId.toString()))){
            log.info("::{}::查询30s订单参数缺失{}",CommonUtil.getRequestId(matchId),JSONObject.toJSONString(matchId));
            return HttpResponse.fail("查询30s订单参数缺失");
        }
        try {
            List<JSONObject> list = orderDetailService.initBetRecode(matchId);
           return HttpResponse.success(list);
        } catch (Exception e) {
			log.error("::{}::手动注单下单:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
        }
        return HttpResponse.success();
    }
    
    @RequestMapping(value = "dataSource/heart")
    public HttpResponse<Map<String, Object>> dataSourceHeart(Integer matchId) {
    	Map<String, Object> result = new HashMap<String, Object>();
        try {
        	Map<String, String> map = redisClient.hGetAll("rcs:heart:datasource", String.class);
        	if(map != null && map.size() > 0 ) {
        		map.entrySet().stream().forEach(entry ->{
        			Long time = Long.parseLong(entry.getValue());
        			if(System.currentTimeMillis() - time > 1000 * 20) {
        				result.put(entry.getKey(), "2");//断连
        			}else {
        				result.put(entry.getKey(), "1");//正常 
        			}
        		});
        	}
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(result);
    }
    
    @RequestMapping(value = "getCurrentAllMatch")
    public HttpResponse<IPage<StandardMatchAllSellVo>> getCurrentAllMatch(@RequestBody MarketLiveOddsQueryVo marketLiveOddsQueryVo) {
        try {
        	if(!com.panda.sport.rcs.utils.StringUtils.isBlank(marketLiveOddsQueryVo.getMatchManageId())) {
        		String managerId = marketLiveOddsQueryVo.getMatchManageId();
            	managerId = com.panda.sport.rcs.utils.StringUtils.filtration(managerId);
            	marketLiveOddsQueryVo.setMatchManageId(managerId);
        	}
        	
        	IPage<StandardMatchAllSellVo> iPage = new Page<>(marketLiveOddsQueryVo.getCurrentPage(), marketLiveOddsQueryVo.getPageSize());
            IPage<StandardMatchAllSellVo> rtnList = standardMatchInfoMapper.getAllSellMatchList(iPage, marketLiveOddsQueryVo);
            List<StandardMatchAllSellVo> list = rtnList.getRecords();
            log.info("::{}::MTS应急开关的赛事={}",CommonUtil.getRequestId(), JSON.toJSONString(list));
            //过滤掉赛事已经结束的
            list = list.stream().filter(standardMatchAllSellVo -> standardMatchAllSellVo.getMatchStatus() != 3).collect(Collectors.toList());
            if(list == null || list.size() <= 0 ) {
            	return HttpResponse.success(rtnList);
            }
            List<Long> nameCodeList = new ArrayList<Long>();
            list.stream().forEach(consumber -> {
            	if(!org.apache.commons.lang3.StringUtils.isBlank(consumber.getHomeNameCode()))
            		nameCodeList.add(Long.parseLong(consumber.getHomeNameCode()));
            	if(!org.apache.commons.lang3.StringUtils.isBlank(consumber.getAwayNameCode()))
            		nameCodeList.add(Long.parseLong(consumber.getAwayNameCode()));
            	if(!org.apache.commons.lang3.StringUtils.isBlank(consumber.getTournamentCode()))
            		nameCodeList.add(Long.parseLong(consumber.getTournamentCode()));
            });
        	
            
            Map<String, List<I18nItemVo>> langMap = rcsLanguageInternationService.getCachedNamesByCode(nameCodeList,false);
            list.stream().forEach(bean -> {
            	bean.setHomeNameList(langMap.get(String.valueOf(bean.getHomeNameCode())));
            	bean.setAwayNameList(langMap.get(String.valueOf(bean.getAwayNameCode())));
            	bean.setTournamentList(langMap.get(String.valueOf(bean.getTournamentCode())));
            });
            return HttpResponse.success(rtnList);
        } catch (Exception e) {
            log.error("::{}::getCurrentAllMatch:{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return HttpResponse.success(null);
    }
}
