package com.panda.sport.rcs.trade.vo;

import lombok.Data;

/**
 * @program: xindaima
 * @description:
 * @author: kimi
 * @create: 2021-01-13 20:32
 **/
@Data
public class MedianVo {
    /**
     * 所属时段
     */
    private Integer  theirTime;
    /**
     * 分差中值
     */
    private Integer differential;
    /**
     * 总分钟值
     */
    private Integer total;
}
