package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.constants.MatchEventEnum;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RcsTournamentOrderAcceptEventConfig  extends RcsBaseEntity<RcsTournamentOrderAcceptEventConfig> {

    /**
     * 主键id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 联赛id
     */
    private Long tournamentId;
    /**
     * 事件英文
     */
    private String eventCode;
    /**
     * 事件中文
     */
    @TableField(exist = false)
    private String eventCodeZH;
    /**
     * 等待时间
     */
    private Integer maxWait;
    /**
     * 0 无效 1 有效
     */
    private Boolean valid;

    public RcsTournamentOrderAcceptEventConfig(MatchEventEnum matchEventEnum, Long matchId) {
        this.tournamentId = matchId;
        this.eventCode = matchEventEnum.getCode();
        this.maxWait = matchEventEnum.getWaitTime();
        this.valid = false;
    }
}