package com.panda.sport.rcs.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.vo
 * @Description : 玩法查询参数
 * @Author : Paca
 * @Date : 2020-07-22 10:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
public class MarketCategoryQueryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 运动种类ID
     */
    private Long sportId;

    /**
     * 标准赛事ID
     */
    private Long matchId;

    /**
     * 玩法集ID，0表示查询全部
     */
    private Long categorySetId;

    /**
     * 赔率类型，OU-欧洲盘，HK-香港盘
     */
    private String marketOddsKind;

    /**
     * 是否属于前十五分钟快照赛事，1-是，0-否
     */
    private Integer matchSnapshot = 0;

}
