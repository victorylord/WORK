package com.panda.rcs.warning.vo;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.rcs.warning.vo
 * @Description :  TODO
 * @Date: 2022-07-20 15:23
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@ApiModel(value = "操盘异常监控告警列表查询对象")
public class MatchOperateListQuery extends PageQuery implements Serializable {

    /**
     * 赛事id
     */
    @ApiModelProperty(value = "赛事id集合")
    private List<Long> matchIds;
    /**
     * 赛事状态集合.  比如:1早盘 0滚球
     */
    @ApiModelProperty(value = "赛事状态集合.  比如:1早盘 0滚球 ")
    private List<Integer> matchStatues;
    /**
     * 监控等级
     */
    @ApiModelProperty(value = "监控级别集合 1高 2中 3低 ")
    private List<Integer> monitorLevels;
    /**
     * 体育种类id. 运动种类id 对应sport.id
     */
    @ApiModelProperty(value = "体育种类id集合")
    private List<Integer> sportIds;
    /**
     * 联赛分级. 1: 一级联赛; 2:二级联赛; 3: 三级联赛; 以此类推; 0: 未分级
     */
    @ApiModelProperty(value = "联赛分级. 1: 一级联赛; 2:二级联赛; 3: 三级联赛; 以此类推; 0: 未分级")
    private List<Integer> tournamentLevels;
    /**
     * 环境语言
     */
    private String lang;
    /**
     * 通过doneTime判断是监控列表还是Log
     */
    private Long doneTime;
    //赛事ID
    private Long matchManageId;

    /**
     * 赛事类型 2 仅自己操盘 3 仅自己收藏 4 所有赛事
     */
    @ApiModelProperty(value = "赛事类型 2 仅自己操盘 3 仅自己收藏 4 所有赛事")
    private Integer chooseType;
}
