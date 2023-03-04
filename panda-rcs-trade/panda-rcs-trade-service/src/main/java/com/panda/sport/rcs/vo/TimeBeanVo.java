package com.panda.sport.rcs.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author :  Administrator
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-26 15:47
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeBeanVo implements Serializable {

    /**
     * 选择开始时间
     */
    private String startTime;
    /**
     * 选择结束时间
     */
    private String endTime;
    /**
     * 时间粒度
     */
    private String dateName;
    /**
     * 选择开始时间 冗余字段
     */
    private transient Long startTimeValue;
    /**
     * 选择结束时间
     */
    private transient Long endTimeValue;

    public TimeBeanVo(String startTime, String endTime, String dateName) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.dateName = dateName;
    }
}
