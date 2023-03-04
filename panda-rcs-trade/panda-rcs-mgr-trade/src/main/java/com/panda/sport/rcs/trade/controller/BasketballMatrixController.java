package com.panda.sport.rcs.trade.controller;

import com.panda.sport.rcs.mapper.RcsOrderBasketballMatrixMapper;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.pojo.MatchStatisticsInfoDetail;
import com.panda.sport.rcs.pojo.RcsFirstMarket;
import com.panda.sport.rcs.pojo.RcsOrderBasketballMatrix;
import com.panda.sport.rcs.trade.enums.TheirTimeEnum;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.trade.vo.BasketballMatrixDataVo;
import com.panda.sport.rcs.trade.vo.BasketballMatrixVo;
import com.panda.sport.rcs.trade.vo.MatrixDataVo;
import com.panda.sport.rcs.trade.vo.MedianVo;
import com.panda.sport.rcs.vo.HttpResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @program: xindaima
 * @description: 篮球矩阵
 * @author: kimi
 * @create: 2021-01-13 15:19
 **/
@RestController
@RequestMapping(value = "/basketballMatrix")
@Slf4j
@Component
public class BasketballMatrixController {
    @Autowired
    private RcsOrderBasketballMatrixMapper rcsOrderBasketballMatrixMapper;
    @Autowired
    private MatchStatisticsInfoDetailMapper detailMapper;
    /**
     * 矩阵数据库存储大小
     */
    private static final int size=50;
    /**
     * 获取赛事的中值
     *
     * @param matchId
     */
    @RequestMapping(value = "/getMedian", method = RequestMethod.GET)
    public HttpResponse getMedian(Integer matchId) {
        try {
            List<MedianVo> medianVoList = new ArrayList<>();
            List<RcsFirstMarket> median = rcsOrderBasketballMatrixMapper.getMedian(matchId);
            if (!CollectionUtils.isEmpty(median)) {
                for (TheirTimeEnum theirTimeEnum : TheirTimeEnum.values()) {
                    MedianVo medianVo = new MedianVo();
                    medianVo.setTheirTime(theirTimeEnum.getValue());
                    for (RcsFirstMarket rcsFirstMarket : median) {
                        Double value = Double.parseDouble(rcsFirstMarket.getValue());
                        if (rcsFirstMarket.getPlayId() == 38) {
                            medianVo.setTotal((int) (value * theirTimeEnum.getProportion()));
                        } else {
                            medianVo.setDifferential((int) (value * theirTimeEnum.getProportion()*-1));
                        }
                    }
                    medianVoList.add(medianVo);
                }
            }
            return HttpResponse.success(medianVoList);
        } catch (Exception e) {
            log.error("::{}::获取赛事的中值:{}", CommonUtil.getRequestId(matchId), e.getMessage(), e);
            return HttpResponse.failToMsg("服务器故障");
        }
    }

    /**
     * 获取篮球矩阵
     *
     * @param basketballMatrixVo
     * @return
     */
    @RequestMapping(value = "/getList", method = RequestMethod.POST)
    public HttpResponse getList(@RequestBody BasketballMatrixVo basketballMatrixVo) {
        List<BasketballMatrixDataVo> basketballMatrixDataVoList = basketballMatrixVo.getBasketballMatrixDataVoList();
        if (!CollectionUtils.isEmpty(basketballMatrixDataVoList)) {
            List<MatchStatisticsInfoDetail> matchStatisticsInfoDetail = detailMapper.selectScoreTotal(basketballMatrixDataVoList.get(0).getMatchId());
            for (BasketballMatrixDataVo basketballMatrixDataVo : basketballMatrixDataVoList) {
                List<RcsOrderBasketballMatrix> rcsOrderBasketballMatrices;
                List<Integer> playIdList = basketballMatrixDataVo.getPlayIdList();
                List<Long> merchantIdList = basketballMatrixDataVo.getMerchantIdList();
                List<Integer> settlement = basketballMatrixDataVo.getSettlement();
                List<Integer> matchType = basketballMatrixDataVo.getMatchType();
                if (CollectionUtils.isEmpty(matchType) || CollectionUtils.isEmpty(settlement) || CollectionUtils.isEmpty(merchantIdList) || CollectionUtils.isEmpty(playIdList)){
                    rcsOrderBasketballMatrices=new ArrayList<>();
                }else {
                    rcsOrderBasketballMatrices   = rcsOrderBasketballMatrixMapper.selectRcsOrderBasketballMatrix(basketballMatrixDataVo.getMatchId(), playIdList, merchantIdList, matchType, settlement);
                }
                HashMap<Integer,Double> totalHashmap=new HashMap<>();
                HashMap<Integer,Double> halfTotalHashmap=new HashMap<>();
                Integer unit = basketballMatrixDataVo.getUnit();
                if (unit==null){
                    unit=1;
                }
                Integer total =(int) (getTotal(basketballMatrixDataVo.getMatchId()) * TheirTimeEnum.getTheirTimeEnumByValue(basketballMatrixDataVo.getTheirTime()).getProportion());
                Integer differential = basketballMatrixDataVo.getDifferential();
                if (differential==null){
                    differential =-(int) (getHalfTotal(basketballMatrixDataVo.getMatchId()) * TheirTimeEnum.getTheirTimeEnumByValue(basketballMatrixDataVo.getTheirTime()).getProportion());
                    basketballMatrixDataVo.setDifferential(differential);
                }
                Integer totalScore = basketballMatrixDataVo.getTotalScore();
                if (totalScore==null){
                    totalScore=total;
                    basketballMatrixDataVo.setTotalScore(totalScore);
                }
                List<MatrixDataVo> newTotalHashmap=new ArrayList<>();
                List<MatrixDataVo> newHalfTotalHashmap=new ArrayList<>();
                if (!CollectionUtils.isEmpty(rcsOrderBasketballMatrices)) {
                    for (RcsOrderBasketballMatrix rcsOrderBasketballMatrix : rcsOrderBasketballMatrices) {
                        if (rcsOrderBasketballMatrix.getInitMarket().intValue()==total){
                            processingMatrix(rcsOrderBasketballMatrix.getRecVal(),totalHashmap,rcsOrderBasketballMatrix.getInitMarket().intValue(),unit);
                        }else {
                            processingMatrix(rcsOrderBasketballMatrix.getRecVal(),halfTotalHashmap,rcsOrderBasketballMatrix.getInitMarket().intValue(),unit);
                        }
                    }
                }
                Integer column = basketballMatrixDataVo.getColumn();
                if (column==null){
                    column=21;
                }
                column=column/2;
                for (int x=differential-column;x<=differential+column;x++){
                    Double aDouble = halfTotalHashmap.get(x);
                    if (aDouble==null){
                        aDouble=0.0;
                    }
                    newHalfTotalHashmap.add(new MatrixDataVo(x,aDouble.intValue()));
                }
                for (int x=totalScore-column;x<=column+totalScore;x++){
                    Double aDouble = totalHashmap.get(x);
                    if (aDouble==null){
                        aDouble=0.0;
                    }
                    newTotalHashmap.add(new MatrixDataVo(x,aDouble.intValue()));
                }
                basketballMatrixDataVo.setPlayIdList(null);
                basketballMatrixDataVo.setMerchantIdList(null);
                basketballMatrixDataVo.setNewHalfTotalHashmap(newHalfTotalHashmap);
                basketballMatrixDataVo.setNewTotalHashmap(newTotalHashmap);
                MatchStatisticsInfoDetail matchStatisticsInfoDetail1 = getMatchStatisticsInfoDetail(matchStatisticsInfoDetail, basketballMatrixDataVo.getTheirTime());
                Integer currentTotalScore=0;
                Integer currentDifferential=0;
                if (matchStatisticsInfoDetail1!=null){
                    Integer t1 = matchStatisticsInfoDetail1.getT1();
                    Integer t2 = matchStatisticsInfoDetail1.getT2();
                    if (t1==null){
                        t1=0;
                    }
                    if (t2==null){
                        t2=0;
                    }
                    currentTotalScore=t1+t2;
                    currentDifferential=t1-t2;
                }
                basketballMatrixDataVo.setCurrentTotalScore(currentTotalScore);
                basketballMatrixDataVo.setCurrentDifferential(currentDifferential);
            }
        }
      HashMap<Integer,BasketballMatrixDataVo> hashMap=new HashMap<>();
      if (!CollectionUtils.isEmpty(basketballMatrixDataVoList)){
          for (BasketballMatrixDataVo basketballMatrixDataVo:basketballMatrixDataVoList){
              hashMap.put(basketballMatrixDataVo.getTheirTime(),basketballMatrixDataVo);
          }
      }
        return HttpResponse.success(hashMap);
    }

    private MatchStatisticsInfoDetail getMatchStatisticsInfoDetail(List<MatchStatisticsInfoDetail> matchStatisticsInfoDetailList,Integer theirTime){
        if (CollectionUtils.isEmpty(matchStatisticsInfoDetailList)){
            return null;
        }
        TheirTimeEnum theirTimeEnumByValue = TheirTimeEnum.getTheirTimeEnumByValue(theirTime);
        for (MatchStatisticsInfoDetail matchStatisticsInfoDetail:matchStatisticsInfoDetailList){
            if (theirTimeEnumByValue.getCode().equals(matchStatisticsInfoDetail.getCode()) && theirTimeEnumByValue.getFirstNum().equals(matchStatisticsInfoDetail.getFirstNum())){
                return matchStatisticsInfoDetail;
            }
        }
        return null;
    }



    private void  processingMatrix(String recVal,HashMap<Integer,Double> hashMap,Integer initMarket,Integer unit){
        String[] split = recVal.split(",");
        for (int x=0;x<100;x++){
            int i = initMarket + x - 50;
            if (!hashMap.containsKey(i)){
                hashMap.put(i,Double.valueOf(split[x])/unit);
            }else {
                hashMap.put(i,hashMap.get(i)+Double.valueOf(split[x])/unit);
            }
        }
    }


    /**
     *
     * @param matchId
     * @return
     */
    private Integer getTotal(Integer matchId){
        //赛事总分中值
        HashMap<Integer,Double> matchtotalHashmap=new HashMap<>();
        if (!matchtotalHashmap.containsKey(matchId)) {
            List<RcsFirstMarket> median = rcsOrderBasketballMatrixMapper.getMedian(matchId);
            if (!CollectionUtils.isEmpty(median)) {
                for (RcsFirstMarket rcsFirstMarket : median) {
                    if (rcsFirstMarket.getPlayId().intValue() == 38) {
                        matchtotalHashmap.put(matchId, Double.parseDouble(rcsFirstMarket.getValue()));
                    }else {
                        //matchhalftotalHashmap.put(matchId, Double.parseDouble(rcsFirstMarket.getValue()));
                    }
                }
            }else {
                return 0;
            }
        }
        if (!matchtotalHashmap.containsKey(matchId)){
            return 0;
        }
        return matchtotalHashmap.get(matchId).intValue();
    }

    private Integer getHalfTotal(Integer matchId){
        //赛事总分查值
        HashMap<Integer,Double> matchhalftotalHashmap=new HashMap<>();
        if (!matchhalftotalHashmap.containsKey(matchId)) {
            List<RcsFirstMarket> median = rcsOrderBasketballMatrixMapper.getMedian(matchId);
            if (!CollectionUtils.isEmpty(median)) {
                for (RcsFirstMarket rcsFirstMarket : median) {
                    if (rcsFirstMarket.getPlayId().intValue() == 39) {
                        matchhalftotalHashmap.put(matchId, Double.parseDouble(rcsFirstMarket.getValue()));
                    }else {
                        //matchtotalHashmap.put(matchId, Double.parseDouble(rcsFirstMarket.getValue()));
                    }
                }
            }else {
                return 0;
            }
        }
        if (!matchhalftotalHashmap.containsKey(matchId)){
            return 0;
        }
        return matchhalftotalHashmap.get(matchId).intValue();
    }
}
