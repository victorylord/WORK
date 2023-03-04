package com.panda.sport.rcs.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @Project Name : panda-rcs-trade-group
 * @Package Name : panda-rcs-trade-group
 * @Description : BigDecimal 计算工具类
 * @Author : Paca
 * @Date : 2021-12-29 10:48
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
@Getter
@AllArgsConstructor
public enum BigDecimalUtils {

    ROUND_DOWN_2(RoundingMode.DOWN, 2),
    ROUND_HALF_UP_2(RoundingMode.HALF_UP, 2);

    private RoundingMode mode;
    private int scale;

    public BigDecimal add(BigDecimal a, BigDecimal b) {
        return a.add(b).setScale(this.getScale(), this.getMode());
    }

    public BigDecimal subtract(BigDecimal a, BigDecimal b) {
        return a.subtract(b).setScale(this.getScale(), this.getMode());
    }

    public BigDecimal multiply(BigDecimal a, BigDecimal b) {
        return a.multiply(b).setScale(this.getScale(), this.getMode());
    }

    public BigDecimal divide(BigDecimal a, BigDecimal b) {
        return a.divide(b, this.getScale(), this.getMode());
    }

    /**
     * 通过spread计算马来赔，公式：1 - ( spread / 2 )
     *
     * @param spread
     * @return
     */
    public static BigDecimal calMyOddsBySpread(BigDecimal spread) {
        return ROUND_DOWN_2.subtract(BigDecimal.ONE, ROUND_HALF_UP_2.divide(spread, new BigDecimal("2")));
    }

    public static BigDecimal toBigDecimal(String value, BigDecimal defaultValue) {
        try {
            if (StringUtils.isNotBlank(value)) {
                return new BigDecimal(value);
            }
        } catch (Exception e) {
            log.error("转换BigDecimal异常", e);
        }
        return defaultValue;
    }

    public static BigDecimal toBigDecimal(String value) {
        return toBigDecimal(value, BigDecimal.ZERO);
    }

    public static boolean isInteger(BigDecimal value) {
        try {
            // 1 2 3 4 5 6 ...
            value.intValueExact();
            return true;
        } catch (Exception e) {
            // 0.5 1.5 2.5 3.5 4.5 5.5 6.5 ...
            return false;
        }
    }
}
