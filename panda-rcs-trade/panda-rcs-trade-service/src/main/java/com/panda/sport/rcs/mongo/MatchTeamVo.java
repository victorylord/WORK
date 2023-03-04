package com.panda.sport.rcs.mongo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.panda.sport.rcs.pojo.danger.RcsDangerTeam;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.mongo
 * @Description :  赛事比赛队伍
 */
@Data
public class MatchTeamVo {
    @Field(value = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Map<String, String> names;
    private Long nameCode;
    /**
     * 比赛中的作用：主客队或者其他
     */
    private String matchPosition;
    /**
     * 红牌数
     */
    private Integer redCardNum;

    /**
     * 危险球队对象
     */
    private RcsDangerTeam dangerTeam;
}
