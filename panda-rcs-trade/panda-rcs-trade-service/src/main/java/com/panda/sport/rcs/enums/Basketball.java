package com.panda.sport.rcs.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.panda.sport.rcs.constants.I18iConstants;
import com.panda.sport.rcs.utils.I18nUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <a href="http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=32349270">篮球两项盘全部玩法</a>
 *
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 篮球
 * @Author : Paca
 * @Date : 2021-02-18 10:54
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public interface Basketball {

    /**
     * 滚球切A+玩法
     */
    Long[] LIVE_AUTO_PLUS_PLAY = new Long[]{39L, 38L, 19L, 18L, 46L, 45L, 40L, 42L, 47L, 53L, 59L, 65L, 75L, 15L};

    /**
     * 15分钟玩法
     */
    Long[] FIFTEEN_MINUTE_PLAY = new Long[]{32L, 33L, 34L, 231L, 232L, 233L};

    /**
     * 获取占位符玩法
     */
    static List<Long> getPlaceholderPlayIds() {
        return Lists.newArrayList(145L, 146L, 147L, 201L, 214L, 215L, 220L, 221L, 271L, 272L);
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

    @Getter
    @AllArgsConstructor
    enum Main {
        FULL_TIME(37L, 39L, 38L, 40L, "全场"),
        FIRST_HALF(43L, 19L, 18L, 42L, "上半场"),
        SECOND_HALF(142L, 143L, 26L, 75L, "下半场"),
        FIRST_SECTION(48L, 46L, 45L, 47L, "第一节"),
        SECOND_SECTION(54L, 52L, 51L, 53L, "第二节"),
        THIRD_SECTION(60L, 58L, 57L, 59L, "第三节"),
        FOURTH_SECTION(66L, 64L, 63L, 65L, "第四节");

        private Long winAlone;
        private Long handicap;
        private Long total;
        private Long oddEven;
        private String name;

        public List<Long> getPlayIds() {
            return Lists.newArrayList(this.getWinAlone(), this.getHandicap(), this.getTotal(), this.getOddEven());
        }

        /**
         * 获取让球玩法对应的独赢玩法
         *
         * @param playId
         * @return
         */
        public static Long getWinAloneByHandicap(Long playId) {
            for (Main main : Main.values()) {
                if (main.getHandicap().equals(playId)) {
                    return main.getWinAlone();
                }
            }
            return FULL_TIME.getWinAlone();
        }

        public static List<Long> getWinAlonePlayIds() {
            return Stream.of(Main.values()).map(Main::getWinAlone).collect(Collectors.toList());
        }

        public static List<Long> getHandicapPlayIds() {
            return Stream.of(Main.values()).map(Main::getHandicap).collect(Collectors.toList());
        }

        public static List<Long> getTotalPlayIds() {
            return Stream.of(Main.values()).map(Main::getTotal).collect(Collectors.toList());
        }

        public static List<Long> getOddEvenPlayIds() {
            return Stream.of(Main.values()).map(Main::getOddEven).collect(Collectors.toList());
        }

        public static boolean isHandicap(Long playId) {
            return getHandicapPlayIds().contains(playId);
        }

        public static boolean isTotal(Long playId) {
            return getTotalPlayIds().contains(playId);
        }

        public static boolean isOddEven(Long playId) {
            return getOddEvenPlayIds().contains(playId);
        }

        public static boolean isHandicapOrTotal(Long playId) {
            return isHandicap(playId) || isTotal(playId);
        }
    }

    @Getter
    @AllArgsConstructor
    enum Secondary {
        TEAM(new Long[]{198L, 199L, 87L, 97L, 145L, 146L, 88L, 98L}, "球队"),
        PLAYER(new Long[]{220L, 221L, 271L, 272L}, "球员"),
        FULL_TIME(new Long[]{41L, 201L, 214L, 215L, 147L}, "全场"),
        REGULAR(new Long[]{5L, 2L, 10L, 11L, 15L}, "常规赛段"),

        YES_NO(new Long[]{41L}, "是否类玩法");

        private Long[] playIdArr;
        private String name;

        public List<Long> getPlayIds() {
            return Lists.newArrayList(this.getPlayIdArr());
        }
    }

    /**
     * 两项盘玩法集
     */
    @Getter
    @AllArgsConstructor
    enum TwoItemPlaySet {
        TEAM(2001L, 1, "球队","Team") {
            @Override
            public List<Long> getPlayIds() {
                return Secondary.TEAM.getPlayIds();
            }
        },
        PLAYER(2002L, 2, "球员","Player") {
            @Override
            public List<Long> getPlayIds() {
                return Secondary.PLAYER.getPlayIds();
            }
        },
        FULL_TIME(2003L, 3, "全场","Full Time") {
            @Override
            public List<Long> getPlayIds() {
                List<Long> playIds = new ArrayList<>(20);
//                playIds.addAll(Main.FULL_TIME.getPlayIds());
                playIds.addAll(Secondary.FULL_TIME.getPlayIds());
                return playIds;
            }
        },
        //        FIRST_HALF(2004L, 4, "上半场") {
//            @Override
//            public List<Long> getPlayIds() {
//                return Main.FIRST_HALF.getPlayIds();
//            }
//        },
//        SECOND_HALF(2005L, 5, "下半场") {
//            @Override
//            public List<Long> getPlayIds() {
//                return Main.SECOND_HALF.getPlayIds();
//            }
//        },
//        FIRST_SECTION(2006L, 6, "第一节") {
//            @Override
//            public List<Long> getPlayIds() {
//                return Main.FIRST_SECTION.getPlayIds();
//            }
//        },
//        SECOND_SECTION(2007L, 8, "第二节") {
//            @Override
//            public List<Long> getPlayIds() {
//                return Main.SECOND_SECTION.getPlayIds();
//            }
//        },
//        THIRD_SECTION(2008L, 7, "第三节") {
//            @Override
//            public List<Long> getPlayIds() {
//                return Main.THIRD_SECTION.getPlayIds();
//            }
//        },
//        FOURTH_SECTION(2009L, 9, "第四节") {
//            @Override
//            public List<Long> getPlayIds() {
//                return Main.FOURTH_SECTION.getPlayIds();
//            }
//        },
        REGULAR(2010L, 10, "常规赛段","Regular Period") {
            @Override
            public List<Long> getPlayIds() {
                return Secondary.REGULAR.getPlayIds();
            }
        },
//        HALF_TIME(2011L, 11, "半场") {
//            @Override
//            public List<Long> getPlayIds() {
//                List<Long> playIds = new ArrayList<>(20);
//                playIds.addAll(TwoItemPlaySet.FIRST_HALF.getPlayIds());
//                playIds.addAll(TwoItemPlaySet.SECOND_HALF.getPlayIds());
//                return playIds;
//            }
//
//            @Override
//            public List<Long> getSubPlaySetIds() {
//                return Lists.newArrayList(TwoItemPlaySet.FIRST_HALF.getId(), TwoItemPlaySet.SECOND_HALF.getId());
//            }
//        },
//        SECTION(2012L, 12, "小节") {
//            @Override
//            public List<Long> getPlayIds() {
//                List<Long> playIds = new ArrayList<>(40);
//                playIds.addAll(TwoItemPlaySet.FIRST_SECTION.getPlayIds());
//                playIds.addAll(TwoItemPlaySet.SECOND_SECTION.getPlayIds());
//                playIds.addAll(TwoItemPlaySet.THIRD_SECTION.getPlayIds());
//                playIds.addAll(TwoItemPlaySet.FOURTH_SECTION.getPlayIds());
//                return playIds;
//            }
//
//            @Override
//            public List<Long> getSubPlaySetIds() {
//                return Lists.newArrayList(TwoItemPlaySet.FIRST_SECTION.getId(), TwoItemPlaySet.SECOND_SECTION.getId(), TwoItemPlaySet.THIRD_SECTION.getId(), TwoItemPlaySet.FOURTH_SECTION.getId());
//            }
//        }
        ;

        private Long id;
        private Integer sortNo;
        private String name;
        private String enName;

        public List<Long> getSubPlaySetIds() {
            return Lists.newArrayList(this.getId());
        }

        public abstract List<Long> getPlayIds();

        public static List<TwoItemPlaySet> queryList() {
            return Lists.newArrayList(REGULAR, /*HALF_TIME,*/ TEAM, /*PLAYER,*/ FULL_TIME/*, SECTION*/);
        }

        public static List<List<TwoItemPlaySet>> group() {
            List<TwoItemPlaySet> group1 = Lists.newArrayList(TEAM);
            List<TwoItemPlaySet> group2 = Lists.newArrayList(PLAYER);
            List<TwoItemPlaySet> group3 = Lists.newArrayList(FULL_TIME);
//            List<TwoItemPlaySet> group4 = Lists.newArrayList(FIRST_HALF, SECOND_HALF);
//            List<TwoItemPlaySet> group5 = Lists.newArrayList(FIRST_SECTION, THIRD_SECTION);
//            List<TwoItemPlaySet> group6 = Lists.newArrayList(SECOND_SECTION, FOURTH_SECTION);
            List<TwoItemPlaySet> group7 = Lists.newArrayList(REGULAR);
            return Lists.newArrayList(group1, group2, group3, /*group4, group5, group6,*/ group7);
        }

        public static TwoItemPlaySet getGroupByPlayId(Long playId) {
            for (List<TwoItemPlaySet> list : group()) {
                for (TwoItemPlaySet twoItemPlaySet : list) {
                    if (twoItemPlaySet.getPlayIds().contains(playId)) {
                    	Long langId = twoItemPlaySet.getId();
                    	String name = "";
                    	if (langId.equals(2001L)) {
                    		name = I18nUtils.getMessage(I18iConstants.BASKETBALL_TEAM);
                    	} else if (langId.equals(2002L)) {
                    		name = I18nUtils.getMessage(I18iConstants.BASKETBALL_PLAYER);
                    	} else if (langId.equals(2003L)) {
                    		name = I18nUtils.getMessage(I18iConstants.BASKETBALL_FULL_TIME);
                    	} else if (langId.equals(2010L)) {
                    		name = I18nUtils.getMessage(I18iConstants.BASKETBALL_REGULAR_PERIOD);
                    	}
                    	twoItemPlaySet.setName(name);
                        return twoItemPlaySet;
                    }
                }
            }
            return null;
        }
        
        public void setName(String name) {
             this.name = name;
    	}
    }

    /**
     * <a href="http://lan-confluence.sportxxxr1pub.com/pages/viewpage.action?pageId=32369340">联动模式</a>
     */
    @Getter
    @AllArgsConstructor
    enum Linkage {
        FULL_TIME(39L, 38L, 198L, 199L, "", "全场"),
        FULL_TIME_EXCLUDE_OVERTIME(4L, 2L, 10L, 11L, "", "全场不含加时"),
        FIRST_HALF(19L, 18L, 87L, 97L, "", "上半场"),
        SECOND_HALF(143L, 26L, 88L, 98L, "", "下半场"),
        FIRST_SECTION(46L, 45L, 145L, 146L, "1", "第一节"),
        SECOND_SECTION(52L, 51L, 145L, 146L, "2", "第二节"),
        THIRD_SECTION(58L, 57L, 145L, 146L, "3", "第三节"),
        FOURTH_SECTION(64L, 63L, 145L, 146L, "4", "第四节");

        /**
         * 让分玩法ID
         */
        private Long handicap;
        /**
         * 总分玩法ID
         */
        private Long total;
        /**
         * 主队总分玩法ID
         */
        private Long totalT1;
        /**
         * 客队总分玩法ID
         */
        private Long totalT2;
        private String addition2;
        private String name;

        public List<Long> getTargetPlayIds() {
            return Lists.newArrayList(this.getTotalT1(), this.getTotalT2());
        }

        public List<Long> getTargetSubPlayIds() {
            if (StringUtils.isBlank(this.getAddition2())) {
                return Lists.newArrayList(this.getTotalT1(), this.getTotalT2());
            }
            long subT1 = this.getTotalT1() * 100 + NumberUtils.toLong(this.getAddition2());
            long subT2 = this.getTotalT2() * 100 + NumberUtils.toLong(this.getAddition2());
            return Lists.newArrayList(subT1, subT2);
        }

        public static Linkage getByTargetSubPlayId(Long subPlayId) {
            for (Linkage linkage : Linkage.values()) {
                if (linkage.getTargetSubPlayIds().contains(subPlayId)) {
                    return linkage;
                }
            }
            return null;
        }

        public static Linkage getByTargetPlayId(Long playId) {
            for (Linkage linkage : Linkage.values()) {
                if (linkage.getTotalT1().equals(playId) || linkage.getTotalT2().equals(playId)) {
                    return linkage;
                }
            }
            return null;
        }

        public static Linkage getByTargetNormalPlayId(Long playId) {
            for (Linkage linkage : getNormalPlay()) {
                if (linkage.getTotalT1().equals(playId) || linkage.getTotalT2().equals(playId)) {
                    return linkage;
                }
            }
            return null;
        }

        public static Linkage getBySourcePlayId(Long playId) {
            for (Linkage linkage : Linkage.values()) {
                if (linkage.getHandicap().equals(playId) || linkage.getTotal().equals(playId)) {
                    return linkage;
                }
            }
            return null;
        }

        public static boolean isTargetHomePlay(Long playId) {
            for (Linkage linkage : Linkage.values()) {
                if (linkage.getTotalT1().equals(playId)) {
                    return true;
                }
            }
            return false;
        }

        public static boolean isSourcePlay(Long playId) {
            for (Linkage linkage : Linkage.values()) {
                if (linkage.getHandicap().equals(playId) || linkage.getTotal().equals(playId)) {
                    return true;
                }
            }
            return false;
        }

        public static List<Linkage> getNormalPlay() {
            return Lists.newArrayList(FULL_TIME, FULL_TIME_EXCLUDE_OVERTIME, FIRST_HALF, SECOND_HALF);
        }

        public static List<Linkage> getSectionPlay() {
            return Lists.newArrayList(FIRST_SECTION, SECOND_SECTION, THIRD_SECTION, FOURTH_SECTION);
        }

        public static List<Linkage> getSectionList() {
            return Lists.newArrayList(FIRST_SECTION, SECOND_SECTION, THIRD_SECTION, FOURTH_SECTION);
        }

        public static Set<Long> getLinkagePlayId() {
            Set<Long> set = Sets.newHashSet();
            for (Linkage linkage : Linkage.values()) {
                set.add(linkage.getTotalT1());
                set.add(linkage.getTotalT2());
            }
            return set;
        }
    }

    static boolean isHandicap(Long playId) {
        if (Main.getHandicapPlayIds().contains(playId)) {
            return true;
        }
        return false;
    }

    static boolean isTotal(Long playId) {
        if (Main.getTotalPlayIds().contains(playId)) {
            return true;
        }
        if (Secondary.TEAM.getPlayIds().contains(playId)) {
            return true;
        }
        if (Secondary.PLAYER.getPlayIds().contains(playId)) {
            return true;
        }
        return false;
    }

    static boolean isOddEven(Long playId) {
        if (Main.getOddEvenPlayIds().contains(playId)) {
            return true;
        }
        if (playId == 15L) {
            // 15.全场单双(不含加时)
            return true;
        }
        return false;
    }

    static boolean isYesNo(Long playId) {
        if (Secondary.YES_NO.getPlayIds().contains(playId)) {
            return true;
        }
        return false;
    }

    static boolean isHandicapOrTotal(Long playId) {
        return isHandicap(playId) || isTotal(playId);
    }

    /**
     * 是否支持M模式构建盘口
     *
     * @param playId
     * @return
     */
    static boolean isManualBuildMarket(Long playId) {
        if (Main.getHandicapPlayIds().contains(playId)) {
            return true;
        }
        if (Main.getTotalPlayIds().contains(playId)) {
            return true;
        }
        List<Long> teamPlayIds = Secondary.TEAM.getPlayIds();
        teamPlayIds.removeAll(Lists.newArrayList(145L, 146L));
        if (teamPlayIds.contains(playId)) {
            return true;
        }
        if (Main.getOddEvenPlayIds().contains(playId)) {
            return true;
        }
        if (playId == 15L) {
            // 15.全场单双(不含加时)
            return true;
        }
        return false;
    }

    /**
     * 是否支持A+模式
     *
     * @param playId
     * @return
     */
    static boolean isAutoPlus(Long playId) {
        // 主要玩法->让分玩法
        if (Main.getHandicapPlayIds().contains(playId)) {
            return true;
        }
        // 主要玩法->总分大小玩法
        if (Main.getTotalPlayIds().contains(playId)) {
            return true;
        }
        // 主要玩法->单双玩法
        if (Main.getOddEvenPlayIds().contains(playId)) {
            return true;
        }
        if (Lists.newArrayList(198L, 199L, 87L, 97L, 88L, 98L).contains(playId)) {
            return true;
        }
        if (Lists.newArrayList(2L, 10L, 11L, 15L).contains(playId)) {
            // 2.总分(不含加时)
            // 10.球队1总分(不含加时)
            // 11.球队2总分(不含加时)
            // 15.单双(不含加时)
            return true;
        }
        return false;
    }

    /**
     * 是否支持L+模式
     *
     * @param playId
     * @return
     */
    static boolean isLinkage(Long playId) {
        return Linkage.getLinkagePlayId().contains(playId);
    }

    static boolean isNewMarket(Long playId) {
        return Main.getWinAlonePlayIds().contains(playId) ||
                Main.getHandicapPlayIds().contains(playId) ||
                Main.getTotalPlayIds().contains(playId) ||
                Main.getOddEvenPlayIds().contains(playId) || playId == 5L;
    }
}
