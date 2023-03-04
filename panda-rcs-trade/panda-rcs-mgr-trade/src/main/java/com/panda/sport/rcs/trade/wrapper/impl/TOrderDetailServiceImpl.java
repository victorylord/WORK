package com.panda.sport.rcs.trade.wrapper.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.panda.sport.rcs.mapper.*;
import com.panda.sport.rcs.pojo.*;
import com.panda.sport.rcs.trade.util.CommonUtil;
import com.panda.sport.rcs.utils.CommonUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.Maps;
import com.panda.sport.data.rcs.dto.OrderBean;
import com.panda.sport.data.rcs.dto.OrderItem;
import com.panda.sport.rcs.constants.BaseConstants;
import com.panda.sport.rcs.core.cache.client.RedisClient;
import com.panda.sport.rcs.exeception.LogicException;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.trade.log.LogContext;
import com.panda.sport.rcs.mapper.statistics.MatchStatisticsInfoDetailMapper;
import com.panda.sport.rcs.trade.util.PlayOptionNameUtil;
import com.panda.sport.rcs.trade.wrapper.ITOrderDetailService;
import com.panda.sport.rcs.trade.wrapper.MarketCategorySetService;
import com.panda.sport.rcs.trade.wrapper.MatchStatisticsInfoService;
import com.panda.sport.rcs.vo.MatrixVo;
import com.panda.sport.rcs.vo.OrderDetailStatReportVo;
import com.panda.sport.rcs.vo.OrderDetailVo;
import com.panda.sport.rcs.vo.PlayVo;
import com.panda.sport.rcs.vo.RequestMarketOrderVo;

import lombok.extern.slf4j.Slf4j;

/**
 * <p>
 * 投注单详细信息表 服务实现类
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-03
 */
@Service
@Slf4j
public class TOrderDetailServiceImpl extends ServiceImpl<TOrderDetailMapper, TOrderDetail> implements ITOrderDetailService {

    @Autowired
    private TOrderDetailMapper orderDetailMapper;
    @Autowired
    MarketCategorySetService marketCategorySetService;
    @Autowired
    private TOrderMapper orderMapper;
    @Autowired
    RcsRectanglePlayMapper rcsRectanglePlayMapper;
    @Autowired
    StandardMatchInfoMapper standardMatchInfoMapper;
    @Autowired
    private ITOrderDetailService itOrderDetailService;
    @Autowired
    private MatchForecastService matchForecastService;

    @Autowired
    private MatchStatisticsInfoService matchStatisticsInfoService;
    @Autowired
    MatchStatisticsInfoDetailMapper matchStatisticsInfoDetailMapper;
    @Autowired
    private RedisClient redisClient;

    @Autowired
    RcsMatrixInfoMapper matrixInfoMapper;

    LRUMap playOptionsNameCache = new LRUMap(1000);
    LRUMap orderNoCache = new LRUMap(100);
    LRUMap matchInfoCache = new LRUMap(500);
    private static  HashMap<Integer,PlayVo> playVoHashMap=new HashMap<>();
//    private static final String ZS = "zs";
//    private static final String EN = "en";
    private static String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
    private final long waitDate = 30*1000;
    private static final String PREFIX_TEAM_NAME = "rcs:ws:team::%s";
    private static final Long THREE_DAY_TIME_MILLIS = 3 * 24 * 60 * 60 *1000L;

    @Override
    public MatrixVo[][] getMatrixInfoData(RcsMatrixInfoReqVo vo, String score) {
        int homeScore = Integer.parseInt(score.split(":")[0]);
        int awayScore = Integer.parseInt(score.split(":")[1]);

        int homeSize = 13;
        int awaySize = 13;
        BigDecimal[][] result = new BigDecimal[homeSize][awaySize];

        QueryWrapper<RcsMatrixInfo> queryWrapper = new QueryWrapper<>();
        //赛事id
        queryWrapper.eq("match_id", vo.getMatchId());
        //矩阵类型（ 1.全场比分矩阵 2.上半场比分矩阵 3.下半场比分矩阵 4.全场角球矩阵 5.半场角球矩阵 6.全场加时矩阵 7.半场加时矩阵）
        queryWrapper.eq("matrix_type", vo.getMatrixType());
        //玩法类型(多个)
        queryWrapper.in("play_type", vo.getPlayTypes());
        //商户类型（1.现金网 2.信用网）
        queryWrapper.in("business_type", vo.getBusinessTypes());
        //赛事类型（1.早盘 2.滚球）
        queryWrapper.in("match_type", vo.getMatchTypes());
        //提前结算 0、非提前结算 1、提前结算
        queryWrapper.in("early_settlement_type", vo.getEarlySettlementType());


        List<RcsMatrixInfo> rcsMatrixInfos = matrixInfoMapper.selectList(queryWrapper);

        log.info("::{}::数据库中的矩形数据为:{}",CommonUtil.getRequestId(), JSONObject.toJSONString(rcsMatrixInfos));

        for (RcsMatrixInfo rcsMatrixInfo : rcsMatrixInfos) {
            BigDecimal[][] oneRusult = JSON.parseObject(rcsMatrixInfo.getRecVal(), BigDecimal[][].class);
            for (int i = 0; i < homeSize; i++) {
                for (int j = 0; j < awaySize; j++) {
                    try {
                        if(result[i][j] == null){
                            result[i][j] = BigDecimal.ZERO;
                        }
                        //做运算的时候乘以type值（type值是1或者-1，来判断订单是否取消）
                        result[i][j] =  result[i][j].add( Optional.ofNullable(oneRusult[i][j]).orElse(BigDecimal.ZERO) );
                    } catch (Exception e) {
                        log.error("::{}::,矩阵 arr[{}][{}] 转换失败!", CommonUtils.getLinkId(), i, j);
                        log.error("::{}::,矩阵转换失败!,ex:{}", CommonUtils.getLinkId(), e);
                    }
                }
            }
        }

//        RcsMatrixInfo matrixInfo = matrixInfoMapper.selectOne(queryWrapper);
//        result = JSON.parseObject(matrixInfo.getRecVal(), BigDecimal[][].class);
//        String matrixStr = matrixInfo.getRecVal();
//        if(!org.springframework.util.StringUtils.isEmpty(matrixStr)){
//            for(int i=0; i<homeSize; i++){
//                for(int j=0; j<awaySize; j++){
//                    result[i][j] = Optional.ofNullable(result[i][j]).orElse(BigDecimal.ZERO);
//                }
//            }
//        }

        MatrixVo[][] mtx = new MatrixVo[homeSize][awaySize];
        List<Map.Entry<String,BigDecimal>> lowerlist = new ArrayList<Map.Entry<String,BigDecimal>>();
        List<Map.Entry<String,BigDecimal>> greaterlist = new ArrayList<Map.Entry<String,BigDecimal>>();
        sortMatrix(result,homeSize,awaySize,vo.getUnit(),lowerlist,greaterlist);
        getLeveledMatrix(lowerlist,mtx,score);
        getLeveledMatrix(greaterlist,mtx,score);
        return filterMatrix(mtx, homeScore, awayScore, vo.getSize());
    }

    @Override
    public MatrixVo[][] getHalfMatrixByMatchId(TOrderDetail item, Date startTime, Date endTime, List<Long> playIds , List<Long> tenantIds, Integer unit, Integer size) {
        if(item.getMatchId()!=null && item.getMatchId() <= 0) throw new LogicException("60019", "半场矩阵查询matchId不能为空");
        if(unit!=null && unit <= 0) throw new LogicException("60019", "半场矩阵查询单位不合法");
        //if(!StandardSportMarketCategory.firstHalfMatchCategorys.contains(item.getPlayId())) throw new LogicException("60020", "半场矩阵查询玩法ID不合法");
        List<TOrderDetail> list = getMatrixList(item, startTime, endTime,playIds, tenantIds);
        if(list == null) throw new LogicException("60021", "该赛事没有任何注单记录");
        //String score = getCurrentScore(item.getMatchId());
        String score = getHalfScore(item.getMatchId());
        int homeScore = Integer.parseInt(score.split(":")[0]);
        int awayScore = Integer.parseInt(score.split(":")[1]);
//        int homeSize = Optional.ofNullable(size).orElse(10)+Integer.parseInt(score.split(":")[0])+1;
//        int awaySize = Optional.ofNullable(size).orElse(10)+Integer.parseInt(score.split(":")[1])+1;
//        homeSize = homeSize > 13 ? 13 : homeSize;
//        awaySize = awaySize > 13 ? 13 : awaySize;

        int homeSize = 13;
        int awaySize = 13;

        BigDecimal[][] result = new BigDecimal[homeSize][awaySize];
        for(TOrderDetail detail : list){
            //只计算可以用比分矩阵计算的订单
            if(detail.getRecType() != null && detail.getRecType() != 0){
                continue;
            }
            //早盘已结算的不用比分矩阵
            if(detail.getIsSettlement() != null && detail.getIsSettlement() == 1 && detail.getMatchType() == 1){
                continue;
            }
            String matrixStr = detail.getRecVal();
            BigDecimal[][] arr ;
            try {
                arr = JSON.parseObject(matrixStr, BigDecimal[][].class);
            } catch (Exception e) {
                log.error("{},当前注单存入矩阵数据有误：betNo: {}",this.getClass(),detail.getBetNo());
                continue;
            }

            BigDecimal volumePercentage = detail.getVolumePercentage();
            if(volumePercentage==null){
                volumePercentage = BigDecimal.valueOf(1);
            }
            if(volumePercentage.compareTo(BigDecimal.valueOf(1))!=0){
                log.info("::{}::订单:{}:货量百分比{}",CommonUtil.getRequestId(), detail.getOrderNo(), detail.getVolumePercentage());
            }
            for(int i=0; i<homeSize; i++){
                for(int j=0; j<awaySize; j++){
                    result[i][j] = Optional.ofNullable(result[i][j]).orElse(BigDecimal.ZERO) .add( Optional.ofNullable(arr[i][j].multiply(volumePercentage)).orElse(BigDecimal.ZERO));
                }
            }
        }
        MatrixVo[][] mtx = new MatrixVo[homeSize][awaySize];
        List<Map.Entry<String,BigDecimal>> lowerlist = new ArrayList<Map.Entry<String,BigDecimal>>();
        List<Map.Entry<String,BigDecimal>> greaterlist = new ArrayList<Map.Entry<String,BigDecimal>>();
        sortMatrix(result,homeSize,awaySize,unit,lowerlist,greaterlist);
        getLeveledMatrix(lowerlist,mtx,score);
        getLeveledMatrix(greaterlist,mtx,score);
        return filterMatrix(mtx, homeScore, awayScore, size);
    }

    private List<TOrderDetail> getMatrixList(TOrderDetail item, Date startTime, Date endTime,List<Long> playIds, List<Long> tenantIds){
        Map<String,Object> map = Maps.newHashMap();
        map.put("matchId",item.getMatchId());
        map.put("playIds",playIds==null || playIds.size()==0?null:playIds);
        map.put("tenantIds",tenantIds==null || tenantIds.size()==0?null:tenantIds);

        if(item.getIsSettlement()!=null) {
            map.put("isSettlement", item.getIsSettlement());
        }

        map.put("matchType",item.getMatchType());
        map.put("uid",item.getUid());
        //map.put("beginTime", Optional.ofNullable(startTime).orElse(DateUtils.dayBegin(Calendar.getInstance().getTime())).getTime());
        //map.put("endTime", Optional.ofNullable(endTime).orElse(DateUtils.dayEnd(Calendar.getInstance().getTime())).getTime());
        return orderDetailMapper.getMatrixValList(map);
    }

    private void sortMatrix(BigDecimal[][] result,int homeSize,int awaySize,int unit,List<Map.Entry<String,BigDecimal>> lowerlist,List<Map.Entry<String,BigDecimal>> greaterlist){
        Map<String,BigDecimal> lowermap = Maps.newTreeMap();
        Map<String,BigDecimal> greatermap = Maps.newTreeMap();
        for(int i=0; i<homeSize; i++){
            for(int j=0; j<awaySize; j++){
                BigDecimal cur = Optional.ofNullable(result[i][j]).orElse(BigDecimal.ZERO);
                if(cur.doubleValue() < 0) lowermap.put(i+","+j,cur.divide(new BigDecimal(unit),2,BigDecimal.ROUND_HALF_UP));
                else greatermap.put(i+","+j,cur.divide(new BigDecimal(unit),2,BigDecimal.ROUND_HALF_UP));
            }
        }
        // map转换成list进行排序
        List<Map.Entry<String,BigDecimal>> lowlist = new ArrayList<Map.Entry<String,BigDecimal>>(lowermap.entrySet());
        List<Map.Entry<String,BigDecimal>> greatlist = new ArrayList<Map.Entry<String,BigDecimal>>(greatermap.entrySet());
        Collections.sort(lowlist, new Comparator<Map.Entry<String,BigDecimal>>() {
            @Override
            public int compare(Map.Entry<String, BigDecimal> o1 , Map.Entry<String, BigDecimal> o2) {
                return  o1.getValue().compareTo(o2.getValue());
//                return (int)(o1.getValue().longValue() - o2.getValue().longValue());
            }
        });
        Collections.sort(greatlist, new Comparator<Map.Entry<String,BigDecimal>>() {
            @Override
            public int compare(Map.Entry<String, BigDecimal> o1 , Map.Entry<String, BigDecimal> o2) {
                return  o2.getValue().compareTo(o1.getValue());
//                return (int)(o1.getValue().longValue() - o2.getValue().longValue());
            }
        });
        lowerlist.addAll(lowlist);
        greaterlist.addAll(greatlist);
    }

    private void getLeveledMatrix(List<Map.Entry<String, BigDecimal>> list,MatrixVo[][] mtx,String score){
    	if(list == null || list.size() <= 0 ) return;
        //MatrixVo[][] mtx = new MatrixVo[homeSize][awaySize];
        int k = 0,dept=list.size();
        int secondDept = (int)(dept*0.05);
        int thirdDept = (int)(dept*0.2);
        int forthDept = (int)(dept*0.5);
        String[] scoreArr = score.split(":");
        Integer homeScore = Integer.parseInt(scoreArr[0]);
        Integer awayScore = Integer.parseInt(scoreArr[1]);
        BigDecimal min = BigDecimal.ZERO;
        BigDecimal lastVal = list.get(0).getValue();
        MatrixVo lastVo = null;
        for(Map.Entry<String, BigDecimal> entity : list){
            String key = entity.getKey();
            BigDecimal value = entity.getValue().abs();
            String[] keyArr = key.split(",");
            int i = Integer.parseInt(keyArr[0]);
            int j = Integer.parseInt(keyArr[1]);
            MatrixVo vo;
            if((homeScore> 0 && i < homeScore) || (awayScore > 0 && j < awayScore)){
                vo = getMatrixVo(0,BigDecimal.ZERO,Boolean.TRUE);
            }else{
                if(value.compareTo(min) >= 0){
                    vo = getMatrixVo(NumberUtils.INTEGER_ONE,entity.getValue(),Boolean.FALSE);
                    min = value;
                }else{
                	if(k != 0 && lastVal.compareTo(value) == 0) {
                		vo = JSONObject.parseObject(JSONObject.toJSONString(lastVo),MatrixVo.class);
                	}else {
                		if(k <= secondDept ){
                            vo =  getMatrixVo(NumberUtils.INTEGER_TWO,entity.getValue(),Boolean.FALSE);
                        }else if(k > secondDept && k <= thirdDept ){
                            vo =  getMatrixVo(Integer.valueOf(3),entity.getValue(),Boolean.FALSE);
                        }else if(k > thirdDept && k <= forthDept ){
                            vo =  getMatrixVo(Integer.valueOf(4),entity.getValue(),Boolean.FALSE);
                        }else{
                            vo =  getMatrixVo(Integer.valueOf(5),entity.getValue(),Boolean.FALSE);
                        }
                	}
                }
            }
            k++;
            mtx[i][j] = vo;

            if(vo.getLevel() != 0 ) {
            	lastVal = value;
                lastVo = vo;
            }
        }
    }

    private MatrixVo getMatrixVo(int level,BigDecimal value,Boolean isOutcome){
        MatrixVo vo = new MatrixVo();
        vo.setLevel(level);
        vo.setValue(value.divide(new BigDecimal(OrderItem.PlUSTIMES),2,BigDecimal.ROUND_HALF_UP));
        vo.setIsOutcome(isOutcome);
        return vo;
    }
    /*@Autowired
    TOrderDetailMapper orderDetailMapper;
    @Override
    public boolean updateOrderDetailAfterRefund(TOrderDetail detail) {
        return orderDetailMapper.updateOrderDetailAfterRefund(detail);
    }*/


    /**
     * @Description  查询玩法管理中 赛事订单对应的比分矩阵
     * 滚球比分结算：所有矩阵进行累加
     * 最终比分结算 ：
     * 结算时的最新比分-获取结算订单信息 对应的单元格值为 结算金额 其它单元格比分为 0
     * @Param [tenantId, matchId, matchType, settleStatus, playIds, unit]
     * @Param tenantId 商户ID
     * @Param matchId 赛事ID
     * @Param matchType  类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘 , 全部 null
     * @Param settleStatus 结算状态 0 未结算 1：已结算  全部 null
     * @Param playIds 玩法ID集合
     * 单位 1 10 100 1000
     * @Author  max
     * @Date  11:25 2019/11/8
     * @return com.panda.sport.rcs.vo.MatrixVo[][] 默认返回 12*12 大小的矩阵
     **/
    @Override
    public MatrixVo[][] queryMatrixByMatchId(List<Long> tenantIds, Long matchId, Integer matchType, Integer settleStatus, List<Long> playIds, Integer unit,Integer size) {
        if(matchId ==null || matchId<= 0)
        {
            throw new LogicException("60019", "matchId不能为空");
        }
        if(unit!=null && unit <= 0)
        {
            throw new LogicException("60019", "查询单位不合法");
        }
        Map<String,Object> map = Maps.newHashMap();
        map.put("matchId",matchId);
        map.put("playIds",playIds==null || playIds.size()==0?null:playIds);
        map.put("tenantIds",tenantIds==null || tenantIds.size()==0?null:tenantIds);
        map.put("isSettlement",settleStatus);
        map.put("matchType",matchType);
        List<TOrderDetail> list = orderDetailMapper.getMatrixValList(map);
        if(list == null) throw new LogicException("60021", "该赛事没有任何注单记录");

        String score = getCurrentScore(matchId);
        int homeScore = Integer.parseInt(score.split(":")[0]);
        int awayScore = Integer.parseInt(score.split(":")[1]);
//        int homeSize = Optional.ofNullable(size).orElse(10)+Integer.parseInt(score.split(":")[0])+1;
//        int awaySize = Optional.ofNullable(size).orElse(10)+Integer.parseInt(score.split(":")[1])+1;
//        homeSize = homeSize > 13 ? 13 : homeSize;
//        awaySize = awaySize > 13 ? 13 : awaySize;

        int homeSize = 13;
        int awaySize = 13;

        BigDecimal[][] result = new BigDecimal[homeSize][awaySize];
        for(int m = 0 ;m < homeSize ; ++m) {
            for (int n = 0; n < awaySize; ++n) {
                result[m][n] = BigDecimal.ZERO;
            }
        }
        for(TOrderDetail detail : list){
            //只计算可以用比分矩阵计算的订单
            if(detail.getRecType() != null && detail.getRecType() != 0){
                continue;
            }
            //早盘已结算的不用比分矩阵
            if(detail.getIsSettlement() != null && detail.getIsSettlement() == 1 && matchType == 1){
                continue;
            }

            String matrixStr = detail.getRecVal();
            if(!StringUtils.isEmpty(matrixStr)) {
                BigDecimal[][] arr;
                try {
                    arr = JSON.parseObject(matrixStr, BigDecimal[][].class);
                } catch (Exception e) {
                    log.error("::{}::,当前注单存入矩阵数据有误!：{}", CommonUtils.getLinkId(), e);
                    log.error("::{}::,当前注单存入矩阵数据有误, betNo:{},detail:{}", CommonUtils.getLinkId(), detail.getBetNo(), detail);
                    continue;
                }

                BigDecimal volumePercentage = detail.getVolumePercentage();
                if(volumePercentage==null){
                    volumePercentage = BigDecimal.valueOf(1);
                }
                if(volumePercentage.compareTo(BigDecimal.valueOf(1))!=0){
                    log.info("::{}::订单:{}:货量百分比{}",CommonUtil.getRequestId(), detail.getOrderNo(), detail.getVolumePercentage());
                }
                for (int i = 0; i < homeSize; i++) {
                    for (int j = 0; j < awaySize; j++) {
                        try {
                            result[i][j] =  result[i][j].add(Optional.ofNullable(arr[i][j].multiply(volumePercentage)).orElse(BigDecimal.ZERO));
                        } catch (Exception e) {
                            log.error("::{}::,矩阵 arr[{}][{}] 转换失败!", CommonUtils.getLinkId(), i, j);
                            log.error("::{}::,矩阵转换失败!,ex:{}", CommonUtils.getLinkId(), e);
                        }
                    }
                }

            }
        }

        MatrixVo[][] mtx = new MatrixVo[homeSize][awaySize];
        List<Map.Entry<String,BigDecimal>> lowerlist = new ArrayList<Map.Entry<String,BigDecimal>>();
        List<Map.Entry<String,BigDecimal>> greaterlist = new ArrayList<Map.Entry<String,BigDecimal>>();
        sortMatrix(result,homeSize,awaySize,unit,lowerlist,greaterlist);
        log.info("::{}::lowerlist:{},greaterlist:{},mtx:{},score:{}",CommonUtil.getRequestId(),lowerlist,greaterlist,mtx,score);
        getLeveledMatrix(lowerlist,mtx,score);
        getLeveledMatrix(greaterlist,mtx,score);
        return filterMatrix(mtx, homeScore, awayScore, size);
    }

    /**
     * 进球后无效的比分直接删除该列或行
     * @param mtx
     * @param homeScore
     * @param awayScore
     * @param size
     * @return
     */
    private MatrixVo[][] filterMatrix(MatrixVo[][] mtx, int homeScore, int awayScore, int size) {
        //返回长度+1 比如参数是5  需要返回0-5的数据
        size++;

        if (homeScore >= 12 || awayScore >= 12) {
            return mtx;
        }
        //过滤掉比分以下的数据
        MatrixVo[][] shortMatrixVos = new MatrixVo[13 - homeScore][13 - awayScore];

        for (int i = 0; i < 13 - homeScore; i++) {
            for (int j = 0; j < 13 - awayScore; j++) {
                shortMatrixVos[i][j] = mtx[i + homeScore][j + awayScore];
            }
        }
        //返回长度处理
        int homeSize = 13 - homeScore < size ? 13 - homeScore : size;
        int awaySize = 13 - awayScore < size ? 13 - awayScore : size;

        MatrixVo[][] sizeMatrixVos = new MatrixVo[homeSize][awaySize];
        for (int i = 0; i < homeSize; i++) {
            for (int j = 0; j < awaySize; j++) {
                sizeMatrixVos[i][j] = shortMatrixVos[i][j];
            }
        }

        return sizeMatrixVos;
    }



    @Override
    public String getCurrentScore(Long matchId){
        MatchStatisticsInfoDetail matchStatisticsInfoDetail = matchStatisticsInfoDetailMapper.selectScore(matchId.intValue());
        if (matchStatisticsInfoDetail==null){
            return "0:0";
        }else {
            return matchStatisticsInfoDetail.getT1()+":"+matchStatisticsInfoDetail.getT2();
        }
    }

    /**
     * 获取上半场的比分
     * @param matchId
     * @return
     */
    @Override
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
        log.info("::{}::{}矩阵查询上半场比分:{}:{}",CommonUtil.getRequestId(), matchId, t1, t2);
        return t1 + ":" + t2;
    }

    @Override
    public List<OrderDetailStatReportVo> getMarketStatByMatchIdAndPlayIdAndMatchStatus(Long matchId, Long marketCategoryId,Integer matchStatus) {
        return orderDetailMapper.getMarketStatByMatchIdAndPlayIdAndMatchStatus(matchId,marketCategoryId,matchStatus);
    }

    @Override
    public IPage<OrderDetailVo> selectTOrderDetailByMarketIdPage(RequestMarketOrderVo requestMarketOrderVo, Integer matchType) {
        IPage<OrderDetailVo> iPage = new Page<>(requestMarketOrderVo.getPageNum(), requestMarketOrderVo.getPageSize());
        //String oddsType = getOddsType(requestMarketOrderVo.getPlayId(), requestMarketOrderVo.getOrderOdds());
        return orderDetailMapper.selectTOrderDetailByMarketIdPage(iPage, requestMarketOrderVo.getMatchId(), requestMarketOrderVo.getPlayId(), requestMarketOrderVo.getMarketId(),
            requestMarketOrderVo.getOddsType(), matchType, requestMarketOrderVo.getPlaceNum(), requestMarketOrderVo.getSportId());
    }

    public String getOddsType(Integer playId, Integer orderOdds) {
    	if(String.valueOf(playId).matches("1|17|111|119|126|129")) {//1,X,2
    		if("1".equals(String.valueOf(orderOdds))) return "1";
    		if("2".equals(String.valueOf(orderOdds))) return "X";
    		if("3".equals(String.valueOf(orderOdds))) return "2";
        }if(String.valueOf(playId).matches("2|18|114|122|127|38|45|51|57|63|26|134")) {//大小
            if("1".equals(String.valueOf(orderOdds))) return "Over";
            if("2".equals(String.valueOf(orderOdds))) return "Under";
        } if (String.valueOf(playId).matches("40|42|47|53|59|65|75|240")){ //单双
            if("1".equals(String.valueOf(orderOdds))) return "Odd";
            if("2".equals(String.valueOf(orderOdds))) return "Even";
        }
		return String.valueOf(orderOdds);
    }

	@Override
    public List<OrderDetailVo> selectTOrderDetailByMarketId(RequestMarketOrderVo requestMarketOrderVo, Integer orderOdds, Integer matchType) {
        return orderDetailMapper.selectTOrderDetailByMarketId(requestMarketOrderVo.getMatchId(), requestMarketOrderVo.getPlayId(), requestMarketOrderVo.getMarketId(), orderOdds,
                matchType);
    }
    @Override
    public List<OrderItem> queryOptionValue(String orderNo) {
        List<OrderItem> items = (List<OrderItem>)orderNoCache.get(orderNo);
        if (CollectionUtils.isEmpty(items)){
            items = orderDetailMapper.queryOrderDetailList(orderNo);
            if (CollectionUtils.isNotEmpty(items)){
                orderNoCache.put(orderNo,items);
            }else {
                return null;
            }
        }
        for (OrderItem bean : items){
            String playOptionName = getPlayOptionName(bean);
            bean.setPlayOptionsName(playOptionName);
            Map<String, Object> map = getMatchInfoCache(bean);
            //保存联赛名称，这里不新增字段了
            bean.setMatchName(ObjectUtils.isEmpty(map.get("text")) ? "" :map.get("text").toString());
            //保存赛事开始，这里不新增字段了
            bean.setBetTime(Long.valueOf(ObjectUtils.isEmpty(map.get("begin_time")) ? "0" :map.get("begin_time").toString()));

        }
        return items;
    }

    private String getPlayOptionName(OrderItem item) {
        Map<String,Object> map = getMatchInfoCache(item.getMatchId());
        String mapKey = item.getMarketId()+"-"+item.getPlayOptions();
        if (MapUtils.isEmpty(map)){
            playOptionsNameCache.remove(mapKey);
        }
        String optionValue = (String)playOptionsNameCache.get(mapKey);
        if (StringUtils.isEmpty(optionValue)){
            optionValue = itOrderDetailService.queryOptionValue(item);
            playOptionsNameCache.put(mapKey,optionValue);
        }
        Pattern p = Pattern.compile(regEx);
        String playOptionValue = PlayOptionNameUtil.assemblyMarketValue(map,item,optionValue,p);
        return playOptionValue;
    }

    private Map<String, Object> getMatchInfoCache(OrderItem item) {
        Map<String, Object> match = (Map<String, Object>)matchInfoCache.get(item.getMatchId().toString());
        if(org.springframework.util.CollectionUtils.isEmpty(match)){
            match = new HashMap<>();
            match.put("matchId",item.getMatchId());

            if(item.getMatchType().equals(3)){
                match = rcsRectanglePlayMapper.queryOutrightMatchInfo(match);
            }else{
                match = rcsRectanglePlayMapper.queryMatchInfo(match);
            }
            if (ObjectUtils.isEmpty(match)){
                log.info("::{}::赛事关联联赛ID信息不全",CommonUtil.getRequestId());
                return new HashMap<>();
            }
            matchInfoCache.put(item.getMatchId().toString(),match);
        }
        return match;
    }
    @Override
    public List<JSONObject> initBetRecode(Long matchId) {
        long beginTime = System.currentTimeMillis() - waitDate;
        List<String> ids = standardMatchInfoMapper.queryOrderNoByMatchId(matchId,beginTime);
        List<JSONObject> list = new ArrayList<>();
        if (ObjectUtils.isEmpty(ids)){
            log.info("::{}::该赛事下没有为处理的订单matchId:{}",CommonUtil.getRequestId(),matchId);
            return list;
        }
        List<TOrderForChampion> orderList = orderMapper.queryByOrderDetailExtAndIds(ids);
        list = orderItem2Json(matchId, orderList);
        return list;
    }

    private List<JSONObject> orderItem2Json(Long matchId, List<TOrderForChampion> orderList) {
        List<JSONObject> list = new ArrayList<>();
        //1.下方TOrderForChampion实体原来用的是TOrder实体，因冠军操盘851需求返回uid时Long类型返回的数值不正确，则复制出一个实体将uid字段换位String类型
        //2.原TOrder实体无userFlag字段，则在TOrderForChampion实体中新增了该字段，满足851需求
        for (TOrderForChampion order : orderList) {
            //原来用的是OrderBean实体，因冠军操盘851需求返回uid时Long类型返回的数值不正确，则复制出一个实体将uid字段换位String类型
            OrderBeanForChampion orderBean = new OrderBeanForChampion();
            BeanUtils.copyProperties(order, orderBean);
            for (TOrderDetail orderDetail : order.getOrderDetailList()) {
                OrderItem bean = new OrderItem();
                BeanUtils.copyProperties(orderDetail, bean);
                JSONObject json = JSONObject.parseObject(JSONObject.toJSONString(orderDetail));
                if (matchId.longValue() == orderDetail.getMatchId().longValue()){
                    json.put("uid",orderBean.getUid());
                    json.put("userFlag",orderBean.getUserFlag());
                    json.put("userFlagId",orderBean.getUserFlagId());
                    json.put("orderNo",orderBean.getOrderNo());
                    json.put("sportId",orderBean.getSportId());
                    json.put("tenantId",orderBean.getTenantId());
                    json.put("currencyCode",orderBean.getCurrencyCode());
                    json.put("deviceType",orderBean.getDeviceType());
                    json.put("ipArea",orderBean.getIpArea());
                    json.put("ip",orderBean.getIp());
                    json.put("seriesType",orderBean.getSeriesType());
                    String playOptionName = getPlayOptionName(bean);
                    json.put("playOptionsName",playOptionName);
                    json.put("playName",orderDetail.getPlayName().replaceAll("\\\"", ""));
                    Map<String,Object> map = getMatchInfoCache(bean);

                    if(bean.getMatchType().equals(3)){
                        json.put("tournamentName", ObjectUtils.isEmpty(map.get("text")) ? "" :map.get("text").toString());
                        json.put("matchStartTime",Long.valueOf(ObjectUtils.isEmpty(map.get("standrd_outright_match_begion_time")) ? "0" :map.get("standrd_outright_match_begion_time").toString()));
                    }else{
                        json.put("tournamentName", ObjectUtils.isEmpty(map.get("text")) ? "" :map.get("text").toString());
                        json.put("matchStartTime",Long.valueOf(ObjectUtils.isEmpty(map.get("begin_time")) ? "0" :map.get("begin_time").toString()));
                    }

                    json.put("betAmount",new BigDecimal(bean.getBetAmount()).divide(new BigDecimal(BaseConstants.MULTIPLE_BET_AMOUNT_VALUE),2,BigDecimal.ROUND_HALF_UP).setScale(2, RoundingMode.HALF_UP).toString());
                    json.remove("recVal");
                    list.add(json);
                }
            }
        }
        return list;
    }

    @Override
    public Map<String,Object> queryOrderByPage(Map<String, Object> map) {
        Integer count = orderMapper.queryOrderCountByPage(map);
        Integer pageSize = ObjectUtils.isEmpty(map.get("pageSize")) ? 50 : Integer.valueOf(map.get("pageSize").toString());
        Integer page = ObjectUtils.isEmpty(map.get("page")) ? 1 : Integer.valueOf(map.get("page").toString());
        Integer start = ((page - 1) * pageSize) < 0 ? 0 : ((page - 1) * pageSize);
        map.put("start",start);
        map.put("pageSize",pageSize);
        List<JSONObject> list = new ArrayList<>();
        List<TOrderForChampion> orderList = orderMapper.queryOrderDetailByPage(map);
        if (CollectionUtils.isNotEmpty(orderList)){
            list = orderItem2Json(Long.valueOf(map.get("matchIds").toString()), orderList);
        }
        Map<String,Object> result = new HashMap<>();
        result.put("list",list);
        result.put("count",count);
        return result;
    }

    @Override
    public String queryOptionValue(OrderItem bean) {
        return orderDetailMapper.queryOptionValue(bean);
    }

    @Override
    public IPage<OrderDetailVo> queryBetList(OrderDetailVo vo) {
        IPage<OrderDetailVo> betList = new Page<>();
        try {
            initPageInfo(vo.getPageSize(),vo.getCurrentPage(),50,1,vo);
            // 设置查询时间
            setBetTime(vo);
            Long count = orderDetailMapper.queryBetListCount(vo);
            if (!ObjectUtils.isEmpty(count) && count.longValue() > NumberUtils.LONG_ZERO){
                List<OrderDetailVo> list = orderDetailMapper.queryBetList(vo);
                if (CollectionUtils.isNotEmpty(list)){
                    for (OrderDetailVo detail : list){
                        String playOptionName = getPlayOptionName(detail);
                        detail.setPlayOptionsName(playOptionName);
                    }
                }
                betList.setRecords(list);
                betList.setTotal(count);
            }
        }catch (RcsServiceException e){
            log.error("::{}::{}", CommonUtil.getRequestId(), e.getMessage(), e);
        }
        return betList;
    }

    /**
     * @Description   //帅选时间默认3天
     * @Param [vo]
     * @Author  Sean
     * @Date  14:55 2020/10/2
     * @return void
     **/
    private void setBetTime(OrderDetailVo vo) {
        if (ObjectUtils.isEmpty(vo.getMinBetTime()) || ObjectUtils.isEmpty(vo.getMaxBetTime())){
            Long now = System.currentTimeMillis();
            vo.setMaxBetTime(String.valueOf(now));
            Long minBetTime = now - THREE_DAY_TIME_MILLIS;
            vo.setMinBetTime(String.valueOf(minBetTime));
        }
    }

    /**
     * @Description   //获取显示的投注项名称
     * @Param [detail]
     * @Author  Sean
     * @Date  11:43 2020/9/30
     * @return java.lang.String
     **/
    private String getPlayOptionName(OrderDetailVo detail) {
        log.info("::{}::查询投注项名称={}",CommonUtil.getRequestId(),JSONObject.toJSONString(detail));
        Map<String,Object> map = getMatchInfoCache(detail.getMatchId().longValue());

        String mapKey = detail.getMarketId()+"-"+detail.getPlayOptions();
        if (MapUtils.isEmpty(map)){
            playOptionsNameCache.remove(mapKey);
        }
        OrderItem item = JSONObject.parseObject(JSONObject.toJSONString(detail),OrderItem.class);
        String optionValue = (String)playOptionsNameCache.get(mapKey);
        if (StringUtils.isEmpty(optionValue)){
            log.info("::{}::查询数据库optionValue={}",CommonUtil.getRequestId(),JSONObject.toJSONString(item));
            optionValue = itOrderDetailService.queryOptionValue(item);
            playOptionsNameCache.put(mapKey,optionValue);
        }
        Pattern p = Pattern.compile(regEx);
        log.info("::{}::获取显示的投注项名称，map={}，item={}，optionValue={}，p={}",CommonUtil.getRequestId(),JSONObject.toJSONString(map),JSONObject.toJSONString(item),optionValue,p.toString());
        String playOptionValue = PlayOptionNameUtil.assemblyMarketValue(map,item,optionValue,p);
        return playOptionValue;
    }

    /**
     * @Description   //通用的获取分页信息方法
     * @Param [pageSize, currentPage, defaultSize, defaultPage, t]
     * @Author  Sean
     * @Date  9:13 2020/9/30
     * @return com.baomidou.mybatisplus.core.metadata.IPage<T>
     **/
    public void initPageInfo(Integer pageSize,Integer currentPage,Integer defaultSize,Integer defaultPage,OrderDetailVo vo){
        defaultSize = ObjectUtils.isEmpty(defaultSize) ? 50 : defaultSize;
        defaultPage = ObjectUtils.isEmpty(defaultPage) ? 1 : defaultPage;
        pageSize = ObjectUtils.isEmpty(pageSize) ? defaultSize : pageSize;
        currentPage = ObjectUtils.isEmpty(currentPage) ? defaultPage : currentPage;
        vo.setPageSize(pageSize);
        vo.setStart((currentPage - 1) * pageSize.longValue());
    }
    /**
     * @Description   //获取赛事对阵信息
     * @Param [matchId]
     * @Author  Sean
     * @Date  10:57 2020/9/30
     * @return java.util.Map<java.lang.String,java.lang.Object>
     **/
    public Map<String, Object> getMatchInfoCache(Long matchId) {
        String residStr = redisClient.get(String.format(PREFIX_TEAM_NAME,matchId));
        Map<String, Object> match = Maps.newHashMap();
        //如果matchManageId为空，重新从数据库获取
        if(StringUtils.isBlank(residStr)){
            match = matchForecastService.getMatchMarketTeamVos(matchId);
            log.info("::{}::数据库对阵信息={}", CommonUtil.getRequestId(),JSONObject.toJSONString(match));
            if (!ObjectUtils.isEmpty(match)){
                redisClient.setExpiry(String.format(PREFIX_TEAM_NAME,matchId),match,24*60*60L);
            }
        }else {
            match = JSONObject.parseObject(residStr);
        }
        return match;
    }

    @Override
    public List<Integer> getOrderOdds(Integer matchId, Integer playId) {
        return orderDetailMapper.getOrderOdds(matchId,playId);
    }
}
