package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.panda.sport.manager.api.IMarketCategorySellApi;
import com.panda.sport.rcs.cache.RcsCacheUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.mapper.MarketCategorySetMapper;
import com.panda.sport.rcs.mapper.RcsStandardSportMarketSellMapper;
import com.panda.sport.rcs.mapper.RcsTradingAssignmentMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.pojo.RcsCategorySetTraderWeight;
import com.panda.sport.rcs.pojo.RcsStandardSportMarketSell;
import com.panda.sport.rcs.pojo.RcsTradingAssignment;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.pojo.bo.FindMarketCategoryListAndNamesBO;
import com.panda.sport.rcs.pojo.vo.TradingAssignmentDataVo;
import com.panda.sport.rcs.trade.service.TradeVerificationService;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.ChangePersonLiableVo;
import com.panda.sport.rcs.trade.vo.TradingAssignmentSubPlayVo;
import com.panda.sport.rcs.trade.vo.TradingAssignmentVo;
import com.panda.sport.rcs.trade.wrapper.IAuthPermissionService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.RcsCategorySetTraderWeightService;
import com.panda.sport.rcs.trade.wrapper.RcsStandardSportMarketSellService;
import com.panda.sport.rcs.trade.wrapper.RcsTradingAssignmentService;
import com.panda.sport.rcs.utils.StringUtils;
import com.panda.sport.rcs.utils.TradeUserUtils;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsMarketCategorySetVo;
import com.panda.sports.api.ISystemUserOrgAuthApi;
import com.panda.sports.api.vo.SysTraderVO;
import com.panda.sports.auth.permission.AuthRequiredPermission;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.Reference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;


/**
 * @program: xindaima
 * @description:指派相关
 * @author: kimi
 * @create: 2020-11-06 16:29
 **/
@Component
@RestController
@RequestMapping(value = "/tradingAssignment")
@Slf4j
public class TradingAssignmentController {
    @Autowired
    private RcsStandardSportMarketSellMapper rcsStandardSportMarketSellMapper;
    @Autowired
    private MarketCategorySetMapper rcsMarketCategorySetMapper;
    @Autowired
    private RcsTradingAssignmentMapper rcsTradingAssignmentMapper;
    @Autowired
    private RcsTradingAssignmentService rcsTradingAssignmentService;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    RcsStandardSportMarketSellService rcsStandardSportMarketSellService;
    @Autowired
    RcsCategorySetTraderWeightService rcsCategorySetTraderWeightService;

    @Autowired
    IAuthPermissionService iAuthPermissionService;
    @Reference(check = false, lazy = true, retries = 1, timeout = 5000)
    private ISystemUserOrgAuthApi systemUserOrgAuthApi;
    @Reference(check = false, lazy = true, retries = 1, timeout = 100000)
    private IMarketCategorySellApi iMarketCategorySellApi;

    @Autowired
    private MarketCategorySetService marketCategorySetService;

    public static Cache<Long, List<FindMarketCategoryListAndNamesBO>> weightsSetCache = RcsCacheUtils.newCache("weightsPlayCache",
            50, 20000, 60, 60, 0);
    public static Cache<Long, List<FindMarketCategoryListAndNamesBO>> weightsPlayCache = RcsCacheUtils.newCache("weightsPlayCache",
            50, 20000, 60, 60, 0);

    /**
     * @Description:获取指派数据
     * @Param: [matchId]
     * @return: com.panda.sport.rcs.vo.HttpResponse
     * @Author: KIMI
     * @Date: 2020/11/6
     */
    @RequestMapping(value = "/getList", method = RequestMethod.GET)
    public HttpResponse<TradingAssignmentVo> getList(Long sportId, Long matchId, Integer matchType) {
        try {
            //玩法集列表
            TradingAssignmentVo tradingAssignmentVo = new TradingAssignmentVo();
            List<RcsMarketCategorySetVo> rcsMarketCategorySetVoList = rcsMarketCategorySetMapper.selectRcsMarketCategorySet(sportId);
            if (CollectionUtils.isEmpty(rcsMarketCategorySetVoList)) {
                return HttpResponse.fail("没有需要指派的玩法集，无法指派");
            }
            RcsMarketCategorySetVo rcsMarketCategorySetVo = new RcsMarketCategorySetVo();
            rcsMarketCategorySetVo.setId(-1L);
            rcsMarketCategorySetVo.setName("其他类");
            rcsMarketCategorySetVoList.add(rcsMarketCategorySetVo);
            tradingAssignmentVo.setRcsMarketCategorySetVoHashMap(rcsMarketCategorySetVoList);
            //查询指玩家数据
            List<TradingAssignmentDataVo> rcsTradingAssignmentList = rcsTradingAssignmentMapper.selectRcsTradingAssignment(matchId, matchType, rcsMarketCategorySetVoList);
            RcsStandardSportMarketSell rcsStandardSportMarketSell = rcsStandardSportMarketSellMapper.selectRcsStandardSportMarketSellByMatchInfoId(matchId);
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
            int traderId = 0;
            if (matchType == 0 && rcsStandardSportMarketSell.getPreTraderId() != null && rcsStandardSportMarketSell.getPreTraderId().length() > 0) {
                traderId = Integer.parseInt(rcsStandardSportMarketSell.getPreTraderId());
            } else if (matchType == 1 && rcsStandardSportMarketSell.getLiveTraderId() != null && rcsStandardSportMarketSell.getLiveTraderId().length() > 0) {
                traderId = Integer.parseInt(rcsStandardSportMarketSell.getLiveTraderId());
            }
            //操盘手数据
            HashMap<String, SysTraderVO> sysTraderVOHashMap1 = new HashMap<>();
            HashMap<String, TradingAssignmentDataVo> stringTradingAssignmentDataVoHashMap = new LinkedHashMap<>();
            if (traderId > 0) {
                TradingAssignmentDataVo tradingAssignmentDataVo = new TradingAssignmentDataVo();
                tradingAssignmentDataVo.setMatchId(matchId);
                tradingAssignmentDataVo.setMatchType(matchType);
                tradingAssignmentDataVo.setUserId(String.valueOf(traderId));
                if ("PA".equals(standardMatchInfo.getLiveRiskManagerCode()) && 1 == matchType) {
                    tradingAssignmentDataVo.setIsResponsible(1);
                } else if ("PA".equals(standardMatchInfo.getPreRiskManagerCode()) && 0 == matchType) {
                    tradingAssignmentDataVo.setIsResponsible(1);

                }
                for (RcsMarketCategorySetVo rcsMarketCategorySetVo1 : rcsMarketCategorySetVoList) {
                    Set<Long> playCollectionIdList = tradingAssignmentDataVo.getPlayCollectionIdList();
                    playCollectionIdList.add(rcsMarketCategorySetVo1.getId());
                    tradingAssignmentDataVo.setPlayCollectionIdList(playCollectionIdList);
                }
                stringTradingAssignmentDataVoHashMap.put(String.valueOf(traderId), tradingAssignmentDataVo);
                SysTraderVO traderData = rcsTradingAssignmentService.getTraderDataById(Integer.parseInt(tradingAssignmentDataVo.getUserId()));
                if (traderData == null) {
                    return HttpResponse.error(-1, "操盘部没找到玩家数据：" + tradingAssignmentDataVo.getUserId());
                }
                sysTraderVOHashMap1.put(tradingAssignmentDataVo.getUserId(), traderData);
            }
            if (!CollectionUtils.isEmpty(rcsTradingAssignmentList)) {
                for (TradingAssignmentDataVo tradingAssignmentDataVo : rcsTradingAssignmentList) {
                    TradingAssignmentDataVo tradingAssignmentDataVo1 = stringTradingAssignmentDataVoHashMap.get(tradingAssignmentDataVo.getUserId());
                    if (tradingAssignmentDataVo1 == null) {
                        tradingAssignmentDataVo1 = new TradingAssignmentDataVo();
                        tradingAssignmentDataVo1.setMatchId(tradingAssignmentDataVo.getMatchId());
                        tradingAssignmentDataVo1.setMatchType(tradingAssignmentDataVo.getMatchType());
                        tradingAssignmentDataVo1.setUserId(tradingAssignmentDataVo.getUserId());
                        SysTraderVO traderData = rcsTradingAssignmentService.getTraderDataById(Integer.parseInt(tradingAssignmentDataVo.getUserId()));
                        if (traderData == null) {
                            return HttpResponse.error(-1, "操盘部没找到玩家数据：" + tradingAssignmentDataVo.getUserId());
                        }
                        sysTraderVOHashMap1.put(tradingAssignmentDataVo.getUserId(), traderData);
                        stringTradingAssignmentDataVoHashMap.put(tradingAssignmentDataVo1.getUserId(), tradingAssignmentDataVo1);
                    }
                    tradingAssignmentDataVo1.getPlayCollectionIdList().add(tradingAssignmentDataVo.getPlayCollectionId());
                }
            }
            tradingAssignmentVo.setStringTradingAssignmentDataVoHashMap(stringTradingAssignmentDataVoHashMap.values());
            tradingAssignmentVo.setStringSysTraderVOHashMap(sysTraderVOHashMap1);
            return HttpResponse.success(tradingAssignmentVo);
        } catch (RcsServiceException e) {
            log.error("::{}::getList:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::getList:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }


    /**
     * @Description: 新增指派数据
     * @Param: [tradingAssignmentDataVoList]
     * @return: com.panda.sport.rcs.vo.HttpResponse
     * @Author: KIMI
     * @Date: 2020/11/8
     */
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public HttpResponse add(@RequestBody TradingAssignmentDataVo tradingAssignmentDataVo) {
        try {
            Integer tradeId = TradeUserUtils.getUserId();
            return rcsTradingAssignmentService.add(tradingAssignmentDataVo, String.valueOf(tradeId));
        } catch (RcsServiceException e) {
            log.error("::{}::新增指派数据:{}", CommonUtil.getRequestId(tradingAssignmentDataVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::新增指派数据:{}", CommonUtil.getRequestId(tradingAssignmentDataVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }

    /**
     * @Description: 修改指派数据
     * @Param: [rcsTradingAssignmentList]
     * @return: com.panda.sport.rcs.vo.HttpResponse
     * @Author: KIMI
     * @Date: 2020/11/8
     */
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public HttpResponse update(@RequestBody List<RcsTradingAssignment> rcsTradingAssignmentList) {
        try {
            Integer userId = TradeUserUtils.getUserId();
            return rcsTradingAssignmentService.update(rcsTradingAssignmentList, userId);
        } catch (RcsServiceException e) {
            log.error("::{}::修改指派数据:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::修改指派数据:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }


    /**
     * @Description: 模糊查询操盘手数据
     * @Param: [userName]
     * @return: com.panda.sport.rcs.vo.HttpResponse
     * @Author: KIMI
     * @Date: 2020/11/7
     */
    @RequestMapping(value = "/getOperatorData", method = RequestMethod.GET)
    public HttpResponse<List<SysTraderVO>> getOperatorData(String userName) {
        try {
            if (userName == null || userName.length() == 0) {
                return HttpResponse.error(-1, "名字不能为空");
            }
            List<SysTraderVO> traderUserByUserCode = systemUserOrgAuthApi.getTraderUserByUserCode(userName);
            return HttpResponse.success(traderUserByUserCode);
        } catch (RcsServiceException e) {
            log.error("::{}::模糊查询操盘手数据:{}", CommonUtil.getRequestId(userName), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::模糊查询操盘手数据:{}", CommonUtil.getRequestId(userName), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }

    /**
     * 变更赛事负责人
     *
     * @return
     */
    @RequestMapping(value = "/changePersonLiable", method = RequestMethod.POST)
    public HttpResponse changePersonLiablea(@RequestBody ChangePersonLiableVo changePersonLiableVo) {
        try {
            Integer userId = TradeUserUtils.getUserId();
            Integer appId = TradeUserUtils.getAppId();

            boolean b = iAuthPermissionService.checkAuthOpearate( "Risk:Trade:Change:Person");
            if (!b) {
                return HttpResponse.failToMsg("您没有该操作权限！");
            }
            return rcsTradingAssignmentService.changePersonLiable(changePersonLiableVo, userId, appId);
        } catch (RcsServiceException e) {
            log.error("::{}::变更赛事负责人:{}", CommonUtil.getRequestId(changePersonLiableVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::变更赛事负责人:{}", CommonUtil.getRequestId(changePersonLiableVo.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }


    /**
     * 得到权重
     *
     * @return
     */
    @RequestMapping(value = "/getWeights", method = RequestMethod.POST)
    public HttpResponse<TradingAssignmentVo> getWeights(@RequestBody TradingAssignmentVo dto) {
        try {
            List<RcsMarketCategorySetVo> rcsMarketCategorySetVoList = rcsMarketCategorySetMapper.selectRcsMarketCategorySetByParam(dto.getSportId(), 2);
            log.info("::{}::,getWeights1:{}", TradeVerificationService.getRequestIdStatic(), JSONObject.toJSONString(rcsMarketCategorySetVoList));
            if (CollectionUtils.isEmpty(rcsMarketCategorySetVoList)) {
                return HttpResponse.fail("没有需要指派的玩法集，无法指派");
            }
            //老权重转和成新权重
            oldDataToNewData(dto);
            List<Long> setIds = rcsMarketCategorySetVoList.stream().map(e -> e.getId()).collect(Collectors.toList());
            List<FindMarketCategoryListAndNamesBO> marketCategoryListAndNames = weightsSetCache.get(dto.getSportId(), e -> rcsMarketCategorySetMapper.findMarketCategoryListAndNamesPerformance(dto.getSportId(), null));
            log.info("::{}::,getWeights2:{}", TradeVerificationService.getRequestIdStatic(), JSONObject.toJSONString(marketCategoryListAndNames));
            Map<String, List<FindMarketCategoryListAndNamesBO>> sets = marketCategoryListAndNames.stream().filter(e -> setIds.contains(e.getMarketCategorySetId())).collect(Collectors.groupingBy(e -> String.valueOf(e.getMarketCategorySetId())));
            List<FindMarketCategoryListAndNamesBO> marketCategoryListAndNames2 = weightsPlayCache.get(dto.getSportId(), e -> rcsMarketCategorySetMapper.findMarketCategoryListAndNames(dto.getSportId(), null));
            log.info("::{}::,getWeights3:{}", TradeVerificationService.getRequestIdStatic(), JSONObject.toJSONString(marketCategoryListAndNames2));
            Map<String, List<FindMarketCategoryListAndNamesBO>> plays = marketCategoryListAndNames2.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getId())));
            QueryWrapper<RcsStandardSportMarketSell> sellWrapper = new QueryWrapper<>();
            sellWrapper.lambda().eq(RcsStandardSportMarketSell::getMatchInfoId, dto.getMatchId());
            RcsStandardSportMarketSell sellBean = rcsStandardSportMarketSellService.getOne(sellWrapper);
            log.info("::{}::,getWeights4:{}", TradeVerificationService.getRequestIdStatic(), JSONObject.toJSONString(sellBean));
            if (CollectionUtils.isEmpty(rcsMarketCategorySetVoList)) {
                return HttpResponse.fail("无此赛事");
            }
            //玩法集列表
            QueryWrapper<RcsCategorySetTraderWeight> weightQueryWrapper = new QueryWrapper<>();
            weightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getMatchId,dto.getMatchId());
            weightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getSportId,dto.getSportId());
            weightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getMarketType,dto.getMarketType());
            weightQueryWrapper.lambda().ne(RcsCategorySetTraderWeight::getWeight,0);

            List<RcsCategorySetTraderWeight> rcsCategorySetTraderWeights = rcsCategorySetTraderWeightService.list(weightQueryWrapper);
            log.info("::{}::,getWeights5:{}", TradeVerificationService.getRequestIdStatic(), JSONObject.toJSONString(rcsCategorySetTraderWeights));
            Map<String, List<RcsCategorySetTraderWeight>> rcsCategorySetTraderWeightSetGroups = rcsCategorySetTraderWeights.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getSetNo())));
            Map<String, List<RcsCategorySetTraderWeight>> rcsCategorySetTraderWeightPlayGroups = rcsCategorySetTraderWeights.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getTypeId())));
            if (CollectionUtils.isEmpty(rcsCategorySetTraderWeights)) {
                return HttpResponse.fail("无权重 错误数据");
            }
            List<TradingAssignmentVo> tradingAssignmentVos = new ArrayList<>();
            //封装玩法集数据
            for (RcsMarketCategorySetVo rcsMarketCategorySetVo : rcsMarketCategorySetVoList) {
                TradingAssignmentVo tradingAssignmentVo = new TradingAssignmentVo();
                tradingAssignmentVo.setSetNo(rcsMarketCategorySetVo.getId());
                tradingAssignmentVo.setSetNames(JSONObject.parseObject(rcsMarketCategorySetVo.getNames(), Map.class));
                tradingAssignmentVo.setMatchId(dto.getMatchId());
                tradingAssignmentVo.setSportId(dto.getSportId());
                tradingAssignmentVo.setMarketType(dto.getMarketType());
                ArrayList<TradingAssignmentSubPlayVo> objects = new ArrayList<>();
                List<FindMarketCategoryListAndNamesBO> findMarketCategoryListAndNamesBOS = sets.get(String.valueOf(rcsMarketCategorySetVo.getId()));
                if (CollectionUtils.isEmpty(findMarketCategoryListAndNamesBOS)) {
                    log.info("::{}::,玩法集下无玩法:{}", TradeVerificationService.getRequestIdStatic(), rcsMarketCategorySetVo.getId());
                    continue;
                }
                List<String> setPlayIds = findMarketCategoryListAndNamesBOS.stream().map(e -> String.valueOf(e.getId())).collect(Collectors.toList());
                List<RcsCategorySetTraderWeight> rcsCategorySetTraderWeights1 = rcsCategorySetTraderWeightSetGroups.get(String.valueOf(rcsMarketCategorySetVo.getId()));
                if (!CollectionUtils.isEmpty(rcsCategorySetTraderWeights1)) {
                    Map<String, List<RcsCategorySetTraderWeight>> setPlays = rcsCategorySetTraderWeights1.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getTypeId())));
                    //权重表里玩法集有的玩法
                    for (String playId : setPlays.keySet()) {
                        TradingAssignmentSubPlayVo tradingSubPlayVo = new TradingAssignmentSubPlayVo();
                        if (CommonUtil.isBlankOrNull(playId)) {
                            log.info("::{}::,玩法集玩法为空:{}", TradeVerificationService.getRequestIdStatic(), rcsMarketCategorySetVo.getId());
                            continue;
                        }
                        tradingSubPlayVo.setTypeId(Long.valueOf(playId));
                        if (null == plays.get(playId) || null == plays.get(playId).get(0) || CommonUtil.isBlankOrNull((plays.get(playId).get(0).getText()))) {
                            log.info("::{}::,玩法国际化为空:{}", TradeVerificationService.getRequestIdStatic(), playId);
                            continue;
                        }
                        tradingSubPlayVo.setSetNames(JSONObject.parseObject(plays.get(playId).get(0).getText()));
                        tradingSubPlayVo.setSysTraderWeightList(setPlays.get(playId));
                        objects.add(tradingSubPlayVo);
                    }

                    //有玩法集没玩法
                    List<String> weightPlayids = rcsCategorySetTraderWeights1.stream().map(e -> String.valueOf(e.getTypeId())).collect(Collectors.toList());
                    List<String> noPlayIds = new ArrayList<>();
                    noPlayIds.addAll(setPlayIds);
                    noPlayIds.removeAll(weightPlayids);
                    for (String noPlayId : noPlayIds) {
                        if (CommonUtil.isBlankOrNull(noPlayId)) {
                            log.info("::{}::,玩法集玩法为空2:{}", TradeVerificationService.getRequestIdStatic(), rcsMarketCategorySetVo.getId());
                            continue;
                        }
                        TradingAssignmentSubPlayVo tradingAssignmentSubPlayVo = new TradingAssignmentSubPlayVo();
                        tradingAssignmentSubPlayVo.setTypeId(Long.valueOf(noPlayId));
                        if (null == plays.get(noPlayId) || null == plays.get(noPlayId).get(0) || CommonUtil.isBlankOrNull((plays.get(noPlayId).get(0).getText()))) {
                            log.info("::{}::,玩法国际化为空:{}", TradeVerificationService.getRequestIdStatic(), noPlayId);
                            continue;
                        }
                        tradingAssignmentSubPlayVo.setSetNames(JSONObject.parseObject(plays.get(noPlayId).get(0).getText()));
                        tradingAssignmentSubPlayVo.setSysTraderWeightList(rcsCategorySetTraderWeightPlayGroups.get(noPlayId));
                        objects.add(tradingAssignmentSubPlayVo);
                    }
                } else {
                    //没玩法集 补充玩法集
                    for (FindMarketCategoryListAndNamesBO findMarketCategoryListAndNamesBO : findMarketCategoryListAndNamesBOS) {
                        if (rcsMarketCategorySetVo.getId().longValue() == findMarketCategoryListAndNamesBO.getMarketCategorySetId().longValue()) {
                            TradingAssignmentSubPlayVo tradingAssignmentSubPlayVo = new TradingAssignmentSubPlayVo();
                            tradingAssignmentSubPlayVo.setTypeId(findMarketCategoryListAndNamesBO.getId());
                            if (null == plays.get(String.valueOf(findMarketCategoryListAndNamesBO.getId())) || null == plays.get(String.valueOf(findMarketCategoryListAndNamesBO.getId())).get(0) || CommonUtil.isBlankOrNull((plays.get(String.valueOf(findMarketCategoryListAndNamesBO.getId())).get(0).getText()))) {
                                log.info("::{}::,玩法国际化为空:{}", TradeVerificationService.getRequestIdStatic(), String.valueOf(findMarketCategoryListAndNamesBO.getId()));
                                continue;
                            }
                            tradingAssignmentSubPlayVo.setSetNames(JSONObject.parseObject(plays.get(String.valueOf(findMarketCategoryListAndNamesBO.getId())).get(0).getText()));
                            objects.add(tradingAssignmentSubPlayVo);
                        }
                    }
                }
                tradingAssignmentVo.setSysTraderWeightList(objects);
                tradingAssignmentVos.add(tradingAssignmentVo);
            }
            //对玩法补充权重
            for (TradingAssignmentVo tradingAssignmentVo : tradingAssignmentVos) {
                tradingAssignmentVo.setMarketType(dto.getMarketType());
                List<TradingAssignmentSubPlayVo> sysTraderWeightList1 = tradingAssignmentVo.getSysTraderWeightList();
                for (TradingAssignmentSubPlayVo tradingAssignmentSubPlayVo : sysTraderWeightList1) {
                    tradingAssignmentSubPlayVo.setSetNames(JSONObject.parseObject(plays.get(String.valueOf(tradingAssignmentSubPlayVo.getTypeId())).get(0).getText()));
                    List<RcsCategorySetTraderWeight> sysTraderWeightList = tradingAssignmentSubPlayVo.getSysTraderWeightList();
                    if (CollectionUtils.isEmpty(sysTraderWeightList)) {
                        List<RcsCategorySetTraderWeight> initRcsCategorySetTraderWeight = new ArrayList<>();
                        RcsCategorySetTraderWeight rcsCategorySetTraderWeight = new RcsCategorySetTraderWeight();
                        rcsCategorySetTraderWeight.setMatchId(dto.getMatchId());
                        rcsCategorySetTraderWeight.setMarketType(dto.getMarketType());
                        rcsCategorySetTraderWeight.setTypeId(tradingAssignmentSubPlayVo.getTypeId());
                        rcsCategorySetTraderWeight.setSetNo(tradingAssignmentVo.getSetNo());
                        rcsCategorySetTraderWeight.setSportId(dto.getSportId());
                        rcsCategorySetTraderWeight.setWeight(100);
                        if (0 == dto.getMarketType()) {
                            rcsCategorySetTraderWeight.setTraderId(StringUtils.isBlank(sellBean.getLiveTraderId()) ? null : Long.valueOf(sellBean.getLiveTraderId()));
                            rcsCategorySetTraderWeight.setTraderCode(sellBean.getLiveTrader());
                        } else if (1 == dto.getMarketType()) {
                            rcsCategorySetTraderWeight.setTraderId(StringUtils.isBlank(sellBean.getPreTraderId()) ? null : Long.valueOf(sellBean.getPreTraderId()));
                            rcsCategorySetTraderWeight.setTraderCode(sellBean.getPreTrader());
                        }
                        initRcsCategorySetTraderWeight.add(rcsCategorySetTraderWeight);
                        tradingAssignmentSubPlayVo.setSysTraderWeightList(initRcsCategorySetTraderWeight);
                    }
                }
            }
            return HttpResponse.success(tradingAssignmentVos);
        } catch (RcsServiceException e) {
            log.error("::{}::获取权重：{}", CommonUtil.getRequestId(dto.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::获取权重:{}", CommonUtil.getRequestId(dto.getMatchId()), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }

    /**
     * 设置权重
     *
     * @param tradingAssignmentVos
     * @return
     */
    @RequestMapping(value = "/setWeights", method = RequestMethod.POST)
    public HttpResponse setWeights(@RequestBody List<TradingAssignmentVo> tradingAssignmentVos) {
        try {
            rcsTradingAssignmentService.setWeights(tradingAssignmentVos);
            return HttpResponse.success();
        } catch (RcsServiceException e) {
            log.error("::{}::设置权重:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(e.getCode(), e.getErrorMassage());
        } catch (Exception e) {
            log.error("::{}::设置权重:{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.error(-1, "服务器出问题");
        }
    }


    /**
     * 权重ve据转成
     *
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    void oldDataToNewData(TradingAssignmentVo dto) {
        QueryWrapper<RcsCategorySetTraderWeight> weightQueryWrapper = new QueryWrapper<>();
        weightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getMatchId, dto.getMatchId());
        weightQueryWrapper.lambda().eq(RcsCategorySetTraderWeight::getVersion, 0);
        List<RcsCategorySetTraderWeight> rcsCategorySetTraderWeights = rcsCategorySetTraderWeightService.list(weightQueryWrapper);
        if (CollectionUtils.isEmpty(rcsCategorySetTraderWeights)) {
            return;
        }
        log.info("::{}::,getWeights0:{}", TradeVerificationService.getRequestIdStatic(), JSONObject.toJSONString(rcsCategorySetTraderWeights));
        Map<String, List<RcsCategorySetTraderWeight>> sets = rcsCategorySetTraderWeights.stream().collect(Collectors.groupingBy(e -> e.getMarketType() + "_" + e.getTypeId()));
        Map<String, List<RcsCategorySetTraderWeight>> marketTypes = rcsCategorySetTraderWeights.stream().collect(Collectors.groupingBy(e -> String.valueOf(e.getMarketType())));
        ArrayList<RcsCategorySetTraderWeight> list = new ArrayList<>();
        for (String mt : marketTypes.keySet()) {
            List<RcsCategorySetTraderWeight> rcsCategorySetTraderWeights1 = marketTypes.get(mt);
            if (CollectionUtils.isEmpty(rcsCategorySetTraderWeights1)) {
                continue;
            }
            List<Long> setIds = rcsCategorySetTraderWeights1.stream().map(e -> e.getTypeId()).collect(Collectors.toList());
            List<FindMarketCategoryListAndNamesBO> marketCategoryListAndNames = rcsMarketCategorySetMapper.findMarketCategoryListAndNames(dto.getSportId(), setIds);
            log.info("::{}::,{},getWeights01:{}", TradeVerificationService.getRequestIdStatic(), mt, JSONObject.toJSONString(marketCategoryListAndNames));
            if (CollectionUtils.isEmpty(marketCategoryListAndNames)) {
                continue;
            }
            for (FindMarketCategoryListAndNamesBO marketCategoryListAndName : marketCategoryListAndNames) {
                RcsCategorySetTraderWeight rcsCategorySetTraderWeight = new RcsCategorySetTraderWeight();
                rcsCategorySetTraderWeight.setSetNo(marketCategoryListAndName.getMarketCategorySetId());
                rcsCategorySetTraderWeight.setMatchId(dto.getMatchId());
                List<RcsCategorySetTraderWeight> setWeight = sets.get(mt + "_" + marketCategoryListAndName.getMarketCategorySetId());
                rcsCategorySetTraderWeight.setTraderId(setWeight.get(0).getTraderId());
                rcsCategorySetTraderWeight.setWeight(setWeight.get(0).getWeight());
                rcsCategorySetTraderWeight.setTraderCode(setWeight.get(0).getTraderCode());
                rcsCategorySetTraderWeight.setMarketType(Integer.valueOf(mt));
                rcsCategorySetTraderWeight.setSportId(dto.getSportId());
                rcsCategorySetTraderWeight.setVersion(1);
                rcsCategorySetTraderWeight.setTypeId(marketCategoryListAndName.getId());
                list.add(rcsCategorySetTraderWeight);
            }
        }
        rcsCategorySetTraderWeightService.remove(weightQueryWrapper);
        rcsCategorySetTraderWeightService.batchInsertOrUpdate(list);
    }
}
