package com.clmcat.basics.commons.actives;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

import com.clmcat.basics.commons.util.Maps;

public class Counts<K> {
	
	private Maps<K, LongAdder> countMap = new Maps<K, LongAdder>(new HashMap<>(128), LongAdder::new);

	public static <K> Counts<K> of() {
		return new Counts<>();
	}
	public Counts<K> incr(K k) {
		countMap.get(k).increment();
		return this;
	}

	public Map<K, Long> getCounts() {
		Map<K, Long> map = new HashMap<K, Long>();
		countMap.forEach((k, v) -> {
			map.put(k, v.longValue());
		});
		return map;
	}

	public long getCount(K k) {
		return countMap.get(k).longValue();
	}
}
