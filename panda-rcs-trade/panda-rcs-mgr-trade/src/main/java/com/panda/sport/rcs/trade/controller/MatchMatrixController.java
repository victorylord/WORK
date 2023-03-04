package com.panda.sport.rcs.trade.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.panda.sport.rcs.trade.util.CommonUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.rpc.RpcException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Maps;
import com.panda.sport.data.rcs.dto.matrix.MatrixBean;
import com.panda.sport.manager.api.util.Md5;
import com.panda.sport.rcs.common.DateUtils;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.core.utils.JsonFormatUtils;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.matrix.ForecastInfoVo;
import com.panda.sport.rcs.matrix.ForecastReqVo;
import com.panda.sport.rcs.matrix.MatrixMatchInfoVo;
import com.panda.sport.rcs.matrix.MatrixReqVo;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.mongo.MatchMarketLiveBean;
import com.panda.sport.rcs.mongo.MatchTeamVo;
import com.panda.sport.rcs.mongo.PredictForecastVo;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.utils.mongopage.PageResult;
import com.panda.sport.rcs.trade.vo.MatchMatrixDatasVo;
import com.panda.sport.rcs.trade.vo.MatchMatrixVo;
import com.panda.sport.rcs.trade.vo.MatchMatrixsVo;
import com.panda.sport.rcs.trade.wrapper.*;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.*;
import com.panda.sports.api.ISystemUserOrgAuthApi;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.bootstrap.controller
 * @Description :  赛事比分矩阵
 * @Date: 2019-11-18 20:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@RestController
@RequestMapping(value = "matchMatrix")
@Slf4j
public class MatchMatrixController {

    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private MarketCategorySetService marketCategorySetService;
    @Autowired
    private RedisClient redisClient;
    /**
     * 矩阵查询
     */
    @Autowired
    private ITOrderDetailService orderDetailService;
    @Autowired
    private RcsStandardSportMarketSellMapper rcsStandardSportMarketSellMapper;
    @Autowired
    private StandardSportMarketCategoryMapper standardSportMarketCategoryMapper;
    @Autowired
    private RcsCodeMapper rcsCodeMapper;
    @Autowired
    MatchStatisticsInfoDetailMapper matchStatisticsInfoDetailMapper;
    @Autowired
    private RcsTradeConfigService tradeConfigService;
    @Autowired
    private MatchEventInfoService eventInfoService;
    
    @Autowired
    private SportMatchViewService sportMatchViewService;

    @Autowired
    private RcsLanguageInternationService rcsLnguageInternationService;


    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    ISystemUserOrgAuthApi systemUserOrgAuthApi;
    private MatrixBean[][] getMatrixBeans(MatrixVo[][] vos) {
        MatrixBean[][] beans = new MatrixBean[vos.length][vos[0].length];
        for (int i = 0; i < vos.length; i++) {
            for (int j = 0; j < vos[i].length; j++) {
                if (vos[i][j] != null) {
                    MatrixBean bean = new MatrixBean();
                    bean.setValue(vos[i][j].getValue().setScale(0, BigDecimal.ROUND_HALF_UP).longValue());
                    bean.setLevel(vos[i][j].getLevel());
                    bean.setIsOutcome(vos[i][j].getIsOutcome());
                    beans[i][j] = bean;
                }
            }
        }
        return beans;
    }


    /**
//     * @param item       参数（matchId 赛事ID，isSettlement：1 未结算 2 已计算，matchType： 1 早盘 2 滚球盘）
//     * @param matchStage 赛事阶段  1:比分全场矩阵；2:比分上半场矩阵
//     * @param startTime  开始时间
//     * @param endTime    结束时间
//     * @param tenantIds  商户ID
//     * @param playIds    playId 玩法ID，
//     * @param unit       单位 1 10 100 1000
//     * @param size       矩阵大小 5*5 / 6*6
     * @return 赛事矩阵
     */
    @RequestMapping(value = "/get")
    public HttpResponse getMatrixByMatch(@RequestBody MatchMatrixVo vo) throws RpcException {

        //临时做一个锁，用于这个接口 同时只允许一个请求
        String onlyOneStatus = "rcs:matchMatrix:onlyOne:status";
        String onlyOneRun = "rcs:matchMatrix:onlyOne:run";
        if (StringUtils.isNotBlank(redisClient.get(onlyOneStatus)) ) {
            if(!redisClient.setNX(onlyOneRun, "1", 60)){
				log.error("::{}::请求限流,当前有赛事在处理中", CommonUtil.getRequestId());
                return HttpResponse.fail("请求限流,查询过于频繁" );
            }
        }


        String matchId = vo.getMatchId().toString();
        //请求 赛事总数量key
        String matchTotalKey = "rcs:matchMatrix:total:num:";
        //请求 单赛事数量key
        String matchCurrentKey = "rcs:matchMatrix:current:num:";

        int matchMaxNum =12;
        String matchMaxKey = "rcs:matchMatrix:max:num";
        if (StringUtils.isNotBlank(redisClient.get(matchMaxKey))) {
            matchMaxNum = Integer.valueOf(redisClient.get(matchMaxKey));
        }

        int matchSingleMaxNum =3;
        String matchSingleMaxKey = "rcs:matchSingleMatrix:max:num";
        if (StringUtils.isNotBlank(redisClient.get(matchSingleMaxKey))) {
            matchSingleMaxNum = Integer.valueOf(redisClient.get(matchSingleMaxKey));
        }

        redisClient.hSet(matchTotalKey, matchId, "1");
        redisClient.hincrBy(matchCurrentKey, matchId, 1L);
        //保险起见,设置过期时间 避免极端情况,出现重启导致key没删除,占用数量(常规来说,一个查询过程不会超过60s)
        redisClient.expireKey(matchTotalKey, 60);
        redisClient.expireKey(matchCurrentKey, 60);
        try {
            //判断赛事总数量 是否超过12
            Map<String, String> map = redisClient.hGetAll(matchTotalKey, String.class);
            log.info("::{}::矩阵限流,赛事总数{}-{}--->{}", CommonUtil.getRequestId(), matchMaxNum, matchSingleMaxNum, map);
            if (null != map && map.size() > matchMaxNum) {
				log.error("::{}::请求限流,赛事同时总查询 超出数量", CommonUtil.getRequestId());
                return HttpResponse.fail("请求限流,赛事同时总查询 超出数量"+matchMaxNum);
            }
            //判断单个赛事 查询总数量是否大于3
            Long currentNum = redisClient.hincrBy(matchCurrentKey, matchId, 0L);
            log.info("::{}::矩阵限流,单赛事数量{}",CommonUtil.getRequestId(), currentNum);
            if (currentNum >= matchSingleMaxNum) {
				log.error("::{}::请求限流,单赛事 同时查询 超出数量", CommonUtil.getRequestId());
                return HttpResponse.fail("请求限流,单赛事 同时查询 超出数量"+matchSingleMaxNum);
            }


            return HttpResponse.success(getMatrixByMatchData(vo));
        } catch (Exception ex) {
			log.error("::{}::调用矩阵接口异常,请求参数{}", CommonUtil.getRequestId(),JsonFormatUtils.toJson(vo));
			log.error("::{}::调用矩阵接口异常,错误信息{}", CommonUtil.getRequestId(),ex.getMessage());

            return HttpResponse.fail(ex);
        }finally {
            redisClient.hashRemove(matchTotalKey, matchId);
            redisClient.hincrBy(matchCurrentKey, matchId, -1L);
            redisClient.delete(onlyOneRun);
            log.info("::{}::矩阵限流,redis处理完毕{},{},{}",CommonUtil.getRequestId(), vo.getMatchId(), vo.getMatchType(), getCacheKey(vo));
        }
    }

    @RequestMapping(value = "/getMatrixInfo")
    public HttpResponse getMatrixInfo(@RequestBody RcsMatrixInfoReqVo vo) throws RpcException {
        try {
            return HttpResponse.success(getMatrixInfoData(vo));
        } catch (Exception ex) {
			log.error("::{}::调用矩阵查询接口异常,请求参数{}", CommonUtil.getRequestId(),JsonFormatUtils.toJson(vo));
			log.error("::{}::调用矩阵查询接口异常,错误信息{}", CommonUtil.getRequestId(),ex.getMessage());
            return HttpResponse.fail(ex);
        }
    }

    /**
     * 查询条件缓存key
     * @param vo
     * @return
     */
    private String getCacheKey(MatchMatrixVo vo){
        Long endTime = vo.getEndTime();
        vo.setEndTime(0L);
        String str = JSONObject.toJSONString(vo);
        String md5 = Md5.md5(str);
        vo.setEndTime(endTime);
        return md5;
    }


    public  Map<String, Object> getMatrixByMatchData(@RequestBody MatchMatrixVo vo) throws RpcException {
        Map<String, Object> dataMap = new HashMap<>();
        String cacheKey = getCacheKey(vo);
        String cacheData = redisClient.get(cacheKey);
        //先从缓存取值
        if (StringUtils.isNotBlank(cacheData)) {
            JSONObject json = JSONObject.parseObject(cacheData);
            String score = json.getString("score");
            MatrixBean[][] matrixBeans = JSON.parseObject(json.getString("list"), MatrixBean[][].class);
            dataMap.put("list", matrixBeans);
            dataMap.put("score", score);
            log.info("::{}::矩阵限流缓存生效{},{},{}",CommonUtil.getRequestId(), vo.getMatchId(), vo.getMatchType(), cacheKey);
            return dataMap;
        }


        MatrixVo[][] reusult = null;
        String score = "";

        //如果前端没传玩法，则返回0
        if(vo.getPlayIds().size()==0){
            List<Long> plays = new ArrayList<>();
            plays.add(-1L);
            vo.setPlayIds(plays);
        }

        if (vo.getMatchStage() == 2) {
            TOrderDetail item = new TOrderDetail();
            item.setMatchId(vo.getMatchId());
            item.setIsSettlement(vo.getIsSettlement());
            if (vo.getMatchType() != null) {
                item.setMatchType(vo.getMatchType());
            }
            Date startTime = null;
            Date endTime = null;
            if (vo.getStartTime() != null) {
                startTime = DateUtils.transferLongToDate(vo.getStartTime());
            }
            if (vo.getEndTime() != null) {
                endTime = DateUtils.transferLongToDate(vo.getEndTime());
            }
            reusult = orderDetailService.getHalfMatrixByMatchId(item,
                startTime,
                endTime,
                vo.getPlayIds(),
                vo.getTenantIds(),
                vo.getUnit(),
                vo.getSize());
            score = orderDetailService.getHalfScore(item.getMatchId());
        } else {
            reusult = orderDetailService.queryMatrixByMatchId(vo.getTenantIds(),
                vo.getMatchId(),
                vo.getMatchType(),
                vo.getIsSettlement(),
                vo.getPlayIds(),
                vo.getUnit(),
                vo.getSize());
            score = orderDetailService.getCurrentScore(vo.getMatchId());
        }
        if (reusult == null || reusult.length == 0 || reusult[0].length == 0) {
            throw new RpcException("没有相关矩阵相关信息：vo=" + vo.toString());
        }

        dataMap.put("list", getMatrixBeans(reusult));
        dataMap.put("score", score);

        Long cacheTime = 20L;
        String cacheTimeRedis = redisClient.get("rcs:trade:matrix:cache:time");
        //先从缓存取值
        if (StringUtils.isNotBlank(cacheTimeRedis)){
            cacheTime = Long.valueOf(cacheTimeRedis);
        }
        redisClient.setExpiry(cacheKey, JSONObject.toJSONString(dataMap), cacheTime);
        return dataMap;
    }

    public Map<String, Object> getMatrixInfoData(@RequestBody RcsMatrixInfoReqVo vo) throws RpcException {
        String score = "0:0";
        LambdaQueryWrapper<MatchStatisticsInfoDetail> lambdaQueryWrapper = new QueryWrapper<MatchStatisticsInfoDetail>().lambda();

        lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getStandardMatchId, vo.getMatchId());

        if (vo.getMatrixType() == 1) {//全场比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "match_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 0);
        } else if (vo.getMatrixType() == 2) {//上半场比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "set_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 1);
        } else if (vo.getMatrixType() == 3) {//下半场比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "set_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 2);
        } else if (vo.getMatrixType() == 4) {//全场角球比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "corner_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 0);
        } else if (vo.getMatrixType() == 5) {//上半场角球比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "corner_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 1);
        } else if (vo.getMatrixType() == 6) {//全场加时比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "extra_time_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 0);
        }else if (vo.getMatrixType() == 7)  {//上半场加时比分
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getCode, "set_score");
            lambdaQueryWrapper.eq(MatchStatisticsInfoDetail::getFirstNum, 3);
        }
        lambdaQueryWrapper.last(" limit 1 ");

        MatchStatisticsInfoDetail result = matchStatisticsInfoDetailMapper.selectOne(lambdaQueryWrapper);
        if (result == null) {
            log.info("::{}::查询赛事比分未查到数据" + vo.getMatchId(),CommonUtil.getRequestId());
        }else{
            Integer t1 = result.getT1();
            if (t1 == null) {
                t1 = 0;
            }
            Integer t2 = result.getT2();
            if (t2 == null) {
                t2 = 0;
            }
            score = t1 + ":" + t2;
        }

        MatrixVo[][] reusult = orderDetailService.getMatrixInfoData(vo, score);
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("list", getMatrixBeans(reusult));
        dataMap.put("score", score);
        return dataMap;
    }

    @RequestMapping(value = "/getList",method = RequestMethod.POST)
    public  HttpResponse  getList(@RequestBody MatchMatrixsVo matchMatrixsVo){
        try {
            MatchMatrixDatasVo matchMatrixDatasVo=new MatchMatrixDatasVo();
            Long matchStage = matchMatrixsVo.getMatchStage();
            if (matchStage==null){
                return HttpResponse.failToMsg("matchStage字段不能为空");
            }
            Integer unit = matchMatrixsVo.getUnit();
            if (unit==null){
                unit=1;
            }
            Integer size = matchMatrixsVo.getSize();
            if (size==null){
                size=5;
            }
            Integer pageNum = matchMatrixsVo.getPageNum();
            if (pageNum == null) {
                pageNum = 1;
            }
            matchMatrixsVo.getMatchType();
            Integer pageSize = matchMatrixsVo.getPageSize();
            if (pageSize == null) {
                pageSize = 12;
                if (matchMatrixsVo.getMatchType() != null && matchMatrixsVo.getMatchType() == 1) {
                    pageSize = 20;
                }
            }
            long beginTime = DateUtils.getBeginTime(matchMatrixsVo.getMatchStartTime());
            long endTime = DateUtils.getEndTime(matchMatrixsVo.getMatchStartTime());
            //查询满足条件的有多少赛事
            IPage<StandardMatchInfoMatrixVo> iPage = new Page<>(pageNum, pageSize);
            IPage<StandardMatchInfoMatrixVo> longIPage = rcsStandardSportMarketSellMapper.selectMatchId(iPage, matchMatrixsVo.getTournamentList(), matchMatrixsVo.getMatchType()-1, beginTime, endTime, getUserId(matchMatrixsVo.getTraderIdOrg()));
            List<Map<String, Object>> list = new ArrayList<>();
            List<StandardMatchInfoMatrixVo> standardMatchInfoMatrixVoList = longIPage.getRecords();
            if (!CollectionUtils.isEmpty(standardMatchInfoMatrixVoList)) {
                List<Long> playIds = standardSportMarketCategoryMapper.selectPlayIdByTheirTime(matchStage);
                List<Long> longs = rcsCodeMapper.selectMerchantList();
                for (StandardMatchInfoMatrixVo standardMatchInfoMatrixVo : standardMatchInfoMatrixVoList) {
                    MatchMatrixVo matchMatrixVo = new MatchMatrixVo();
                    matchMatrixVo.setMatchId(standardMatchInfoMatrixVo.getId());
                    matchMatrixVo.setMatchStage(matchStage);
                    matchMatrixVo.setPlayIds(playIds);
                    matchMatrixVo.setSize(size);
                    matchMatrixVo.setTenantIds(longs);
                    matchMatrixVo.setUnit(unit);
                    matchMatrixVo.setMatchType(standardMatchInfoMatrixVo.getMatchStatus());
                    Map<String, Object> matrixByMatchData = getMatrixByMatchData(matchMatrixVo);
                    matrixByMatchData.put("matchId",standardMatchInfoMatrixVo.getId());
                    list.add(matrixByMatchData);
                }
            }
            matchMatrixDatasVo.setList(list);
            matchMatrixDatasVo.setPageNum(pageNum);
            matchMatrixDatasVo.setPageSize(pageSize);
            matchMatrixDatasVo.setTotal((int)longIPage.getTotal());
            return HttpResponse.success(matchMatrixDatasVo);
        }catch (Exception e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return  HttpResponse.failToMsg("服务器故障");
        }
    }
    public  Set<String> getUserId(String org){
        //我和我的部下
        Set<String> userIdSet =new HashSet<>();
        if (StringUtils.isNotEmpty(org)) {
            String[] split = org.split(",");
            Integer[] orgList = new Integer[split.length];
            for (int x = 0; x < split.length; x++) {
                orgList[x] = Integer.valueOf(split[x]);
            }
            Set<Integer> orgAllUser = systemUserOrgAuthApi.getOrgAllUser(orgList);
            if (!CollectionUtils.isEmpty(orgAllUser)) {
                for (Integer i : orgAllUser) {
                    userIdSet.add(String.valueOf(i));
                }
            }
        }
        if (CollectionUtils.isEmpty(userIdSet)){
            return null;
        }else {
            return userIdSet;
        }
    }

    @RequestMapping(value = "/categorySetMatrix")
    public HttpResponse categorySetMatrix(@RequestBody MatchMatrixVo vo) throws RpcException {
        try {

            MatrixVo[][] reusult = orderDetailService.queryMatrixByMatchId(vo.getTenantIds(), vo.getMatchId(),
                    vo.getMatchType(), vo.getIsSettlement(), vo.getPlayIds(), vo.getUnit(), vo.getSize());
            if (reusult == null || reusult.length == 0 || reusult[0].length == 0) {
                throw new RpcException("没有相关矩阵相关信息：vo=" + vo.toString());
            }
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("list", getMatrixBeans(reusult));
            dataMap.put("score", "");
            return HttpResponse.success(dataMap);
        } catch (Exception ex) {
//            log.error("调用矩阵接口异常,返回MatrixVo实体bean{}",  JsonFormatUtils.toJson(reusult));
			log.error("::{}::调用矩阵接口异常,请求参数{}", CommonUtil.getRequestId(), JsonFormatUtils.toJson(vo));
			log.error("::{}::调用矩阵接口异常,错误信息{}", CommonUtil.getRequestId(), ex.getMessage());
            return HttpResponse.fail(ex);
        }
    }

    /**
     * 操盘概览
     *
     * @param reqVo
     * @return
     */
    @PostMapping("/matrixList")
    public HttpResponse matrixList(@RequestBody MatrixReqVo reqVo) {
        long s = System.currentTimeMillis();
        //请求 单赛事数量key
        String matchCurrentKey = "rcs:matrixList:current:num:";
        try {
            //判断单个赛事 查询总数量是否大于3
            Long currentNum = redisClient.incrBy(matchCurrentKey, 0L);

            int matchSingleMaxNum =3;
            String matchSingleMaxKey = "rcs:matrixList:max:num";
            if (StringUtils.isNotBlank(redisClient.get(matchSingleMaxKey))) {
                matchSingleMaxNum = Integer.valueOf(redisClient.get(matchSingleMaxKey));
            }
            redisClient.incrBy(matchCurrentKey, 1L);
            //保险起见,设置过期时间 避免极端情况,出现重启导致key没删除,占用数量(常规来说,一个查询过程不会超过60s)
            redisClient.expireKey(matchCurrentKey, 60);
            log.info("::{}::矩阵限流matrixList,数量{}",CommonUtil.getRequestId(), currentNum);
            if (currentNum >= matchSingleMaxNum) {
				log.error("::{}::请求限流matrixList 同时查询超出数量{},{}", CommonUtil.getRequestId(), matchSingleMaxNum, JSONObject.toJSONString(reqVo));
                return HttpResponse.fail("请求限流,同时查询超出数量3");
            }

            Long traderIdOrg = reqVo.getTraderIdOrg();
            reqVo.setTraderIdList(null);
            if (traderIdOrg != null && traderIdOrg > 0L) {
                long start = System.currentTimeMillis();
                Set<String> traderIdList = getUserId(traderIdOrg.toString());
                long end = System.currentTimeMillis();
                log.info("::{}::操盘概览耗时日志：{}ms",CommonUtil.getRequestId(), end - start);
                if (CollectionUtils.isEmpty(traderIdList)) {
                    return HttpResponse.failure("操盘团队下无操盘手");
                }
                reqVo.setTraderIdList(traderIdList);
            }
            IPage<MatrixMatchInfoVo> page = new Page<>(reqVo.getPageNum(), reqVo.getPageSize());
            long start = System.currentTimeMillis();
            IPage<MatrixMatchInfoVo> pageInfo = standardMatchInfoMapper.getMatrixMatchInfo(page, reqVo);
            long end = System.currentTimeMillis();
            log.info("::{}::操盘概览耗时日志：{}ms", CommonUtil.getRequestId(),end - start);
            if (pageInfo == null || CollectionUtils.isEmpty(pageInfo.getRecords())) {
                return HttpResponse.success(pageInfo);
            }
            List<Long> matchIdList = pageInfo.getRecords().stream().map(MatrixMatchInfoVo::getMatchId).collect(Collectors.toList());
            start = System.currentTimeMillis();
            List<MatchMarketLiveBean> matchInfoList = getMatchInfoList(matchIdList);
            matchInfoList = matchInfoList.stream().filter(distinctByKey(MatchMarketLiveBean::getMatchId)).collect(Collectors.toList());
            end = System.currentTimeMillis();
            log.info("::{}::操盘概览耗时日志：{}ms",CommonUtil.getRequestId(), end - start);
            Map<Long, Map<String, I18nBean>> teamMap = getMatchTeamInfo(matchInfoList);
            Map<Long, Integer> traderNumMap = getTraderNum(matchInfoList);
            Map<Long, Integer> secondsMatchStartMap = getSecondsMatchStart(matchInfoList);
            start = System.currentTimeMillis();
            List<Long> merchantList = rcsCodeMapper.selectMerchantList();
            end = System.currentTimeMillis();
            log.info("::{}::操盘概览耗时日志：{}ms",CommonUtil.getRequestId(), end - start);
            start = System.currentTimeMillis();
            List<Long> playIdList = marketCategorySetService.getMatrixPlayIdList(reqVo.getSportId(), reqVo.getMatchStage());
            end = System.currentTimeMillis();
            log.info("::{}::操盘概览耗时日志：{}ms",CommonUtil.getRequestId(), end - start);
            pageInfo.getRecords().parallelStream().forEach(item -> {
                Long matchId = item.getMatchId();
                MatchMatrixVo matchMatrixVo = new MatchMatrixVo();
                matchMatrixVo.setMatchId(matchId);
                matchMatrixVo.setPlayIds(playIdList);
                matchMatrixVo.setTenantIds(merchantList);
                matchMatrixVo.setMatchType(item.getMatrixMatchType());
                matchMatrixVo.setMatchStage(reqVo.getMatchStage());
                matchMatrixVo.setUnit(reqVo.getUnit());
                matchMatrixVo.setSize(reqVo.getMatrixSize());
                long t1 = System.currentTimeMillis();
                Map<String, Object> matrixData = getMatrixByMatchData(matchMatrixVo);
                long t2 = System.currentTimeMillis();
                log.info("::{}::操盘概览耗时日志：{}ms",CommonUtil.getRequestId(), t2 - t1);
                item.setMatrixData(matrixData);
                if (teamMap.containsKey(matchId)) {
                    Map<String, I18nBean> team = teamMap.get(matchId);
                    if (CollectionUtils.isNotEmpty(team)) {
                        item.setHomeTeam(team.get(RcsConstant.HOME_POSITION));
                        item.setAwayTeam(team.get(RcsConstant.AWAY_POSITION));
                    }
                }
                item.setTraderNum(traderNumMap.getOrDefault(matchId, 0));
                item.setSecondsMatchStart(secondsMatchStartMap.getOrDefault(matchId, 0));
            });
            return HttpResponse.success(pageInfo);
        } catch (Exception e) {
			log.error("::{}::操盘概览异常:", CommonUtil.getRequestId(), e);
            return HttpResponse.failToMsg("风控服务异常");
        }finally {
            redisClient.incrBy(matchCurrentKey, -1L);
            log.info("::{}::矩阵限流matrixList,redis处理完毕{}", CommonUtil.getRequestId(),JSONObject.toJSONString(reqVo));
            long end = System.currentTimeMillis();
            log.info("::{}::操盘概览耗时日志：{}ms",CommonUtil.getRequestId(), end - s);
        }
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    private MarketLiveOddsQueryVo createMatchQueryVoByForecastReqVo(ForecastReqVo reqVo) throws Exception {
        MarketLiveOddsQueryVo matchQueryVo = new MarketLiveOddsQueryVo();
        BeanUtils.copyProperties(reqVo, matchQueryVo);
        matchQueryVo.setTradeId(Long.valueOf(TradeUserUtils.getUserId()));
        matchQueryVo.setSortType( reqVo.getSortName() == 2 ? 2 : 1);
        matchQueryVo.setLiveOddBusiness(reqVo.getMatchType() == null ? null : reqVo.getMatchType() == 0 ? 1 : null);
        return matchQueryVo;
    }
    /**
     * forecast概览
     * @return
     */
    @PostMapping("/forecastList")
    public HttpResponse forecastList(@RequestBody ForecastReqVo reqVo) {

        if(reqVo.getChooseType() == null || reqVo.getChooseType() == 0){
            return HttpResponse.failure("赛事选择类型ChooseType参数非法");
        }
        if(reqVo.getCategorySetId() == null || reqVo.getCategorySetId() == 0L){
            return HttpResponse.failure("主盘口玩法参数不能非法");
        }
        try {
            Long traderIdOrg = reqVo.getTraderIdOrg();
            reqVo.setTraderIdList(null);
            if (traderIdOrg != null && traderIdOrg > 0L) {
                Set<String> traderIdList = getUserId(traderIdOrg.toString());
                if (CollectionUtils.isEmpty(traderIdList)) {
                    return HttpResponse.failure("操盘团队下无操盘手");
                }
                reqVo.setTraderIdList(traderIdList);
            }
            MarketLiveOddsQueryVo matchQueryVo = createMatchQueryVoByForecastReqVo(reqVo);
            Long beginTime = System.currentTimeMillis();
            log.info("::{}::---forecastList 获取操盘手：{}forecast数据，参数：{}",CommonUtil.getRequestId(), TradeUserUtils.getUserId(), matchQueryVo);

            /********获取赛事id******/
            //0-滚球，1-早盘，为空全部
            PageResult<MatchMarketLiveBean> matchIdPagePre = null;
            PageResult<MatchMarketLiveBean> matchIdPageLive = null;
            List<MatchMarketLiveBean> matchInfoList = new ArrayList<>();
            if (reqVo.getMatchType() == null) {
                Criteria criteria = sportMatchViewService.buildMongoCriteria(matchQueryVo, 1);
                matchIdPageLive = sportMatchViewService.queryMatchList(matchQueryVo, criteria);
                matchInfoList.addAll(matchIdPageLive.getList());

                Criteria criteriaPre = sportMatchViewService.buildMongoCriteria(matchQueryVo, 2);
                matchIdPagePre = sportMatchViewService.queryMatchList(matchQueryVo, criteriaPre);
                matchInfoList.addAll(matchIdPagePre.getList());
            } else if (reqVo.getMatchType() == 0) {
                Criteria criteria = sportMatchViewService.buildMongoCriteria(matchQueryVo, 1);
                matchIdPageLive = sportMatchViewService.queryMatchList(matchQueryVo, criteria);
                matchInfoList.addAll(matchIdPageLive.getList());
            } else if (reqVo.getMatchType() == 1) {
                Criteria criteria = sportMatchViewService.buildMongoCriteria(matchQueryVo, 2);
                matchIdPagePre = sportMatchViewService.queryMatchList(matchQueryVo, criteria);
                matchInfoList.addAll(matchIdPagePre.getList());
            }


            IPage<ForecastInfoVo> page = new Page<>(reqVo.getPageNum(), reqVo.getPageSize());
            log.info("::{}::---forecastList 获取操盘手：{} 耗时： {} 赛事id： {}",CommonUtil.getRequestId(), TradeUserUtils.getUserId(),  System.currentTimeMillis() - beginTime,
                    matchInfoList.stream().map(MatchMarketLiveBean :: getMatchId).collect(Collectors.toList()));
            if(CollectionUtils.isEmpty(matchInfoList)){
                return HttpResponse.success(page);
            }
            reqVo.setMatchIds(matchInfoList.stream().map(MatchMarketLiveBean :: getMatchId).distinct().collect(Collectors.toList()));

            /********获取赛事id******/

            IPage<ForecastInfoVo> pageInfo = standardMatchInfoMapper.getForecastMatchInfo(page, reqVo);

            if (pageInfo == null || CollectionUtils.isEmpty(pageInfo.getRecords())) {
                return HttpResponse.success(pageInfo);
            }
            matchInfoList = matchInfoList.stream().filter(distinctByKey(MatchMarketLiveBean::getMatchId)).collect(Collectors.toList());
            Map<Long, Map<String, I18nBean>> teamMap = getMatchTeamInfo(matchInfoList);
            Map<Long, Integer> traderNumMap = getTraderNum(matchInfoList);
            Map<Long, Integer> secondsMatchStartMap = getSecondsMatchStart(matchInfoList);
            List<Long> nameCodes = pageInfo.getRecords().stream().map(ForecastInfoVo::getTournamentNameCode).distinct().collect(Collectors.toList());
            Map<Long, Map<String, String>> cachedNamesMapByCodes = rcsLnguageInternationService.getCachedNamesMapByCodes(nameCodes);
            Map<Long, Map<String, String>> tempCachedNamesMapByCodes = CollectionUtils.isEmpty(cachedNamesMapByCodes) ? new HashMap<>() : cachedNamesMapByCodes;
            pageInfo.getRecords().parallelStream().forEach(item -> {
                //设置联赛国际化名称
                if(!CollectionUtils.isEmpty(tempCachedNamesMapByCodes.get(item.getTournamentNameCode()))){
                    item.setTournamentNames(JSON.toJSONString(tempCachedNamesMapByCodes.get(item.getTournamentNameCode())));
                }
                Long matchId = item.getMatchId();
                if (teamMap.containsKey(matchId)) {
                    Map<String, I18nBean> team = teamMap.get(matchId);
                    if (CollectionUtils.isNotEmpty(team)) {
                        item.setHomeTeam(team.get(RcsConstant.HOME_POSITION));
                        item.setAwayTeam(team.get(RcsConstant.AWAY_POSITION));
                    }
                }
                item.setTraderNum(traderNumMap.getOrDefault(matchId, 0));
                item.setSecondsMatchStart(secondsMatchStartMap.getOrDefault(matchId, 0));

                if(item.getBeginTime() - System.currentTimeMillis() < 900000 && item.getLiveOddBusiness() == 1){
                    item.setMatchSnapshot(1);
                }else{
                    item.setMatchSnapshot(0);
                }

                //查询出 全场大小、让球和上半场大小、让球的forecast集合；大小type=1，让球type=2
                List<PredictForecastVo> predictForecastVos = standardMatchInfoMapper.selectForecastPlayList(matchId, item.getMatchType()==1?1:2, 1);
                predictForecastVos.addAll(standardMatchInfoMapper.selectForecastPlayList(matchId, item.getMatchType()==1?1:2, 2));

                if(predictForecastVos.size()>0){
                    Map<Long, List<PredictForecastVo>> collect = predictForecastVos.stream().collect(Collectors.groupingBy(bean -> bean.getPlayId()));
                    //没有forecast的设置默认值
                    Long arr[] = new Long[]{2L,4L,18L,19L};
                    for (Long playId : arr) {
                        if (collect.get(playId) == null) {
                            List<PredictForecastVo> forecastList = new ArrayList<>();

                            //大小球（排序从小到大）
                            if(playId==2L || playId == 18L){
                                for (int i = 0; i <=25 ; i++) {
                                    PredictForecastVo vo = new PredictForecastVo();
                                    vo.setMatchId(matchId);
                                    vo.setMatchType(item.getMatchType()==1?1:2);
                                    vo.setPlayId(playId);
                                    vo.setProfitValue(BigDecimal.ZERO);
                                    vo.setScore(i);
                                    forecastList.add(vo);
                                }
                                collect.put(playId,forecastList);
                            }

                            //让球（排序从大到小）
                            if(playId==4L || playId == 19L){
                                for (int i = 12; i >=-12 ; i--) {
                                    PredictForecastVo vo = new PredictForecastVo();
                                    vo.setMatchId(matchId);
                                    vo.setMatchType(item.getMatchType()==1?1:2);
                                    vo.setPlayId(playId);
                                    vo.setProfitValue(BigDecimal.ZERO);
                                    vo.setScore(i);
                                    forecastList.add(vo);
                                }
                                collect.put(playId,forecastList);
                            }

                        }
                    }
                    item.setPredictForecastVoList(collect);
                }

                //setMatchMarketStatus
                RcsTradeConfig matchStatusConfig = tradeConfigService.getMatchStatusConfig(matchId);
                if(ObjectUtils.isNotEmpty(matchStatusConfig)){
                    item.setMatchMarketStatus(matchStatusConfig.getStatus());
                }

                //setBusinessEvent
//                RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellService.selectStandardMarketSellVo(matchId);
//                if(ObjectUtils.isNotEmpty(rcsStandardSportMarketSell)){
//                    item.setBusinessEvent(rcsStandardSportMarketSell.getBusinessEvent());
//                }

                //setEventInfo 包括事件编码
                long t6 = System.currentTimeMillis();
                List<CustomizedEventBeanVo> customizedEventBeanVos = eventInfoService.selectMatchEventInfoByMatchId(matchId, null, null, null, null, 1,null);
                long t7 = System.currentTimeMillis();
                log.info("::{}::耗时 time4="+(t7-t6),CommonUtil.getRequestId());
                if(CollectionUtils.isNotEmpty(customizedEventBeanVos)){
                    item.setEventBeanVo(customizedEventBeanVos.get(0));
                }
            });
            log.info("::{}::---forecastList 获取操盘手：{} 耗时： {} 完成",CommonUtil.getRequestId(), TradeUserUtils.getUserId(),  System.currentTimeMillis() - beginTime);
            return HttpResponse.success(pageInfo);
        } catch (Exception e) {
			log.error("::{}::forecast概览异常{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("风控服务异常");
        }
    }


    private List<MatchMarketLiveBean> getMatchInfoList(List<Long> matchIdList) {
        Query query = new Query().addCriteria(Criteria.where("matchId").in(matchIdList));
        return mongoTemplate.find(query, MatchMarketLiveBean.class);
    }

    private Map<Long, Map<String, I18nBean>> getMatchTeamInfo(List<MatchMarketLiveBean> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(MatchMarketLiveBean::getMatchId, bean -> {
            // 球队信息
            Map<String, I18nBean> teamMap = Maps.newHashMap();
            if (CollectionUtils.isNotEmpty(bean.getTeamList())) {
                for (MatchTeamVo team : bean.getTeamList()) {
                    String position = team.getMatchPosition();
                    Map<String, String> names = team.getNames();
                    if (StringUtils.isNotBlank(position) && CollectionUtils.isNotEmpty(names)) {
                        teamMap.put(position.toLowerCase(), new I18nBean(names));
                    }
                }
            }
            return teamMap;
        }));
    }

    private Map<Long, Integer> getTraderNum(List<MatchMarketLiveBean> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(MatchMarketLiveBean::getMatchId, MatchMarketLiveBean::getTraderNum));
    }

    private Map<Long, Integer> getSecondsMatchStart(List<MatchMarketLiveBean> list) {
        if (CollectionUtils.isEmpty(list)) {
            return Maps.newHashMap();
        }
        return list.stream().collect(Collectors.toMap(MatchMarketLiveBean::getMatchId, match -> {
            Integer secondsMatchStart = match.getSecondsMatchStart() == null ? 0 : match.getSecondsMatchStart();
            if ("timeout".equalsIgnoreCase(match.getEventCode())) {
                match.setSecondsMatchStart(secondsMatchStart);
                return secondsMatchStart;
            }
            long eventTime = match.getEventTime() == null ? 0 : match.getEventTime();
            long time = (System.currentTimeMillis() - eventTime) / 1000;
            eventTime = eventTime > 0 ? time : 0;
            int secondsTime = secondsMatchStart + (int) eventTime;
            return secondsTime > 0 ? secondsTime : 0;
        }));
    }
}
