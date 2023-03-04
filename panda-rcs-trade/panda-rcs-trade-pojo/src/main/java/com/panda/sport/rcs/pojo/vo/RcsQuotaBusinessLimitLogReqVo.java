package com.panda.sport.rcs.pojo.vo;

import com.panda.sport.rcs.pojo.RcsQuotaBusinessLimitLog;

import java.io.Serializable;

public class RcsQuotaBusinessLimitLogReqVo extends RcsQuotaBusinessLimitLog implements Serializable {

    /**
     * 当前是第几页
     */
    private Integer current;

    /**
     * 每一页大小
     */
    private Integer size;


    public Integer getCurrent() {
        if (this.current == null) {
            return 1;
        }
        return this.current;
    }

    public Integer getSize() {
        if (this.size == null) {
            return 10;
        }
        return this.size;
    }

    private String startTime;

    private String endTime;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
