package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.vo.MarketLiveOddsQueryVo;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.console.service
 * @Description :  TODO
 * @Date: 2020-02-10 15:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface RcsMatchConfigLogsService {
    /**
     * @return void
     * @Description //更新盘口配置的时候  插入记录
     * @Param [rcsMatchConfig]
     * @Author kimi
     * @Date 2020/2/10
     **/
    void insertRcsMatchConfig(MarketLiveOddsQueryVo marketLiveOddsQueryVo);
}
