package com.panda.sport.rcs.trade.enums;

public enum MatchTradeType {
    TRADE_TYPE_AU(0,"Au","自动操盘"),
    TRADE_TYPE_MA(1,"Ma","手动操盘"),
    TRADE_TYPE_M_A(2,"M+A","自动加强操盘");

    Integer code;
    String desc;
    String detail;

    private MatchTradeType(Integer code, String desc, String detail){
        this.code = code;
        this.desc = desc;
        this.detail = detail;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public static String getCodeDesc(Integer code){
        if(code == null) return null;
        for( MatchTradeType type : MatchTradeType.values()){
            if(type.getCode().equals(code)){
                return type.getDesc();
            }
        }
        return null;
    }
}
