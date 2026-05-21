package com.clmcat.basics.commons.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class MapWrapper<K, V> {
	Map<K, V> map;
	private Supplier<V> supplier0;
	private Function<K, V> supplier1;

	public MapWrapper(Map<K, V> map) {
		this.map = map;
	}

	public MapWrapper(Map<K, V> map, Supplier<V> supplier0) {
		this.map = map;
		this.supplier0 = supplier0;
	}

	public MapWrapper(Map<K, V> map, Function<K, V> supplier1) {
		this.map = map;
		this.supplier1 = supplier1;
	}
	
	public static <K,V> MapWrapper<K, V> hashMap(Function<K, V> supplier1) {
		return new MapWrapper<K, V>(new HashMap<K, V>(), supplier1);
	}
	

	public V get0(K key, Supplier<V> supplier) {
		V val = map.get(key);

		if (val == null) {
			synchronized (map) {
				val = map.get(key);
				if (val == null) {
					map.put(key, val = supplier.get());
				}
			}
		}

		return val;
	}

	public V get1(K key, Function<K, V> supplier) {
		V val = map.get(key);

		if (val == null) {
			synchronized (map) {
				val = map.get(key);
				if (val == null) {
					map.put(key, val = supplier.apply(key));
				}
			}
		}
		return val;
	}

	public V get(K key) {
		if (supplier0 != null) {
			return get0(key, supplier0);
		} else if (supplier1 != null) {
			return get1(key, supplier1);
		} else {
			return map.get(key);
		}
	}

	public Map<K, V> toMap() {
		return this.map;
	}

	public Collection<V> values() {
		return this.map.values();
	}

	public Set<K> keySet() {
		return this.map.keySet();
	}

	public void forEach(BiConsumer<? super K, ? super V> action) {
		this.map.forEach(action);
	}
	/**
	 * 获得值并重新设置
	 * @param key
	 * @param function (oldValue) -> return newValue
	 */
	public V option(K key, Function<V, V> function) {
		V value = get(key);
		value = function.apply(value);
		this.map.put(key, value);
		return value;
	}
	
	public int size() {
		return map == null ? 0 : map.size();
	}
	/**
	 * 获取数据, 如果数据存在 则回调用callback函数. 如果数据不存在则直接返回null
	 * @param <R>
	 * @param key   KEY
	 * @param callback 存在数据回调
	 * @return
	 */
	public <R> R getCallback(K key, Function<V, R> callback) {
		V val = this.map.get(key);	
		if (val == null) {
			return null;
		}
		return callback.apply(val);
	}
	/**
	 * 获取数据, 如果数据存在 则回调用callback函数. 如果数据不存在则直接返回null
	 * @param <R>
	 * @param key   KEY
	 * @param callback 存在数据回调
	 * @param def 默认值, 如果是null 则直接使用这个默认结果.(不会进行存储)
	 * @return
	 */
	public <R> R getCallback(K key, Function<V, R> callback, R def) {
		V val = this.map.get(key);	
		if (val == null) {
			return def;
		}
		return callback.apply(val);
	}
	
	/**
	 * 获取数据, 如果数据存在 则回调用callback函数. 如果数据不存在则直接返回null
	 * @param <R>
	 * @param key   KEY
	 * @param callback 存在数据回调
	 * @param def 默认值, 如果是null 则直接使用这个创造默认值(不会进行存储)
	 * @return
	 */
	public <R> R getCallback(K key, Function<V, R> callback, Supplier<R> def) {
		V val = this.map.get(key);	
		if (val == null) {
			return def == null ? null : def.get();
		}
		return callback.apply(val);
	}
	
	/**
	 * 获取数据, 如果数据存在 则回调用callback函数. 如果数据不存在则直接不回调用
	 * @param <R>
	 * @param keys   所有KEY
	 * @param callback 存在数据回调
	 * @return
	 */
	public <R> Map<K,R> getAllCallback(Iterable<K> keys, Function<V, R> callback) {
		Map<K, R> result = new HashMap<>();
		for (K key : keys) {
			V val = this.map.get(key);	
			if (val != null) {
				R r = callback.apply(val);
				if (r != null) {
					result.put(key, r);
				}
			}
		}
		return result;
	}
	
	/**
	 * 只获取
	 * @param key
	 * @return
	 */
	public V getOnly(K key) {
		return this.map.get(key);
	}
}
