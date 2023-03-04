package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.log.annotion.format.LogFormatAnnotion;
import lombok.Data;

import java.util.List;

/**
 * @author :  carver
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.vo.betOrder
 * @Description :  一键秒接配置
 * @Date: 2020-11-17 20:13
 */
@Data
@LogFormatAnnotion
public class OrderSecondConfigVo {
    /**
     * 赛种
     */
    private Long sportId;
    /**
     * 操盘手id
     */
    private Long uid;
    /**
     * 标准赛事id
     */
    private Long matchInfoId;
    /**
     * 玩法集id
     */
    private Long playSetId;
    /**
     * 玩法集名称
     */
    private String playSetName;
    /**
     * 秒接状态 0 关闭秒接  1 开启秒接
     */
    private int secondStatus;
    /**
     * 玩法集列表
     */
    private List<OrderSecondConfigVo> playSetList;
    /**
     *投注金额
     */
    private Long betAmount;
    /**
     * 用户等级
     */
    private List<Integer> userLevels;
    /**
     * 操盘手名称
     */
    private String trader;
}
