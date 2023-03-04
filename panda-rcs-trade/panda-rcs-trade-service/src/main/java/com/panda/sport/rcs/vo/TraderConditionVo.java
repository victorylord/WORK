package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @ClassName TraderConditionVo
 * @Description: TODO
 * @Author Vector
 * @Date 2019/10/16
 **/
@Data
public class TraderConditionVo {
    /**
     * 比赛ID
     */
    private Long id;
    /**
     * 联赛级别
     */
    private Integer tournamentLevel;
    /**
     * 体育类型ID
     */
    private Long sportId;
    /**
     * liveOddBu
     * 是否支持滚球:1=支持；0=不支持(变更为是否开滚球，对应siness字段)
     */
    private Integer liveOddBusiness;
    /**
     * 比赛ID
     */
    private String matchManageId;
    /**
     * 比赛开始时间
     */
    private Long beginTime;

}
