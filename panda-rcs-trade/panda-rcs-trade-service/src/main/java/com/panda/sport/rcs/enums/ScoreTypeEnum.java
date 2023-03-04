package com.panda.sport.rcs.enums;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 比分类型枚举
 * @Author : Paca
 * @Date : 2021-01-23 16:08
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface ScoreTypeEnum {

    /*@Getter
    @AllArgsConstructor
    enum FootballPlay {

        FULL_TIME("1", new Long[]{4L, 269L}, "全场比分"),
        FIRST_HALF("11", new Long[]{19L, 270L}, "上半场比分"),
        SECOND_HALF("12", new Long[]{143L}, "下半场比分"),

        CORNER_FULL_TIME("2", new Long[]{113L}, "角球全场比分"),
        CORNER_FIRST_HALF("21", new Long[]{121L}, "角球上半场比分"),

        OVERTIME_FULL_TIME("5", new Long[]{128L}, "加时全场比分"),
        OVERTIME_FIRST_HALF("51", new Long[]{130L}, "加时上半场比分"),

        PENALTY_CARD_FULL_TIME("3", new Long[]{306L}, "罚牌全场比分"),
        PENALTY_CARD_FIRST_HALF("31", new Long[]{308L}, "罚牌上半场比分"),

        YELLOW_CARD_FULL_TIME("4", new Long[]{324L}, "黄牌全场比分"),
        YELLOW_CARD_FIRST_HALF("41", new Long[]{327L}, "黄牌上半场比分"),

        PENALTY_SHOOTOUT("6", new Long[]{334L}, "点球大战比分"),

        MINUTES_GOAL_SCORE_15("71", new Long[]{33L}, "（0-15）分钟比分"),
        MINUTES_GOAL_SCORE_30("72", new Long[]{33L}, "（16-30）分钟比分"),
        MINUTES_GOAL_SCORE_45("73", new Long[]{33L}, "（31-45）分钟比分"),
        MINUTES_GOAL_SCORE_60("74", new Long[]{33L}, "（46-60）分钟比分"),
        MINUTES_GOAL_SCORE_75("75", new Long[]{33L}, "（61-75）分钟比分"),
        MINUTES_GOAL_SCORE_90("76", new Long[]{33L}, "（76-90）分钟比分"),

        MINUTES_CORNER_SCORE_15("81", new Long[]{232L}, "（0-15）分钟角球比分"),
        MINUTES_CORNER_SCORE_30("82", new Long[]{232L}, "（16-30）分钟角球比分"),
        MINUTES_CORNER_SCORE_45("83", new Long[]{232L}, "（31-45）分钟角球比分"),
        MINUTES_CORNER_SCORE_60("84", new Long[]{232L}, "（46-60）分钟角球比分"),
        MINUTES_CORNER_SCORE_75("85", new Long[]{232L}, "（61-75）分钟角球比分"),
        MINUTES_CORNER_SCORE_90("86", new Long[]{232L}, "（76-90）分钟角球比分");

        private String type;
        private Long[] playIdArray;
        private String name;

        public List<Long> getPlayIds() {
            return Lists.newArrayList(this.playIdArray);
        }

        public static boolean exist(Long playId) {
            for (FootballPlay footballPlay : FootballPlay.values()) {
                if (footballPlay.getPlayIds().contains(playId)) {
                    return true;
                }
            }
            return false;
        }
    }*/

    @Getter
    @AllArgsConstructor
    enum Football implements ScoreTypeEnum {
        FULL_TIME("match_score", 0L, 0, 0, "全场比分"),
        FIRST_HALF("set_score", 0L, 1, 0, "上半场比分"),
        SECOND_HALF("set_score", 0L, 2, 0, "下半场比分"),
        CORNER_FULL_TIME("corner_score", 0L, 0, 0, "角球全场比分"),
        CORNER_FIRST_HALF("corner_score", 0L, 1, 0, "角球上半场比分"),
        CORNER_SECOND_HALF("corner_score", 0L, 2, 0, "角球下半场比分"),
        OVERTIME_FULL_TIME("extra_time_score", 0L, 0, 0, "加时全场比分"),
        OVERTIME_FIRST_HALF("set_score", 0L, 3, 0, "加时上半场比分"),
        OVERTIME_SECOND_HALF("set_score", 0L, 4, 0, "加时下半场比分"),
//        RED_CARD_FULL_TIME("red_card_score", 0, "红牌全场比分"),
//        RED_CARD_FIRST_HALF("red_card_score", 1, "红牌上半场比分"),
//        RED_CARD_SECOND_HALF("red_card_score", 2, "红牌下半场比分"),
//        YELLOW_CARD_FULL_TIME("yellow_card_score", 0, "黄牌全场比分"),
//        YELLOW_CARD_FIRST_HALF("yellow_card_score", 1, "黄牌上半场比分"),
//        YELLOW_CARD_SECOND_HALF("yellow_card_score", 2, "黄牌下半场比分")
        MINUTES_GOAL_SCORE_15("minutesGoalScore_15", 3301L, 0, 0, "（0-15）分钟比分"),
        MINUTES_GOAL_SCORE_30("minutesGoalScore_30", 3302L, 0, 0, "（16-30）分钟比分"),
        MINUTES_GOAL_SCORE_45("minutesGoalScore_45", 3303L, 0, 0, "（31-45）分钟比分"),
        MINUTES_GOAL_SCORE_60("minutesGoalScore_60", 3304L, 0, 0, "（46-60）分钟比分"),
        MINUTES_GOAL_SCORE_75("minutesGoalScore_75", 3305L, 0, 0, "（61-75）分钟比分"),
        MINUTES_GOAL_SCORE_90("minutesGoalScore_90", 3306L, 0, 0, "（76-90）分钟比分"),

        MINUTES_CORNER_SCORE_15("minutesCornerScore_15", 23201L, 0, 0, "（0-15）分钟角球比分"),
        MINUTES_CORNER_SCORE_30("minutesCornerScore_30", 23202L, 0, 0, "（16-30）分钟角球比分"),
        MINUTES_CORNER_SCORE_45("minutesCornerScore_45", 23203L, 0, 0, "（31-45）分钟角球比分"),
        MINUTES_CORNER_SCORE_60("minutesCornerScore_60", 23204L, 0, 0, "（46-60）分钟角球比分"),
        MINUTES_CORNER_SCORE_75("minutesCornerScore_75", 23205L, 0, 0, "（61-75）分钟角球比分"),
        MINUTES_CORNER_SCORE_90("minutesCornerScore_90", 23206L, 0, 0, "（76-90）分钟角球比分");

        private String code;
        private Long subPlayId;
        private Integer firstNum;
        private Integer secondNum;
        private String text;

        public static String getCodeBySubPlayId(Long subPlayId) {
            for (Football football : Football.values()) {
                if (football.getSubPlayId().equals(subPlayId)) {
                    return football.getCode();
                }
            }
            return null;
        }
    }

    @Getter
    @AllArgsConstructor
    enum Basketball implements ScoreTypeEnum {
        FULL_TIME("match_score", 0, 0, "全场比分"),
        FIRST_HALF("period_score", 1, 0, "上半场比分"),
        SECOND_HALF("period_score", 2, 0, "下半场比分"),
        FIRST_SECTION("set_score", 1, 0, "第一节比分"),
        SECOND_SECTION("set_score", 2, 0, "第二节比分"),
        THIRD_SECTION("set_score", 3, 0, "第三节比分"),
        FOURTH_SECTION("set_score", 4, 0, "第四节比分");

        private String code;
        private Integer firstNum;
        private Integer secondNum;
        private String text;
    }

    String getCode();

    Integer getFirstNum();

    Integer getSecondNum();

    static ScoreTypeEnum getScoreTypeEnum(Long sportId, Long playId) {
        ScoreTypeEnum scoreTypeEnum = null;
        if (sportId == 1L) {
            if (playId == 4L || playId == 2L) {
                // 全场让球、全场大小
                scoreTypeEnum = Football.FULL_TIME;
            } else if (playId == 19L || playId == 18L) {
                // 上半场让球、上半场大小
                scoreTypeEnum = Football.FIRST_HALF;
            } else if (playId == 113L || playId == 114L) {
                // 角球让球、角球大小
                scoreTypeEnum = Football.CORNER_FULL_TIME;
            } else if (playId == 121L || playId == 122L) {
                // 上半场角球让球、上半场角球大小
                scoreTypeEnum = Football.CORNER_FIRST_HALF;
            } else if (playId == 128L || playId == 127L) {
                // 加时让球、加时大小
                scoreTypeEnum = Football.OVERTIME_FULL_TIME;
            } else if (playId == 130L) {
                // 加时上半场让球
                scoreTypeEnum = Football.OVERTIME_FIRST_HALF;
            }
        } else if (sportId == 2L) {
            if (playId == 39L || playId == 38L) {
                // 全场让球、全场大小
                scoreTypeEnum = Basketball.FULL_TIME;
            } else if (playId == 19L || playId == 18L) {
                // 上半场让球、上半场大小
                scoreTypeEnum = Basketball.FIRST_HALF;
            } else if (playId == 143L || playId == 26L) {
                // 下半场让球、下半场大小
                scoreTypeEnum = Basketball.SECOND_HALF;
            } else if (playId == 46L || playId == 45L) {
                // 第一节让球、第一节大小
                scoreTypeEnum = Basketball.FIRST_SECTION;
            } else if (playId == 52L || playId == 51L) {
                // 第二节让球、第二节大小
                scoreTypeEnum = Basketball.SECOND_SECTION;
            } else if (playId == 58L || playId == 57L) {
                // 第三节让球、第三节大小
                scoreTypeEnum = Basketball.THIRD_SECTION;
            } else if (playId == 64L || playId == 63L) {
                // 第四节让球、第四节大小
                scoreTypeEnum = Basketball.FOURTH_SECTION;
            }
        }
        return scoreTypeEnum;
    }
}
