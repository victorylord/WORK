package com.panda.sport.rcs.trade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author :  enzo
 */
@Getter
@AllArgsConstructor
public enum PeriodEnum {
    FULL_TIME_1(1, 1, 0, 10001L, "FOOTBALL_GOAL", "常规进球"),
    CORNER_KICK_1(1, 2, 0, 10002L, "FOOTBALL_CORNER", "常规角球"),
    EXTRA_TIME_1(1, 4, 0, 10003L, "FOOTBALL_OVERTIME", "加时进球"),
    PENALTY_SHOOT_1(1, 5, 0, 10004L, "FOOTBALL_PENALTY_SHOOTOUT", "点球大战"),
    PENALTY_CARD_1(1, 3, 0, 10005L, "FOOTBALL_PENALTY_CARD", "常规罚牌"),

    FULL_TIME_2(2, 7, 0, 20001L, "", "全场"),
    FIRST_HALF_2(2, 3, 1, 20002L, "", "上半场"),
    SECTION_ONE_2(2, 1, 13, 20003L, "", "第一节"),
    SECTION_TWO_2(2, 2, 14, 20004L, "", "第二节"),
    SECTION_THREE_2(2, 4, 15, 20005L, "", "第三节"),
    SECTION_FOUR_2(2, 5, 16, 20006L, "", "第四节"),
    SECOND_HALF_2(2, 6, 2, 20007L, "", "下半场"),
    ;

    private int sportId;
    private int sort;
    private int period;
    private Long categorySetId;
    /**
     * 玩法集编码
     */
    private String playSetCode;
    private String name;

    public static PeriodEnum transferCategorySetId(int sportId, int periodId) {
        for (PeriodEnum periodEnum : PeriodEnum.values()) {
            if (periodEnum.getSportId() == sportId && periodEnum.getPeriod() == periodId) {
                return periodEnum;
            }
        }
        return null;
    }

    public static int getSort(Long categorySetId) {
        for (PeriodEnum periodEnum : PeriodEnum.values()) {
            if (categorySetId.equals(periodEnum.getCategorySetId())) {
                return periodEnum.getSort();
            }
        }
        return -1;
    }

    public static String getPlaySetCodeByPlaySetId(Long playSetId) {
        for (PeriodEnum periodEnum : PeriodEnum.values()) {
            if (periodEnum.getCategorySetId().equals(playSetId)) {
                return periodEnum.getPlaySetCode();
            }
        }
        return "";
    }
}
