package com.panda.sport.rcs.enums;

/****
 * 赛事设置枚举
 */
public enum MatchSetEnum {

    UPDTAE_MARKET_STATUS(1, "修改盘口状态"),
    UPDATE_MARKET_TRADETYPE(2, "修改自动手动"),
    UPDATE_RISKMANAGER_CODE(3, "修改操盘方式"),
    UPDATE_ODDS_VALUE(4, "修改次要玩法赔率"),
    UPDTAE_MARKETCONFIG(5, "修改盘口配置"),
    UPDTAE_MATCHCONFIG(6, "修改賽事配置"),
    UPDTAE_MARKET_ODDS_VALUE(7, "主要玩法快速修改赔率");

    private Integer code;

    private String value;

    MatchSetEnum(Integer code, String value) {
        this.code = code;
        this.value = value;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public static MatchSetEnum getMatchSet(Integer code) {
        for (MatchSetEnum matchSetEnum : MatchSetEnum.values()) {
            if (code.equals(matchSetEnum.getCode())) {
                return matchSetEnum;
            }
        }
        return null;
    }

    public static boolean isStatusConfig(Integer methodNo) {
        return UPDTAE_MARKET_STATUS.getCode().equals(methodNo);
    }

    public static boolean isTradeTypeConfig(Integer methodNo) {
        return UPDATE_MARKET_TRADETYPE.getCode().equals(methodNo);
    }

}
