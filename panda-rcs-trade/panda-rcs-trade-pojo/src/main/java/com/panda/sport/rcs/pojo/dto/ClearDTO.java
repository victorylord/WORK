package com.panda.sport.rcs.pojo.dto;

import com.panda.sport.rcs.core.bean.RcsBaseEntity;
import lombok.Data;

import java.util.List;

@Data
public class ClearDTO extends RcsBaseEntity<ClearDTO> {

    /**
     * 赛事ID
     */
    private Long matchId;
    /**
     * 1.来自融合的自动清理标识  2.手动多项盘清理标识   3，task比分清理标识  4.赛前切滚球清识标识  5.数据源切换清理标识
     * 6.手动赔率变更清理平衡值等  7.篮球清水差 8.数据源切换 15.marketSource清理标识 20.融合传过来的清理我们库里的水差和平衡值
     */
    private Integer clearType;
    /**
     * 赛事类型,0:普通赛事、1冠军赛事
     */
    private Integer type;
    /**
     * 清理类型
     */
    private List<ClearSubDTO> list;
    /**
     * 清理类型
     */
    private List<Long> playIds;
    /**
     * 体育种类
     */
    private Long sportId;
    /**
     * 比赛开始时间. 比赛开始时间 UTC时间
     */
    private Long beginTime;


}
