package com.panda.sport.rcs.trade.vo;

import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-15 14:12
 **/
@Data
public class ChangePersonLiableVo {
    Integer matchId;
    /**
     * 0早盘 1滚球
     */
    Integer matchType;
    /**
     *
     */
    Integer tradeId;
    /**
     * 体育种类
     */
    Integer sportId;
    /**
     * 操盘手名称
     */
    String traderName;
}
