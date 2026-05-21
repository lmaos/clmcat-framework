package com.clmcat.basics.commons.timec;

import lombok.Getter;
/**
 * 时间范围
 * 
 * @author zhangxingyu
 *
 */
public class TimeRange {

	@Getter
	private long startTime;
	@Getter
	private long endTime;


	public TimeRange(long startTime, long endTime) {
		super();
		this.startTime = startTime;
		this.endTime = endTime;
	}

	/**
	 * 是否开始, true开始, false 没有开始或已经结束
	 * 
	 * @param time
	 * @return
	 */
	public boolean isStart(long time) {
		return time > startTime && !isEnd(time);
	}

	/**
	 * 是否结束, true 已经结束, false 没有结束.
	 * 
	 * @param time
	 * @return
	 */
	public boolean isEnd(long time) {
		return endTime > 0 && time >= endTime;
	}

	/**
	 * 剩余结束时间
	 * 
	 * @param time
	 * @return
	 */
	public long reEndTime(long time) {
		if (endTime <= 0) {
			return -1;
		}
		return Math.max(0, endTime - time);
	}

	/**
	 * 剩余开始时间
	 * 
	 * @param time
	 * @return
	 */
	public long reStartTime(long time) {
		if (isStart(time)) {
			return 0;
		}
		return Math.max(0, startTime - time);
	}
	
	
}
