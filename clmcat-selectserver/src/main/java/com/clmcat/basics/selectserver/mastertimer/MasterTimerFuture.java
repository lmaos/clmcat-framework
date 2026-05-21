package com.clmcat.basics.selectserver.mastertimer;

/**
 * 启动任务后的 future  用于停止任务
 * 
 * @author zhangxingyu
 *
 */
public interface MasterTimerFuture {
	
	public MasterTimerRunnable getMasterTimerRunnable() ;
	
	public boolean cancel() ;
}
