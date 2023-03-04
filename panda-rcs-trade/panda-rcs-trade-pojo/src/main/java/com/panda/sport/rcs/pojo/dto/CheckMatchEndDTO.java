package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

@Data
public class CheckMatchEndDTO extends RcsBaseEntity<CheckMatchEndDTO> {

    /**
     * 赛事ID
     */
    private Long matchId;

    /**
     * 1接受 0拒绝
     */
    private Integer isEnd;

}
