package com.panda.sport.rcs.trade.service;

import com.panda.sport.rcs.trade.enums.LinkedTypeEnum;
import com.panda.sport.rcs.vo.MarketStatusUpdateVO;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘模式服务
 * @Author : Paca
 * @Date : 2021-12-26 15:28
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface TradeModeService {

    /**
     * 切换操盘模式
     *
     * @param updateVO
     * @return
     */
    String updateTradeMode(MarketStatusUpdateVO updateVO);

    /**
     * 篮球玩法开售切换L/A+模式
     *
     * @param sportId
     * @param matchId
     * @param playIdList
     * @param linkedTypeEnum
     */
    void basketballPlaySaleSwitchLinkage(Long sportId, Long matchId, List<Long> playIdList, LinkedTypeEnum linkedTypeEnum);
}
