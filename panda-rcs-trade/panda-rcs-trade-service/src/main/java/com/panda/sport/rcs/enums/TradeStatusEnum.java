package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 操盘状态枚举
 * @Author : Paca
 * @Date : 2020-11-05 10:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum TradeStatusEnum {
    OPEN(0, "开"),
    CLOSE(2, "关"),
    SEAL(1, "封"),
    LOCK(11, "锁"),
    DISABLE(12, "弃用"),
    END(13, "收盘");

    private Integer status;
    private String name;

    public static boolean isOpen(Integer status) {
        return OPEN.getStatus().equals(status);
    }

    public static boolean isClose(Integer status) {
        return CLOSE.getStatus().equals(status);
    }

    public static boolean isSeal(Integer status) {
        return SEAL.getStatus().equals(status);
    }

    public static boolean isLock(Integer status) {
        return LOCK.getStatus().equals(status);
    }

    public static boolean isDisable(Integer status) {
        return DISABLE.getStatus().equals(status);
    }

    public static boolean isEnd(Integer status) {
        return END.getStatus().equals(status);
    }

    public static Integer disableFlagConvert(Integer disableFlag) {
        if (disableFlag != null && disableFlag == 1) {
            return DISABLE.getStatus();
        }
        return SEAL.getStatus();
    }

    public static boolean checkMarketStatus(Integer status) {
        return isOpen(status) || isClose(status) || isSeal(status) || isLock(status) || isEnd(status);
    }

    public static TradeStatusEnum getByStatus(Integer status) {
        for (TradeStatusEnum tradeStatusEnum : TradeStatusEnum.values()) {
            if (tradeStatusEnum.getStatus().equals(status)) {
                return tradeStatusEnum;
            }
        }
        return OPEN;
    }
}
