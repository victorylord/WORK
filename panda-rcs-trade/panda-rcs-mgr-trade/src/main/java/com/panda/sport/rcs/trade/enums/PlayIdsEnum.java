package com.panda.sport.rcs.trade.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.enums
 * @Description :  TODO
 * @Date: 2020-10-07 16:31
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public enum PlayIdsEnum {
    //篮球
    //    play_2(2,3),//大小
//    play_4(4,3),//让球
    play_18(18,1),//上半场大小
    play_19(19,1),//上半场让球
    play_39(39,3),//全场让球
    play_38(38,3),//全场大小
    play_46(46,6),//第一节让球
    play_45(45,6),//第一节大小
    play_52(52,7),//第二节让球
    play_51(51,7),//第二节大小
    play_58(58,8),//第三节让球
    play_57(57,8),//第三节大小
    play_64(64,9),//第四节让球
    play_63(63,9),//第四节大小
    play_143(143,2),//下半场让球
    play_26(26,2),//下半场大小

    play_198(198,61),//主队总分
    play_199(199,61),//客队总分
    play_87(87,62),//上半场总分
    play_97(97,62),//上半场总分
    play_88(88,63),//下半场总分
    play_98(98,63),//下半场总分
    play_14501(14501,64),//第X节主队总分
    play_14601(14601,64),//第X节客队总分
    play_14502(14502,65),//第X节主队总分
    play_14602(14602,65),//第X节客队总分
    play_14503(14503,66),//第X节主队总分
    play_14603(14603,66),//第X节客队总分
    play_14504(14504,67),//第X节主队总分
    play_14604(14604,67),//第X节客队总分

    //网球
    play_154(154,11),//全场让盘

    play_155(155,12),//全场让局
    play_202(202,12),//全场局数

    play_16301(16301,13),//第1盘让局
    play_16401(16401,13),//第1盘局数
    play_16302(16302,14),//第2盘让局
    play_16402(16402,14),//第2盘局数
    play_16303(16303,15),//第3盘让局
    play_16403(16403,15),//第3盘局数
    play_16304(16304,16),//第4盘让局
    play_16404(16404,16),//第4盘局数
    play_16305(16305,17),//第5盘让局
    play_16406(16405,17),//第5盘局数

    //乒乓球 and  羽毛球
    play_172(172,22),//全场让分
    play_173(173,22),//全场总分

    play_17601(17601,23),//第1盘让分
    play_17701(17701,23),//第1盘总分
    play_17602(17602,24),//第2盘让分
    play_17702(17702,24),//第2盘总分
    play_17603(17603,25),//第3盘让分
    play_17703(17703,25),//第3盘总分
    play_17604(17604,26),//第4盘让分
    play_17704(17704,26),//第4盘总分
    play_17605(17605,27),//第5盘让分
    play_17705(17705,27),//第5盘总分
    play_17606(17606,28),//第6盘让分
    play_17706(17706,28),//第6盘总分
    play_17607(17607,29),//第7盘让分
    play_17707(17707,29),//第7盘总分

    //排球

    //以下俩玩法172,173共用乒乓球的playTimeStage
    //play_172(172,22),//全场让分
    //play_173(173,22),//全场总分
    play_25301(25301,31),//第1盘让分
    play_25401(25401,31),//第1盘总分
    play_25302(25302,32),//第2盘让分
    play_25402(25402,32),//第2盘总分
    play_25303(25303,33),//第3盘让分
    play_25403(25403,33),//第3盘总分
    play_25304(25304,34),//第4盘让分
    play_25404(25404,34),//第4盘总分
    play_25305(25305,35),//第5盘让分
    play_25405(25405,35),//第5盘总分
    play_25306(25306,36),//第6盘让分
    play_25406(25406,36),//第6盘总分
    play_25307(25307,37),//第7盘让分
    play_25407(25407,37),//第7盘总分


    //斯诺克 185,186,181,182
    play_181(181, 41),//全场让局
    play_182(182,41),//总局数
    play_18501(18501,42),//第1局让分
    play_18601(18601,42),//第1局总分
    play_18502(18502,43),//第2局让分
    play_18602(18602,43),//第2局总分
    play_18503(18503,44),//第3局让分
    play_18603(18603,44),//第3局总分
    play_18504(18504,45),//第4局让分
    play_18604(18604,45),//第4局总分
    play_18505(18505,46),//第5局让分
    play_18605(18605,46),//第5局总分
    play_18506(18506,47),//第6局让分
    play_18606(18606,47),//第6局总分
    play_18507(18507,48),//第7局让分
    play_18607(18607,48),//第7局总分
    play_18508(18508,49),//第8局让分
    play_18608(18608,49),//第8局总分
    play_18509(18509,50),//第9局让分
    play_18609(18609,50),//第9局总分
    play_18510(18510,51),//第10局让分
    play_18610(18610,51),//第10局总分
    play_18511(18511,52),//第11局让分
    play_18611(18611,52),//第11局总分
    play_18512(18512,53),//第12局让分
    play_18612(18612,53),//第12局总分
    play_18513(18513,54),//第13局让分
    play_18613(18613,54),//第13局总分
    play_18514(18514,55),//第14局让分
    play_18614(18614,55),//第14局总分
    play_18515(18515,56),//第15局让分
    play_18615(18615,56),//第15局总分
    play_18516(18516,57),//第16局让分
    play_18616(18616,57),//第16局总分
    play_18517(18517,58),//第17局让分
    play_18617(18617,58),//第17局总分
    play_18518(18518,59),//第18局让分
    play_18618(18618,59),//第18局总分
    play_18519(18519,60),//第19局让分
    play_18619(18619,60),//第19局总分
    play_18520(18520,61),//第20局让分
    play_18620(18620,61),//第20局总分
    play_18521(18521,62),//第21局让分
    play_18621(18621,62),//第21局总分
    play_18522(18522,63),//第22局让分
    play_18622(18622,63),//第22局总分
    play_18523(18523,64),//第23局让分
    play_18623(18623,64),//第23局总分
    play_18524(18524,65),//第24局让分
    play_18624(18624,65),//第24局总分
    play_18525(18525,66),//第25局让分
    play_18625(18625,66),//第25局总分
    play_18526(18526,67),//第26局让分
    play_18626(18626,67),//第26局总分
    play_18527(18527,68),//第27局让分
    play_18627(18627,68),//第27局总分
    play_18528(18528,69),//第28局让分
    play_18628(18628,69),//第28局总分
    play_18529(18529,70),//第29局让分
    play_18629(18629,70),//第29局总分
    play_18530(18530,71),//第30局让分
    play_18630(18630,71),//第30局总分
    play_18531(18531,72),//第31局让分
    play_18631(18631,72),//第31局总分
    play_18532(18532,73),//第32局让分
    play_18632(18632,73),//第32局总分
    play_18533(18533,74),//第33局让分
    play_18633(18633,74),//第33局总分
    play_18534(18534,75),//第34局让分
    play_18634(18634,75),//第34局总分
    play_18535(18535,76),//第35局让分
    play_18635(18635,76),//第35局总分


    //棒球
    play_243(243,81),//全场让球
    play_244(244,81),//全场大小
    play_245(245,81),//主队大小
    play_246(246,81),//客队大小

    play_249(249,82),//前五局让球
    play_250(250,82),//前五局大小
    play_251(251,82),//前五局主队大小
    play_252(252,82),//前五局客队大小
    ;
    /**
     * 玩法Id
     */
    private Integer playId;
    /**
     * 所属时段
     */
    private Integer playTimeStage;
    private PlayIdsEnum(Integer playId, Integer playTimeStage) {
        this.playId = playId;
        this.playTimeStage = playTimeStage;
    }

    public Integer getPlayId() {
        return playId;
    }

    public Integer getPlayTimeStage() {
        return playTimeStage;
    }
    public static PlayIdsEnum getPlayIdsEnum(Integer playId1){
        for (PlayIdsEnum playIdsEnum:PlayIdsEnum.values()){
            if (playId1.equals(playIdsEnum.getPlayId())){
                return playIdsEnum;
            }
        }
        return null;
    }

    public static List<PlayIdsEnum> getPlayIdsEnums(List<Integer> playTimeStages ){
        List<PlayIdsEnum> playIdsEnumList=new ArrayList<>();
        for (PlayIdsEnum playIdsEnum:PlayIdsEnum.values()){
            if (playTimeStages.contains(playIdsEnum.getPlayTimeStage())){
                playIdsEnumList.add(playIdsEnum);
            }
        }
        return playIdsEnumList;
    }
    public static List<Integer> getPlayIds(List<Integer> playTimeStages ){
        List<Integer> playIdsEnumList=new ArrayList<>();
        for (PlayIdsEnum playIdsEnum:PlayIdsEnum.values()){
            if (playTimeStages.contains(playIdsEnum.getPlayTimeStage())){
                playIdsEnumList.add(playIdsEnum.getPlayId());
            }
        }
        return playIdsEnumList;
    }
}
