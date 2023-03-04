package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.MatchTeamInfo;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 盘口弃用入参
 * @Author : Paca
 * @Date : 2020-11-05 11:06
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MarketDisableVO implements Serializable {

    private static final long serialVersionUID = 6913585277840183142L;

    /**
     * 赛种
     */
    private Long sportId;

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 玩法ID
     */
    private Long playId;

    /**
     * 盘口ID
     */
    private String marketId;

    /**
     * 盘口类型，1-赛前，0-滚球
     */
    private Integer marketType;

    /**
     * 盘口值
     */
    private String marketValue;

    /**
     * 弃用标志，1-弃用，0-启用
     */
    private Integer disableFlag;

    /**
     * 操作類別編碼
     */
    private Integer operatePageCode;

    /**
     * 球队
     */
    private List<MatchTeamInfo> teamList;

    private MarketDisableVO beforeParams;
    /**
     *  赛事管理id
     */
    private String matchManageId;
}
