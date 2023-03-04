package com.panda.sport.rcs.trade.wrapper.statistics;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.statistics.RcsMatchDimensionStatistics;
import com.panda.sport.rcs.vo.statistics.RcsMatchDimensionStatisticsVo;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.wrapper.statistics
 * @Description :  TODO
 * @Date: 2019-11-05 15:59
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMatchDimensionStatisticsService extends IService<RcsMatchDimensionStatistics> {
    /**
     * 订单mq处理
     * 判断是否存在
     * @param rcsMatchDimensionStatistics
     */
    void updateByOrderHandle(RcsMatchDimensionStatistics rcsMatchDimensionStatistics);

    /**
     * 结算mq处理
     * 更新总货量、总注数
     * @param rcsMatchDimensionStatistics
     */
    void updateBySettledHandle(RcsMatchDimensionStatistics rcsMatchDimensionStatistics);


    /**
     * 查询近一小时内实货量
     * @param matchId
     * @return
     */
    List<RcsMatchDimensionStatisticsVo> searchNearlyOneHourRealTimeValue(Long[] matchId,Long startTime);

    /**
     * 推送实时数据到前台
     * @param matchId
     * @return
     */
    Boolean sendSysnData(Long matchId);
}
