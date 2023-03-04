package com.panda.sport.rcs.trade.enums;

public enum TheirTimeEnum {
    FULL_COURT(3,"全场",1.0,"match_score",0),
    FIRST_HALF(1,"上半场",0.5,"period_score",1),
    SECOND_HALF(2,"下半场",0.5,"period_score",2),
    SECTION_ONE(6,"第一节",0.25,"set_score",1),
    SECTION_TWO(7,"第二节",0.25,"set_score",2),
    SECTION_THREE(8,"第三节",0.25,"set_score",3),
    SECTION_FOUR(9,"第四节",0.25,"set_score",4),
    EXTRA_TIME(10,"加时赛",0.25,"extra_time_score",0);

    private Integer value;
    private String describe;
    private Double proportion;
    private String code;
    private Integer firstNum;

    TheirTimeEnum(Integer value, String describe, Double proportion, String code, Integer firstNum) {
        this.value = value;
        this.describe = describe;
        this.proportion = proportion;
        this.code = code;
        this.firstNum = firstNum;
    }

    public Integer getValue() {
        return value;
    }

    public String getDescribe() {
        return describe;
    }

    public Double getProportion() {
        return proportion;
    }

    public static TheirTimeEnum  getTheirTimeEnumByValue(Integer value){
       for (TheirTimeEnum theirTimeEnum:TheirTimeEnum.values()){
           if (theirTimeEnum.getValue().intValue()==value){
               return theirTimeEnum;
           }
       }
       return TheirTimeEnum.FIRST_HALF;
    }

    public static TheirTimeEnum  getTheirTimeEnumByCode(String code,Integer firstNum){
        for (TheirTimeEnum theirTimeEnum:TheirTimeEnum.values()){
            if (theirTimeEnum.getCode().equals(code) && theirTimeEnum.getFirstNum().equals(firstNum)){
                return theirTimeEnum;
            }
        }
        return TheirTimeEnum.FIRST_HALF;
    }

    public String getCode() {
        return code;
    }

    public Integer getFirstNum() {
        return firstNum;
    }
}
