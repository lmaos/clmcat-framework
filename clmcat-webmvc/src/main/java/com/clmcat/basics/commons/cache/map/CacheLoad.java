package com.clmcat.basics.commons.cache.map;

public interface CacheLoad<K, V> {

	V load(K key) throws Exception ;
}
