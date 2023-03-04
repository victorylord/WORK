package com.panda.sport.rcs.pojo.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import com.panda.sport.rcs.utils.LongToStringSerializer;
import lombok.Data;

import java.io.Serializable;
import java.util.Set;

/**
 * 风控消费清除水差
 *
 * @author gray
 * @version 1.0.0
 * @date 2023/2/4 13:59
 */
@Data
public class StandardCategoryIdsDiffDTO extends RcsBaseEntity<StandardCategoryIdsDiffDTO>{


    /**
     * 标准赛事ID
     */
    private Long standardMatchId;
    /**
     * 赛种ID
     */
    private Long sportId;
    /**
     * ao赛事id
     */
    private Long aoMatchId;
    /**
     * 玩法ID集合
     */
    private Set<Long> standardCategoryIds;
}
