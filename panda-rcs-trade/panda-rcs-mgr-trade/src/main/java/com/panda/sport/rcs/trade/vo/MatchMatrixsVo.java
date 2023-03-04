package com.panda.sport.rcs.trade.vo;


import lombok.Data;

import java.util.List;

/**
 * @author :  toney
 * @Project Name :  panda-rcs
 * @Package Name :  com.panda.sport.rcs.bootstrap.vo
 * @Description : 矩阵查询参数
 * @Date: 2019-11-19 20:57
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchMatrixsVo{
    /**
     * 联赛id
     */
    private List<Long> tournamentList;
    /**
     * 类型：1 ：早盘 ，2： 滚球盘， 3： 冠军盘
     */
    private Integer matchType;
    /**
     * 开始时间
     */
    private Long  matchStartTime;
    /**
     * 结束时间
     */
    private Long matchEndTime;
    /**
     * 操盘手
     */
    private String traderIdOrg;
    /**
     * 赛事阶段  1:比分全场矩阵；2:比分上半场矩阵
     */
    private Long matchStage;
    /**
     * 商户ID
     */
    private List<Long> playIds;
    /**
     * 矩阵大小 5*5 / 6*6
     */
    private Integer size;
    /**
     * 商户id
     */
    private List<Long> tenantIds;
    /**
     * 单位 1 10 100 1000
     */
    private Integer unit;
    /**
     * 第几页
     */
    private Integer pageNum;
    /**
     * 每页大小
     */
    private Integer pageSize;
}
