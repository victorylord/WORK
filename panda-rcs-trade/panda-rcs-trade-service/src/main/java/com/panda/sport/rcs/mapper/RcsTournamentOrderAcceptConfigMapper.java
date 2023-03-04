package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RcsTournamentOrderAcceptConfigMapper extends BaseMapper<RcsTournamentOrderAcceptConfig> {
    int updateBatch(List<RcsTournamentOrderAcceptConfig> list);

    int batchInsert(@Param("list") List<RcsTournamentOrderAcceptConfig> list);

    int insertOrUpdate(RcsTournamentOrderAcceptConfig record);

    int insertOrUpdateSelective(RcsTournamentOrderAcceptConfig record);
}