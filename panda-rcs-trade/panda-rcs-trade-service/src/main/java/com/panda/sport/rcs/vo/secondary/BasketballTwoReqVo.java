package com.panda.sport.rcs.vo.secondary;

import com.panda.sport.rcs.enums.Basketball;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class BasketballTwoReqVo implements Serializable {

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
     * 玩法集ID集合，2010-常规赛段，2011-半场，2001-球队，2002-球员，2003-全场，2012-小节
     */
    private List<Long> playSetIds;
    /**
     * 足球玩法集ID ：
     */
    private Long categorySetId;

    /**
     * liveOddBu
     * 是否支持滚球:1=支持；0=不支持(变更为是否开滚球，对应siness字段)
     */
    private Integer liveOddBusiness;

    public Collection<Long> getPlayIds() {
        Set<Long> playIds = new HashSet<>(100);
        this.playSetIds.forEach(playSetId -> {
            for (Basketball.TwoItemPlaySet twoItemPlaySet : Basketball.TwoItemPlaySet.values()) {
                if (twoItemPlaySet.getId().equals(playSetId)) {
                    playIds.addAll(twoItemPlaySet.getPlayIds());
                }
            }
        });
        return playIds;
    }

    public Collection<Long> getSubPlaySetIds() {
        Set<Long> subPlaySetIds = new HashSet<>(20);
        this.playSetIds.forEach(playSetId -> {
            for (Basketball.TwoItemPlaySet twoItemPlaySet : Basketball.TwoItemPlaySet.values()) {
                if (twoItemPlaySet.getId().equals(playSetId)) {
                    subPlaySetIds.addAll(twoItemPlaySet.getSubPlaySetIds());
                }
            }
        });
        return subPlaySetIds;
    }
}
