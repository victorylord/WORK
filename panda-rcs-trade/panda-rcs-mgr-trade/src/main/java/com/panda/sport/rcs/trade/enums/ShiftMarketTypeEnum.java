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
public enum ShiftMarketTypeEnum {


    LIVE("滚球","In-play",0),

    DAY_SHIFT( "早盘","Pre-Match",1),

    DAY_SHIFT_EU("早盘欧洲","Prematch-Europe",10),

    DAY_SHIFT_AS("早盘亚洲","Prematch-Asia",20),

    DAY_SHIFT_AM("早盘美洲","Prematch-America",30),

    CAMBODIA("柬埔寨","Cambodia",40),

    DEAFAULT_A("未分","Not differentiated",100);


    private String name;
    private String enName;
    private Integer num;

    ShiftMarketTypeEnum(String name, String enName, Integer num) {
        this.name = name;
        this.enName = enName;
        this.num = num;
    }

    public String getName() {
        return this.name;
    }
    public Integer getNum() {
        return this.num;
    }


    public static String  getName(Integer num){
        for (ShiftMarketTypeEnum shiftMarketTypeEnum:ShiftMarketTypeEnum.values()){
            if (num.equals(shiftMarketTypeEnum.num)){
                return shiftMarketTypeEnum.name;
            }
        }
        return "";
    }

    public static String getEnName(Integer num){
        for (ShiftMarketTypeEnum shiftMarketTypeEnum:ShiftMarketTypeEnum.values()){
            if (num.equals(shiftMarketTypeEnum.num)){
                return shiftMarketTypeEnum.enName;
            }
        }
        return "";
    }
}
