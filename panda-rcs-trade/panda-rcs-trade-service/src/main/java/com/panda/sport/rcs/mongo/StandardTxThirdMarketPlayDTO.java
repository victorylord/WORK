package com.panda.sport.rcs.mongo;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.List;

/**
 * 百家赔  基础类
 */

@Data
@Document(collection = "multiple_odds")
public class StandardTxThirdMarketPlayDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 比赛ID:third_match_info.id
     */
    private String matchId;

    /**
     * 运动种类ID. 联赛所属体育种类id, 对应 sport.id
     */
    private Long sportId;

    /**
     * 赛事类型,0:普通赛事、1冠军赛事
     */
    private Integer matchType;

    /**
     * 三方玩法id
     */
    private Long marketCategoryId;

    /**
     * 详情见data_source
     */
    private String dataSourceCode;

    private Long updateTime;

    //盘口列表
    private List<ThirdSportMarketMessage> marketList;
}
