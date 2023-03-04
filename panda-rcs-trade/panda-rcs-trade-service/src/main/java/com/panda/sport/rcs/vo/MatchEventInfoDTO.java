package com.panda.sport.rcs.vo;

import lombok.Data;

import java.util.List;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-07-22 11:49
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class MatchEventInfoDTO {
    /**
     * 赛事Id
     **/
    Long matchId;
    /**
     * 数据来源
     **/
    String dataSource;
    //    /**
//     * 当前页数
//     **/
//    Integer pageNum;
//    /**
//     * 当前页数大小
//     **/
//    Integer pageSize;
    /**
     * 时间
     **/
    Long eventTime;


    List<Integer> eventTypes;

    Integer sort;

    Integer limit;


}
