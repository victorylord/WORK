package com.panda.sport.rcs.pojo.dao;

import lombok.Data;

import java.util.Objects;

@Data
public class QueryOrderDangerousParam {
    /**
     * 赛事id
     */
    private Long matchId;


    private Long beginTime;

    private Long endTime;

    /**
     * 事件英文
     */
    private String eventCode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        QueryOrderDangerousParam that = (QueryOrderDangerousParam) o;
        return Objects.equals(matchId, that.matchId) &&
                Objects.equals(eventCode, that.eventCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchId, eventCode);
    }
}
