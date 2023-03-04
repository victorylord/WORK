package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsTournamentOrderAcceptEventConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RcsTournamentOrderAcceptEventConfigMapper extends BaseMapper<RcsTournamentOrderAcceptEventConfig> {
    int updateBatch(List<RcsTournamentOrderAcceptEventConfig> list);

    int batchInsert(@Param("list") List<RcsTournamentOrderAcceptEventConfig> list);

    int insertOrUpdate(RcsTournamentOrderAcceptEventConfig record);

    int insertOrUpdateSelective(RcsTournamentOrderAcceptEventConfig record);
}