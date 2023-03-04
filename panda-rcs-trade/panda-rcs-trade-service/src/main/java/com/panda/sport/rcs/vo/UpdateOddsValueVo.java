package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-03-10 16:24
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class UpdateOddsValueVo {
    /**
     * 赛事id
     */
    private Long matchId;

    /**
     * 玩法id
     */
    private Long playId;

    /**
     * 盘口id
     */
    private Long marketId;
    /**
     * 盘口位置
     */
    private Integer marketIndex;
    /**
     * 投注项数据
     */
    private List<OddsValueVo> oddsValueList;
    /**
     * 操盘类型 EU 欧盘 MY 马来盘
     */
    private String marketType;
    /**
     * 比赛类型 0-滚球，1-早盘，3-冠军盘
     */
    private Integer matchType;
    /**
     * 是否生效  0不生效 1生效
     **/
    private Integer active;
}
