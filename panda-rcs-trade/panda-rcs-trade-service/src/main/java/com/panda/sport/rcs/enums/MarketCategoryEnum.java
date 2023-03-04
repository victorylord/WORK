package com.panda.sport.rcs.enums;

import com.panda.sport.rcs.constants.RcsConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.enums
 * @Description : 玩法枚举类 exclude [44,67], [145,147],[154,215],[219,221],[242,999]
 * @Author : Paca
 * @Date : 2020-07-29 15:38
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum MarketCategoryEnum {

    /**
     * 赛果
     */
    ID_1(1L, "1x2"),
    ID_17(17L, "1st half - 1x2"),
    ID_25(25L, "2nd half - 1x2"),
    ID_111(111L, "Corner 1x2"),
    ID_119(119L, "1st half - corner 1x2"),
    ID_126(126L, "Overtime - 1x2"),
    ID_129(129L, "Overtime 1st half - 1x2"),

    /**
     * 大小盘
     */
    ID_2(2L, "Total"),
    ID_18(18L, "1st half - total"),
    ID_26(26L, "2nd half - total"),
    ID_114(114L, "Total corners"),
    ID_122(122L, "1st half - total corners"),
    ID_127(127L, "Overtime - total"),
    ID_134(134L, "Penalty shootout - total"),
    ID_10(10L, "{$competitor1} total"),
    ID_11(11L, "{$competitor2} total"),
    ID_87(87L, "1st half - {$competitor1} total"),
    ID_88(88L, "2nd half - {$competitor1} total"),
    ID_97(97L, "1st half - {$competitor2} total"),
    ID_98(98L, "2nd half - {$competitor2} total"),
    ID_115(115L, "{$competitor1} total corners"),
    ID_116(116L, "{$competitor2} total corners"),
    ID_123(123L, "1st half - {$competitor1} total corners"),
    ID_124(124L, "1st half - {$competitor2} total corners"),

    /**
     * 让球赛果
     */
    ID_3(3L, "Handicap {hcp}"),
    ID_69(69L, "1st half - handicap {hcp}"),
    ID_71(71L, "2nd half - handicap {hcp}"),

    /**
     * 让球
     */
    ID_4(4L, "Handicap"),
    ID_19(19L, "1st half - handicap"),
    ID_143(143L, "2nd half - handicap"),
    ID_113(113L, "Corner handicap"),
    ID_121(121L, "1st half - corner handicap"),
    ID_128(128L, "Overtime - handicap"),
    ID_130(130L, "Overtime 1st half - handicap"),

    /**
     * 平局退款
     */
    ID_5(5L, "Draw no bet"),
    ID_43(43L, "Draw No Bet first half"),
    ID_142(142L, "2nd half - draw no bet"),
    /**
     * {主/客队}获胜退款
     */
    ID_77(77L, "{$competitor1} no bet"),
    ID_91(91L, "{$competitor2} no bet"),

    /**
     * 双重机会
     */
    ID_6(6L, "Double chance"),
    ID_70(70L, "1st half - double chance"),
    ID_72(72L, "2nd half - double chance"),

    /**
     * 比分
     */
    ID_7(7L, "Correct score"),
    ID_20(20L, "1st half - correct score"),
    ID_341(341L, "1st half - correct score"),
    ID_74(74L, "2nd half - correct score"),
    ID_342(342L, "2nd half - correct score"),
    ID_103(103L, "Halftime/fulltime correct score"),
    ID_236(236L, "Overtime - correct score [{score}]"),
    ID_343(343L, "Overtime - correct score [{score}]"),
    ID_241(241L, "Penalty shootout - correct score"),
    ID_344(344L, "Correct score"),
    ID_260(260L, "Correct score"),
    ID_204(204L, "Correct score"),
    ID_267(267L, "xth period - correct score"),

    /**
     * 两队都进球
     */
    ID_12(12L, "Both teams to score"),
    ID_24(24L, "1st half - both teams to score"),
    ID_76(76L, "2nd half - both teams to score"),
    ID_108(108L, "1st/2nd half both teams to score"),

    /**
     * 赛果 & 进球大小
     */
    ID_13(13L, "1x2 & total"),
    ID_345(345L, "1x2 & total"),
    ID_346(346L, "1x2 & total"),
    ID_347(347L, "1x2 & total"),
    ID_348(348L, "1x2 & total"),
    ID_349(349L, "1x2 & total"),
    ID_350(350L, "1x2 & total"),
    ID_351(351L, "1x2 & total"),
    ID_353(353L, "1x2 & total"),
    ID_360(360L, "1x2 & total"),

    /**
     * 准确进球数
     */
    ID_14(14L, "Exact goals"),
    ID_23(23L, "1st half - exact goals"),
    ID_73(73L, "2nd half - exact goals"),
    ID_8(8L, "{$competitor1} exact goals"),
    ID_9(9L, "{$competitor2} exact goals"),
    ID_21(21L, "1st half - {$competitor1} exact goals"),
    ID_22(22L, "1st half - {$competitor2} exact goals"),
    ID_239(239L, "Penalty shootout - exact goals"),

    /**
     * 单/双
     */
    ID_15(15L, "Odd/even"),
    ID_42(42L, "Odd/Even for first half"),
    ID_75(75L, "2nd half - odd/even"),
    ID_118(118L, "Odd/even corners"),
    ID_229(229L, "1st half - odd/even corners"),
    ID_78(78L, "{$competitor1} odd/even"),
    ID_92(92L, "{$competitor2} odd/even"),
    ID_240(240L, "Penalty shootout - odd/even"),

    /**
     * 最多进球的半场
     */
    ID_16(16L, "Highest scoring half"),

    /**
     * 剩余时间获胜
     */
    ID_27(27L, "Which team wins the rest of the match"),
    ID_29(29L, "1st half - which team wins the rest"),

    /**
     * 全场胜负
     */
    ID_37(37L, "Which team will win the match, including overtime"),
    /**
     * 总分
     */
    ID_38(38L, "Total for whole match, including overtime"),
    /**
     * 让分
     */
    ID_39(39L, "Asian handicap for whole match, including overtime"),
    /**
     * 总分单/双
     */
    ID_40(40L, "Odd/Even for whole match including overtime"),
    /**
     * 是否进行加时赛
     */
    ID_41(41L, "Will there be overtime"),

    /**
     * 第{!goalnr}个进球
     */
    ID_28(28L, "{!goalnr} goal"),
    ID_30(30L, "1st half - {!goalnr} goal"),
    /**
     * 第{!goalnr}个进球何时发生？
     */
    ID_31(31L, "When will the {!goalnr} goal be scored (15 min interval)"),
    /**
     * 上半场第{!cornernr}个角球
     */
    ID_120(120L, "1st half - {!cornernr} corner"),
    /**
     * 谁先获得{cornernr}个角球
     */
    ID_125(125L, "Race to {cornernr} corners"),
    /**
     * 点球大战-第{!penaltynr}个点球是否射进
     */
    ID_133(133L, "Penalty shootout - {!penaltynr} penalty scored"),
    /**
     * 第{goalnr}个进球队员
     */
    ID_148(148L, "{!goalnr} goalscorer"),
    /**
     * 第{!scorenr} 个进球方式
     */
    ID_222(222L, "{!scorenr} scoring type"),
    /**
     * 第{!bookingnr} 张罚牌
     */
    ID_224(224L, ""),
    /**
     * 第{!cornernr} 个角球
     */
    ID_225(225L, "{!cornernr} corner"),
    /**
     * 上半场谁先获得{cornernr} 个角球
     */
    ID_230(230L, ""),
    /**
     * 加时赛-第{!goalnr} 个进球
     */
    ID_235(235L, "Overtime - {!goalnr} goal"),
    /**
     * 点球大战-第{!goalnr}个进球
     */
    ID_237(237L, "Penalty shootout - {!goalnr} goal"),

    /**
     * 15分钟进球-赛果({from}-{to})
     */
    ID_32(32L, "15 minutes - 1x2 from {from} to {to}"),
    /**
     * 15分钟进球-让球({from}-{to})
     */
    ID_33(33L, "1-15 Min. Goals Handicap"),
    /**
     * 15分钟进球-大小({from}-{to})
     */
    ID_34(34L, "15 minutes - total from {from} to {to}"),

    /**
     * 任何时间进球队员
     */
    ID_36(36L, "Anytime goalscorer"),
    /**
     * 最后进球队员
     */
    ID_150(150L, "Last goalscorer"),
    /**
     * 进2+球的队员
     */
    ID_151(151L, "Player to score 2+"),
    /**
     * 进3+球的队员
     */
    ID_152(152L, "Player to score 3+"),

    /**
     * 总进球数区间
     */
    ID_68(68L, "Goal range"),
    /**
     * 角球总数区间
     */
    ID_117(117L, "Corner range"),
    ID_226(226L, "{$competitor1} corner range"),
    ID_227(227L, "{$competitor2} corner range"),
    ID_228(228L, "1st half - corner range"),
    /**
     * 总分区间
     */
    ID_218(218L, "Point range"),

    /**
     * {主/客队}零失球
     */
    ID_79(79L, "{$competitor2} clean sheet"),
    ID_81(81L, "{$competitor1} clean sheet"),
    ID_89(89L, "2nd half - {$competitor1} clean sheet"),
    ID_90(90L, "1st half - {$competitor1} clean sheet"),
    ID_99(99L, "2nd half - {$competitor2} clean sheet"),
    ID_100(100L, "1st half - {$competitor2} clean sheet"),

    /**
     * {主/客队}零失球获胜
     */
    ID_80(80L, "{$competitor2} win to nil"),
    ID_82(82L, "{$competitor1} win to nil"),

    /**
     * {主/客队}上/下半场全胜
     */
    ID_83(83L, "{$competitor1} to win both halves"),
    ID_93(93L, "{$competitor2} to win both halves"),

    /**
     * {主/客队}任意半场获胜
     */
    ID_84(84L, "{$competitor1} to win either half"),
    ID_94(94L, "{$competitor2} to win either half"),

    /**
     * {主/客队}最高得分半场
     */
    ID_85(85L, "{$competitor1} highest scoring half"),
    ID_95(95L, "{$competitor2} highest scoring half"),

    /**
     * {主/客队}上/下半场均进球
     */
    ID_86(86L, "{$competitor1} to score in both halves"),
    ID_96(96L, "{$competitor2} to score in both halves"),

    /**
     * 赛果 & 两队都进球
     */
    ID_101(101L, "1x2 & both teams to score"),
    ID_105(105L, "1st half - 1x2 & both teams to score"),
    ID_106(106L, "2nd half - 1x2 & both teams to score"),

    /**
     * 进球大小 & 两队都进球
     */
    ID_102(102L, "Total & both teams to score"),

    /**
     * 半/全场
     */
    ID_104(104L, "Halftime/fulltime"),

    /**
     * 双重机会 & 两队都进球
     */
    ID_107(107L, "Double chance & both teams to score"),

    /**
     * 上/下半场进球数均大于{total}
     */
    ID_109(109L, "Both halves over {total}"),
    /**
     * 上/下半场进球数均小于{total}
     */
    ID_110(110L, "Both halves under {total}"),

    /**
     * 最后一个角球
     */
    ID_112(112L, "Last corner"),

    /**
     * 是否进行点球大战
     */
    ID_131(131L, "Will there be a penalty shootout"),
    /**
     * 点球大战-获胜者
     */
    ID_132(132L, "Penalty shootout - winner"),

    /**
     * 晋级球队
     */
    ID_135(135L, "To qualify"),
    /**
     * 冠军
     */
    ID_136(136L, "Which team will win the final"),
    /**
     * 获胜方式
     */
    ID_137(137L, "Winning method"),

    /**
     * 比赛中出现红牌
     */
    ID_138(138L, "Sending off"),
    ID_139(139L, "{$competitor1} sending off"),
    ID_140(140L, "{$competitor2} sending off"),

    /**
     * 净胜分
     */
    ID_141(141L, "Winning margin"),
    ID_340(340L, "Winning margin4+"),
    ID_359(359L, "Winning margin"),
    ID_238(238L, "Penalty shootout - winning margin"),
    /**
     * 篮球
     */
    ID_200(200L, "Winning margin 3"),
    ID_209(209L, "Winning margin 6"),
    ID_210(210L, "Winning margin 7"),
    ID_211(211L, "Winning margin 12"),
    ID_212(212L, "Winning margin 14"),

    /**
     * 谁先开球
     */
    ID_144(144L, "Which team kicks off"),

    /**
     * 最后进球队伍
     */
    ID_149(149L, "Last goal"),

    /**
     * 比赛获胜
     */
    ID_153(153L, "Winner"),

    /**
     * 首先获得{pointnr}分
     */
    ID_201(201L, ""),
    /**
     * 谁先获得第{!pointnr}分(含加时)
     */
    ID_214(214L, ""),
    /**
     * 第{!quarternr}节首先获得{pointnr}分
     */
    ID_215(215L, ""),

    /**
     * 独赢&总分
     */
    ID_216(216L, "Winner & total (incl. overtime)"),
    /**
     * 准确总分大小
     */
    ID_217(217L, ""),

    /**
     * 哪队得分
     */
    ID_223(223L, "Which team to score"),

    /**
     * 15分钟角球-赛果({from}-{to})
     */
    ID_231(231L, ""),
    /**
     * 15分钟角球-让球({from}-{to})
     */
    ID_232(232L, ""),
    /**
     * 15分钟角球-大小({from}-{to})
     */
    ID_233(233L, ""),

    /**
     * 加时赛是否进球
     */
    ID_234(234L, "Overtime & goal");

    /**
     * 玩法ID
     */
    private Long id;
    private String text;

    /**
     * 投注项类型 设置为 投注项名称
     *
     * @param marketCategoryId 玩法ID
     * @return
     */
    public static boolean isSetOddsNameByOddsType(Long marketCategoryId) {
        return isCorrectScore(marketCategoryId) ||
                isExactGoals(marketCategoryId) ||
                isGoalscorer(marketCategoryId) ||
                isRange(marketCategoryId) ||
                ID_210.getId().equals(marketCategoryId);
    }

    public static boolean isCorrectScore(Long marketCategoryId) {
        return ID_7.getId().equals(marketCategoryId) ||
                ID_20.getId().equals(marketCategoryId) ||
                ID_341.getId().equals(marketCategoryId) ||
                ID_74.getId().equals(marketCategoryId) ||
                ID_342.getId().equals(marketCategoryId) ||
                ID_103.getId().equals(marketCategoryId) ||
                ID_236.getId().equals(marketCategoryId) ||
                ID_343.getId().equals(marketCategoryId) ||
                ID_344.getId().equals(marketCategoryId) ||
                ID_241.getId().equals(marketCategoryId)||
                ID_267.getId().equals(marketCategoryId)||
                ID_260.getId().equals(marketCategoryId)||
                ID_204.getId().equals(marketCategoryId);
    }
    public static boolean isCorrectScoreBy344(Long marketCategoryId) {
        return ID_344.getId().equals(marketCategoryId); 
    }

    public static boolean isExactGoals(Long marketCategoryId) {
        return ID_14.getId().equals(marketCategoryId) ||
                ID_23.getId().equals(marketCategoryId) ||
                ID_73.getId().equals(marketCategoryId) ||
                ID_8.getId().equals(marketCategoryId) ||
                ID_9.getId().equals(marketCategoryId) ||
                ID_21.getId().equals(marketCategoryId) ||
                ID_22.getId().equals(marketCategoryId) ||
                ID_239.getId().equals(marketCategoryId);
    }

    public static boolean isGoalscorer(Long marketCategoryId) {
        return RcsConstant.GOALSCORER.contains(marketCategoryId);
    }

    public static boolean isRange(Long marketCategoryId) {
        return ID_68.getId().equals(marketCategoryId) ||
                ID_117.getId().equals(marketCategoryId) ||
                ID_226.getId().equals(marketCategoryId) ||
                ID_227.getId().equals(marketCategoryId) ||
                ID_228.getId().equals(marketCategoryId) ||
                ID_218.getId().equals(marketCategoryId);
    }

    public static boolean isWinningMargin(Long marketCategoryId) {
        return ID_141.getId().equals(marketCategoryId) ||
                ID_238.getId().equals(marketCategoryId) ||
                ID_200.getId().equals(marketCategoryId) ||
                ID_209.getId().equals(marketCategoryId) ||
//                ID_210.getId().equals(marketCategoryId) ||
                ID_211.getId().equals(marketCategoryId) ||
                ID_340.getId().equals(marketCategoryId) ||
                ID_359.getId().equals(marketCategoryId) ||
                ID_212.getId().equals(marketCategoryId);
    }

}
