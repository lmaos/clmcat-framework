package com.clmcat.basics.selectserver.mastertimer;

import java.util.concurrent.ScheduledFuture;
/**
 * 启动任务后的 future  用于停止任务
 * 
 * @author zhangxingyu
 *
 */
public class MasterTimerFutureJdk implements MasterTimerFuture {
	
	private MasterTimerRunnable masterTimerRunnable;
	
	private ScheduledFuture<?> future;

	MasterTimerFutureJdk(MasterTimerRunnable masterTimerRunnable, ScheduledFuture<?> future) {
		super();
		this.masterTimerRunnable = masterTimerRunnable;
		this.future = future;
	}

	public ScheduledFuture<?> getFuture() {
		return future;
	}

	public MasterTimerRunnable getMasterTimerRunnable() {
		
		return masterTimerRunnable;
	}
	
	public boolean cancel() {
		return future.cancel(true);
	}
}
