package com.panda.sport.rcs.trade.service;

import com.panda.sport.rcs.pojo.tourTemplate.RcsTournamentTemplate;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.trade.service
 * @Description :  TODO
 * @Date: 2022-05-20 16:20
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface DistanceSwitchServer {

    /**
     * 发送接距开关给融合
     *
     * @param temp
     */
    void sendDistanceSwitch(RcsTournamentTemplate temp);
}
