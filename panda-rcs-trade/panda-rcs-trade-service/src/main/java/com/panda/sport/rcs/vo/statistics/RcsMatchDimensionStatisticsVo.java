package com.panda.sport.rcs.vo.statistics;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo.statistics
 * @Description :  TODO
 * @Date: 2019-11-07 14:21
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsMatchDimensionStatisticsVo {
    /**
     * 赛事Id
     */
    private Long matchId;
    /**
     * 实货量
     */
    private BigDecimal realTimeValue;
}
