package com.panda.sport.rcs.trade.wrapper.statistics.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.panda.sport.rcs.mapper.statistics.RcsMatchDimensionStatisticsMapper;
import com.panda.sport.rcs.pojo.statistics.RcsMatchDimensionStatistics;
import com.panda.sport.rcs.pojo.statistics.RcsTotalValueNearlyOneTime;
import com.panda.sport.rcs.trade.wrapper.MarketViewService;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsMatchDimensionStatisticsService;
import com.panda.sport.rcs.trade.wrapper.statistics.RcsTotalValueNearlyOneTimeService;
import com.panda.sport.rcs.vo.statistics.RcsMatchDimensionStatisticsVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.statistics.impl
 * @Description :  更新单场比赛
 * @Date: 2019-11-05 15:59
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
@Slf4j
public class RcsMatchDimensionStatisticsServiceImpl extends ServiceImpl<RcsMatchDimensionStatisticsMapper, RcsMatchDimensionStatistics> implements RcsMatchDimensionStatisticsService {
    @Autowired
    private RcsMatchDimensionStatisticsMapper mapper;

    @Autowired
    private MarketViewService marketViewService;

    @Autowired
    private RcsTotalValueNearlyOneTimeService rcsTotalValueNearlyOneTimeService;

    /**
     * 订单mq处理
     * 判断是否存在
     * @param rcsMatchDimensionStatistics
     * @return true为存在；false为不存在
     */
    private Boolean exists(RcsMatchDimensionStatistics rcsMatchDimensionStatistics){
        RcsMatchDimensionStatistics m = mapper.selectById(rcsMatchDimensionStatistics.getMatchId());
        if(m == null){
            return false;
        }
        return true;
    }

    /**
     * 结算mq处理
     * 更新总货量、总注数
     * @param rcsMatchDimensionStatistics
     */
    @Override
    public void updateByOrderHandle(RcsMatchDimensionStatistics rcsMatchDimensionStatistics) {
        if(exists(rcsMatchDimensionStatistics)){
            rcsMatchDimensionStatistics.setModifyTime(System.currentTimeMillis());
            mapper.updateByOrderHandle(rcsMatchDimensionStatistics);
        }
        else{
            rcsMatchDimensionStatistics.setCreateTime(System.currentTimeMillis());
            mapper.insert(rcsMatchDimensionStatistics);
        }
    }

    /**
     * 更新已结算货量、已结算盈亏
     * @param rcsMatchDimensionStatistics
     */
    @Override
    public void updateBySettledHandle(RcsMatchDimensionStatistics rcsMatchDimensionStatistics) {
        if(exists(rcsMatchDimensionStatistics)) {
            rcsMatchDimensionStatistics.setModifyTime(System.currentTimeMillis());
            mapper.updateBySettledHandle(rcsMatchDimensionStatistics);
        }else{
            rcsMatchDimensionStatistics.setCreateTime(System.currentTimeMillis());
            mapper.insert(rcsMatchDimensionStatistics);
        }
    }

    /**
     * 查询近一小时内实货量
     * @param matchId
     * @return
     */
    @Override
    public List<RcsMatchDimensionStatisticsVo> searchNearlyOneHourRealTimeValue(Long[] matchId,Long startTime){
        List<Long> longs = Arrays.asList(matchId);
        List<RcsMatchDimensionStatisticsVo> list = new ArrayList<>();
        for(int i=0;i<longs.size();i++){
            if(longs.get(i) !=null) {
                RcsTotalValueNearlyOneTime  oneTime= rcsTotalValueNearlyOneTimeService.getById(longs.get(i));
                RcsMatchDimensionStatisticsVo rcsMatchDimensionStatisticsVo = null;
                if(oneTime!=null){
                    rcsMatchDimensionStatisticsVo = mapper.searchNearlyOneHourRealTimeValue(longs.get(i),oneTime.getOrderDetailId());
                }
                
                if(rcsMatchDimensionStatisticsVo== null) {
                    rcsMatchDimensionStatisticsVo = new RcsMatchDimensionStatisticsVo();
                    rcsMatchDimensionStatisticsVo.setRealTimeValue(BigDecimal.ZERO);
                    rcsMatchDimensionStatisticsVo.setMatchId(longs.get(i));
                }

                //除以100
                list.add(rcsMatchDimensionStatisticsVo);
            }
        }

        return list;
    }

    /**
     * 推送实时数据到前台
     * @param matchId
     * @return
     */
    @Override
    public Boolean sendSysnData(Long matchId){
        log.debug("开始推送实时数据到前台：matchId:" + matchId );
        RcsMatchDimensionStatistics rcsMatchDimensionStatistics = getById(matchId);
        //%100
        if(rcsMatchDimensionStatistics.getSettledRealTimeValue() == null){
            rcsMatchDimensionStatistics.setSettledRealTimeValue(BigDecimal.ZERO);
        }
        if(rcsMatchDimensionStatistics.getTotalValue() == null){
            rcsMatchDimensionStatistics.setTotalValue(BigDecimal.ZERO);
        }
        if(rcsMatchDimensionStatistics.getTotalValue() == null){
            rcsMatchDimensionStatistics.setTotalValue(BigDecimal.ZERO);
        }
        if(rcsMatchDimensionStatistics.getSettledProfitValue() == null){
            rcsMatchDimensionStatistics.setSettledProfitValue(BigDecimal.ZERO);
        }
        rcsMatchDimensionStatistics.setSettledRealTimeValue(rcsMatchDimensionStatistics.getSettledRealTimeValue());
        rcsMatchDimensionStatistics.setSettledProfitValue(rcsMatchDimensionStatistics.getSettledProfitValue());
        rcsMatchDimensionStatistics.setTotalValue(rcsMatchDimensionStatistics.getTotalValue());
        rcsMatchDimensionStatistics.setSettledProfitValue(rcsMatchDimensionStatistics.getSettledProfitValue());
        marketViewService.updateMatchBetChange(rcsMatchDimensionStatistics);



        log.debug("结束推送实时数据到前台：matchId:" + matchId );
        return true;
    }
}
