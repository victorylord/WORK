package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsStandardOutrightMatchInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public interface RcsStandardOutrightMatchInfoMapper extends BaseMapper<RcsStandardOutrightMatchInfo> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(RcsStandardOutrightMatchInfo record);

    RcsStandardOutrightMatchInfo selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RcsStandardOutrightMatchInfo record);

    int updateByPrimaryKey(RcsStandardOutrightMatchInfo record);

    int updateBatch(List<RcsStandardOutrightMatchInfo> list);

    int updateBatchSelective(List<RcsStandardOutrightMatchInfo> list);

    int batchInsert(@Param("list") List<RcsStandardOutrightMatchInfo> list);

    int insertOrUpdate(RcsStandardOutrightMatchInfo record);

    int insertOrUpdateSelective(RcsStandardOutrightMatchInfo record);

    int batchInsertOrUpdate(List<RcsStandardOutrightMatchInfo> standardSportMarketCategories);
}