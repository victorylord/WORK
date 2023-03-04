package com.panda.sport.rcs.mapper.statistics;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 投注项/坑位-期望值/货量 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2021-02-19
 */
public interface RcsPredictBetOddsMapper extends BaseMapper<RcsPredictBetOdds> {

    List<RcsPredictBetOdds> queryBetOdds(@Param("matchIds") List<Long> matchIds,@Param("dataType")Integer dataType, @Param("seriesType")Integer seriesType);
}
