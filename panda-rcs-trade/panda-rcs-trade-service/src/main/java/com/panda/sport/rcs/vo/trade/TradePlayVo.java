package com.panda.sport.rcs.vo.trade;

import lombok.Data;

import java.io.Serializable;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 玩法信息
 * @Author : Paca
 * @Date : 2021-09-16 16:01
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class TradePlayVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 玩法ID
     */
    private Long playId;

    /**
     * 子玩法ID
     */
    private Long subPlayId;
}
