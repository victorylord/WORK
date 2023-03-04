package com.panda.sport.rcs.mongo;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * 赛事玩法集信息
 */
@Data
@Accessors(chain = true)
public class MatchCatgorySetVo {

    private Long catgorySetId;

    private String score;

    private int sort;

    /**
     * 显示
     */
    private boolean categorySetShow = false;

    /**
     * fore显示
     */
    private  boolean forecastSetShow = false;

}
