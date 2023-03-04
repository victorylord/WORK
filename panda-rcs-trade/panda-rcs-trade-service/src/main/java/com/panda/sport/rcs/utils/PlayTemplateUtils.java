package com.panda.sport.rcs.utils;

import com.google.common.collect.Lists;
import com.panda.sport.rcs.constants.Placeholder;
import com.panda.sport.rcs.constants.RcsConstant;
import com.panda.sport.rcs.enums.Basketball;
import com.panda.sport.rcs.enums.MarketCategoryEnum;
import com.panda.sport.rcs.enums.OddsTypeEnum;
import com.panda.sport.rcs.factory.BeanFactory;
import com.panda.sport.rcs.mongo.I18nBean;
import com.panda.sport.rcs.mongo.MarketCategory;
import com.panda.sport.rcs.mongo.MatchMarketOddsVo;
import com.panda.sport.rcs.mongo.MatchMarketVo;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 玩法模板工具类
 * @Author : Paca
 * @Date : 2020-11-19 20:39
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class PlayTemplateUtils {

    private static final String MINUS = "-";
    private static final String PLUS = "+";
    private static final String COLON = ":";
    private static final String SLASH = "/";
    private static final String BLANK_SPACE = " ";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String ESPERLUETTE ="&";
    private static final List<Long> FROM_TO_PLAYS = Lists.newArrayList(32L, 34L, 231L, 233L);
    private static final List<Long> FROM_TO_PLAYS_2 = Lists.newArrayList(33L, 232L);

    private static final List<Long> A1_PLAYS = Lists.newArrayList(28L, 30L, 31L, 109L, 110L, 120L, 125L, 133L, 148L, 201L, 208L, 214L, 222L, 224L, 225L, 230L, 235L, 237L, 255L, 261L, 266L, 267L, 275L, 283L,362L);
    private static final List<Long> A2_PLAYS = Lists.newArrayList(145L, 146L, 162L, 163L, 164L, 165L, 166L, 170L, 175L, 176L, 177L, 178L, 184L, 185L, 186L, 187L, 189L, 190L, 191L, 192L, 193L, 194L, 196L, 197L, 253L, 254L, 262L, 263L, 264L,265L, 268L,276L, 280L, 281L, 282L, 287L, 288L, 289L, 297L, 298L);

    private static final List<Long> A3_PLAYS = Lists.newArrayList( 357L, 336L);
    private static final List<Long> PLACEHOLDER_PLAYS;
    public static final List<Long> BASEBALL_SECTION= Arrays.asList(274L, 277L, 278L, 279L);

    public static final List<Long> SPECIAL_PLAYER_PLAYS= Arrays.asList(363L, 364L, 365L, 366L);
    
    


    static {
        PLACEHOLDER_PLAYS = Lists.newArrayList(147L, 167L, 168L, 179L, 188L, 195L, 203L, 215L, 256L);
        PLACEHOLDER_PLAYS.addAll(FROM_TO_PLAYS);
        PLACEHOLDER_PLAYS.addAll(FROM_TO_PLAYS_2);
        PLACEHOLDER_PLAYS.addAll(A1_PLAYS);
        PLACEHOLDER_PLAYS.addAll(A2_PLAYS);
    }

    private static final List<Long> ODDS_TYPE_1X2 = Lists.newArrayList(1L, 3L, 4L, 5L, 17L, 19L, 25L, 27L, 29L, 32L, 33L, 37L, 39L, 43L, 44L, 46L, 48L, 50L, 52L, 54L, 56L, 58L, 60L, 62L, 64L, 66L, 69L, 71L, 77L, 91L, 111L, 113L, 119L, 121L, 126L, 128L, 129L, 130L, 132L, 135L, 136L, 142L, 143L, 144L, 153L, 154L, 155L, 162L, 163L, 167L, 168L, 172L, 175L, 176L, 179L, 181L, 184L, 185L, 188L, 189L, 195L, 196L, 201L, 203L, 214L, 215L, 231L, 232L, 242L, 243L, 249L, 253L, 256L, 259L, 261L, 268L, 269L, 270L, 352L);
    private static final List<Long> CORRECT_SCORE = Lists.newArrayList(7L, 20L, 74L, 236L, 241L, 204L, 166L, 341L, 342L, 343L, 344L, 260L, 267L);
    private static final List<Long> EXACT_GOALS = Lists.newArrayList(8L, 9L, 14L, 21L, 22L, 23L, 73L, 239L);
    private static final List<Long> RANGE = Lists.newArrayList(31L, 68L, 117L, 190L, 210L, 218L, 226L, 227L, 228L, 239L);
    private static final List<Long> ODDS_TYPE_1x1AND0 = Lists.newArrayList(360L);

    /**
     * 是否显示盘口名称
     *
     * @param playId
     * @return
     */
    public static boolean isShowMarketName(Long playId) {
        return PLACEHOLDER_PLAYS.contains(playId);
    }

    /**
     * 盘口名称占位符替换
     *
     * @param market
     * @param teamMap
     */
    public static void handleMarketName(MatchMarketVo market, Map<String, I18nBean> teamMap) {
        I18nBean names = market.getNames();
        if (names == null) {
            return;
        }
        // 替换掉 主客队 占位符
        if (!CollectionUtils.isEmpty(teamMap)) {
            names.replaceTeam(teamMap);
        }
        Long playId = market.getMarketCategoryId();
        if (playId == 147L) {
            // A2,A1 -> 第{!quarternr}节首先获得{pointnr}分
            names.replaceQuarter(market.getAddition2());
            names.replacePoint(market.getAddition1());
        } else if (playId == 167L) {
            // A1,A2,A2+1 -> 第{!setnr}盘第{!gamenrX}局和第{!gamenrY}局谁获胜多
            names.replaceSet(market.getAddition1());
            names.replace(Placeholder.WHICH_GAME_X, market.getAddition2());
            BigDecimal gameY = CommonUtils.toBigDecimal(market.getAddition2(), BigDecimal.ZERO).add(BigDecimal.ONE);
            names.replace(Placeholder.WHICH_GAME_Y, gameY.toPlainString());
        } else if (playId == 168L) {
            // A1,A2 -> 第{!setnr}盘第{gamenr}局获胜
            names.replaceSet(market.getAddition1());
            names.replaceGame(market.getAddition2());
        } else if (playId == 179L) {
            // A2,A1 -> 第{!gamenr}局第{!pointnr}分
            names.replaceGame(market.getAddition2());
            names.replacePoint(market.getAddition1());
        } else if (playId == 188L) {
            // A2,A1 -> 第{!framenr}局首先到达{pointnr}分
            names.replaceFrame(market.getAddition2());
            names.replacePoint(market.getAddition1());
        } else if (playId == 195L) {
            // A2,A1 -> 第{!framenr}局第{!xth}个进球的选手
            names.replaceFrame(market.getAddition2());
            names.replaceXth(market.getAddition1());
        } else if (playId == 203L) {
            // A1,A2 -> 第{!gamenr}局首先获得{pointnr}分
            names.replaceGame(market.getAddition1());
            names.replacePoint(market.getAddition2());
        } else if (playId == 215L) {
            // A1,A2 -> 第{!quarternr}节首先获得{pointnr}分
            names.replaceQuarter(market.getAddition1());
            names.replacePoint(market.getAddition2());
        } else if (playId == 256L) {
            // A1,A2 -> 第{!setnr}局谁先获得{pointnr}分
            names.replaceSet(market.getAddition1());
            names.replacePoint(market.getAddition2());
        } else if (FROM_TO_PLAYS.contains(playId)) {
            // 替换 15分钟 玩法中的 {from} 和 {to} 占位符
            names.replaceFromAndTo(market.getAddition2(), market.getAddition3());
        } else if (FROM_TO_PLAYS_2.contains(playId)) {
            // 替换 15分钟 玩法中的 {from} 和 {to} 占位符
            String from = "";
            String to = "";
            String addition5 = market.getAddition5();
            if (StringUtils.isNotBlank(addition5)) {
                String[] array = addition5.split(",");
                if (array.length > 0) {
                    from = array[0];
                }
                if (array.length > 1) {
                    to = array[1];
                }
            }
            names.replaceFromAndTo(from, to);
        } else if (A1_PLAYS.contains(playId)) {
            // A1
            names.replaceWithA1(market.getAddition1());
        } else if (A2_PLAYS.contains(playId)) {
            // A2
            names.replaceWithA2(market.getAddition2());
//        } else if (RcsConstant.BASK_GOALSCORER.contains(playId) && StringUtils.isNotBlank(market.getOddsName())) {
//            market.setAddition3(market.getOddsName());
        } else if (A3_PLAYS.contains(playId)) {
            names.replaceInningnrX(market.getAddition1());
        } else if(BASEBALL_SECTION.contains(playId)){
            //棒球区间转义
            names.baseBallSectionReplace(market.getAddition2(),market.getAddition3());
        } else if(SPECIAL_PLAYER_PLAYS.contains(playId)){
            //第{X}个进球球员
            names.replaceInningnrX(market.getAddition1());
        }
    }

    /**
     * 玩法名称占位符替换
     *
     * @param category
     * @param teamMap
     */
    public static void handlePlayName(MarketCategory category, Map<String, I18nBean> teamMap) {
        I18nBean names = category.getNames();
        if (names == null) {
            return;
        }
        if (!CollectionUtils.isEmpty(teamMap)) {
            // 替换掉 主客队 占位符
            names.replaceTeam(teamMap);
        }
        Long playId = category.getId();
        if (playId == 147L) {
            names.replaceQuarter(X);
            names.replacePoint(Y);
        } else if (playId == 167L) {
            names.replaceSet(X);
            names.replace(Placeholder.WHICH_GAME_X, Y);
            names.replace(Placeholder.WHICH_GAME_Y, Y + "+1");
        } else if (playId == 168L) {
            names.replaceSet(X);
            names.replaceGame(Y);
        } else if (playId == 179L) {
            names.replaceGame(X);
            names.replacePoint(Y);
        } else if (playId == 188L) {
            names.replaceFrame(X);
            names.replacePoint(Y);
        } else if (playId == 195L) {
            names.replaceFrame(X);
            names.replaceXth(Y);
        } else if (playId == 203L) {
            names.replaceGame(X);
            names.replacePoint(Y);
        } else if (playId == 215L) {
            names.replaceQuarter(X);
            names.replacePoint(Y);
        } else if (playId == 256L) {
            names.replaceSet(X);
            names.replacePoint(Y);
        } else if (FROM_TO_PLAYS.contains(playId) || FROM_TO_PLAYS_2.contains(playId)) {
            names.replaceFromAndTo(X, Y);
        } else if (A1_PLAYS.contains(playId)) {
            // A1
            names.replaceWithA1(X);
        } else if (A2_PLAYS.contains(playId)) {
            // A2
            names.replaceWithA2(X);
        } else if (SPECIAL_PLAYER_PLAYS.contains(playId)) {
            // 特殊球员玩法
            names.replaceInningnrX(X);
        }
    }

    public static void handleMarketOdds(Long playId, Integer templateId, MatchMarketOddsVo marketOdds, Map<String, I18nBean> teamMap) {
        String oddsType = marketOdds.getOddsType();
        if (RcsConstant.TOTAL.contains(playId) || Basketball.Secondary.PLAYER.getPlayIds().contains(playId)) {
            // 大小盘
            if (OddsTypeEnum.Total.OVER.getOddsType().equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 1, 1, Placeholder.OVER, BeanFactory.getOverI18n());
            } else if (OddsTypeEnum.Total.UNDER.getOddsType().equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, 2, Placeholder.UNDER, BeanFactory.getUnderI18n());
            }
        } else if (RcsConstant.OVER_X_UNDER.contains(playId)) {
            if (OddsTypeEnum.OVER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 1, 1, Placeholder.OVER, BeanFactory.getOverI18n());
            } else if (OddsTypeEnum.UNDER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, 3, Placeholder.UNDER, BeanFactory.getUnderI18n());
            } else if (OddsTypeEnum.DRAW.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, 2, Placeholder.DRAW, BeanFactory.getDrawI18n());
            }
        } else if (ODDS_TYPE_1X2.contains(playId)) {
            if (OddsTypeEnum.HOME.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 1, 1, Placeholder.HOME, I18nBean.getHome(teamMap));
            } else if (OddsTypeEnum.AWAY.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, 3, Placeholder.AWAY, I18nBean.getAway(teamMap));
            } else if (OddsTypeEnum.DRAW.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, 2, Placeholder.DRAW, BeanFactory.getDrawI18n());
            }
        }  else if (ODDS_TYPE_1x1AND0.contains(playId)) {
        	//多重玩法，且无投注项模板，因此只能风控自助根据oddsType进行装换
            if (StringUtils.containsIgnoreCase(oddsType, OddsTypeEnum.AND)) {
            	String oddsConversion = "";
                int index = 0;
                String[] array = oddsType.split(OddsTypeEnum.AND);
                if (array != null && array.length == NumberUtils.INTEGER_TWO) {
                    if (StringUtils.contains(oddsType, SLASH)) {
                        String[] arrayTwo = StringUtils.split(array[0], SLASH);
                        if (arrayTwo != null && arrayTwo.length == NumberUtils.INTEGER_TWO) {
                            if (OddsTypeEnum.HOME.equals(arrayTwo[0])) {
                            	index=1;
                            	oddsConversion+=Placeholder.HOME;
                            }else if (OddsTypeEnum.AWAY.equals(arrayTwo[0])) {
                            	index=3;
                            	oddsConversion+=Placeholder.AWAY;
                            }else if (OddsTypeEnum.DRAW.equals(arrayTwo[0]) || OddsTypeEnum.DRAW_CW.equals(arrayTwo[0])) {
                            	index=2;
                            	oddsConversion+=Placeholder.DRAW;
                            }
                            oddsConversion+=SLASH;
                            if (OddsTypeEnum.HOME.equals(arrayTwo[1])) {
                            	oddsConversion+=Placeholder.HOME;
                            }else if (OddsTypeEnum.AWAY.equals(arrayTwo[1])) {
                            	oddsConversion+=Placeholder.AWAY;
                            }else if (OddsTypeEnum.DRAW.equals(arrayTwo[1]) || OddsTypeEnum.DRAW_CW.equals(arrayTwo[1])) {
                            	oddsConversion+=Placeholder.DRAW;
                            } 
                        }
                    }
                    oddsConversion+=BLANK_SPACE+ESPERLUETTE+BLANK_SPACE+array[1];
	                marketOddsSetValue(marketOdds, index, index, marketOdds.getName(), I18nBean.getAXB(oddsConversion,teamMap));
                }
            }
        }
        else if (RcsConstant.WINNING_MARGIN.contains(playId) && templateId == 5) {
            // 净胜分
            if (StringUtils.containsIgnoreCase(oddsType, OddsTypeEnum.AND)) {
                String[] array = StringUtils.split(oddsType, OddsTypeEnum.AND);
                if (array != null && array.length == NumberUtils.INTEGER_TWO) {
                    int sortNo = getSortNo(array[1]);
                    if (sortNo < Integer.MAX_VALUE / BigInteger.TEN.intValue()) {
                        sortNo = NumberUtils.toInt(sortNo + StringUtils.EMPTY + array[0], sortNo);
                    }
                    // 投注项名称、标题名称
                    if (OddsTypeEnum.HOME.equals(array[0])) {
                        marketOddsSetValue(marketOdds, 1, sortNo, Placeholder.HOME, I18nBean.getHome(teamMap));
                    } else if (OddsTypeEnum.AWAY.equals(array[0])) {
                        marketOddsSetValue(marketOdds, 2, sortNo, Placeholder.AWAY, I18nBean.getAway(teamMap));
                    }
                }
            } else if (OddsTypeEnum.OTHER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, Integer.MAX_VALUE, Placeholder.OTHER, BeanFactory.getOtherI18n());
            } else if (OddsTypeEnum.DRAW.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, Integer.MAX_VALUE, Placeholder.DRAW, BeanFactory.getDrawI18n());
            } else if (OddsTypeEnum.NONE.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, Integer.MAX_VALUE, Placeholder.NONE, BeanFactory.getNoneI18n());
            } else if (OddsTypeEnum.DRAW0.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, Integer.MAX_VALUE, Placeholder.DRAW0, BeanFactory.getNoneI18n());
            } else if (OddsTypeEnum.DRAW1.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 3, Integer.MAX_VALUE, Placeholder.DRAW1, BeanFactory.getDraw1I18n());
            }
        }
        else if (CORRECT_SCORE.contains(playId)) {
            // 比分
            if (StringUtils.contains(oddsType, COLON)) {
                String[] array = StringUtils.split(oddsType, COLON);
                if (array != null && array.length == NumberUtils.INTEGER_TWO) {
                    int home = NumberUtils.toInt(array[0], 0);
                    int away = NumberUtils.toInt(array[1], 0);
                    if (home > away) {
                        int sortNo = NumberUtils.toInt("1" + array[0] + array[1], Integer.MAX_VALUE);
                        marketOddsSetValue(marketOdds, 1, sortNo, Placeholder.HOME, new I18nBean(oddsType));
                    } else if (home < away) {
                        int sortNo = NumberUtils.toInt("3" + array[1] + array[0], Integer.MAX_VALUE);
                        marketOddsSetValue(marketOdds, 3, sortNo, Placeholder.AWAY, new I18nBean(oddsType));
                    } else {
                        int sortNo = NumberUtils.toInt("2" + array[0] + array[1], Integer.MAX_VALUE);
                        marketOddsSetValue(marketOdds, 2, sortNo, Placeholder.DRAW, new I18nBean(oddsType));
                    }
                }else if (array != null && array.length > NumberUtils.INTEGER_TWO) {
                	//344 多重波胆投注
                	String[] slashArray = StringUtils.split(oddsType, SLASH);
                	if(slashArray.length>1) {
                		array = StringUtils.split(slashArray[0], COLON);
                        int home = NumberUtils.toInt(array[0], 0);
                        int away = NumberUtils.toInt(array[1], 0);
                        if (home > away) {
                            int sortNo = NumberUtils.toInt("1" + array[0] + array[1], Integer.MAX_VALUE);
                            marketOddsSetValue(marketOdds, 1, sortNo, Placeholder.HOME, new I18nBean(oddsType));
                        } else if (home < away) {
                            int sortNo = NumberUtils.toInt("3" + array[1] + array[0], Integer.MAX_VALUE);
                            marketOddsSetValue(marketOdds, 3, sortNo, Placeholder.AWAY, new I18nBean(oddsType));
                        } else {
                            int sortNo = NumberUtils.toInt("2" + array[0] + array[1], Integer.MAX_VALUE);
                            marketOddsSetValue(marketOdds, 2, sortNo, Placeholder.DRAW, new I18nBean(oddsType));
                        }
                	}
                }
            } else if (OddsTypeEnum.OTHER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, Integer.MAX_VALUE, Placeholder.OTHER, BeanFactory.getOtherI18n());
            } else if (OddsTypeEnum.AWAY_OTHER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, Integer.MAX_VALUE, Placeholder.AWAY_OTHER, BeanFactory.getAwayOtherI18n());
            } else if (OddsTypeEnum.DRAW_OTHER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, Integer.MAX_VALUE, Placeholder.DRAW_OTHER, BeanFactory.getDarwOtherI18n());
            } else if (OddsTypeEnum.HOME_OTHER.equalsIgnoreCase(oddsType)) {
                marketOddsSetValue(marketOdds, 2, Integer.MAX_VALUE, Placeholder.HOME_OTHER, BeanFactory.getHomeOtherI18n());
            }
        } else if (RcsConstant.GOALSCORER.contains(playId)) {
            if (OddsTypeEnum.OTHER.equalsIgnoreCase(oddsType)) {
                marketOdds.setNames(BeanFactory.getOtherI18n());
            } else if (OddsTypeEnum.NONE.equalsIgnoreCase(oddsType)) {
                marketOdds.setNames(BeanFactory.getNoneI18n());
            } else if (OddsTypeEnum.OWN_GOAL.equalsIgnoreCase(oddsType)) {
                marketOdds.setNames(BeanFactory.getOwnGoalI18n());
            } else {
                marketOdds.setNames(new I18nBean(marketOdds.getName()));
            }
            String addition2 = marketOdds.getAddition2();
            if (OddsTypeEnum.HOME.equalsIgnoreCase(addition2)) {
                marketOddsSetValue(marketOdds, 1, Placeholder.HOME);
            } else if (OddsTypeEnum.AWAY.equalsIgnoreCase(addition2)) {
                marketOddsSetValue(marketOdds, 2, Placeholder.AWAY);
            } else {
                marketOddsSetValue(marketOdds, 3, Placeholder.OTHER);
            }
        }else {
            marketOdds.setSortNo(marketOdds.getOrderOdds());
            // 投注项类型 设置为 投注项名称
            if (MarketCategoryEnum.isSetOddsNameByOddsType(playId)) {
                if (MarketCategoryEnum.isGoalscorer(playId)) {//进球球员
                    marketOdds.setNames(new I18nBean(marketOdds.getName()));
                } else {
                    if (OddsTypeEnum.OTHER.equalsIgnoreCase(oddsType)) {
                        marketOdds.setNames(BeanFactory.getOtherI18n());
                    } else {
                        marketOdds.setNames(new I18nBean(oddsType));
                    }
                }
            } else {
                I18nBean names = marketOdds.getNames();
                if (names != null) {
                    // 替换掉 主客队 占位符
                    names.replaceTeam(teamMap);
                    // {total} 替换为 nameExpressionValue
                    if (RcsConstant.TOTAL_AND_OTHER.contains(playId)) {
                        if (playId == 217L) {
                            if (isNumber(marketOdds.getNameExpressionValue())) {
                                names.replaceTotal(new BigDecimal(marketOdds.getNameExpressionValue()).abs().toPlainString());
                            }
                        } else {
                            names.replaceTotal(marketOdds.getNameExpressionValue());
                        }
                    }
                }
            }
            // 准确进球数类、区间类玩法 投注项排序处理
            if (EXACT_GOALS.contains(playId) || RANGE.contains(playId)) {
                marketOdds.setSortNo(getSortNo(oddsType));
                marketOdds.setOrderOdds(marketOdds.getSortNo());
            }
        }
    }

    private static int getSortNo(String oddsType) {
        if (StringUtils.isBlank(oddsType)) {
            return Integer.MAX_VALUE;
        }
        try {
            if (StringUtils.contains(oddsType, MINUS)) {
                String[] array = oddsType.split(MINUS);
                String value = Double.valueOf(array[0]).intValue() + StringUtils.EMPTY + Double.valueOf(array[1]).intValue();
                return Integer.parseInt(value);
            } else if (StringUtils.contains(oddsType, PLUS)) {
                return Double.valueOf(StringUtils.replace(oddsType, PLUS, StringUtils.EMPTY)).intValue() * 1000000;
            } else {
                return Double.valueOf(oddsType).intValue();
            }
        } catch (Throwable t) {
            return Integer.MAX_VALUE;
        }
    }

    private static void marketOddsSetValue(MatchMarketOddsVo marketOdds, int groupId, int sortNo, String titleName, I18nBean names) {
        marketOdds.setGroupId(groupId);
        marketOdds.setSortNo(sortNo);
        marketOdds.setOrderOdds(sortNo);
        marketOdds.setTitleName(titleName);
        marketOdds.setNames(names);
    }

    private static void marketOddsSetValue(MatchMarketOddsVo marketOdds, int groupId, String titleName) {
        marketOdds.setGroupId(groupId);
        marketOdds.setSortNo(marketOdds.getOrderOdds());
        marketOdds.setTitleName(titleName);
    }

    private static boolean isNumber(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
