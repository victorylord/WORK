package com.panda.sport.rcs.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.report.RcsOrderStatisticMatchTime;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  TODO
 * @Date: 2019-12-25 20:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Service
public interface RcsOrderStatisticMatchTimeMapper extends BaseMapper<RcsOrderStatisticMatchTime> {
    void updateRcsOrderStatisticMatchTime(@Param("list") List<RcsOrderStatisticMatchTime> list);
    void updateRcsOrderStatisticMatch(@Param("item") RcsOrderStatisticMatchTime match);

    void deleteInfoByDate(@Param("startDate")String startDate);
}
