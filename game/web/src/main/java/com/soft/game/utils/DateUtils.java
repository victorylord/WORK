/*
 * Copyright (C) 2023 Baidu, Inc. All Rights Reserved.
 */
package com.soft.game.utils;

import com.soft.game.exception.MessageException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class DateUtils {

    public static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYY_MM_DD_T_HH_MM_SS = "yyyy-MM-dd\'T\'HH:mm:ss";

    private DateUtils() {

    }

    /**
     * 计算两个日期相差年、月、日、时、分、秒
     */
    public static int subtract(Date source, Date target, int field) {

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        if (source.before(target)) {
            cal1.setTime(source);
            cal2.setTime(target);
        } else {
            cal1.setTime(target);
            cal2.setTime(source);
        }

        switch (field) {

            case Calendar.YEAR:
                // 只保留年月日进行比较
                cal1.set(Calendar.HOUR_OF_DAY, 0);
                cal1.set(Calendar.MINUTE, 0);
                cal1.set(Calendar.SECOND, 0);
                cal1.set(Calendar.MILLISECOND, 0);

                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.set(Calendar.MINUTE, 0);
                cal2.set(Calendar.SECOND, 0);
                cal2.set(Calendar.MILLISECOND, 0);

                int years = cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR);
                cal1.add(Calendar.YEAR, years);
                if (years > 0 && cal1.after(cal2)) {
                    return years - 1;
                }
                return years;

            case Calendar.MONTH:

                // 只保留年月日进行比较
                cal1.set(Calendar.HOUR_OF_DAY, 0);
                cal1.set(Calendar.MINUTE, 0);
                cal1.set(Calendar.SECOND, 0);
                cal1.set(Calendar.MILLISECOND, 0);

                cal2.set(Calendar.HOUR_OF_DAY, 0);
                cal2.set(Calendar.MINUTE, 0);
                cal2.set(Calendar.SECOND, 0);
                cal2.set(Calendar.MILLISECOND, 0);

                int month = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
                month = (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)) * 12 + month;
                cal1.add(Calendar.MONTH, month);
                if (month > 0 && cal1.after(cal2)) {
                    return month - 1;
                }
                return month;

            case Calendar.DATE:
                return (int) ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 3600 * 24));

            case Calendar.HOUR:
                return (int) ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 3600));

            case Calendar.MINUTE:
                return (int) ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / (1000 * 60));

            case Calendar.SECOND:
                return (int) ((cal2.getTimeInMillis() - cal1.getTimeInMillis()) / 1000);

            default:
                throw new RuntimeException("field 超出范围");
        }

    }

    /**
     * 计算两个月之差
     */
    public static int subtractMonth(Date source, Date target) {

        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();

        if (source.before(target)) {
            cal1.setTime(source);
            cal2.setTime(target);
        } else {
            cal1.setTime(target);
            cal2.setTime(source);
        }

        int month = cal2.get(Calendar.MONTH) - cal1.get(Calendar.MONTH);
        month = (cal2.get(Calendar.YEAR) - cal1.get(Calendar.YEAR)) * 12 + month;
        return Math.abs(month);

    }

    /**
     * 获取字符串格式时间
     */
    public static String getFormatDate(Date date, int field) {
        if (date == null) {
            return "";
        }
        String dateFormatStr = null;
        switch (field) {
            case Calendar.SECOND:
                dateFormatStr = "yyyy-MM-dd HH:mm:ss";
                break;
            case Calendar.MINUTE:
                dateFormatStr = "yyyy-MM-dd HH:mm";
                break;
            case Calendar.HOUR:
                dateFormatStr = "yyyy-MM-dd HH";
                break;
            case Calendar.DATE:
                dateFormatStr = "yyyy-MM-dd";
                break;
            case Calendar.MONTH:
                dateFormatStr = "yyyy-MM";
                break;
            case Calendar.YEAR:
                dateFormatStr = "yyyy";
                break;
            default:
                throw new RuntimeException("field 超出范围");
        }
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatStr);
        return simpleDateFormat.format(date);
    }

    /**
     * 将字符串转换为指定格式的时间类型
     *
     * @param date   待转换字段
     * @param format 指定格式
     * @return 日期类型参数
     * @throws ParseException
     */
    public static Date getFormatDate(String date, String format) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        sdf.setLenient(false);
        return sdf.parse(date);
    }

    /**
     * 对日期进行格式化（默认格式）
     */
    public static String format(Date date) {
        return format(date, DEFAULT_PATTERN);
    }

    /**
     * 对日期进行格式化
     */
    public static String format(Date date, String pattern) {

        if (date == null) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(date);
    }

    /**
     * 对日期进行格式化（T的格式）
     */
    public static String formatT(Date date) {
        if (date == null) {
            return null;
        }
        return format(date, YYY_MM_DD_T_HH_MM_SS);
    }

    /**
     * Long类型转Date
     */
    public static Date longToDate(Long source) {
        if (source == null || source.longValue() == 0) {
            return null;
        }
        return new Date(source);
    }

    /**
     * 获取最小日期
     */
    public static Date min(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            throw new MessageException("参数不能为空");
        }
        return date1.before(date2) ? date1 : date2;
    }

    public static Date firstDayOfMonth() {
        // 获取本月的第一天
        Calendar cale = Calendar.getInstance();
        cale.add(Calendar.MONTH, 0);
        cale.set(Calendar.DAY_OF_MONTH, 1);
        cale.set(Calendar.HOUR_OF_DAY, 0);
        cale.set(Calendar.MINUTE, 0);
        cale.set(Calendar.SECOND, 1);
        return cale.getTime();

    }

    public static Date lastDayOfMonth() {

        Calendar cale = Calendar.getInstance();
        // 获取本月的最后一天
        cale.add(Calendar.MONTH, 1);
        cale.set(Calendar.DAY_OF_MONTH, 0);
        cale.set(Calendar.HOUR_OF_DAY, 23);
        cale.set(Calendar.MINUTE, 59);
        cale.set(Calendar.SECOND, 59);
        return cale.getTime();
    }

    public static Date getSpecialTime(int year, int month, int day, int hour, int minute, int second) {
        Calendar cale = Calendar.getInstance();
        cale.set(year, month, day, hour, minute, second);
        return cale.getTime();

    }

    /**
     * 按照指定格式格式化时间，并获取时间的毫秒值
     *
     * @param date   待处理时间
     * @param format 指定格式
     * @return 时间的毫秒值
     * @throws ParseException
     */
    public static long getTimeInMillis(String date, String format) throws ParseException {
        Date formatDate = new SimpleDateFormat(format).parse(date);
        Calendar b = Calendar.getInstance();
        b.setTime(formatDate);
        return b.getTimeInMillis();
    }

    /**
     * 判断第一个时间在第二个时间之前，或者和第二个时间相同
     *
     * @param source 时间1
     * @param target 时间2
     * @return 结果，true-第一个时间在第二个时间之前，或者和第二个时间相同 false-第一个时间在第二个时间之后
     */
    public static boolean compareDate(Date source, Date target) {
        if (source == null || target == null) {
            throw new MessageException("参数不能为空");
        }
        return source.before(target) || source.equals(target);
    }

    /**
     * 对日期进行加减n天/n个月/n年等处理
     *
     * @param date  待处理日期
     * @param field 操作单位（天、月、年等）
     * @param day   天数或者月数或者年数等，传正数则进行加操作，传负数则进行减操作
     * @return 处理后的日期
     */
    public static Date getAddTime(Date date, int field, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(field, day);
        return calendar.getTime();
    }

    /**
     * 获取日期的小时和分钟
     *
     * @param date 时间
     * @return 时间的小时和分钟数
     */
    public static Map<String, Integer> getHourAndMinutes(Date date) {
        HashMap<String, Integer> result = new HashMap<>();
        Calendar instance = Calendar.getInstance();
        instance.setTime(date);
        result.put("Minute", instance.get(Calendar.MINUTE));
        result.put("Hour", instance.get(Calendar.HOUR_OF_DAY));
        return result;
    }

    /**
     * @param date  待处理日期
     * @param month 需要加减对应月份
     * @return 处理后的日期
     * @description 对日期加减对应月份
     */
    public static Date getAddTimeByMonth(Date date, int month) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.MONTH, month);
        return calendar.getTime();
    }

    /**
     * @param date 待处理日期
     * @param year 需要加减对应年份
     * @return 处理后的日期
     * @description 对日期加减对应年份
     */
    public static Date getAddTimeByYear(Date date, int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.YEAR, year);
        return calendar.getTime();
    }

    public static int getAge(Date birthday) {

        if (null == birthday) {
            return 0;
        }

        Calendar now = Calendar.getInstance();
        Calendar born = Calendar.getInstance();

        now.setTime(new Date());
        born.setTime(birthday);

        if (born.after(now)) {
            return 0;
        }

        int age = now.get(Calendar.YEAR) - born.get(Calendar.YEAR);
        if (now.get(Calendar.DAY_OF_YEAR) < born.get(Calendar.DAY_OF_YEAR)) {
            age -= 1;
        }
        return age;
    }

    /**
     * 获取某年第一天日期
     * @param year
     * @return
     */
    public static Date getFirstDayOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }

    /**
     * 获取某年最后一天日期
     * @param year
     * @return
     */
    public static Date getLastDayOfYear(int year) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }

    /**
     * 获取当年年份第一天
     * @return
     */
    public static Date getCurrentYearFirstDay() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        return getFirstDayOfYear(currentYear);
    }

    /**
     * 获取当前年份最后一天
     * @return
     */
    public static Date getCurrentYearLastDay() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        return getLastDayOfYear(currentYear);
    }

    /**
     * 获取当前年份
     * @return
     */
    public static int getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        return currentYear;
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime 当前时间
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

}
