package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 累计差额计算方式
 * @Author : Paca
 * @Date : 2021-02-13 21:03
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum BalanceOptionEnum {

    BET(0, "投注额差值"),
    MIX(1, "投注/赔付混合差值");

    private Integer value;
    private String name;

    public static boolean isBet(Integer value) {
        return BET.getValue().equals(value);
    }

    public static boolean isMix(Integer value) {
        return MIX.getValue().equals(value);
    }
}
