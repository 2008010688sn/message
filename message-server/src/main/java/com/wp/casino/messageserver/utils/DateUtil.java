package com.wp.casino.messageserver.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * @author sn
 * @date 2020/6/3 16:45
 */
public class DateUtil {

    /**
     * 获取当天的零点时间戳
     *
     * @return 当天的零点时间戳
     */
    public static int getTodayStartTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        long todayTimeStampLong=calendar.getTime().getTime();
        int todayTimeStamp=(int)(todayTimeStampLong/1000);
        return todayTimeStamp;
    }

}
