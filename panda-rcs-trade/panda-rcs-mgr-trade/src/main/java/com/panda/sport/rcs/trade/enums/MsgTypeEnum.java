package com.panda.sport.rcs.trade.enums;

public enum MsgTypeEnum {
    WARNING(1,"预警消息"),
    SETTLEMENT(2,"结算消息"),
    SEALING(3,"封盘消息"),
    ERROR_END(4,"赛事异常结束消息"),
    ORDER_UNSETTLE(5,"订单未结算");

    private MsgTypeEnum(Integer msgType, String value) {
        this.msgType = msgType;
        this.value = value;
    }

    private Integer msgType;
    private String value;

    public Integer getMsgType() {
        return msgType;
    }

    public String getValue() {
        return value;
    }
}
