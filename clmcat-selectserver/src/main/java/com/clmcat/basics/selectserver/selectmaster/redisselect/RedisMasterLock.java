package com.clmcat.basics.selectserver.selectmaster.redisselect;
/**
 * Redis 分布式锁
 * 
 * @author zhangxingyu
 *
 */
public interface RedisMasterLock {
	/**
	 * 获得锁，获得失败则返回false
	 * 
	 * @return
	 */
	boolean lock();

	/**
	 * 释放锁
	 * 
	 * @return
	 */
	void unlock();

	/**
	 * 更新锁持有状态
	 */
	boolean lockHold();
}
