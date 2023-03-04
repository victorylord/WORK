package com.panda.sport.rcs.trade.enums;

import org.apache.commons.lang3.math.NumberUtils;

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
public enum BasketBallPlayIdScoreTypeEnum {
    play_45(45,1,48,153),
    play_46(46,1,48,155),
    play_47(47,1,48,157),
    play_48(48,1,48,225),

    play_51(51,2,54,153),
    play_52(52,2,54,155),
    play_53(53,2,54,157),
    play_54(54,2,54,225),

    play_57(57,3,60,153),
    play_58(58,3,60,155),
    play_59(59,3,60,157),
    play_60(60,3,60,225),

    play_63(63,4,66,153),
    play_64(64,4,66,155),
    play_65(65,4,66,157),
    play_66(66,4,66,225),

    play_18(18,5,43,153),
    play_19(19,5,43,155),
    play_42(42,5,43,157),
    play_43(43,5,43,225),
    play_87(87,5,43),
    play_97(97,5,43),

    play_26(26,6,142,153),
    play_75(75,6,142,157),
    play_88(88,6,142),
    play_98(98,6,142),
    play_142(142,6,142,225),
    play_143(143,6,142,155),

    play_37(37,7,37,225),
    play_38(38,7,37,153),
    play_39(39,7,37,155),
    play_40(40,7,37,157),
    play_145(145,7,37),
    play_146(146,7,37),
    play_198(198,7,37),
    play_199(199,7,37),

    //冰球玩法
    ICE_HOCKEY_PLAY(4,8,1);

    /**
     * 玩法Id
     */
    private Integer playId;
    /**
     * 所属时段
     */
    private Integer stage;
    /**
     * 独赢玩法
     */
    private Integer singleWinePlayId;
    /**
     * 投注模板id
     */
    private Integer oddsFieldsTempletId;

    private BasketBallPlayIdScoreTypeEnum(Integer playId, Integer stage,Integer singleWinePlayId,Integer oddsFieldsTempletId) {
        this.playId = playId;
        this.stage = stage;
        this.singleWinePlayId = singleWinePlayId;
        this.oddsFieldsTempletId = oddsFieldsTempletId;
    }
    private BasketBallPlayIdScoreTypeEnum(Integer playId, Integer stage,Integer singleWinePlayId) {
        this.playId = playId;
        this.stage = stage;
        this.singleWinePlayId = singleWinePlayId;
    }

    public Integer getPlayId() {
        return playId;
    }

    public Integer getStage() {
        return stage;
    }
    public Integer getSingleWinePlayId() {
        return singleWinePlayId;
    }
    public Integer getOddsFieldsTempletId() {
        return oddsFieldsTempletId;
    }

    public static Integer getStageValue(Long playId) {
        for (BasketBallPlayIdScoreTypeEnum ele : values()) {
            if(ele.getPlayId().intValue() == playId.intValue()) {
                return ele.getStage();
            }
        }
        return NumberUtils.INTEGER_ZERO;
    }
    public static Integer getSingleWinePlayId(Long playId) {
        for (BasketBallPlayIdScoreTypeEnum ele : values()) {
            if(ele.getPlayId().intValue() == playId.intValue()) {
                return ele.getSingleWinePlayId();
            }
        }
        return NumberUtils.INTEGER_ZERO;
    }
    public static Integer getOddsFieldsTempletId(Long playId) {
        for (BasketBallPlayIdScoreTypeEnum ele : values()) {
            if(ele.getPlayId().intValue() == playId.intValue()) {
                return ele.getOddsFieldsTempletId();
            }
        }
        return NumberUtils.INTEGER_ZERO;
    }
}
