package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsMatchOrderAcceptConfig;
import com.panda.sport.rcs.vo.BaseMatchInfoVo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface RcsMatchOrderAcceptConfigMapper extends BaseMapper<RcsMatchOrderAcceptConfig> {
    int updateBatch(List<RcsMatchOrderAcceptConfig> list);

    int batchInsert(@Param("list") List<RcsMatchOrderAcceptConfig> list);

    int insertOrUpdate(RcsMatchOrderAcceptConfig record);

    int insertOrUpdateSelective(RcsMatchOrderAcceptConfig record);

    List<BaseMatchInfoVo> queryManualTradeMatch(Map<String, Object> map);
}