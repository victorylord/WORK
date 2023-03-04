package com.panda.sport.rcs.trade.enums;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.enums
 * @Description :  TODO
 * @Date: 2020-08-16 14:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public enum AutoCloseMarketEnum {
    FIRST_DURING(6, "上半场期间"),
    SECOND_DURING(7, "下半场期间"),
    FIRST_DURING_OVERTIME(41, "加时上半场期间"),
    SECOND_DURING_OVERTIME(42, "加时下半场期间");
    private Integer type;
    private String value;

    AutoCloseMarketEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public static String getValue(Integer type) {
        for (AutoCloseMarketEnum autoCloseMarketEnum:values()){
            if (autoCloseMarketEnum.type.equals(type)){
                return autoCloseMarketEnum.value;
            }
        }
        return null;
    }
}
