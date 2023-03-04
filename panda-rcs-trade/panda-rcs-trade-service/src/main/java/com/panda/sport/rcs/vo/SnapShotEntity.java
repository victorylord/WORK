package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  panda-rcs-trade-group
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-07-09 19:19
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class SnapShotEntity {
    /**
     * 玩法id
     **/
    private Long playId;
    /**
     * 排序大小
     **/
    private Long order;
    /**
     * 分组  在第几组
     **/
    private Integer group;

}
