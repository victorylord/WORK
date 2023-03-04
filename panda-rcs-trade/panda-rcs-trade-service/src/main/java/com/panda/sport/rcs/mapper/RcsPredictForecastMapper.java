package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.vo.statistics.RcsPredictForecastVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 多语言 Mapper 接口
 * </p>
 *
 * @author CodeGenerator
 * @since 2019-09-03
 */
@Repository
public interface RcsPredictForecastMapper extends BaseMapper<RcsPredictForecastVo> {
    /**
     * @Description   查询赛事级别的forecast
     * @Param [vo]
     * @Author  Sean
     * @Date  17:54 2020/7/23
     * @return java.util.List<com.panda.sport.rcs.vo.statistics.RcsPredictForecastVo>
     **/
    List<RcsPredictForecastVo> queryMatchForecast(@Param("forecast") RcsPredictForecastVo vo, @Param("start")Integer start,@Param("size") Integer pageSize);

    Integer queryMatchForecastCount(@Param("forecast") RcsPredictForecastVo vo);
}
