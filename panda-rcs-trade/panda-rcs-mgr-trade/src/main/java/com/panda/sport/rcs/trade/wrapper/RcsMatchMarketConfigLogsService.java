package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.pojo.RcsMatchMarketConfig;
import com.panda.sport.rcs.pojo.RcsMatchMarketConfigLogs;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.service
 * @Description :  TODO
 * @Date: 2020-02-10 15:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMatchMarketConfigLogsService {
    /**
     * @return void
     * @Description //更新盘口的时候插入数据
     * @Param [rcsMatchMarketConfig]
     * @Author kimi
     * @Date 2020/2/10
     **/
    void insert(RcsMatchMarketConfig rcsMatchMarketConfig);

    /**
     * @return java.util.List<com.panda.sport.rcs.pojo.RcsMatchMarketConfigLogs>
     * @Description //查看详细的操作记录
     * @Param [matchId, marketId]
     * @Author kimi
     * @Date 2020/2/11
     **/
    List<RcsMatchMarketConfigLogs> selectRcsMatchMarketConfigLogs(Integer matchId, Long marketId);
}
