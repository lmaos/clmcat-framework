package com.clmcat.basics.commons.https.safety;

import java.util.concurrent.atomic.LongAdder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.clmcat.basics.commons.locks.SingleLock;

/**
 * 失败配置
 * 
 * @author zhangxingyu
 *
 */
public class HttpSafetyFailRecord {

	private static final Logger log = LoggerFactory.getLogger(HttpSafetyFailRecord.class);

	/**
	 * 允许最大的失败次数
	 */
	private int failMaxSize;
	/**
	 * 时长-在这么长时间内失败
	 */
	private long duration;

	private long recordTime = 0;

	private long sleepTime; // 失眠这么久
	private long sleepStartTime; // 睡眠记录开始时间

	private Object failResult; // 失败结果
	
	/**
	 * 
	 * @param failMaxSize 失败重试最大数
	 * @param duration    失败统计周期
	 * @param sleepTime   在失败周期内持续失败等于或超过最大数则停止应答这么久的时间
	 * @param failResult  失败后直接返回默认值.
	 */
	public HttpSafetyFailRecord(int failMaxSize, long duration, long sleepTime, Object failResult) {
		this.failMaxSize = failMaxSize;
		this.duration = duration;
		this.sleepTime = sleepTime;
		this.failResult = failResult;
	}

	/**
	 * 失败计数
	 */
	private LongAdder failSize = new LongAdder();

	private SingleLock lock = new SingleLock();

	public int getFailMaxSize() {
		return failMaxSize;
	}

	public long getDuration() {
		return duration;
	}

	public LongAdder getFailSize() {
		return failSize;
	}

	public Object getFailResult() {
		return failResult;
	}

	public boolean isFail() {
		verifyReset();
		return verifyIsFail();
	}

	/**
	 * 失败数
	 * 
	 * @return
	 */
	public long getFailSizeValue() {
		verifyReset();
		return failSize.longValue();
	}

	/**
	 * 增加计数
	 */
	public long incrBy() { // 增加计数
		if (!isFail()) {
			failSize.increment();
		}
		return failSize.longValue();
	}

	private boolean verifyIsFail() {
		if (sleepStartTime == 0 && failSize.longValue() >= this.failMaxSize) { // 触发失败
			try {
				return lock.synchronizedLock(() -> {
					// 标记失败 要验证是否可以标记. 失败没有被标记并且确实是触发失败的条件
					if (sleepStartTime == 0 && failSize.longValue() >= this.failMaxSize) {
						sleepStartTime = System.currentTimeMillis(); // 确认失败了
						return true;
					} else {
						return sleepStartTime > 0;
					}
				});
			} catch (Exception e) {
				log.error("http 安全计数配置 verifyIsFail", e);
			}
		}
		return sleepStartTime > 0;
	}

	private void verifyReset() { // 验证并重置
		long currentTime = System.currentTimeMillis();
		if (isTimeout()) { // 是否过期
			lock.synchronizedLockNotThrows(() -> {
				if (isTimeout()) { // 满足条件则重新计数
					recordTime = currentTime;
					sleepStartTime = 0;
					failSize.reset();
				}
			});
		}
	}

	// 超时状态-- 重新计数验证
	private boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		// 如果没有触发 sleep 状态, 则 duration 周期计数清空, 如果触发 sleep 状态 使用 sleepStartTime 周期清空.
		if (sleepStartTime > 0) {
			return currentTime - sleepStartTime > sleepTime;
		} else {
			return currentTime - recordTime > duration;
		}

	}

}