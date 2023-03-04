package com.panda.sport.rcs.trade.vo.betOrder;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  中场休息
 * @Date: 2020-01-31 18:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class SetHalftimeVo {
    /**
     * @Description  赛事Id
     * @Param 
     * @Author  toney
     * @Date  18:31 2020/1/31
     * @return 
     **/
    private Long matchId;
    /**
     * 联赛id
     */
    private Long tournamentId;
    /**
     * @Description   状态
     * @Param 0：开放 1：关闭
     * @Author  toney
     * @Date  18:34 2020/1/31
     * @return 
     **/
    private Integer state;
}
