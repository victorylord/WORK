package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.enums
 * @Description : 操盘级别枚举
 * @Author : Paca
 * @Date : 2020-10-27 9:36
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum TradeLevelEnum {
    MATCH(1, "赛事级别"),
    PLAY(2, "玩法级别"),
    MARKET(3, "盘口级别"),
    PLAY_SET(4, "玩法集级别"),
    BATCH_PLAY(5, "批量玩法级别"),
    BET_ITEM(6, "投注项级别"),
    SCORE_EVENT(7, "比分事件触发操盘状态改变"),
    BATCH_SUB_PLAY(8, "批量子玩法级别"),
    PLAY_SET_CODE(9, "玩法集编码"),
    CASH_OUT(10, "提前结算");

    private Integer level;
    private String name;

    public static boolean isMatchLevel(Integer tradeLevel) {
        return MATCH.getLevel().equals(tradeLevel);
    }

    public static boolean isPlayLevel(Integer tradeLevel) {
        return PLAY.getLevel().equals(tradeLevel);
    }

    public static boolean isMarketLevel(Integer tradeLevel) {
        return MARKET.getLevel().equals(tradeLevel);
    }

    public static boolean isPlaySetLevel(Integer tradeLevel) {
        return PLAY_SET.getLevel().equals(tradeLevel);
    }

    public static boolean isBatchPlayLevel(Integer tradeLevel) {
        return BATCH_PLAY.getLevel().equals(tradeLevel);
    }

    public static boolean isBetItemLevel(Integer tradeLevel) {
        return BET_ITEM.getLevel().equals(tradeLevel);
    }

    public static boolean isScoreEvent(Integer tradeLevel) {
        return SCORE_EVENT.getLevel().equals(tradeLevel);
    }

    public static boolean isBatchSubPlayLevel(Integer tradeLevel) {
        return BATCH_SUB_PLAY.getLevel().equals(tradeLevel);
    }

    public static boolean isPlaySetCodeLevel(Integer tradeLevel) {
        return PLAY_SET_CODE.getLevel().equals(tradeLevel);
    }

    /**
     * 操盘模式切换 支持的操盘级别
     *
     * @param tradeLevel
     * @return
     */
    public static boolean updateTradeTypeCheck(Integer tradeLevel) {
        return isPlayLevel(tradeLevel) || isBatchPlayLevel(tradeLevel);
    }

    /**
     * 状态变更 支持的操盘级别
     *
     * @param tradeLevel
     * @return
     */
    public static boolean updateStatusCheck(Integer tradeLevel) {
        return isMatchLevel(tradeLevel) ||
                isPlayLevel(tradeLevel) ||
                isMarketLevel(tradeLevel) ||
                isPlaySetLevel(tradeLevel) ||
                isBatchPlayLevel(tradeLevel) ||
                isBatchSubPlayLevel(tradeLevel) ||
                isPlaySetCodeLevel(tradeLevel);
    }
}
