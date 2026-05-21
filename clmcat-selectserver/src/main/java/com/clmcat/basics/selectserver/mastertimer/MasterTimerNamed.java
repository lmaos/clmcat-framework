package com.clmcat.basics.selectserver.mastertimer;

import java.util.concurrent.ThreadFactory;
/**
 * 主机任务线程名
 * 
 * @author zhangxingyu
 *
 */
public class MasterTimerNamed implements ThreadFactory {
	private String name;

	public MasterTimerNamed(String name) {
		super();
		this.name = name;
	}
	@Override
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r, name);
		thread.setDaemon(true);
		return thread;
	}
	
	public static MasterTimerNamed of(String name) {
		return new MasterTimerNamed(name);
	}
}
