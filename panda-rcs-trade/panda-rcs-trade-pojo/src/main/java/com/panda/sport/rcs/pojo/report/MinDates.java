package com.panda.sport.rcs.pojo.report;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  holly
 * @Project Name :rcs-parent
 * @Package Name :com.panda.sport.rcs.pojo.report
 * @Description :
 * @Date: 2019-12-28 18:39
 */
@Data
public class MinDates  implements Serializable {

    /**
     * 下注时间
     */
    private String minBetDate;

    /*
    赛事开始时间
     */
    private String minMatchDate;
}
