package com.panda.sport.rcs.mongo;

import lombok.Data;

/**
 * @Describtion
 * @Auther jstyDC
 * @Date 2022-03-2022/3/16 16:23
 */
@Data
public class BaseBallScoreVo {


    //总比分
    String matchScore;
    //局比分
    String setScore;
    //第一垒
    Integer firstBase;
    //第二垒
    Integer secondBase;
    //第三垒
    Integer thirdBase;
    /**
     * 比赛阶段
     */
    private Integer period;
    //排序
    private int sort;
    /**
     * 显示
     */
    private boolean categorySetShow = true;
}
