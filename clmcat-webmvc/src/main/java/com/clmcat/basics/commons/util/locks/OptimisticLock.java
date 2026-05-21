package com.clmcat.basics.commons.util.locks;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 乐观锁
 * 
 * @author zhangxingyu
 *
 */
public class OptimisticLock {

	private AtomicInteger integer = new AtomicInteger(0);
	private volatile long id;

	/**
	 * 锁, 获得锁返回true, 没有获得锁返回false
	 * 
	 * @return
	 */
	public boolean lock() {
		if (integer.compareAndSet(0, 1)) {
			id = Thread.currentThread().getId();
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 是否有锁
	 * 
	 * @return
	 */
	public boolean isLock() {
		return integer.get() == 1 && Thread.currentThread().getId() == id;
	}

	/**
	 * 解锁
	 */
	public void unlock() {
		if (Thread.currentThread().getId() == id) {
			integer.compareAndSet(1, 0);
		}
	}

}
