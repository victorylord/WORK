package com.panda.sport.rcs.pojo.statistics;

import lombok.Data;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo.statistics
 * @Description :  近一时货量统计
 * @Date: 2019-12-30 20:02
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsOrofitNearlyOneTime {
    /**
     * id
     */
    private Long id;
    /**
     * 赛事Id
     */
    private Long matchId;
    /**
     * 订单详情Id
     */
    private Long orderDetailId;
    /**
     * 更新时间
     */
    private Long updateTime;
}
