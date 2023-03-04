package com.panda.sport.rcs.trade.enums;

import com.panda.sport.rcs.constants.RcsConstant;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author :  toney
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.enums
 * @Description :  matchType
 * @Date: 2020-08-05 10:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Getter
@AllArgsConstructor
public enum MatchTypeEnum {
    LIVE(0, "live", "滚球"),

    EARLY(1, "pre", "早盘");

    private Integer id;
    private String code;
    private String name;

    public static String getNameById(Integer value) {
        MatchTypeEnum[] businessModeEnums = values();
        for (MatchTypeEnum businessModeEnum : businessModeEnums) {
            if (businessModeEnum.getId().equals(value)) {
                return businessModeEnum.getName();
            }
        }
        return null;
    }

    public static boolean isLive(Integer type) {
        return LIVE.getId().equals(type);
    }

    public static boolean isEarly(Integer type) {
        return EARLY.getId().equals(type);
    }

    public static MatchTypeEnum getByMatchStatus(Integer matchStatus) {
        return RcsConstant.isLive(matchStatus) ? LIVE : EARLY;
    }
}
