package com.panda.sport.rcs.vo.secondary;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 篮球两项盘请求入参
 * @Author : Paca
 * @Date : 2021-02-19 10:50
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FootballTwoReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 赛种ID
     */
    private Long sportId;
    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 1单关，2串关
     */
    private Integer seriesType;

    /**
     * 足球玩法集ID ：10011-全场 10012-上半场 10013-下半场 10014-&玩法 10015-时间类玩法
     * 10016-特殊玩法  10017-角球玩法 10018-罚牌 10019-加时进球 10020-点球 10021-晋级
     */
    private List<Long> categorySetIds;

    /**
     * liveOddBu
     * 是否支持滚球:1=支持；0=不支持(变更为是否开滚球，对应siness字段)
     */
    private Integer liveOddBusiness;
}
