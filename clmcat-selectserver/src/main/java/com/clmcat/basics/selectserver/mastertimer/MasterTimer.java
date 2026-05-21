package com.clmcat.basics.selectserver.mastertimer;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.Setter;
import com.clmcat.basics.selectserver.selectmaster.SelectMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.support.CronTrigger;

/**
 * 主机定时任务
 * 
 * @author zhangxingyu
 *
 */
public class MasterTimer implements InitializingBean, BeanNameAware, EnvironmentAware {

	private static final Logger log = LoggerFactory.getLogger(MasterTimer.class);
	@Setter
	private SelectMaster selectMaster;
	@Getter
	private ScheduledExecutorService task;
	private ScheduledTaskRegistrar scheduledTaskRegistrar;
	// 主机任务等待执行的队列
	private LinkedList<MasterTimerRunnable> masterTimerRunnables = new LinkedList<MasterTimerRunnable>();
	// 主机任务正在执行的队列
	private LinkedList<MasterTimerFuture> masterTimerFutures = new LinkedList<MasterTimerFuture>();
	// 睡眠操作-> wait
	private Object sleepWait = new Object();
	@Setter
	@Getter
	private String beanName;
	@Setter
	private Environment environment;
	@Setter
	@Getter
	private String masterName;

	private Thread listenerThread = new Thread(this::listenerRun, "master-timer");
	{
		listenerThread.setDaemon(true);
	}

	public MasterTimer(SelectMaster selectMaster, ScheduledExecutorService task) {
		this.selectMaster = selectMaster;
		this.task = task;
		this.scheduledTaskRegistrar = new ScheduledTaskRegistrar();
		this.scheduledTaskRegistrar.setScheduler(task);
	}

	public MasterTimer() {

	}

	public void setSelectMaster(SelectMaster selectMaster) {
		this.selectMaster = selectMaster;
	}

	public void setTask(ScheduledExecutorService task) {
		this.task = task;
		if (scheduledTaskRegistrar == null) {
			scheduledTaskRegistrar = new ScheduledTaskRegistrar();
			scheduledTaskRegistrar.setScheduler(task);
		}
	}
	
	public ScheduledExecutorService getTask() {
		return task;
	}
	
	/**
	 * 添加定时任务
	 * 
	 * @param run 运行的程序块
	 * @param delay 延迟的时间
	 * @param timeUnit 延迟时间单位
	 */
	public void addTimer(String timerName, Runnable run, int delay, TimeUnit timeUnit) {
		synchronized (masterTimerRunnables) {
			masterTimerRunnables.add(new MasterTimerRunnable(timerName, selectMaster, this, run, delay, timeUnit));
		}
		notifySleep(); // 唤醒睡眠
	}

//	public void addTimer(String timerName, Runnable run, int delay, TimeUnit timeUnit) {
//		addTimer(timerName, run, delay, timeUnit, null);
//	}
	
	/**
	 * 添加定时任务
	 * 
	 * @param run 运行的程序块
	 * @param cron quartz 时间表达式
	 * @param timeZone 时区
	 */
	public void addTimer(String timerName, Runnable run, String cron, TimeZone timeZone) {
		synchronized (masterTimerRunnables) {
			masterTimerRunnables.add(new MasterTimerRunnable(timerName, selectMaster, this, run, cron, timeZone));
		}
		notifySleep(); // 唤醒睡眠
	}
	
//	/**
//	 * 添加定时任务
//	 *
//	 * @param run 运行的程序块
//	 * @param cron quartz 时间表达式
//	 * @param timeZone 时区 null时未当前JVM启动的默认时区设置.
//	 */
//	public void addTimer(String timerName, Runnable run, String cron, TimeZone timeZone) {
//		addTimer(timerName, run, cron, timeZone, null);
//	}
//
	/**
	 * 添加定时任务
	 * 
	 * @param run 运行的程序块
	 * @param cron quartz 时间表达式
	 */
	public final void addTimer(String timerName, Runnable run, String cron) {
		addTimer(timerName, run, cron, null);
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		if (environment != null && (this.masterName == null || this.masterName.isEmpty())) {
			if (beanName != null && !beanName.isEmpty()) {
				// 获得 applicationName
				String applicationName = environment.getProperty("spring.application.name");
				this.masterName = applicationName + "." + beanName;
			}
		}

		if (masterName == null || masterName.isEmpty()) {
			throw new RuntimeException("masterName is null or empty");
		}

		listenerThread.start();
	}
	public boolean isEnable() {
		return true;
	}
	private void listenerRun() {
		while (isEnable()) {
			try {
				// 选择主机
				if (selectMaster.selectMaster()) {
					// 如果我是主机。
					synchronized (masterTimerRunnables) {
						// 如果有未开始执行的程序
						if (masterTimerRunnables.size() > 0) {
							int size = masterTimerRunnables.size();
							for (int i = 0; i < size; i++) {
								MasterTimerRunnable masterTimerRunnable = masterTimerRunnables.pop();
								if (masterTimerRunnable != null) {
									if (masterTimerRunnable.isCronTimer()) {
										String cron = masterTimerRunnable.getCron();
										try {
											TimeZone timeZone = masterTimerRunnable.getTimeZone();
											CronTask cronTask = new CronTask(masterTimerRunnable, new CronTrigger(cron, timeZone));
											ScheduledTask scheduledTask = scheduledTaskRegistrar.scheduleCronTask(cronTask);
											masterTimerFutures.add(new MasterTimerFutureSpring(masterTimerRunnable, scheduledTask));
										} catch (Exception e) {
											// timer 表达式异常, 所以无法执行这个timer
											log.error("timer 表达式 方式异常, 请检查配置: {}", cron, e);
										}
									} else {
										int delay = masterTimerRunnable.getDelay();
										TimeUnit timeUnit = masterTimerRunnable.getTimeUnit();
										ScheduledFuture<?> future = task.scheduleWithFixedDelay(masterTimerRunnable, 0, delay, timeUnit);
										masterTimerFutures.add(new MasterTimerFutureJdk(masterTimerRunnable, future));
									}
									
								}
							}
						}
					}
				} else {
					// 如果不是主机。
					// 已经执行的任务进行关闭将任务追加回未执行
					if (masterTimerFutures.size() > 0) {
						synchronized (masterTimerRunnables) {
							int size = masterTimerFutures.size();
							for (int i = 0; i < size; i++) {
								MasterTimerFuture future = masterTimerFutures.pop();
								if (future != null) {
									future.cancel();
									masterTimerRunnables.add(future.getMasterTimerRunnable());
								}
							}
						}
					}
				}
			} catch (InterruptedException e) {
				log.error("选择主机中断!", e);
			}
			sleep(3000); // 3秒选择一次
		}
	}

	// 睡眠
	private void sleep(long time) {
		try {
			synchronized (sleepWait) {
				sleepWait.wait(time);
			}
		} catch (InterruptedException e) {
		}
	}
	// 唤醒睡眠
	private void notifySleep() {
		synchronized (sleepWait) {
			sleepWait.notifyAll();
		}
	}
	/**
	 * 运行中的Timer
	 * @return
	 */
	public Set<String> getRunTimerNames() {
		Set<String> names = new HashSet<String>();
		synchronized (masterTimerRunnables) {
			for (MasterTimerFuture timerFuture: this.masterTimerFutures) {
				names.add(timerFuture.getMasterTimerRunnable().getTimerName());
			}
		}
		return names;
	}
	
	/**
	 * 等待运行的Timer们
	 * @return
	 */
	public Set<String> getReadyTimerNames() {
		Set<String> names = new HashSet<String>();
		synchronized (masterTimerRunnables) {
			for (MasterTimerRunnable timeRun: this.masterTimerRunnables) {
				names.add(timeRun.getTimerName());
			}
		}
		return names;
	}

}
