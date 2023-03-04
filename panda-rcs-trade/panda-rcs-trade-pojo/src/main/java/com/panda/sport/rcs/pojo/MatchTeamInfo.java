package com.panda.sport.rcs.pojo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Map;

/**
 * @author :  YiMing
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.pojo
 * @Description :  赛事比赛队伍
 */
@Data
public class MatchTeamInfo {
    @Field(value = "id")
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    private Map<String, String> names;
    private String text;
    private Long nameCode;
    /**
     * 比赛中的作用：主客队或者其他
     */
    private String matchPosition;
    /**
     * 红牌数
     */
    private Integer redCardNum;
}
