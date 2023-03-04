package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.dubbo.common.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <a href="http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=32349270">篮球两项盘全部玩法</a>
 *
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 足球
 * @Author : Paca
 * @Date : 2021-02-18 10:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Football {

    /**
     * 获取占位符玩法
     *
     * @return
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(336L, 28L, 30L, 109L, 110L, 34L, 32L, 33L, 31L, 222L, 148L, 233L, 225L, 120L, 125L, 230L, 231L, 232L, 224L, 235L, 133L, 237L, 357L, 363L, 364L, 365L, 366L);
    }

    /**
     * 是否是占位符玩法
     *
     * @param playId
     * @return
     */
    static boolean isPlaceholderPlay(Long playId) {
        return getPlaceholderPlayIds().contains(playId);
    }

    static boolean isOddsLimitPlay(Long playId){
        return Lists.newArrayList(7L,103L,241L,340L,341L,342L,343L,344L,345L,346L,347L,348L,349L,350L,351L,352L,353L,354L,355L,356L,357L,359L,360L,361L,362L).contains(playId);
    }

    static boolean isXOddsTypePlay(Long playId){
        return Lists.newArrayList(220L, 221L, 271L, 272L, 145L, 146L, 147L, 201L, 214L, 215L, 336L, 28L, 30L, 109L, 110L, 34L, 32L, 33L, 31L, 222L, 148L, 233L, 225L, 120L, 125L, 230L, 231L, 232L, 224L, 235L, 133L, 237L, 357L, 361L, 362L)
                .contains(playId);
    }

    /**
     * 角球玩法集ID
     */
    Long CORNER_PLAY_SET_ID = 10002L;

    @Getter
    @AllArgsConstructor
    enum CategorySet {
        FULL_TIME(10011L, "FOOTBALL_GOAL", Arrays.asList(6L, 7L, 12L, 15L, 78L, 92L, 141L,340L, 336L, 3L, 5L, 28L, 81L, 82L, 79L, 80L, 77L, 91L, 8L, 9L, 10L, 11L, 14L, 68L, 223L, 269L, 27L, 344L, 352L, 354L, 355L, 356L, 357L, 358L), "全场"),
        FIRST_HALF(10012L, "FOOTBALL_GOAL", Arrays.asList(70L, 20L, 104L, 42L, 43L, 21L, 22L, 23L, 103L, 24L, 69L, 29L, 30L, 87L, 97L, 90L, 100L, 83L, 86L, 93L, 96L, 84L, 85L, 94L, 95L, 16L, 108L, 109L, 110L, 341L, 359L), "上半场"),
        SECOND_HALF(10013L, "FOOTBALL_GOAL", Arrays.asList(25L, 143L, 26L, 72L, 74L, 75L, 76L, 71L, 73L, 88L, 98L, 89L, 99L, 142L, 270L, 342L), "下半场"),
        AND_OR(10014L, "FOOTBALL_GOAL", Arrays.asList(13L, 101L, 102L, 105L, 106L, 107L, 345L, 346L, 347L, 348L, 349L, 350L, 351L, 353L, 360L), "&玩法"),

        TIME_TYEP(10015L, "FOOTBALL_GOAL", Arrays.asList(34L, 32L, 33L, 31L), "时间类玩法"),
        SPECIAL_TYEP(10016L, "", Arrays.asList(144L, 137L, 35L, 36L, 222L, 148L, 149L, 150L, 151L, 152L, 363L, 364L, 365L, 366L), "特殊玩法"),

        CORNER_KICK(10017L, "FOOTBALL_CORNER", Arrays.asList(114L, 122L, 113L, 111L, 121L, 119L, 118L, 229L, 233L, 225L, 112L, 115L, 116L, 117L, 120L, 123L, 124L, 125L, 226L, 227L, 228L, 230L, 231L, 232L, 331L), "角球玩法"),
        PENALTY_CARD(10018L, "FOOTBALL_PENALTY_CARD", Arrays.asList(307L, 309L, 312L, 313L, 224L, 310L, 311L, 306L, 308L, 314L, 315L, 316L, 317L, 318L, 319L, 320L, 321L, 322L, 323L, 324L, 325L, 326L, 327L, 328L, 329L, 138L, 139L, 140L), "罚牌"),
        EXTRA_TIME(10019L, "FOOTBALL_OVERTIME", Arrays.asList(126L, 127L, 128L, 332L, 129L, 130L, 236L, 330L, 131L, 234L, 235L, 343L), "加时进球"),
        PENALTY_SHOOT(10020L, "FOOTBALL_PENALTY_SHOOTOUT", Arrays.asList(333L, 335L, 334L, 134L, 240L, 132L, 133L, 237L, 238L, 239L, 241L), "点球"),
        PROMOTION_TYPE(10021L, "", Arrays.asList(135L, 136L), "晋级"),

        CORRECT_SCORE(10022L, "FOOTBALL_CORRECT_SCORE", Arrays.asList(7L, 20L, 74L, 341L, 342L), "波胆"),
        TIME_5MINS(10023L, "FOOTBALL_TIME_5MINS", Arrays.asList(361L, 362L), "5分钟玩法");
        private Long categorySetId;

        /**
         * 玩法集编码
         */
        private String playSetCode;
        private List<Long> categoryIds;
        private String name;

        /**
         * 通过玩法Id获取去玩法集Id
         *
         * @param categoryId
         * @return
         */
        public static CategorySet getCategorySetId(Long categoryId) {
            for (CategorySet categorySet : CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet;
                }
            }
            return null;
        }

        /**
         * 对应玩法集ID
         *
         * @param categorySetIds
         * @return
         */
        public static List<Long> getCategoryIdsBySetId(List<Long> categorySetIds) {
            List<Long> categoryIds = new ArrayList();
            for (CategorySet categorySet : CategorySet.values()) {
                if (CollectionUtils.isNotEmpty(categorySetIds)) {
                    if (categorySetIds.contains(categorySet.getCategorySetId())) {
                        categoryIds.addAll(categorySet.getCategoryIds());
                    }
                } else {
                    categoryIds.addAll(categorySet.getCategoryIds());
                }
            }
            return categoryIds;
        }

        /**
         * 列数
         *
         * @param categoryId
         * @return
         */
        public static Integer getColNo(Long categoryId) {

            for (CategorySet categorySet : CategorySet.values()) {
                if (categorySet.getCategoryIds().contains(categoryId)) {
                    return categorySet.getCategoryIds().indexOf(categoryId) + 1;
                }
            }
            return 0;
        }

        public static List<Long> getcategorySets() {
            List<Long> result = new ArrayList<>();
            for (CategorySet categorySet : CategorySet.values()) {
                if (!result.contains(categorySet.categorySetId)) {
                    result.add(categorySet.categorySetId);
                }
            }
            return result;
        }

        public static String getPlaySetCodeByPlaySetId(Long playSetId) {
            for (CategorySet categorySet : CategorySet.values()) {
                if (categorySet.getCategorySetId().equals(playSetId)) {
                    return categorySet.getPlaySetCode();
                }
            }
            return "";
        }
    }

    @Getter
    @AllArgsConstructor
    enum PlaySetCode {
        FOOTBALL_GOAL("FOOTBALL_GOAL", "足球-进球类"),
        FOOTBALL_CORNER("FOOTBALL_CORNER", "足球-角球类"),
        FOOTBALL_PENALTY_CARD("FOOTBALL_PENALTY_CARD", "足球-罚牌类"),
        FOOTBALL_PENALTY_SHOOTOUT("FOOTBALL_PENALTY_SHOOTOUT", "足球-点球大战"),
        FOOTBALL_OVERTIME("FOOTBALL_OVERTIME", "足球-加时赛");

        private String code;
        private String name;
    }
}
