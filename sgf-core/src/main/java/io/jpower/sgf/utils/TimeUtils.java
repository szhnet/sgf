package io.jpower.sgf.utils;

import java.util.Calendar;

/**
 * @author zheng.sun
 */
public class TimeUtils {

    /**
     * 1分钟有多少秒
     */
    public static final int MINUTE_SECONDS = 60;

    /**
     * 1小时有多少秒
     */
    public static final int HOUR_SECONDS = 60 * MINUTE_SECONDS;

    /**
     * 1天有多少秒
     */
    public static final int DAY_SECONDS = 24 * HOUR_SECONDS;

    /**
     * 1秒有多少毫秒
     */
    public static final int SECONDS_MILLIS = 1000;

    /**
     * 1分钟有多少毫秒
     */
    public static final int MINUTE_MILLIS = 60 * SECONDS_MILLIS;

    /**
     * 1小时有多少毫秒
     */
    public static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;

    /**
     * 1天有多少毫秒
     */
    public static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    /**
     * 判断指定的两个时间是否在同一天内
     *
     * @param time1
     * @param time2
     * @return
     */
    public static boolean isSameDay(long time1, long time2) {
        Calendar cal = Calendar.getInstance();

        cal.setTimeInMillis(time1);
        int year1 = cal.get(Calendar.YEAR);
        int day1 = cal.get(Calendar.DAY_OF_YEAR);

        cal.setTimeInMillis(time2);
        int year2 = cal.get(Calendar.YEAR);
        int day2 = cal.get(Calendar.DAY_OF_YEAR);

        return (year1 == year2) && (day1 == day2);
    }

    /**
     * 计算指定的两个时间的间隔天数
     *
     * @param time1
     * @param time2
     * @return
     */
    public static int betweenDays(long time1, long time2) {
        long beginOfDay1 = getBeginOfDay(time1);
        long beginOfDay2 = getBeginOfDay(time2);

        long d = Math.abs(beginOfDay1 - beginOfDay2);

        return (int) (d / DAY_MILLIS);
    }

    /**
     * 得到指定时间当天的开始时间，也就是当天的 00:00:00.000
     *
     * @param time
     * @return
     */
    public static long getBeginOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    /**
     * 得到指定时间当天的结束时间，也就是当天的 23:59:59.999
     *
     * @param time
     * @return
     */
    public static long getEndOfDay(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        return cal.getTimeInMillis();
    }

    /**
     * 获得指定时间的下一个小时的时间
     * <p>
     * <p>
     * 比如现在是2016.10.17 17:50，那么返回的时间是2016.10.17 18:00
     *
     * @param time 指定的时间
     * @return
     */
    public static long getNextHour(long time) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        cal.add(Calendar.HOUR_OF_DAY, 1);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

}
