package com.panda.sport.rcs.trade.enums;

public enum RiskMerchantManagerTypeEnum {
    
    //1.投注特征标签,2特殊限额,3特殊延时,4提前结算,5赔率分组
    Type_1(1, "投注特征标签"),
    Type_2(2, "特殊限额"),
    Type_3(3, "特殊延时"),
    Type_4(4, "提前结算"),
    Type_5(5, "赔率分组"),
    Type_6(6, "投注特征预警变更标签"),
    Type_7(7, "自动化任务标签变更"),
    ;

    Integer code;
    String desc;
    private RiskMerchantManagerTypeEnum(Integer code, String desc){
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
        for( RiskMerchantManagerTypeEnum type : RiskMerchantManagerTypeEnum.values()){
            if(type.getCode().equals(code)){
                return type.getDesc();
            }
        }
        return null;
    }
}
