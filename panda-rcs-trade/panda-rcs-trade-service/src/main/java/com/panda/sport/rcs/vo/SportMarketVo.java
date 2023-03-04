package com.panda.sport.rcs.vo;

import com.panda.sport.rcs.pojo.StandardSportMarket;
import lombok.Data;

import java.util.Map;

@Data
public class SportMarketVo extends StandardSportMarket implements Comparable {

    /**
     * 盘口投注项
     */
    private Map<Long, SportMarketOddsVo> marketOddsMap = null;


    /**
     * 排序值。
     */
    private Integer orderNo;

    @Override
    public int compareTo(Object other) {
        if (other == null) {
            return 1;
        }
        // 先比较orderNo，orderNo相等则比较Id
        SportMarketVo otherObj = (SportMarketVo) other;
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
