package com.panda.sport.rcs.mongo;

import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  玩法集设置
 */
@Document(collection = "match_categorySet")
@Data
@Accessors(chain = true)
public class MatchCategorySetVo implements Serializable {

    @Field(value = "id")
    private Long id;
    /**
     * 运动类型ID
     */
    private Long sportId;
    /**
     * 赛事Id
     */
    private Long matchId;
    /**
     * 玩法集Id
     */
    private Long categorySetId;
    /**
     * 客户端玩法集展示开关 0关 1开
      */
    private Integer clientShow;
    /**
     * 操盘手Id
     */
    private String traderId;
    /**
     * 0 早盘 1 滚球
     */
    private Integer liveOdds;

}
