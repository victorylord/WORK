package com.panda.sport.rcs.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @Project Name: panda-rcs-trade-group
 * @Package Name: com.panda.sport.rcs.utils
 * @Description : 通用工具类
 * @Author : Paca
 * @Date : 2020-10-07 14:30
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
@Slf4j
public final class CommonUtils {

    public static final String LINK_ID = "linkId";
//    public static final String TRACE_ID = "X-B3-TraceId";

    public static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static String getLinkId() {
        return getUUID() + "_trade";
    }

    public static String getLinkId(String suffix) {
        return getLinkId() + "_" + suffix;
    }

    public static String getLinkIdByMdc() {
        try {
            return MDC.get(LINK_ID);
        } catch (Throwable t) {
            log.error("MDC.put 异常", t);
        }
        return getUUID();
    }

    public static String mdcPut() {
        String uuid = getUUID();
        try {
            MDC.put(LINK_ID, uuid);
        } catch (Throwable t) {
            log.error("MDC.put 异常", t);
        }
        return uuid;
    }

    public static void mdcPut(String linkId) {
        try {
            MDC.put(LINK_ID, linkId);
        } catch (Throwable t) {
            log.error("MDC.put 异常", t);
        }
    }

    public static void mdcPutIfAbsent() {
        try {
            String uuid;
            String linkId = MDC.get(LINK_ID);
            if (StringUtils.isBlank(linkId)) {
                uuid = getUUID();
                MDC.put(LINK_ID, uuid);
            }
        } catch (Throwable t) {
            log.error("MDC.put 异常", t);
        }
    }

    public static void mdcRemove() {
        try {
            MDC.remove(LINK_ID);
        } catch (Throwable t) {
            log.error("MDC.remove 异常", t);
        }
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

    public static Integer toInteger(String value, Integer defaultValue) {
        try {
            if (StringUtils.isNotBlank(value)) {
                return new Integer(value);
            }
        } catch (Exception e) {
            log.error("转换Integer异常", e);
        }
        return defaultValue;
    }

    public static boolean isNumber(String value) {
        try {
            new BigDecimal(value);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isInteger(BigDecimal value) {
        try {
            value.intValueExact();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void sleep(TimeUnit timeUnit, long timeout) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.error("sleep异常", e);
        }
    }
}
