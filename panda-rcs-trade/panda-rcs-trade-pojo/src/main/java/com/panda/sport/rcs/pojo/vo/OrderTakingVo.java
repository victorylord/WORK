package com.panda.sport.rcs.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  订单处理
 * @Date: 2020-01-31 18:41
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OrderTakingVo {
    /**
     * @Description   订单编号
     **/
    private List<String> ids;
    /**
     * @Description   状态
     * @Param 1：接收 2：拒绝  3：先接单，在取消注单 4：手动接单  5：手动拒单  8:暂停接单 9:暂停拒单
     **/
    private String state;
    /**
     * 操盘手名称
     */
    private String userName;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     *暂停秒数
     */
    private Long pauseTime;
}
