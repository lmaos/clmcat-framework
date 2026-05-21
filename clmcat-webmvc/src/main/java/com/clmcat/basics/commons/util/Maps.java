package com.clmcat.basics.commons.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Maps<K, V> {
	Map<K, V> map;
	private Supplier<V> supplier0;
	private Function<K, V> supplier1;

	public Maps(Map<K, V> map) {
		this.map = map;
	}

	public Maps(Map<K, V> map, Supplier<V> supplier0) {
		this.map = map;
		this.supplier0 = supplier0;
	}

	public Maps(Map<K, V> map, Function<K, V> supplier1) {
		this.map = map;
		this.supplier1 = supplier1;
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
	
	public void option(K key, Function<V, V> function) {
		V value = get(key);
		value = function.apply(value);
		this.map.put(key, value);
	}
}
