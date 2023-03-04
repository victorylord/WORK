package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.pojo.TSettle;
import com.panda.sport.rcs.pojo.report.CalcSettleItem;
import com.panda.sport.rcs.pojo.report.MinDates;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 结算表 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-10-07
 */
@Service
public interface TSettleMapper extends BaseMapper<TSettle> {

    Long getCountCustomizedOrder(@Param("beginTime") Long beginTime, @Param("endTime") Long endTime);

    List<CalcSettleItem> getCustomizedOrderList( @Param("beginTime") Long beginTime, @Param("endTime") Long endTime,@Param("start") Integer start,@Param("limit") Integer limit);

    MinDates getMinDates(@Param("startTime") Long startTime);

    void updateTSettleToOrderStatic(@Param("list") CalcSettleItem list);

    IPage<Long> selectIdByDate(Page<Long> param,@Param("startDate") Long startDate,@Param("endDate") Long endDate);

    void updateSettleStatusByIds(@Param("list") List<Long> list);



    List<TSettle>  selectByMatchId(Long matchId);

}
