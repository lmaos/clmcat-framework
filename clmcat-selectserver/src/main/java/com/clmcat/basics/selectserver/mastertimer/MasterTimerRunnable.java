package com.clmcat.basics.selectserver.mastertimer;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import com.clmcat.basics.selectserver.selectmaster.SelectMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 主机任务的 Runable 包装
 * 
 * @author zhangxingyu
 *
 */
public class MasterTimerRunnable implements Runnable {
	private String timerName;
	private SelectMaster selectMaster;
	private MasterTimer masterTimer;
	private Runnable runnable;
	private int delay;
	private TimeUnit timeUnit; // 时间单位
	// 
	private String cron;
	private TimeZone timeZone;
	
	// private BooleanSupplier enableSupplier = () -> true; // 默认是永久启用
	@Getter
	@Setter
	private boolean enable = true;

	private static final Logger log = LoggerFactory.getLogger(MasterTimerRunnable.class);
	
	private boolean cronTimer = false;

	MasterTimerRunnable(String timerName, SelectMaster selectMaster, MasterTimer masterTimer, Runnable runnable, int delay,
			TimeUnit timeUnit) {
		super();
		this.timerName = timerName;
		this.selectMaster = selectMaster;
		this.masterTimer = masterTimer;
		this.runnable = runnable;
		this.delay = delay;
		this.timeUnit = timeUnit;
		cronTimer = false;
	}
	
	MasterTimerRunnable(String timerName, SelectMaster selectMaster, MasterTimer masterTimer, Runnable runnable, String cron,
			TimeZone timeZone) {
		super();
		this.timerName = timerName;
		this.selectMaster = selectMaster;
		this.masterTimer = masterTimer;
		this.runnable = runnable;
		this.cron = cron;
		this.timeZone = timeZone == null ? TimeZone.getDefault() : timeZone;
		cronTimer = true;
	}

	@Override
	public void run() {
		try {
			// 启用支持并且是主机才能执行。
			if (enable && selectMaster.isMasterServer()) {
				this.runnable.run();
			}
		} catch (Exception e) {
			if (timerName == null) {
				log.error("timer-error", e);
			} else {
				log.error("timer-error: {}", timerName, e);
			}
		}
	}

	public int getDelay() {
		return delay;
	}

	public TimeUnit getTimeUnit() {
		return timeUnit;
	}
	
//	public boolean enable() {
//		return this.enableSupplier.getAsBoolean();
//	}
	public String getCron() {
		return cron;
	}
	public TimeZone getTimeZone() {
		return timeZone;
	}
	public boolean isCronTimer() {
		return cronTimer;
	}
	
	public MasterTimer getMasterTimer() {
		return masterTimer;
	}
	public String getTimerName() {
		return timerName;
	}
}
