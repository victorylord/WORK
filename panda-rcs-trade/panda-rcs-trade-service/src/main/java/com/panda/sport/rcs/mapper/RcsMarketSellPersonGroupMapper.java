package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.bean.RcsMarketSellPersonGroup;
import com.panda.sport.rcs.vo.StandardMarketSellQueryV2Vo;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RcsMarketSellPersonGroupMapper extends BaseMapper<RcsMarketSellPersonGroup> {

    List<RcsMarketSellPersonGroup> selectHistoryPerson(@Param("param") StandardMarketSellQueryV2Vo param);

    int batchInsertOrUpdate(@Param("list") List<RcsMarketSellPersonGroup> list);
}
