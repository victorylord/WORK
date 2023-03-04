package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author :  koala
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  监控列表
 * @Date: 2022-07-19 13:46
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RcsMatchMonitorList implements Serializable {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    // 标准赛事Id
    @TableField("match_id")
    private Long matchId;
    // 赛事Id
    @TableField("match_manage_id")
    private String matchManageId;
    // 联赛Id
    @TableField("standard_tournament_id")
    private Long standardTournamentId;
    //事件时间
    @TableField("event_time")
    private Long eventTime;
    //赛事类型 1早盘 0滚球
    @TableField("match_type")
    private Integer matchType;
    //联赛级别
    @TableField("tournament_level")
    private Integer tournamentLevel;
    //球类id
    @TableField("sport_id")
    private Integer sportId;
    //开始时间
    @TableField("begin_time")
    private Long beginTime;
    // 联赛编码
    @TableField("tour_name_code")
    private Long tourNameCode;
    //联赛名称
    @TableField(exist = false)
    private String tourName;
    //主队编码
    @TableField("team_name_code")
    private String teamNameCode;
    //主队名称
    @TableField(exist = false)
    private String teamName;
    @TableField("data_type")
    //1.数据商断连告警（Odds feed Disconnect）
    private Integer dataType;
    @TableField("levels_danger")
    //1:高危 2:中等 3:安全
    private Integer levelsDanger;
    //玩法ID
    @TableField("play_id")
    private Integer playId;

    private Long playIdCode;
    //玩法名称
    @TableField(exist = false)
    private String playName;
    //当前时间戳
    @TableField(exist = false)
    private Long timer;
    //赛事状态 0: 未开盘; 1: 开盘; 2: 关盘; 3: 封盘
    @TableField(exist = false)
    private Integer operateMatchStatus;
}
