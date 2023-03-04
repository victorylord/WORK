package com.panda.sport.rcs.trade.enums;

public enum MatchTradeStatu {
    Trade_state_0(0, "开盘"),
    Trade_state_1(1, "封盘"),
    Trade_state_2(2, "关盘"),
    Trade_state_11(11, "锁盘");

    Integer code;
    String desc;
    private MatchTradeStatu(Integer code, String desc){
        this.code = code;
        this.desc = desc;
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
        for( MatchTradeStatu type : MatchTradeStatu.values()){
            if(type.getCode().equals(code)){
                return type.getDesc();
            }
        }
        return null;
    }
}
