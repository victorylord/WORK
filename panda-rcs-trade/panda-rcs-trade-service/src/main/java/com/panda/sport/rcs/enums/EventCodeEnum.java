package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 事件编码枚举
 * @Author : Paca
 * @Date : 2021-03-20 11:22
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum EventCodeEnum {
    TIMEOUT("timeout", "暂停"),
    TIMEOUT_OVER("timeout_over", "暂停结束");

    private String code;
    private String name;

    public static boolean isTimeout(String eventCode) {
        return TIMEOUT.getCode().equalsIgnoreCase(eventCode);
    }
}
