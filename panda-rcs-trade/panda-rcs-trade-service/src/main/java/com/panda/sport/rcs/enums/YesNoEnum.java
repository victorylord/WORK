package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 是否枚举
 * @Author : Paca
 * @Date : 2020-12-04 16:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum YesNoEnum {

    Y(1, "1", "是"),
    N(0, "0", "否");

    private Integer value;
    private String code;
    private String name;

    public static boolean isYes(Integer value) {
        return Y.getValue().equals(value);
    }

    public static boolean isNo(Integer value) {
        return N.getValue().equals(value);
    }

    public static boolean isNotYes(Integer value) {
        return !isYes(value);
    }

    public static boolean isNotNo(Integer value) {
        return !isNo(value);
    }

    public static Integer convert(boolean flag) {
        return flag ? Y.getValue() : N.getValue();
    }
}
