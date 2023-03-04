package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.report.BaseRcsOrderStatisticTime;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-26 10:18
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class BaseRcsOrderStatisticTimeVo extends BaseRcsOrderStatisticTime {
    /**
     * 时间粒度显示
     */
    private String date;
    /**
     * 联赛名称
     */
    private String tournamentName;
    /**
     * 联赛分级. 1: 一级联赛; 2:二级联赛; 3: 三级联赛; 以此类推; 0: 未分级
     */
    private Integer tournamentLevel;
    /**
     * 阶段名称
     */
    private String matchTypeName;
    /**
     * 查询日期
     */
    private String baseDate;
    /**
     * 玩法名称
     */
    private String playName;
    /**
     * 赛种名称
     */
    private String sportName;
    /**
     * 日
     */
    private String orderDay;
    /**
     * 年
     */
    private String orderYear;
    /**
     * 期
     */
    private String orderPhase;
    /**
     * 周
     */
    private String orderWeek;
    /**
     * 期 周 开始时间
     */
    private String startDate;


}
