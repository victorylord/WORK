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
 * @Description :  更新时间
 */
@Document(collection = "match_top")
@Data
@Accessors(chain = true)
public class MatchTop implements Serializable {

    @Field(value = "id")
    private Long id;
    /**
     * 赛事Id
     */
    private Long matchId;
    /**
     * 置顶时间
     */
    private Long topTime;
    /**
     * 操盘手Id
     */
    private String traderId;
    /**
     * 1 置顶 2 取消置顶
     */
    private Integer topStatus;

}
