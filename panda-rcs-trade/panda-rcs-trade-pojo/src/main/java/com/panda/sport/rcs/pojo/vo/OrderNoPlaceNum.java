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
public class OrderNoPlaceNum {
    /**
     * 注单号
     */
    private String betNo;

    /**
     *  订单编号
     **/
    private String orderNo;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 运动种类编号
     */
    private Integer sportId;

    /**
     * 玩法ID
     */
    private Integer playId;

    /**
     * 盘口位置
     */
    private Integer placeNum;

    /**
     * 处理状态 0 待处理 1 接单  2拒单
     */
    private Integer orderStatus;

    /**
     * 订单扫描状态 0 未处理 1 已处理 2处理中
     */
    private Integer handleStatus;

    /**
     * 位置状态
     */
    private Integer placeNumStatus;
}
