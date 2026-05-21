package com.clmcat.basics.selectserver.selectmaster;

import com.clmcat.basics.selectserver.selectmaster.redisselect.RedisMaster;

import java.util.function.Consumer;


/**
 * 选择主机
 * 
 * @author zhangxingyu
 *
 */
public interface SelectMaster {
	/**
	 * 选择我是主机，如果不是则永久阻塞， 发生异常后则中断任务。
	 * 
	 * @return
	 * @throws InterruptedException
	 */
	public boolean selectMaster() throws InterruptedException;

	/**
	 * 选择我是主机，如果不是是则阻塞 time 时间， 并返回是否是主机的状态， 发生异常后则中断任务。
	 * 
	 * @param waitTime
	 * @return
	 * @throws InterruptedException
	 */
	public boolean selectMaster(long waitTime) throws InterruptedException;

	/**
	 * 判断是否是主机
	 * 
	 * @return
	 */
	public boolean isMasterServer();

	/**
	 * 进行主机执行。不是主机则会 阻塞。
	 * 
	 * @param exec
	 * @throws InterruptedException
	 */
	public void masterExecute(Consumer<RedisMaster> exec) throws InterruptedException;

	/**
	 * 进行主机执行。不是主机则会 阻塞 time 毫秒时间, 阻塞结束后会验证当前是否是主机，如果不是则不进行执行exec()函数，如果是主机则执行。
	 * 
	 * @param exec
	 * @param waitTime 阻塞时间 毫秒
	 * @throws InterruptedException
	 */
	public void masterExecute(Consumer<RedisMaster> exec, long waitTime) throws InterruptedException;
}
