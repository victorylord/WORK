package com.panda.sport.rcs.vo;

import lombok.Data;

/**
 * @author :  myname
 * @Project Name :  daima1
 * @Package Name :  com.panda.sport.rcs.vo
 * @Description :  TODO
 * @Date: 2019-12-24 16:43
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class OrderDetailStatisticVo {
    /**
     * 体育种类
     */
    private Integer sportId;
    /**
     * 联赛id
     */
    private Long standardTournamentId;
    /**
     * 下注类型
     */
    private Integer matchType;
    /**
     * 玩法Id
     */
    private Integer playId;
    /**
     * 投注是否有效
     */
    private Integer isValid;
    /**
     * 注单id
     */
    private String betNo;
}
