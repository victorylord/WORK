package com.panda.sport.rcs.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.enums
 * @Description : 投注项类型枚举
 * @Author : Paca
 * @Date : 2020-08-01 13:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface OddsTypeEnum {

    String AND = "And";

    String OTHER = "Other";
    
    String DRAW_OTHER = "DrawOther";
    
    String HOME_OTHER = "HomeOther";
    
    String AWAY_OTHER = "AwayOther";

    String HOME = "1";

    String AWAY = "2";

    String DRAW = "X";
    //兼容数据支持下发的错误类型
    String DRAW_CW = "draw";
    
    String DRAW0 = "X0";
    
    String DRAW1 = "X1";

    String OVER = "Over";

    String UNDER = "Under";

    String NONE = "None";

    String OWN_GOAL = "OwnGoal";

    String ODD = "Odd";

    String EVEN = "Even";

    String YES = "Yes";

    String NO = "No";

    /**
     * 大小盘
     */
    @Getter
    @AllArgsConstructor
    enum Total implements OddsTypeEnum {
        OVER(OddsTypeEnum.OVER),
        UNDER(OddsTypeEnum.UNDER);

        private String oddsType;
    }

    /**
     * 净胜分
     */
    @Getter
    @AllArgsConstructor
    enum WinningMargin implements OddsTypeEnum {
        HOME_AND_ONE("1And1"),
        HOME_AND_TWO("1And2"),
        HOME_AND_THREE_PLUS("1And3+"),
        AWAY_AND_ONE("2And1"),
        AWAY_AND_TWO("2And2"),
        AWAY_AND_THREE_PLUS("2And3+"),
        DRAW(OddsTypeEnum.DRAW),
        DRAW0(OddsTypeEnum.DRAW0),
        DRAW1(OddsTypeEnum.DRAW1),
        OTHER(OddsTypeEnum.OTHER);

        private String oddsType;
    }

    String getOddsType();

    static boolean isHome(String oddsType) {
        return HOME.equals(oddsType);
    }

    static boolean isAway(String oddsType) {
        return AWAY.equals(oddsType);
    }

    static boolean isOver(String oddsType) {
        return OVER.equalsIgnoreCase(oddsType);
    }

    static boolean isUnder(String oddsType) {
        return UNDER.equalsIgnoreCase(oddsType);
    }

    static boolean isOdd(String oddsType) {
        return ODD.equalsIgnoreCase(oddsType);
    }

    static boolean isEven(String oddsType) {
        return EVEN.equalsIgnoreCase(oddsType);
    }

    static boolean isYes(String oddsType) {
        return YES.equalsIgnoreCase(oddsType);
    }

    static boolean isNo(String oddsType) {
        return NO.equalsIgnoreCase(oddsType);
    }

    static boolean isHomeOddsType(String oddsType) {
        return isHome(oddsType) || isOver(oddsType) || isOdd(oddsType) || isYes(oddsType);
    }

    static boolean isAwayOddsType(String oddsType) {
        return isAway(oddsType) || isUnder(oddsType) || isEven(oddsType) || isNo(oddsType);
    }

    static String getHomeOddsTypeByPlayId(Long playId) {
        if (Basketball.isHandicap(playId)) {
            return HOME;
        }
        if (Basketball.isTotal(playId)) {
            return OVER;
        }
        if (Basketball.isOddEven(playId)) {
            return ODD;
        }
        if (Basketball.isYesNo(playId)) {
            return YES;
        }
        return HOME;
    }

    static String getHomeOddsTypeByOddsType(String oddsType) {
        if (isHome(oddsType) || isAway(oddsType)) {
            return HOME;
        }
        if (isOver(oddsType) || isUnder(oddsType)) {
            return OVER;
        }
        if (isOdd(oddsType) || isEven(oddsType)) {
            return ODD;
        }
        if (isYes(oddsType) || isNo(oddsType)) {
            return YES;
        }
        return oddsType;
    }
}
