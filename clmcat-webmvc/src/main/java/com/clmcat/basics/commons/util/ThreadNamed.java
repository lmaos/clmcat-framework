package com.clmcat.basics.commons.util;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import com.clmcat.basics.commons.trace.TraceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 使用线程名创建线程, 实现线程工厂
 * 
 * @author zhangxingyu
 *
 */
public class ThreadNamed implements ThreadFactory {
	private String name; // 线程名称
	private boolean daemon = true;
	private AtomicLong id = new AtomicLong();
	
	private static final Logger log = LoggerFactory.getLogger(ThreadNamed.class);


	public ThreadNamed(String name) {
		this.name = name;
	}

	public ThreadNamed(String name, boolean daemon) {
		this.name = name;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread(Runnable r) {
		Runnable runnable = TraceUtils.trace(r);
		Thread thread = new Thread(runnable, name + "-" + id.incrementAndGet());
		thread.setDaemon(daemon);
		return thread;
	}

	/**
	 * thread.setDaemon(true);
	 * 
	 * @param name
	 * @return
	 */
	public static ThreadNamed of(String name) {
		return new ThreadNamed(name);
	}

	/**
	 * thread.setDaemon(false);
	 * 
	 * @param name
	 * @return
	 */
	public static ThreadNamed ofNoDaemon(String name) {
		return new ThreadNamed(name, false);
	}

	public Thread start(Runnable r) {
		Thread thread = newThread(r);
		thread.start();
		return thread;
	}
}
