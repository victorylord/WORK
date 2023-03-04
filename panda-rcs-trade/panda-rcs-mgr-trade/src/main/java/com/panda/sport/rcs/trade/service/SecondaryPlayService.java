package com.panda.sport.rcs.trade.service;

import com.panda.sport.rcs.vo.secondary.*;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 次要玩法
 * @Author : Paca
 * @Date : 2021-02-19 10:42
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface SecondaryPlayService {

    /**
     * 篮球两项盘玩法集
     *
     * @return
     */
    List<BasketballTwoPlaySet> basketballTwoPlaySet(String lang);

    /**
     * 篮球两项盘
     *
     * @param reqVo
     * @return
     */
    List<List<BasketballTwoPlaySetInfo>> basketballTwoList(BasketballTwoReqVo reqVo);

    /**
     * 次要玩法
     * @param reqVo
     * @return
     */
    List<FootballTwoPlaySetInfo> footballTwoPlaySet(FootballTwoReqVo reqVo);
}
