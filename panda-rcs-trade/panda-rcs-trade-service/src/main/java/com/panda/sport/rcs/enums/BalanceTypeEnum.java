package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 平衡值类型
 * @Author : Paca
 * @Date : 2021-02-13 20:58
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum BalanceTypeEnum {

    JUMP_ODDS(1, "跳赔平衡值"),
    JUMP_MARKET(2, "跳盘平衡值");

    private Integer type;
    private String name;

    public static boolean isJumpOdds(Integer type) {
        return JUMP_ODDS.getType().equals(type);
    }

    public static boolean isJumpMarket(Integer type) {
        return JUMP_MARKET.getType().equals(type);
    }
}
