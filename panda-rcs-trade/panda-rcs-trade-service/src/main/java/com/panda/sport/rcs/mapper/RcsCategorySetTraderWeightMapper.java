package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsCategorySetTraderWeight;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface RcsCategorySetTraderWeightMapper extends BaseMapper<RcsCategorySetTraderWeight> {

    int insertOrUpdate(RcsCategorySetTraderWeight record);

    int batchInsertOrUpdate(@Param("list") List<RcsCategorySetTraderWeight> list);
    /**
     * @Description   //根据玩法集id、赛事、赛事阶段等查询绩效型玩法
     * @Param [record]
     * @Author  sean
     * @Date   2022/5/20
     * @return java.lang.Integer
     **/
    Integer selectPlayIdBySetId(@Param("config")RcsMatchMarketConfig config,@Param("userId")Integer userId);

}