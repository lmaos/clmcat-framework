package com.clmcat.basics.commons.trace;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TraceExecutorService implements ExecutorService {

	private ExecutorService ex;

	public TraceExecutorService(ExecutorService ex) {
		this.ex = ex;
	}

	@Override
	public void execute(Runnable command) {
		ex.execute(TraceUtils.trace(command));
	}

	@Override
	public void shutdown() {
		ex.shutdown();

	}

	@Override
	public List<Runnable> shutdownNow() {
		return ex.shutdownNow();
	}

	@Override
	public boolean isShutdown() {
		return ex.isShutdown();
	}

	@Override
	public boolean isTerminated() {
		return ex.isTerminated();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return ex.awaitTermination(timeout, unit);
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		return ex.submit(TraceUtils.trace(task));
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		return ex.submit(TraceUtils.trace(task), result);
	}

	@Override
	public Future<?> submit(Runnable task) {
		return ex.submit(TraceUtils.trace(task));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		return ex.invokeAll(TraceUtils.traceCallables(tasks));
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		return ex.invokeAll(TraceUtils.traceCallables(tasks), timeout, unit);
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		return ex.invokeAny(TraceUtils.traceCallables(tasks));
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return ex.invokeAny(TraceUtils.traceCallables(tasks), timeout, unit);
	}

}