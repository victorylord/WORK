package com.panda.sport.rcs.trade.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

/**
 * @author :  enzo
 */
@Getter
@AllArgsConstructor
public enum BasketballEnum {

    SECTION_ONE(1, Arrays.asList(13), 20003L, Lists.newArrayList(48L, 46L, 45L, 47L), "第一节"),
    SECTION_TWO(2, Arrays.asList(14, 301), 20004L, Lists.newArrayList(54L, 52L, 51L, 53L), "第二节"),
    FIRST_HALF(3, Arrays.asList(1), 20002L, Lists.newArrayList(43L, 19L, 18L, 42L), "上半场"),
    SECTION_THREE(4, Arrays.asList(15, 302, 31), 20005L, Lists.newArrayList(60L, 58L, 57L, 59L), "第三节"),
    SECTION_FOUR(5, Arrays.asList(16, 303), 20006L, Lists.newArrayList(66L, 64L, 63L, 65L), "第四节"),
    SECOND_HALF(6, Arrays.asList(2), 20007L, Lists.newArrayList(142L, 143L, 26L, 75L), "下半场"),
    FULL_TIME(7, Arrays.asList(32, 40), 20001L, Lists.newArrayList(37L, 39L, 38L, 40L), "全场"),
    ;

    private int sort;
    private List<Integer> period;
    private Long categorySetId;
    private List<Long> categoryIdList;
    private String name;

    public static BasketballEnum getEnumByPeriod(int period) {
        for (BasketballEnum periodEnum : BasketballEnum.values()) {
            if (periodEnum.getPeriod().contains(period)) {
                return periodEnum;
            }
        }
        return null;
    }

    public static int getSort(Long categorySetId) {
        for (BasketballEnum periodEnum : BasketballEnum.values()) {
            if (categorySetId.equals(periodEnum.getCategorySetId())) {
                return periodEnum.getSort();
            }
        }
        return -1;
    }

    public static BasketballEnum getByCategorySetId(Long categorySetId) {
        for (BasketballEnum basketballEnum : BasketballEnum.values()) {
            if (basketballEnum.getCategorySetId().equals(categorySetId)) {
                return basketballEnum;
            }
        }
        return null;
    }
}
