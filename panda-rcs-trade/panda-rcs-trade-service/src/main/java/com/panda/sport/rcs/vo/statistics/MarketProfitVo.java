package com.panda.sport.rcs.vo.statistics;


import lombok.Data;

import java.math.BigDecimal;

/**
 * @author carver
 * 盘口期望值
 */

@Data
public class MarketProfitVo implements Comparable<MarketProfitVo>{
    /**
     * 比分
     */
    private Integer score;

    /**
     * 期望值
     */
    private BigDecimal profitValue;
    
    
    /**
     * @Description   //TODO
     * @Param [o]
     * @Author  myname
     * @Date  14:19 2020/1/13
     * @return int
     **/
    @Override
    public int compareTo(MarketProfitVo o){
        if(this.score >o.getScore()){
            return 1;
        }
        return -1;
    }
}
