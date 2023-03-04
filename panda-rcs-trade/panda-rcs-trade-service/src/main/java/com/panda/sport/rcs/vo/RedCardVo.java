package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @author :  enzo
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2020-01-15 11:56
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class RedCardVo implements Serializable {

    private Long sportId;

    /**
     * 标准赛事的id. 对应 standard_match_info.id
     */
    private Long standardMatchId;
    /**
     * 主队红牌数
     */
    private Integer homeNum;
    /**
     * 客队红牌数
     */
    private Integer awayNum;

}
