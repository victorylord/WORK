package com.panda.sport.rcs.pojo;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.List;

@Data
public class RcsMatchOrderAcceptConfig extends RcsBaseEntity<RcsMatchOrderAcceptConfig> {
    /**
     * 主键 赛事id
     */
    @TableId
    private Long matchId;

    /**
    * 1 SR 2 BC 3 BG
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
    */
    private Integer mode;

    /**
    * 中场休息 0 关闭 1 开启
    */
    private Integer halfTime;


    /**
     * 联赛id
     */
    @TableField(exist = false)
    private Long tournamentId;
    /**
     * 事件
     */
    @TableField(exist = false)
    private List<RcsMatchOrderAcceptEventConfig> list;
}