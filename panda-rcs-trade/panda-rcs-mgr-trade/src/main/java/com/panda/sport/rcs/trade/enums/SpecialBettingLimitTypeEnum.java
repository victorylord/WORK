package com.panda.sport.rcs.trade.enums;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-29 17:51
 **/

public enum  SpecialBettingLimitTypeEnum {
    NO(0,"无"),
    LABEL_LIMIT(1,"无"),
    PERCENTAGE_LIMIT(2,"特殊百分比限额"),
    SINGLE_GAME_QUOTA(3,"特殊单注单场限额"),
    VIP_LIMIT(4,"特殊VIP限额");
    private Integer type;
    private String value;

    private SpecialBettingLimitTypeEnum(Integer type, String value) {
        this.type = type;
        this.value = value;
    }

    public Integer getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public static String getName(Integer type){
        for (SpecialBettingLimitTypeEnum specialBettingLimitTypeEnum:SpecialBettingLimitTypeEnum.values()){
            if (specialBettingLimitTypeEnum.getType().equals(type)){
                return specialBettingLimitTypeEnum.getValue();
            }
        }
        return "无";
    }
}
