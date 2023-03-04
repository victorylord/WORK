package com.panda.sport.rcs.pojo.constants;

import com.google.common.collect.Lists;
import com.panda.sport.rcs.constants.BaseConstants;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * @author :  sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.pojo.constants
 * @Description :  TODO
 * @Date: 2021-01-08 15:36
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class TradeConstant implements BaseConstants {

    public static String MATCH_STATUS_1 = "1";
    public static String MATCH_STATUS_2 = "2";
    public static String MAX_MY_ODDS = "-0.01";
    public static String MIN_MY_ODDS = "0.01";
    public static String MAX_EU_ODDS = "100";
    public static String MIN_EU_ODDS = "1.01";
    public static String DEFAULT_MY_SPREAD = "0.1";
    public static String DEFAULT_BET_MAX = "1000000";
    public static String DEFAULT_EU_MARGIN = "110";
    public static String MATCH_STATUS_10 = "10";
    public static String MATCH_STATUS_MTS = "MTS";
    public static String DEFAULT_AUTO_RATIO_MAX = "0.3";
    public static String DEFAULT_AUTO_RATIO_MIN = "-0.3";

    public static String ODD_TYPE_FIRSTHALF = "FirstHalf";
    public static String ODD_TYPE_SECONDHALF = "SecondHalf";
    public static String ODD_TYPE_EQUALS = "Equals";
    public static String ODD_TYPE_NONE = "None";
    public static String ODD_TYPE_X2 = "X2";
    public static String ODD_TYPE_1X = "1X";
    public static String ODD_TYPE_12 = "12";
    public static String ODD_TYPE_5_6 = "5-6";
    public static String ODD_TYPE_7 = "7+";
    public static String ODD_TYPE_0_4 = "0-4";
    public static String ODD_TYPE_0_8 = "0-8";
    public static String ODD_TYPE_12_OVER = "12+";
    public static String ODD_TYPE_9_11 = "9-11";

    public static String MARKET_VALUE_MIN_HIC = "0.5";
    public static String MIN_MARGIN = "0.5";
    public static String MAX_MARGIN = "1.5";

    public static String EU_MIN_MARGIN = "101";
    public static String EU_MAX_MARGIN = "150";

    public static Long IS_OVERTIME_PLAY = 41L;
    public static String OTHER_PLAY = "other";
    public static String TIMEOUT = "timeout";

    // 足球基准分玩法
    public static List<Integer> FOOTBALL_BENCHMARK_SCORE_PLAYS = Arrays.asList(4,19,128,130,143,269,270,33,113,121,232,306,308,327,324,334);
//    // 足球基准分玩法
//    public static List<Integer> FOOTBALL_SECONDARY_BENCHMARK_SCORE_PLAYS = Arrays.asList(113,121,334,306,308,324,327);
    // 足球大小玩法
    public static List<Integer> FOOTBALL_OVER_UNDER_PLAYS = Arrays.asList(2,18,26,114,122,127,307,325,328,331,332,34,233);
    // 足球其他大小玩法
    public static List<Integer> FOOTBALL_SECONDARY_ADDITION1_PLAYS = Arrays.asList(3,10,11,69,71,87,88,97,98,115,116,123,124,314,315,316,317,335,134,309);
    // 足球其他让分大小玩法
    public static List<Integer> FOOTBALL_SECONDARY_HANDICAP_PLAYS = Arrays.asList(3);
    // 足球其他大小玩法
    public static List<Integer> FOOTBALL_SORCE_PLAYS = Arrays.asList(27,29);
    // 足球多项盘玩法
    public static List<Integer> FOOTBALL_MOST_ODDS_TYPE_PLAYS = Arrays.asList(35,7,141,8,9,14,68,223,20,104,21,22,23,103,108,74,73,13,101,102,105,106,107,137,36,150,151,152,226,227,236,238,239,241,318,319,320,321,322,323,340,344,345,346,347,348,349,350,351,353,360,361,362);
    // 篮球独赢玩法
    public static List<Integer> BASKETBALL_SINGLE_WIN_PLAYS = Arrays.asList(37,43,48,54,60,66,142);
    // 篮球是否加时玩法
    public static List<Integer> BASKETBALL_OVERTIME_PLAYS = Arrays.asList(41);
    // 快捷修改盘口值玩法
    public static List<Integer> FOOTBALL_MARKET_VALUE__UPDATE_PLAYS = Arrays.asList(2,4,18,19,113,114,121,122,127,128,134,332);
    // 投注项排序
    public static List<String> FOOTBALL_ODDS_TYPE_ORDER_1 = Arrays.asList("1","1X","FirstHalf");
    public static List<String> FOOTBALL_ODDS_TYPE_ORDER_2 = Arrays.asList("X","12","SecondHalf","None");
    public static List<String> FOOTBALL_ODDS_TYPE_ORDER_3 = Arrays.asList("2","X2","Equals");
    public static List<Integer> FOOTBALL_ODDS_TYPE_ORDER_NONE = Arrays.asList(112,149);
    public static List<Integer> FOOTBALL_ODDS_TYPE_ORDER_NUMBER = Arrays.asList(228,117);
    public static List<Integer> FOOTBALL_ODDS_TYPE_ORDER_X2 = Arrays.asList(77);
    public static List<Integer> FOOTBALL_ODDS_TYPE_ORDER_1X = Arrays.asList(91);
    // 篮球主要欧赔玩法
    public static List<Integer> BASKETBALL_MAIN_EU_PLAYS = Arrays.asList(5,37,41,43,48,54,60,66,142);
    // 网球主要欧赔玩法
    public static List<Integer> TANNIS_MAIN_EU_PLAYS = Arrays.asList(5,37,41,43,48,54,60,66,142);
    // 足球球主要欧赔玩法
    public static List<Integer> FOOTBALL_MAIN_EU_PLAYS = Arrays.asList(1,5,6,3,27,17,70,69,29,85,95,16,25,72,71,149,111,117,119,112,126,129,310,311,326,329,228,43,142,333,352,354,355,356,357,358);
    // 篮球主要总分玩法
    public static List<Integer> BASKETBALL_TATAL_PLAYS = Arrays.asList(198,199,145,146,87,88,97,98);

    public static String ODDS_RULE_ERROR = "赔率不符合规则";

    public static final String RCS_TRADE_MATCH_ODDS_CONFIG = "RCS_TRADE_MATCH_ODDS_CONFIG";
    /**清除平衡值*/
    public static final String RCS_TRADE_CLEAR_BALANCE_TOP = "RCS_TRADE_CLEAR_BALANCE_TOPIC";
    /**
     * 主要玩法-篮球-让分，全场
     */
    public static Long MAIN_BASKETBALL_TOTAL_HANDICAP = 39L;

    /**
     * 主要玩法-篮球-大小，全场/上半场/下半场/第一节/第二节/第三节/第四节
     */
    public static final List<Long> MAIN_BASKETBALL_TOTAL = Lists.newArrayList(38L, 18L, 26L, 45L, 51L, 57L, 63L);

    /**
     * 主要玩法-篮球-单双，全场/上半场/下半场/第一节/第二节/第三节/第四节
     */
    List<Long> MAIN_BASKETBALL_ODD_EVEN = Lists.newArrayList(40L, 42L, 75L, 47L, 53L, 59L, 65L);

    /**
     * 净胜分
     */
    List<Long> WINNING_MARGIN = Lists.newArrayList(49L, 55L, 61L, 67L, 141L, 200L, 209L, 211L, 212L, 219L, 238L, 340L);

    public static final String WATER_OUT_OF_LIMIT = "正确的范围为：-30%~30%，当前设置为：xx%";
    public static final String ODDS_OUT_OF_LIMIT = "正确的范围为：%s~%s，当前设置为：%s";

    public static final String DATA_SOURCE_CODE_PA = "PA";


    public static List<Integer> FOOTBALL_CAN_CREATE_MY_PLAYS = Lists.newArrayList(4,2,12,15,78,92,81,82,79,80,77,91,10,11,19,18,42,43,24,87,97,90,100,83,86,93,96,84,94,143,26,75,76,88,98,89,99,142,135,136,144,138,139,140,114,122,113,121,118,229,115,116,123,124,127,128,130,131,234,335,334,134,240,307,309,312,313,306,308,314,315,316,317,324,325,327,328,330,331,332,269,270,132);
//    public static List<Integer> FOOTBALL_CAN_CREATE_EU_PLAYS = Lists.newArrayList(1,5,6,3,27,17,70,69,29,85,95,16,25,72,71,149,111,117,119,112,126,129,310,311,326,329,228,43,142,333);
    // 足球带参数玩法
    public static List<Integer> FOOTBALL_X_NO_INSERT_PLAYS = Lists.newArrayList(31,148,222,363,364,365,366,362);

    // 足球带参数玩法
    public static List<Integer> FOOTBALL_X_INSERT_PLAYS = Lists.newArrayList(336,28,30,109,110,34,32,33,233,225,120,125,230,231,232,224,235,133,237,357,362);

    public static List<Integer> FOOTBALL_X_A1_PLAYS = Lists.newArrayList(336,28,30,109,110,225,120,125,230,232,224,235,133,237,357);
    public static List<Integer> FOOTBALL_X_A2_PLAYS = Lists.newArrayList(145,146);
    public static List<Integer> FOOTBALL_X_A3_PLAYS = Lists.newArrayList(34,32,33,233,231,232);
    public static List<Integer> FOOTBALL_X_15M_GOAL_SCORE_PLAYS = Lists.newArrayList(34,32,33);
    public static List<Integer> FOOTBALL_X_15M_CORNER_SCORE_PLAYS = Lists.newArrayList(233,231,232);
    public static List<Integer> FOOTBALL_X_A1_A2_PLAYS = Lists.newArrayList(215);
    public static List<Integer> FOOTBALL_X_A2_A1_PLAYS = Lists.newArrayList(147);
    public static List<Integer> FOOTBALL_X_A1_A2_A3_PLAYS = Lists.newArrayList(34,33,233,232);
    public static List<Integer> FOOTBALL_X_A5_PLAYS = Lists.newArrayList(33,232);

    public static List<Integer> FOOTBALL_X_EU_PLAYS = Lists.newArrayList(28,30,32,225,120,125,230,231,224,235,237);

    public static List<Integer> FOOTBALL_X_GOAL_PLAYS = Lists.newArrayList(336,28,30,109,110,225,120,125,230,224,235,133,237,357);



    // 蓝球带参数玩法
    public static List<Integer> BASKETBALL_X_PLAYS = Lists.newArrayList(220,221,271,272,145,146,147,215,201,214);
    // 可新增蓝球带参数玩法
    public static List<Integer> BASKETBALL_X_INSERT_PLAYS = Lists.newArrayList(145,146,147,215,201,214);
    // 可新增蓝球带参数玩法
    public static List<Integer> BASKETBALL_X_SCORCE_PLAYS = Lists.newArrayList(201,214);
    // 蓝球带参数球员类玩法
    public static List<Integer> FOOTBALL_X_PLAYER_PLAYS = Lists.newArrayList(220,221,271,272);

//    public static List<Integer> BASKETBALL_X_A2_PLAYS = Lists.newArrayList(145,146);
//    public static List<Integer> BASKETBALL_X_A1_A2_PLAYS = Lists.newArrayList(215);
//    public static List<Integer> BASKETBALL_X_A2_A1_PLAYS = Lists.newArrayList(147);
// 蓝球带参数欧赔玩法
    public static List<Integer> BASKETBALL_X_EU_PLAYS = Lists.newArrayList(147,201,215);

    // 统计跳分次数
    public static final String RCS_COUNT_TIMES = "rcs:risk:change:count:times:%s";
    // 统计跳分次数
    public static final String RCS_MARKET_TIMES = "rcs:risk:change:match:times:%s";

    public static final String RCS_TRADE_MARKET_S_MATCH = "rcs:trade:market:s:match:%s";

    // 需要统计跳分次数的玩法
    public static final List<Integer> RCS_COUNT_TIMES_PLAY = Lists.newArrayList(1,2,4,17,18,19);
    // 独赢盘口级别开盘清次数
    public static final List<Integer> THREE_ODDS_TYPE = Lists.newArrayList(1,17);

    /***************************************/
    public static List<Long> OTHER_SPORT_PLAYS = Lists.newArrayList(1L,153L,184L,204L,190L,197L,180L,181L,182L,183L,185L,186L,187L,188L,189L,191L,192L,193L,194L,195L,196L,175L,174L,172L,173L,176L,177L,178L,179L,203L,159L,162L,253L,254L,255L,256L);
    // 赛事赔率
    //public static String REDIS_MATCH_MARKET_ODDS = "rcs:redis:match:odds:%s";
    public static String REDIS_MATCH_MARKET_ODDS = "rcs:playId:%s:redis:match:%s:odds";
    //public static String REDIS_MATCH_MARKET_ODDS = "rcs:playId:%s:redis:match:%s:odds";
    //1666需求
    public static String REDIS_MATCH_MARKET_ODDS_NEW = "rcs:redis:playId:%s:match:%s:odds:new";

    // 子玩法配置
    public static String REDIS_MATCH_MARKET_SUB_CONFIG = "rcs:redis:match:market:sub:config:%s:%s:%s";
    // 盘口差配置
    public static String REDIS_MATCH_MARKET_HEAD_CONFIG = "rcs:redis:match:market:head:%s:%s:%s";
    // 玩法配置
    public static String REDIS_MATCH_MARKET_CONFIG = "rcs:redis:match:market:config:%s:%s";
    // 水差
    public static String REDIS_MATCH_MARKET_WATER = "rcs:redis:match:market:water:%s";
    // 足球大小玩法
    public static List<Integer> FOOTBALL_HEAD_CHECK_PLAYS = Arrays.asList(2,10,11,18,26,34,87,88,97,98,114,115,116,122,123,124,127,134,233,307,309,314,315,316,317,325,328,331,332,335);
    // 其他球种
    public static List<Integer> OTHER_BALL = Lists.newArrayList(6,11,12,13,14,15,16);
    // 足篮外的可操盘赛种
    public static List<Integer> OTHER_CAN_TRADE_SPORT = Lists.newArrayList(3,4,5,7,8,9,10);

    //默认spread
    public static BigDecimal DEFAULT_SPREAD = new BigDecimal("0.4");

    // 投注货量动态风控开关配置
    public static final String RCS_BET_VOLUME_CONFIG = "rcs:trade:bet:volume:config";

    // 篮球单双玩法增加A+操盘模式
    public static final List<Long> BASKETBALL_SINGLE_DOUBLE_PLAY = Arrays.asList(40L, 42L, 47L, 53L, 59L, 65L, 75L, 15L);

    //用于联赛模板做业务判断
    public static final List<Integer> TEMPLATE_BUSI_PLAY = Arrays.asList(6,70,72,107,347);

    // 冰球切换A模式不推赔率玩法
    public static final List<Long> ICE_HOCKEY_NO_PUSHODDS_PLAY = Arrays.asList(259L);
}
