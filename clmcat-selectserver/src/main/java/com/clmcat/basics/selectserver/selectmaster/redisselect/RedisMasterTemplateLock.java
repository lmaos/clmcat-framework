package com.clmcat.basics.selectserver.selectmaster.redisselect;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.connection.RedisStringCommands.SetOption;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
/**
 * StringRedisTemplate 实现Redis分布式锁
 * 
 * @author zhangxingyu
 *
 */
public class RedisMasterTemplateLock implements RedisMasterLock {

	StringRedisTemplate redisTemplate;
	int secondsTimeout = 12; // 12秒超时
	private String lockKey;
	private String unlockKey;
	private String currentValue;

	public RedisMasterTemplateLock(String key, StringRedisTemplate redisTemplate) {
		this.redisTemplate = redisTemplate;
		this.lockKey = key + ".lock";
		this.unlockKey = key + ".unlock";
		this.currentValue = new Random().nextInt(10000) + "_" + System.currentTimeMillis();
	}

	@Override
	public boolean lock() {
		try {
			return redisTemplate.execute(connection -> {
				byte[] keyBytes = lockKey.getBytes(Charset.forName("UTF-8"));
				byte[] valueBytes = currentValue.getBytes();
				Boolean ok = connection.set(keyBytes, valueBytes, Expiration.from(secondsTimeout, TimeUnit.SECONDS),
						SetOption.SET_IF_ABSENT);
				return ok != null && ok;
			}, true);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public void unlock() {
		redisTemplate.execute(connection -> {
			byte[] unkeyBytes = unlockKey.getBytes(Charset.forName("UTF-8"));
			byte[] keyBytes = lockKey.getBytes(Charset.forName("UTF-8"));
			byte[] valueBytes = connection.get(keyBytes);
			if (valueBytes == null) {
				return false;
			}
			String value = new String(valueBytes);
			if (value.equals(this.currentValue)) {
				connection.del(keyBytes);
				connection.publish(unkeyBytes, keyBytes);
				return true;
			} else {
				return false;
			}

		}, true);
	}

	@Override
	public boolean lockHold() {
		try {
			return redisTemplate.execute(connection -> {
				byte[] keyBytes = lockKey.getBytes(Charset.forName("UTF-8"));
				byte[] valueBytes = connection.get(keyBytes);
				if (valueBytes == null) {
					return false;
				}
				String value = new String(valueBytes);
				if (value.equals(this.currentValue)) {
					connection.expire(keyBytes, secondsTimeout); // 刷新时间
					return true;
				} else {
					return false;
				}

			}, true);
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
