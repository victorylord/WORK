package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-03-10 16:29
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OddsValueVo {

    private String id;
    /**
     * 投注项id
     **/
    private String oddsType;
    /**
     * 投注项新的赔率
     **/
    private Double value;
    /**
     * 是否生效  0不生效 1生效
     **/
    private Integer active;
}
