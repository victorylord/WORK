package com.panda.sport.rcs.trade.wrapper;

import com.baomidou.mybatisplus.extension.service.IService;
import com.panda.sport.rcs.pojo.RcsStandardOutrightMatchInfo;

import java.util.List;

public interface RcsStandardOutrightMatchInfoService extends IService<RcsStandardOutrightMatchInfo> {

    int deleteByPrimaryKey(Long id);

    int insertSelective(RcsStandardOutrightMatchInfo record);

    RcsStandardOutrightMatchInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RcsStandardOutrightMatchInfo record);

    int updateByPrimaryKey(RcsStandardOutrightMatchInfo record);

    int updateBatch(List<RcsStandardOutrightMatchInfo> list);

    int updateBatchSelective(List<RcsStandardOutrightMatchInfo> list);

    int batchInsert(List<RcsStandardOutrightMatchInfo> list);

    int insertOrUpdate(RcsStandardOutrightMatchInfo record);

    int insertOrUpdateSelective(RcsStandardOutrightMatchInfo record);

    int batchInsertOrUpdate(List<RcsStandardOutrightMatchInfo> standardSportMarketCategories);
}





