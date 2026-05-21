package com.clmcat.basics.commons.cache.map;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.clmcat.basics.commons.util.ThreadNamed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 本地缓存MAP
 * 
 * @author zhangxingyu
 *
 * @param <K> 缓存KEY
 * @param <V> 缓存VAL
 */
public class LocalCacheMap<K, V> implements CacheMap<K, V> {
	static Logger log = LoggerFactory.getLogger(LocalCacheMap.class);
	private static ScheduledExecutorService task = Executors.newScheduledThreadPool(1,
			ThreadNamed.of("local-cache-clear"));

	private ConcurrentMap<K, CacheEntry<K>> cache = new ConcurrentHashMap<>();
	private CacheLoad<K, V> cacheLoad;
	private CacheLoadAll<K, V> cacheLoadAll;
	private long timeout = 120;
	private TimeUnit timeUnit = TimeUnit.SECONDS;

	private long timeoutDefVal = -1;
	private TimeUnit timeUnitDefVal = TimeUnit.SECONDS;
	private CacheDefault<K, V> cacheDefault;
	private ScheduledFuture<?> scheduledFuture;

	public LocalCacheMap() {
		clearListener();
	}

	private void clearListener() {
		if (scheduledFuture == null) {
			synchronized (this) {
				if (scheduledFuture == null) {
					scheduledFuture = task.scheduleWithFixedDelay(() -> {
						try {
							for (CacheEntry<K> cacheEntry : cache.values()) {
								if (cacheEntry.isInvalid()) { // 失效移除
									cache.remove(cacheEntry.getKey(), cacheEntry);
								}
							}
						} catch (Exception e) {
							log.error("local cache clear", e);
						}
					}, 2, 2, TimeUnit.MINUTES);
				}
			}
		}
	}

	protected V getValue(K key, CacheLoad<K, V> cacheLoad) {
		clearListener();
		try {
			CacheEntry<K> cacheEntry = cache.get(key);
			Object value = cacheEntry == null ? null : cacheEntry.getValue();
			if (value == null || cacheEntry.isInvalid()) {
				value = null;
				if (cacheLoad != null) {
					value = cacheLoad.load(key);
				} else if (cacheLoadAll != null) {
					Map<K, V> map = cacheLoadAll.load(Arrays.asList(key));
					if (map != null && map.size() > 0) {
						value = map.get(key);
					}
				}
				// load 非空
				if (value != null) {
					long time = getInvalidTime();
					if (time != -1) {
						CacheEntry<K> newCacheEntry = new CacheEntry<K>(key, value, time);
						cache.put(key, newCacheEntry);
					}
				} else if (cacheDefault != null) {
					value = cacheDefault.load(key);
					CacheEntry<K> newCacheEntry = null;
					long time = getDefaultInvalidTime();
					if (value == null) {
						newCacheEntry = new CacheEntry<K>(key, Null.NULL_VALL, time);
					} else {
						newCacheEntry = new CacheEntry<K>(key, value, time);
					}
					cache.put(key, newCacheEntry);
				}
			} else if (value == Null.NULL_VALL) {
				value = null; // cache null
			}

			return (V) value;
		} catch (Exception e) {
			throw new CacheMapException(key, e);
		}
	}

	protected Map<K, V> getValues(Collection<K> keys, CacheLoadAll<K, V> cacheLoadAll) {
		clearListener();
		Map<K, V> map = new HashMap<>();
		if (cacheLoadAll == null) {
			for (K key : keys) {
				V value = getValue(key, cacheLoad);
				if (value != null && value != Null.NULL_VALL) {
					map.put(key, value);
				}
			}

		} else {
			List<K> noexists = new ArrayList<>(keys.size());
			// 从缓存里找
			for (K key : keys) {
				CacheEntry<K> cacheEntry = cache.get(key);
				Object value = cacheEntry == null ? null : cacheEntry.getValue();
				if (value == null || cacheEntry.isInvalid()) {
					noexists.add(key);
				} else if (value != Null.NULL_VALL) {
					map.put(key, (V) value);
				}
			}

			if (noexists.size() > 0) {
				// 缓存找不到
				try {
					Map<K, V> nmap = cacheLoadAll.load(noexists);
					if (nmap != null && nmap.size() > 0) {
						map.putAll(nmap);
						for (K key : noexists) {
							V value = nmap.get(key);
							// loadall 非空
							if (value != null) {
								long time = getInvalidTime();
								if (time != -1) {
									CacheEntry<K> newCacheEntry = new CacheEntry<K>(key, value, time);
									cache.put(key, newCacheEntry);
								}
							} else if (cacheDefault != null) {
								value = cacheDefault.load(key);
								CacheEntry<K> newCacheEntry = null;
								long time = getDefaultInvalidTime();
								if (value == null) {
									newCacheEntry = new CacheEntry<K>(key, Null.NULL_VALL, time);
								} else {
									map.put(key, value);
									newCacheEntry = new CacheEntry<K>(key, value, time);
								}
								cache.put(key, newCacheEntry);
							}

						}

					} else if (cacheDefault != null) {
						long time = getDefaultInvalidTime();
						if (time != -1) {
							for (K key : noexists) {
								V value = cacheDefault.load(key);
								CacheEntry<K> newCacheEntry = null;
								if (value == null) {
									newCacheEntry = new CacheEntry<K>(key, Null.NULL_VALL, time);
								} else {
									map.put(key, value);
									newCacheEntry = new CacheEntry<K>(key, value, time);
								}
								cache.put(key, newCacheEntry);
							}
						}
					}

				} catch (Exception e) {
					throw new CacheMapException(keys, e);
				}
			}
		}

		return map;
	}

	private long getDefaultInvalidTime() {
		if (timeoutDefVal > -1 && timeUnitDefVal != null) {
			return timeoutDefVal == 0 ? 0 : System.currentTimeMillis() + timeUnitDefVal.toMillis(timeoutDefVal);
		} else {
			return -1;
		}
	}

	private long getInvalidTime() {
		if (timeout > -1 && timeUnit != null) {
			return timeout == 0 ? 0 : System.currentTimeMillis() + timeUnit.toMillis(timeout);
		} else {
			return -1;
		}
	}

	@Override
	public V get(K key) {

		return getValue(key, cacheLoad);
	}

	@Override
	public Map<K, V> getAll(Collection<K> keys) {

		return getValues(keys, cacheLoadAll);
	}

	@Override
	public V put(K key, V value) {
		return put(key, value, timeout, timeUnit);
	}

	@Override
	public V put(K key, V value, long timeout, TimeUnit timeUnit) {
		clearListener();
		if (timeout > -1 && timeUnit != null) {
			long time = timeout == 0 ? 0 : System.currentTimeMillis() + timeUnit.toMillis(timeout);
			CacheEntry<K> newCacheEntry = new CacheEntry<K>(key, value, time);
			CacheEntry<K> oldCacheEntry = cache.put(key, newCacheEntry);
			Object oldValue = oldCacheEntry != null ? oldCacheEntry.getValue() : null;
			if (oldValue == null || oldValue == Null.NULL_VALL) {
				return null;
			} else {
				return (V) oldValue;
			}
		} else {
			return null;
		}
	}

	@Override
	public V remove(K key) {
		CacheEntry<K> oldCacheEntry = cache.remove(key);
		Object oldValue = oldCacheEntry != null ? oldCacheEntry.getValue() : null;
		if (oldValue == null || oldValue == Null.NULL_VALL) {
			return null;
		} else {
			return (V) oldValue;
		}
	}

	@Override
	public CacheMap<K, V> setCacheDefault(CacheDefault<K, V> cacheDefault, long timeout, TimeUnit timeUnit) {
		this.timeoutDefVal = timeout;
		this.timeUnitDefVal = timeUnit;
		this.cacheDefault = cacheDefault;
		return this;
	}

	@Override
	public CacheMap<K, V> setTimeout(long timeout, TimeUnit timeUnit) {
		this.timeout = timeout;
		this.timeUnit = timeUnit;
		return this;
	}

	@Override
	public CacheMap<K, V> setCacheLoad(CacheLoad<K, V> cacheLoad) {
		this.cacheLoad = cacheLoad;
		return this;
	}

	@Override
	public CacheMap<K, V> setCacheLoadAll(CacheLoadAll<K, V> cacheLoadAll) {
		this.cacheLoadAll = cacheLoadAll;
		return this;
	}

	@Override
	public void updateCacheTime(K key) {
		CacheEntry<K> cacheEntry = cache.get(key);
		if (cacheEntry != null && !cacheEntry.isGc()) {
			cacheEntry.updateInvalidTime(timeout, timeUnit);
		}
	}

	@Override
	public void clear() {
		cache.clear();
	}

	public void close() {
		synchronized (this) {
			if (this.scheduledFuture != null) {
				clear();
				this.scheduledFuture.cancel(true);
			}
		}
	}

	public static void main(String[] args) throws Exception {
		CacheMap<String, String> cacheMap = new LocalCacheMap<>();
		cacheMap.setCacheDefault((key) -> {
			System.out.println("-------" + key);
			return "def:" + key;
		}, 1, TimeUnit.SECONDS);
//		cacheMap.setCacheLoad((key) -> {
//			System.out.println("key->>cache>>" + key);
//			return "key-->" + key;
//		});
		cacheMap.setCacheLoadAll((keys) -> {
			System.out.println("keys->>cache>>" + keys);
			Map<String, String> map = new HashMap<>();
			for (String key : keys) {
				if (key.equals("123"))
				map.put(key, "keys->key->" + key);
			}
			return map;
		});
		cacheMap.setTimeout(1, TimeUnit.SECONDS);
		System.out.println(cacheMap.getAll(Arrays.asList("123", "234", "3fdg")));
		System.out.println(cacheMap.getAll(Arrays.asList("123", "234", "3fdg")));
		System.out.println(cacheMap.getAll(Arrays.asList("123", "234", "3fdg")));
		Thread.sleep(2000);
		System.out.println(cacheMap.getAll(Arrays.asList("123", "234", "3fdg")));
	}
}