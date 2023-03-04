package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.vo.OddsSnapShotVo;

import java.util.List;

/**
 * @author :  enzo
 * @Project Name :  trade
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-11-07 17:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MatchService {
    void updateTraderNums(Long matchId, Integer num);
}
