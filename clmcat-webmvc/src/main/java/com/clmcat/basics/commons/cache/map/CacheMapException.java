package com.clmcat.basics.commons.cache.map;

import java.util.Arrays;
import java.util.Collection;

public class CacheMapException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private Collection<Object> keys;

	public CacheMapException(Object key, Throwable e) {
		super("key=" + key, e);
		this.keys = Arrays.asList(key);
	}

	public CacheMapException(Collection<Object> keys, Throwable e) {
		super(e);
		this.keys = keys;
	}

	public Collection<Object> getKeys() {
		return keys;
	}
}
