package com.clmcat.basics.commons.cache.map;

public interface CacheDefault<K, V> {

	V load(K key);

}
