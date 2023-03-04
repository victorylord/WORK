package com.panda.sport.rcs.trade.enums;


public enum ShiftEnum {
    DEAFAULT_A("未分","Not differentiated","0"),
    DAY_SHIFT("早班","Day","10"),
    DAY_SHIFT_1( "早班1","Day 1","20"),
    DAY_SHIFT_2("早班2","Day 2","30"),
    MIDDLE_SHIFT("中班","Mid","40"),
    MIDDLE_SHIFT_1("中班1","Mid 1","50"),
    MIDDLE_SHIFT_2("中班2","Mid 2","60"),
    NIGHT_SHIFT("晚班","Night","70"),
    NIGHT_SHIFT_1("晚班1","Night 1","80"),
    NIGHT_SHIFT_2("晚班2","Night 2","90");

    private String name;
    private String enName;
    private String num;

    ShiftEnum(String name,String enName, String num) {
        this.name = name;
        this.enName = enName;
        this.num = num;
    }

    public String getName() {
        return this.name;
    }
    public String getNum() {
        return this.num;
    }

    public static String  getName(String num){
        for (ShiftEnum shiftEnum:ShiftEnum.values()){
            if (num.equals(shiftEnum.num)){
                return shiftEnum.name;
            }
        }
        return "";
    }

    public static String  getEnName(String num){
        for (ShiftEnum shiftEnum:ShiftEnum.values()){
            if (num.equals(shiftEnum.num)){
                return shiftEnum.enName;
            }
        }
        return "";
    }

}
