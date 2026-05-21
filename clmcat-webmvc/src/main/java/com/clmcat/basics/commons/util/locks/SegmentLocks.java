package com.clmcat.basics.commons.util.locks;

/**
 * 分段锁
 * 
 * @author zhangxingyu
 *
 */
public class SegmentLocks {

	public final static int MAXIMUM_CAPACITY = 1 << 30;
	private SingleLock[] locks;
	private OptimisticLock[] olocks;

	public SegmentLocks() {
		this(128);
	}

	/**
	 * 
	 * @param parallelSize 允许的并行数量
	 */
	public SegmentLocks(int parallelSize) {
		parallelSize = parallelSize <= 0 ? 1 : parallelSize;
		int length = tableSizeFor(parallelSize);
		locks = new SingleLock[length];
		for (int i = 0; i < length; i++) {
			locks[i] = new SingleLock();
		}
	}

	static final int tableSizeFor(int cap) {
		int n = cap - 1;
		n |= n >>> 1;
		n |= n >>> 2;
		n |= n >>> 4;
		n |= n >>> 8;
		n |= n >>> 16;
		return (n < 0) ? 1 : (n >= MAXIMUM_CAPACITY) ? MAXIMUM_CAPACITY : n + 1;
	}

	/**
	 * 获得当前段段单锁
	 * 
	 * @param value
	 * @return
	 */
	public SingleLock getLock(Object value) {
		if (value == null) {
			return locks[0];
		} else {
			return locks[value.hashCode() & (locks.length - 1)];
		}
	}
	
	public SingleLock getLock(int index) {
		return locks[index & (locks.length - 1)];
	}

	/**
	 * 获得乐观锁 (不可与SingleLock getLock在同业务限制上混用, 否则会加锁失效)
	 * 
	 * @param value
	 * @return
	 */
	public OptimisticLock getOptimisticLock(Object value) {
		if (value == null) {
			return olocks[0];
		} else {
			return olocks[value.hashCode() & (olocks.length - 1)];
		}
	}
	
	/**
	 * 获得乐观锁 (不可与SingleLock getLock在同业务限制上混用, 否则会加锁失效)
	 * 
	 * @param value
	 * @return
	 */
	public OptimisticLock getOptimisticLock(int index) {
		return olocks[index & (olocks.length - 1)];
	}

}
