package com.clmcat.basics.commons.cache.map;

import java.util.Collection;
import java.util.Map;

public interface CacheLoadAll<K, V> {

	Map<K, V> load(Collection<K> keys) throws Exception;
}
