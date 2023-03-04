package com.panda.sport.rcs.pojo.odds;

import com.panda.sport.rcs.pojo.constants.TradeConstant;
import lombok.Data;

import java.io.Serializable;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 平衡值入参
 * @Author : Paca
 * @Date : 2021-02-11 10:55
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BalanceReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 平衡值类型，1-跳赔平衡值，2-跳盘平衡值，清零平衡值时必传
     */
    private Integer balanceType;

    /**
     * 累计差额计算方式，0-投注额差值，1-投注/赔付混合差值
     */
    private Integer balanceOption;

    /**
     * 赛种ID
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
     * 子玩法ID
     */
    private String subPlayId;

    /**
     * 盘口位置
     */
    private Integer placeNum;

    /**
     * 盘口ID
     */
    private Long marketId;

    public String getPlaceNumId() {
        String placeNumId = String.format("%s_%s_%s", getMatchId(), getPlayId(), getPlaceNum());
        if (TradeConstant.BASKETBALL_X_PLAYS.contains(getPlayId().intValue()) || (!new Long(2L).equals(sportId))){
            placeNumId = String.format("%s_%s_%s_%s", getMatchId(), getPlayId(),getSubPlayId(), getPlaceNum());
        }
        return placeNumId;
    }

    /**
     * 足球按盘口ID统计，篮球按位置ID统计
     *
     * @return
     */
    public String keySuffix() {
        if (new Long(1L).equals(sportId)) {
            return String.valueOf(marketId);
        }
        return getPlaceNumId();
    }
}
