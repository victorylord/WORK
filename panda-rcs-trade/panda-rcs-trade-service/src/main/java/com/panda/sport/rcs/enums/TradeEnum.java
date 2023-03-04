package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TradeEnum {
    AUTO(0, "A", "自动模式(A)"),
    MANUAD(1, "M", "手动模式(M)"),
    AUTOADD(2, "A+", "自动加强模式(A+)"),
    LINKAGE(3, "L", "联动模式(L)");

    private Integer code;
    private String mode;
    private String value;

    public static boolean isAuto(Integer tradeType) {
        return AUTO.getCode().equals(tradeType);
    }

    public static boolean isManual(Integer tradeType) {
        return MANUAD.getCode().equals(tradeType);
    }

    public static boolean isAutoAdd(Integer tradeType) {
        return AUTOADD.getCode().equals(tradeType);
    }

    public static boolean isLinkage(Integer tradeType) {
        return LINKAGE.getCode().equals(tradeType);
    }

    public static boolean checkTradeType(Integer tradeType) {
        return isAuto(tradeType) || isManual(tradeType) || isAutoAdd(tradeType) || isLinkage(tradeType);
    }

    public static TradeEnum getByTradeType(Integer tradeType) {
        for (TradeEnum tradeEnum : TradeEnum.values()) {
            if (tradeEnum.getCode().equals(tradeType)) {
                return tradeEnum;
            }
        }
        return AUTO;
    }
}
