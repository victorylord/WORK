package com.panda.sport.rcs.trade.wrapper;

import com.panda.sport.rcs.vo.OddsSnapShotVo;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.wrapper
 * @Description :  TODO
 * @Date: 2020-07-08 17:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface MatchSnapShotService {
    List<OddsSnapShotVo> selectMatchSnapShot(Long matchId, Integer matchStatus);
}
