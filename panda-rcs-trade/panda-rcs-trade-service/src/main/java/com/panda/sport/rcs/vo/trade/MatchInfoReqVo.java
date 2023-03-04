package com.panda.sport.rcs.vo.trade;

import lombok.Data;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.Serializable;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 赛事信息请求类
 * @Author : Paca
 * @Date : 2021-03-16 15:37
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchInfoReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛种ID
     */
    private Long sportId;
    /**
     * 联赛ID
     */
    private Long tournamentId;
    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 0-早盘，1-滚球，默认0
     */
    private Integer liveOddBusiness;

    public Integer getLiveOddBusiness() {
        if (NumberUtils.INTEGER_ONE.equals(liveOddBusiness)) {
            return NumberUtils.INTEGER_ONE;
        } else {
            return NumberUtils.INTEGER_ZERO;
        }
    }
}
