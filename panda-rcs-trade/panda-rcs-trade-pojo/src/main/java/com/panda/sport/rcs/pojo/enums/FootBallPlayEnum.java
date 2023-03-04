package com.panda.sport.rcs.pojo.enums;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.List;

/** 
 * @author :  sean
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.pojo.enums
 * @Description :  足球常用玩法配置
 * @Date: 2021-02-07 12:24
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public enum FootBallPlayEnum {
    // 常规进球玩法
    PLAY_1(1,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_2(2,"match_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_4(4,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    // 全场进球次要玩法
    PLAY_6(6,"match_score",0,Lists.newArrayList(0,6,7,31),"1X"),
    PLAY_12(12,"match_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_15(15,"match_score",0,Lists.newArrayList(0,6,7,31),"Odd"),
    PLAY_78(78,"match_score",0,Lists.newArrayList(0,6,7,31),"Odd"),
    PLAY_92(92,"match_score",0,Lists.newArrayList(0,6,7,31),"Odd"),
    PLAY_3(3,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_5(5,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_81(81,"match_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_82(82,"match_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_79(79,"match_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_80(80,"match_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_77(77,"match_score",0,Lists.newArrayList(0,6,7,31),"X"),
    PLAY_91(91,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_10(10,"match_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_11(11,"match_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_27(27,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_28(28,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_117(117,"match_score",0,Lists.newArrayList(0,6,7,31),"0-8"),
    PLAY_34(34,"match_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_32(32,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_33(33,"match_score",0,Lists.newArrayList(0,6,7,31),"1"),

    // 半场主要玩法
    PLAY_17(17,"set_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_18(18,"set_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_19(19,"set_score",1,Lists.newArrayList(0,6),"1"),
    // 上半场次要玩法
    PLAY_70(70,"set_score",1,Lists.newArrayList(0,6),"1X"),
    PLAY_42(42,"set_score",1,Lists.newArrayList(0,6),"Odd"),
    PLAY_43(43,"set_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_24(24,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_69(69,"set_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_29(29,"set_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_30(30,"set_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_87(87,"set_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_97(97,"set_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_90(90,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_100(100,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_83(83,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_86(86,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_93(93,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_96(96,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_84(84,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_85(85,"set_score",1,Lists.newArrayList(0,6),"FirstHalf"),
    PLAY_94(94,"set_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_95(95,"set_score",1,Lists.newArrayList(0,6),"FirstHalf"),
    PLAY_16(16,"set_score",1,Lists.newArrayList(0,6),"FirstHalf"),
    PLAY_109(109,"match_score",1,Lists.newArrayList(0,6),"Yes"),
    PLAY_110(110,"match_score",1,Lists.newArrayList(0,6),"Yes"),
    // 常规角球玩法
    PLAY_114(114,"corner_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_122(122,"corner_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_113(113,"corner_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_111(111,"corner_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_121(121,"corner_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_119(119,"corner_score",1,Lists.newArrayList(0,6),"1"),
    // 角球次要玩法
    PLAY_118(118,"corner_score",0,Lists.newArrayList(0,6,7,31),"Odd"),
    PLAY_229(229,"corner_score",1,Lists.newArrayList(0,6),"Odd"),
    PLAY_112(112,"corner_score",0,Lists.newArrayList(0,6,7,31),"None"),
    PLAY_115(115,"corner_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_116(116,"corner_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_123(123,"corner_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_124(124,"corner_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_228(228,"corner_score",1,Lists.newArrayList(0,6),"5-6"),
    PLAY_233(233,"corner_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_225(225,"corner_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_120(120,"corner_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_125(125,"corner_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_230(230,"corner_score",0,Lists.newArrayList(0,6),"1"),
    PLAY_231(231,"corner_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_232(232,"corner_score",0,Lists.newArrayList(0,6,7,31),"1"),
    // 加时进球玩法
    PLAY_126(126,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,41,42),"1"),
    PLAY_127(127,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,41,42),"Over"),
    PLAY_128(128,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,41,42),"1"),
    PLAY_129(129,"extra_time_score",1,Lists.newArrayList(0,6,7,31,32,41),"1"),
    PLAY_332(332,"extra_time_score",1,Lists.newArrayList(0,6,7,31,32,41),"Over"),
    PLAY_130(130,"extra_time_score",1,Lists.newArrayList(0,6,7,31,32,41),"1"),
    PLAY_330(330,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"Odd"),
    // 加时次要玩法
    PLAY_131(131,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,41,42,100),"Yes"),
    PLAY_234(234,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,41,42,100),"Yes"),
    PLAY_235(235,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,41,42,100),"1"),
    //点球玩法,
    PLAY_333(333,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"1"),
    PLAY_335(335,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"Over"),
    PLAY_334(334,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"1"),
    PLAY_134(134,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"Over"),
    PLAY_132(132,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"1"),
    PLAY_240(240,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"Odd"),
    PLAY_133(133,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"Yes"),
    PLAY_237(237,"penalty_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"1"),
    // 下半场次要玩法
    PLAY_25(25,"set_score",2,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_143(143,"set_score",2,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_26(26,"set_score",2,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_72(72,"set_score",2,Lists.newArrayList(0,6,7,31),"1X"),
    PLAY_75(75,"set_score",2,Lists.newArrayList(0,6,7,31),"Odd"),
    PLAY_76(76,"set_score",2,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_71(71,"set_score",2,Lists.newArrayList(0,6,31),"1"),
    PLAY_88(88,"set_score",2,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_98(98,"set_score",2,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_89(89,"set_score",2,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_99(99,"set_score",2,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_142(142,"set_score",2,Lists.newArrayList(0,6,7,31),"1"),

    //晋级
    PLAY_135(135,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"1"),
    PLAY_136(136,"extra_time_score",0,Lists.newArrayList(0,6,7,31,32,33,34,41,42,50,100,110),"1"),

    // 红牌玩法
    PLAY_138(138,"red_card_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_139(139,"red_card_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    PLAY_140(140,"red_card_score",0,Lists.newArrayList(0,6,7,31),"Yes"),
    // 罚牌玩法
    PLAY_307(307,"card",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_309(309,"card",1,Lists.newArrayList(0,6),"Over"),
    PLAY_312(312,"card",0,Lists.newArrayList(0,6,7,31),"Odd"),
    PLAY_313(313,"card",1,Lists.newArrayList(0,6),"Odd"),
    PLAY_310(310,"card",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_311(311,"card",1,Lists.newArrayList(0,6),"1"),
    PLAY_306(306,"card",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_308(308,"card",1,Lists.newArrayList(0,6),"Over"),
    PLAY_314(314,"card",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_315(315,"card",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_316(316,"card",1,Lists.newArrayList(0,6),"Over"),
    PLAY_317(317,"card",1,Lists.newArrayList(0,6),"Over"),
    PLAY_224(224,"card",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_324(324,"yellow_card_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_325(325,"yellow_card_score",0,Lists.newArrayList(0,6,7,31),"Over"),
    PLAY_326(326,"yellow_card_score",0,Lists.newArrayList(0,6,7,31),"1"),
    PLAY_327(327,"yellow_card_score",1,Lists.newArrayList(0,6),"1"),
    PLAY_328(328,"yellow_card_score",1,Lists.newArrayList(0,6),"Over"),
    PLAY_329(329,"yellow_card_score",1,Lists.newArrayList(0,6),"1"),

    // 特殊玩法
    PLAY_144(144,"match_score",0,Lists.newArrayList(0),"1"),
    PLAY_149(149,"match_score",0,Lists.newArrayList(0,6,7,31),"None");

    FootBallPlayEnum(Integer id, String scoreType, Integer stage, List<Integer> period,String oddsType){
        this.id = id;
        this.scoreType = scoreType;
        this.stage = stage;
        this.period = period;
        this.oddsType = oddsType;
    }
    public Integer getId(){
        return this.id;
    }
    public Integer getStage(){
        return this.stage;
    }
    public String getScoreType(){
        return this.scoreType;
    }
    public String getOddsType(){
        return this.oddsType;
    }
    public List<Integer> getPeriod(){
        return this.period;
    }
    private Integer id;
    private Integer stage;
    private String scoreType;
    private String oddsType;
    private List<Integer> period;
    public static String getScoreType(Long playId) {
        for (FootBallPlayEnum ele : values()) {
            if(ele.getId().intValue() == playId.intValue()) {
                return ele.getScoreType();
            }
        }
        return null;
    }
    public static Integer getStage(Long playId) {
        for (FootBallPlayEnum ele : values()) {
            if(ele.getId().intValue() == playId.intValue()) {
                return ele.getStage();
            }
        }
        return null;
    }
    public static List<Integer> getPeriod(Long playId) {
        for (FootBallPlayEnum ele : values()) {
            if(ele.getId().intValue() == playId.intValue()) {
                return ele.getPeriod();
            }
        }
        return Lists.newArrayList();
    }
    public static String getOddsType(Long playId) {
        for (FootBallPlayEnum ele : values()) {
            if(ele.getId().intValue() == playId.intValue()) {
                return ele.getOddsType();
            }
        }
        return "";
    }
}
