package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.report.RcsOrderStatisticSettleTime;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.mapper
 * @Description :  TODO
 * @Date: 2019-12-25 20:57
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsOrderStatisticSettleTimeMapper extends BaseMapper<RcsOrderStatisticSettleTime> {
    void deleteInfoByDate(@Param("startDate")String startDate);

    void updateRcsOrderStatisticSettleTime(@Param("list") List<RcsOrderStatisticSettleTime> list);
    void updateRcsOrderStatisticSettle(@Param("item") RcsOrderStatisticSettleTime settle);
}
