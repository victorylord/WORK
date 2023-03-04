package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.MatchPeriod;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName MatchPeriodMapper
 * @Description: TODO 
 * @Author Vector
 * @Date 2019/11/19 
**/
public interface MatchPeriodMapper extends BaseMapper<MatchPeriod> {
    int batchInsert(@Param("list") List<MatchPeriod> list);

    int insertOrUpdate(MatchPeriod record);
}