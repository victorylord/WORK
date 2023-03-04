package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.StandardSportMarketOdds;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SportMarketOddsVo extends StandardSportMarketOdds implements Comparable {


    /**
     * 排序值。
     */
    private Integer orderNo;

    /**
     * 实货量
     */
    private BigDecimal betAmount;

    /**
     * 盈利期望值
     */
    private BigDecimal profitValue;

    /**
     * 注单数
     */
    private BigDecimal betNum;

    @Override
    public int compareTo(Object other) {
        if (other == null) {
            return 1;
        }
        // 先比较orderNo，orderNo相等则比较Id
        SportMarketOddsVo otherObj = (SportMarketOddsVo) other;
        if (otherObj.getOrderNo() == null && this.getOrderNo() == null) {
            return this.getId() - otherObj.getId() <= 0L ? -1 : 1;
        }
        if (otherObj.getOrderNo() == null) {
            return 1;
        }
        if (this.getOrderNo() == null) {
            return -1;
        }
        int compareVal = this.getOrderNo() - otherObj.getOrderNo();
        if (compareVal == 0) {
            return this.getId() - otherObj.getId() <= 0L ? -1 : 1;
        }
        return compareVal;


    }
}
