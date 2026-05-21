package com.clmcat.basics.commons.timec;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeF {
	/**
	 * 格式化时间 <br>
	 * 
	 * 每天的 中午12点 : yyyy-MM-dd 12:00:00 <br><br>
	 * 
	 * 每月1号的 15点 : yyyy-MM-01 15:00:00 <br><br>
	 * 
	 * 每天每小时的第10分: yyyy-MM-dd HH:10:00 <br><br>
	 * 
	 * 年: yyyy, 月 MM, 日 dd, 小时HH , 分 mm, 秒 ss
	 * 
	 * 
	 * @param formatTime
	 * @return
	 */
	public static String formatTime(String formatTime) {
		return formatTime(formatTime, null);
	}
	
	
	/**
	 * 格式化时间 <br>
	 * 
	 * 每天的 中午12点 : yyyy-MM-dd 12:00:00 <br><br>
	 * 
	 * 每月1号的 15点 : yyyy-MM-01 15:00:00 <br><br>
	 * 
	 * 每天每小时的第10分: yyyy-MM-dd HH:10:00 <br><br>
	 * 
	 * 年: yyyy, 月 MM, 日 dd, 小时HH , 分 mm, 秒 ss
	 * 
	 * 
	 * @param formatTime
	 * @return
	 */
	public static String formatTime(String formatTime, TimeZone timeZone) {
		return formatTime(System.currentTimeMillis(), formatTime, timeZone);
	}
	
	/**
	 * 格式化时间 <br>
	 * 
	 * 每天的 中午12点 : yyyy-MM-dd 12:00:00 <br><br>
	 * 
	 * 每月1号的 15点 : yyyy-MM-01 15:00:00 <br><br>
	 * 
	 * 每天每小时的第10分: yyyy-MM-dd HH:10:00 <br><br>
	 * 
	 * 年: yyyy, 月 MM, 日 dd, 小时HH , 分 mm, 秒 ss
	 * 
	 * W1,W2,W3,W4,W5,W6,W7代表切换到 周1,2,3,4,5,6,7
	 * 
	 * D+1 代表明天, D+n代表 增加N天的时间.  可以简化为 D1,D2,D3 分别代表增加1天,2天,3天
	 * D-1 代表减少1条, D-n代表 减少N天的时间, 不可简化.
	 * 
	 * 举例: 下周一 D+7W1yyyyMMdd, 上上周三D-14W3yyyyMMdd
	 * 
	 * 
	 * 
	 * @param formatTime
	 * @return
	 */
	public static String formatTime(long currentTime, String formatTime, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		
		if (timeZone != null) {
			calendar.setTimeZone(timeZone);
		}
		calendar.setTimeInMillis(currentTime);
		StringBuilder sbuf = new StringBuilder(formatTime.length() + 1);
		char[] stack = new char[4];
		int index = 0;
		int lastYearStartIndex = -1;
		int lastYearValue = -1;
		for (int i = 0; i < formatTime.length(); i++) {
			char c = formatTime.charAt(i);
			if (c >= '1' && c <= '7') {
				if (index > 0) {
					// 切换周一到周日
					if (stack[index - 1] == 'W') {
						int week = c - '0';
						int curweel = calendar.get(Calendar.DAY_OF_WEEK);
						curweel--;
						if (curweel == 0) {
							curweel = 7;
						}
						// 切换到这周几
						calendar.add(Calendar.DAY_OF_MONTH, week - curweel);
						
					} else {
						sbuf.append('W');
					}
					index = 0;
				} else {
					sbuf.append(c);
				}
			} else if (c == 'D') { // D1... D2... D100

				StringBuilder numF = new StringBuilder(10);
				int j = i + 1;
				for ( ; j < formatTime.length(); j++) {
					char n = formatTime.charAt(j);
					
					if (n == '-' || n == '+') {
						if (j > i + 1) {
							break;
						}
					}else if (n < '0' || n > '9' ) {
						break;
					} 
					numF.append(n);
				}
				if (numF.length() == 0) {
					sbuf.append('D');
				} else {
					String text = numF.toString();
					int day = 0;
					if (text.equals("+")) {
						day = 1;
					} else if (text.equals("-")) {
						day = -1;
					} else {
						day = Integer.parseInt(text);
					}
					calendar.add(Calendar.DAY_OF_MONTH, day);
					i = j-1;
				}
				
			} else if (c == 'y' || c == 'M' || c == 'd' || c == 'H' || c == 'm' || c == 's' || c == 'S' || c == 'W' || c == 'w' || c == '#') {
				// 如果切换了
				if (index > 0 && stack[index - 1] != c) {
					sbuf.append(stack, 0, index);
					index = 0;
				}
				
				// 记录当前字符
				stack[index++] = c;

				// 年份和周的处理需要特殊进行. 如果不是年和周的混合处理则恢复默认
				if (c != 'y' && c != 'W' && lastYearStartIndex != -1) { 
					lastYearStartIndex = -1;
					lastYearValue = -1;
				}
				
				if (index == 4) {
					if (c == 'y') { // 如果有4个一样的
						lastYearStartIndex = sbuf.length(); // 记录最后一次追加年的位置
						lastYearValue = calendar.get(Calendar.YEAR);
						sbuf.append(lastYearValue);
					} else if (c == '#') { // 时间回归
						calendar.setTimeInMillis(currentTime);
					} else {
						sbuf.append(stack);
					}
					index = 0;
				} else if (index == 3) {
					if (c == 'S') {
						int value = calendar.get(Calendar.MILLISECOND);// 000;
						index = 0;
						if (value < 10) {
							sbuf.append("00" + value);
						} else if (value < 100) {
							sbuf.append("0" + value);
						} else {
							sbuf.append(value);
						}
					}
				} else if (index == 2) {
					Integer value = null;
					if (c == 'M') {
						value = calendar.get(Calendar.MONTH) + 1; // 00
					} else if (c == 'd') {
						value = calendar.get(Calendar.DAY_OF_MONTH); // 00
					} else if (c == 'H') {
						value = calendar.get(Calendar.HOUR_OF_DAY); // 00
					} else if (c == 'm') {
						value = calendar.get(Calendar.MINUTE); // 00
					} else if (c == 's') {
						value = calendar.get(Calendar.SECOND); // 00
					} else if (c == 'W') {
						calendar.add(Calendar.DAY_OF_MONTH, -1); // 减掉1天, 日历从周日开始计算的
						value = calendar.get(Calendar.WEEK_OF_YEAR); // 00
						calendar.add(Calendar.DAY_OF_MONTH, 1);  // 切换回现在
						if (lastYearStartIndex != -1) { // 如果年存在
							if (calendar.get(Calendar.MONTH) == 11 && value == 1) {//如果是12月并且是第一周则处理近期的年值.
								lastYearValue ++;
								sbuf.replace(lastYearStartIndex, lastYearStartIndex+4, String.valueOf(lastYearValue));
							}
							// 还原年
							lastYearStartIndex = -1;
							lastYearValue = -1;
						}
					} else if (c == 'w') {
						int curweel = calendar.get(Calendar.DAY_OF_WEEK);
						curweel--;
						if (curweel == 0) {
							curweel = 7;
						}
						value = curweel;
					}
					if (value != null) {
						index = 0;
						if (value < 10) {
							sbuf.append("0" + value);
						} else {
							sbuf.append(value);
						}
					}
				}
					
			} else {
				sbuf.append(c);
			}
		}
		return sbuf.toString();
	}
	
	/**
	 * 格式化时间 -- 以下周1为起点 + 7天<br>
	 * 
	 * 每天的 中午12点 : yyyy-MM-dd 12:00:00 <br><br>
	 * 
	 * 每月1号的 15点 : yyyy-MM-01 15:00:00 <br><br>
	 * 
	 * 每天每小时的第10分: yyyy-MM-dd HH:10:00 <br><br>
	 * 
	 * 年: yyyy, 月 MM, 日 dd, 小时HH , 分 mm, 秒 ss
	 * 
	 * 
	 * @param formatTime
	 * @param timeZone 时区  null 为当前时区
	 * @return
	 */
	public static String formatTimeNextWeek(String formatTime, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.DAY_OF_WEEK, 2);
		calendar.add(Calendar.DAY_OF_MONTH, 7);
		return formatTime(calendar.getTimeInMillis(), formatTime, timeZone);
	}
	
	/**
	 * 格式化时间 -- 以上周1为起点 - 7天<br>
	 * 
	 * 每天的 中午12点 : yyyy-MM-dd 12:00:00 <br><br>
	 * 
	 * 每月1号的 15点 : yyyy-MM-01 15:00:00 <br><br>
	 * 
	 * 每天每小时的第10分: yyyy-MM-dd HH:10:00 <br><br>
	 * 
	 * 年: yyyy, 月 MM, 日 dd, 小时HH , 分 mm, 秒 ss
	 * 
	 * 
	 * @param formatTime
	 * @param timeZone 时区  null 为当前时区
	 * @return
	 */
	public static String formatTimePreyWeek(String formatTime, TimeZone timeZone) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(Calendar.DAY_OF_WEEK, 2);
		calendar.add(Calendar.DAY_OF_MONTH, -7);
		return formatTime(calendar.getTimeInMillis(), formatTime, timeZone);
	}
	
	public static void main(String[] args) {
		// 从今天 19点到明天01点
		long currentTime = System.currentTimeMillis(); // 实用一个确定点时间戳.
		String format = formatTime(currentTime, "W3yyyy-MM-dd 19 ww,####yyyy-MM-dd 01 ww", null);
		System.out.println(format);
		
		
		//System.out.println(formatTime("D+10W1 W2 W3 W4 W5 W6 W7 WW yyyyMMddHHmmss SSS", null));
		//System.out.println(formatTimePreyWeek("W1 W2 W3 W4 W5 W6 W7 WW yyyyMMddHHmmss SSS", null));
	}
}
