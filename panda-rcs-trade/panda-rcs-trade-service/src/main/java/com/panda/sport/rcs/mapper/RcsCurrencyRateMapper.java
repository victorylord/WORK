package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsCurrencyRate;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @ClassName RcsCurrencyRateMapper
 * @Description: TODO 
 * @Author Vector
 * @Date 2019/12/20 
**/
public interface RcsCurrencyRateMapper  extends BaseMapper<RcsCurrencyRate> {
    int updateBatch(List<RcsCurrencyRate> list);

    int batchInsert(@Param("list") List<RcsCurrencyRate> list);

    int insertOrUpdate(RcsCurrencyRate record);

    int insertOrUpdateSelective(RcsCurrencyRate record);

    int batchSaveOrUpdate(@Param("list") List<RcsCurrencyRate> listRcsCurrencyRates);
}