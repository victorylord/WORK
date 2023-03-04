package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsLogFomat;

import java.util.List;

public interface RcsLogFomatService extends IService<RcsLogFomat> {


    int updateBatch(List<RcsLogFomat> list);

    int batchInsert(List<RcsLogFomat> list);

    int insertOrUpdate(RcsLogFomat record);

    int insertOrUpdateSelective(RcsLogFomat record);

    List<RcsLogFomat> getChampionMatchOperateLogs(String matchId);
}
