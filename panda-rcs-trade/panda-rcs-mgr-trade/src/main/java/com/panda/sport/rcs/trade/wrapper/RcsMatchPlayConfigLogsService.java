package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsMatchPlayConfig;
import com.panda.sport.rcs.pojo.RcsMatchPlayConfigLogs;
import com.panda.sport.rcs.pojo.RcsPlayConfig;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.service
 * @Description :  TODO
 * @Date: 2020-02-10 15:34
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */

public interface RcsMatchPlayConfigLogsService {
    /**
     * @return java.util.List<com.panda.sport.rcs.console.pojo.RcsMatchPlayConfigLogs>
     * @Description //根据赛事id查询记录
     * @Param [matchId]
     * @Author kimi
     * @Date 2020/2/10
     **/
    List<RcsMatchPlayConfigLogs> selectByMatchId(Long matchId);

    /**
     * @return void
     * @Description //更新玩法数据的时候  增加日志
     * @Param [rcsMatchPlayConfig]
     * @Author kimi
     * @Date 2020/2/10
     **/
    void insertRcsMatchPlayConfigLogs(RcsMatchPlayConfig rcsMatchPlayConfig);

    /**
     * @return void
     * @Description //TODO
     * @Param [rcsPlayConfig]
     * @Author kimi
     * @Date 2020/2/25
     **/
    void insertRcsMatchPlayConfigLogs(RcsPlayConfig rcsPlayConfig);
}
