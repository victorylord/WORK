package com.panda.rcs.warning.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.panda.rcs.warning.vo.MatchOperateExListVo;
import com.panda.rcs.warning.vo.RollBallMatchInfo;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.mapper
 * @Description :  TODO
 * @Date: 2022-07-19 15:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Repository
public interface MatchOperateExceptionMonitorMapper {

    List<RollBallMatchInfo> queryRollBallMatchInfo(@Param("type") Integer type, @Param("level") Integer level);


    MatchOperateExListVo queryMatchByTimerAndMatchStatus(@Param("matchId") Long matchId);

    List<Long> queryCollectMatch(@Param("userId") Integer userId);

    List<Long> queryTraderMatch(@Param("userId") Integer userId);

    List<Long> queryMatchList(@Param("type") Integer type);
}
