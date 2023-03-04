package com.panda.sport.rcs.pojo;

import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

@Data
public class MatchStatisticsInfoDetail {
    private Long id;

    /**
     * 标准赛事id
     */
    private Long standardMatchId;

    private String code;

    private Integer firstNum;

    private Integer secondNum;

    private Integer t1;

    private Integer t2;

    private Long createTime;

    private Long modifyTime;

    public Integer getT1() {
        if (t1 == null) {
            return NumberUtils.INTEGER_ZERO;
        }
        return t1;
    }

    public Integer getT2() {
        if (t2 == null) {
            return NumberUtils.INTEGER_ZERO;
        }
        return t2;
    }
}