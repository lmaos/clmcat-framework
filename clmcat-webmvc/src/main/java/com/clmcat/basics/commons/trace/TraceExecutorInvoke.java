package com.clmcat.basics.commons.trace;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;

public class TraceExecutorInvoke implements InvocationHandler {
	private ExecutorService src;
//	private static final Logger log = LoggerFactory.getLogger(TraceExecutorInvoke.class);
	public TraceExecutorInvoke(ExecutorService src) {
		if (src instanceof ScheduledExecutorService) {
			this.src = new TraceScheduledExecutorService((ScheduledExecutorService)src);
		} else {
			this.src = new TraceExecutorService(src);
			
		}
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		
		return method.invoke(src, args);
	}

	
}
