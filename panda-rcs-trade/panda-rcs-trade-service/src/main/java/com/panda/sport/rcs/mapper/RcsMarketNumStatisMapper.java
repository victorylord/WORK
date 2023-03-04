package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMarketNumStatis;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 盘口位置统计表
 * @author lithan auto
 * @since 2020-10-04
 */
@Service
public interface RcsMarketNumStatisMapper extends BaseMapper<RcsMarketNumStatis> {

    List<RcsMarketNumStatis> queryBetNums(@Param("matchIds") List<Long> matchIds);

}