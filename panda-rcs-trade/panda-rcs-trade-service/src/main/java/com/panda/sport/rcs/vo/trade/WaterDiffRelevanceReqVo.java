package com.panda.sport.rcs.vo.trade;

import com.google.common.collect.Lists;
import com.panda.sport.rcs.enums.SportIdEnum;
import com.panda.sport.rcs.enums.YesNoEnum;
import com.panda.sport.rcs.exeception.RcsServiceException;
import com.panda.sport.rcs.utils.RcsAssert;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : 水差关联请求参数
 * @Author : Paca
 * @Date : 2021-01-15 11:25
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
public class WaterDiffRelevanceReqVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运动种类ID
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
    private Long subPlayId;

    /**
     * 水差是否关联，0-不关联，1-关联
     */
    private Integer relevanceType;

    public void paramCheck() {
        RcsAssert.gtZero(sportId, "赛种[sportId]不能为空");
        RcsAssert.gtZero(matchId, "赛事ID[matchId]不能为空");
        RcsAssert.gtZero(playId, "玩法ID[playId]不能为空");
        if (!YesNoEnum.isYes(relevanceType) && !YesNoEnum.isNo(relevanceType)) {
            throw new RcsServiceException("关联标志[relevanceType]有误");
        }
        if (SportIdEnum.isBasketball(sportId) && Lists.newArrayList(145L, 146L).contains(playId)) {
            RcsAssert.gtZero(playId, "子玩法ID[subPlayId]不能为空");
        }
    }
}
