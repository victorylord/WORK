package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

import java.util.Map;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.trade.wrapper
 * @Description : 盘口状态服务类
 * @Author : Paca
 * @Date : 2020-07-17 11:00
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MarketStatusService {

    /**
     * 赛事级别、玩法级别、玩法集修改操盘类型
     * 滚球前15分钟赛事
     *
     * @param marketStatusUpdateVO
     * @author Enzo
     */
    void updateSnapshotTradeType(MarketStatusUpdateVO marketStatusUpdateVO);

    /**
     * 赛事级别、玩法级别、盘口级别、玩法集修改盘口状态
     * 滚球前15分钟赛事
     *
     * @param marketStatusUpdateVO
     * @author Enzo
     */
    void updatSnapshotStatus(MarketStatusUpdateVO marketStatusUpdateVO);

    /**
     * 获取赛前15分钟操盘类型和状态
     *
     * @param matchId
     * @param categoryId
     * @return
     */
    Map<Integer, Integer> getSnapshotMarketPlaceStatus(Long matchId, Long categoryId);

}
