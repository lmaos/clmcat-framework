package com.clmcat.basics.selectserver.mastertimer;

import org.springframework.scheduling.config.ScheduledTask;
/**
 * 启动任务后的 future  用于停止任务
 * 
 * @author zhangxingyu
 *
 */
public class MasterTimerFutureSpring implements MasterTimerFuture {
	
	private MasterTimerRunnable masterTimerRunnable;
	
	private ScheduledTask scheduledTask;

	MasterTimerFutureSpring(MasterTimerRunnable masterTimerRunnable, ScheduledTask scheduledTask) {
		super();
		this.masterTimerRunnable = masterTimerRunnable;
		this.scheduledTask = scheduledTask;
	}

	public ScheduledTask getScheduledTask() {
		return scheduledTask;
	}

	public MasterTimerRunnable getMasterTimerRunnable() {
		
		return masterTimerRunnable;
	}
	
	public boolean cancel() {
		scheduledTask.cancel();
		return true;
	}
}
