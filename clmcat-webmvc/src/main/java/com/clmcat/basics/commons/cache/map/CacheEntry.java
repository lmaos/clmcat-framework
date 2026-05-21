package com.clmcat.basics.commons.cache.map;

import java.lang.ref.SoftReference;
import java.util.concurrent.TimeUnit;

public class CacheEntry<K> {
	private K key;
	private SoftReference<Object> value;
	private long invalidTime; // 失效时间

	public CacheEntry(K key, Object value, long invalidTime) {
		this.key = key;
		this.value = new SoftReference<>(value);
		this.invalidTime = invalidTime;
	}

	public boolean isGc() {
		return value.get() == null;
	}

	public boolean isInvalid() {
		if (invalidTime == 0) {
			return isGc();
		} else {
			return System.currentTimeMillis() > invalidTime || isGc();
		}
	}

	public K getKey() {
		return key;
	}

	public Object getValue() {
		return value.get();
	}
	
	public void updateInvalidTime(long timeout, TimeUnit timeUnit) {
		this.invalidTime = timeout == 0 ? 0 : System.currentTimeMillis() + timeUnit.toMillis(timeout);
	}
	
	
}
