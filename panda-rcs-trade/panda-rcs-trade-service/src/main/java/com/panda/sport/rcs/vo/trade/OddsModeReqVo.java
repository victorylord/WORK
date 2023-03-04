package com.panda.sport.rcs.vo.trade;

import lombok.Data;

import java.io.Serializable;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 修改投注项模式 入参
 * @Author : Paca
 * @Date : 2022-05-16 13:26
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OddsModeReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛种
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 玩法ID
     */
    private Long playId;

    /**
     * 子玩法ID
     */
    private Long subPlayId;

    /**
     * 盘口ID
     */
    private Long marketId;

    /**
     * 投注项ID
     */
    private Long oddsId;

    /**
     * 投注项类型
     */
    private String oddsType;

    /**
     * 赔率，100000倍赔率
     */
    private Long oddsValue;

    /**
     * 0-关闭手动模式，1-开启手动模式
     */
    private Integer mode;
}
