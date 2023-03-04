package com.panda.sport.rcs.mongo;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.mongo
 * @Description : 玩法集、玩法排序
 * @Author : Paca
 * @Date : 2020-08-26 21:04
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@Accessors(chain = true)
public class CategorySetOrderNo {

    /**
     * 玩法ID
     */
    private Long marketCategoryId;

    /**
     * 玩法集ID
     */
    private Long marketCategorySetId;

    /**
     * 玩法集排序
     */
    private Integer displaySort;

    /**
     * 玩法排序
     */
    private Integer orderNo;

}
