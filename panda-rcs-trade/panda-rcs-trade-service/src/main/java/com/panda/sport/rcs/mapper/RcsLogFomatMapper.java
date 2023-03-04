package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsLogFomat;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RcsLogFomatMapper extends BaseMapper<RcsLogFomat> {
    int updateBatch(List<RcsLogFomat> list);

    int batchInsert(@Param("list") List<RcsLogFomat> list);

    int insertOrUpdate(RcsLogFomat record);

    int insertOrUpdateSelective(RcsLogFomat record);

    List<RcsLogFomat> getChampionMatchOperateLogs(String matchId);

}