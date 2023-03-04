package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsFirstMarket;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RcsFirstMarketMapper extends BaseMapper<RcsFirstMarket> {
    int updateBatch(List<RcsFirstMarket> list);

    int batchInsert(@Param("list") List<RcsFirstMarket> list);

    int batchInsertOrUpdate(@Param("list") List<RcsFirstMarket> list);

    int insertOrUpdate(RcsFirstMarket record);

    int insertOrUpdateSelective(RcsFirstMarket record);

}