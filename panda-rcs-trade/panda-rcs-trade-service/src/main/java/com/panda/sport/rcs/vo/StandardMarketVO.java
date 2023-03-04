package com.panda.sport.rcs.vo;

import java.io.Serializable;

import com.panda.merge.dto.StandardMarketDTO;

import lombok.Data;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.vo
 * @Description : 标准盘口信息
 * @Author : Paca
 * @Date : 2020-08-08 14:17
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class StandardMarketVO extends StandardMarketDTO implements Serializable {

    private static final long serialVersionUID = 6758090001775433829L;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 盘口ID
     */
    private Long marketId;

    /**
     * 盘口位置
     */
    private Integer marketPlaceNum;

    /**
     * 赔率差值，用于盘口排序获取盘口位置
     */
    private Integer differentValue = Integer.MAX_VALUE;

    /**
     * 0-两、三项盘修改赔率，1-+-赔率
     * 0-表示修改状态和赔率，1-表示只修改赔率
     */
    private Integer funType = 0;

}
