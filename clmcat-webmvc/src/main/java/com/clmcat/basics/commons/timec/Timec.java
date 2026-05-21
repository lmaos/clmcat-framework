package com.clmcat.basics.commons.timec;

import com.alibaba.fastjson.JSON;

public class Timec {

	private long time;

	public Timec(long startTime, long endTime) {
		this(endTime - startTime);
	}
	
	public Timec(long time) {
		if (time > 0) {
			this.time = time;
			day = (int) (time / 1000 / 60 / 60 / 24);
			hour = (int) ((time / 1000 / 60 / 60) % 24);
			min = (int) ((time / 1000 / 60) % 60);
			sec = (int) ((time / 1000) % 60);
			ms = (int) (time % 1000);
		}
	}

	private int day;
	private int hour;
	private int min;
	private int sec;
	private int ms;

	public long getTime() {
		return time;
	}

	public int getDay() {
		return day;
	}

	public int getHour() {
		return hour;
	}

	public int getMin() {
		return min;
	}

	public int getSec() {
		return sec;
	}

	public int getMs() {
		return ms;
	}

	public static void main(String[] args) {
		Timec timec = new Timec(1000 * 60 * 60 * 24 * 68L);
		System.out.println(JSON.toJSONString(timec));
	}

	public String getMod() {
		if (day > 0) {
			return "day";
		}
		if (hour > 0) {
			return "hour";
		}
		if (min > 0) {
			return "min";
		}
		if (sec > 0) {
			return "sec";
		}
		return "ms";
	}
}
