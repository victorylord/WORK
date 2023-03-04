package com.panda.sport.rcs.trade.param;

import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  panda-rcs-trade
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  修改
 * @Date: 2020-06-26 14:27
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RcsMatchConfigParam {
    private Long matchId;
    /**
     * 1为早盘2为滚球
     */
    private Integer matchType;
    private String liveRiskManagerCode;
    private String preRiskManagerCode;

    private List<Long> categoryIds;

    /**
     * 操盘手
     */
    private String userName;
}
