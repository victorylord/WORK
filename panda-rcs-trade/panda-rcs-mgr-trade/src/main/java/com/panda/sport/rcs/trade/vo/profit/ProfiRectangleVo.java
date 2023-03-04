package com.panda.sport.rcs.trade.vo.profit;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author :  toney
 * @Project Name :  rcs-parent
 * @Package Name :  com.panda.sport.rcs.trade.vo
 * @Description :  期望详情矩阵
 * @Date: 2020-03-05 11:43
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Data
public class ProfiRectangleVo  implements Comparable<ProfiRectangleVo> {
    /**
     * 分数
     */
    private Integer score;
    /**
     * 期望值
     */
    private BigDecimal profitValue;

    @Override
    public int compareTo(ProfiRectangleVo vo) {//升序
        return this.score - vo.getScore();
    }
}
