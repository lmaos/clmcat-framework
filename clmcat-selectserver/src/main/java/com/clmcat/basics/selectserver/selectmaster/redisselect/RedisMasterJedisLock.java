package com.clmcat.basics.selectserver.selectmaster.redisselect;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

import java.util.Random;
import java.util.function.Supplier;

/**
 * Jedis 实现Redis分布式锁
 * 
 * @author zhangxingyu
 *
 */
@Slf4j
public class RedisMasterJedisLock implements RedisMasterLock, ApplicationContextAware, InitializingBean {

	Supplier<Jedis> redis;
	int secondsTimeout = 12; // 12秒超时
	private String lockKey;
	private String unlockKey;
	private String currentValue;

	private ApplicationContext applicationContext;

	public RedisMasterJedisLock(Supplier<Jedis> redis) {
		// 没写, 则默认: application-name
		this.redis = redis;
		this.currentValue = new Random().nextInt(10000) + "_" + System.currentTimeMillis();
	}
	public RedisMasterJedisLock(String key, Supplier<Jedis> redis) {
		this.redis = redis;
		this.lockKey = key + ".lock";
		this.unlockKey = key + ".unlock";
		this.currentValue = new Random().nextInt(10000) + "_" + System.currentTimeMillis();
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (this.applicationContext == null) {
			throw new RuntimeException("未找到ApplicationContext!");
		}
		if (lockKey == null) {
			log.warn("未配置Timer,Key . 将使用: spring.application.name");
			Environment environment = this.applicationContext.getEnvironment();
			String property = environment.getProperty("spring.application.name");
			if (property != null) {
				lockKey = property + "-ns-timer.lock";
				unlockKey = property + "-ns-timer.unlock";
				log.info("lockKey={}, unlockKey={}",lockKey, unlockKey);
			} else {
				throw new RuntimeException("未找到spring.application.name, 请在application.properties中配置!");
			}
		}
	}
	boolean init;
	private void init() {
		if (!init) {
			init = true;
            try {
                afterPropertiesSet();
            } catch (Exception e) {
                log.error("无法初始化 RedisMaster",e);
            }
        }
	}
	@Override
	public boolean lock() {
		init(); // 初始化
		if (lockKey == null) {
			log.warn("redis选主、为配置: key!!!");
			return false;
		}
		try {
			try (Jedis jedis = redis.get()) {
				String ok = jedis.set(lockKey, currentValue, SetParams.setParams().nx().ex(secondsTimeout));
				return "OK".equalsIgnoreCase(ok);
			}
		} catch (Exception e) {
			log.error("redis获得锁失败!", e);
			return false;
		}
	}

	@Override
	public void unlock() {
		if (lockKey == null) {
			log.warn("redis选主、为配置: key!!!");
		}
		try (Jedis jedis = redis.get()) {
			String value = jedis.get(lockKey);
			if (value == null) {
				return;
			}
			if (value.equals(this.currentValue)) {
				jedis.del(lockKey);
				jedis.publish(unlockKey, lockKey);
				return;
			} else {
				return;
			}

		}
	}

	@Override
	public boolean lockHold() {
		if (lockKey == null) {
			log.warn("redis选主、为配置: key!!!");
		}
		try {
			try (Jedis jedis = redis.get()) {
				String value = jedis.get(lockKey);
				if (value == null) {
					return false;
				}
				if (value.equals(this.currentValue)) {
					// 刷新时间
					jedis.expire(lockKey, secondsTimeout);
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public String getLockKey() {
		return lockKey;
	}

	public String getUnlockKey() {
		return unlockKey;
	}

}
