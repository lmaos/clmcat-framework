package com.clmcat.basics.commons.trace;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class TraceScheduledExecutorService extends TraceExecutorService implements ScheduledExecutorService {
	private ScheduledExecutorService ex;

	public TraceScheduledExecutorService(ScheduledExecutorService ex) {
		super(ex);
		ex = this.ex;
	}

	@Override
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return ex.schedule(TraceUtils.trace(command), delay, unit);
	}

	@Override
	public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
		return ex.schedule(TraceUtils.trace(callable), delay, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return ex.scheduleAtFixedRate(TraceUtils.trace(command), initialDelay, period, unit);
	}

	@Override
	public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
		return ex.scheduleWithFixedDelay(TraceUtils.trace(command), initialDelay, delay, unit);
	}

}
