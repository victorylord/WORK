package com.panda.rcs.warning.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtils {

    private static long HoursMillis = 60*60*1000;
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取指定天数前时间戳
     * @param numberDays
     * @return
     */
    public static Long getTimestamp(int numberDays){
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - numberDays, 23, 59, 59);
        return calendar.getTimeInMillis();
    }

    /**
     * 獲取指定小時數前時間戳
     * @param hours
     * @return
     */
    public static Long minusTimestampByHours(int hours){
        return System.currentTimeMillis() - hours * HoursMillis;
    }

    public static String getCurrTime(int numberDays){
        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH) - numberDays, 23, 59, 59);
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(calendar.getTimeInMillis());
    }

    public static String transferLongToDateStrings(Long millSec) {
        if (millSec == null) {
            return null;
        }
        String dateFormat = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
        Date date = new Date(millSec);
        return sdf.format(date);
    }
    public static String getDateExpect(Long time) {
        Date date = new Date(time);
        date = org.apache.commons.lang3.time.DateUtils.addHours(date, -12);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }


}
