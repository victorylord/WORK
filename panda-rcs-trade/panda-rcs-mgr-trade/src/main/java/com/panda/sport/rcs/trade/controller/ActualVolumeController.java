package com.panda.sport.rcs.trade.controller;

import com.alibaba.fastjson.JSONObject;
import com.panda.merge.common.utils.ListUtils;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.mapper.RcsMatchProfitMapper;
import com.panda.sport.rcs.mapper.RcsPredictBetStatisMapper;
import com.panda.sport.rcs.mapper.StandardMatchInfoMapper;
import com.panda.sport.rcs.pojo.StandardMatchInfo;
import com.panda.sport.rcs.trade.enums.PlayIdsEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.ao.ActualVolumeAO;
import com.panda.sport.rcs.vo.HttpResponse;
import com.panda.sport.rcs.vo.RcsMatchProfitVo;
import com.panda.sport.rcs.vo.statistics.ActualVolumeVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.common.utils.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.mgr.controller
 * @Description :  实货量接口
 * @Date: 2020-10-07 15:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Deprecated
@RestController
@RequestMapping(value = "/actualVolume/Deprecated")
@Slf4j
public class ActualVolumeController {
    @Autowired
    private RcsPredictBetStatisMapper rcsPredictBetStatisMapper;
    @Autowired
    private StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private RcsMatchProfitMapper rcsMatchProfitMapper;

    @Autowired
    private RedisClient redisClient;

    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    public HttpResponse<HashMap<Integer, HashMap<Integer, List<ActualVolumeVO>>>> getList(@RequestBody ActualVolumeAO actualVolumeAO) {
        try {
            Integer matchId = actualVolumeAO.getMatchId();
            if (matchId == null) {
                return HttpResponse.error(HttpResponse.FAIL, "赛事id不能为空");
            }
            List<Integer> playTimeStages = actualVolumeAO.getPlayTimeStages();
            if (CollectionUtils.isEmpty(playTimeStages)) {
                return HttpResponse.error(HttpResponse.FAIL, "赛事赛节不能为空");
            }
            Integer matchType = actualVolumeAO.getMatchType();
            if (matchType == null) {
                return HttpResponse.error(HttpResponse.FAIL, "赛事阶段不能为空");
            }
            StandardMatchInfo standardMatchInfo = standardMatchInfoMapper.selectById(matchId);
            Long sportId = standardMatchInfo.getSportId();
            Integer matchStatus = standardMatchInfo.getMatchStatus();
            List<Integer> playIds = PlayIdsEnum.getPlayIds(playTimeStages);
            List<ActualVolumeVO> actualVolumeVOList = rcsPredictBetStatisMapper.getRcsPredictBetStatisVo(matchId, playIds, matchType);
            //过滤为0的数据
            actualVolumeVOList = actualVolumeVOList.stream().filter(e -> e.getBetNum() > 0).collect(Collectors.toList());
            HashMap<Integer, HashMap<Integer, List<ActualVolumeVO>>> map = new HashMap<>();
            if (!CollectionUtils.isEmpty(actualVolumeVOList)) {
                HashMap<String, List<ActualVolumeVO>> hashMap = new HashMap<>();
                for (ActualVolumeVO actualVolumeVO : actualVolumeVOList) {
                    if (actualVolumeVO.getOddsItem().equals("Over")) {
                        actualVolumeVO.setOddsItem("1");
                    } else if (actualVolumeVO.getOddsItem().equals("Under")) {
                        actualVolumeVO.setOddsItem("2");
                    }
                    List<ActualVolumeVO> actualVolumeVOS = hashMap.get(actualVolumeVO.getMarketId());
                    if (CollectionUtils.isEmpty(actualVolumeVOS)) {
                        actualVolumeVOS = new ArrayList<>();
                        hashMap.put(actualVolumeVO.getMarketId(), actualVolumeVOS);
                    }
                    actualVolumeVOS.add(actualVolumeVO);
                }
                //根据盘口添加一组数据
                HashMap<String,Double> betAmountHashMap=new HashMap<>();
                HashMap<String,Double> betAmountPayHashMap=new HashMap<>();
                HashMap<String,Double> betAmountComplexHashMap=new HashMap<>();
                for (List<ActualVolumeVO> list : hashMap.values()) {
                    if (list.size() == 1) {
                        ActualVolumeVO actualVolumeVO = list.get(0);
                        ActualVolumeVO copyActualVolumeVO = new ActualVolumeVO();
                        //BeanCopyUtils.copyProperties(actualVolumeVO, copyActualVolumeVO);
                        BeanUtils.copyProperties(actualVolumeVO, copyActualVolumeVO);
                        copyActualVolumeVO.setOddsItem(String.valueOf(3 - Integer.parseInt(actualVolumeVO.getOddsItem())));
                        copyActualVolumeVO.setBetNum(0L);
                        copyActualVolumeVO.setBetAmount(BigDecimal.ZERO);
                        copyActualVolumeVO.setBetAmountComplex(BigDecimal.ZERO);
                        copyActualVolumeVO.setBetAmountPay(BigDecimal.ZERO);
                        list.add(copyActualVolumeVO);
                    }
                    for (ActualVolumeVO actualVolumeVO:list) {
                        Double betAmountALong = betAmountHashMap.get(actualVolumeVO.getMarketId());
                        Double betAmountPayALong = betAmountPayHashMap.get(actualVolumeVO.getMarketId());
                        Double betAmountComplexALong = betAmountComplexHashMap.get(actualVolumeVO.getMarketId());
                        if (betAmountALong == null) {
                            betAmountALong = 0.0;
                        }
                        if (betAmountPayALong == null) {
                            betAmountPayALong = 0.0;
                        }
                        if (betAmountComplexALong == null) {
                            betAmountComplexALong = 0.0;
                        }
                        if (actualVolumeVO.getOddsItem().equals("1")) {
                            betAmountHashMap.put(actualVolumeVO.getMarketId(), betAmountALong + actualVolumeVO.getBetAmount().doubleValue());
                            betAmountPayHashMap.put(actualVolumeVO.getMarketId(), betAmountPayALong + actualVolumeVO.getBetAmountPay().doubleValue());
                            betAmountComplexHashMap.put(actualVolumeVO.getMarketId(), betAmountComplexALong + actualVolumeVO.getBetAmountComplex().doubleValue());
                        }else {
                            betAmountHashMap.put(actualVolumeVO.getMarketId(), betAmountALong - actualVolumeVO.getBetAmount().doubleValue());
                            betAmountPayHashMap.put(actualVolumeVO.getMarketId(), betAmountPayALong - actualVolumeVO.getBetAmountPay().doubleValue());
                            betAmountComplexHashMap.put(actualVolumeVO.getMarketId(), betAmountComplexALong - actualVolumeVO.getBetAmountComplex().doubleValue());
                        }
                    }
                }
                //遍历放进去
                for (List<ActualVolumeVO> list : hashMap.values()) {
                    ActualVolumeVO actualVolumeVO = list.get(0);
                    PlayIdsEnum playIdsEnum = PlayIdsEnum.getPlayIdsEnum(Integer.valueOf(actualVolumeVO.getSubPlayId()));
                    Integer playTimeStage = playIdsEnum.getPlayTimeStage();
                    HashMap<Integer, List<ActualVolumeVO>> integerListHashMap = map.get(playTimeStage);
                    if (CollectionUtils.isEmptyMap(integerListHashMap)) {
                        integerListHashMap = new HashMap<>();
                    }
                    List<ActualVolumeVO> list1 = integerListHashMap.get(Integer.valueOf(actualVolumeVO.getSubPlayId()));
                    if (CollectionUtils.isEmpty(list1)){
                        list1=new ArrayList<>();
                    }
                    list1.addAll(list);
                    double betAmountADouble = betAmountHashMap.get(list.get(0).getMarketId());
                    double betAmountPayADouble = betAmountPayHashMap.get(list.get(0).getMarketId());
                    double betAmountComplexADouble = betAmountComplexHashMap.get(list.get(0).getMarketId());
                    for (ActualVolumeVO actualVolumeVO1:list){
                        if (actualVolumeVO1.getOddsItem().equals("1") && betAmountADouble>=0) {
                            actualVolumeVO1.setMarketBetAmount(BigDecimal.valueOf(betAmountADouble));
                        }else if (actualVolumeVO1.getOddsItem().equals("2") && betAmountADouble<0){
                            actualVolumeVO1.setMarketBetAmount(BigDecimal.valueOf(-betAmountADouble));
                        }
                        if (actualVolumeVO1.getOddsItem().equals("1") && betAmountPayADouble>=0) {
                            actualVolumeVO1.setMarketBetAmountPay(BigDecimal.valueOf(betAmountPayADouble));
                        }else if (actualVolumeVO1.getOddsItem().equals("2") && betAmountPayADouble<0){
                            actualVolumeVO1.setMarketBetAmountPay(BigDecimal.valueOf(-betAmountPayADouble));
                        }
                        if (actualVolumeVO1.getOddsItem().equals("1") && betAmountComplexADouble>=0) {
                            actualVolumeVO1.setMarketBetAmountComplex(BigDecimal.valueOf(betAmountComplexADouble));
                        }else if (actualVolumeVO1.getOddsItem().equals("2") && betAmountComplexADouble<0){
                            actualVolumeVO1.setMarketBetAmountComplex(BigDecimal.valueOf(-betAmountComplexADouble));
                        }
                    }
                    if(SportIdEnum.isPingpong(sportId)){
                        //如果是乒乓球第X局总分玩法则进行降序排序
                        actualVolumeVO.setSort(actualVolumeVO.getMarketValueComplete());
                        list.get(1).setSort(list.get(1).getMarketValueComplete());
                        if(actualVolumeVO.getPlayId().equals(177)){
                            ListUtils.sort(list1, false, "sort");
                        }
                    }
                    integerListHashMap.put(Integer.valueOf(actualVolumeVO.getSubPlayId()), list1);
                    map.put(playTimeStage, integerListHashMap);
                }
                //如果是结束了的算盈利
                if (matchStatus == 3) {
                    dealingWithprofits(map, matchId, matchType, playIds);
                }
            }
            HashMap<Object, Object> returnHashMap = new HashMap<>(map);
            returnHashMap.put("matchLength", standardMatchInfo.getMatchLength() == null ? 0 : standardMatchInfo.getMatchLength());
            if (matchStatus == 3) {
                BigDecimal bigDecimal = rcsMatchProfitMapper.selectRcsMatchProfitByMatchId(matchId);
                returnHashMap.put("profitAmountNum", bigDecimal);
            }
            returnHashMap.put("matchStatus", matchStatus);
            return HttpResponse.success(returnHashMap);
        } catch (Exception e) {
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }

    /**
     * 方法废弃
     *
     * @param matchId
     * @param playIds
     * @param matchType
     * @return
     */
    @Deprecated
    private List<ActualVolumeVO> getRcsPredictBetStatisVo(Integer matchId, List<Integer> playIds, Integer matchType) {
        List<ActualVolumeVO> actualVolumeVOList = rcsPredictBetStatisMapper.getRcsPredictBetStatisVo(matchId, playIds, matchType);
        Map<String, List<ActualVolumeVO>> collect = actualVolumeVOList.stream().collect(Collectors.groupingBy(ActualVolumeVO::getMarketId));
        collect.forEach((key, item) -> {
            if (item.size() == 1) {
                ActualVolumeVO actualVolumeVO = item.get(0);
                ActualVolumeVO copyActualVolumeVO = new ActualVolumeVO();
                BeanUtils.copyProperties(actualVolumeVO, copyActualVolumeVO);
                switch (copyActualVolumeVO.getOddsItem()) {
                    case "Over":
                        copyActualVolumeVO.setOddsItem("Under");
                        break;
                    case "Under":
                        copyActualVolumeVO.setOddsItem("Over");
                        break;
                    case "1":
                        copyActualVolumeVO.setOddsItem("2");
                        break;
                    case "2":
                        copyActualVolumeVO.setOddsItem("1");
                        break;
                }
                copyActualVolumeVO.setBetNum(0L);
                copyActualVolumeVO.setBetAmount(BigDecimal.ZERO);
                copyActualVolumeVO.setBetAmountComplex(BigDecimal.ZERO);
                copyActualVolumeVO.setBetAmountPay(BigDecimal.ZERO);
                actualVolumeVOList.add(copyActualVolumeVO);
            }
        });
        actualVolumeVOList.forEach(item -> {
            String betStatisKey = "rcs:risk:predict:betSatis.match_id.%s.match_type.%s.play_id.%s.sub_play_id.%s.market_id.%s.odds_item.%s.bet_score.%s";
            betStatisKey = String.format(betStatisKey, item.getMatchId(), item.getMatchType(), item.getPlayId(), item.getSubPlayId(), item.getMarketId(), item.getOddsItem(), item.getBetScore());
            Long totalBetNum = redisClient.hincrBy(betStatisKey, "totalBetNum", 0L);
            if (!ObjectUtils.isEmpty(totalBetNum)) {
                item.setBetNum(totalBetNum);
            }
            Long totalBetAmount = redisClient.hincrBy(betStatisKey, "totalBetAmount", 0L);
            if (!ObjectUtils.isEmpty(totalBetAmount)) {
                BigDecimal bigDecimal = new BigDecimal(totalBetAmount).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
                item.setBetAmount(bigDecimal);
            }
            Long totalBetAmountPay = redisClient.hincrBy(betStatisKey, "totalBetAmountPay", 0L);
            if (!ObjectUtils.isEmpty(totalBetAmountPay)) {
                BigDecimal bigDecimal = new BigDecimal(totalBetAmountPay).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
                item.setBetAmountPay(bigDecimal);
            }
            Long totalBetAmountComplex = redisClient.hincrBy(betStatisKey, "totalBetAmountComplex", 0L);
            if (!ObjectUtils.isEmpty(totalBetAmountComplex)) {
                BigDecimal bigDecimal = new BigDecimal(totalBetAmountComplex).divide(new BigDecimal("100"), 2, RoundingMode.DOWN);
                item.setBetAmountComplex(bigDecimal);
            }
        });
        log.info("货量总表数据查询赛事ID：{}，玩法ID：{}，结果集：{}", matchId, JSONObject.toJSONString(playIds), JSONObject.toJSONString(actualVolumeVOList));
        return actualVolumeVOList;
    }

    /**
     * @Description: 处理赛事结束的盈利值
     * @Param: [map, matchId, matchType, playIds]
     * @return: void
     * @Author: KIMI
     * @Date: 2020/12/5
     */
    private void dealingWithprofits(HashMap<Integer, HashMap<Integer, List<ActualVolumeVO>>> map, Integer matchId, Integer matchType, List<Integer> playIds) {
        //赛事结束
        List<RcsMatchProfitVo> rcsMatchProfitVos = rcsMatchProfitMapper.selectRcsMatchProfitByPlayId(matchId, playIds, matchType);
        if (!CollectionUtils.isEmpty(rcsMatchProfitVos)) {
            for (RcsMatchProfitVo rcsMatchProfitVo:rcsMatchProfitVos){
                HashMap<Integer, List<ActualVolumeVO>> integerListHashMap = map.get(PlayIdsEnum.getPlayIdsEnum(rcsMatchProfitVo.getPlayId().intValue()).getPlayTimeStage());
                List<ActualVolumeVO> list = integerListHashMap.get(rcsMatchProfitVo.getPlayId().intValue());
                for (ActualVolumeVO actualVolumeVO:list){
                    BigDecimal profitAmount = rcsMatchProfitVo.getProfitAmount();
                    if (rcsMatchProfitVo.getMarketId().equals(Long.parseLong(actualVolumeVO.getMarketId()))){
                        if (actualVolumeVO.getMarketBetAmount()!=null ) {
                            actualVolumeVO.setProfit(profitAmount);
                        } else {
                            actualVolumeVO.setProfit(null);
                        }
                    }
                }
            }
        }
    }
}
