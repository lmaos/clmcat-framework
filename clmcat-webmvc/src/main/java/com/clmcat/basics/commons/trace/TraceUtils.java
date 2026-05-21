package com.clmcat.basics.commons.trace;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

import com.clmcat.basics.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

public class TraceUtils {

	private static final Logger log = LoggerFactory.getLogger(TraceUtils.class);

	public static String getRequestId() {
		try {
			return MDC.get("requestId");
		} catch (Exception e) {
			log.error("请求追踪获取ID失败", e);
			return null;
		}
	}

	public static void setRequestId(String requestId) {
		try {
			if (requestId != null) {
				MDC.put("requestId", requestId);
			} else {
				MDC.remove("requestId");
			}
		} catch (Exception e) {
			log.error("请求追踪设置ID失败", e);
		}
	}

	public static void removeRequestId() {
		try {
			MDC.remove("requestId");
		} catch (Exception e) {
			log.error("请求追踪删除ID失败", e);
		}
	}

	public static Runnable trace(Runnable r) {
		String requestId = getRequestId();
		boolean trace = StringUtils.isNotBlank(requestId); // 是否追踪.
		if (!trace) { // 不追踪.
			return r;
		}
		Runnable runnable = () -> {
			try {
				setRequestId(requestId);
				r.run();// 无论如何都会执行.
			} finally {
				removeRequestId();
			}
		};
		return runnable;
	}

	public static <T> List<Callable<T>> traceCallables(Collection<? extends Callable<T>> rs) {
		List<Callable<T>> callables = new ArrayList<Callable<T>>(rs.size());
		for (Callable<T> callable : rs) {
			callables.add(trace(callable));
		}
		return callables;
	}

	public static <T> Callable<T> trace(Callable<T> r) {
		String requestId = MDC.get("requestId");
		boolean trace = StringUtils.isNotBlank(requestId); // 是否追踪.
		if (!trace) { // 不追踪.
			return r;
		}
		Callable<T> runnable = () -> {
			try {
				MDC.put("requestId", requestId);
				return r.call();// 无论如何都会执行.
			} catch (Exception e) {
				log.error("线程跟踪线程错误!", e);
				throw e;
			} finally {
				MDC.remove("requestId");
			}
		};
		return runnable;
	}

	public static ExecutorService trace(ExecutorService es) {
		return new TraceExecutorService(es);
	}

	public static ExecutorService proxy(ExecutorService es) {
		ExecutorService nes = (ExecutorService) Proxy.newProxyInstance(es.getClass().getClassLoader(),
				new Class[] { ExecutorService.class }, new TraceExecutorInvoke(es));
		return nes;
	}

	public static ScheduledExecutorService trace(ScheduledExecutorService es) {
		return new TraceScheduledExecutorService(es);
	}

	public static ScheduledExecutorService proxy(ScheduledExecutorService es) {
		ScheduledExecutorService nes = (ScheduledExecutorService) Proxy.newProxyInstance(es.getClass().getClassLoader(),
				new Class[] { ScheduledExecutorService.class }, new TraceExecutorInvoke(es));
		return nes;
	}
}
