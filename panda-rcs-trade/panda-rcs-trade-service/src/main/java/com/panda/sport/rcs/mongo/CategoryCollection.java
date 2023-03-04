package com.panda.sport.rcs.mongo;

import com.panda.sport.rcs.pojo.statistics.RcsPredictBetOdds;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :玩法集mongo
 * @Date: 2020-07-15 19:15
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Document(collection = "category_collection")
public class CategoryCollection {

    /**
     * 玩法集ID
     */
    @Field(value = "id")
    private Long id;

    /**
     * 运动种类ID
     */
    private Long sportId;

    /**
     * 标准联赛ID
     */
    private Long matchId;

    /**
     * 赛事状态
     */
    private Integer operateMatchStatus;

    /**
     * 玩法集数据源，0-自动，1-手动
     */
    @Deprecated
    private Integer tradeType;

    /**
     * 玩法集状态
     */
    private Integer status;

    /**
     * 玩法集下的所有玩法ID
     */
    private List<Long> categoryIds;

    /**
     * 玩法集合
     */
    private List<MarketCategory> marketCategoryList;

    /**
     * 盘口货量
     */
    private Map<String, List<RcsPredictBetOdds>> betMap;
}
