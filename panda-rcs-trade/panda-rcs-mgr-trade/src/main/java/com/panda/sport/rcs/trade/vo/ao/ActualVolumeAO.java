package com.panda.sport.rcs.trade.vo.ao;

import lombok.Data;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.vo.ao
 * @Description :  TODO
 * @Date: 2020-10-07 16:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ActualVolumeAO {
    /**
     * 赛事id
     */
    private Integer matchId;
    /**
     * 赛节赛制
     */
    private List<Integer> playTimeStages;
    /**
     * 1赛前  2滚球
     */
    private Integer matchType;
}
