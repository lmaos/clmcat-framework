package com.clmcat.basics.commons.timec;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.TreeMap;

import org.apache.commons.lang3.time.DateFormatUtils;

public class Times {

	
	static TreeMap<Integer, Integer> MI5 = new TreeMap<Integer, Integer>();
	static TreeMap<Integer, Integer> MI2 = new TreeMap<Integer, Integer>();
	static TreeMap<Integer, Integer> MI10 = new TreeMap<Integer, Integer>();
	static {
		int len = 60 / 5;
		for (int i = 0; i < len; i++) {
			MI5.put(i * 5, i * 5);
		}
		len = 60 / 2;
		for (int i = 0; i < len; i++) {
			MI2.put(i * 2, i * 2);
		}
		len = 60 / 10;
		for (int i = 0; i < len; i++) {
			MI5.put(i * 10, i * 10);
		}
	}
	public static enum TimesType {
		MI5, MI2, MI10;
	}
	
	
	/**
	 * yyyyMMddHHmm
	 * 
	 * 返回 5分钟的值.
	 * 202211120005,202211120010,202211120015
	 */
	public static String minXTime(long time, TimeZone timeZone, TimesType timesType) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(time);
		// 分钟
		int mi = calendar.get(Calendar.MINUTE);
		if (timesType == TimesType.MI5) {
			mi = MI5.floorKey(mi);
		} else if (timesType == TimesType.MI2) {
			mi = MI2.floorKey(mi);
		} else if (timesType == TimesType.MI10) {
			mi = MI10.floorKey(mi);
		}
		calendar.set(Calendar.MINUTE, mi);
		
		return DateFormatUtils.format(calendar, "yyyyMMddHHmm", timeZone);
	}	
	public static String min5Time(long time) {
		return minXTime(time, TimeZone.getTimeZone("GMT+08:00"), TimesType.MI5);
	}
	public static String min5Time() {
		return min5Time(System.currentTimeMillis());
	}
	
	public static String min10Time(long time) {
		return minXTime(time, TimeZone.getTimeZone("GMT+08:00"), TimesType.MI10);
	}
	public static String min10Time() {
		return min10Time(System.currentTimeMillis());
	}
}
