package com.panda.sport.rcs.mapper;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.RcsBusinessDayPaidConfig;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 商户单日最大赔付 Mapper 接口
 * </p>
 *
 * @author admin
 * @since 2019-10-04
 */
public interface RcsBusinessDayPaidConfigMapper extends BaseMapper<RcsBusinessDayPaidConfig> {

    List<RcsBusinessDayPaidConfig> queryBusDayConifgs();

    void updateRcsBusinessDayPaidConfig(@Param("dayPaid") RcsBusinessDayPaidConfig rcsBusinessDayPaidConfig);

    RcsBusinessDayPaidConfig selectOneDayPaid(@Param("businessId")Long businessId);
}
