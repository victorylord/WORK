package com.panda.sport.rcs.pojo.vo;

import lombok.Data;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  TODO
 * @Date: 2020-10-22 15:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OperateMessageVo {
    /**
     * 消息数据  预警消息和封盘消息
     */
    private List<RcsBroadCastVo> rcsBroadCastVoList;
    /**
     * 页数数量
     **/
    private Integer pageNum;
    /**
     * 页数大小
     **/
    private Integer pageSize;
    /**
     * 消息总数
     **/
    private Integer total;

    /**
     * 未读的消息总数
     **/
    private Integer noReadTotal;

    /**
     * 已读的消息总数
     **/
    private Integer readTotal;
    /**
     * 消息类型
     */
    private Integer msgType;
}
