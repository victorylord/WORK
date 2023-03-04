package com.panda.sport.rcs.trade.enums;

public enum UserLogTypeEnum {
    TIME_LIMIT(1,"限时"),
    QUOTA(2,"限额"),
    REMARKS(3,"备注"),
    TRADER(4,"操作者"),
    BETS(5,"投注限额"),
    SPORT(6,"体育种类"),
    SETTLE(7,"提前结算"),
    CHAMPION(8,"冠军玩法限额比例"),
    GROUP(9,"赔率分组"),
    BETRATE(10,"用户各赛种货量百分比"),
    DYNAMIC_GROUP(11,"动态赔率分组风控开关"),

    SETTLE_LIMIT(12,"派彩限额-特殊用户限制");

    private Integer value;
    private String type;

    UserLogTypeEnum(Integer value, String type) {
        this.value = value;
        this.type = type;
    }

    public Integer getValue() {
        return value;
    }

    public String getType() {
        return type;
    }
}
