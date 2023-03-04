package com.panda.sport.rcs.mapper.statistics;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.panda.sport.rcs.pojo.dto.StatMatchIpDto;
import com.panda.sport.rcs.pojo.statistics.StatMatchIp;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mapper.statistics
 * @Description :  ip统计
 * @Date: 2020-06-07 16:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Mapper
public interface StatMatchIpMapper extends BaseMapper<StatMatchIp> {
    /**
     * 添加或者更新
     * @param statMatchIp
     * @return
     */
    int insertOrUpdate(StatMatchIp statMatchIp);

    /**
     * 按赛事id进行检索
     * @param matchId
     * @return
     */

    StatMatchIpDto queryByMatchIdAndIpAddr(@Param("matchId") Long matchId,@Param("ipAddr")String ipAddr);

    List<StatMatchIp> queryByMatchId(@Param("matchId") Long matchId);

}
