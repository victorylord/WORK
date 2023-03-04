package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.List;

@Data
public class RcsTournamentOrderAcceptConfig extends RcsBaseEntity<RcsTournamentOrderAcceptConfig> {
    /**
     * 联赛id
     */
    @TableId
    private Long tournamentId;

    /**
     * 1 SR 2 BC 3 BG
     * 枚举 dataSourceEnum
     */
    private String dataSource;

    /**
     * 最短等待时间
     */
    private Integer minWait;

    /**
     * 最长等待时间
     */
    private Integer maxWait;

    /**
     * 接单模式 0 自动 1 手动
     * modeEnum 枚举
     */
    private Integer mode;

    /**
     * 中场休息 0 关闭 1 开启
     * halfTimeEnum  枚举
     */
    private Integer halfTime;

    /**
     * 事件
     */
    @TableField(exist = false)
    private List<RcsTournamentOrderAcceptEventConfig> list;
}