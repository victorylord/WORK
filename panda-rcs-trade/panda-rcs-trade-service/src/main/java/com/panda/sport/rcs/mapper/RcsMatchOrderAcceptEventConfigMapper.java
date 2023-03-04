package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptEventConfig;
import com.panda.sport.rcs.pojo.dao.QueryOrderDangerousParam;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

@Component
public interface RcsMatchOrderAcceptEventConfigMapper extends BaseMapper<RcsMatchOrderAcceptEventConfig> {
    int updateBatch(List<RcsMatchOrderAcceptEventConfig> list);

    int batchInsert(@Param("list") List<RcsMatchOrderAcceptEventConfig> list);

    int insertOrUpdate(RcsMatchOrderAcceptEventConfig record);

    int insertOrUpdateSelective(RcsMatchOrderAcceptEventConfig record);

    List<RcsMatchOrderAcceptConfig> queryMatchOrderConfig(@Param("matchId") Long matchId, @Param("tournamentId") Long tournamentId);

    List<RcsMatchOrderAcceptConfig> queryOrderConfigList();

    List<Long> queryMatchOrderDangerousExist(@Param("matchIds") List<Long> matchIds, @Param("set")Set<QueryOrderDangerousParam> queryOrderDangerousParams);
}