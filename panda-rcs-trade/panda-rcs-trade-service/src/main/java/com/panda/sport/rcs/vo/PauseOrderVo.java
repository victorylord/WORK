package com.panda.sport.rcs.vo;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * 暂停接拒单
 */
@Data
@Accessors(chain = true)
public class PauseOrderVo {
    /**
     * 赛种
     */
    private Long pauseTime;
    /**
     * 操盘手id
     */
    private Integer traderId;
    /**
     * 操盘手名称
     */
    private String trader;
}
