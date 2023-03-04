package com.panda.sport.rcs.trade.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
@AllArgsConstructor
public enum CategoryShowEnum {

    CORNER_KICK(10101L, Arrays.asList(10002L,10017L)),
    EXTRA_TIME(10102L, Arrays.asList(10003L,10019L)),
    PENALTY_SHOOT(10103L, Arrays.asList(10004L,10020L)),
    PENALTY_CARD (10104L, Arrays.asList(10005L,10018L)),
    GOAL(10105L, Arrays.asList(10021L)),
    TIME_15MINS(10106L, Arrays.asList(10015L)),
    CORRECT_SCORE(10107L, Arrays.asList(10022L)),
    TIME_5MINS(10108L, Arrays.asList(10023L));
    private Long showId;
    private List<Long> categorySetId;

    public static Long  querySendId(Long  categorySetId){
        for (CategoryShowEnum em:CategoryShowEnum.values()){
            if (em.getCategorySetId().contains(categorySetId)){
                return em.getShowId();
            }
        }
        return 0L;
    }

    public static List<Long> queryCategorySetId(Long  showId){
        for (CategoryShowEnum em:CategoryShowEnum.values()){
            if (em.getShowId().equals(showId)){
                return em.getCategorySetId();
            }
        }
        return Arrays.asList(0L);
    }
}
