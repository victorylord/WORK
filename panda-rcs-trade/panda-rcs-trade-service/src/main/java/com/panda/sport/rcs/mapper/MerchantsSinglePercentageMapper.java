package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.panda.sport.rcs.pojo.MerchantsSinglePercentage;
import com.panda.sport.rcs.pojo.RcsBusinessDayPaidConfig;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * <p>
 * 商户单场限额监控表 Mapper 接口
 * </p>
 *
 * @author lithan
 * @since 2021-11-24
 */
public interface MerchantsSinglePercentageMapper extends BaseMapper<MerchantsSinglePercentage> {

    List<Long> list();

}
