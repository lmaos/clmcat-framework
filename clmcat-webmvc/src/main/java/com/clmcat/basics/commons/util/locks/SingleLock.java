package com.clmcat.basics.commons.util.locks;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import com.clmcat.basics.commons.util.locks.LockConsumerWrapper.LockCallback;
import com.clmcat.basics.commons.util.locks.LockConsumerWrapper.LockComsumer;
import com.clmcat.basics.commons.util.locks.LockConsumerWrapper.LockComsumerNotThrows;
import com.clmcat.basics.commons.util.locks.LockConsumerWrapper.LockExecutor;
import com.clmcat.basics.commons.util.locks.LockConsumerWrapper.LockExecutorrNotThrows;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 单锁
 * 
 * @author zhangxingyu
 *
 */
public class SingleLock {

	private static final Logger log = LoggerFactory.getLogger(SingleLock.class);

	private ReentrantLock lock = new ReentrantLock();

	private Condition await = lock.newCondition();

	public static SingleLock newLockEntry() {
		return new SingleLock();
	}

	public void synchronizedLock(LockComsumer exec) throws Exception {
		try {
			lock.lock();
			exec.exec(await);
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 同步锁 不抛出异常-支持await操作
	 * 
	 * @param exec
	 * @return 返回当前锁, 利用一把锁可以进行多段操作.
	 */
	public SingleLock synchronizedLockNotThrows(LockComsumerNotThrows exec) {
		try {
			lock.lock();
			exec.exec(await);
		} catch (Exception e) {
			log.error("lock-err", e);
		} finally {
			lock.unlock();
		}
		return this;

	}

	/**
	 * 同步锁 抛出异常
	 * 
	 * @param exec 执行单代码逻辑
	 * @throws Exception
	 */
	public void synchronizedLock(LockExecutor exec) throws Exception {
		try {
			lock.lock();
			exec.exec();
		} finally {
			lock.unlock();
		}
	}

	public <T>T synchronizedLock(LockCallback callback) throws Exception {
		try {
			lock.lock();
			return (T)callback.exec();
		} finally {
			lock.unlock();
		}
	}

	/**
	 * 同步锁 不抛出异常
	 * 
	 * @param exec 执行单代码逻辑
	 */
	public SingleLock synchronizedLockNotThrows(LockExecutorrNotThrows exec) {
		try {
			lock.lock();
			exec.exec();
		} catch (Exception e) {
			log.error("lock-err", e);
		} finally {
			lock.unlock();
		}
		return this;
	}
	/**
	 * 乐观锁, 返回false 则没有拿到锁.
	 */
	public boolean optimisticLock(LockExecutor exec) throws Exception {
		if (lock.tryLock()) {
			try {
				exec.exec();
			} finally {
				lock.unlock();
			}
			return true;
		} else {
			return false;
		}
	}
	/**
	 * 乐观锁, 返回false 则没有拿到锁.
	 */
	public boolean optimisticLockNotThrows(LockExecutorrNotThrows exec) {
		if (lock.tryLock()) {
			try {
				exec.exec();
			} catch (Exception e) {
				log.error("optimisticLock-err", e);
			} finally {
				lock.unlock();
			}
			return true;
		} else {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		SingleLock lock = SingleLock.newLockEntry();

		// 单段开锁.
		lock.synchronizedLock(() -> {
			// 同步
			System.out.println(00);
		});

		// 分三段让行
		lock.synchronizedLockNotThrows(() -> {
			// 业务逻辑 1
			System.out.println(11);
		}).synchronizedLockNotThrows(() -> {
			// 业务逻辑 2
			System.out.println(22);
		}).synchronizedLockNotThrows(() -> {
			// 业务逻辑 3
			System.out.println(33);
		});
	}
}