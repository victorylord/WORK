package com.panda.sport.rcs.trade.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @author :  myname
 * @Project Name :  xindaima
 * @Package Name :  com.panda.sport.rcs.trade.util
 * @Description :  TODO
 * @Date: 2020-08-16 15:10
 * @ModificationHistory Who    When    What
 * --------  ---------  --------------------------
 */
public class DateUtil {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public  static  StringBuilder getMinuteBySecond(Long second ){
        StringBuilder stringBuilder=new StringBuilder();
        stringBuilder.append(second / 60);
        stringBuilder.append(":");
        stringBuilder.append(second % 60);
        return stringBuilder;
    }

    public static String getCurrentDayStr(){
        return sdf.format(Calendar.getInstance().getTime()).substring(0,10);
    }
}
