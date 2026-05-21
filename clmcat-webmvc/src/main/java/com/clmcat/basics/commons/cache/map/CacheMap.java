package com.clmcat.basics.commons.cache.map;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public interface CacheMap<K, V> {

	V get(K key);

	Map<K, V> getAll(Collection<K> keys);

	V put(K key, V value);

	V put(K key, V value, long timeout, TimeUnit timeUnit);

	V remove(K key);

	/**
	 * 设置默认值
	 * 
	 * @param cacheDefault 默认值构造
	 * @param timeout      超时时间
	 * @param timeUnit     单位
	 * @return
	 */
	CacheMap<K, V> setCacheDefault(CacheDefault<K, V> cacheDefault, long timeout, TimeUnit timeUnit);

	default CacheMap<K, V> setCacheDefault(CacheDefault<K, V> cacheDefault) {
		return setCacheDefault(cacheDefault, -1, null);
	}

	/**
	 * 设置缓存超时时间
	 * 
	 * @param timeout
	 * @param timeUnits
	 * @return
	 */
	CacheMap<K, V> setTimeout(long timeout, TimeUnit timeUnits);

	/**
	 * 设置缓存单个加载方式
	 * 
	 * @param cacheLoad
	 * @return
	 */
	CacheMap<K, V> setCacheLoad(CacheLoad<K, V> cacheLoad);

	/**
	 * 设置缓存批量加载方式
	 * 
	 * @param cacheLoadAll
	 * @return
	 */
	CacheMap<K, V> setCacheLoadAll(CacheLoadAll<K, V> cacheLoadAll);

	/**
	 * 更新这个缓存时间
	 * 
	 * @param key
	 */
	void updateCacheTime(K key);

	/**
	 * 清空缓存
	 */
	void clear();
}