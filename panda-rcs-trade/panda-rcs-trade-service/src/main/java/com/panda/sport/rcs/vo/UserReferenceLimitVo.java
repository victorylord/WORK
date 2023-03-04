package com.panda.sport.rcs.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserReferenceLimitVo implements Serializable {

    private static final long serialVersionUID = 3312124506068991696L;

    /**
     * 赛种 -1表示其他
     */
    private Long sportId;
    /**
     * 用户单注
     */
    private Long userSingleLimit;
    /**
     * 用户单场
     */
    private Long userMatchLimit;
}
