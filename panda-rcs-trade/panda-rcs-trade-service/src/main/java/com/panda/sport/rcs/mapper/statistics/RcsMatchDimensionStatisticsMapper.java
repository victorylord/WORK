package com.panda.sport.rcs.mapper.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.statistics.RcsMatchDimensionStatistics;
import com.panda.sport.rcs.vo.statistics.RcsMatchDimensionStatisticsVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  TODO
 * @Date: 2019-11-05 18:09
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface RcsMatchDimensionStatisticsMapper extends BaseMapper<RcsMatchDimensionStatistics> {
    /**
     * 订单mq处理
     * @param rcsMatchDimensionStatistics
     */
    void updateByOrderHandle(RcsMatchDimensionStatistics rcsMatchDimensionStatistics);

    /**
     * 结算mq处理
     * @param rcsMatchDimensionStatistics
     */
    void updateBySettledHandle(RcsMatchDimensionStatistics rcsMatchDimensionStatistics);

    /**
     *
     * @param matchId
     * @return
     */
    RcsMatchDimensionStatisticsVo searchNearlyOneHourRealTimeValue(@Param("matchId") Long matchId,@Param("orderDetailId") Long orderDetailId);

    /**
     * 添加
     * @param rcsMatchDimensionStatistics
     */
    void insertOrSave(RcsMatchDimensionStatistics rcsMatchDimensionStatistics);
}
