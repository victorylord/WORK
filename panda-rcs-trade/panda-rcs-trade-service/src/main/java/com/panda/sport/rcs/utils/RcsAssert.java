package com.panda.sport.rcs.utils;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.panda.sport.rcs.exeception.RcsServiceException;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.utils
 * @Description : 断言类
 * @Author : Paca
 * @Date : 2020-08-27 17:32
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public final class RcsAssert {

    public static void isTrue(boolean expression, String errorMsg) {
        if (!expression) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void isFalse(boolean expression, String errorMsg) {
        if (expression) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void isNotBlank(String value, String errorMsg) {
        if (StringUtils.isBlank(value)) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void gtZero(Long number, String errorMsg) {
        if (number == null || number <= 0) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void gtZero(Integer number, String errorMsg) {
        if (number == null || number <= 0) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void geZero(Long number, String errorMsg) {
        if (number == null || number < 0) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void le(Integer number, int value, String errorMsg) {
        if (number == null || number > value) {
            throw new RcsServiceException(errorMsg);
        }
    }

    public static void isNotEmpty(Collection<?> coll, String errorMsg) {
        if (CollectionUtils.isEmpty(coll)) {
            throw new RcsServiceException(errorMsg);
        }
    }

}
