package com.clmcat.basics.selectserver.selectmaster.redisselect;

import java.util.function.Consumer;

import com.clmcat.basics.selectserver.selectmaster.SelectMaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 * Redis 选择主机的实现
 * 
 * @author zhangxingyu
 *
 */
public class RedisMaster implements SelectMaster, InitializingBean {

	private static final Logger log = LoggerFactory.getLogger(RedisMaster.class);

	
	private Object lock = new Object();

	private RedisMasterLock masterLock;
	
	public void setMasterLock(RedisMasterLock masterLock) {
		this.masterLock = masterLock;
	}

	private boolean masterServer; // 标记是否是主机

	private Thread selectMasterThread = new Thread(this::run, "selectMasterThread");
	{
		selectMasterThread.setDaemon(true);
	}
	
	public RedisMaster() {

	}
	

	public RedisMaster(RedisMasterLock masterLock) {
		this.masterLock = masterLock;
	}


	/**
	 * 选择我是主机，如果不是则永久阻塞， 发生异常后则中断任务。
	 * 
	 * @throws InterruptedException
	 */
	@Override
	public boolean selectMaster() throws InterruptedException {
		while (!isMasterServer()) {
			synchronized (lock) {
				if (!masterServer) {
					lock.wait(120000);
				}
			}
		}
		return isMasterServer();
	}
	
	/**
	 * 选择我是主机，如果不是是则阻塞 time 时间， 并返回是否是主机的状态， 发生异常后则中断任务。
	 * @param waitTime
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public boolean selectMaster(long waitTime) throws InterruptedException {
		if (waitTime <= 0) {
			return selectMaster();
		}
		synchronized (lock) {
			if (!masterServer) {
				lock.wait(waitTime);
			}
			return masterServer;
		}
	}

	@Override
	public boolean isMasterServer() {
		synchronized (lock) {
			return masterServer;
		}
	}

	private void ackMasterServer() {
		synchronized (lock) {
			masterServer = true;
			lock.notifyAll(); // 释放所有
		}
	}
	
	private void cancelMasterServer() {
		synchronized (lock) {
			masterServer = false;
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		// 选择的主机的操作
		selectMasterThread.start();
	}
	
	private void run() {
		log.info("启动 Redis 选主任务");
		int holdFail = 0;
		while (true) {
			try {
				if (masterLock == null) {
					log.error("不存在 RedisMasterLock 配置");
					continue;
				}
				if (isMasterServer()) {
					// 如果没 hold 成功，则取消当前主机状态
					if (!masterLock.lockHold()) {
						holdFail++;
						if (holdFail == 3) {
							cancelMasterServer();
						}
					} else {
						holdFail = 0; // hold 失败数设置为 0
					}
				} else {
					if (masterLock.lock()) {
						holdFail = 0;
						ackMasterServer();
					}
				}
				
			} catch (Exception e) {
				log.error("选主操作发生异常!");
			} finally {
				sleep(3000); // 3秒选择一次或3秒更新一次
			}
		}
	}
	
	
	
	private void sleep(long time) {
		try {
			Thread.sleep(time);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 进行主机执行。不是主机则会 阻塞。
	 * @param exec
	 * @throws InterruptedException
	 */
	@Override
	public void masterExecute(Consumer<RedisMaster> exec) throws InterruptedException {
		if (this.selectMaster()) {
			exec.accept(this);
		}
	}
	
	/**
	 * 进行主机执行。不是主机则会 阻塞 time 毫秒时间, 阻塞结束后会验证当前是否是主机，如果不是则不进行执行exec()函数，如果是主机则执行。
	 * @param exec
	 * @param waitTime 阻塞时间 毫秒
	 * @throws InterruptedException
	 */
	@Override
	public void masterExecute(Consumer<RedisMaster> exec, long waitTime) throws InterruptedException {
		if (this.selectMaster(waitTime)) {
			exec.accept(this);
		}
	}
	
	
}
