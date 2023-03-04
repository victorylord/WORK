package com.panda.sport.rcs.trade.enums;

public enum RiskMerchantManagerStatusEnum {

    //状态:0待处理,1同意,2拒绝,3强制执行
    Type_0(0, "待处理"),
    Type_1(1, "同意"),
    Type_2(2, "拒绝"),
    Type_3(3, "强制执行")
    ;

    Integer code;
    String desc;
    private RiskMerchantManagerStatusEnum(Integer code, String desc){
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
        for( RiskMerchantManagerStatusEnum type : RiskMerchantManagerStatusEnum.values()){
            if(type.getCode().equals(code)){
                return type.getDesc();
            }
        }
        return null;
    }
}
