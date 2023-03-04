package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.panda.sport.rcs.pojo.RcsMerchantLimitWarning;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-03-06 16:52
 **/
@Component
public interface RcsMerchantLimitWarningMapper extends BaseMapper<RcsMerchantLimitWarning> {
    /**
     *
     * @param page
     * @param time
     * @return
     */
    IPage<RcsMerchantLimitWarning> selectByPage(IPage<RcsMerchantLimitWarning> page, @Param("time") Long time);


    Integer getCurrentDayCount();
}
