package com.panda.sport.rcs.mongo;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 赛事玩法集信息
 */
@Data
public class ScoreVo {
    /**
     currentScore 小分 matchScore 盘分 setScore 局比分 qiangScore  抢分
     */
    private String currentScore;
    private String matchScore;
    private String setScore;
    private String qiangScore;
    /**
     * 比赛阶段
     */
    private Integer period;
    /**
     * 0没有赔率 1 有赔率
     */
    private Integer isOdds;

    private int sort;
    /**
     * 显示
     */
    private boolean categorySetShow = true;

}
