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
public enum MatchEventEnum {
    /**安全事件*/
    EVENT_SAFETY("safety",0,30),
    /**危险事件*/
    EVENT_DANGER("danger",0,60),
    /**封盘事件*/
    EVENT_CLOSING( "closing",60,120),
    /**拒单事件*/
    EVENT_REJECT("reject",0,0);

    private String code;
    private Integer minTime;
    private Integer maxTime;

    MatchEventEnum(String code,Integer minTime,Integer maxTime) {
        this.minTime = minTime;
        this.maxTime = maxTime;
        this.code = code;
    }

    public Integer getMinTime() {
        return this.minTime;
    }
    public Integer getMaxTime() {
        return this.maxTime;
    }
    public String getCode() {
        return this.code;
    }
}
