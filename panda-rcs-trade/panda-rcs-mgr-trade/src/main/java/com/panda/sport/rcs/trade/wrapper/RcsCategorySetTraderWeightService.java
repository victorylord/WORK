package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsCategorySetTraderWeight;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;

import java.util.List;

public interface RcsCategorySetTraderWeightService extends IService<RcsCategorySetTraderWeight> {

    int insertOrUpdate(RcsCategorySetTraderWeight record);

    int batchInsertOrUpdate(List<RcsCategorySetTraderWeight> list);
    /**
     * @Description   //根据玩法集id、赛事、赛事阶段等查询绩效型玩法
     * @Param [record]
     * @Author  sean
     * @Date   2022/5/20
     * @return java.lang.Integer
     **/
    Integer selectPlayIdBySetId(RcsMatchMarketConfig config,Integer userId);

}
