package com.panda.sport.rcs.trade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author :  enzo
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.enums
 * @Description :  LinkedTypeEnum
 * @Date:
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum LinkedTypeEnum {
    DEFAULT(0, "", ""),
    LIVE(1, "_live", "切滚球"),
    FIFTEEN(2, "_fifteen", "赛前十五分钟"),
    DATA_PROVIDER(3, "_dataProvider", "数据商挡板"),
    AUTO_PLUS(4, "_APlus", "A+模式出现让球0或±0.5"),
    MANUAL(5, "_M", "M模式出现让球0或±0.5"),
    BALL_HEAD(6, "_ballHead", "球头改变"),
    JUMP_ODDS(7, "_jumpOdds", "跳赔封盘"),
    JUMP_ODDS_OPEN(8, "_jumpOddsOpen", "跳赔封盘自动开盘"),
    JUMP_MARKET(9, "_jumpMarketSeal", "跳盘封盘"),
    AUTO_CLOSE(10, "_autoClose", "自动关盘"),
    AUTO(11, "_A", "A模式出现让球0或±0.5"),
    SCORE_CHANGE(12, "_scoreChange", "进球封盘"),
    JUMP_LIMIT(13, "_jumpOverLimit", "跳水跳盘超过限制封盘"),
    TRADE_OVER_LIMIT(14, "_tradeOverLimit", "操盘超过限制封盘"),
    NEW_MARKET(15, "_newMarket", "新增盘口"),
    TX_INSIDE_SWITCH(16, "_txInsideSwitch", "TX内部切换数据源"),
    EVENT(17, "_scoreEvent", "进球封盘"),
    WINDOW(18, "_window", "调价窗口"),
    TRADE_MODE(19, "_tradeMode", "切换操盘模式"),
    FIFTEEN_MINUTES_SUB_PLAY_CLOSE(20, "_15close", "15分钟子玩法关盘"),
    PLAY_SALE(89, "_playSale", "玩法开售"),
    SYNC_PLAY_SALE(90, "_syncPlaySale", "同步玩法开售"),
    DEFAULT_L(91, "_defaultL", "默认L模式切换"),
    PA_2_MTS(92, "_toMts", "PA切换MTS操盘"),
    MTS_2_PA(93, "_toPa", "MTS切换PA操盘"),
    DATA_SOURCE_WEIGHT(101, "_dataSourceWeight", "切换数据源权重"),
    ODDS_SOURCE(102, "_oddsSource", "切换玩法赔率源"),
    TEMPLATE_CHANGE(103, "_templateChange", "模板变更"),
    EVENT_SOURCE_CHANGE(104, "_eventSourceChange", "事件源切换"),
    EVENT_PLAY_SET(111, "_eventPlaySet", "事件玩法集封盘"),
    CHU_ZHANG(121, "_chuZhang", "出涨封盘");

    private Integer code;
    private String suffix;
    private String remark;

    public boolean isYes(Integer linkedType) {
        return this.getCode().equals(linkedType);
    }

    public static String getSuffix(String func, Integer linkedType) {
        return func + getByCode(linkedType).getSuffix();
    }

    public static LinkedTypeEnum getByCode(Integer code) {
        if (code == null) {
            return DEFAULT;
        }
        for (LinkedTypeEnum linkedTypeEnum : LinkedTypeEnum.values()) {
            if (linkedTypeEnum.getCode().equals(code)) {
                return linkedTypeEnum;
            }
        }
        return DEFAULT;
    }
}
